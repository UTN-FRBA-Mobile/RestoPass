package com.example.restopass.main.ui.reservations

import androidx.lifecycle.ViewModel

class ReservationCreateViewModel(
    var name: String = "",
    var lastName: String = "",
    var email: String = "",
    var password: String = "")
    : ViewModel()