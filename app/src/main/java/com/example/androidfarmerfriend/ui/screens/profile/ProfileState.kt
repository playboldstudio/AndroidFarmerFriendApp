package com.example.androidfarmerfriend.ui.screens.profile

import com.example.androidfarmerfriend.data.localization.Language

data class ProfileState(
    val userName: String = "",
    val userPhone: String = "",
    val userEmail: String = "",
    val displayName: String = "",
    val locationName: String = "",
    val languageLabel: String = "",
    val isEditing: Boolean = false,
    val tempName: String = "",
    val tempPhone: String = "",
    val tempEmail: String = "",
    val nameError: String? = null,
    val phoneError: String? = null,
    val selectedLanguage: Language = Language.ENGLISH,
    val showLogoutDialog: Boolean = false,
    val message: String? = null,
    val dynamicColorEnabled: Boolean = false
)
