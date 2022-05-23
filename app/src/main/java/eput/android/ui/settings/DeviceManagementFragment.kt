package eput.android.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import eput.android.R
import eput.android.databinding.FragmentRecyclerBinding
import eput.android.databinding.VhDeviceBinding
import eput.android.db.DeviceWithCustomizations
import eput.android.getDeviceDisplayName

class DeviceManagementFragment : Fragment() {
    private val viewModel: SettingsViewModel by activityViewModels()
    private lateinit var binding: FragmentRecyclerBinding
    private lateinit var adapter: DeviceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecyclerBinding.inflate(inflater, container, false)
        adapter = DeviceAdapter {
            onItemClick(it)
        }
        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recycler.addItemDecoration(divider)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity)
            .supportActionBar?.setTitle(R.string.title_device_management)
        viewModel.getDevices().observe(viewLifecycleOwner) {
            adapter.submitList(it)
            if (it.isEmpty()) {
                binding.recycler.visibility = View.GONE
                binding.textEmpty.visibility = View.VISIBLE
            } else {
                binding.recycler.visibility = View.VISIBLE
                binding.textEmpty.visibility = View.GONE
            }
        }
    }

    private fun onItemClick(position: Int) {
        val act = requireActivity()
        if (act is SettingsActivity) {
            viewModel.selectedDeviceIndex = position
            act.switchFragment(DeviceEditFragment::class.qualifiedName!!)
        }
    }

    private class DeviceAdapter(private val clickListener: (pos: Int) -> Unit) :
        ListAdapter<DeviceWithCustomizations, DeviceViewHolder>(DIFF_CALLBACK) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
            val binding = VhDeviceBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
            return DeviceViewHolder(binding)
        }

        override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
            holder.setData(getItem(position))
            holder.itemView.setOnClickListener {
                clickListener(position)
            }
        }
    }

    private class DeviceViewHolder(private val binding: VhDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(data: DeviceWithCustomizations) {
            val context = binding.root.context
            binding.name.text = getDeviceDisplayName(context, data.knownDevice)
            val configs = data.configurations.size
            val uiChanged = data.customizedPreferences.isNotEmpty()
            binding.detail.text = if (configs > 0) {
                if (uiChanged) {
                    context.getString(R.string.detail_device_ui, configs)
                } else {
                    context.getString(R.string.detail_device_no_ui, configs)
                }
            } else {
                context.getString(R.string.detail_device_none)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DeviceWithCustomizations>() {
            override fun areItemsTheSame(
                oldItem: DeviceWithCustomizations,
                newItem: DeviceWithCustomizations
            ): Boolean {
                return oldItem.knownDevice.deviceId.contentEquals(newItem.knownDevice.deviceId)
            }

            override fun areContentsTheSame(
                oldItem: DeviceWithCustomizations,
                newItem: DeviceWithCustomizations
            ): Boolean {
                return oldItem.knownDevice == newItem.knownDevice
                        && oldItem.configurations.size == newItem.configurations.size
                        && oldItem.customizedPreferences.size == newItem.customizedPreferences.size
            }
        }
    }
}