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
import eput.protocol.properties.TimeProperty
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle

class TimePreference : TextInput, Preference {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    private lateinit var fragmentManager: FragmentManager
    private var property: TimeProperty? = null
    private var hideOnDisable: Boolean = false

    private fun setUp(
        view: TextInputEditText,
        fragmentManager: FragmentManager,
        hideOnDisable: Boolean
    ) {
        this.hideOnDisable = hideOnDisable
        super.setUp(view) { text ->
            try {
                property?.value = formatter.parse(text.trim(), LocalTime::from)
                onError(null)
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
        val prop = property as TimeProperty
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
            val timePicker = MaterialTimePicker.Builder()
                .setTitleText(R.string.title_time_dialog)
                .setHour(it.value.hour)
                .setMinute(it.value.minute)
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .build()
            timePicker.addOnPositiveButtonClickListener { _ ->
                it.value = LocalTime.of(timePicker.hour, timePicker.minute)
                input.setText(it.value.format(formatter))
            }
            timePicker.show(fragmentManager, "time_picker")
        }
    }

    companion object {
        fun get(
            parent: ViewGroup,
            property: BaseItem,
            displayLanguage: String?,
            hideOnDisable: Boolean,
            fragmentManager: FragmentManager
        ): TimePreference {
            val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.input_time, parent, false) as TimePreference
            val input = layout.findViewById<TextInputEditText>(R.id.input)
            layout.setUp(input, fragmentManager, hideOnDisable)
            layout.setProperty(property, displayLanguage)
            return layout
        }
    }
}