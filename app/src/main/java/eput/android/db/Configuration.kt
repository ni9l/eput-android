package eput.android.db

import androidx.room.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Entity(
    tableName = "configuration",
    foreignKeys = [ForeignKey(
        entity = KnownDevice::class,
        parentColumns = arrayOf("device_id"),
        childColumns = arrayOf("device_id"),
        onDelete = ForeignKey.CASCADE)],
    indices = [Index("device_id")]
)
data class Configuration(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "timestamp") val timestamp: Instant,
    @ColumnInfo(name = "device_id") val deviceId: ByteArray,
    @ColumnInfo(name = "data") val data: ByteArray,
    @ColumnInfo(name = "type") val type: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        return if (other is Configuration) {
            (other.id == id
                    && other.name == name
                    && other.timestamp == timestamp
                    && other.deviceId.contentEquals(deviceId)
                    && other.data.contentEquals(data)
                    && other.type.contentEquals(type))
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return 31 * (31 * (31 * (31 * (31 *
                id.hashCode() + name.hashCode()) +
                timestamp.hashCode()) +
                deviceId.contentHashCode()) +
                data.contentHashCode()) +
                type.contentHashCode()
    }

    fun getDisplayName(): String {
        val time = timestamp.atZone(zone)
        return "$name (${time.format(formatter)})"
    }

    companion object {
        private val zone = ZoneId.systemDefault()
        private val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
    }
}