package eput.android.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import eput.android.R
import eput.protocol.BaseItem
import eput.protocol.properties.DateTimeRangeProperty
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle

class DateTimeRangePreference : TextInput, Preference {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
    private lateinit var fragmentManager: FragmentManager
    private var property: DateTimeRangeProperty? = null
    private var hideOnDisable: Boolean = false

    private fun setUp(
        view: TextInputEditText,
        fragmentManager: FragmentManager,
        hideOnDisable: Boolean
    ) {
        this.hideOnDisable = hideOnDisable
        super.setUp(view) { text ->
            try {
                val parts = text.split("-").map(String::trim)
                if (parts.isEmpty() || parts.size != 2) {
                    throw DateTimeParseException("Invalid input", parts.toString(), 0)
                } else {
                    val fromDateTime = formatter.parse(parts[0], LocalDateTime::from)
                    val toDateTime = formatter.parse(parts[1], LocalDateTime::from)
                    onError(null)
                    property?.apply {
                        valueFrom = fromDateTime
                        valueTo = toDateTime
                    }
                }
            } catch (e: DateTimeParseException) {
                onError(context.getString(R.string.error_format, formatter.toString()))
            }
        }
        this.fragmentManager = fragmentManager
        setEndIconOnClickListener { openDialog() }
    }

    override fun setProperty(property: BaseItem, displayLanguage: String?) {
        isEnabled = true
        visibility = VISIBLE
        val prop = property as DateTimeRangeProperty
        this.property = prop
        hint = prop.getDisplayName(displayLanguage)
        input.setText(
            context.getString(
                R.string.range_template,
                prop.valueFrom.format(formatter),
                prop.valueTo.format(formatter)
            )
        )
    }

    override fun clearProperty() {
        property = null
        clear()
    }

    override fun setEnabled(enabled: Boolean, ids: List<String>) {
        if (property?.id in ids) {
            isEnabled = enabled
            if (hideOnDisable) {
                visibility = if (enabled) VISIBLE else GONE
            }
        }
    }

    private fun openDialog() {
        property?.let {
            val dateFrom = it.valueFrom
            val dateTo = it.valueTo
            val instantFrom = it.valueFromAsInstant
            val instantTo = it.valueToAsInstant
            val datePickerFrom = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.title_date_dialog)
                .setSelection(instantFrom.toEpochMilli())
                .build()
            val timePickerFrom = MaterialTimePicker.Builder()
                .setTitleText(R.string.title_time_dialog)
                .setHour(dateFrom.hour)
                .setMinute(dateFrom.minute)
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .build()
            val datePickerTo = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.title_date_dialog)
                .setSelection(instantTo.toEpochMilli())
                .build()
            val timePickerTo = MaterialTimePicker.Builder()
                .setTitleText(R.string.title_time_dialog)
                .setHour(dateTo.hour)
                .setMinute(dateTo.minute)
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .build()
            datePickerFrom.addOnPositiveButtonClickListener {
                timePickerFrom.show(fragmentManager, "time_picker_from")
            }
            timePickerFrom.addOnPositiveButtonClickListener {
                datePickerTo.show(fragmentManager, "date_picker_to")
            }
            datePickerTo.addOnPositiveButtonClickListener {
                timePickerTo.show(fragmentManager, "time_picker_to")
            }
            timePickerTo.addOnPositiveButtonClickListener { _ ->
                val newInstantFrom = Instant.ofEpochMilli(datePickerFrom.selection ?: 0)
                val newInstantTo = Instant.ofEpochMilli(datePickerTo.selection ?: 0)
                it.setValueFrom(newInstantFrom, timePickerFrom.hour, timePickerFrom.minute)
                it.setValueTo(newInstantTo, timePickerTo.hour, timePickerTo.minute)
                input.setText(
                    context.getString(
                        R.string.range_template,
                        it.valueFrom.format(formatter),
                        it.valueTo.format(formatter)
                    )
                )
            }
            datePickerFrom.show(fragmentManager, "date_picker_from")
        }
    }

    companion object {
        fun get(
            parent: ViewGroup,
            property: BaseItem,
            displayLanguage: String?,
            hideOnDisable: Boolean,
            fragmentManager: FragmentManager
        ): DateTimeRangePreference {
            val layout = LayoutInflater.from(parent.context).inflate(
                R.layout.input_date_time_range,
                parent,
                false
            ) as DateTimeRangePreference
            val input = layout.findViewById<TextInputEditText>(R.id.input)
            layout.setUp(input, fragmentManager, hideOnDisable)
            layout.setProperty(property, displayLanguage)
            return layout
        }
    }
}