package eput.android.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import androidx.fragment.app.FragmentManager
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textview.MaterialTextView
import eput.android.R
import eput.android.db.CustomizedPreference
import eput.android.instantiatePreference
import eput.android.setPreferenceProperty
import eput.protocol.BaseItem
import eput.protocol.properties.ArrayProperty

class ArrayPreference : LinearLayoutCompat, Preference {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private val itemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>,
            view: View,
            position: Int,
            id: Long
        ) {
            setSelected(position)
            container.children.forEach { if (it is Preference) it.onSetUpComplete() }
        }

        override fun onNothingSelected(parent: AdapterView<*>) {
            // Ignore
        }
    }

    private lateinit var fragmentManager: FragmentManager
    private lateinit var title: MaterialTextView
    private lateinit var spinner: Spinner
    private lateinit var container: LinearLayout
    private var selectedProfile = 0
    private var property: ArrayProperty? = null
    private var displayLanguage: String? = null
    private var uiConfig: List<CustomizedPreference> = listOf()
    private var hideOnDisable: Boolean = false

    private fun setUp(
        titleView: MaterialTextView,
        spinnerView: Spinner,
        containerView: LinearLayout,
        fragmentManager: FragmentManager,
        hideOnDisable: Boolean
    ) {
        this.hideOnDisable = hideOnDisable
        this.fragmentManager = fragmentManager
        title = titleView
        spinner = spinnerView
        container = containerView
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(R.attr.backgroundColor, typedValue, true)
        val backgroundColor = AppCompatResources.getColorStateList(context, typedValue.resourceId)
        theme.resolveAttribute(R.attr.colorOnBackground, typedValue, true)
        val borderColor = AppCompatResources.getColorStateList(context, typedValue.resourceId)
        val model = ShapeAppearanceModel.builder(
            context,
            R.style.ShapeAppearance_MaterialComponents_MediumComponent,
            0)
            .build()
        val drawable = MaterialShapeDrawable(model)
        drawable.fillColor = backgroundColor
        drawable.setStrokeTint(borderColor)
        drawable.strokeWidth = context.resources.getDimension(R.dimen.border_width)
        background = drawable
    }

    override fun setProperty(property: BaseItem, displayLanguage: String?) {
        isEnabled = true
        visibility = VISIBLE
        val prop = property as ArrayProperty
        this.property = prop
        this.displayLanguage = displayLanguage
        title.text = prop.getDisplayName(displayLanguage)
        if (prop.profileCount > 0) {
            for (p in prop.getProfile(0)) {
                instantiatePreference(
                    p,
                    uiConfig,
                    container,
                    displayLanguage,
                    hideOnDisable,
                    fragmentManager
                ) { }
            }
        }
        val items = (0 until prop.profileCount).map {
            context.getString(R.string.profile_name, it + 1)
        }
        val adapter = ArrayAdapter(context, R.layout.entry_text_list, items)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = itemSelectedListener
    }

    override fun clearProperty() {
        property = null
        title.text = ""
        spinner.onItemSelectedListener = null
        spinner.adapter = null
        container.children.forEach { if (it is Preference) it.clearProperty() }
        container.removeAllViews()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        title.isEnabled = enabled
        spinner.isEnabled = enabled
        container.children.forEach { it.isEnabled = enabled }
        if (enabled) {
            container.children.forEach { if (it is Preference) it.onSetUpComplete() }
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

    override fun onSetUpComplete() {
        container.children.forEach { if (it is Preference) it.onSetUpComplete() }
    }

    fun setUiConfig(uiConfig: List<CustomizedPreference>) {
        this.uiConfig = uiConfig
    }

    private fun setSelected(index: Int) {
        if (!isEnabled) return
        property?.let {
            if (-1 < index && index < it.profileCount) {
                selectedProfile = index
                container.children.forEachIndexed { viewIndex, view ->
                    if (view is Preference) {
                        view.clearProperty()
                        val prop = it.getProfile(index)[viewIndex]
                        setPreferenceProperty(view, prop, uiConfig, displayLanguage)
                    }
                }
            }
        }
    }

    companion object {
        fun get(
            parent: ViewGroup,
            property: BaseItem,
            displayLanguage: String?,
            hideOnDisable: Boolean,
            fragmentManager: FragmentManager,
            uiConfig: List<CustomizedPreference>,
        ): ArrayPreference {
            val layout = LayoutInflater.from(parent.context).inflate(
                R.layout.input_array,
                parent,
                false
            ) as ArrayPreference
            val title = layout.findViewById<MaterialTextView>(R.id.title)
            val spinner = layout.findViewById<Spinner>(R.id.spinner)
            val container = layout.findViewById<LinearLayout>(R.id.container)
            layout.setUp(title, spinner, container, fragmentManager, hideOnDisable)
            layout.setUiConfig(uiConfig)
            layout.setProperty(property, displayLanguage)
            return layout
        }
    }
}