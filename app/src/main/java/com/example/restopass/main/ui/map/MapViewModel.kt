package com.example.restopass.main.ui.map

import androidx.lifecycle.ViewModel
import com.example.restopass.domain.Membership

class MapViewModel : ViewModel() {
    lateinit var filters: Filters
    var selectedFilters: SelectedFilters = SelectedFilters()
}

data class SelectedFilters(var tags: MutableList<String> = mutableListOf(), var plan: Int? = null, var search: String? = null)
data class Filters(var tags: Map<String, List<String>>, var plans: List<Membership>)