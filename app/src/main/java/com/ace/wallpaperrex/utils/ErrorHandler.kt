package com.ace.wallpaperrex.utils

import android.util.Log
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import java.io.IOException

fun mapToUserFriendlyException(throwable: Throwable): Exception {
    Log.e("ErrorHandler", "Caught exception: ${throwable::class.java.simpleName}", throwable)
    return when (throwable) {
        is ClientRequestException -> {
            when (throwable.response.status) {
                HttpStatusCode.Unauthorized -> Exception(
                    "Invalid API Key. Please check your credentials.",
                    throwable
                )

                HttpStatusCode.BadRequest -> Exception(
                    "Invalid request. Please check the search query or parameters.",
                    throwable
                )

                HttpStatusCode.TooManyRequests -> Exception(
                    "You have exceeded the API rate limit.",
                    throwable
                )

                else -> Exception(
                    "Client error: ${throwable.response.status.description}. Please try again.",
                    throwable
                )
            }
        }

        is ServerResponseException -> Exception(
            "Server error (${throwable.response.status.value}). Please try again later.",
            throwable
        )

        is IOException -> Exception(
            "Network error. Please check your internet connection.",
            throwable
        )

        else -> Exception(
            "An unexpected error occurred: ${throwable.message}",
            throwable
        )
    }
}