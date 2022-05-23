package eput.android

import android.content.Context
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.util.Base64
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import eput.android.db.CustomizedPreference
import eput.android.db.KnownDevice
import eput.android.ui.widgets.*
import eput.protocol.BaseItem
import eput.protocol.DeviceType
import eput.protocol.NamedItem
import eput.protocol.Type
import org.json.JSONException
import org.json.JSONObject

fun <T> List<T>.copy(): List<T> {
    return mutableListOf<T>().apply { addAll(this@copy) }
}

fun bytesToBase64(bytes: ByteArray): String {
    return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP)
}

fun base64ToBytes(string: String): ByteArray {
    return Base64.decode(string, Base64.URL_SAFE)
}

const val PREF_MOD_DIVIDER = "mod_div"
const val PREF_MOD_HEADER = "mod_header"
const val PREF_SEL_RADIO = "sel_radio"
const val PREF_SEL_DROPDOWN = "sel_dropdown"
const val PREF_SEL_CHECK = "sel_check"
const val PREF_BOOL_SWITCH = "bool_switch"
const val PREF_ARRAY_FRAME = "array_frame"
const val PREF_NUM_TEXT = "num_text"
const val PREF_NUM_SLIDER = "num_slider"
const val PREF_NUMBER_LIST_DROPDOWN = "number_list_dropdown"
const val PREF_NUMBER_LIST_BUTTONS = "number_list_buttons"
const val PREF_DATE_PICKER = "date_picker"
const val PREF_DATE_TIME_PICKER = "date_time_picker"
const val PREF_ZONED_DATE_TIME_PICKER = "zoned_date_time_picker"
const val PREF_DATE_RANGE_PICKER = "date_range_picker"
const val PREF_DATE_TIME_RANGE_PICKER = "date_time_range_picker"
const val PREF_TIME_PICKER = "time_picker"
const val PREF_TIME_RANGE_PICKER = "time_range_picker"
const val PREF_STR_TEXT = "str_text"
const val PREF_STR_COLOR = "str_color"
const val PREF_LANGUAGE = "language"

fun setPreferenceProperty(
    preference: Preference,
    property: BaseItem,
    uiConfig: List<CustomizedPreference>,
    displayLanguage: String?
) {
    if (preference is ArrayPreference) {
        preference.apply {
            setUiConfig(uiConfig)
            setProperty(property, displayLanguage)
        }
    } else {
        preference.setProperty(property, displayLanguage)
    }
}

fun getPreferenceIdForProperty(property: BaseItem, uiConfig: List<CustomizedPreference>): String {
    val propId = if (property is NamedItem) property.id else null
    val config = uiConfig.find { it.propertyId == propId }
    return config?.preference ?: getDefaultPreferenceIdForProperty(property)
}

fun getDefaultPreferenceIdForProperty(property: BaseItem): String {
    return when (property.type) {
        Type.DIVIDER -> PREF_MOD_DIVIDER
        Type.HEADER -> PREF_MOD_HEADER
        Type.ONE_OUT_OF_M -> PREF_SEL_RADIO
        Type.N_OUT_OF_M -> PREF_SEL_CHECK
        Type.BOOL -> PREF_BOOL_SWITCH
        Type.ARRAY -> PREF_ARRAY_FRAME
        Type.UINT8_T,
        Type.UINT16_T,
        Type.UINT32_T,
        Type.UINT64_T,
        Type.INT8_T,
        Type.INT16_T,
        Type.INT32_T,
        Type.INT64_T,
        Type.FLOAT,
        Type.DOUBLE -> PREF_NUM_TEXT
        Type.NUMBER_LIST_INT,
        Type.NUMBER_LIST_DOUBLE -> PREF_NUMBER_LIST_DROPDOWN
        Type.DATE -> PREF_DATE_PICKER
        Type.DATE_TIME -> PREF_DATE_TIME_PICKER
        Type.ZONED_DATE_TIME -> PREF_ZONED_DATE_TIME_PICKER
        Type.DATE_RANGE -> PREF_DATE_RANGE_PICKER
        Type.DATE_TIME_RANGE -> PREF_DATE_TIME_RANGE_PICKER
        Type.TIME -> PREF_TIME_PICKER
        Type.TIME_RANGE -> PREF_TIME_RANGE_PICKER
        Type.STR_ASCII,
        Type.STR_UTF8,
        Type.STR_EMAIL,
        Type.STR_PHONE,
        Type.STR_URI,
        Type.STR_PASSWORD -> PREF_STR_TEXT
        Type.LANGUAGE -> PREF_LANGUAGE
        else -> ""
    }
}

