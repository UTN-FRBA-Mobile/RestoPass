package com.example.restopass.main.ui.map.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restopass.R
import com.example.restopass.main.ui.map.MapViewModel
import com.example.restopass.main.ui.map.SelectedFilters
import kotlinx.android.synthetic.main.fragment_filter.*
import kotlinx.android.synthetic.main.view_filter_checkbox.*


class FilterFragment : Fragment() {
    private lateinit var mapViewModel: MapViewModel
    private lateinit var checkboxWithTitleAdapter: CheckboxFilterWithTitleAdapter
    private lateinit var checkboxWithTitleReciclerView: RecyclerView

    private lateinit var radioFilterAdapter: PlanRadioFiltersAdapter
    private lateinit var radioReciclerView: RecyclerView

    lateinit var selectedFilters: SelectedFilters

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mapViewModel =
            ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        // Inflate the layout for this fragment
        selectedFilters = mapViewModel.selectedFilters.copy(tags = (mapViewModel.selectedFilters.tags).toMutableList())
        return inflater.inflate(R.layout.fragment_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        closeFiltersButton.setOnClickListener { this.activity?.onBackPressed() }

        radioFilterAdapter =
            PlanRadioFiltersAdapter(mapViewModel, this)
        radioReciclerView =  switchRecycler.apply {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = radioFilterAdapter
        }

        checkboxWithTitleAdapter =
            CheckboxFilterWithTitleAdapter(mapViewModel, this)
        checkboxWithTitleReciclerView =  checkboxWithTitleRecycler.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = checkboxWithTitleAdapter
        }

        applyFiltersButton.setOnClickListener {
            mapViewModel.selectedFilters = selectedFilters.copy()
            this.activity?.onBackPressed()
        }

        deleteFiltersButton.setOnClickListener {
            mapViewModel.selectedFilters = SelectedFilters()
            this.activity?.onBackPressed()
        }
    }
}
