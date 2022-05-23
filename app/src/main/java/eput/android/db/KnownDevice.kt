package eput.android.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_device")
data class KnownDevice(
    @PrimaryKey @ColumnInfo(name = "device_id") val deviceId: ByteArray,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "preferred_language") val preferredLanguage: String?
) {
    override fun equals(other: Any?): Boolean {
        return if (other is KnownDevice) {
            deviceId.contentEquals(other.deviceId)
                    && name == other.name
                    && type == other.type
                    && preferredLanguage == other.preferredLanguage
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return 31 * (31 * deviceId.contentHashCode() + type) + name.hashCode()
    }
}