package eput.android.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import com.google.android.material.textview.MaterialTextView
import eput.android.R
import eput.protocol.BaseItem
import eput.protocol.properties.OneOutOfMProperty

class RadioPreference : LinearLayoutCompat, Preference {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private val childState = mutableListOf<Boolean>()
    private lateinit var title: MaterialTextView
    private lateinit var radioGroup: RadioGroup
    private var property: OneOutOfMProperty? = null
    private var hideOnDisable: Boolean = false

    private fun setUp(
        titleView: MaterialTextView,
        radioGroupView: RadioGroup,
        hideOnDisable: Boolean
    ) {
        this.hideOnDisable = hideOnDisable
        title = titleView
        radioGroup = radioGroupView
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            property?.let {
                if (checkedId < 0) {
                    it.selectedIndex = -1
                } else {
                    it.selectedIndex = checkedId
                }
                applyDependencies(it)
            }
        }
    }

    override fun setProperty(property: BaseItem, displayLanguage: String?) {
        isEnabled = true
        visibility = VISIBLE
        val prop = property as OneOutOfMProperty
        this.property = prop
        title.text = prop.getDisplayName(displayLanguage)
        prop.entries.forEachIndexed { index, entry ->
            val radioButton = RadioButton(radioGroup.context)
            radioButton.id = index
            radioButton.text = entry.getTranslated(displayLanguage)
            radioButton.isEnabled = isEnabled
            val radioParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            radioGroup.addView(radioButton, radioParams)
        }
        radioGroup.check(prop.selectedIndex)
    }

    override fun clearProperty() {
        property = null
        title.text = ""
        radioGroup.removeAllViews()
        childState.clear()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        title.isEnabled = enabled
        radioGroup.isEnabled = enabled
        radioGroup.children.forEach { it.isEnabled = enabled }
    }

    override fun setEnabled(enabled: Boolean, ids: List<String>) {
        property?.let {
            if (it.id in ids) {
                if (isEnabled != enabled) {
                    if (enabled) {
                        isEnabled = enabled
                        restoreChildState()
                    } else {
                        saveChildState()
                        isEnabled = enabled
                    }
                }
                if (hideOnDisable) {
                    visibility = if (enabled) VISIBLE else GONE
                }
            } else {
                it.entries.forEachIndexed { index, entry ->
                    if (entry.id in ids) {
                        if (isEnabled) {
                            val v = radioGroup.findViewById<View>(index)
                            v.isEnabled = enabled
                            if (hideOnDisable) {
                                v.visibility = if (enabled) VISIBLE else GONE
                            }
                        } else {
                            childState[index] = enabled
                        }
                    }
                }
            }
        }
    }

    private fun saveChildState() {
        childState.clear()
        radioGroup.children.forEach { childState.add(it.isEnabled) }
    }

    private fun restoreChildState() {
        radioGroup.children.forEachIndexed { index, view ->
            view.isEnabled = childState.getOrNull(index) ?: view.isEnabled
        }
        childState.clear()
    }

    override fun onSetUpComplete() {
        property?.let {
            applyDependencies(it)
        }
    }

    private fun applyDependencies(property: OneOutOfMProperty) {
        val par = parent
        val ids = property.dependencyIds
        if (par is ViewGroup && ids.isNotEmpty()) {
            val enabledIds = property.dependencyIdMap[property.selectedIndex] ?: emptyList()
            val disabledIds = ids.minus(enabledIds).toList()
            par.children.forEach { child ->
                if (child is Preference) {
                    child.setEnabled(true, enabledIds)
                    child.setEnabled(false, disabledIds)
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
        ): RadioPreference {
            val layout = LayoutInflater.from(parent.context).inflate(
                R.layout.input_radio,
                parent,
                false
            ) as RadioPreference
            val title = layout.findViewById<MaterialTextView>(R.id.title)
            val radioGroup = layout.findViewById<RadioGroup>(R.id.radio_group)
            layout.setUp(title, radioGroup, hideOnDisable)
            layout.setProperty(property, displayLanguage)
            return layout
        }
    }
}