fun getAvailablePreferencesForProperty(property: BaseItem): List<String> {
    return when (property.type) {
        Type.DIVIDER -> listOf(PREF_MOD_DIVIDER)
        Type.HEADER -> listOf(PREF_MOD_HEADER)
        Type.ONE_OUT_OF_M -> listOf(PREF_SEL_RADIO, PREF_SEL_DROPDOWN)
        Type.N_OUT_OF_M -> listOf(PREF_SEL_CHECK)
        Type.BOOL -> listOf(PREF_BOOL_SWITCH)
        Type.ARRAY -> listOf(PREF_ARRAY_FRAME)
        Type.UINT8_T,
        Type.UINT16_T,
        Type.UINT32_T,
        Type.UINT64_T,
        Type.INT8_T,
        Type.INT16_T,
        Type.INT32_T,
        Type.INT64_T -> listOf(PREF_NUM_TEXT, PREF_NUM_SLIDER)
        Type.FLOAT,
        Type.DOUBLE -> listOf(PREF_NUM_TEXT, PREF_NUM_SLIDER)
        Type.NUMBER_LIST_INT,
        Type.NUMBER_LIST_DOUBLE -> listOf(PREF_NUMBER_LIST_DROPDOWN, PREF_NUMBER_LIST_BUTTONS)
        Type.DATE -> listOf(PREF_DATE_PICKER)
        Type.DATE_TIME -> listOf(PREF_DATE_TIME_PICKER)
        Type.TIME -> listOf(PREF_TIME_PICKER)
        Type.ZONED_DATE_TIME -> listOf(PREF_ZONED_DATE_TIME_PICKER)
        Type.DATE_RANGE -> listOf(PREF_DATE_RANGE_PICKER)
        Type.DATE_TIME_RANGE -> listOf(PREF_DATE_TIME_RANGE_PICKER)
        Type.TIME_RANGE -> listOf(PREF_TIME_RANGE_PICKER)
        Type.STR_ASCII,
        Type.STR_UTF8 -> listOf(PREF_STR_TEXT, PREF_STR_COLOR)
        Type.STR_EMAIL,
        Type.STR_PHONE,
        Type.STR_URI,
        Type.STR_PASSWORD -> listOf(PREF_STR_TEXT)
        Type.FIXP32,
        Type.FIXP64 -> listOf(PREF_NUM_TEXT, PREF_NUM_SLIDER)
        Type.LANGUAGE -> listOf(PREF_LANGUAGE)
        else -> listOf()
    }
}

@StringRes
fun getPreferenceDisplayName(preferenceName: String): Int {
    return when (preferenceName) {
        PREF_MOD_DIVIDER -> R.string.pref_mod_divider
        PREF_MOD_HEADER -> R.string.pref_mod_header
        PREF_SEL_RADIO -> R.string.pref_sel_radio
        PREF_SEL_DROPDOWN -> R.string.pref_sel_dropdown
        PREF_SEL_CHECK -> R.string.pref_sel_check
        PREF_BOOL_SWITCH -> R.string.pref_bool_switch
        PREF_ARRAY_FRAME -> R.string.pref_array_frame
        PREF_NUM_TEXT -> R.string.pref_num_text
        PREF_NUM_SLIDER -> R.string.pref_num_slider
        PREF_NUMBER_LIST_DROPDOWN -> R.string.pref_number_list_dropdown
        PREF_NUMBER_LIST_BUTTONS -> R.string.pref_number_list_buttons
        PREF_DATE_PICKER -> R.string.pref_date_picker
        PREF_DATE_TIME_PICKER -> R.string.pref_date_time_picker
        PREF_ZONED_DATE_TIME_PICKER -> R.string.pref_zoned_date_time_picker
        PREF_DATE_RANGE_PICKER -> R.string.pref_date_range_picker
        PREF_DATE_TIME_RANGE_PICKER -> R.string.pref_date_time_range_picker
        PREF_TIME_PICKER -> R.string.pref_time_picker
        PREF_TIME_RANGE_PICKER -> R.string.pref_time_range_picker
        PREF_STR_TEXT -> R.string.pref_str_text
        PREF_STR_COLOR -> R.string.pref_str_color
        PREF_LANGUAGE -> R.string.pref_language
        else -> R.string.pref_unknown
    }
}

