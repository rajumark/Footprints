package com.example.footprints.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ButtonDefaults
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footprints.R
import com.example.footprints.theme.Border
import com.example.footprints.theme.DarkBorder
import com.example.footprints.theme.DarkOnSurface
import com.example.footprints.theme.DarkPageBg
import com.example.footprints.theme.DarkPrimary
import com.example.footprints.theme.DarkSurface
import com.example.footprints.theme.OnPrimary
import com.example.footprints.theme.OnSurface
import com.example.footprints.theme.PageBg
import com.example.footprints.theme.Primary
import com.example.footprints.theme.Surface
import com.example.footprints.theme.AppDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val statusText by viewModel.statusText.collectAsStateWithLifecycle()
    val showSettings by viewModel.showSettings.collectAsStateWithLifecycle()
    val showConversations by viewModel.showConversations.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val tokenCount by viewModel.tokenCount.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val voiceText by viewModel.voiceText.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val isDark = AppDarkTheme

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startListening()
    }

    val bg = if (isDark) DarkPageBg else PageBg
    val surface = if (isDark) DarkSurface else Surface
    val onSurface = if (isDark) DarkOnSurface else OnSurface
    val primary = if (isDark) DarkPrimary else Primary
    val border = if (isDark) DarkBorder else Border

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        containerColor = surface,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_elephant),
                            contentDescription = "Logo",
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Footprints",
                                fontSize = 20.sp,
                                fontWeight = FontWeight(500),
                                color = onSurface,
                            )
                            Text(
                                text = statusText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight(500),
                                color = onSurface.copy(alpha = 0.5f),
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleDarkTheme() },
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = if (isDark) R.drawable.ic_sunny else R.drawable.ic_moon),
                                contentDescription = "Toggle theme",
                                tint = onSurface,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                        IconButton(
                            onClick = { viewModel.toggleConversations() },
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_menu),
                                contentDescription = "Conversations",
                                tint = onSurface,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedButton(
                            onClick = { viewModel.newChat() },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = primary,
                                containerColor = Color.Transparent,
                            ),
                            border = BorderStroke(1.dp, border),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp),
                        ) {
                            Text(
                                text = "+ NEW",
                                fontSize = 11.sp,
                                fontWeight = FontWeight(500),
                                letterSpacing = 0.3.sp,
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(1.dp)
                        .background(border.copy(alpha = 0.2f))
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(surface)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = primary,
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = statusText,
                            fontSize = 13.sp,
                            color = onSurface.copy(alpha = 0.5f),
                        )
                    }
                }
            } else if (messages.isEmpty() || (messages.size == 1 && !messages[0].isUser && messages[0].content.contains("Hello! I am Footprints"))) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Footprints",
                            fontSize = 36.sp,
                            fontWeight = FontWeight(500),
                            color = onSurface.copy(alpha = 0.08f),
                            letterSpacing = 2.sp,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Private AI \u00B7 100% Offline",
                            fontSize = 12.sp,
                            color = onSurface.copy(alpha = 0.25f),
                            letterSpacing = 0.5.sp,
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                ) {
                    items(messages) { message ->
                        ChatBubble(
                            message = message,
                            isDark = isDark,
                            onExport = { viewModel.exportChat() },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .background(surface)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isListening) {
                    Text(
                        text = if (voiceText.isNotBlank()) voiceText else "Listening...",
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        fontSize = 13.sp,
                        color = onSurface.copy(alpha = 0.6f),
                        fontStyle = FontStyle.Italic,
                    )
                    IconButton(
                        onClick = { viewModel.stopListening() },
                        modifier = Modifier.size(36.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = primary,
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = voiceText.ifBlank { inputText },
                        onValueChange = {
                            inputText = it
                            if (voiceText.isNotBlank()) viewModel.stopListening()
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Type a message...",
                                fontSize = 13.sp,
                                color = onSurface.copy(alpha = 0.35f),
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            color = onSurface,
                        ),
                        enabled = !isGenerating,
                        singleLine = false,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = border,
                            unfocusedBorderColor = border.copy(alpha = 0.3f),
                            cursorColor = primary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    IconButton(
                        onClick = {
                            val hasPerm = if (Build.VERSION.SDK_INT >= 33)
                                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                            else true
                            if (hasPerm) {
                                viewModel.startListening()
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(primary.copy(alpha = 0.1f)),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mic),
                            contentDescription = "Voice input",
                            tint = primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        },
                        enabled = (inputText.isNotBlank() || voiceText.isNotBlank()) && !isGenerating,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(primary),
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_send),
                            contentDescription = "Send",
                            tint = if (isDark) DarkOnSurface.copy(alpha = 0.9f) else OnPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }

    if (showSettings) {
        SettingsSheet(
            settings = settings,
            isDark = isDark,
            onDismiss = { viewModel.toggleSettings() },
            onSelectPreset = { viewModel.selectPreset(it) },
            onUpdateSettings = { viewModel.updateSettings(it) },
        )
    }

    if (showConversations) {
        ConversationsSheet(
            conversations = conversations,
            isDark = isDark,
            onDismiss = { viewModel.toggleConversations() },
            onSelect = { viewModel.switchConversation(it) },
            onDelete = { viewModel.deleteConversation(it) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    settings: AppSettings,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSelectPreset: (Int) -> Unit,
    onUpdateSettings: (AppSettings) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val bg = if (isDark) DarkSurface else Surface
    val onSurface = if (isDark) DarkOnSurface else OnSurface
    val primary = if (isDark) DarkPrimary else Primary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bg,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("Personality", fontSize = 16.sp, fontWeight = FontWeight(500), color = onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            PRESETS.forEachIndexed { index, (name, _) ->
                val selected = settings.selectedPreset == index
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable { onSelectPreset(index) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) primary.copy(alpha = 0.12f) else bg
                    ),
                    border = if (selected) BorderStroke(1.dp, primary.copy(alpha = 0.4f)) else null,
                ) {
                    Text(
                        text = name,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight(500) else FontWeight(400),
                        color = onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Max Tokens: ${settings.maxTokens}", fontSize = 14.sp, fontWeight = FontWeight(500), color = onSurface)
            Slider(
                value = settings.maxTokens.toFloat(),
                onValueChange = { onUpdateSettings(settings.copy(maxTokens = it.toInt())) },
                valueRange = 128f..2048f,
                steps = 14,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationsSheet(
    conversations: List<Conversation>,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val bg = if (isDark) DarkSurface else Surface
    val onSurface = if (isDark) DarkOnSurface else OnSurface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bg,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            Text("Conversations", fontSize = 16.sp, fontWeight = FontWeight(500), color = onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            if (conversations.isEmpty()) {
                Text(
                    "No saved conversations",
                    fontSize = 13.sp,
                    color = onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 20.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(conversations) { conv ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(conv.id) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(conv.name, fontSize = 13.sp, color = onSurface)
                                Text(
                                    "${conv.messages.size} messages",
                                    fontSize = 10.sp,
                                    color = onSurface.copy(alpha = 0.4f),
                                )
                            }
                            IconButton(
                                onClick = { onDelete(conv.id) },
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_close),
                                    contentDescription = "Delete",
                                    tint = onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    isDark: Boolean,
    onExport: () -> Unit,
) {
    val isUser = message.isUser
    val clipboardManager = LocalClipboardManager.current
    val displayText = message.content.stripMarkdown()
    val onSurface = if (isDark) DarkOnSurface else OnSurface
    val surface = if (isDark) DarkSurface else Surface
    val primary = if (isDark) DarkPrimary else Primary
    val onPrimary = if (isDark) DarkOnSurface else com.example.footprints.theme.OnPrimary
    val border = if (isDark) DarkBorder else Border

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            if (isUser) {
                Card(
                    shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                    colors = CardDefaults.cardColors(containerColor = primary),
                ) {
                    SelectionContainer {
                        Text(
                            text = displayText,
                            modifier = Modifier.padding(14.dp),
                            color = onPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                        )
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                    colors = CardDefaults.cardColors(containerColor = surface),
                    border = BorderStroke(1.5.dp, border.copy(alpha = 0.2f)),
                ) {
                    if (message.isGenerating) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp).clip(CircleShape),
                                strokeWidth = 1.5.dp,
                                color = primary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Thinking...",
                                color = onSurface.copy(alpha = 0.5f),
                                fontSize = 13.sp,
                            )
                        }
                    } else {
                        SelectionContainer {
                            Text(
                                text = displayText,
                                modifier = Modifier.padding(14.dp),
                                color = onSurface,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                            )
                        }
                    }
                }
            }
        }
        if (!message.isGenerating) {
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedButton(
                    onClick = { clipboardManager.setText(AnnotatedString(message.content)) },
                    border = BorderStroke(0.5.dp, border.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                    modifier = Modifier.height(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = onSurface.copy(alpha = 0.35f)),
                ) {
                    Text(text = "Copy", fontSize = 8.sp, letterSpacing = 0.3.sp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedButton(
                    onClick = onExport,
                    border = BorderStroke(0.5.dp, border.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                    modifier = Modifier.height(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = onSurface.copy(alpha = 0.35f)),
                ) {
                    Text(text = "Export", fontSize = 8.sp, letterSpacing = 0.3.sp)
                }
            }
        }
    }
}

private fun String.stripMarkdown(): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val text = this@stripMarkdown
        while (i < text.length) {
            when {
                text.startsWith("```", i) -> {
                    val end = text.indexOf("```", i + 3)
                    val codeEnd = if (end == -1) text.length else end + 3
                    val code = text.substring(
                        text.indexOf('\n', i) + 1.let { if (it > i + 3) it else i + 3 },
                        if (end == -1) text.length else end
                    )
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0x15000000))) {
                        append(code.trim())
                    }
                    i = codeEnd
                }
                text.startsWith("`", i) && !text.startsWith("```", i) -> {
                    val end = text.indexOf('`', i + 1)
                    val code = text.substring(i + 1, if (end == -1) text.length else end)
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0x15000000))) {
                        append(code)
                    }
                    i = if (end == -1) text.length else end + 1
                }
                text.startsWith("**", i) || text.startsWith("__", i) -> {
                    val end = text.indexOf(
                        if (text.startsWith("**", i)) "**" else "__",
                        i + 2
                    )
                    val inner = text.substring(i + 2, if (end == -1) text.length else end)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(inner) }
                    i = if (end == -1) text.length else end + 2
                }
                text.startsWith("*", i) && !text.startsWith("**", i) ||
                    text.startsWith("_", i) && !text.startsWith("__", i) -> {
                    val end = text.indexOf(
                        if (text.startsWith("*", i)) "*" else "_",
                        i + 1
                    )
                    val inner = text.substring(i + 1, if (end == -1) text.length else end)
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(inner) }
                    i = if (end == -1) text.length else end + 1
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}
