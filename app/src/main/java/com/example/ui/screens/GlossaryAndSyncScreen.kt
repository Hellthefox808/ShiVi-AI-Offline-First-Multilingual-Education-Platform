package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.local.CurriculumContentEntity
import com.example.data.local.GlossaryEntity
import com.example.data.seed.PreloadedData
import com.example.domain.model.RagCurriculumMatch
import com.example.domain.model.RagMatchType
import com.example.domain.model.RagQueryContext
import com.example.domain.model.TargetLanguage
import com.example.ui.components.FullStackArchitectureView
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.LanguageSelectorChipRow
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun GlossaryAndSyncScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val searchedGlossary by viewModel.searchedGlossary.collectAsState()
    val glossaryQuery by viewModel.glossaryQuery.collectAsState()
    val searchedCurriculum by viewModel.searchedCurriculum.collectAsState()
    val curriculumQuery by viewModel.curriculumSearchQuery.collectAsState()
    val selectedCurriculumLanguageFilter by viewModel.selectedCurriculumLanguageFilter.collectAsState()
    
    val ragQueryContext by viewModel.ragQueryContext.collectAsState()
    val ragQueryInput by viewModel.ragQueryInput.collectAsState()
    val selectedRagLanguage by viewModel.selectedRagLanguage.collectAsState()
    
    val pendingOutboxCount by viewModel.pendingOutboxCount.collectAsState()
    val isOfflineSimulated by viewModel.isOfflineSimulated.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val recentSyncLogs by viewModel.recentSyncLogs.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) }
    var ragViewMode by remember { mutableStateOf(0) } // 0: Local Embedding RAG, 1: Full Schema Browser
    val subTabs = listOf("📚 जनजातीय शब्दकोश", "📖 RAG ज्ञानकोष (Offline)", "🔄 आउटबॉक्स सिंक", "🏛️ आर्किटेक्चर व टेक स्टैक")
    val glass = LocalGlassColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Tab Selector in Glass Card
        GlassmorphicCard(
            shape = RoundedCornerShape(18.dp),
            containerColor = glass.surfaceFloating,
            elevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            ScrollableTabRow(
                selectedTabIndex = activeSubTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 8.dp,
                divider = {}
            ) {
                subTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = activeSubTab == index,
                        onClick = { activeSubTab = index },
                        text = { Text(title, fontSize = 11.sp, fontWeight = if (activeSubTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (activeSubTab) {
            0 -> {
                // --- SUB-TAB 0: Tribal FLN Glossary & Search ---
                OutlinedTextField(
                    value = glossaryQuery,
                    onValueChange = { viewModel.setGlossarySearch(it) },
                    placeholder = { Text("शब्द खोजें (उदा. पेड़, पानी, ᱫᱟᱨᱮ, गाय)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("glossary_search_input"),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(searchedGlossary) { item ->
                        GlossaryItemCard(
                            item = item,
                            onSpeakText = { text -> viewModel.speakText(text, "hi") }
                        )
                    }
                }
            }

            1 -> {
                // --- SUB-TAB 1: Local Semantic Embedding & Hybrid RAG Knowledge Engine ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = ragViewMode == 0,
                        onClick = { ragViewMode = 0 },
                        label = { Text("⚡ स्थानीय एम्बेडिंग RAG खोज (Local Vector Search)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = ragViewMode == 1,
                        onClick = { ragViewMode = 1 },
                        label = { Text("📖 पाठ्यक्रम तालिका (${searchedCurriculum.size})", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (ragViewMode == 0) {
                    // --- MODE 0: Local Semantic Embedding & Hybrid RAG Search ---
                    OutlinedTextField(
                        value = ragQueryInput,
                        onValueChange = { viewModel.setRagQueryInput(it) },
                        placeholder = { Text("शिक्षक का प्रश्न/पाठ (उदा. साल का पेड़, संख्या, पानी, सरहुल)...") },
                        leadingIcon = { Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            if (ragQueryInput.isNotBlank()) {
                                IconButton(onClick = { viewModel.setRagQueryInput("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("local_rag_embedding_search_input"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Query Suggestion Chips
                    Text(
                        text = "त्वरित RAG क्वेरी परीक्षण (Quick Grounding Tests):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "साल का पेड़ (Sarjom)" to "SANTHALI",
                            "संख्या व गिनती (1-10)" to "SANTHALI",
                            "पानी और नदियां (Daq)" to "HO",
                            "परिवार व घर (Hatu)" to "MUNDARI"
                        ).forEach { (label, lang) ->
                            SuggestionChip(
                                onClick = {
                                    viewModel.setRagQueryInput(label)
                                    viewModel.setRagLanguage(lang)
                                },
                                label = { Text(label, fontSize = 10.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Target Language Filter Chips for RAG
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedRagLanguage == null,
                            onClick = { viewModel.setRagLanguage(null) },
                            label = { Text("सभी भाषाएं", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedRagLanguage == "SANTHALI",
                            onClick = { viewModel.setRagLanguage(if (selectedRagLanguage == "SANTHALI") null else "SANTHALI") },
                            label = { Text("संथाली (Ol Chiki)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedRagLanguage == "HO",
                            onClick = { viewModel.setRagLanguage(if (selectedRagLanguage == "HO") null else "HO") },
                            label = { Text("हो (Ho)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedRagLanguage == "MUNDARI",
                            onClick = { viewModel.setRagLanguage(if (selectedRagLanguage == "MUNDARI") null else "MUNDARI") },
                            label = { Text("मुण्डारी", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Real-time On-Device RAG Telemetry Banner
                    GlassmorphicCard(
                        shape = RoundedCornerShape(14.dp),
                        containerColor = glass.surface,
                        elevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Column {
                                    Text(
                                        text = "स्थानीय एम्बेडिंग इंजन (64-D Vector Space)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Dense Cosine Sim + BM25 RRF • 100% Offline Room DB",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.dp, glass.borderLight)
                            ) {
                                Text(
                                    text = "${ragQueryContext.retrievalLatencyMs} ms लेटेंसी",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // RAG Results Stream
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(ragQueryContext.topMatches) { match ->
                            RagCurriculumMatchCard(
                                match = match,
                                onLoadToStudio = { chosenMatch ->
                                    viewModel.loadRagMatchIntoLessonStudio(chosenMatch)
                                }
                            )
                        }

                        if (ragQueryContext.topMatches.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                                        Text("कोई RAG मिलान नहीं मिला। कृपया अन्य शब्द खोजें।", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // --- MODE 1: Full JCERT Schema Browser ---
                    OutlinedTextField(
                        value = curriculumQuery,
                        onValueChange = { viewModel.setCurriculumSearch(it) },
                        placeholder = { Text("पाठ्यक्रम तालिका खोजें (उदा. साल, ᱫᱟᱨᱮ, गिनती, पानी, FLN)...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("curriculum_rag_search_input"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Language Filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCurriculumLanguageFilter == null,
                            onClick = { viewModel.setCurriculumLanguageFilter(null) },
                            label = { Text("सभी (${searchedCurriculum.size})", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedCurriculumLanguageFilter == "SANTHALI",
                            onClick = { viewModel.setCurriculumLanguageFilter(if (selectedCurriculumLanguageFilter == "SANTHALI") null else "SANTHALI") },
                            label = { Text("संथाली", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedCurriculumLanguageFilter == "HO",
                            onClick = { viewModel.setCurriculumLanguageFilter(if (selectedCurriculumLanguageFilter == "HO") null else "HO") },
                            label = { Text("हो", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedCurriculumLanguageFilter == "MUNDARI",
                            onClick = { viewModel.setCurriculumLanguageFilter(if (selectedCurriculumLanguageFilter == "MUNDARI") null else "MUNDARI") },
                            label = { Text("मुण्डारी", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(searchedCurriculum) { chunk ->
                            CurriculumChunkCard(chunk = chunk)
                        }
                    }
                }
            }

            2 -> {
                // --- SUB-TAB 2: Durable Outbox & Offline Sync Manager ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        GlassmorphicCard(
                            shape = RoundedCornerShape(24.dp),
                            containerColor = glass.surface,
                            elevation = 3.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SectionHeader(
                                    title = "ऑफलाइन-प्रथम आउटबॉक्स प्रबंधक",
                                    subtitle = "Room SQLite System of Record • Idempotent Sequence Queue",
                                    icon = Icons.Default.CloudSync
                                )

                                // Offline Simulation Toggle
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isOfflineSimulated) WarningAmber.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(
                                            text = if (isOfflineSimulated) "⚠️ ऑफलाइन मोड सक्रिय (Simulated)" else "🌐 ऑनलाइन नेटवर्क कनेक्टेड",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isOfflineSimulated) WarningAmber else MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = if (isOfflineSimulated) "सभी लेन-देन स्थानीय Room DB आउटबॉक्स में कतारबद्ध हैं" else "सर्वर से सीधा डेटा सिंक्रनाइज़ेशन चालू",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    Switch(
                                        checked = isOfflineSimulated,
                                        onCheckedChange = { viewModel.toggleOfflineSimulation(it) },
                                        modifier = Modifier.testTag("offline_sim_switch")
                                    )
                                }

                                // Outbox Stats
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        border = BorderStroke(1.dp, glass.borderLight),
                                        modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = "$pendingOutboxCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                            Text(text = "कतारबद्ध क्रियाएं (Outbox)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        border = BorderStroke(1.dp, glass.borderLight),
                                        modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = "100%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                            Text(text = "डेटा अखंडता (Integrity)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                    }
                                }

                                // Sync Message
                                Text(
                                    text = syncMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Trigger Sync Button
                                Button(
                                    onClick = { viewModel.triggerSync() },
                                    enabled = !isSyncing,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("trigger_sync_button")
                                ) {
                                    if (isSyncing) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("सिंक्रनाइज़ हो रहा है...")
                                    } else {
                                        Icon(Icons.Default.Sync, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("अभी सिंक करें (Durable Outbox Flush)")
                                    }
                                }
                            }
                        }
                    }

                    // Sync Logs Timeline
                    item {
                        SectionHeader(
                            title = "सिंक गतिविधि लॉग (Sync Logs)",
                            subtitle = "Idempotent Transaction History",
                            icon = Icons.AutoMirrored.Filled.ReceiptLong
                        )
                    }

                    items(recentSyncLogs) { log ->
                        GlassmorphicCard(
                            shape = RoundedCornerShape(14.dp),
                            containerColor = glass.surface,
                            elevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = log.status,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (log.status.contains("SUCCESS")) SuccessGreen else WarningAmber
                                    )
                                    Text(
                                        text = log.details,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${log.networkLatencyMs}ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            3 -> {
                // --- SUB-TAB 3: Full-Stack Tech Stack & Workflow Architecture ---
                FullStackArchitectureView(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun GlossaryItemCard(
    item: GlossaryEntity,
    onSpeakText: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    GlassmorphicCard(
        shape = RoundedCornerShape(18.dp),
        containerColor = glass.surface,
        elevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = item.hindiWord,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { onSpeakText("${item.hindiWord}. ${item.santhaliWord}") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak Pronunciation",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = item.englishMeaning,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = glass.surfaceUltraLight,
                border = BorderStroke(1.dp, glass.borderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "• संथाली (Santhali): ${item.santhaliWord} [${item.santhaliOlChiki}]",
                        style = MaterialTheme.typography.bodySmall,
                        color = SanthaliAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• हो (Ho): ${item.hoWord} / ${item.hoDevanagari}",
                        style = MaterialTheme.typography.bodySmall,
                        color = HoAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• मुण्डारी (Mundari): ${item.mundariWord}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MundariAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (item.exampleSentenceTarget.isNotBlank()) {
                Text(
                    text = "वाक्य उदाहरण: ${item.exampleSentenceTarget}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun CurriculumChunkCard(chunk: CurriculumContentEntity, modifier: Modifier = Modifier) {
    val glass = LocalGlassColors.current
    val langColor = when (chunk.tribalLanguage) {
        "SANTHALI" -> SanthaliAccent
        "HO" -> HoAccent
        else -> MundariAccent
    }

    GlassmorphicCard(
        shape = RoundedCornerShape(20.dp),
        containerColor = glass.surface,
        elevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Tags: Grade, Subject, Board & Language Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        border = BorderStroke(1.dp, glass.borderLight)
                    ) {
                        Text(
                            text = chunk.grade,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, glass.borderLight)
                    ) {
                        Text(
                            text = chunk.subject,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = langColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, langColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "${chunk.tribalLanguage} • ${chunk.tribalScriptType}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = langColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Chapter Title & Topic
            Column {
                Text(
                    text = "अध्याय ${chunk.chapterNumber}: ${chunk.chapterTitle}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "प्रकरण (Topic): ${chunk.topic}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Learning Outcome Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, glass.borderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "प्रतिफल कोड: ${chunk.learningOutcomeCode}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = chunk.learningOutcomeDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Lesson Hindi Text
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = glass.surfaceUltraLight,
                border = BorderStroke(1.dp, glass.borderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "📖 पाठ्यांश (Hindi Curriculum Text):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = chunk.lessonTextHindi,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )
                }
            }

            // Tribal Translation & Native Script Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = langColor.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, langColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🗣️ मातृभाषा अनुवाद (${chunk.tribalLanguage}):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = langColor
                        )
                        Text(
                            text = chunk.dialectOrRegion,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = chunk.tribalLessonText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (chunk.tribalNativeScriptText.isNotBlank()) {
                        Text(
                            text = "मूल लिपि: ${chunk.tribalNativeScriptText}",
                            style = MaterialTheme.typography.bodySmall,
                            color = langColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "उच्चारण (Roman): ${chunk.transliterationLatin}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            // Classroom Activity & Assessment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, glass.borderLight),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "🎯 कक्षा गतिविधि",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = chunk.classroomActivityPrompt,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 16.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, glass.borderLight),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "❓ मौखिक मूल्यांकन",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = chunk.oralAssessmentQuestion,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Footer Metadata Badges: Bloom's Level, Cultural Tag, Source Reference
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, glass.borderLight)
                    ) {
                        Text(
                            text = "Bloom's: ${chunk.bloomsTaxonomyLevel}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, glass.borderLight)
                    ) {
                        Text(
                            text = "संस्कृति: ${chunk.culturalContextTag}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = chunk.textbookSourceReference,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RagCurriculumMatchCard(
    match: RagCurriculumMatch,
    onLoadToStudio: (RagCurriculumMatch) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    val chunk = match.chunk
    val langColor = when (chunk.tribalLanguage) {
        "SANTHALI" -> SanthaliAccent
        "HO" -> HoAccent
        else -> MundariAccent
    }
    val scorePercentage = (match.similarityScore * 100).toInt()

    GlassmorphicCard(
        shape = RoundedCornerShape(20.dp),
        containerColor = glass.surface,
        elevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Similarity Score Gauge + Match Type Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Match Type Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(match.matchType.badgeColorHex).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(match.matchType.badgeColorHex).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(match.matchType.badgeColorHex),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = match.matchType.labelHindi,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(match.matchType.badgeColorHex)
                        )
                    }
                }

                // Relevance Percentage Badge
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, glass.borderLight)
                ) {
                    Text(
                        text = "$scorePercentage% RAG प्रासंगिकता",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Embedding vs Lexical Score Breakdown Bar
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = glass.surfaceUltraLight,
                border = BorderStroke(1.dp, glass.borderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📐 Cosine Sim: ${(match.denseCosineScore * 100).toInt()}%  |  🔤 BM25: ${(match.bm25LexicalScore * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = match.relevanceExplanation,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Matched Keywords Chips (if any)
            if (match.matchedKeywords.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "मिलान कुंजी-शब्द:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    match.matchedKeywords.take(4).forEach { kw ->
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, glass.borderLight)
                        ) {
                            Text(
                                text = kw,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = glass.borderLight)

            // Curriculum Chapter & Topic Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "अध्याय ${chunk.chapterNumber}: ${chunk.chapterTitle}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "प्रकरण: ${chunk.topic} (${chunk.grade} • ${chunk.subject})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = langColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, langColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "${chunk.tribalLanguage} • ${chunk.tribalScriptType}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = langColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Learning Outcome Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, glass.borderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text(
                        text = "${chunk.learningOutcomeCode}: ${chunk.learningOutcomeDescription}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Bilingual Grounded Content (Hindi + Tribal)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = glass.surfaceUltraLight,
                border = BorderStroke(1.dp, glass.borderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "📖 पाठ्यांश (Hindi):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = chunk.lessonTextHindi,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = langColor.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, langColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "🗣️ मातृभाषा (${chunk.tribalLanguage}):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = langColor
                    )
                    Text(
                        text = chunk.tribalLessonText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (chunk.tribalNativeScriptText.isNotBlank()) {
                        Text(
                            text = "मूल लिपि: ${chunk.tribalNativeScriptText}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = langColor
                        )
                    }
                    Text(
                        text = "उच्चारण: ${chunk.transliterationLatin}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            // Load into Lesson Studio Action Button
            Button(
                onClick = { onLoadToStudio(match) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("load_rag_to_studio_button_${chunk.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("यह RAG संदर्भ पाठ स्टूडियो में लोड करें", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


