package com.example.androidfarmerfriend.ui.screens.profile

import com.example.androidfarmerfriend.data.localization.Language

data class ProfileState(
    val userName: String = "",
    val userPhone: String = "",
    val isEditing: Boolean = false,
    val tempName: String = "",
    val tempPhone: String = "",
    val phoneError: String? = null,
    val selectedLanguage: Language = Language.ENGLISH,
    val message: String? = null
)
