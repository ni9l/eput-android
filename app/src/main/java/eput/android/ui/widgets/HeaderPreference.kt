package eput.android.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.textview.MaterialTextView
import eput.android.R
import eput.protocol.BaseItem
import eput.protocol.modifiers.Header

class HeaderPreference : MaterialTextView, Preference {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    private var property: Header? = null
    private var hideOnDisable: Boolean = false

    private fun setUp(hideOnDisable: Boolean) {
        this.hideOnDisable = hideOnDisable
    }

    override fun setProperty(
        property: BaseItem,
        displayLanguage: String?
    ) {
        isEnabled = true
        visibility = VISIBLE
        val prop = property as Header
        this.property = prop
        text = prop.getDisplayName(displayLanguage)
    }

    override fun clearProperty() {
        property = null
        text = ""
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
        fun get(
            parent: ViewGroup,
            property: BaseItem,
            displayLanguage: String?,
            hideOnDisable: Boolean
        ): HeaderPreference {
            val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.input_header, parent, false) as HeaderPreference
            layout.setUp(hideOnDisable)
            layout.setProperty(property, displayLanguage)
            return layout
        }
    }
}