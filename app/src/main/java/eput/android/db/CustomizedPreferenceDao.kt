package eput.android.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CustomizedPreferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customizedPreference: CustomizedPreference)

    @Query("DELETE FROM preference_selection WHERE device_id = :deviceId")
    suspend fun delete(deviceId: ByteArray)

    @Query("DELETE FROM preference_selection")
    suspend fun deleteAll()
}