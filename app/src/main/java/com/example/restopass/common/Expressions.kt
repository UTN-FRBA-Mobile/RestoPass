package com.example.restopass.common

import com.example.restopass.connection.ApiError
import com.example.restopass.connection.ApiException
import com.example.restopass.connection.ObjectMapperProvider.mapper
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import java.math.BigInteger
import java.security.MessageDigest


internal inline fun Any?.orElse(f: ()-> Unit): Any?{
    if (this == null){
        f()
    }
    return this
}

internal inline fun < reified T: Any > Response<T>.error() : ApiException {
    Timber.e("Error occurred. Deserializing body")

    val apiError = mapper.readValue(this.errorBody()!!.string(), ApiError::class.java)

    Timber.e("Error connecting with service: ${apiError.error}")

    return ApiException(apiError)
}

fun Any.toJson(): String {
    return mapper.writeValueAsString(this)
}

internal inline fun <reified T> String.fromJson(): T{
    return mapper.readValue(this,T::class.java)
}

internal inline fun <reified T> ResponseBody.fromJson(): T{
    return mapper.readValue(this.toJson(),T::class.java)
}


fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}