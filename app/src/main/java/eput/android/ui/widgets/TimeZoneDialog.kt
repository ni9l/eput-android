package eput.android.ui.widgets

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import eput.android.R
import eput.android.databinding.DialogTimeZoneBinding
import eput.android.databinding.VhZoneIdBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

class TimeZoneDialog : DialogFragment() {
    private lateinit var binding: DialogTimeZoneBinding
    private lateinit var adapter: ZoneIdDataAdapter
    private var zones: List<ZoneIdData> = listOf()
    var onItemSelectedListener: ((ZoneId) -> Unit)? = null
    var instant: Instant? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(
            requireContext(),
            R.style.ThemeOverlay_TimeZoneDialog
        )
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val window = requireDialog().window
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogTimeZoneBinding.inflate(inflater)
        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }
        adapter = ZoneIdDataAdapter()
        adapter.setOnClickListener {
            onItemSelectedListener?.invoke(it)
            dismiss()
        }
        binding.recycler.adapter = adapter
        val divider = MaterialDividerItemDecoration(
            requireContext(),
            LinearLayoutManager.VERTICAL
        )
        binding.recycler.addItemDecoration(divider)
        lifecycleScope.launchWhenStarted {
            getTimeZones()
        }
        binding.toolbar.menu.findItem(R.id.action_search)?.let {
            val searchView = it.actionView as SearchView
            val queryTextListener = object : SearchView.OnQueryTextListener {
                var filter: Filter? = null

                override fun onQueryTextSubmit(query: String?): Boolean {
                    val f = filter
                    return if (f != null) {
                        f.filter(query)
                        true
                    } else {
                        false
                    }
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrBlank() || newText.length < 3) {
                        return true
                    }
                    val f = filter
                    return if (f != null) {
                        f.filter(newText)
                        true
                    } else {
                        false
                    }
                }
            }

            it.setOnActionExpandListener(
                object : MenuItem.OnActionExpandListener {
                    override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                        queryTextListener.filter = adapter.filter
                        return true
                    }

                    override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                        queryTextListener.filter = null
                        adapter.submitList(zones)
                        return true
                    }
                }
            )
            searchView.setOnQueryTextListener(queryTextListener)
        }
        return binding.root
    }

    private suspend fun getTimeZones() {
        withContext(Dispatchers.Default) {
            val availableZones = ZoneId.getAvailableZoneIds().map {
                val zoneId = ZoneId.of(it)
                ZoneIdData(zoneId, instant)
            }
            withContext(Dispatchers.Main) {
                zones = availableZones
                adapter.submitList(availableZones)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ZoneIdData>() {
            override fun areItemsTheSame(oldItem: ZoneIdData, newItem: ZoneIdData): Boolean {
                return oldItem.zoneId.id == newItem.zoneId.id
            }

            override fun areContentsTheSame(oldItem: ZoneIdData, newItem: ZoneIdData): Boolean {
                return oldItem.displayName == newItem.displayName
                        && oldItem.offset == newItem.offset
            }
        }
    }

    private class ZoneIdDataAdapter
        : ListAdapter<ZoneIdData, ZoneIdDataViewHolder>(DIFF_CALLBACK), Filterable {
        private var onClickListener: ((ZoneId) -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZoneIdDataViewHolder {
            val binding = VhZoneIdBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
            return ZoneIdDataViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ZoneIdDataViewHolder, position: Int) {
            val zoneId = getItem(position)
            holder.setData(zoneId)
            holder.itemView.setOnClickListener {
                onClickListener?.invoke(zoneId.zoneId)
            }
        }

        fun setOnClickListener(listener: ((ZoneId) -> Unit)?) {
            onClickListener = listener
        }

        override fun getFilter(): Filter {
            return ZoneIdDataFilter(this, currentList)
        }
    }

    private class ZoneIdDataFilter(
        private val adapter: ZoneIdDataAdapter,
        private val zones: List<ZoneIdData>
    ): Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filtered = if (constraint != null) {
                zones.filter {
                    it.displayName.contains(constraint, true)
                }
            } else {
                listOf()
            }
            val result = ZoneIdDataFilterResult()
            result.valuesAsList = filtered
            result.values = filtered
            result.count = filtered.size
            return result
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val list: List<ZoneIdData> = if (results is ZoneIdDataFilterResult) {
                results.valuesAsList
            } else {
                listOf()
            }
            adapter.submitList(list)
        }

        private class ZoneIdDataFilterResult : FilterResults() {
            var valuesAsList: List<ZoneIdData> = listOf()
        }
    }

    private class ZoneIdDataViewHolder(private val binding: VhZoneIdBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(data: ZoneIdData) {
            binding.title.text = data.displayName
            if (data.offset.isBlank()) {
                binding.detail.visibility = View.GONE
            } else {
                binding.detail.visibility = View.VISIBLE
                binding.detail.text = data.offset
            }
        }
    }

    private class ZoneIdData(val zoneId: ZoneId, instant: Instant?) {
        val displayName: String =
            zoneId.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
        val offset: String = if (instant != null) {
            zoneId.rules.getOffset(instant).toString()
        } else {
            ""
        }
    }
}