fun instantiatePreference(
    property: BaseItem,
    uiConfig: List<CustomizedPreference>,
    root: LinearLayout,
    displayLanguage: String?,
    hideOnDisable: Boolean,
    fragmentManager: FragmentManager,
    languageChangedListener: () -> Unit
): View {
    val preference = when (getPreferenceIdForProperty(property, uiConfig)) {
        PREF_MOD_DIVIDER ->
            DividerPreference.get(root)
        PREF_MOD_HEADER ->
            HeaderPreference.get(root, property, displayLanguage, hideOnDisable)
        PREF_SEL_RADIO ->
            RadioPreference.get(root, property, displayLanguage, hideOnDisable)
        PREF_SEL_DROPDOWN ->
            DropdownPreference.get(root, property, displayLanguage, hideOnDisable)
        PREF_SEL_CHECK ->
            CheckBoxPreference.get(root, property, displayLanguage, hideOnDisable)
        PREF_BOOL_SWITCH ->
            SwitchPreference.get(root, property, displayLanguage, hideOnDisable)
        PREF_ARRAY_FRAME ->
            ArrayPreference.get(
                root,
                property,
                displayLanguage,
                hideOnDisable,
                fragmentManager,
                uiConfig
            )
        PREF_NUM_TEXT ->
            NumberPreference.get(root, property, displayLanguage, hideOnDisable)
        PREF_NUM_SLIDER ->
            SliderPreference.get(root, property, displayLanguage, hideOnDisable)
        PREF_NUMBER_LIST_DROPDOWN ->
            NumberListDropdownPreference.get(root, property, displayLanguage, hideOnDisable)
        PREF_NUMBER_LIST_BUTTONS ->
            NumberListButtonsPreference.get(root, property, displayLanguage, hideOnDisable)
        PREF_DATE_PICKER ->
            DatePreference.get(root, property, displayLanguage, hideOnDisable, fragmentManager)
        PREF_DATE_TIME_PICKER ->
            DateTimePreference.get(root, property, displayLanguage, hideOnDisable, fragmentManager)
        PREF_ZONED_DATE_TIME_PICKER ->
            ZonedDateTimePreference.get(
                root,
                property,
                displayLanguage,
                hideOnDisable,
                fragmentManager
            )
        PREF_DATE_RANGE_PICKER ->
            DateRangePreference.get(root, property, displayLanguage, hideOnDisable, fragmentManager)
        PREF_DATE_TIME_RANGE_PICKER ->
            DateTimeRangePreference.get(
                root,
                property,
                displayLanguage,
                hideOnDisable,
                fragmentManager
            )
        PREF_TIME_PICKER ->
            TimePreference.get(root, property, displayLanguage, hideOnDisable, fragmentManager)
        PREF_TIME_RANGE_PICKER ->
            TimeRangePreference.get(root, property, displayLanguage, hideOnDisable, fragmentManager)
        PREF_STR_TEXT ->
            StringPreference.get(root, property, displayLanguage, hideOnDisable)
        PREF_STR_COLOR ->
            ColorStringPreference.get(
                root,
                property,
                displayLanguage,
                hideOnDisable,
                fragmentManager
            )
        PREF_LANGUAGE ->
            LanguagePreference.get(
                root,
                property,
                displayLanguage,
                hideOnDisable,
                languageChangedListener
            )
        else ->
            PlaceholderPreference(root.context)
    }
    root.addView(preference)
    return preference
}

fun createNdefMessageFromJson(context: Context, dataToWrite: String): NdefMessage {
    try {
        val json = JSONObject(dataToWrite)
        val meta = json.getJSONObject("metadata")
        val metadataPayload = base64ToBytes(meta.getString("payload"))
        val metadataRecord = NdefRecord(
            NdefRecord.TNF_ABSOLUTE_URI,
            getNdefUri(context, "meta", meta.optBoolean("compressed", true))
                .toByteArray(Charsets.US_ASCII),
            ByteArray(1).apply { set(0, 0x01) },
            metadataPayload
        )
        val data = json.getJSONObject("data")
        val dataLength = data.getInt("size")
        val dataPayload = if (data.has("payload")) {
            base64ToBytes(data.getString("payload"))
        } else {
            ByteArray(dataLength)
        }
        val dataRecord = NdefRecord(
            NdefRecord.TNF_ABSOLUTE_URI,
            getNdefUri(context, "data").toByteArray(Charsets.US_ASCII),
            ByteArray(1).apply { set(0, 0x02) },
            dataPayload
        )
        return NdefMessage(dataRecord, metadataRecord)
    } catch (e: JSONException) {
        // Data is not json - write as plain text
        val record = NdefRecord(
            NdefRecord.TNF_MIME_MEDIA,
            "text/plain".toByteArray(Charsets.US_ASCII),
            ByteArray(0x01).apply { set(0, 0x01) },
            dataToWrite.toByteArray(Charsets.US_ASCII)
        )
        return NdefMessage(record)
    }
}

fun getNdefUri(context: Context, type: String = "", compressed: Boolean = true): String {
    val scheme = context.getString(R.string.ndef_scheme)
    val host = context.getString(R.string.ndef_host)
    val path = context.getString(R.string.ndef_path)
    var uri = context.getString(R.string.ndef_uri, scheme, host, path)
    if (type.isNotBlank() || !compressed) {
        uri += "?"
    }
    if (type.isNotBlank()) {
        uri += context.getString(R.string.ndef_arg_type, type)
    }
    if (!compressed) {
        if (!uri.endsWith("?")) {
            uri += "&"
        }
        uri += context.getString(R.string.ndef_arg_type, type)
    }
    return uri
}

@StringRes
fun getTypeDisplayName(type: Int): Int {
    return when (type) {
        DeviceType.CUSTOM,
        DeviceType.CUSTOM_NO_TRUNCATE -> R.string.type_custom
        DeviceType.LIGHT -> R.string.type_light
        DeviceType.WASHING_MACHINE -> R.string.type_washing_machine
        DeviceType.HEATER -> R.string.type_heater
        else -> R.string.type_unknown
    }
}

fun getDeviceDisplayName(context: Context, knownDevice: KnownDevice): String {
    val name = knownDevice.name
    return name.ifBlank {
        val nameRes = getTypeDisplayName(knownDevice.type)
        context.getString(nameRes)
    }
}