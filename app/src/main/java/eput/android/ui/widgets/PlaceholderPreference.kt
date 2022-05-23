package eput.android.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.textview.MaterialTextView
import eput.android.R

class PlaceholderPreference : MaterialTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    companion object {
        fun get(parent: ViewGroup): PlaceholderPreference {
            return LayoutInflater.from(parent.context).inflate(
                R.layout.input_placeholder,
                parent,
                false
            ) as PlaceholderPreference
        }
    }
}