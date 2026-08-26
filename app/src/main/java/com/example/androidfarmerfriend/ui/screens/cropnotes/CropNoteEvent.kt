package com.example.androidfarmerfriend.ui.screens.cropnotes

sealed interface CropNoteEvent {
    data class SelectCrop(val crop: String) : CropNoteEvent
    data class Search(val query: String) : CropNoteEvent
    data object Retry : CropNoteEvent
}
