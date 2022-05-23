package eput.android.ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

open class TextInput : TextInputLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    protected lateinit var input: TextInputEditText

    protected open fun setUp(view: TextInputEditText, textListener: (String) -> Unit) {
        input = view
        input.addTextChangedListener { editable ->
            textListener(editable.toString())
        }
    }

    protected fun onError(message: String?) {
        if (message == null) {
            isErrorEnabled = false
        } else {
            isErrorEnabled = true
            error = message
        }
    }

    fun clear() {
        helperText = ""
        hint = ""
        counterMaxLength = 0
        isErrorEnabled = false
        error = ""
        input.setText("")
    }
}