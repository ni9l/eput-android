package eput.android.model

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import eput.android.db.Configuration
import eput.protocol.BaseItem
import eput.protocol.Device
import eput.protocol.Type
import java.time.Instant

class EPutDevice private constructor(
    private val device: Device,
    private val isCompressed: Boolean,
    private val metadataRecord: NdefRecord,
    private val dataType: ByteArray,
    val tagId: ByteArray
) {
    val name: String = device.name
    val type: Int = device.type
    val ids: ByteArray = device.ids
    val availableLanguages: List<String> = device.availableLanguages

    fun getProperties(): List<BaseItem> {
        return device.properties
    }

    fun toNdefMessage(fullMetadata: Boolean): NdefMessage {
        val dataPayload = device.serializeData()
        val data = NdefRecord(
            NdefRecord.TNF_ABSOLUTE_URI,
            dataType,
            ByteArray(1).apply { set(0, 0x02) },
            dataPayload
        )
        val writeFullMeta = ((device.type and 0b10000000) > 0) || fullMetadata
        val metadata = if (writeFullMeta) {
            metadataRecord
        } else {
            val metaPayload = if (isCompressed) {
                val originalMeta = Device.decompress(metadataRecord.payload)
                val payload = getShortenedMetadata(originalMeta)
                Device.compress(payload)
            } else {
                getShortenedMetadata(metadataRecord.payload)
            }
            NdefRecord(
                NdefRecord.TNF_ABSOLUTE_URI,
                metadataRecord.type,
                metadataRecord.id,
                metaPayload
            )
        }
        return NdefMessage(data, metadata)
    }

    fun exportConfiguration(name: String): Configuration {
        return Configuration(
            null,
            name,
            Instant.now(),
            ids,
            device.serializeData(),
            dataType
        )
    }

    fun withImportedConfiguration(configuration: Configuration): EPutDevice {
        if (configuration.deviceId.contentEquals(ids)) {
            return EPutDevice(
                Device.deserialize(metadataRecord.payload, configuration.data, isCompressed),
                isCompressed,
                metadataRecord,
                dataType,
                tagId
            )
        } else {
            throw IllegalArgumentException("Device IDs do not match, can't import configuration.")
        }
    }

    companion object {
        fun fromNdefMessage(message: NdefMessage, tagId: ByteArray): EPutDevice {
            if (message.records.size < 2) {
                throw IllegalArgumentException("NDEF message contains not enough records")
            } else {
                val dataRecord = message.records[0]
                val metaRecord = message.records[1]
                val isCompressed = metaRecord.toUri()
                    .getBooleanQueryParameter("zip", true)
                val device = Device
                    .deserialize(metaRecord.payload, dataRecord.payload, isCompressed)
                return EPutDevice(device, isCompressed, metaRecord, dataRecord.type, tagId)
            }
        }

        fun deviceIdsFromNdefMessage(message: NdefMessage): ByteArray {
            val metaRecord = message.records[1]
            val isCompressed = metaRecord.toUri()
                .getBooleanQueryParameter("zip", true)
            return Device.getDeviceIds(metaRecord.payload, isCompressed)
        }

        private fun getShortenedMetadata(originalMetadata: ByteArray): ByteArray {
            val infoBlock = originalMetadata.copyOfRange(0, 9)
            val payload = infoBlock.copyOf(infoBlock.size + 2)
            payload[9] = 0 // Clear device name
            payload[10] = Type.METADATA_TRUNCATED.toByte()
            return payload
        }
    }
}