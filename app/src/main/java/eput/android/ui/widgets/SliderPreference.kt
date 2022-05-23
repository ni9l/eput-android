package eput.android.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.slider.Slider
import com.google.android.material.textview.MaterialTextView
import eput.android.R
import eput.protocol.BaseItem
import eput.protocol.properties.*
import java.math.BigDecimal
import java.math.RoundingMode

open class SliderPreference : ConstraintLayout, Preference {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    private lateinit var title: MaterialTextView
    private lateinit var slider: Slider
    private lateinit var label: MaterialTextView
    private var property: NumberProperty? = null
    private var hideOnDisable: Boolean = false

    protected fun setUp(
        titleView: MaterialTextView,
        sliderView: Slider,
        labelView: MaterialTextView,
        hideOnDisable: Boolean
    ) {
        this.hideOnDisable = hideOnDisable
        title = titleView
        slider = sliderView
        label = labelView
        slider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                setPropertyValue(value)
            }
        }
    }

    override fun setProperty(property: BaseItem, displayLanguage: String?) {
        isEnabled = true
        visibility = VISIBLE
        val prop = property as NumberProperty
        this.property = prop
        title.text = prop.getDisplayName(displayLanguage)
        when (prop) {
            is IntegerProperty -> {
                val a = BigDecimal(prop.value)
                val min = BigDecimal(prop.minValue)
                val max = BigDecimal(prop.maxValue)
                setSliderValue(a, min, max)
            }
            is FloatProperty -> {
                val a = BigDecimal.valueOf(prop.value.toDouble())
                val min = BigDecimal.valueOf(prop.minValue.toDouble())
                val max = BigDecimal.valueOf(prop.maxValue.toDouble())
                setSliderValue(a, min, max)
            }
            is DoubleProperty -> {
                val a = BigDecimal.valueOf(prop.value)
                val min = BigDecimal.valueOf(prop.minValue)
                val max = BigDecimal.valueOf(prop.maxValue)
                setSliderValue(a, min, max)
            }
            is FixedPointProperty -> {
                val a = prop.value
                val min = prop.minValue
                val max = prop.maxValue
                setSliderValue(a, min, max)
            }
            else -> {
                setSliderValue(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
            }
        }
    }

    override fun clearProperty() {
        property = null
        title.text = ""
        slider.value = 0F
        label.text = ""
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        title.isEnabled = enabled
        slider.isEnabled = enabled
        label.isEnabled  = enabled
    }

    override fun setEnabled(enabled: Boolean, ids: List<String>) {
        if (property?.id in ids) {
            isEnabled = enabled
            if (hideOnDisable) {
                visibility = if (enabled) VISIBLE else GONE
            }
        }
    }

    private fun setSliderValue(
        value: BigDecimal,
        minValue: BigDecimal,
        maxValue: BigDecimal
    ) {
        label.text = String.format("%.2g", value)
        slider.value = propertyToSlider(value, minValue, maxValue)
    }

    private fun setPropertyValue(value: Float) {
        property?.let {
            when (it) {
                is IntegerProperty -> {
                    val num = sliderToProperty(
                        value,
                        it.minValue.toBigDecimal(),
                        it.maxValue.toBigDecimal()
                    ).toBigInteger().coerceIn(it.minValue, it.maxValue)
                    it.value = num.minus(num.remainder(it.stepSize))
                    label.text = String.format("%d", it.value)
                }
                is FloatProperty -> {
                    it.value = sliderToProperty(
                        value,
                        BigDecimal.valueOf(it.minValue.toDouble()),
                        BigDecimal.valueOf(it.maxValue.toDouble())
                    ).toFloat().coerceIn(it.minValue, it.maxValue)
                    label.text = String.format("%.2g", it.value)
                }
                is DoubleProperty -> {
                    it.value = sliderToProperty(
                        value,
                        BigDecimal.valueOf(it.minValue),
                        BigDecimal.valueOf(it.maxValue)
                    ).toDouble().coerceIn(it.minValue, it.maxValue)
                    label.text = String.format("%.2g", it.value)
                }
                is FixedPointProperty -> {
                    it.value = sliderToProperty(
                        value,
                        it.minValue,
                        it.maxValue
                    ).coerceIn(it.minValue, it.maxValue)
                    label.text = String.format("%.2g", it.value)
                }
            }
        }
    }

    protected open fun sliderToProperty(
        value: Float,
        minValue: BigDecimal,
        maxValue: BigDecimal
    ): BigDecimal {
        val bdValue = BigDecimal.valueOf(value.toDouble())
        val a = bdValue.multiply(maxValue.subtract(minValue))
        return a.add(minValue)
    }

    protected open fun propertyToSlider(
        value: BigDecimal,
        minValue: BigDecimal,
        maxValue: BigDecimal
    ): Float {
        val a = value.subtract(minValue)
        val b = a.divide(maxValue.subtract(minValue), RoundingMode.HALF_UP)
        return b.toFloat()
    }

    companion object {
        fun get(
            parent: ViewGroup,
            property: BaseItem,
            displayLanguage: String?,
            hideOnDisable: Boolean
        ): SliderPreference {
            val layout = LayoutInflater.from(parent.context).inflate(
                R.layout.input_slider,
                parent,
                false
            ) as SliderPreference
            val title = layout.findViewById<MaterialTextView>(R.id.title)
            val slider = layout.findViewById<Slider>(R.id.slider)
            val label = layout.findViewById<MaterialTextView>(R.id.label)
            layout.setUp(title, slider, label, hideOnDisable)
            layout.setProperty(property, displayLanguage)
            return layout
        }
    }
}