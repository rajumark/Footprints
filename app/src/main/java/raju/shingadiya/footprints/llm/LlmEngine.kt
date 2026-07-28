package raju.shingadiya.footprints.llm

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import net.amazingapps.llama.android.core.AiChat
import net.amazingapps.llama.android.core.InferenceEngine
import java.io.File
import java.io.FileOutputStream

object LlmEngine {
    private var engine: InferenceEngine? = null
    private const val MODEL_NAME = "qwen2.5-0.5b-instruct-q4_k_m.gguf"
    private var _systemPrompt = "You are a helpful, harmless, and honest AI assistant called Footprints. Keep responses concise and helpful."

    val state: StateFlow<InferenceEngine.State>?
        get() = engine?.state

    var systemPrompt: String
        get() = _systemPrompt
        set(value) {
            _systemPrompt = value
        }

    fun initialize(appContext: Context) {
        engine = AiChat.getInferenceEngine(appContext)
    }

    suspend fun loadModel(appContext: Context) {
        val e = engine ?: throw IllegalStateException("LlmEngine not initialized")
        val modelFile = copyModelToInternal(appContext)
        e.loadModel(modelFile.absolutePath)
        withContext(Dispatchers.IO) {
            e.setSystemPrompt(_systemPrompt)
        }
    }

    suspend fun updateSystemPrompt() {
        val e = engine ?: return
        withContext(Dispatchers.IO) {
            e.setSystemPrompt(_systemPrompt)
        }
    }

    fun sendMessage(message: String, maxTokens: Int = 1024): Flow<String> {
        val e = engine ?: throw IllegalStateException("LlmEngine not initialized")
        return e.sendUserPrompt(message, maxTokens)
    }

    fun isInitialized(): Boolean = engine != null

    fun close() {
        engine?.cleanUp()
        engine?.destroy()
        engine = null
    }

    private fun copyModelToInternal(context: Context): File {
        val internalFile = File(context.filesDir, MODEL_NAME)
        if (internalFile.exists() && internalFile.length() > 0) return internalFile
        context.assets.open(MODEL_NAME).use { input ->
            FileOutputStream(internalFile).use { output ->
                input.copyTo(output, bufferSize = 8192)
            }
        }
        return internalFile
    }
}
