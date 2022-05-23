package eput.android.ui.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputEditText
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import eput.android.R
import eput.protocol.BaseItem
import eput.protocol.properties.StringProperty

class ColorStringPreference : TextInput, Preference {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private val regex = Regex(HEX_COLOR_PATTERN)
    private lateinit var fragmentManager: FragmentManager
    private var property: StringProperty? = null
    private var hideOnDisable: Boolean = false

    private fun setUp(
        view: TextInputEditText,
        fragmentManager: FragmentManager,
        hideOnDisable: Boolean
    ) {
        this.hideOnDisable = hideOnDisable
        super.setUp(view) { text ->
            try {
                val trimmed = text.trim()
                if (regex.matches(trimmed)) {
                    property?.value = trimmed
                    onError(null)
                } else {
                    onError(context.getString(R.string.error_color_format))
                }
            } catch (e: IllegalArgumentException) {
                onError(context.getString(R.string.error_invalid_string))
            }
        }
        this.fragmentManager = fragmentManager
        setEndIconOnClickListener { openDialog() }
    }

    override fun setProperty(property: BaseItem, displayLanguage: String?) {
        isEnabled = true
        visibility = VISIBLE
        val prop = property as StringProperty
        this.property = prop
        hint = prop.getDisplayName(displayLanguage)
        val text = prop.value.ifBlank { "#00000000" }
        input.setText(text, TextView.BufferType.EDITABLE)
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
            val color = stringToColor(it.value)
            val colorPicker = ColorPickerDialog.newBuilder()
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setShowAlphaSlider(true)
                .setDialogTitle(R.string.dialog_title_color)
                .setAllowPresets(true)
                .setAllowCustom(true)
                .setColor(color)
                .create()
            colorPicker.setColorPickerDialogListener(object : ColorPickerDialogListener {
                override fun onColorSelected(dialogId: Int, color: Int) {
                    val text = colorToString(color)
                    it.value = text
                    input.setText(text, TextView.BufferType.EDITABLE)
                }

                override fun onDialogDismissed(dialogId: Int) {
                    // Ignore.
                }
            })
            colorPicker.show(fragmentManager, "color_picker")
        }
    }

    private fun stringToColor(hex: String): Int {
        val trimmed = hex.trim()
        return Color.parseColor(trimmed)
    }

    private fun colorToString(color: Int): String {
        val a = Color.alpha(color)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return "#%2X%2X%2X%2X".format(a, r, g, b)
    }

    companion object {
        private const val HEX_COLOR_PATTERN = "^#(?:[a-fA-F0-9]{2}){3,4}$"

        fun get(
            parent: ViewGroup,
            property: BaseItem,
            displayLanguage: String?,
            hideOnDisable: Boolean,
            fragmentManager: FragmentManager
        ): ColorStringPreference {
            val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.input_color, parent, false) as ColorStringPreference
            val input = layout.findViewById<TextInputEditText>(R.id.input)
            layout.setUp(input, fragmentManager, hideOnDisable)
            layout.setProperty(property, displayLanguage)
            return layout
        }
    }
}