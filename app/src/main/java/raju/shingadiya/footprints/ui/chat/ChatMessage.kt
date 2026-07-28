package raju.shingadiya.footprints.ui.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val isGenerating: Boolean = false,
)
