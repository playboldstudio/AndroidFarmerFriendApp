package com.example.androidfarmerfriend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.androidfarmerfriend.ui.screens.MainScreen
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidFarmerFriendTheme {
                MainScreen()
            }
        }
    }
}
