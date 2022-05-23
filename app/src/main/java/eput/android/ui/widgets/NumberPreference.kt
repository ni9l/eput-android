package eput.android.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import eput.android.R
import eput.android.model.LengthUnitHandler
import eput.android.model.TimeUnitHandler
import eput.android.model.UnitHandler
import eput.android.model.WeightUnitHandler
import eput.protocol.BaseItem
import eput.protocol.Type
import eput.protocol.properties.*
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.ParseException

class NumberPreference : LinearLayoutCompat, Preference {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private val format = DecimalFormat.getInstance()
    private lateinit var layout: TextInputLayout
    private lateinit var input: TextInputEditText
    private lateinit var stub: ViewStub
    private var spinner: Spinner? = null
    private var property: NumberProperty? = null
    private var unitHandler: UnitHandler? = null
    private var hideOnDisable: Boolean = false

    private fun setUp(
        layoutView: TextInputLayout,
        inputView: TextInputEditText,
        stubView: ViewStub,
        hideOnDisable: Boolean
    ) {
        this.hideOnDisable = hideOnDisable
        layout = layoutView
        input = inputView
        stub = stubView
        input.addTextChangedListener { editable ->
            updatePropertyValue(editable.toString())
        }
    }

    override fun setProperty(property: BaseItem, displayLanguage: String?) {
        isEnabled = true
        visibility = VISIBLE
        val prop = property as NumberProperty
        this.property = prop
        layout.hint = prop.getDisplayName(displayLanguage)
        layout.isHelperTextEnabled = true
        if (prop is IntegerProperty) {
            layout.helperText = context
                .getString(R.string.helper_number_range_int, prop.minValue, prop.maxValue)
            input.setText(String.format("%d", prop.value))
            if (prop is SignedIntegerProperty) {
                input.inputType = EditorInfo.TYPE_CLASS_NUMBER or
                        EditorInfo.TYPE_NUMBER_FLAG_SIGNED
            } else {
                input.inputType = EditorInfo.TYPE_CLASS_NUMBER
            }
        } else if (prop is FloatProperty) {
            layout.helperText = context
                .getString(R.string.helper_number_range_dec, prop.minValue, prop.maxValue)
            input.setText(String.format("%f", prop.value))
            input.inputType = EditorInfo.TYPE_CLASS_NUMBER or
                    EditorInfo.TYPE_NUMBER_FLAG_SIGNED or
                    EditorInfo.TYPE_NUMBER_FLAG_DECIMAL
        } else if (prop is DoubleProperty) {
            layout.helperText = context
                .getString(R.string.helper_number_range_dec, prop.minValue, prop.maxValue)
            input.setText(String.format("%f", prop.value), TextView.BufferType.EDITABLE)
            input.inputType = EditorInfo.TYPE_CLASS_NUMBER or
                    EditorInfo.TYPE_NUMBER_FLAG_SIGNED or
                    EditorInfo.TYPE_NUMBER_FLAG_DECIMAL
        }
        setupContentType(prop)
    }

    // TODO can't input comma as separator on keyboard

    override fun clearProperty() {
        property = null
        layout.hint = ""
        layout.helperText = ""
        input.inputType = EditorInfo.TYPE_CLASS_NUMBER
        input.setText("", TextView.BufferType.EDITABLE)
        spinner?.adapter = null
        spinner?.onItemSelectedListener = null
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        layout.isEnabled = enabled
        input.isEnabled = enabled
        spinner?.isEnabled = enabled
    }

    override fun setEnabled(enabled: Boolean, ids: List<String>) {
        if (property?.id in ids) {
            isEnabled = enabled
            if (hideOnDisable) {
                visibility = if (enabled) VISIBLE else GONE
            }
        }
    }

    private fun updatePropertyValue(stringValue: String) {
        property?.let { prop ->
            val handler = unitHandler
            try {
                when (prop) {
                    is IntegerProperty -> {
                        var newValue = BigInteger(stringValue)
                        handler?.let {
                            newValue = it.fromUnit(newValue)
                        }
                        prop.value = newValue
                    }
                    is FloatProperty -> {
                        val number = format.parse(stringValue)!!
                        var newValue = number.toFloat()
                        handler?.let {
                            newValue = it.fromUnit(newValue)
                        }
                        prop.value = newValue
                    }
                    is DoubleProperty -> {
                        val number = format.parse(stringValue)!!
                        var newValue = number.toDouble()
                        handler?.let {
                            newValue = it.fromUnit(newValue)
                        }
                        prop.value = newValue
                    }
                }
                layout.isErrorEnabled = false
            } catch (e: java.lang.NumberFormatException) {
                onError()
            } catch (e: ParseException) {
                onError()
            }
        }
    }

