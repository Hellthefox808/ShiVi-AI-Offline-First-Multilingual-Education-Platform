package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.domain.model.TargetLanguage
import com.example.domain.model.VoiceSettings
import com.example.domain.model.VoiceTurn
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.LanguageSelectorChipRow
import com.example.ui.components.LatencyBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceTranslateScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val voiceTurns by viewModel.voiceTurns.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val partialSpeech by viewModel.partialSpeech.collectAsState()
    val speechError by viewModel.speechError.collectAsState()
    val speechRmsDb by viewModel.speechRmsDb.collectAsState()
    val voiceSettings by viewModel.voiceSettings.collectAsState()
    val isVoiceSettingsOpen by viewModel.isVoiceSettingsOpen.collectAsState()

    var inputUtterance by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Permission launcher for Microphone
    var hasRecordAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRecordAudioPermission = isGranted
        if (isGranted) {
            viewModel.startVoiceRecognition()
        }
    }

    // Dynamic animation scale for pulsating mic when listening
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val quickPhrases = listOf(
        "नमस्ते बच्चों! (Welcome)" to "नमस्ते बच्चों! आज हम सब मिलकर पढ़ाई करेंगे।",
        "किताबें खोलिए (Open books)" to "अपनी किताबें खोलिए और पाठ निकालिए।",
        "साल का पेड़ (Sal tree)" to "इस पेड़ का नाम बताइए और इसके उपयोग बताएं।",
        "एक साथ बोलिए (Repeat together)" to "सभी बच्चे एक साथ मिलकर बोलिए।",
        "बहुत शाबाश! (Well done!)" to "बहुत शाबाश! आप सभी ने सही उत्तर दिया।",
        "नदी का पानी (River water)" to "गाँव की नदी का पानी स्वच्छ और जीवनदायी है।",
        "नई कविता (Learn poem)" to "आज हम सब मिलकर नई कविता सीखेंगे और गाएंगे।"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Target Language Selection Header
        LanguageSelectorChipRow(
            selectedLanguage = selectedLanguage,
            onLanguageSelected = { viewModel.setLanguage(it) },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Real-Time SLA & Voice Settings Header Card (Glassmorphic)
        GlassmorphicCard(
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            borderBrush = Brush.linearGradient(
                listOf(GlassBorderHighlight, GlassBorderLight)
            ),
            elevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "Live SLA",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Live Classroom Voice Relay",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Rate: ${"%.2f".format(voiceSettings.speechRate)}x • Pitch: ${"%.1f".format(voiceSettings.pitch)}x • ${if (voiceSettings.isBilingualRelayEnabled) "द्विभाषी रिले चालू" else "मातृभाषा मात्र"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Voice Fine-Tuning Sheet Trigger Button
                FilledTonalButton(
                    onClick = { viewModel.openVoiceSettings(true) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Voice Settings",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "ध्वनि ट्यूनिंग", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Quick Classroom Phrases Carousel
        Text(
            text = "त्वरित कक्षा वाक्य (Quick Classroom Phrases):",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(quickPhrases) { (label, fullText) ->
                AssistChip(
                    onClick = {
                        viewModel.sendVoiceUtterance(fullText)
                        coroutineScope.launch {
                            listState.animateScrollToItem((voiceTurns.size).coerceAtLeast(0))
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = GlassSurfaceLight,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = GlassBorderLight
                    ),
                    label = { Text(label.substringBefore(" ("), fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }

        // Live Listening Banner when Microphone is active
        AnimatedVisibility(
            visible = isListening,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .scale(pulseScale)
                            .background(Color.Red, CircleShape)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🎤 सुन रहे हैं... शिक्षक हिन्दी में बोलें",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        if (partialSpeech.isNotBlank()) {
                            Text(
                                text = "\"$partialSpeech\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    TextButton(
                        onClick = { viewModel.stopVoiceRecognition() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("रोकें", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Error message banner if speech recognition fails
        speechError?.let { error ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Conversation History Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(voiceTurns) { turn ->
                VoiceTurnBubble(
                    turn = turn,
                    targetLanguage = selectedLanguage,
                    isSpeaking = isSpeaking,
                    onPlayTribal = { viewModel.playTribalSpeech(turn, false) },
                    onPlaySlowTribal = { viewModel.playTribalSpeech(turn, true) },
                    onPlayBilingual = { viewModel.playBilingualRelay(turn) },
                    onPlayHindi = { viewModel.playSourceHindi(turn) }
                )
            }
        }

        // Microphone & Utterance Input Bar (Glassmorphic)
        GlassmorphicCard(
            shape = RoundedCornerShape(24.dp),
            elevation = 3.dp,
            containerColor = GlassSurfaceFloating,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputUtterance,
                    onValueChange = { inputUtterance = it },
                    placeholder = { Text("शिक्षक हिन्दी में बोलें या टाइप करें...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GlassSurfaceUltraLight,
                        unfocusedContainerColor = GlassSurfaceUltraLight,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = GlassBorderLight
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("voice_text_input"),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                // Voice Mic / Send Action Button
                FilledIconButton(
                    onClick = {
                        if (inputUtterance.isNotBlank()) {
                            viewModel.sendVoiceUtterance(inputUtterance)
                            inputUtterance = ""
                            coroutineScope.launch {
                                listState.animateScrollToItem((voiceTurns.size).coerceAtLeast(0))
                            }
                        } else {
                            // Toggle Speech Recognition
                            if (isListening) {
                                viewModel.stopVoiceRecognition()
                            } else {
                                if (hasRecordAudioPermission) {
                                    viewModel.startVoiceRecognition()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .scale(if (isListening) pulseScale else 1f)
                        .testTag("mic_send_button")
                ) {
                    Icon(
                        imageVector = when {
                            inputUtterance.isNotBlank() -> Icons.Default.Send
                            isListening -> Icons.Default.Stop
                            else -> Icons.Default.Mic
                        },
                        contentDescription = "Voice Input",
                        tint = Color.White
                    )
                }
            }
        }
    }

    // Voice Fine-Tuning Modal Sheet
    if (isVoiceSettingsOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.openVoiceSettings(false) },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            VoiceSettingsSheetContent(
                settings = voiceSettings,
                targetLanguage = selectedLanguage,
                onRateChange = { viewModel.updateSpeechRate(it) },
                onPitchChange = { viewModel.updatePitch(it) },
                onBilingualToggle = { viewModel.toggleBilingualRelay(it) },
                onSlowModeToggle = { viewModel.toggleSlowClassroomMode(it) },
                onAutoPlayToggle = { viewModel.toggleAutoPlayOnTranslate(it) },
                onTestVoice = {
                    val testDevanagari = when (selectedLanguage) {
                        TargetLanguage.SANTHALI -> "जोहार गिदरा को! तेहेञ दो आबो सारजोम दारे बाबोत बोन पाढ़ाव-आ।"
                        TargetLanguage.HO -> "जोहार गिदरा को! तिसिंग दो आबो मियाद ते बोन चेद-आ।"
                        TargetLanguage.MUNDARI -> "जोहार गिदरा को! तिसिंग दो आबु मियाद ते बु पढ़व-ए।"
                    }
                    if (voiceSettings.isBilingualRelayEnabled) {
                        viewModel.ttsManager.speakBilingualRelay(
                            "नमस्ते बच्चों! आज हम सब मिलकर पढ़ाई करेंगे।",
                            testDevanagari
                        )
                    } else {
                        viewModel.ttsManager.speakTribalPhonetic(
                            devanagariPhonetic = testDevanagari,
                            fallbackText = "जोहार",
                            slowMode = voiceSettings.isSlowClassroomMode
                        )
                    }
                },
                onClose = { viewModel.openVoiceSettings(false) }
            )
        }
    }
}

@Composable
fun VoiceSettingsSheetContent(
    settings: VoiceSettings,
    targetLanguage: TargetLanguage,
    onRateChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onBilingualToggle: (Boolean) -> Unit,
    onSlowModeToggle: (Boolean) -> Unit,
    onAutoPlayToggle: (Boolean) -> Unit,
    onTestVoice: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SettingsVoice,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "ध्वनि प्रणाली फाइन-ट्यूनिंग (Voice Tuning)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        HorizontalDivider()

        // Speech Rate Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "उच्चारण गति (Speech Rate):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${"%.2f".format(settings.speechRate)}x (${if (settings.speechRate < 0.85f) "कक्षा के लिए धीमा" else if (settings.speechRate > 1.0f) "तेज़" else "सामान्य"})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = settings.speechRate,
                onValueChange = onRateChange,
                valueRange = 0.65f..1.3f,
                steps = 6,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Pitch Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "स्वर पिच (Voice Pitch):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${"%.2f".format(settings.pitch)}x (${if (settings.pitch < 0.95f) "गंभीर/प्रौढ़" else if (settings.pitch > 1.05f) "उत्साही/बाल-मित्र" else "प्राकृतिक"})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = settings.pitch,
                onValueChange = onPitchChange,
                valueRange = 0.8f..1.3f,
                steps = 5,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Bilingual Relay Toggle
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "द्विभाषी रिले मोड (Bilingual Relay)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "शिक्षक की हिन्दी बात के तुरंत बाद मातृभाषा अनुवाद बोले",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.isBilingualRelayEnabled,
                    onCheckedChange = onBilingualToggle
                )
            }
        }

        // Foundational Slow Mode Toggle
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FLN बालवाटिका धीमी गति (0.72x)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "कक्षा 1-2 के बच्चों को साफ़ समझने के लिए धीमा उच्चारण",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.isSlowClassroomMode,
                    onCheckedChange = onSlowModeToggle
                )
            }
        }

        // Test Voice Button
        Button(
            onClick = onTestVoice,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp)
        ) {
            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("ध्वनि परीक्षण करें (Test ${targetLanguage.displayName} Voice)")
        }
    }
}

@Composable
fun VoiceTurnBubble(
    turn: VoiceTurn,
    targetLanguage: TargetLanguage,
    isSpeaking: Boolean,
    onPlayTribal: () -> Unit,
    onPlaySlowTribal: () -> Unit,
    onPlayBilingual: () -> Unit,
    onPlayHindi: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        shape = RoundedCornerShape(20.dp),
        containerColor = GlassSurfaceLight,
        borderBrush = Brush.linearGradient(
            listOf(GlassBorderHighlight, GlassBorderLight)
        ),
        elevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Speaker Header & Latency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        border = BorderStroke(1.dp, GlassBorderLight),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Teacher",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "शिक्षक (Hindi Source):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                LatencyBadge(latencyMs = turn.latencyMs)
            }

            // Hindi Text
            Text(
                text = turn.hindiText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(color = GlassBorderLight)

            // Target Mother-Tongue Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Target Speech",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${targetLanguage.displayName} (${targetLanguage.nativeName}):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Native Script indicator
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = targetLanguage.scriptName.substringBefore(" &"),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Native Script Spoken Line
            Text(
                text = turn.targetText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            // Transliteration Phonetic Guides
            if (turn.transliterationDevanagari.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, GlassBorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(
                            text = "🔊 उच्चारण (Devanagari): ${turn.transliterationDevanagari}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (turn.transliteration.isNotBlank()) {
                            Text(
                                text = "🔤 Roman: ${turn.transliteration}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Fine-Tuned Audio Playback Control Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play Tribal Audio (Phonetic synthesis)
                FilledTonalButton(
                    onClick = onPlayTribal,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("play_tribal_${turn.id}")
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.GraphicEq else Icons.Default.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("मातृभाषा", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                // Bilingual Relay (Hindi + Tribal)
                OutlinedButton(
                    onClick = onPlayBilingual,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("द्विभाषी रिले", fontSize = 11.sp)
                }

                // Slow FLN Practice
                OutlinedButton(
                    onClick = onPlaySlowTribal,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(34.dp)
                ) {
                    Text("🐢 धीमा", fontSize = 11.sp)
                }

                // Hindi Source
                IconButton(
                    onClick = onPlayHindi,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        Icons.Default.Hearing,
                        contentDescription = "Play Hindi",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
