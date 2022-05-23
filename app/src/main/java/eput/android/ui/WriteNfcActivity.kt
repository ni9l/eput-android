package eput.android.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import eput.android.R
import eput.android.createNdefMessageFromJson
import eput.android.databinding.ActivityWriteNfcBinding
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class WriteNfcActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWriteNfcBinding
    private var jsonDataToWrite: String? = null
    private val getFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) {
            val text = contentResolver.openInputStream(it)?.let { stream ->
                InputStreamReader(stream).use { reader ->
                    return@use reader.readText()
                }
            }
            if (text == null) {
                jsonDataToWrite = null
                Toast.makeText(this, R.string.error_reading_file, Toast.LENGTH_LONG).show()
                binding.textFile.text = getString(R.string.text_file, "None")
            } else {
                jsonDataToWrite = text
                binding.textFile.text = getString(R.string.text_file, it.toString())
            }
        }
    }
    private val saveTag = registerForActivityResult(SaveActivity.Save()) { success ->
        Toast.makeText(
            this,
            if (success) R.string.text_wrote_tag else R.string.error_writing_tag,
            Toast.LENGTH_SHORT
        ).show()
        finishAfterTransition()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteNfcBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.textFile.text = getString(R.string.text_file, "None")
        binding.buttonDownload.setOnClickListener {
            val url = URL(getString(R.string.eput_service))
            val urlConnection: HttpsURLConnection = url.openConnection() as HttpsURLConnection
            try {
                InputStreamReader(urlConnection.inputStream).use { reader ->
                    val text = StringBuilder()
                    reader.forEachLine {
                        if (!it.startsWith("#")) text.append(it + "\n")
                    }
                    jsonDataToWrite = text.toString()
                    binding.textFile.text = getString(R.string.text_file, url.toString())
                }
            } finally {
                urlConnection.disconnect()
            }
        }
        binding.buttonSelectFile.setOnClickListener {
            getFile.launch(arrayOf("application/json", "text/plain"))
        }
        binding.buttonWrite.setOnClickListener {
            val json = jsonDataToWrite
            if (json != null) {
                val message = createNdefMessageFromJson(this, json)
                val data = SaveActivity.NdefData(
                    false,
                    message,
                    checkReceiver = false,
                    dataOnly = false,
                    receiverTagId = null
                )
                saveTag.launch(data)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("current_json_data", jsonDataToWrite)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (jsonDataToWrite == null) {
            jsonDataToWrite = savedInstanceState.getString("current_json_data")
        }
    }
}