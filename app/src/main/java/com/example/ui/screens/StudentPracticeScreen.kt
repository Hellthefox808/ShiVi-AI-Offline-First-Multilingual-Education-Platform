package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.StudentFlashcard
import com.example.domain.model.TargetLanguage
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.LanguageSelectorChipRow
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

import androidx.compose.ui.graphics.vector.ImageVector

enum class PracticeSection(val title: String, val icon: ImageVector, val emoji: String = "") {
    QUIZ("खेल पहेली", Icons.Default.Psychology, "🎯"),
    FLASHCARDS("शब्द चित्र कार्ड", Icons.Default.Style, "🃏"),
    MY_STARS("मेरी प्रगति", Icons.Default.Star, "⭐"),
    TEACHER_REPORT("शिक्षक रिपोर्ट", Icons.Default.Assessment, "📊")
}

@Composable
fun StudentPracticeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allAttempts by viewModel.allAttempts.collectAsState()
    val currentStudentIndex by viewModel.currentStudentIndex.collectAsState()
    val isQuizCompleted by viewModel.isQuizCompleted.collectAsState()
    val lastEarnedScore by viewModel.lastEarnedScore.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val studentFlashcards by viewModel.studentFlashcards.collectAsState()
    val selectedFlashcardCategory by viewModel.selectedFlashcardCategory.collectAsState()
    val studentStars by viewModel.studentStars.collectAsState()
    val glass = LocalGlassColors.current

    val currentQuizQuestion by viewModel.currentQuizQuestion.collectAsState()
    val currentQuizQuestionIndex by viewModel.currentQuizQuestionIndex.collectAsState()
    val practiceQuestions by viewModel.practiceQuestions.collectAsState()

    val currentStudent = allStudents.getOrNull(currentStudentIndex)
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var activePracticeSection by remember { mutableStateOf(PracticeSection.QUIZ) }

    // Reset local option state when question index changes
    LaunchedEffect(currentQuizQuestionIndex) {
        selectedOption = null
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
    ) {
        // Active Student Header Banner (BhashaSetu Collective Glass)
        item {
            GlassmorphicCard(
                shape = RoundedCornerShape(22.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                borderBrush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.4f), Color.Transparent)
                ),
                elevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "छात्र प्रोफ़ाइल",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentStudent?.name ?: "सुनीता मुर्मू",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "कक्षा ${currentStudent?.grade ?: 2} • ${currentStudent?.village ?: "खूंटी"} (${selectedLanguage.displayName})",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    // Student Star Score Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "$studentStars",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Next Student Switcher Button
                    IconButton(
                        onClick = {
                            viewModel.resetQuiz()
                        },
                        modifier = Modifier.testTag("switch_student_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "अगला छात्र",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Sub-Section Navigation Tabs (Pills)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PracticeSection.entries.forEach { section ->
                    val isSelected = activePracticeSection == section
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else glass.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else glass.borderLight
                        ),
                        shadowElevation = if (isSelected) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { activePracticeSection = section }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = section.icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = section.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Target Language Selector
        item {
            LanguageSelectorChipRow(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { viewModel.setLanguage(it) }
            )
        }

        when (activePracticeSection) {
            PracticeSection.QUIZ -> {
                // Question Navigator Row
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "प्रश्न प्रगति (${currentQuizQuestionIndex + 1}/${practiceQuestions.size}):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { viewModel.prevQuizQuestion() },
                                    modifier = Modifier.size(32.dp).testTag("prev_quiz_button")
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
                                }
                                IconButton(
                                    onClick = { viewModel.nextQuizQuestion() },
                                    modifier = Modifier.size(32.dp).testTag("next_quiz_button")
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                                }
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(practiceQuestions) { idx, q ->
                                val isCurrent = idx == currentQuizQuestionIndex
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else glass.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isCurrent) MaterialTheme.colorScheme.primary else glass.borderLight
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.selectQuizQuestion(idx) }
                                        .testTag("quiz_tab_$idx")
                                ) {
                                    Text(
                                        text = "प्रश्न ${idx + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Quiz Question Card
                item {
                    GlassmorphicCard(
                        shape = RoundedCornerShape(22.dp),
                        containerColor = glass.surface,
                        elevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    border = BorderStroke(1.dp, glass.borderLight)
                                ) {
                                    Text(
                                        text = "अभ्यास प्रश्न ${currentQuizQuestionIndex + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }

                                // Audio Pronunciation Button
                                FilledTonalButton(
                                    onClick = {
                                        viewModel.speakText(
                                            currentQuizQuestion.audioUtterance,
                                            "hi"
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("play_quiz_audio_button")
                                ) {
                                    Icon(
                                        imageVector = if (isSpeaking) Icons.Default.GraphicEq else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Audio question",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isSpeaking) "बोल रहा है..." else "🔊 प्रश्न सुनो",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Hindi + Native Script Question
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = currentQuizQuestion.questionHindi,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = currentQuizQuestion.questionTarget,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            HorizontalDivider(color = glass.borderLight)

                            // Options list
                            currentQuizQuestion.options.forEachIndexed { index, optionText ->
                                val isSelected = selectedOption == optionText
                                val optionLetter = ('A' + index).toString()
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else glass.surfaceTinted,
                                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, glass.borderLight),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            if (!isQuizCompleted) selectedOption = optionText
                                        }
                                        .testTag("quiz_option_$index")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = optionLetter,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Text(
                                            text = optionText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.weight(1f)
                                        )
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { if (!isQuizCompleted) selectedOption = optionText }
                                        )
                                    }
                                }
                            }

                            if (!isQuizCompleted) {
                                Button(
                                    onClick = {
                                        val isCorrect = selectedOption == currentQuizQuestion.options.getOrNull(currentQuizQuestion.correctIndex)
                                        viewModel.submitStudentQuiz(selectedOption ?: "", isCorrect)
                                    },
                                    enabled = selectedOption != null,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("submit_quiz_button")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("उत्तर सबमिट करें (Submit Answer)", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // Cheering Feedback for young learners
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = SuccessGreen.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, SuccessGreen),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("🎉", fontSize = 24.sp)
                                            Text(
                                                text = "शाबाश! अंक: $lastEarnedScore/100 ⭐",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = SuccessGreen
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = currentQuizQuestion.explanationHindi,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    viewModel.nextQuizQuestion()
                                                },
                                                shape = RoundedCornerShape(14.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 44.dp)
                                                    .minimumInteractiveComponentSize()
                                                    .testTag("next_question_button")
                                            ) {
                                                Text("अगला प्रश्न", fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    selectedOption = null
                                                    viewModel.resetQuiz()
                                                },
                                                shape = RoundedCornerShape(14.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 44.dp)
                                                    .minimumInteractiveComponentSize()
                                                    .testTag("next_student_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Face,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("अगला छात्र")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            PracticeSection.FLASHCARDS -> {
                // Category Filter Chips
                item {
                    val categories = listOf(
                        Triple("सभी", "सभी", Icons.Default.Apps),
                        Triple("पशु-पक्षी", "🐾 पशु-पक्षी (Animals)", Icons.Default.Pets),
                        Triple("गिनती", "🔢 गिनती (Numbers)", Icons.Default.FormatListNumbered),
                        Triple("प्रकृति व जंगल", "🌳 प्रकृति व जंगल (Nature)", Icons.Default.Park),
                        Triple("शाला व मित्र", "🏫 शाला व मित्र (School & Life)", Icons.Default.School)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { (label, fullCat, icon) ->
                            val isSelected = if (label == "सभी") selectedFlashcardCategory == null else selectedFlashcardCategory == fullCat
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setFlashcardCategory(if (label == "सभी") null else fullCat)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .heightIn(min = 44.dp)
                                    .minimumInteractiveComponentSize()
                            )
                        }
                    }
                }

                // Filtered Flashcards
                val filteredCards = if (selectedFlashcardCategory == null) {
                    studentFlashcards
                } else {
                    studentFlashcards.filter { it.category == selectedFlashcardCategory }
                }

                items(filteredCards) { card ->
                    val tribalWord = when (selectedLanguage) {
                        TargetLanguage.SANTHALI -> card.santhaliOlChiki
                        TargetLanguage.HO -> card.hoWord
                        TargetLanguage.MUNDARI -> card.mundariWord
                    }

                    GlassmorphicCard(
                        shape = RoundedCornerShape(20.dp),
                        containerColor = glass.surface,
                        elevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, glass.borderLight),
                                modifier = Modifier.size(62.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(card.iconEmoji, fontSize = 32.sp)
                                }
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = card.hindiWord,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = tribalWord,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "उच्चारण: ${card.devanagariPhonetic} • ${card.englishMeaning}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = card.exampleSentenceTribal,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            FilledTonalIconButton(
                                onClick = { viewModel.playFlashcardAudio(card) },
                                shape = CircleShape,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Listen tribal word",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            PracticeSection.MY_STARS -> {
                item {
                    GlassmorphicCard(
                        shape = RoundedCornerShape(22.dp),
                        containerColor = glass.surface,
                        elevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("🏆", fontSize = 48.sp)
                            Text(
                                text = "कुल सितारे: $studentStars ⭐",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${currentStudent?.name ?: "सुनीता"} ने बहुत अच्छा प्रदर्शन किया है!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            HorizontalDivider(color = glass.borderLight)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.MilitaryTech,
                                        contentDescription = null,
                                        tint = WarningAmber,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("भाषा सेतु रत्न", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text("3 भाषाएं", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("पहेली मास्टर", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text("5/5 प्रश्न", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = SanthaliAccent,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("दैनिक लय", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text("3 दिन", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                item {
                    SectionHeader(
                        title = "कक्षा के अन्य साथी (Classmates)",
                        subtitle = "टेबलेट साझा करने के लिए किसी भी छात्र को चुनें",
                        icon = Icons.Default.Groups
                    )
                }

                itemsIndexed(allStudents) { idx, student ->
                    val isCurrent = idx == currentStudentIndex
                    GlassmorphicCard(
                        shape = RoundedCornerShape(16.dp),
                        containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else glass.surface,
                        elevation = if (isCurrent) 3.dp else 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.resetQuiz()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (student.name.contains("सुनीता") || student.name.contains("सानिया")) "👧" else "👦",
                                fontSize = 22.sp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "रोल नं: ${student.rollNo} • ${student.village} • कुल अंक: ${student.totalScore} pts",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isCurrent) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            PracticeSection.TEACHER_REPORT -> {
                item {
                    SectionHeader(
                        title = "हालिया मूल्यांकन रिकॉर्ड (Teacher's Log)",
                        subtitle = "${allAttempts.size} प्रयास स्थानीय Room DB में सुरक्षित (WAL Mode)",
                        icon = Icons.Default.HistoryEdu
                    )
                }

                items(allAttempts) { attempt ->
                    GlassmorphicCard(
                        shape = RoundedCornerShape(16.dp),
                        containerColor = glass.surface,
                        elevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = attempt.studentName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = attempt.lessonTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (attempt.score >= 80) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, if (attempt.score >= 80) SuccessGreen.copy(alpha = 0.3f) else WarningAmber.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "${attempt.score} / ${attempt.maxScore} pts",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (attempt.score >= 80) SuccessGreen else WarningAmber,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
