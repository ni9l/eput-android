package eput.android.ui.widgets

import eput.protocol.BaseItem

interface Preference {
    fun setProperty(property: BaseItem, displayLanguage: String?)
    fun clearProperty()
    fun setEnabled(enabled: Boolean, ids: List<String>)
    fun onSetUpComplete() {
        // Ignore.
    }
}