package eput.android.ui.settings

import androidx.annotation.WorkerThread
import androidx.lifecycle.*
import eput.android.DataService
import eput.android.base64ToBytes
import eput.android.bytesToBase64
import eput.android.db.Configuration
import eput.android.db.DeviceWithCustomizations
import eput.android.db.KnownDevice
import eput.android.ui.SaveActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.lang.IllegalArgumentException
import java.time.Instant

class SettingsViewModel : ViewModel() {
    private val serviceBinder = MutableLiveData<DataService.DataBinder?>(null)

    fun setBinder(binder: DataService.DataBinder) {
        serviceBinder.value = binder
    }

    fun clearBinder() {
        serviceBinder.value = null
    }

    var selectedDeviceIndex = -1

    fun getDevices(): LiveData<List<DeviceWithCustomizations>> {
        return Transformations.switchMap(serviceBinder) { binder ->
            binder?.getDevicesWithCustomizations() ?: MutableLiveData(listOf())
        }
    }

    fun updateKnownDevice(knownDevice: KnownDevice) {
        viewModelScope.launch(Dispatchers.IO) {
            serviceBinder.value?.updateKnownDevice(knownDevice)
        }
    }

    fun deleteKnownDeviceAndRelations(knownDevice: KnownDevice) {
        viewModelScope.launch(Dispatchers.IO) {
            serviceBinder.value?.deleteKnownDevice(knownDevice)
        }
    }

    fun clearCustomizedPreferences(deviceId: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            serviceBinder.value?.clearCustomizedPreferences(deviceId)
        }
    }

    fun clearCustomizedPreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            serviceBinder.value?.clearCustomizedPreferences()
        }
    }

    fun clearConfigurations(deviceId: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            serviceBinder.value?.deleteConfigurations(deviceId)
        }
    }

    fun deleteConfigurations(configurations: List<Configuration>) {
        viewModelScope.launch(Dispatchers.IO) {
            serviceBinder.value?.deleteConfigurations(configurations)
        }
    }

    fun getNdefDataForSavedConfiguration(configuration: Configuration): SaveActivity.NdefData? {
        return serviceBinder.value?.getNdefDataForSavedConfiguration(configuration)
    }

    fun configurationToJson(configuration: Configuration): String {
        val json = JSONObject()
        json.put("name", configuration.name)
        json.put("timestamp", configuration.timestamp.toEpochMilli())
        json.put("device_id", bytesToBase64(configuration.deviceId))
        json.put("data", bytesToBase64(configuration.data))
        json.put("type", bytesToBase64(configuration.type))
        return json.toString()
    }

    @WorkerThread
    suspend fun importConfiguration(configurationJson: String): Boolean {
        return try {
            val json = JSONObject(configurationJson)
            val name = json.getString("name")
            val timestamp = Instant.ofEpochMilli(json.getLong("timestamp"))
            val deviceId = base64ToBytes(json.getString("device_id"))
            val data = base64ToBytes(json.getString("data"))
            val type = base64ToBytes(json.getString("type"))
            val configuration = Configuration(null, name, timestamp, deviceId, data, type)
            serviceBinder.value?.insertConfiguration(configuration)
            true
        } catch (e: JSONException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}