    private fun setupContentType(property: NumberProperty) {
        val handler: UnitHandler? = when (property.contentType) {
            Type.NumberContentType.TIME -> TimeUnitHandler(context, property.contentTypeDefault)
            Type.NumberContentType.WEIGHT -> WeightUnitHandler(context, property.contentTypeDefault)
            Type.NumberContentType.LENGTH -> LengthUnitHandler(context, property.contentTypeDefault)
            else -> null
        }
        unitHandler = handler
        if (handler != null) {
            if (spinner == null) {
                spinner = findViewById<ViewStub>(R.id.stub).inflate() as Spinner
            }
            spinner?.visibility = VISIBLE
            val items = handler.displayUnits
            val adapter = ArrayAdapter(context, R.layout.entry_text_list, items)
            spinner?.adapter = adapter
            spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    unitHandler?.selectedUnit = position
                    val helpText = when (property) {
                        is IntegerProperty -> {
                            val minValue = handler.toUnit(property.minValue)
                            val maxValue = handler.toUnit(property.maxValue)
                            context.getString(
                                R.string.helper_number_range_int,
                                minValue,
                                maxValue
                            )
                        }
                        is FloatProperty -> {
                            val minValue = handler.toUnit(property.minValue)
                            val maxValue = handler.toUnit(property.maxValue)
                            context.getString(
                                R.string.helper_number_range_dec,
                                minValue,
                                maxValue
                            )
                        }
                        is DoubleProperty -> {
                            val minValue = handler.toUnit(property.minValue)
                            val maxValue = handler.toUnit(property.maxValue)
                            context.getString(
                                R.string.helper_number_range_dec,
                                minValue,
                                maxValue
                            )
                        }
                        else -> null
                    }
                    helpText?.let {
                        layout.helperText = it
                    }
                    updatePropertyValue(input.text.toString())
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Ignore
                }
            }
            spinner?.setSelection(handler.selectedUnit)
        } else {
            spinner?.visibility = GONE
        }
    }

    private fun onError() {
        layout.error = property?.let { prop ->
            val handler = unitHandler
            return@let if (handler != null) {
                when (prop) {
                    is IntegerProperty -> {
                        val minValue = handler.toUnit(prop.minValue)
                        val maxValue = handler.toUnit(prop.maxValue)
                        context.getString(
                            R.string.error_invalid_number_int,
                            minValue,
                            maxValue
                        )
                    }
                    is FloatProperty -> {
                        val minValue = handler.toUnit(prop.minValue)
                        val maxValue = handler.toUnit(prop.maxValue)
                        context.getString(
                            R.string.error_invalid_number_dec,
                            minValue,
                            maxValue
                        )
                    }
                    is DoubleProperty -> {
                        val minValue = handler.toUnit(prop.minValue)
                        val maxValue = handler.toUnit(prop.maxValue)
                        context.getString(
                            R.string.error_invalid_number_dec,
                            minValue,
                            maxValue
                        )
                    }
                    else -> null
                }
            } else {
                when (prop) {
                    is IntegerProperty -> context.getString(
                        R.string.error_invalid_number_int,
                        prop.minValue,
                        prop.maxValue
                    )
                    is FloatProperty -> context.getString(
                        R.string.error_invalid_number_dec,
                        prop.minValue,
                        prop.maxValue
                    )
                    is DoubleProperty -> context.getString(
                        R.string.error_invalid_number_dec,
                        prop.minValue,
                        prop.maxValue
                    )
                    else -> null
                }
            }
        } ?: context.getString(R.string.error_invalid_number_int, 0, 0)
        layout.isErrorEnabled = true
    }

    companion object {
        fun get(
            parent: ViewGroup,
            property: BaseItem,
            displayLanguage: String?,
            hideOnDisable: Boolean
        ): NumberPreference {
            val root = LayoutInflater.from(parent.context)
                .inflate(R.layout.input_number, parent, false) as NumberPreference
            val layout = root.findViewById<TextInputLayout>(R.id.layout)
            val input = root.findViewById<TextInputEditText>(R.id.input)
            val stub = root.findViewById<ViewStub>(R.id.stub)
            root.setUp(layout, input, stub, hideOnDisable)
            root.setProperty(property, displayLanguage)
            return root
        }
    }
}