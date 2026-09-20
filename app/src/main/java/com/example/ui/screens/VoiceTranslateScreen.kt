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
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.domain.model.VoiceGender
import com.example.domain.model.VoiceSettings
import com.example.domain.model.VoiceSpeakerRole
import com.example.domain.model.VoiceTimbre
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
    val currentUtteranceId by viewModel.currentUtteranceId.collectAsState()
    val partialSpeech by viewModel.partialSpeech.collectAsState()
    val speechError by viewModel.speechError.collectAsState()
    val speechRmsDb by viewModel.speechRmsDb.collectAsState()
    val voiceSettings by viewModel.voiceSettings.collectAsState()
    val isVoiceSettingsOpen by viewModel.isVoiceSettingsOpen.collectAsState()
    val glass = LocalGlassColors.current

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Target Language Selection Header
        LanguageSelectorChipRow(
            selectedLanguage = selectedLanguage,
            onLanguageSelected = { viewModel.setLanguage(it) },
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Compact Dual-Direction Dialogue Mode Selector
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = glass.surface,
            border = BorderStroke(1.dp, glass.borderLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isTeacherRole = voiceSettings.activeSpeakerRole == VoiceSpeakerRole.TEACHER
                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = if (isTeacherRole) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .clickable { viewModel.setVoiceSpeakerRole(VoiceSpeakerRole.TEACHER) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = if (isTeacherRole) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "शिक्षक (हिंदी ➔ मातृभाषा)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isTeacherRole) FontWeight.Bold else FontWeight.Normal,
                            color = if (isTeacherRole) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = if (!isTeacherRole) MaterialTheme.colorScheme.secondary else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .clickable { viewModel.setVoiceSpeakerRole(VoiceSpeakerRole.STUDENT) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            tint = if (!isTeacherRole) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "विद्यार्थी (मातृभाषा ➔ हिंदी)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (!isTeacherRole) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isTeacherRole) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Sleek SLA & Quick Settings Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Live SLA & Offline Indicator Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SuccessGreen.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "Sub-2s SLA",
                        tint = SuccessGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "लाइव < 2s SLA • 100% ऑफलाइन",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Voice Gender (Male / Female) Quick Toggle
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = glass.surface,
                    border = BorderStroke(1.dp, glass.borderLight),
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val next = if (voiceSettings.voiceGender == VoiceGender.FEMALE) VoiceGender.MALE else VoiceGender.FEMALE
                            viewModel.setVoiceGender(next)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(text = voiceSettings.voiceGender.icon, fontSize = 11.sp)
                        Text(
                            text = voiceSettings.voiceGender.displayNameHindi,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // FLN Slow Speed Toggle
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (voiceSettings.isSlowClassroomMode) WarningAmber.copy(alpha = 0.18f) else glass.surface,
                    border = BorderStroke(1.dp, if (voiceSettings.isSlowClassroomMode) WarningAmber else glass.borderLight),
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.toggleSlowClassroomMode(!voiceSettings.isSlowClassroomMode) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = if (voiceSettings.isSlowClassroomMode) "🐢 FLN 0.72x" else "⚡ 0.92x",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = if (voiceSettings.isSlowClassroomMode) WarningAmber else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Clear turns button
                if (voiceTurns.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearVoiceTurns() },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Session",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Voice Fine-Tuning Sheet Trigger
                IconButton(
                    onClick = { viewModel.openVoiceSettings(true) },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Voice Settings",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Quick Classroom Phrases Carousel
        val classroomQuickPhrases by viewModel.classroomQuickPhrases.collectAsState()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "त्वरित कक्षा निर्देश (Quick Classroom Phrases - 1-Tap Relay):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(classroomQuickPhrases) { phrase ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = glass.surface,
                    border = BorderStroke(1.dp, glass.borderLight),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            viewModel.triggerClassroomQuickPhrase(phrase)
                            coroutineScope.launch {
                                listState.animateScrollToItem((voiceTurns.size).coerceAtLeast(0))
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(phrase.iconEmoji, fontSize = 14.sp)
                        Text(
                            text = phrase.hindiText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Play Relay",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Live Listening Banner when Microphone is active
        AnimatedVisibility(
            visible = isListening,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            GlassmorphicCard(
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.90f),
                borderBrush = Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ),
                elevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .scale(pulseScale)
                                    .background(Color.Red, CircleShape)
                            )
                            Text(
                                text = "लाइव रिकॉर्डिंग (Live Listening)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            LiveAudioWaveformVisualizer(
                                rmsDb = speechRmsDb,
                                isListening = isListening,
                                activeColor = MaterialTheme.colorScheme.error,
                                maxBarHeight = 18.dp,
                                minBarHeight = 4.dp,
                                barCount = 7
                            )
                        }

                        FilledTonalButton(
                            onClick = { viewModel.stopVoiceRecognition() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .heightIn(min = 36.dp)
                                .minimumInteractiveComponentSize()
                        ) {
                            Text("रोकें", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (partialSpeech.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    text = "\"$partialSpeech\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "लाइव अनुवाद तैयार हो रहा है (${selectedLanguage.displayName})...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "शिक्षक कक्षा में बोलें — आवाज़ को पहचान कर रीयल-टाइम अनुवाद किया जाएगा...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                        )
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

        // Conversation History Thread or Empty State
        if (voiceTurns.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                GlassmorphicCard(
                    shape = RoundedCornerShape(24.dp),
                    containerColor = glass.surface,
                    borderBrush = Brush.linearGradient(
                        listOf(glass.borderHighlight, glass.borderLight)
                    ),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        Text(
                            text = "कक्षा अनुवाद सत्र तैयार है",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "शिक्षक नीचे दिए गए माइक बटन पर टैप करके बोलें या त्वरित कक्षा वाक्यों में से चुनें। भाषासेतु 100% ऑफलाइन रहते हुए भी रीयल-टाइम अनुवाद करेगा।",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
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
                        currentUtteranceId = currentUtteranceId,
                        onPlayTribal = { viewModel.playTribalSpeech(turn, false) },
                        onPlaySlowTribal = { viewModel.playTribalSpeech(turn, true) },
                        onPlayBilingual = { viewModel.playBilingualRelay(turn) },
                        onPlayHindi = { viewModel.playSourceHindi(turn) },
                        onPlaySyllable = { viewModel.playSyllable(it) },
                        onToggleFavorite = { viewModel.toggleFavoriteVoiceTurn(turn.id) },
                        onStopAudio = { viewModel.stopAudioPlayback() }
                    )
                }
            }
        }

        // Microphone & Utterance Input Bar (Glassmorphic)
        GlassmorphicCard(
            shape = RoundedCornerShape(24.dp),
            elevation = 3.dp,
            containerColor = glass.surfaceFloating,
            borderBrush = Brush.linearGradient(
                listOf(glass.borderHighlight, glass.borderLight)
            ),
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
                    placeholder = {
                        Text(
                            if (voiceSettings.activeSpeakerRole == VoiceSpeakerRole.STUDENT)
                                "विद्यार्थी मातृभाषा में बोलें या टाइप करें..."
                            else
                                "शिक्षक हिन्दी में बोलें या टाइप करें..."
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = glass.surfaceUltraLight,
                        unfocusedContainerColor = glass.surfaceUltraLight,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = glass.borderLight
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("voice_text_input"),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                // Inline Waveform Visualizer when recording
                if (isListening) {
                    LiveAudioWaveformVisualizer(
                        rmsDb = speechRmsDb,
                        isListening = true,
                        activeColor = MaterialTheme.colorScheme.error,
                        maxBarHeight = 22.dp,
                        minBarHeight = 5.dp,
                        barCount = 5
                    )
                }

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
                            inputUtterance.isNotBlank() -> Icons.AutoMirrored.Filled.Send
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
                onGenderChange = { viewModel.setVoiceGender(it) },
                onTimbreChange = { viewModel.setVoiceTimbre(it) },
                onRateChange = { viewModel.updateSpeechRate(it) },
                onPitchChange = { viewModel.updatePitch(it) },
                onRelayPauseChange = { viewModel.updateRelayPauseMs(it) },
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
                            testDevanagari,
                            pauseMs = voiceSettings.relayPauseMs
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
    onGenderChange: (VoiceGender) -> Unit,
    onTimbreChange: (VoiceTimbre) -> Unit,
    onRateChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onRelayPauseChange: (Long) -> Unit,
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

        // 0. Voice Gender Selection (Male / Female)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "आवाज़ का लिंग (Voice Gender):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                VoiceGender.values().forEach { gender ->
                    val isSelected = (settings.voiceGender == gender)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onGenderChange(gender) }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = gender.icon, fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = gender.displayNameHindi,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = gender.displayNameEnglish,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 1. Voice Timbre Presets
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "ध्वनि स्वरूप प्रीसेट (Voice Timbre Preset):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(VoiceTimbre.values()) { timbre ->
                    val isSelected = (settings.timbre == timbre)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTimbreChange(timbre) },
                        label = {
                            Text(
                                text = timbre.displayNameHindi,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
            Text(
                text = settings.timbre.descriptionHindi,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // 2. Speech Rate Slider
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

        // 3. Pitch Slider
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

        // 4. Relay Pause Duration Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "रिले ठहराव (Relay Pause):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${settings.relayPauseMs} ms",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = settings.relayPauseMs.toFloat(),
                onValueChange = { onRelayPauseChange(it.toLong()) },
                valueRange = 250f..750f,
                steps = 9,
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
            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
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
    currentUtteranceId: String? = null,
    onPlayTribal: () -> Unit,
    onPlaySlowTribal: () -> Unit,
    onPlayBilingual: () -> Unit,
    onPlayHindi: () -> Unit,
    onPlaySyllable: (String) -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onStopAudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    val isTribalSpeaking = isSpeaking && (currentUtteranceId == "tr_${turn.id}")
    val isBilingualSpeaking = isSpeaking && (currentUtteranceId?.startsWith("relay_${turn.id}") == true)
    val isHindiSpeaking = isSpeaking && (currentUtteranceId == "hi_${turn.id}")
    val isThisTurnSpeaking = isTribalSpeaking || isBilingualSpeaking || isHindiSpeaking
    val isStudentTurn = turn.speakerRole == VoiceSpeakerRole.STUDENT || !turn.isTeacher

    GlassmorphicCard(
        shape = RoundedCornerShape(20.dp),
        containerColor = if (isThisTurnSpeaking) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        } else if (isStudentTurn) {
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f)
        } else {
            glass.surface
        },
        borderBrush = Brush.linearGradient(
            if (isThisTurnSpeaking) {
                listOf(MaterialTheme.colorScheme.primary, glass.borderHighlight)
            } else if (isStudentTurn) {
                listOf(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f), glass.borderLight)
            } else {
                listOf(glass.borderHighlight, glass.borderLight)
            }
        ),
        elevation = if (isThisTurnSpeaking) 4.dp else 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Speaker Header & Latency & Favorite Action
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
                        color = if (isStudentTurn) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        border = BorderStroke(1.dp, glass.borderLight),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isStudentTurn) Icons.Default.Face else Icons.Default.Person,
                                contentDescription = if (isStudentTurn) "Student" else "Teacher",
                                tint = if (isStudentTurn) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isStudentTurn) "विद्यार्थी (Student Spoken Tribal):" else "शिक्षक (Hindi Source):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isStudentTurn) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Bookmark / Favorite Toggle
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (turn.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Bookmark phrase",
                            tint = if (turn.isFavorite) WarningAmber else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    LatencyBadge(latencyMs = turn.latencyMs)
                }
            }

            // Primary Spoken Text
            if (isStudentTurn) {
                // In Student turn, primary utterance is tribal speech
                Text(
                    text = turn.targetText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                // In Teacher turn, primary utterance is Hindi
                Text(
                    text = turn.hindiText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(color = glass.borderLight)

            // Secondary / Translated Target Header
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
                        imageVector = if (isStudentTurn) Icons.Default.School else Icons.Default.Campaign,
                        contentDescription = "Translation",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isStudentTurn) {
                            "शिक्षक समझ (Hindi Comprehension):"
                        } else {
                            "${targetLanguage.displayName} (${targetLanguage.nativeName}):"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Script Indicator
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = if (isStudentTurn) "हिंदी (शिक्षक हेतु)" else targetLanguage.scriptName.substringBefore(" &"),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Secondary / Translated Text
            if (isStudentTurn) {
                Text(
                    text = turn.hindiText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Text(
                    text = turn.targetText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Transliteration Phonetic Guides & Interactive Syllables
            if (turn.transliterationDevanagari.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, glass.borderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
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

                        // Syllable pills for FLN practice
                        if (turn.phoneticSyllables.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "वर्ण / शब्दांश अभ्यास (Tap syllable to hear):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(turn.phoneticSyllables) { syllable ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                        shadowElevation = 1.dp,
                                        modifier = Modifier
                                            .heightIn(min = 44.dp)
                                            .minimumInteractiveComponentSize()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onPlaySyllable(syllable) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = syllable,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
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
                if (isStudentTurn) {
                    // In Student turn: Primary button plays Hindi comprehension for teacher
                    FilledTonalButton(
                        onClick = {
                            if (isHindiSpeaking) onStopAudio() else onPlayHindi()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = if (isHindiSpeaking) {
                            ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            ButtonDefaults.filledTonalButtonColors()
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .heightIn(min = 40.dp)
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = if (isHindiSpeaking) Icons.Default.GraphicEq else Icons.Default.Hearing,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindiSpeaking) "चल रहा है..." else "हिंदी समझ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Play student tribal utterance
                    OutlinedButton(
                        onClick = {
                            if (isTribalSpeaking) onStopAudio() else onPlayTribal()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp)
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = if (isTribalSpeaking) Icons.Default.GraphicEq else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("मातृभाषा", fontSize = 11.sp)
                    }

                    // Slow FLN Practice
                    OutlinedButton(
                        onClick = onPlaySlowTribal,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(0.9f)
                            .heightIn(min = 40.dp)
                            .minimumInteractiveComponentSize()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SlowMotionVideo,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Text("धीमा", fontSize = 11.sp)
                        }
                    }
                } else {
                    // Teacher Turn: Play Tribal Audio (Phonetic synthesis)
                    FilledTonalButton(
                        onClick = {
                            if (isTribalSpeaking) onStopAudio() else onPlayTribal()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = if (isTribalSpeaking) {
                            ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            ButtonDefaults.filledTonalButtonColors()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("play_tribal_${turn.id}")
                    ) {
                        Icon(
                            imageVector = if (isTribalSpeaking) Icons.Default.GraphicEq else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isTribalSpeaking) "चल रहा है..." else "मातृभाषा", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Bilingual Relay (Hindi + Tribal)
                    OutlinedButton(
                        onClick = {
                            if (isBilingualSpeaking) onStopAudio() else onPlayBilingual()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isBilingualSpeaking) {
                            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            ButtonDefaults.outlinedButtonBorder(enabled = true)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp)
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = if (isBilingualSpeaking) Icons.Default.GraphicEq else Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBilingualSpeaking) "रिले चालू..." else "द्विभाषी रिले", fontSize = 11.sp)
                    }

                    // Slow FLN Practice
                    OutlinedButton(
                        onClick = onPlaySlowTribal,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(0.9f)
                            .heightIn(min = 40.dp)
                            .minimumInteractiveComponentSize()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SlowMotionVideo,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Text("धीमा", fontSize = 11.sp)
                        }
                    }

                    // Hindi Source
                    IconButton(
                        onClick = {
                            if (isHindiSpeaking) onStopAudio() else onPlayHindi()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = if (isHindiSpeaking) Icons.Default.GraphicEq else Icons.Default.Hearing,
                            contentDescription = "Play Hindi",
                            tint = if (isHindiSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Real-time dynamic audio waveform visualizer driven by SpeechRecognizer RMS dB.
 * Oscillates organic multi-bar amplitudes with responsive spring physics.
 */
@Composable
fun LiveAudioWaveformVisualizer(
    rmsDb: Float,
    isListening: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 7,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    maxBarHeight: androidx.compose.ui.unit.Dp = 22.dp,
    minBarHeight: androidx.compose.ui.unit.Dp = 4.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_phase")

    val phase0 by infiniteTransition.animateFloat(
        initialValue = 0.35f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(320, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "p0"
    )
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0.40f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(390, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "p1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0.30f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(460, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "p2"
    )
    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0.50f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "p3"
    )
    val phase4 by infiniteTransition.animateFloat(
        initialValue = 0.30f, targetValue = 0.90f,
        animationSpec = infiniteRepeatable(tween(440, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "p4"
    )
    val phase5 by infiniteTransition.animateFloat(
        initialValue = 0.45f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(370, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "p5"
    )
    val phase6 by infiniteTransition.animateFloat(
        initialValue = 0.35f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(340, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "p6"
    )

    val phases = listOf(phase0, phase1, phase2, phase3, phase4, phase5, phase6)

    val normalizedRms = if (!isListening) {
        0.1f
    } else {
        ((rmsDb.coerceAtLeast(0f) + 1f) / 9f).coerceIn(0.2f, 1.0f)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val count = barCount.coerceIn(1, phases.size)
        for (i in 0 until count) {
            val phase = phases[i % phases.size]
            val centerWeight = 1f - (kotlin.math.abs(i - (count / 2f)) / count.toFloat())
            val targetFraction = if (isListening) {
                (normalizedRms * phase * (0.5f + 0.5f * centerWeight)).coerceIn(0.18f, 1.0f)
            } else {
                0.15f
            }

            val animatedHeight by animateDpAsState(
                targetValue = minBarHeight + (maxBarHeight - minBarHeight) * targetFraction,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "bar_$i"
            )

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(animatedHeight)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(
                        if (isListening) {
                            activeColor.copy(alpha = (0.5f + 0.5f * phase).coerceIn(0.4f, 1f))
                        } else {
                            activeColor.copy(alpha = 0.25f)
                        }
                    )
            )
        }
    }
}
