package com.weighttracker.android.domain.model

enum class UserProfile(val id: String, val displayName: String) {
    ME("me", "Me"),
    PARTNER("partner", "Partner");

    companion object {
        fun fromId(id: String): UserProfile {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ME
        }
    }
}
