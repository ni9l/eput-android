package eput.android.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface KnownDeviceDao {
    @Query("SELECT * FROM saved_device WHERE device_id = :id")
    suspend fun getById(id: ByteArray): KnownDevice

    @Transaction
    @Query("SELECT * FROM saved_device")
    fun getDevicesWithCustomizations(): LiveData<List<DeviceWithCustomizations>>

    @Transaction
    @Query("SELECT * FROM saved_device WHERE device_id = :deviceId")
    fun getDeviceWithCustomization(deviceId: ByteArray): LiveData<DeviceWithCustomizations>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(device: KnownDevice)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(device: KnownDevice)

    @Delete
    suspend fun delete(device: KnownDevice)

    @Query("DELETE FROM saved_device")
    suspend fun deleteAll()
}