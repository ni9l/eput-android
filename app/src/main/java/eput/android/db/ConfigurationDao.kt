package eput.android.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ConfigurationDao {
    @Query("SELECT * FROM configuration")
    fun getAll(): LiveData<List<Configuration>>

    @Query("SELECT * FROM configuration WHERE id = :id")
    suspend fun getById(id: Long): Configuration

    @Query("SELECT * FROM configuration WHERE device_id = :deviceId")
    fun getByDeviceId(deviceId: ByteArray): LiveData<List<Configuration>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(configuration: Configuration)

    @Delete
    suspend fun delete(configurations: List<Configuration>)

    @Query("DELETE FROM configuration WHERE device_id = :deviceId")
    suspend fun delete(deviceId: ByteArray)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(configurations: Configuration)
}