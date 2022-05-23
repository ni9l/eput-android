package eput.android.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import eput.android.R
import eput.protocol.BaseItem
import eput.protocol.properties.ZonedDateTimeProperty
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import kotlin.math.absoluteValue
import kotlin.math.sign

class ZonedDateTimePreference : LinearLayoutCompat, Preference {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private val regex = Regex(OFFSET_PATTERN)
    private val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
    private lateinit var layout: TextInputLayout
    private lateinit var input: TextInputEditText
    private lateinit var zoneLayout: TextInputLayout
    private lateinit var zoneInput: TextInputEditText
    private lateinit var fragmentManager: FragmentManager
    private var property: ZonedDateTimeProperty? = null
    private var hideOnDisable: Boolean = false

    private fun setUp(
        layout: TextInputLayout,
        input: TextInputEditText,
        zoneLayout: TextInputLayout,
        zoneInput: TextInputEditText,
        fragmentManager: FragmentManager,
        hideOnDisable: Boolean
    ) {
        this.hideOnDisable = hideOnDisable
        this.layout = layout
        this.input = input
        this.zoneLayout = zoneLayout
        this.zoneInput = zoneInput
        this.fragmentManager = fragmentManager
        this.input.addTextChangedListener { editable ->
            try {
                val date = formatter.parse(editable.toString().trim(), LocalDateTime::from)
                onError(layout, null)
                property?.apply {
                    val offset = value.offset
                    value = date.atOffset(offset)
                }
            } catch (e: DateTimeParseException) {
                onError(
                    this.layout,
                    context.getString(R.string.error_format, formatter.toString())
                )
            }
        }
        this.layout.setEndIconOnClickListener { openDateTimeDialog() }
        this.zoneLayout.setEndIconOnClickListener { openTimeZoneDialog() }
        this.zoneInput.doOnTextChanged { text, _, _, _ ->
            val offset = parseOffsetMinutes(text)
            if (offset != null) {
                property?.apply {
                    setOffset(ZoneOffset.ofTotalSeconds(offset * 60))
                }
                onError(this.zoneLayout, null)
            } else {
                onError(this.zoneLayout, context.getString(R.string.error_format_zone))
            }
        }
    }

    override fun setProperty(property: BaseItem, displayLanguage: String?) {
        isEnabled = true
        visibility = VISIBLE
        val prop = property as ZonedDateTimeProperty
        this.property = prop
        layout.hint = prop.getDisplayName(displayLanguage)
        input.setText(prop.value.format(formatter))
        setZoneText(prop.value.offset)
    }

    override fun clearProperty() {
        property = null
        layout.hint = ""
        layout.isErrorEnabled = false
        layout.error = ""
        input.setText("")
        zoneLayout.hint = ""
        zoneLayout.isErrorEnabled = false
        zoneLayout.error = ""
        zoneInput.setText("")
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        layout.isEnabled = enabled
        input.isEnabled = enabled
        zoneLayout.isEnabled = enabled
        zoneInput.isEnabled = enabled
    }

    override fun setEnabled(enabled: Boolean, ids: List<String>) {
        if (property?.id in ids) {
            isEnabled = enabled
            if (hideOnDisable) {
                visibility = if (enabled) VISIBLE else GONE
            }
        }
    }

    private fun onError(textInputLayout: TextInputLayout, message: String?) {
        if (message == null) {
            textInputLayout.isErrorEnabled = false
        } else {
            textInputLayout.isErrorEnabled = true
            textInputLayout.error = message
        }
    }

    private fun parseOffsetMinutes(charSequence: CharSequence?): Int? {
        val match = regex.find(charSequence ?: "")
        if (match != null) {
            val sign = match.groups[1]
            val h = match.groups[2] ?: return null
            val m = match.groups[3] ?: return null
            val hours = h.value.toInt()
            val minutes = m.value.toInt()
            val offset = (hours * 60) + minutes
            return if (sign?.value == "-") {
                -offset
            } else {
                offset
            }
        }
        return null
    }

    private fun setZoneText(offset: ZoneOffset) {
        val sign = if (offset.totalSeconds.sign < 0) "-" else "+"
        val minutes = (offset.totalSeconds.absoluteValue / 60) % 60
        val hours = offset.totalSeconds.absoluteValue / 3600
        val text = context.getString(R.string.format_zone, sign, hours, minutes)
        zoneInput.setText(text, TextView.BufferType.EDITABLE)
    }

    private fun openDateTimeDialog() {
        property?.let {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.title_date_dialog)
                .setSelection(it.valueAsInstant.toEpochMilli())
                .build()
            val timePicker = MaterialTimePicker.Builder()
                .setTitleText(R.string.title_time_dialog)
                .setHour(it.value.hour)
                .setMinute(it.value.minute)
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .build()
            datePicker.addOnPositiveButtonClickListener {
                timePicker.show(fragmentManager, "time_picker")
            }
            timePicker.addOnPositiveButtonClickListener { _ ->
                it.setValue(
                    it.value.offset.normalized(),
                    Instant.ofEpochMilli(datePicker.selection ?: 0),
                    timePicker.hour,
                    timePicker.minute
                )
                input.setText(it.value.format(formatter))
            }
            datePicker.show(fragmentManager, "date_picker")
        }
    }

    private fun openTimeZoneDialog() {
        property?.let {
            val dialog = TimeZoneDialog()
            dialog.onItemSelectedListener = { zoneId ->
                val offset = zoneId.rules.getOffset(it.valueAsInstant)
                it.setOffset(offset)
                setZoneText(offset)
            }
            dialog.instant = it.valueAsInstant
            dialog.show(fragmentManager, "timer_zone_dialog")
        }
    }

    companion object {
        private const val OFFSET_PATTERN =
            "^\\s*([+-])?([0-1]?[0-9]):([0-5][0-9])\\s*\$"

        fun get(
            parent: ViewGroup,
            property: BaseItem,
            displayLanguage: String?,
            hideOnDisable: Boolean,
            fragmentManager: FragmentManager
        ): ZonedDateTimePreference {
            val root = LayoutInflater.from(parent.context).inflate(
                R.layout.input_zoned_date_time,
                parent,
                false
            ) as ZonedDateTimePreference
            val layout = root.findViewById<TextInputLayout>(R.id.layout)
            val input = root.findViewById<TextInputEditText>(R.id.input)
            val zoneLayout = root.findViewById<TextInputLayout>(R.id.zone_layout)
            val zoneInput = root.findViewById<TextInputEditText>(R.id.zone_input)
            root.setUp(layout, input, zoneLayout, zoneInput, fragmentManager, hideOnDisable)
            root.setProperty(property, displayLanguage)
            return root
        }
    }
}