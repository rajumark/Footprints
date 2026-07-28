package com.example.footprints.ui.chat

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.FileProvider
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class Conversation(
    val id: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()),
    val name: String = "",
    val messages: List<ChatMessage> = emptyList(),
)

data class AppSettings(
    val darkTheme: Boolean = false,
    val systemPrompt: String = "You are a helpful, harmless, and honest AI assistant called Footprints. Keep responses concise and helpful.",
    val selectedPreset: Int = 0,
    val maxTokens: Int = 1024,
)

val PRESETS = listOf(
    "Default" to "You are a helpful, harmless, and honest AI assistant called Footprints. Keep responses concise and helpful.",
    "Creative" to "You are a creative and imaginative AI called Footprints. Be poetic, use vivid language, and think outside the box.",
    "Professional" to "You are a professional AI assistant called Footprints. Be precise, well-structured, and formal in your responses.",
    "Concise" to "You are a direct AI assistant called Footprints. Answer as briefly as possible, ideally 1-3 sentences. No fluff.",
    "Custom" to "",
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _statusText = MutableStateFlow("Loading model...")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _showConversations = MutableStateFlow(false)
    val showConversations: StateFlow<Boolean> = _showConversations.asStateFlow()

    private val _tokenCount = MutableStateFlow(0)
    val tokenCount: StateFlow<Int> = _tokenCount.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _voiceText = MutableStateFlow("")
    val voiceText: StateFlow<String> = _voiceText.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var currentConversationId: String? = null
    private var startTime = 0L

    init {
        loadConversations()
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
                    _statusText.value = "Ready · Qwen 2.5 0.5B"
                    addWelcomeMessage()
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

    private fun addWelcomeMessage() {
        _messages.value = listOf(
            ChatMessage(
                content = "Hello! I am Footprints, powered by Qwen 2.5 0.5B running 100% offline on your device. How can I help you?",
                isUser = false
            )
        )
    }

    // ---- Settings ----

    fun toggleSettings() {
        _showSettings.value = !_showSettings.value
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        LlmEngine.systemPrompt = newSettings.systemPrompt
        viewModelScope.launch {
            LlmEngine.updateSystemPrompt()
        }
    }

    fun selectPreset(index: Int) {
        val preset = PRESETS[index].second
        updateSettings(_settings.value.copy(selectedPreset = index, systemPrompt = preset))
    }

    fun toggleDarkTheme() {
        com.example.footprints.theme.AppDarkTheme = !com.example.footprints.theme.AppDarkTheme
        _settings.value = _settings.value.copy(darkTheme = com.example.footprints.theme.AppDarkTheme)
    }

    // ---- Conversations ----

    fun toggleConversations() {
        _showConversations.value = !_showConversations.value
    }

    private fun loadConversations() {
        val dir = getApplication<Application>().filesDir.resolve("conversations")
        if (!dir.exists()) return
        val list = dir.listFiles()?.sortedByDescending { it.lastModified() }?.mapNotNull { file ->
            try {
                val text = file.readText()
                val conv = json.decodeFromString<Conversation>(text)
                conv
            } catch (e: Exception) { null }
        } ?: emptyList()
        _conversations.value = list
    }

    fun switchConversation(id: String) {
        val conv = _conversations.value.find { it.id == id } ?: return
        _messages.value = conv.messages
        currentConversationId = id
        _showConversations.value = false
    }

    private fun saveCurrentConversation() {
        val messages = _messages.value
        if (messages.isEmpty() || (messages.size == 1 && !messages[0].isUser)) return
        val id = currentConversationId ?: SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        if (currentConversationId == null) currentConversationId = id
        val title = messages.firstOrNull { it.isUser }?.content?.take(50)?.replace("\n", " ")?.trim()
            ?: "Chat"
        val conv = Conversation(id = id, name = title, messages = messages)
        val dir = getApplication<Application>().filesDir.resolve("conversations")
        dir.mkdirs()
        val file = dir.resolve("$id.json")
        viewModelScope.launch(Dispatchers.IO) {
            file.writeText(json.encodeToString(conv))
            loadConversations()
        }
    }

    fun deleteConversation(id: String) {
        val dir = getApplication<Application>().filesDir.resolve("conversations")
        val file = dir.resolve("$id.json")
        file.delete()
        if (id == currentConversationId) {
            currentConversationId = null
        }
        loadConversations()
    }

    // ---- Chat ----

    fun newChat() {
        saveCurrentConversation()
        currentConversationId = null
        _tokenCount.value = 0
        addWelcomeMessage()
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isGenerating.value) return

        val userMessage = ChatMessage(content = text, isUser = true)
        _messages.value = _messages.value + userMessage

        val placeholderMessage = ChatMessage(content = "", isUser = false, isGenerating = true)
        _messages.value = _messages.value + placeholderMessage
        _isGenerating.value = true
        startTime = System.currentTimeMillis()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tokenFlow = LlmEngine.sendMessage(text, maxTokens = _settings.value.maxTokens)
                val responseBuilder = StringBuilder()
                var tokenNum = 0

                tokenFlow.collectLatest { token ->
                    responseBuilder.append(token)
                    tokenNum++
                    withContext(Dispatchers.Main) {
                        _tokenCount.value = tokenNum
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
                    saveCurrentConversation()
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

    // ---- Export ----

    fun exportChat() {
        val messages = _messages.value
        if (messages.isEmpty()) return
        val text = messages.joinToString("\n\n") { msg ->
            val prefix = if (msg.isUser) "You" else "Footprints"
            "$prefix:\n${msg.content}"
        }
        val context = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fileName = "Footprints_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.txt"
                if (Build.VERSION.SDK_INT >= 30) {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    uri?.let {
                        context.contentResolver.openOutputStream(it)?.use { os ->
                            os.write(text.toByteArray())
                        }
                    }
                } else {
                    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    dir.mkdirs()
                    File(dir, fileName).writeText(text)
                }
                withContext(Dispatchers.Main) {
                    _statusText.value = "Exported to Downloads/$fileName"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _statusText.value = "Export failed: " + e.message
                }
            }
        }
    }

    // ---- Voice Input ----

    fun startListening() {
        val context = getApplication<Application>()
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: android.os.Bundle?) {
                    _isListening.value = true
                    _voiceText.value = ""
                }
                override fun onResults(results: android.os.Bundle?) {
                    val texts = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = texts?.firstOrNull() ?: ""
                    _voiceText.value = spoken
                    _isListening.value = false
                    if (spoken.isNotBlank()) {
                        sendMessage(spoken)
                    }
                }
                override fun onPartialResults(partialResults: android.os.Bundle?) {
                    val texts = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partial = texts?.firstOrNull() ?: ""
                    _voiceText.value = partial
                }
                override fun onError(error: Int) { _isListening.value = false }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { _isListening.value = false }
                override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
            })
            speechRecognizer?.startListening(intent)
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        _isListening.value = false
    }

    override fun onCleared() {
        super.onCleared()
        saveCurrentConversation()
        speechRecognizer?.destroy()
    }
}
