package eput.android.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import eput.android.R
import eput.protocol.BaseItem
import eput.protocol.properties.*

class StringPreference : TextInput, Preference {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private val regex = Regex(MAIL_PATTERN)
    private var property: StringProperty? = null
    private var hideOnDisable: Boolean = false

    private fun setUp(
        view: TextInputEditText,
        hideOnDisable: Boolean
    ) {
        this.hideOnDisable = hideOnDisable
        super.setUp(view) { text ->
            try {
                if (input.inputType == TYPE_MAIL) {
                    val trimmed = text.trim()
                    if (regex.matches(trimmed)) {
                        property?.value = trimmed
                        onError(null)
                    } else {
                        onError(context.getString(R.string.error_invalid_mail))
                    }
                } else {
                    property?.value = text
                    onError(null)
                }
            } catch (e: IllegalArgumentException) {
                onError(context.getString(R.string.error_invalid_string))
            }
        }
    }

    override fun setProperty(property: BaseItem, displayLanguage: String?) {
        isEnabled = true
        visibility = VISIBLE
        val prop = property as StringProperty
        this.property = prop
        hint = prop.getDisplayName(displayLanguage)
        input.setText(prop.value, TextView.BufferType.EDITABLE)
        counterMaxLength = prop.maxLength
        input.inputType = when (prop) {
            is StrEmailProperty -> TYPE_MAIL
            is StrPasswordProperty -> TYPE_PW
            is StrPhoneProperty -> TYPE_PHONE
            is StrUriProperty -> TYPE_URI
            else -> TYPE_TEXT
        }
        if (prop is StrPasswordProperty) {
            endIconMode = END_ICON_PASSWORD_TOGGLE
        }
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

    companion object {
        // Adapted from https://stackoverflow.com/a/44697205
        private const val MAIL_PATTERN =
            "(?!.*\\.\\.)^[^@\\s.]+(?:\\.[^@\\s]+)*@[^@\\s]+\\.[^@\\s.]+$"
        private const val TYPE_TEXT = EditorInfo.TYPE_CLASS_TEXT
        private const val TYPE_MAIL = EditorInfo.TYPE_CLASS_TEXT or
                EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        private const val TYPE_PHONE = EditorInfo.TYPE_CLASS_PHONE
        private const val TYPE_URI = EditorInfo.TYPE_CLASS_TEXT or
                EditorInfo.TYPE_TEXT_VARIATION_URI
        private const val TYPE_PW = EditorInfo.TYPE_CLASS_TEXT or
                EditorInfo.TYPE_TEXT_VARIATION_PASSWORD

        fun get(
            parent: ViewGroup,
            property: BaseItem,
            displayLanguage: String?,
            hideOnDisable: Boolean
        ): StringPreference {
            val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.input_string, parent, false) as StringPreference
            val input = layout.findViewById<TextInputEditText>(R.id.input)
            layout.setUp(input, hideOnDisable)
            layout.setProperty(property, displayLanguage)
            return layout
        }
    }
}