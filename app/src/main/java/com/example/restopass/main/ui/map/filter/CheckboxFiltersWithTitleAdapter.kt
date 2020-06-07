package com.example.restopass.main.ui.map.filter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restopass.R
import com.example.restopass.main.ui.map.MapViewModel
import kotlinx.android.synthetic.main.view_filter_checkbox.*
import kotlinx.android.synthetic.main.view_filter_checkbox.view.*
import kotlinx.android.synthetic.main.view_filter_checkbox_item.view.*

class CheckboxFilterWithTitleAdapter(
    private val modelViewModel: MapViewModel,
    private val filterFragment: FilterFragment
) :
    RecyclerView.Adapter<CheckboxFilterWithTitleAdapter.FilterViewHolder>() {

    private lateinit var checkboxFilterAdapter: CheckboxFilterAdapter
    private lateinit var checkboxReciclerView: RecyclerView

    private val tags = modelViewModel.filters.tags.toList()

    override fun getItemCount() = tags.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder =
        FilterViewHolder.from(
            parent
        )

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.itemView.apply {
            val tag = tags[position]
            checkboxFilterTitle.text = tag.first
            checkboxFilterAdapter =
                CheckboxFilterAdapter(modelViewModel, filterFragment, tag.second)
            checkboxReciclerView =  checkboxRecycler.apply {
                layoutManager = LinearLayoutManager(this.context)
                adapter = checkboxFilterAdapter
            }
        }
    }

    class FilterViewHolder(view: View)
        : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): FilterViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.view_filter_checkbox, parent, false)
                return FilterViewHolder(
                    view
                )
            }
        }
    }
}
