package eput.android.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.textview.MaterialTextView
import eput.android.R
import eput.protocol.BaseItem
import eput.protocol.properties.NumberListProperty

class NumberListButtonsPreference : LinearLayoutCompat, Preference {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private lateinit var title: MaterialTextView
    private lateinit var text: MaterialTextView
    private lateinit var buttonUp: ImageButton
    private lateinit var buttonDown: ImageButton
    private var property: NumberListProperty? = null
    private var hideOnDisable: Boolean = false

    private fun setUp(
        titleView: MaterialTextView,
        textView: MaterialTextView,
        buttonUpView: ImageButton,
        buttonDownView: ImageButton,
        hideOnDisable: Boolean
    ) {
        this.hideOnDisable = hideOnDisable
        title = titleView
        text = textView
        buttonUp = buttonUpView
        buttonDown = buttonDownView
        buttonUp.setOnClickListener {
            property?.let {
                val newIndex = it.selectedIndex + 1
                setVal(newIndex)
            }
        }
        buttonDown.setOnClickListener {
            property?.let {
                val newIndex = it.selectedIndex - 1
                setVal(newIndex)
            }
        }
    }

    override fun setProperty(property: BaseItem, displayLanguage: String?) {
        isEnabled = true
        visibility = VISIBLE
        val prop = property as NumberListProperty
        this.property = prop
        title.text = prop.getDisplayName(displayLanguage)
        setSelected(prop)
    }

    override fun clearProperty() {
        property = null
        title.text = ""
        text.text = ""
        buttonUp.isEnabled = false
        buttonDown.isEnabled = false
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        title.isEnabled = enabled
        text.isEnabled = enabled
        buttonUp.isEnabled = enabled
        buttonDown.isEnabled = enabled
        if (enabled) {
            property?.let { setSelected(it) }
        }
    }

    override fun setEnabled(enabled: Boolean, ids: List<String>) {
        if (property?.id in ids) {
            isEnabled = enabled
            if (hideOnDisable) {
                visibility = if (enabled) VISIBLE else GONE
            }
        }
    }

    private fun setVal(index: Int) {
        property?.let {
            it.selectedIndex = index
            setSelected(it)
        }
    }

    private fun setSelected(prop: NumberListProperty) {
        text.text = prop.selectedStringValue
        when {
            prop.selectedIndex <= 0 -> {
                buttonUp.isEnabled = true
                buttonDown.isEnabled = false
            }
            prop.selectedIndex >= prop.entryCount - 1 -> {
                buttonUp.isEnabled = false
                buttonDown.isEnabled = true
            }
            else -> {
                buttonUp.isEnabled = true
                buttonDown.isEnabled = true
            }
        }
    }

    companion object {
        fun get(
            parent: ViewGroup,
            property: BaseItem,
            displayLanguage: String?,
            hideOnDisable: Boolean
        ): NumberListButtonsPreference {
            val layout = LayoutInflater.from(parent.context).inflate(
                R.layout.input_number_list_buttons,
                parent,
                false
            ) as NumberListButtonsPreference
            val title = layout.findViewById<MaterialTextView>(R.id.title)
            val text = layout.findViewById<MaterialTextView>(R.id.text)
            val buttonUp = layout.findViewById<ImageButton>(R.id.button_up)
            val buttonDown = layout.findViewById<ImageButton>(R.id.button_down)
            layout.setUp(title, text, buttonUp, buttonDown, hideOnDisable)
            layout.setProperty(property, displayLanguage)
            return layout
        }
    }
}