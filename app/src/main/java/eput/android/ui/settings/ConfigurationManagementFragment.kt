package eput.android.ui.settings

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.google.android.material.divider.MaterialDividerItemDecoration
import eput.android.R
import eput.android.copy
import eput.android.databinding.FragmentRecyclerBinding
import eput.android.databinding.VhConfigurationBinding
import eput.android.db.Configuration
import eput.android.ui.SaveActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ConfigurationManagementFragment : Fragment() {
    private val viewModel: SettingsViewModel by activityViewModels()
    private lateinit var binding: FragmentRecyclerBinding
    private lateinit var adapter: ConfigurationAdapter
    private var actionMode: ActionMode? = null
    private val exportConfigurations: MutableList<Configuration> = mutableListOf()
    private val getConfigFile = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) {
        if (it == null) {
            Toast.makeText(
                requireContext(),
                R.string.error_no_file_selected,
                Toast.LENGTH_SHORT
            ).show()
        } else {
            importConfiguration(it)
        }
    }
    private val getExportFolder = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) {
        if (it == null) {
            exportConfigurations.clear()
            Toast.makeText(
                requireContext(),
                R.string.error_no_folder_selected,
                Toast.LENGTH_SHORT
            ).show()
        } else {
            exportConfigurations(it)
        }
    }
    private val saveTag = registerForActivityResult(SaveActivity.Save()) { }
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            MenuInflater(context).inflate(R.menu.menu_manage_configurations_action_mode, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.action_export -> {
                    exportConfigurations.addAll(adapter.getSelectedItems())
                    getExportFolder.launch(null)
                    mode?.finish()
                    true
                }
                R.id.action_delete -> {
                    viewModel.deleteConfigurations(adapter.getSelectedItems())
                    mode?.finish()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            adapter.clearSelection()
            actionMode = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecyclerBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        adapter = ConfigurationAdapter(selectionListener = { selectionMode, selectedCount ->
            if (selectionMode) {
                if (actionMode == null) {
                    actionMode = (requireActivity() as AppCompatActivity)
                        .startSupportActionMode(actionModeCallback)
                }
                actionMode?.title =
                    getString(R.string.title_item_selection_action_mode, selectedCount)
            } else {
                actionMode?.finish()
            }
        }, clickListener = {
            val ndefData = viewModel.getNdefDataForSavedConfiguration(it)
            saveTag.launch(ndefData)
        })
        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recycler.addItemDecoration(divider)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity)
            .supportActionBar?.setTitle(R.string.title_manage_configuration)
        val index = viewModel.selectedDeviceIndex
        if (index < 0) {
            finish()
        }
        viewModel.getDevices().observe(viewLifecycleOwner) {
            val dev = it[index]
            val list = dev.configurations.toMutableList()
            adapter.submitList(list)
            if (list.isEmpty()) {
                binding.recycler.visibility = View.GONE
                binding.textEmpty.visibility = View.VISIBLE
            } else {
                binding.recycler.visibility = View.VISIBLE
                binding.textEmpty.visibility = View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_manage_configurations, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_import -> {
                getConfigFile.launch(arrayOf("application/json"))
                true
            }
            R.id.action_delete_all -> {
                deleteAll()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun deleteAll() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_clear_configs)
            .setMessage(R.string.dialog_desc_clear_configs)
            .setPositiveButton(R.string.button_ok) { _, _ ->
                val list = adapter.currentList
                if (list.isNotEmpty()) {
                    viewModel.clearConfigurations(list[0].deviceId)
                }
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    private fun finish() {
        requireActivity().onBackPressed()
    }

    private fun importConfiguration(file: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val inputS = requireContext().contentResolver.openInputStream(file) ?: return@launch
            InputStreamReader(inputS).use {
                val text = it.readText()
                val res = viewModel.importConfiguration(text)
                withContext(Dispatchers.Main) {
                    if (!res) {
                        Toast.makeText(
                            requireContext(),
                            R.string.error_import_invalid,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun exportConfigurations(folder: Uri) {
        val exportItems = exportConfigurations.copy()
        exportConfigurations.clear()
        lifecycleScope.launch(Dispatchers.IO) {
            val outputFolder = DocumentFile.fromTreeUri(requireContext(), folder)!!
            for (item in exportItems) {
                val json = viewModel.configurationToJson(item)
                val name = "config_${item.id}.json"
                val res = writeExportFile(outputFolder, name, json)
                if (!res) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            requireContext().getString(R.string.error_export_write_failed, name),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun writeExportFile(parent: DocumentFile, name: String, content: String): Boolean {
        val exportFile = parent.createFile("application/json", name) ?: return false
        try {
            val stream = requireContext().contentResolver
                .openOutputStream(exportFile.uri) ?: return false
            OutputStreamWriter(stream).use {
                it.write(content)
            }
            return true
        } catch (e: IOException) {
            return false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private class ConfigurationAdapter(
        private val selectionListener: (Boolean, Int) -> Unit,
        private val clickListener: (Configuration) -> Unit
    ) : ListAdapter<Configuration, ConfigurationViewHolder>(DIFF_CALLBACK) {
        private val selectedPositions = mutableListOf<Int>()
        private var selectionMode = false

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfigurationViewHolder {
            val binding = VhConfigurationBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
            return ConfigurationViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ConfigurationViewHolder, position: Int) {
            val itemWasSelected = position in selectedPositions
            holder.setData(getItem(position), selectionMode, itemWasSelected)
            holder.itemView.setOnLongClickListener {
                selectItem(position, !itemWasSelected)
                true
            }
            holder.itemView.setOnClickListener {
                if (selectionMode) {
                    selectItem(position, !itemWasSelected)
                } else {
                    clickListener(getItem(position))
                }
            }
        }

        override fun submitList(list: MutableList<Configuration>?) {
            selectedPositions.clear()
            super.submitList(list)
        }

        override fun submitList(list: MutableList<Configuration>?, commitCallback: Runnable?) {
            selectedPositions.clear()
            super.submitList(list, commitCallback)
        }

        fun getSelectedItems() : List<Configuration> {
            val currentItems = currentList
            return selectedPositions.map(currentItems::get)
        }

        fun clearSelection() {
            selectedPositions.clear()
            selectionMode = false
            notifyDataSetChanged()
        }

        private fun selectItem(position: Int, select: Boolean) {
            if (select) {
                selectedPositions.add(position)
                if (selectionMode) {
                    notifyItemChanged(position)
                } else {
                    selectionMode = true
                    notifyDataSetChanged()
                }
            } else {
                selectedPositions.remove(position)
                if (selectedPositions.size == 0) {
                    selectionMode = false
                    notifyDataSetChanged()
                } else {
                    notifyItemChanged(position)
                }
            }
            selectionListener(selectionMode, selectedPositions.size)
        }
    }

    private class ConfigurationViewHolder(private val binding: VhConfigurationBinding)
        : RecyclerView.ViewHolder(binding.root) {
            fun setData(data: Configuration, selectionActive: Boolean, selected: Boolean) {
                val context = binding.root.context
                val dateTime = ZonedDateTime.ofInstant(data.timestamp, zone)
                val timestamp = formatter.format(dateTime)
                binding.name.text = data.name
                binding.time.text = context.getString(R.string.detail_timestamp, timestamp)
                if (selectionActive) {
                    binding.checkbox.isChecked = selected
                    binding.checkbox.visibility = View.VISIBLE
                } else {
                    binding.checkbox.isChecked = false
                    binding.checkbox.visibility = View.GONE
                }
            }
    }

    companion object {
        private val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        private val zone = ZoneId.systemDefault()
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Configuration>() {
            override fun areItemsTheSame(oldItem: Configuration, newItem: Configuration): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: Configuration,
                newItem: Configuration
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}