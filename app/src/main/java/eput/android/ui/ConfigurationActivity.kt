package eput.android.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import eput.android.DataService
import eput.android.R
import eput.android.databinding.ActivityConfigurationBinding
import eput.android.db.Configuration
import eput.android.getDeviceDisplayName
import eput.android.instantiatePreference
import eput.android.ui.settings.SettingsActivity
import eput.android.ui.widgets.Preference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ConfigurationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfigurationBinding
    private val serviceBinder = MutableLiveData<DataService.DataBinder?>(null)
    private val device = Transformations.switchMap(serviceBinder) { binder ->
        binder?.getDeviceWithCustomizations()
            ?: MutableLiveData<DataService.DeviceSet?>(null)
    }
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as DataService.DataBinder
            serviceBinder.value = binder
            binder.getError().observe(this@ConfigurationActivity) { msg ->
                msg?.let {
                    onError(it)
                    binder.clearError()
                }
            }
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
        binding = ActivityConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_apply -> {
                    val data = serviceBinder.value?.getDeviceAsNdefData(true)
                    saveTag.launch(data)
                    true
                }
                R.id.action_select_language -> {
                    onSelectLanguage()
                    true
                }
                R.id.action_export -> {
                    onExport()
                    true
                }
                R.id.action_import -> {
                    onImport()
                    true
                }
                R.id.action_dump -> {
                    val data = serviceBinder.value?.getDeviceAsNdefData(true)
                    val intent = Intent(this, DumpActivity::class.java)
                    intent.action = DumpActivity.ACTION_SAVE
                    intent.putExtra(DumpActivity.EXTRA_MESSAGE, data?.message)
                    startActivity(intent)
                    true
                }
                R.id.action_configure_layout -> {
                    val intent = Intent(this, UiEditActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        device.observe(this) {
            it?.let {
                setupConfigurationUi(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_quit)
            .setMessage(R.string.dialog_desc_quit)
            .setPositiveButton(R.string.button_ok) { _, _ ->
                serviceBinder.value?.clearDevice()
                super.onBackPressed()
            }
            .setNegativeButton(R.string.button_cancel) { _, _ ->
                // Ignore
            }
            .show()
    }

    private fun onError(message: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.error_read_title)
            .setMessage(message)
            .setPositiveButton(R.string.button_ok) { _, _ ->
                finishAfterTransition()
            }
            .show()
    }

    private fun onExport() {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_title_export_name)
            .setView(R.layout.dialog_text_input)
            .setNegativeButton(R.string.button_cancel, null)
            .create()
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.button_ok))
        { _, _ ->
            val textInput = dialog.findViewById<TextInputEditText>(R.id.input)
            val name = textInput?.text.toString()
            lifecycleScope.launch {
                serviceBinder.value?.saveDeviceConfiguration(name)
            }
        }
        dialog.show()
    }

    private fun onImport() {
        val devId = device.value?.device?.ids
        val service = serviceBinder.value
        if (devId != null && service != null) {
            val dialog = MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_import)
                .setNegativeButton(R.string.button_cancel, null)
            val configLD = service.getStoredConfigurationsForDevice(devId)
            val observer = object : Observer<List<Configuration>> {
                override fun onChanged(configs: List<Configuration>?) {
                    if (configs != null) {
                        val items = configs.map(Configuration::getDisplayName).toTypedArray()
                        dialog.setItems(items) { _, which ->
                            val selected = configs[which]
                            service.loadDeviceConfiguration(selected)
                        }
                        dialog.show()
                        configLD.removeObserver(this)
                    }
                }
            }
            configLD.observe(this, observer)
        }
    }

    private fun onSelectLanguage() {
        val service = serviceBinder.value
        val deviceSet = device.value
        if (service != null && deviceSet != null) {
            val items = deviceSet.device.availableLanguages.map {
                Locale.forLanguageTag(it).displayLanguage
            }.toTypedArray()
            val initial = deviceSet.device.availableLanguages.indexOf(deviceSet.uiLanguage)
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_select_language)
                .setSingleChoiceItems(items, initial) { dialog, which ->
                    val selected = deviceSet.device.availableLanguages[which]
                    if (selected != deviceSet.uiLanguage) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            service.updateKnownDevice(
                                deviceSet.customizations.knownDevice.copy(preferredLanguage = selected)
                            )
                        }
                        dialog.dismiss()
                    }
                }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
        }
    }

    private fun setupConfigurationUi(
        deviceSet: DataService.DeviceSet
    ) {
        binding.topAppBar.title =
            getDeviceDisplayName(this, deviceSet.customizations.knownDevice)
        val root = binding.container
        val props = deviceSet.device.getProperties()
        clearPreferences(root)
        props.forEach {
            instantiatePreference(
                it,
                deviceSet.customizations.customizedPreferences,
                root,
                deviceSet.uiLanguage,
                serviceBinder.value?.getHideDependencies() ?: false,
                supportFragmentManager
            ) {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.dialog_title_language_changed)
                    .setMessage(R.string.dialog_desc_language_changed)
                    .setPositiveButton(R.string.button_ok, null)
                    .show()
            }
        }
        root.children.forEach { if (it is Preference) it.onSetUpComplete() }
    }

    private fun clearPreferences(root: ViewGroup) {
        root.children.forEach {
            if (it is Preference) {
                it.clearProperty()
            }
        }
        root.removeAllViews()
    }
}