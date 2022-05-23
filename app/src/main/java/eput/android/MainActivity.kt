package eput.android

import android.app.PendingIntent
import android.content.*
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import eput.android.databinding.ActivityMainBinding
import eput.android.ui.ConfigurationActivity
import eput.android.ui.SaveActivity
import eput.android.ui.WriteNfcActivity
import eput.android.ui.settings.SettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var nfcAdapter: NfcAdapter? = null
    private var currentSnackBar: Snackbar? = null
    private var started = false
    private val serviceBinder = MutableLiveData<DataService.DataBinder?>(null)
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            serviceBinder.value = service as DataService.DataBinder
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            serviceBinder.value = null
        }
    }
    private val saveTag = registerForActivityResult(SaveActivity.Save()) { success ->
        if (success) {
            serviceBinder.value?.clearDevice()
            finishAfterTransition()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Intent(this, DataService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        onIntent(intent)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        started = true
        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_write_nfc -> startWriteNFCActivity()
                R.id.action_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        binding.buttonDemo.setOnClickListener {
            loadDemoNdef("demo.json", byteArrayOf(0x0F, 0x0F))
        }
        binding.buttonDemoH.setOnClickListener {
            loadDemoNdef("heater.json", byteArrayOf(0x0F, 0x2F))
        }
        binding.buttonSocket.setOnClickListener {
            loadDemoNdef("socket_timer.json", byteArrayOf(0x0F, 0x4F))
        }
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, R.string.error_no_nfc, Toast.LENGTH_LONG).show()
        }
        setUpLastConfigCard()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    override fun onPause() {
        super.onPause()
        disableForegroundDispatch()
    }

    override fun onResume() {
        super.onResume()
        enableForegroundDispatch()
    }

    private fun enableForegroundDispatch() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val mutability = if (Build.VERSION.SDK_INT >= 31) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, mutability)
        val intentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            addDataScheme(getString(R.string.ndef_scheme))
            addDataAuthority(getString(R.string.ndef_host), null)
        }
        val actions = arrayOf(intentFilter)
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, actions ,null)
    }

    private fun disableForegroundDispatch() {
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { onIntent(it) }
    }

    private fun onIntent(intent: Intent) {
        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (rawMessages == null || tag == null) {
                Toast.makeText(this, "Tag or messages is null", Toast.LENGTH_LONG).show()
            } else {
                val id = tag.id
                val message = rawMessages[0] as NdefMessage
                if (started) {
                    currentSnackBar?.dismiss()
                    val snackBar = Snackbar
                        .make(
                            binding.coordinator,
                            R.string.text_found_device,
                            Snackbar.LENGTH_INDEFINITE
                        )
                        .setAction(R.string.action_configure) {
                            currentSnackBar = null
                            startConfigurationActivity(id, message, false)
                        }
                    currentSnackBar = snackBar
                    snackBar.show()
                } else {
                    startConfigurationActivity(id, message, true)
                }
            }
        }
    }

    private fun startWriteNFCActivity(): Boolean {
        val intent = Intent(this, WriteNfcActivity::class.java)
        startActivity(intent)
        return true
    }

    private fun loadDemoNdef(fileName: String, id: ByteArray) {
        resources.assets.open(fileName).reader().use {
            val message = createNdefMessageFromJson(this, it.readText())
            startConfigurationActivity(id, message, false)
        }
    }

    private fun startConfigurationActivity(id: ByteArray, message: NdefMessage, finish: Boolean) {
        val serviceIntent = Intent(this, DataService::class.java)
        serviceIntent.action = DataService.ACTION_NDEF
        serviceIntent.putExtra(DataService.EXTRA_TID, id)
        serviceIntent.putExtra(DataService.EXTRA_NDEF, message)
        startService(serviceIntent)
        val configIntent = Intent(this, ConfigurationActivity::class.java)
        startActivity(configIntent)
        if (finish) finish()
    }

    private fun setUpLastConfigCard() {
        serviceBinder.observe(this) {
            lifecycleScope.launch(Dispatchers.IO) {
                val lastConfig = it?.getLastConfiguration()
                if (lastConfig != null) {
                    withContext(Dispatchers.Main) {
                        val deviceName =
                            getDeviceDisplayName(this@MainActivity, lastConfig.first)
                        binding.lastConfigName.text =
                            String.format("%s: %s", deviceName, lastConfig.second.name)
                        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        val dateTime = ZonedDateTime.ofInstant(
                            lastConfig.second.timestamp,
                            ZoneId.systemDefault()
                        )
                        val timestamp = formatter.format(dateTime)
                        binding.lastConfigTime.text = timestamp
                        binding.buttonApplyLastConfig.setOnClickListener {
                            val ndefData = serviceBinder.value?.getNdefDataForSavedConfiguration(
                                lastConfig.second
                            ) ?: return@setOnClickListener
                            saveTag.launch(ndefData)
                        }
                        binding.lastConfigCard.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}