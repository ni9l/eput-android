package eput.android.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.children
import com.google.android.material.switchmaterial.SwitchMaterial
import eput.android.R
import eput.protocol.BaseItem
import eput.protocol.properties.BoolProperty

class SwitchPreference : SwitchMaterial, Preference {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private var property: BoolProperty? = null
    private var hideOnDisable: Boolean = false

    private fun setUp(hideOnDisable: Boolean) {
        this.hideOnDisable = hideOnDisable
    }

    override fun setProperty(property: BaseItem, displayLanguage: String?) {
        isEnabled = true
        visibility = VISIBLE
        val prop = property as BoolProperty
        this.property = prop
        text = prop.getDisplayName(displayLanguage)
        isChecked = prop.value
        setOnCheckedChangeListener { _, isChecked ->
            prop.value = isChecked
            applyDependencies(isChecked, prop.getDependencyIds(true))
            applyDependencies(!isChecked, prop.getDependencyIds(false))
        }
    }

    override fun clearProperty() {
        property = null
        text = ""
        setOnCheckedChangeListener(null)
        isChecked = false
    }

    override fun setEnabled(enabled: Boolean, ids: List<String>) {
        if (property?.id in ids) {
            isEnabled = enabled
            if (hideOnDisable) {
                visibility = if (enabled) VISIBLE else GONE
            }
        }
    }

    override fun onSetUpComplete() {
        property?.let {
            applyDependencies(it.value, it.getDependencyIds(true))
            applyDependencies(!it.value, it.getDependencyIds(false))
        }
    }

    private fun applyDependencies(enabled: Boolean, ids: List<String>) {
        val par = parent
        if (par is ViewGroup && ids.isNotEmpty()) {
            par.children.forEach { child ->
                if (child is Preference) {
                    child.setEnabled(enabled, ids)
                }
            }
        }
    }

    companion object {
        fun get(
            parent: ViewGroup,
            property: BaseItem,
            displayLanguage: String?,
            hideOnDisable: Boolean
        ): SwitchPreference {
            val layout = LayoutInflater.from(parent.context).inflate(
                R.layout.input_switch,
                parent,
                false
            ) as SwitchPreference
            layout.setUp(hideOnDisable)
            layout.setProperty(property, displayLanguage)
            return layout
        }
    }
}