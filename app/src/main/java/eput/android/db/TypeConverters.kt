package eput.android.db

import androidx.room.TypeConverter
import java.time.Instant

class TypeConverters {
    @TypeConverter
    fun fromTimestamp(value: Long): Instant {
        return Instant.ofEpochMilli(value)
    }

    @TypeConverter
    fun instantToTimestamp(instant: Instant): Long {
        return instant.toEpochMilli()
    }
}