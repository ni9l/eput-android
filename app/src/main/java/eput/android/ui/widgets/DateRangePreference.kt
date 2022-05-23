package eput.android.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import eput.android.R
import eput.protocol.BaseItem
import eput.protocol.properties.DateRangeProperty
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle

class DateRangePreference : TextInput, Preference {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
    private lateinit var fragmentManager: FragmentManager
    private var property: DateRangeProperty? = null
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
                    val fromDate = formatter.parse(parts[0], LocalDate::from)
                    val toDate = formatter.parse(parts[1], LocalDate::from)
                    onError(null)
                    property?.apply {
                        valueFrom = fromDate
                        valueTo = toDate
                    }
                }
            } catch (e: DateTimeParseException) {
                onError(
                    context.getString(
                        R.string.error_format_range,
                        formatter.toString(),
                        formatter.toString()
                    )
                )
            }
        }
        this.fragmentManager = fragmentManager
        setEndIconOnClickListener { openDialog() }
    }

    override fun setProperty(property: BaseItem, displayLanguage: String?) {
        isEnabled = true
        visibility = VISIBLE
        val prop = property as DateRangeProperty
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
            val from = it.valueFromAsInstant.toEpochMilli()
            val to = it.valueToAsInstant.toEpochMilli()
            val dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText(R.string.title_date_dialog)
                    .setSelection(Pair(from, to))
                    .build()
            dateRangePicker.addOnPositiveButtonClickListener { date ->
                it.setValueFrom(Instant.ofEpochMilli(date.first))
                it.setValueTo(Instant.ofEpochMilli(date.second))
                input.setText(
                    context.getString(
                        R.string.range_template,
                        it.valueFrom.format(formatter),
                        it.valueTo.format(formatter)
                    )
                )
            }
            dateRangePicker.show(fragmentManager, "date_range_picker")
        }
    }

    companion object {
        fun get(
            parent: ViewGroup,
            property: BaseItem,
            displayLanguage: String?,
            hideOnDisable: Boolean,
            fragmentManager: FragmentManager
        ): DateRangePreference {
            val layout = LayoutInflater.from(parent.context).inflate(
                R.layout.input_date_range,
                parent,
                false
            ) as DateRangePreference
            val input = layout.findViewById<TextInputEditText>(R.id.input)
            layout.setUp(input, fragmentManager, hideOnDisable)
            layout.setProperty(property, displayLanguage)
            return layout
        }
    }
}