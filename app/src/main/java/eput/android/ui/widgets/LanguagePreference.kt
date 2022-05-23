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
import eput.protocol.properties.LanguageProperty

class LanguagePreference : LinearLayoutCompat, Preference {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private lateinit var title: MaterialTextView
    private lateinit var spinner: Spinner
    private lateinit var adapter: ArrayAdapter<String>
    private var property: LanguageProperty? = null
    private var hideOnDisable: Boolean = false
    private var languageChangedListener: (() -> Unit)? = null
    private var setSelectionCalled = false
    private val selectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            if (!setSelectionCalled) {
                property?.let {
                    if (position != it.selectedIndex) {
                        if (position < 0) {
                            it.selectedIndex = -1
                        } else {
                            it.selectedIndex = position
                        }
                        languageChangedListener?.invoke()
                    }
                }
            }
            setSelectionCalled = false
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // Ignored.
        }
    }

    private fun setUp(
        titleView: MaterialTextView,
        spinnerView: Spinner,
        hideOnDisable: Boolean,
        languageChangedListener: () -> Unit
    ) {
        this.hideOnDisable = hideOnDisable
        this.languageChangedListener = languageChangedListener
        title = titleView
        spinner = spinnerView
        adapter = ArrayAdapter(context, R.layout.entry_text_list)
        spinner.adapter = adapter
    }

    override fun setProperty(property: BaseItem, displayLanguage: String?) {
        isEnabled = true
        visibility = VISIBLE
        val prop = property as LanguageProperty
        this.property = prop
        title.text = prop.getDisplayName(displayLanguage)
        val items = prop.entries.map { it.getTranslated(displayLanguage) }
        adapter.clear()
        adapter.addAll(items)
        setSelectionCalled = true
        spinner.setSelection(prop.selectedIndex)
        spinner.onItemSelectedListener = selectedListener
    }

    override fun clearProperty() {
        spinner.onItemSelectedListener = null
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

    companion object {
        fun get(
            parent: ViewGroup,
            property: BaseItem,
            displayLanguage: String?,
            hideOnDisable: Boolean,
            languageChangedListener: () -> Unit
        ): LanguagePreference {
            val layout = LayoutInflater.from(parent.context).inflate(
                R.layout.input_language,
                parent,
                false
            ) as LanguagePreference
            val title = layout.findViewById<MaterialTextView>(R.id.title)
            val spinner = layout.findViewById<Spinner>(R.id.spinner)
            layout.setUp(title, spinner, hideOnDisable, languageChangedListener)
            layout.setProperty(property, displayLanguage)
            return layout
        }
    }
}