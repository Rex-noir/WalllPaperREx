package com.ace.wallpaperrex.data.repositories

class GeneralSettingRepository {

    companion object {
        enum class AutoChangeWallpaperSource(val key: String) {
            FAVORITES("favorites"),
            CUSTOM_SOURCES("custom_sources");

            companion object {
                fun fromKey(key: String): AutoChangeWallpaperSource? =
                    entries.find { it.key == key }
            }
        }

    }
}