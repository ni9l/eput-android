package eput.android.ui

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import eput.android.R
import eput.android.databinding.ActivitySaveBinding
import eput.android.model.EPutDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.IllegalArgumentException

class SaveActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySaveBinding
    private var nfcAdapter: NfcAdapter? = null
    private var dataToWrite: NdefData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setResult(RESULT_CANCELED)
        if (intent.action == ACTION_SAVE) {
            dataToWrite = NdefData(
                intent.getBooleanExtra(EXTRA_SAVE_SINGLE, true),
                intent.getParcelableExtra(EXTRA_MESSAGE) ?: return,
                intent.getBooleanExtra(EXTRA_CHECK, true),
                intent.getBooleanExtra(EXTRA_DATA_ONLY, true),
                intent.getByteArrayExtra(EXTRA_ID)
            )
        }
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, R.string.error_no_nfc, Toast.LENGTH_LONG).show()
            finishAfterTransition()
        }
    }

    override fun onPause() {
        super.onPause()
        disableForegroundDispatch()
    }

    override fun onResume() {
        super.onResume()
        enableForegroundDispatch()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val data = dataToWrite
        if (data != null) {
            outState.putBoolean(EXTRA_SAVE_SINGLE, data.saveSingle)
            outState.putParcelable(EXTRA_MESSAGE, data.message)
            outState.putBoolean(EXTRA_CHECK, data.checkReceiver)
            outState.putByteArray(EXTRA_ID, data.receiverTagId)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (dataToWrite == null) {
            dataToWrite = NdefData(
                savedInstanceState.getBoolean(EXTRA_SAVE_SINGLE, true),
                savedInstanceState.getParcelable(EXTRA_MESSAGE) ?: return,
                savedInstanceState.getBoolean(EXTRA_CHECK, true),
                savedInstanceState.getBoolean(EXTRA_DATA_ONLY, true),
                savedInstanceState.getByteArray(EXTRA_ID)
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val tagFromIntent: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) ?: return
        binding.textStatus.setText(R.string.text_found_tag)
        val data = dataToWrite
        if (data != null) {
            if (data.checkReceiver && !checkTag(data, tagFromIntent, intent)) {
                Toast.makeText(
                    this,
                    R.string.error_different_tag,
                    Toast.LENGTH_LONG
                ).show()
                finishAfterTransition()
            }
            val message = if (data.dataOnly) {
                val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                if (rawMessages != null) {
                    val deviceMessage = rawMessages[0] as NdefMessage
                    combineMessage(deviceMessage, data.message)
                } else {
                    throw IllegalArgumentException("NDEF message from intent is empty")
                }
            } else {
                data.message
            }
            lifecycleScope.launch {
                writeToTag(tagFromIntent, message)
            }.invokeOnCompletion {
                if (it == null) {
                    binding.textStatus.setText(R.string.text_wrote_tag)
                    setResult(RESULT_OK)
                } else {
                    binding.textStatus.setText(R.string.error_writing_tag)
                    setResult(RESULT_CANCELED)
                }
                if (data.saveSingle) {
                    disableForegroundDispatch()
                    Handler(Looper.getMainLooper()).postDelayed({
                        finishAfterTransition()
                    }, 1000)
                }
            }
        }
    }

    private fun combineMessage(deviceMessage: NdefMessage, dataMessage: NdefMessage): NdefMessage {
        val metaRecord = deviceMessage.records[1]
        val dataRecord = dataMessage.records[0]
        return NdefMessage(dataRecord, metaRecord)
    }

    private fun checkTag(data: NdefData, tag: Tag, intent: Intent): Boolean {
        if (data.saveSingle && data.receiverTagId != null) {
            return tag.id.contentEquals(data.receiverTagId)
        } else {
            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (rawMessages != null) {
                val currentId = EPutDevice.deviceIdsFromNdefMessage(rawMessages[0] as NdefMessage)
                val dataId = EPutDevice.deviceIdsFromNdefMessage(data.message)
                return dataId.contentEquals(currentId)
            }
        }
        return false
    }

    private fun enableForegroundDispatch() {
        val intent = Intent(this, SaveActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val mutability = if (Build.VERSION.SDK_INT >= 31) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, mutability)
        val actions = arrayOf(IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))
        val techList = arrayOf(arrayOf<String>(Ndef::class.java.name))
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, actions, techList)
    }

    private fun disableForegroundDispatch() {
        nfcAdapter?.disableForegroundDispatch(this)
    }

    private suspend fun writeToTag(tag: Tag, message: NdefMessage) {
        if (Ndef::class.java.canonicalName in tag.techList) {
            Ndef.get(tag).use {
                withContext(Dispatchers.IO) {
                    it.connect()
                    if (it.isConnected) {
                        try {
                            it.writeNdefMessage(message)
                        } catch (e: IOException) {
                            Result.failure<Unit>(e)
                        }
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@SaveActivity,
                    R.string.error_unsupported_tag,
                    Toast.LENGTH_LONG
                ).show()
                Result.failure<Unit>(UnsupportedOperationException())
            }
        }
    }

    companion object {
        private const val ACTION_SAVE: String = "eput.android.save"
        private const val EXTRA_SAVE_SINGLE: String = "eput.android.save.save_single"
        private const val EXTRA_MESSAGE: String = "eput.android.save.message"
        private const val EXTRA_CHECK: String = "eput.android.save.check"
        private const val EXTRA_ID: String = "eput.android.save.id"
        private const val EXTRA_DATA_ONLY: String = "eput.android.save.data_only"
    }

    class Save : ActivityResultContract<NdefData, Boolean>() {
        override fun createIntent(context: Context, input: NdefData): Intent {
            return Intent(ACTION_SAVE, null, context, SaveActivity::class.java).apply {
                putExtra(EXTRA_SAVE_SINGLE, input.saveSingle)
                putExtra(EXTRA_MESSAGE, input.message)
                putExtra(EXTRA_CHECK, input.checkReceiver)
                putExtra(EXTRA_DATA_ONLY, input.dataOnly)
                if (input.receiverTagId != null) {
                    putExtra(EXTRA_ID, input.receiverTagId)
                }
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return resultCode == Activity.RESULT_OK
        }
    }

    data class NdefData(
        val saveSingle: Boolean,
        val message: NdefMessage,
        val checkReceiver: Boolean,
        val dataOnly: Boolean,
        val receiverTagId: ByteArray?
    ) {
        override fun equals(other: Any?): Boolean {
            return if (other is NdefData) {
                other.saveSingle == saveSingle
                        && other.message == message
                        && other.checkReceiver == checkReceiver
                        && other.dataOnly == dataOnly
                        && other.receiverTagId.contentEquals(receiverTagId)
            } else {
                false
            }
        }

        override fun hashCode(): Int {
            return 31 * (31 * (31 * ( 31 * (
                    saveSingle.hashCode() +
                    message.hashCode()) +
                    dataOnly.hashCode()) +
                    checkReceiver.hashCode()) +
                    receiverTagId.contentHashCode())
        }
    }
}