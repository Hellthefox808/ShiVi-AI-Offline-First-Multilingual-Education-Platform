package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ChatMessage
import com.example.domain.model.ChatPersonaRole
import com.example.domain.model.GeminiModelChoice
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiChatbotScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val selectedPersona by viewModel.selectedPersona.collectAsState()
    val selectedModelChoice by viewModel.selectedModelChoice.collectAsState()
    val isSearchGrounding by viewModel.isSearchGroundingInChat.collectAsState()
    val isSending by viewModel.isSendingChatMessage.collectAsState()
    val inputText by viewModel.chatInputText.collectAsState()

    val listState = rememberLazyListState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val glass = LocalGlassColors.current

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size, isSending) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickPrompts = remember(selectedPersona) {
        when (selectedPersona) {
            ChatPersonaRole.MTB_MLE_PEDAGOGY -> listOf(
                "संथाली में सरहुल त्योहार (Baha Porob) पर कक्षा 2 के लिए 5 वाक्य बताएं",
                "हिंदी भाषी शिक्षक बच्चों से संथाली में पहला संवाद कैसे शुरू करे?",
                "हो भाषा में प्रकृति व पेड़ों से जुड़ी बाल कविता व अर्थ बताएं",
                "मुण्डारी में दैनिक उपयोग की 10 बुनियादी क्रियाएं समझाएं"
            )
            ChatPersonaRole.TRIBAL_LINGUIST -> listOf(
                "ओल चिकी (Ol Chiki) लिपि के 6 मूल स्वर और उच्चारण समझाएं",
                "हो (Warang Chiti) और मुण्डारी लिपि में क्या समानताएं हैं?",
                "संथाली और हो भाषा में 'पानी' और 'पेड़' के शब्दों की व्युत्पत्ति बताएं",
                "मुण्डारी भाषा के आदरसूचक सर्वनाम व वाक्य उदाहरण दें"
            )
            ChatPersonaRole.NIPUN_LESSON_PLANNER -> listOf(
                "कक्षा 1 के लिए 45-मिनट का द्विभाषी सर्कल-टाइम पाठ प्लान बनाएं",
                "स्थानीय महुआ व साल के पत्तों से संख्या 1 से 20 तक की गतिविधि",
                "बुनियादी साक्षरता (FLN) के लिए 3 मौखिक मूल्यांकन प्रश्न तैयार करें",
                "संथाली-हिंदी द्विभाषी कार्यपत्रक (Worksheet) की रूपरेखा तैयार करें"
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // --- 1. Role / Persona Picker Banner ---
        GlassmorphicCard(
            shape = RoundedCornerShape(20.dp),
            containerColor = glass.surface,
            elevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(1.dp, glass.borderLight),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🤖", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "BhashaSetu AI गुरुमित्र (Chatbot)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = selectedPersona.titleHindi,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            viewModel.clearChatHistory()
                            Toast.makeText(context, "संवाद साफ़ किया गया", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp).testTag("clear_chat_button")
                    ) {
                        Icon(
                            Icons.Outlined.DeleteSweep,
                            contentDescription = "Clear Chat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Persona Selection Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ChatPersonaRole.values()) { persona ->
                        val isSelected = persona == selectedPersona
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setChatPersona(persona) },
                            label = {
                                Text(
                                    text = persona.titleHindi.substringBefore(" ("),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Text(
                                    when (persona) {
                                        ChatPersonaRole.MTB_MLE_PEDAGOGY -> "🏫"
                                        ChatPersonaRole.TRIBAL_LINGUIST -> "📜"
                                        ChatPersonaRole.NIPUN_LESSON_PLANNER -> "🎯"
                                    },
                                    fontSize = 14.sp
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = glass.surface,
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = glass.borderLight,
                                selectedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("persona_chip_${persona.id}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Model Selection Row & Search Grounding Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(BorderStroke(1.dp, glass.borderLight), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Model Dropdown / Picker Pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "मॉडल:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        GeminiModelChoice.values().forEach { model ->
                            val isChosen = model == selectedModelChoice
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChosen) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                modifier = Modifier
                                    .clickable { viewModel.setChatModelChoice(model) }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = model.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Google Search Grounding Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            viewModel.toggleSearchGroundingInChat(!isSearchGrounding)
                        }
                    ) {
                        Text(
                            text = "🌐 सर्च",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSearchGrounding) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = isSearchGrounding,
                            onCheckedChange = { viewModel.toggleSearchGroundingInChat(it) },
                            modifier = Modifier.height(24.dp).testTag("chat_search_grounding_switch"),
                            thumbContent = null
                        )
                    }
                }
            }
        }

        // --- 2. Messages List ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageBubble(
                        message = message,
                        onCopyText = {
                            clipboardManager.setText(AnnotatedString(message.text))
                            Toast.makeText(context, "संदेश कॉपी किया गया", Toast.LENGTH_SHORT).show()
                        },
                        onSpeakText = { text ->
                            viewModel.speakText(text, "hi")
                        }
                    )
                }

                if (isSending) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(glass.surfaceFloating)
                                .border(BorderStroke(1.dp, glass.borderLight), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "BhashaSetu AI विचार कर रहा है (${selectedModelChoice.label})...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // --- 3. Quick Prompt Suggestions ---
        if (!isSending) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickPrompts) { prompt ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = glass.surface,
                        border = BorderStroke(1.dp, glass.borderLight),
                        modifier = Modifier.clickable {
                            viewModel.sendChatMessage(prompt)
                        }
                    ) {
                        Text(
                            text = "💡 $prompt",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // --- 4. Bottom Input Bar (Glassmorphic) ---
        GlassmorphicCard(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
            containerColor = glass.surfaceFloating,
            elevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.setChatInput(it) },
                    placeholder = {
                        Text(
                            text = "पूछें (उदा. संथाली में फल-सब्जियों के नाम)...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = glass.surfaceUltraLight,
                        unfocusedContainerColor = glass.surfaceUltraLight,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = glass.borderLight
                    ),
                    maxLines = 4
                )

                FilledIconButton(
                    onClick = { viewModel.sendChatMessage() },
                    enabled = inputText.isNotBlank() && !isSending,
                    shape = RoundedCornerShape(16.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("send_chat_message_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    onCopyText: () -> Unit,
    onSpeakText: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val glass = LocalGlassColors.current
    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, glass.borderLight),
                modifier = Modifier.size(32.dp).padding(top = 2.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🌿", fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        GlassmorphicCard(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            containerColor = if (isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.92f) else glass.surface,
            borderBrush = if (isUser) Brush.linearGradient(listOf(Color.White.copy(alpha = 0.4f), Color.Transparent)) else Brush.linearGradient(listOf(glass.borderHighlight, glass.borderLight)),
            elevation = if (isUser) 2.dp else 2.dp,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header details for bot message
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, glass.borderLight)
                        ) {
                            Text(
                                text = message.modelUsed?.substringBefore("-preview") ?: "Gemini 3.5 Flash",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (message.searchGrounded) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                border = BorderStroke(1.dp, glass.borderLight)
                            ) {
                                Text(
                                    text = "🌐 Google Search",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Message Text
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Footer with timestamp, TTS speaker & copy button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUser) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!isUser) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { onSpeakText(message.text) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Speak Text",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = onCopyText,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy Text",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                border = BorderStroke(1.dp, glass.borderLight),
                modifier = Modifier.size(32.dp).padding(top = 2.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("👨‍🏫", fontSize = 16.sp)
                }
            }
        }
    }
}

