package eput.android.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import eput.android.R
import eput.protocol.BaseItem
import eput.protocol.properties.TimeRangeProperty
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle

class TimeRangePreference : TextInput, Preference {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    private lateinit var fragmentManager: FragmentManager
    private var property: TimeRangeProperty? = null
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
                    throw DateTimeParseException("Two time values needed", text, 0)
                } else {
                    val fromTime = formatter.parse(parts[0], LocalTime::from)
                    val toTime = formatter.parse(parts[1], LocalTime::from)
                    onError(null)
                    property?.apply {
                        valueFrom = fromTime
                        valueTo = toTime
                    }
                }
            } catch (e: DateTimeParseException) {
                val format = formatter.toString()
                onError(context.getString(R.string.error_format_range, format, format))
            }
        }
        this.fragmentManager = fragmentManager
        setEndIconOnClickListener { openDialog() }
    }

    override fun setProperty(property: BaseItem, displayLanguage: String?) {
        isEnabled = true
        visibility = VISIBLE
        val prop = property as TimeRangeProperty
        this.property = prop
        hint = prop.getDisplayName(displayLanguage)
        val from = prop.valueFrom.format(formatter)
        val to = prop.valueTo.format(formatter)
        input.setText(context.getString(R.string.range_template, from, to))
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
            val fromTimePicker = MaterialTimePicker.Builder()
                .setTitleText(R.string.title_time_dialog_from)
                .setHour(it.valueFrom.hour)
                .setMinute(it.valueFrom.minute)
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .build()
            val toTimePicker = MaterialTimePicker.Builder()
                .setTitleText(R.string.title_time_dialog_to)
                .setHour(it.valueTo.hour)
                .setMinute(it.valueTo.minute)
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .build()
            fromTimePicker.addOnPositiveButtonClickListener {
                toTimePicker.show(fragmentManager, "to_time_picker")
            }
            toTimePicker.addOnPositiveButtonClickListener { _ ->
                it.valueFrom = LocalTime.of(fromTimePicker.hour, fromTimePicker.minute)
                it.valueTo = LocalTime.of(toTimePicker.hour, toTimePicker.minute)
                val from = it.valueFrom.format(formatter)
                val to = it.valueTo.format(formatter)
                input.setText(context.getString(R.string.range_template, from, to))
            }
            fromTimePicker.show(fragmentManager, "from_time_picker")
        }
    }

    companion object {
        fun get(
            parent: ViewGroup,
            property: BaseItem,
            displayLanguage: String?,
            hideOnDisable: Boolean,
            fragmentManager: FragmentManager
        ): TimeRangePreference {
            val layout = LayoutInflater.from(parent.context).inflate(
                R.layout.input_time_range,
                parent,
                false
            ) as TimeRangePreference
            val input = layout.findViewById<TextInputEditText>(R.id.input)
            layout.setUp(input, fragmentManager, hideOnDisable)
            layout.setProperty(property, displayLanguage)
            return layout
        }
    }
}