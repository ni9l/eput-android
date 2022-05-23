package eput.android.ui.settings

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import eput.android.R
import eput.android.bytesToBase64
import eput.android.databinding.FragmentEditDeviceBinding
import eput.android.db.DeviceWithCustomizations
import eput.android.getTypeDisplayName
import java.util.*

class DeviceEditFragment : Fragment() {
    private val viewModel: SettingsViewModel by activityViewModels()
    private lateinit var binding: FragmentEditDeviceBinding
    private var device: DeviceWithCustomizations? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditDeviceBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        binding.buttonClearUi.isEnabled = false
        binding.buttonManageConfig.isEnabled = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity)
            .supportActionBar?.setTitle(R.string.title_edit_device)
        val index = viewModel.selectedDeviceIndex
        if (index < 0) {
            finish()
        }
        viewModel.getDevices().observe(viewLifecycleOwner) {
            val dev = it[index]
            device = dev
            setDevice(dev)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_edit_device, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_apply -> {
                saveChanges()
                true
            }
            R.id.action_delete -> {
                deleteDevice()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setDevice(device: DeviceWithCustomizations) {
        binding.typeInfo.setText(getTypeDisplayName(device.knownDevice.type))
        binding.nameInput.setText(device.knownDevice.name)
        val uiConfCount = device.customizedPreferences.size
        if (uiConfCount > 0) {
            binding.uiInfo.text = getString(R.string.detail_ui_form, uiConfCount)
            binding.buttonClearUi.isEnabled = true
        } else {
            binding.uiInfo.text = getString(R.string.detail_ui_none)
            binding.buttonClearUi.isEnabled = false
        }
        val confCount = device.configurations.size
        if (confCount > 0) {
            binding.configInfo.text = getString(R.string.detail_configs_form, confCount)
            binding.buttonManageConfig.isEnabled = true
        } else {
            binding.configInfo.text = getString(R.string.detail_configs_none)
            binding.buttonManageConfig.isEnabled = false
        }
        binding.misc.text = bytesToBase64(device.knownDevice.deviceId)

        binding.buttonClearUi.setOnClickListener {
            viewModel.clearCustomizedPreferences(device.knownDevice.deviceId)
        }
        binding.buttonManageConfig.setOnClickListener {
            val act = requireActivity()
            if (act is SettingsActivity) {
                act.switchFragment(ConfigurationManagementFragment::class.qualifiedName!!)
            }
        }
        val preferredLanguage = device.knownDevice.preferredLanguage
        if (preferredLanguage != null) {
            val languageName = Locale.forLanguageTag(preferredLanguage).displayName
            binding.languageInfo.text = getString(R.string.detail_language_form, languageName)
            binding.buttonResetLanguage.isEnabled = true
        } else {
            binding.languageInfo.text = getString(R.string.detail_language_none)
            binding.buttonResetLanguage.isEnabled = false
        }
        binding.buttonResetLanguage.setOnClickListener {
            viewModel.updateKnownDevice(device.knownDevice.copy(preferredLanguage = null))
        }
    }

    private fun saveChanges() {
        val newName = binding.nameInput.text.toString()
        val newDev = device?.knownDevice?.copy(name = newName) ?: return
        viewModel.updateKnownDevice(newDev)
        finish()
    }

    private fun deleteDevice() {
        val dev = device?.knownDevice ?: return
        viewModel.deleteKnownDeviceAndRelations(dev)
        finish()
    }

    private fun finish() {
        viewModel.selectedDeviceIndex = -1
        parentFragmentManager.popBackStack()
    }
}