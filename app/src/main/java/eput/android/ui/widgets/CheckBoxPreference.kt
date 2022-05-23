package eput.android.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import com.google.android.material.textview.MaterialTextView
import eput.android.R
import eput.protocol.BaseItem
import eput.protocol.properties.NOutOfMProperty

class CheckBoxPreference : LinearLayoutCompat, Preference {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private val checkBoxes: MutableList<CheckBox> = mutableListOf()
    private val childState = mutableListOf<Boolean>()
    private val onCheckedListener = CompoundButton
        .OnCheckedChangeListener { buttonView, isChecked ->
            property?.let {
                val index = buttonView.tag as Int
                it.setSelected(index, isChecked)
                it.dependencyIdMap[index]?.let { ids ->
                    applyDependencies(isChecked, ids)
                }
            }
        }
    private lateinit var title: MaterialTextView
    private var property: NOutOfMProperty? = null
    private var hideOnDisable: Boolean = false

    private fun setUp(titleView: MaterialTextView, hideOnDisable: Boolean) {
        this.hideOnDisable = hideOnDisable
        title = titleView
    }

    override fun setProperty(property: BaseItem, displayLanguage: String?) {
        isEnabled = true
        visibility = VISIBLE
        val prop = property as NOutOfMProperty
        this.property = prop
        title.text = prop.getDisplayName(displayLanguage)
        prop.entries.forEachIndexed { index, entry ->
            val checkBox = CheckBox(context)
            checkBox.id = generateViewId()
            checkBox.tag = index
            checkBox.text = entry.getTranslated(displayLanguage)
            checkBox.isChecked = prop.isSelected(index)
            checkBox.setOnCheckedChangeListener(onCheckedListener)
            checkBox.isEnabled = isEnabled
            checkBoxes.add(checkBox)
            val checkBoxParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            addView(checkBox, checkBoxParams)
        }
    }

    override fun clearProperty() {
        property = null
        title.text = ""
        checkBoxes.forEach { removeView(it) }
        checkBoxes.clear()
        childState.clear()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        title.isEnabled = enabled
        for (box in checkBoxes) {
            box.isEnabled = enabled
        }
    }

    override fun setEnabled(enabled: Boolean, ids: List<String>) {
        property?.let {
            if (it.id in ids && isEnabled != enabled) {
                if (enabled) {
                    isEnabled = enabled
                    restoreChildState()
                } else {
                    saveChildState()
                    isEnabled = enabled
                }
                if (hideOnDisable) {
                    visibility = if (enabled) VISIBLE else GONE
                }
            } else {
                it.entries.forEachIndexed { index, entry ->
                    if (entry.id in ids) {
                        if (isEnabled) {
                            val v = findViewWithTag<CheckBox>(index)
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
        checkBoxes.forEach { childState.add(it.isEnabled) }
    }

    private fun restoreChildState() {
        checkBoxes.forEachIndexed { index, view ->
            view.isEnabled = childState.getOrNull(index) ?: view.isEnabled
        }
        childState.clear()
    }

    override fun onSetUpComplete() {
        property?.let {
            it.entries.indices.forEach { index ->
                it.dependencyIdMap[index]?.let { ids ->
                    applyDependencies(it.isSelected(index), ids)
                }
            }
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
        ): CheckBoxPreference {
            val layout = LayoutInflater.from(parent.context).inflate(
                R.layout.input_checkbox,
                parent,
                false
            ) as CheckBoxPreference
            val title = layout.findViewById<MaterialTextView>(R.id.title)
            layout.setUp(title, hideOnDisable)
            layout.setProperty(property, displayLanguage)
            return layout
        }
    }
}