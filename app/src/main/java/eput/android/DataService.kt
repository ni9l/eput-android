package eput.android

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.os.Binder
import android.os.IBinder
import androidx.annotation.WorkerThread
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.preference.PreferenceManager
import eput.android.db.*
import eput.android.model.EPutDevice
import eput.android.ui.SaveActivity
import kotlinx.coroutines.*
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

class DataService : Service() {
    private val binder: DataBinder = DataBinder()
    private val coroutineScope = CoroutineScope(Job() + Dispatchers.IO)
    private val systemLanguages = LocaleListCompat.getDefault()
    private val error = MutableLiveData<String?>(null)
    private val device = MutableLiveData<EPutDevice?>(null)
    private val customizations = Transformations.switchMap(device) { dev ->
        return@switchMap if (dev == null) {
            null
        } else {
            database.knownDeviceDao().getDeviceWithCustomization(dev.ids)
        }
    }
    private val deviceWithCustomizations = object
        : MediatorLiveData<DeviceSet?>() {
        private var device: EPutDevice? = null
        private var uiConf: DeviceWithCustomizations? = null

        init {
            addSource(this@DataService.device) {
                device = it
                val conf = uiConf
                if (it != null && conf != null) {
                    val lang = getUiLanguage(it, conf, systemLanguages)
                    value = DeviceSet(it, conf, lang)
                } else if (value != null) {
                    value = null
                    uiConf = null
                }
            }
            addSource(customizations) {
                uiConf = it
                val dev = device
                if (it != null && dev != null) {
                    val lang = getUiLanguage(dev, it, systemLanguages)
                    value = DeviceSet(dev, it, lang)
                } else if (value != null) {
                    value = null
                }
            }
        }
    }
    private lateinit var database: AppDatabase
    private lateinit var sharedPreferences: SharedPreferences

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (it.action == ACTION_NDEF) {
                val ndefMessage = it.getParcelableExtra<NdefMessage>(EXTRA_NDEF)
                val tagId = it.getByteArrayExtra(EXTRA_TID)
                if (
                    ndefMessage != null &&
                    tagId != null &&
                    !tagId.contentEquals(device.value?.tagId)
                ) {
                    coroutineScope.launch {
                        createDevice(ndefMessage, tagId)
                    }
                }
            }
        }
        return START_REDELIVER_INTENT
    }

    override fun onCreate() {
        database = AppDatabase.getInstance(this)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        clear()
        coroutineScope.cancel()
    }

    private fun getUiLanguage(
        dev: EPutDevice,
        customizations: DeviceWithCustomizations,
        systemLanguages: LocaleListCompat
    ): String? {
        if (!isTranslationEnabled()) {
            return null
        }
        val availableLanguages = dev.availableLanguages
        if (availableLanguages.isEmpty()) {
            return null
        }
        val preferredLanguage = customizations.knownDevice.preferredLanguage
        if (preferredLanguage != null && availableLanguages.contains(preferredLanguage)) {
            return preferredLanguage
        }
        for (i in 0 until systemLanguages.size()) {
            val lang = systemLanguages[i].language
            if (availableLanguages.contains(lang)) {
                return lang
            }
        }
        return availableLanguages[0]
    }

    private suspend fun createDevice(message: NdefMessage, tagId: ByteArray) {
        withContext(Dispatchers.IO) {
            try {
                val dev = EPutDevice.fromNdefMessage(message, tagId)
                val savedDev = KnownDevice(dev.ids, dev.name, dev.type, null)
                database.knownDeviceDao().insert(savedDev)
                withContext(Dispatchers.Main) {
                    device.value = dev
                }
            } catch (e: IllegalArgumentException) {
                withContext(Dispatchers.Main) {
                    error.value = getString(R.string.error_invalid_message)
                }
            } catch (e: IllegalStateException) {
                withContext(Dispatchers.Main) {
                    error.value = getString(R.string.error_message_truncated)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    error.value = getString(
                        R.string.error_unknown,
                        e::class.simpleName,
                        e.message
                    )
                }
            }
        }
    }

    private fun clear() {
        device.value = null
        coroutineScope.coroutineContext.cancelChildren()
        error.value = null
    }

    private suspend fun saveConfiguration(name: String) {
        device.value?.let {
            withContext(Dispatchers.IO) {
                val config = it.exportConfiguration(name)
                database.configurationDao().insert(config)
            }
        }
    }

    private fun loadConfiguration(configuration: Configuration) {
        sharedPreferences.edit().putLong("last_config_id", configuration.id ?: -1).apply()
        device.value?.let {
            val newDevice = it.withImportedConfiguration(configuration)
            device.value = newDevice
        }
    }

    private fun isTranslationEnabled(): Boolean {
        return sharedPreferences.getBoolean(
            getString(R.string.key_translations),
            true
        )
    }

    private fun isForceOverwrite(): Boolean {
        return sharedPreferences.getBoolean(
            getString(R.string.key_force_overwrite),
            false
        )
    }

    private fun writeFullMetadata(): Boolean {
        return sharedPreferences.getBoolean(
            getString(R.string.key_write_full_meta),
            false
        )
    }

    private fun hideDependencies(): Boolean {
        return sharedPreferences.getBoolean(
            getString(R.string.key_hide_dependencies),
            false
        )
    }

    companion object {
        const val ACTION_NDEF = "eput.android.DataService.ndef"
        const val EXTRA_NDEF = "eput.android.DataService.extra.ndef"
        const val EXTRA_TID = "eput.android.DataService.extra.tid"
    }

    class DeviceSet(
        val device: EPutDevice,
        val customizations: DeviceWithCustomizations,
        val uiLanguage: String?
    )

    inner class DataBinder : Binder() {
        @WorkerThread
        suspend fun updateKnownDevice(knownDevice: KnownDevice) {
            database.knownDeviceDao().update(knownDevice)
        }

        @WorkerThread
        suspend fun deleteKnownDevice(knownDevice: KnownDevice) {
            database.knownDeviceDao().delete(knownDevice)
        }

        @WorkerThread
        suspend fun clearCustomizedPreferences(deviceId: ByteArray) {
            database.preferenceSelectionDao().delete(deviceId)
        }

        @WorkerThread
        suspend fun clearCustomizedPreferences() {
            database.preferenceSelectionDao().deleteAll()
        }

        @WorkerThread
        suspend fun updatePreferenceSelection(customizedPreference: CustomizedPreference) {
            database.preferenceSelectionDao().insert(customizedPreference)
        }

        suspend fun saveDeviceConfiguration(name: String) {
            saveConfiguration(name)
        }

        fun loadDeviceConfiguration(configuration: Configuration) {
            loadConfiguration(configuration)
        }

        fun getStoredConfigurationsForDevice(id: ByteArray): LiveData<List<Configuration>> {
            return database.configurationDao().getByDeviceId(id)
        }

        @WorkerThread
        suspend fun deleteConfigurations(configurations: List<Configuration>) {
            database.configurationDao().delete(configurations)
        }

        @WorkerThread
        suspend fun deleteConfigurations(deviceId: ByteArray) {
            database.configurationDao().delete(deviceId)
        }

        @WorkerThread
        suspend fun insertConfiguration(configuration: Configuration) {
            database.configurationDao().insert(configuration)
        }

        @WorkerThread
        suspend fun getLastConfiguration(): Pair<KnownDevice, Configuration>? {
            val id = sharedPreferences.getLong("last_config_id", -1)
            return if (id > -1) {
                val config = database.configurationDao().getById(id)
                val device = database.knownDeviceDao().getById(config.deviceId)
                device to config
            } else {
                null
            }
        }

        fun getDevicesWithCustomizations(): LiveData<List<DeviceWithCustomizations>> {
            return database.knownDeviceDao().getDevicesWithCustomizations()
        }

        fun getDeviceWithCustomizations(): LiveData<DeviceSet?> {
            return deviceWithCustomizations
        }

        fun getDeviceAsNdefData(saveSingle: Boolean): SaveActivity.NdefData? {
            val dev = device.value ?: return null
            return SaveActivity.NdefData(
                saveSingle,
                dev.toNdefMessage(writeFullMetadata()),
                !isForceOverwrite(),
                false,
                dev.tagId
            )
        }

        fun getNdefDataForSavedConfiguration(
            savedConfiguration: Configuration
        ): SaveActivity.NdefData {
            val dataRecord = NdefRecord(
                NdefRecord.TNF_ABSOLUTE_URI,
                savedConfiguration.type,
                ByteArray(1).apply { set(0, 0x02) },
                savedConfiguration.data
            )
            return SaveActivity.NdefData(
                false,
                NdefMessage(dataRecord),
                checkReceiver = false,
                dataOnly = true,
                receiverTagId = null
            )
        }

        fun clearDevice() {
            clear()
        }

        fun getError(): LiveData<String?> {
            return error
        }

        fun clearError() {
            error.value = null
        }

        fun getHideDependencies(): Boolean {
            return hideDependencies()
        }
    }
}