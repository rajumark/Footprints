package com.example.footprints.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.footprints.llm.LlmEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _statusText = MutableStateFlow("Loading model...")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val systemPrompt = "You are a helpful, harmless, and honest AI assistant called Footprints. Keep responses concise and helpful."

    init {
        initializeModel()
    }

    private fun initializeModel() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) { _statusText.value = "Initializing engine..." }
                LlmEngine.initialize(getApplication())

                withContext(Dispatchers.Main) { _statusText.value = "Loading model (first run takes time)..." }
                LlmEngine.loadModel(getApplication())

                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _statusText.value = "Ready"
                    _messages.value = listOf(
                        ChatMessage(
                            content = "Hello! I am Footprints, powered by Qwen 2.5 0.5B running 100% offline on your device. How can I help you?",
                            isUser = false
                        )
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _statusText.value = "Error: " + e.message
                    _messages.value = listOf(
                        ChatMessage(
                            content = "Failed to load model: " + e.message,
                            isUser = false
                        )
                    )
                }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isGenerating.value) return

        val userMessage = ChatMessage(content = text, isUser = true)
        _messages.value = _messages.value + userMessage

        val placeholderMessage = ChatMessage(content = "", isUser = false, isGenerating = true)
        _messages.value = _messages.value + placeholderMessage
        _isGenerating.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tokenFlow = LlmEngine.sendMessage(text, maxTokens = 1024)
                val responseBuilder = StringBuilder()

                tokenFlow.collectLatest { token ->
                    responseBuilder.append(token)
                    withContext(Dispatchers.Main) {
                        val currentMessages = _messages.value.toMutableList()
                        if (currentMessages.isNotEmpty()) {
                            val lastIndex = currentMessages.lastIndex
                            currentMessages[lastIndex] = ChatMessage(
                                content = responseBuilder.toString(),
                                isUser = false
                            )
                            _messages.value = currentMessages
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    _isGenerating.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val currentMessages = _messages.value.toMutableList()
                    if (currentMessages.isNotEmpty()) {
                        val lastIndex = currentMessages.lastIndex
                        val currentContent = currentMessages[lastIndex].content
                        currentMessages[lastIndex] = ChatMessage(
                            content = currentContent.ifBlank { "Error: " + e.message },
                            isUser = false
                        )
                        _messages.value = currentMessages
                    }
                    _isGenerating.value = false
                }
            }
        }
    }
}
