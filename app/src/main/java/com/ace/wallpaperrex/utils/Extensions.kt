package com.ace.wallpaperrex.utils

val String.getExtensionFromString: String
    get() {
        val lastSegment = this.substringAfterLast("/").substringBefore("?")
        return lastSegment.substringAfterLast(".", "").let { if (it.isNotEmpty()) ".$it" else "" }
    }