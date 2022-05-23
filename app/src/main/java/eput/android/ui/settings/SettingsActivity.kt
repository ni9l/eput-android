package eput.android.ui.settings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import eput.android.DataService
import eput.android.R
import eput.android.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var binding: ActivitySettingsBinding
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val serviceBinder = service as DataService.DataBinder
            viewModel.setBinder(serviceBinder)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            viewModel.clearBinder()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Intent(this, DataService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, MainSettingsFragment())
                .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        pref.fragment?.let { switchFragment(it) }
        return true
    }

    fun switchFragment(className: String) {
        val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, className)
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.settings_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
