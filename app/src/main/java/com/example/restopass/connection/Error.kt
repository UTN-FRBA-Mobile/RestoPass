package com.example.restopass.connection

data class ApiError(
    val status: Number,
    val httpStatusCode: Number,
    val message: String,
    val error: String
)