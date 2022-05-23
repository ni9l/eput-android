package eput.android.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import com.google.android.material.textview.MaterialTextView
import eput.android.R
import eput.protocol.BaseItem
import eput.protocol.properties.OneOutOfMProperty

class DropdownPreference : LinearLayoutCompat, Preference {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private lateinit var title: MaterialTextView
    private lateinit var spinner: Spinner
    private lateinit var adapter: ArrayAdapter<String>
    private var property: OneOutOfMProperty? = null
    private var hideOnDisable: Boolean = false

    private fun setUp(
        titleView: MaterialTextView,
        spinnerView: Spinner,
        hideOnDisable: Boolean
    ) {
        this.hideOnDisable = hideOnDisable
        title = titleView
        spinner = spinnerView
        adapter = ArrayAdapter(context, R.layout.entry_text_list)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                property?.let {
                    if (position < 0) {
                        it.selectedIndex = -1
                    } else {
                        it.selectedIndex = position
                    }
                    applyDependencies(it)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Ignored.
            }
        }
    }

    override fun setProperty(property: BaseItem, displayLanguage: String?) {
        isEnabled = true
        visibility = VISIBLE
        val prop = property as OneOutOfMProperty
        this.property = prop
        title.text = prop.getDisplayName(displayLanguage)
        val items = prop.entries.map { it.getTranslated(displayLanguage) }
        adapter.clear()
        adapter.addAll(items)
        spinner.setSelection(prop.selectedIndex)
        applyDependencies(prop)
    }

    override fun clearProperty() {
        property = null
        title.text = ""
        adapter.clear()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        title.isEnabled = enabled
        spinner.isEnabled = enabled
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
        ): DropdownPreference {
            val layout = LayoutInflater.from(parent.context).inflate(
                R.layout.input_dropdown,
                parent,
                false
            ) as DropdownPreference
            val title = layout.findViewById<MaterialTextView>(R.id.title)
            val spinner = layout.findViewById<Spinner>(R.id.spinner)
            layout.setUp(title, spinner, hideOnDisable)
            layout.setProperty(property, displayLanguage)
            return layout
        }
    }
}