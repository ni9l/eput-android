package eput.android.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.lifecycleScope
import eput.android.DataService
import eput.android.R
import eput.android.databinding.ActivityUiEditBinding
import eput.android.databinding.EntryUiEditBinding
import eput.android.db.CustomizedPreference
import eput.android.getAvailablePreferencesForProperty
import eput.android.getPreferenceDisplayName
import eput.protocol.NamedItem
import eput.protocol.properties.ArrayProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UiEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUiEditBinding
    private val serviceBinder = MutableLiveData<DataService.DataBinder?>(null)
    private val device = Transformations.switchMap(serviceBinder) { binder ->
        binder?.getDeviceWithCustomizations()
            ?: MutableLiveData<DataService.DeviceSet?>(null)
    }
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            serviceBinder.value = service as DataService.DataBinder
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            serviceBinder.value = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Intent(this, DataService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        binding = ActivityUiEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        device.observe(this) { dev ->
            dev?.let {
                setupUi(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    private fun setupUi(deviceSet: DataService.DeviceSet) {
        val root = binding.container
        root.removeAllViews()
        for (prop in deviceSet.device.getProperties().filterIsInstance<NamedItem>()) {
            if (prop is ArrayProperty) {
                setupArrayUi(
                    root,
                    deviceSet.uiLanguage,
                    deviceSet.device.ids,
                    deviceSet.customizations.customizedPreferences,
                    prop
                )
            } else {
                setupItemUi(
                    root,
                    deviceSet.uiLanguage,
                    deviceSet.device.ids,
                    deviceSet.customizations.customizedPreferences,
                    prop
                )
            }
        }
    }

    private fun setupArrayUi(
        root: ViewGroup,
        uiLanguage: String?,
        deviceId: ByteArray,
        customizedPreferences: List<CustomizedPreference>,
        property: ArrayProperty
    ) {
        if (property.profileCount > 0) {
            for (prop in property.getProfile(0).filterIsInstance<NamedItem>()) {
                setupItemUi(root, uiLanguage, deviceId, customizedPreferences, prop)
            }
        }
    }

    private fun setupItemUi(
        root: ViewGroup,
        uiLanguage: String?,
        deviceId: ByteArray,
        customizedPreferences: List<CustomizedPreference>,
        property: NamedItem
    ) {
        val items = getAvailablePreferencesForProperty(property)
        if (items.size > 1) {
            val prefSel = customizedPreferences.find {
                it.propertyId == property.id
            } ?: CustomizedPreference(null, deviceId, property.id, items[0])
            val widgetBinding = EntryUiEditBinding
                .inflate(layoutInflater, root, true)
            widgetBinding.title.text = property.getDisplayName(uiLanguage)
            val displayItems = items.map { getString(getPreferenceDisplayName(it)) }
            val adapter = ArrayAdapter(this, R.layout.entry_text_list, displayItems)
            widgetBinding.spinnerOptions.adapter = adapter
            widgetBinding.spinnerOptions.setSelection(
                items.indexOf(prefSel.preference),
                false
            )
            widgetBinding.spinnerOptions.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View,
                        position: Int,
                        id: Long
                    ) {
                        val selection = items[position]
                        setUiChoice(prefSel, selection)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Ignore
                    }
                }
        }
    }

    private fun setUiChoice(customizedPreference: CustomizedPreference, selection: String) {
        serviceBinder.value?.let { binder ->
            lifecycleScope.launch(Dispatchers.IO) {
                val newPrefSel = customizedPreference.copy(preference = selection)
                binder.updatePreferenceSelection(newPrefSel)
            }
        }
    }
}