package eput.android.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "preference_selection",
    foreignKeys = [ForeignKey(
        entity = KnownDevice::class,
        parentColumns = arrayOf("device_id"),
        childColumns = arrayOf("device_id"),
        onDelete = ForeignKey.CASCADE)],
    indices = [androidx.room.Index("device_id")]
)
data class CustomizedPreference(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long?,
    @ColumnInfo(name = "device_id") val deviceId: ByteArray,
    @ColumnInfo(name = "property_id") val propertyId: String,
    @ColumnInfo(name = "preference") val preference: String
) {
    override fun equals(other: Any?): Boolean {
        return if (other is CustomizedPreference) {
            id == other.id
                    && deviceId.contentEquals(other.deviceId)
                    && propertyId == other.propertyId
                    && preference == other.preference
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return 31 * (31 * (31 *
                id.hashCode() + deviceId.contentHashCode()) +
                propertyId.hashCode()) +
                preference.hashCode()
    }
}