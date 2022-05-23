package eput.android.ui

import android.net.Uri
import android.nfc.NdefMessage
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile

class DumpActivity : AppCompatActivity() {
    private lateinit var messageToWrite: NdefMessage

    private val getFile = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
        if (it == null) {
            Toast.makeText(this, "No folder selected", Toast.LENGTH_SHORT).show()
        } else if (!saveDump(it)) {
            Toast.makeText(this, "No message or error writing to file", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val message = intent.getParcelableExtra<NdefMessage>(EXTRA_MESSAGE)
        if (intent.action == ACTION_SAVE && message != null) {
            messageToWrite = message
        } else {
            Toast.makeText(this, "No message to write", Toast.LENGTH_LONG).show()
            finish()
        }
        getFile.launch(null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EXTRA_MESSAGE, messageToWrite)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val message = savedInstanceState.getParcelable<NdefMessage>(EXTRA_MESSAGE)
        if (message != null) {
            messageToWrite = message
        }
    }

    private fun saveDump(path: Uri): Boolean {
        val records = messageToWrite.records
        if (records.size > 1) {
            val dataRecord = records[0]
            val metaRecord = records[1]
            val outputFolder = DocumentFile.fromTreeUri(this, path) ?: return false
            val metaFile = outputFolder.createFile(
                "application/octet-stream",
                "meta.bin") ?: return false
            val dataFile = outputFolder.createFile(
                "application/octet-stream",
                "data.bin") ?: return false
            contentResolver.openOutputStream(metaFile.uri)?.use {
                it.write(metaRecord.payload)
            }
            contentResolver.openOutputStream(dataFile.uri)?.use {
                it.write(dataRecord.payload)
            }
            return true
        } else {
            return false
        }
    }

    companion object {
        const val ACTION_SAVE: String = "eput.android.save"
        const val EXTRA_MESSAGE: String = "eput.android.save.message"
    }
}