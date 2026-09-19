package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LessonEntity
import com.example.data.local.WorksheetEntity
import com.example.data.local.parseQuestions
import com.example.data.seed.PreloadedData
import com.example.domain.model.GradeLevel
import com.example.domain.model.SubjectArea
import com.example.domain.model.TargetLanguage
import com.example.domain.model.WorksheetQuestion
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun LessonStudioScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedGrade by viewModel.selectedGrade.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val learningOutcome by viewModel.learningOutcome.collectAsState()
    val hindiPromptInput by viewModel.hindiPromptInput.collectAsState()
    val enableHighThinking by viewModel.enableHighThinking.collectAsState()
    val isGeneratingLesson by viewModel.isGeneratingLesson.collectAsState()
    val currentLessonDetail by viewModel.currentLessonDetail.collectAsState()
    val allLessons by viewModel.allLessons.collectAsState()
    val allWorksheets by viewModel.allWorksheets.collectAsState()
    val activeWorksheet by viewModel.activeWorksheet.collectAsState()
    val isGeneratingWorksheet by viewModel.isGeneratingWorksheet.collectAsState()
    val isWorksheetDialogVisible by viewModel.isWorksheetDialogVisible.collectAsState()

    val context = LocalContext.current
    val glassColors = LocalGlassColors.current
    val glass = glassColors

    var showOutcomeDropdown by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // Hero Banner: Pedagogical Studio Purpose (BhashaSetu Collective Hero)
        item {
            GlassmorphicCard(
                containerColor = BhashaNavyPrimary,
                borderBrush = Brush.linearGradient(
                    listOf(
                        BhashaTealSecondary.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.2f)
                    )
                ),
                elevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "Lesson Studio",
                                    tint = Color.White
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "शिक्षक पाठ स्टूडियो (Lesson Studio)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "NCERT/JCERT FLN Grounded • Santhali, Ho, Mundari",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        // Configuration Glass Card
        item {
            GlassmorphicCard(
                containerColor = glass.surface,
                elevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Target Language Selector
                    LanguageSelectorChipRow(
                        selectedLanguage = selectedLanguage,
                        onLanguageSelected = { viewModel.setLanguage(it) }
                    )

                    HorizontalDivider(color = glass.borderLight)

                    // Grade Selector
                    GradeSelectorChipRow(
                        selectedGrade = selectedGrade,
                        onGradeSelected = { viewModel.setGrade(it) }
                    )

                    // Learning Outcome Preset Selector
                    Column {
                        Text(
                            text = "पाठ्यक्रम अधिगम प्रतिफल (Learning Outcome):",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = learningOutcome,
                            onValueChange = { viewModel.setLearningOutcome(it) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = glass.surfaceUltraLight,
                                unfocusedContainerColor = glass.surfaceUltraLight,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = glass.borderLight
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("learning_outcome_input"),
                            trailingIcon = {
                                IconButton(onClick = { showOutcomeDropdown = !showOutcomeDropdown }) {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Outcome")
                                }
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                        DropdownMenu(
                            expanded = showOutcomeDropdown,
                            onDismissRequest = { showOutcomeDropdown = false }
                        ) {
                            PreloadedData.flnSyllabusTopics.forEach { topic ->
                                DropdownMenuItem(
                                    text = { Text(topic) },
                                    onClick = {
                                        viewModel.setLearningOutcome(topic)
                                        showOutcomeDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Quick Lesson Presets Row
                    Column {
                        Text(
                            text = "⚡ त्वरित पाठ विषय (1-Tap Presets):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val quickPresets = listOf(
                            "🔢 गिनती 1-10" to ("FLN-M1: 1 से 10 तक की वस्तुओं की गिनती" to "बच्चों को परिवेश की वस्तुओं से 1 से 10 तक गिनती सिखाएं।"),
                            "🧼 स्वच्छता" to ("FLN-H1: व्यक्तिगत स्वच्छता व हाथ धोना" to "बच्चों को साबुन से हाथ धोने के नियम मातृभाषा में समझाएं।"),
                            "🌳 सरजोम पेड़" to ("FLN-E1: साल (सरजोम) वृक्ष का सांस्कृतिक महत्व" to "बच्चों को साल के पेड़ (सरजोम दारे) के बारे में बताएं।"),
                            "🏫 कक्षा नियम" to ("FLN-L1: कक्षा में सहयोग और अनुशासन" to "कक्षा में एक-दूसरे का सहयोग करने के नियम सिखाएं।")
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quickPresets) { (label, preset) ->
                                AssistChip(
                                    onClick = {
                                        viewModel.setLearningOutcome(preset.first)
                                        viewModel.setHindiPrompt(preset.second)
                                    },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = glass.surface,
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = AssistChipDefaults.assistChipBorder(
                                        enabled = true,
                                        borderColor = glass.borderLight
                                    )
                                )
                            }
                        }
                    }

                    // Hindi Lesson Input
                    Column {
                        Text(
                            text = "शिक्षक का हिन्दी निर्देश / पाठ विषय (Hindi Prompt):",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = hindiPromptInput,
                            onValueChange = { viewModel.setHindiPrompt(it) },
                            placeholder = { Text("उदा. बच्चों को साल के पेड़ (Sarjom) का महत्व और पत्तियों की गिनती सिखाएं...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = glass.surfaceUltraLight,
                                unfocusedContainerColor = glass.surfaceUltraLight,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = glass.borderLight
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 90.dp)
                                .testTag("hindi_prompt_input"),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    // High Thinking Mode Toggle (Gemini 3.1 Pro high reasoning)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                            .border(BorderStroke(1.dp, glass.borderLight), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Thinking Mode",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "High Thinking Mode (गहन तर्कण)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "gemini-3.1-pro-preview (ThinkingLevel.HIGH)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Switch(
                            checked = enableHighThinking,
                            onCheckedChange = { viewModel.toggleHighThinking(it) },
                            modifier = Modifier.testTag("high_thinking_switch")
                        )
                    }

                    // Generate Action Button
                    Button(
                        onClick = { viewModel.generateLesson() },
                        enabled = !isGeneratingLesson && hindiPromptInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("generate_lesson_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isGeneratingLesson) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("शिक्षण अनुकूलन जनरेट हो रहा है...", color = Color.White)
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "पाठ जनरेट करें (Pedagogical RAG)",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Display Current Generated Result & Teacher Approval Gate
        if (currentLessonDetail != null) {
            val lesson = currentLessonDetail!!
            item {
                GlassmorphicCard(
                    containerColor = glass.surface,
                    borderBrush = Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            glass.borderHighlight
                        )
                    ),
                    borderWidth = 1.5.dp,
                    elevation = 4.dp,
                    modifier = Modifier.fillMaxWidth().testTag("generated_lesson_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎯 शिक्षण अनुकूलन परिणाम",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (lesson.status == "APPROVED") SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, if (lesson.status == "APPROVED") SuccessGreen.copy(alpha = 0.3f) else WarningAmber.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = if (lesson.status == "APPROVED") "✅ APPROVED & READY" else "⚠️ REVIEW REQUIRED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (lesson.status == "APPROVED") SuccessGreen else WarningAmber,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        QualityScoreBadge(
                            qualityScore = lesson.qualityScore,
                            groundingScore = lesson.groundingScore
                        )

                        // 1. Native Target Language Script (Ol Chiki / Devanagari)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, glassColors.borderLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "मातृभाषा पाठ (${lesson.targetLanguage} - Native Script):",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lesson.adaptedExplanation,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 24.sp
                                )
                            }
                        }

                        // 2. Roman Transliteration Guide for Non-Native Teacher
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, glassColors.borderLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🔤 शिक्षक उच्चारण लिपि (Roman Transliteration):",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lesson.transliterationText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }

                        // 3. Cultural Analogy & Real Life Bridge
                        if (lesson.culturalAnalogy.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Park, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                                Column {
                                    Text(
                                        text = "स्थानीय सांस्कृतिक संदर्भ (Local Analogy):",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = lesson.culturalAnalogy,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        // 4. Play-based Classroom Activity
                        if (lesson.activityPrompt.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.SportsEsports, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                                Column {
                                    Text(
                                        text = "कक्षा गतिविधि (Classroom Activity):",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = lesson.activityPrompt,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        // 5. Pronunciation guidance
                        if (lesson.pronunciationGuide.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Column {
                                    Text(
                                        text = "ध्वनि व उच्चारण निर्देश:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = lesson.pronunciationGuide,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = glassColors.borderLight)

                        // Bilingual Worksheet Action Button
                        OutlinedButton(
                            onClick = {
                                val existingWs = allWorksheets.find { it.lessonId == lesson.id }
                                if (existingWs != null) {
                                    viewModel.openWorksheet(existingWs)
                                } else {
                                    viewModel.generateWorksheetForLesson(lesson)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            if (isGeneratingWorksheet) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("कार्यपत्रक तैयार हो रहा है... (Generating Worksheet)")
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "📄 द्विभाषी कार्यपत्रक (Bilingual Worksheet Output)",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Teacher Verification & Approval Action
                        if (lesson.status != "APPROVED") {
                            Button(
                                onClick = { viewModel.approveLesson(lesson.id) },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("approve_lesson_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("शिक्षक सत्यापन व स्वीकृति (Approve & Publish to Field)")
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, tint = SuccessGreen, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "स्वीकृत व ऑफलाइन टैबलेट्स के लिए आउटबॉक्स में कतारबद्ध",
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Existing Lessons Library Section
        item {
            SectionHeader(
                title = "सहेजे गए FLN पाठ (Saved Curriculum Library)",
                subtitle = "${allLessons.size} पाठ उपलब्ध (ऑफलाइन सिंक सक्षम)",
                icon = Icons.Default.FolderSpecial
            )
        }

        items(allLessons) { lesson ->
            GlassmorphicCard(
                shape = RoundedCornerShape(18.dp),
                containerColor = glassColors.surface,
                elevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = lesson.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${lesson.grade} • ${lesson.subject}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (lesson.status == "APPROVED" || lesson.status == "PUBLISHED") SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (lesson.status == "APPROVED" || lesson.status == "PUBLISHED") SuccessGreen.copy(alpha = 0.3f) else WarningAmber.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = lesson.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (lesson.status == "APPROVED" || lesson.status == "PUBLISHED") SuccessGreen else WarningAmber,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = lesson.adaptedExplanation,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                val existingWs = allWorksheets.find { it.lessonId == lesson.id }
                                if (existingWs != null) {
                                    viewModel.openWorksheet(existingWs)
                                } else {
                                    viewModel.generateWorksheetForLesson(lesson)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("कार्यपत्रक (Worksheet)", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Available Bilingual Worksheets Section
        item {
            SectionHeader(
                title = "📋 पाठ्यचर्या कार्यपत्रक (Bilingual Worksheets Library)",
                subtitle = "${allWorksheets.size} द्विभाषी कार्यपत्रक उपलब्ध (ऑफलाइन प्रिंट/शेयर सक्षम)",
                icon = Icons.Default.Description
            )
        }

        items(allWorksheets) { ws ->
            GlassmorphicCard(
                shape = RoundedCornerShape(16.dp),
                containerColor = glassColors.surface,
                elevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ws.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${ws.grade} • ${ws.targetLanguage}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (ws.isApproved) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (ws.isApproved) SuccessGreen.copy(alpha = 0.3f) else WarningAmber.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = if (ws.isApproved) "स्वीकृत (Approved)" else "समीक्षा (Draft)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (ws.isApproved) SuccessGreen else WarningAmber,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = ws.instructions,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "प्रश्नों की संख्या: ${ws.parseQuestions().size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { viewModel.openWorksheet(ws) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("देखें (Preview)", style = MaterialTheme.typography.labelSmall)
                            }

                            IconButton(
                                onClick = { viewModel.shareOrPrintWorksheet(ws, context) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Bilingual Worksheet Dialog
    if (isWorksheetDialogVisible && activeWorksheet != null) {
        BilingualWorksheetDialog(
            worksheet = activeWorksheet!!,
            onDismiss = { viewModel.closeWorksheetDialog() },
            onShare = { viewModel.shareOrPrintWorksheet(activeWorksheet!!, context) },
            onApprove = { viewModel.approveWorksheet(activeWorksheet!!.id) }
        )
    }
}

@Composable
fun BilingualWorksheetDialog(
    worksheet: WorksheetEntity,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onApprove: () -> Unit
) {
    val questions = remember(worksheet.questionsJson) { worksheet.parseQuestions() }
    var showAnswerKey by remember { mutableStateOf(false) }
    val glassColors = LocalGlassColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .fillMaxHeight(0.90f),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Printable Classroom Header
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "भाषासेतु AI • द्विभाषी कार्यपत्रक",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (worksheet.isApproved) SuccessGreen.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (worksheet.isApproved) "✅ APPROVED" else "📝 DRAFT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (worksheet.isApproved) SuccessGreen else WarningAmber,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = worksheet.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("कक्षा: ${worksheet.grade}", style = MaterialTheme.typography.labelSmall)
                            Text("मातृभाषा: ${worksheet.targetLanguage}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = glassColors.borderLight)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("विद्यार्थी: ____________________", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("दिनांक: ________", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Instructions
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = glassColors.surface,
                    border = BorderStroke(1.dp, glassColors.borderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text(
                            text = worksheet.instructions,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Questions List
                questions.forEachIndexed { index, q ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = glassColors.surfaceFloating,
                        border = BorderStroke(1.dp, glassColors.borderLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "प्रश्न ${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = q.type,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Hindi Question
                            Text(
                                text = q.questionHindi,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            // Target Native Script Question
                            if (q.questionTarget.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = q.questionTarget,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            // Options
                            if (q.options.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    q.options.forEach { opt ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = glassColors.surface,
                                            border = BorderStroke(1.dp, glassColors.borderLight),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = opt,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Local Context Hint
                            if (q.localContextHint.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = q.localContextHint,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            // Answer reveal
                            if (showAnswerKey) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SuccessGreen.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "✅ सही उत्तर: ${q.correctAnswer}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle Answer Key
                TextButton(
                    onClick = { showAnswerKey = !showAnswerKey },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        imageVector = if (showAnswerKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (showAnswerKey) "उत्तर कुंजी छुपाएं (Hide Answer Key)" else "उत्तर कुंजी देखें (Teacher Answer Key)",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onShare,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("प्रिंट / साझा करें (Print & Share)")
                }
                if (!worksheet.isApproved) {
                    Button(
                        onClick = onApprove,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("स्वीकृत करें")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("बंद करें (Close)")
            }
        }
    )
}

