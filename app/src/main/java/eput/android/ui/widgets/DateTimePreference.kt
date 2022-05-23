package eput.android.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import eput.android.R
import eput.protocol.BaseItem
import eput.protocol.properties.DateTimeProperty
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle

class DateTimePreference : TextInput, Preference {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
    private lateinit var fragmentManager: FragmentManager
    private var property: DateTimeProperty? = null
    private var hideOnDisable: Boolean = false

    private fun setUp(
        view: TextInputEditText,
        fragmentManager: FragmentManager,
        hideOnDisable: Boolean
    ) {
        this.hideOnDisable = hideOnDisable
        super.setUp(view) { text ->
            try {
                property?.value = formatter.parse(text.trim(), LocalDateTime::from)
                onError(null)
            } catch (e: DateTimeParseException) {
                onError(context.getString(R.string.error_format, formatter.toString()))
            }
        }
        this.fragmentManager = fragmentManager
        input.inputType = EditorInfo.TYPE_CLASS_DATETIME
        setEndIconOnClickListener { openDialog() }
    }

    override fun setProperty(property: BaseItem, displayLanguage: String?) {
        isEnabled = true
        visibility = VISIBLE
        val prop = property as DateTimeProperty
        this.property = prop
        hint = prop.getDisplayName(displayLanguage)
        input.setText(prop.value.format(formatter))
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
                val instant = Instant.ofEpochMilli(datePicker.selection ?: 0)
                it.setValue(instant, timePicker.hour, timePicker.minute)
                input.setText(it.value.format(formatter))
            }
            datePicker.show(fragmentManager, "date_picker")
        }
    }

    companion object {
        fun get(
            parent: ViewGroup,
            property: BaseItem,
            displayLanguage: String?,
            hideOnDisable: Boolean,
            fragmentManager: FragmentManager
        ): DateTimePreference {
            val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.input_date_time, parent, false) as DateTimePreference
            val input = layout.findViewById<TextInputEditText>(R.id.input)
            layout.setUp(input, fragmentManager, hideOnDisable)
            layout.setProperty(property, displayLanguage)
            return layout
        }
    }
}