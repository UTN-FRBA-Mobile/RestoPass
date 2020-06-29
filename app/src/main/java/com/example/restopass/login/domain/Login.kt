package com.example.restopass.login.domain

data class Login(
    val email: String,
    val password: String
)

data class LoginResponse(
    val user: User,
    val xAuthToken: String,
    val xRefreshToken: String
)

data class User(
    val email: String,
    val name: String,
    val lastName: String,
    val actualMembership: Int?,
    var visits : Int,
    var favoriteRestaurants: MutableList<String>?
) {
   val firebaseTopic = email.replace("@", "")
}