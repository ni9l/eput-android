package eput.android.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import eput.android.R

class MainSettingsFragment : PreferenceFragmentCompat() {
    private val viewModel: SettingsViewModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        findPreference<Preference>(getString(R.string.key_device_management))?.fragment =
            DeviceManagementFragment::class.qualifiedName
        findPreference<Preference>(getString(R.string.key_about))?.fragment =
            AboutFragment::class.qualifiedName
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity)
            .supportActionBar?.setTitle(R.string.title_main_preference)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        return if (preference.key == getString(R.string.key_reset_ui)) {
            deleteUiConfigurations()
            true
        } else {
            super.onPreferenceTreeClick(preference)
        }
    }

    private fun deleteUiConfigurations() {
        Toast.makeText(requireContext(), R.string.msg_reset_ui_running, Toast.LENGTH_SHORT).show()
        viewModel.clearCustomizedPreferences()
    }
}