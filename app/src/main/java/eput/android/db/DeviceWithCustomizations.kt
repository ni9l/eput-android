package eput.android.db

import androidx.room.Embedded
import androidx.room.Relation

data class DeviceWithCustomizations(
    @Embedded val knownDevice: KnownDevice,
    @Relation(parentColumn = "device_id", entityColumn = "device_id")
    val customizedPreferences: List<CustomizedPreference>,
    @Relation(parentColumn = "device_id", entityColumn = "device_id")
    val configurations: List<Configuration>
)