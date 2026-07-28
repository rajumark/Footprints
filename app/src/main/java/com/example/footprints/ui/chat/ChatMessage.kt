package com.example.footprints.ui.chat

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val isGenerating: Boolean = false
)
