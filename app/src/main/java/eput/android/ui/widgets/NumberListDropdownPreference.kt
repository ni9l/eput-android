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
import com.google.android.material.textview.MaterialTextView
import eput.android.R
import eput.protocol.BaseItem
import eput.protocol.properties.NumberListProperty

class NumberListDropdownPreference : LinearLayoutCompat, Preference {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private lateinit var title: MaterialTextView
    private lateinit var spinner: Spinner
    private var property: NumberListProperty? = null
    private var hideOnDisable: Boolean = false

    private fun setUp(
        titleView: MaterialTextView,
        spinnerView: Spinner,
        hideOnDisable: Boolean
    ) {
        this.hideOnDisable = hideOnDisable
        title = titleView
        spinner = spinnerView
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                property?.let {
                    it.selectedIndex = position
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Ignore
            }
        }
    }

    override fun setProperty(property: BaseItem, displayLanguage: String?) {
        isEnabled = true
        visibility = VISIBLE
        val prop = property as NumberListProperty
        this.property = prop
        title.text = prop.getDisplayName(displayLanguage)
        val items = prop.stringEntries
        val adapter = ArrayAdapter(context, R.layout.entry_text_list, items)
        spinner.adapter = adapter
        spinner.setSelection(prop.selectedIndex)
    }

    override fun clearProperty() {
        property = null
        title.text = ""
        spinner.adapter = null
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

    companion object {
        fun get(
            parent: ViewGroup,
            property: BaseItem,
            displayLanguage: String?,
            hideOnDisable: Boolean
        ): NumberListDropdownPreference {
            val layout = LayoutInflater.from(parent.context).inflate(
                R.layout.input_number_list_dropdown,
                parent,
                false
            ) as NumberListDropdownPreference
            val title = layout.findViewById<MaterialTextView>(R.id.title)
            val spinner = layout.findViewById<Spinner>(R.id.spinner)
            layout.setUp(title, spinner, hideOnDisable)
            layout.setProperty(property, displayLanguage)
            return layout
        }
    }
}