package eput.android.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.divider.MaterialDivider
import eput.android.R

class DividerPreference : MaterialDivider {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    companion object {
        fun get(parent: ViewGroup): DividerPreference {
            return LayoutInflater.from(parent.context).inflate(
                R.layout.input_divider,
                parent,
                false
            ) as DividerPreference
        }
    }
}