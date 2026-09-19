package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.seed.PreloadedData
import com.example.domain.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun FullStackArchitectureView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val techTiers by viewModel.techStackTiers.collectAsState()
    val selectedTier by viewModel.selectedTechTier.collectAsState()
    val monorepoNodes by viewModel.monorepoNodes.collectAsState()
    val selectedNode by viewModel.selectedMonorepoNode.collectAsState()
    val diskAnnBenchmarks by viewModel.diskAnnBenchmarks.collectAsState()
    val qeSamples by viewModel.sampleQualityEstimations.collectAsState()
    val activeQe by viewModel.activeQeEvaluation.collectAsState()
    val qeSourceInput by viewModel.qeSourceInput.collectAsState()
    val qeTargetInput by viewModel.qeTargetInput.collectAsState()
    val isEvaluatingQe by viewModel.isEvaluatingQe.collectAsState()
    val offlineTabletState by viewModel.offlineTabletState.collectAsState()

    val glass = LocalGlassColors.current
    var activeArchSection by remember { mutableStateOf(0) }
    val archSections = listOf(
        "🏛️ 5-टियर आर्किटेक्चर",
        "📁 मोनोरेपो व कॉन्ट्रैक्ट्स",
        "⚡ DiskANN बनाम HNSW",
        "🎯 COMET/XCOMET QE",
        "📱 2GB RAM टैबलेट सिंक",
        "📜 SIH ट्रेसिबिलिटी"
    )

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Selector Tabs
        GlassmorphicCard(
            shape = RoundedCornerShape(16.dp),
            containerColor = glass.surfaceFloating,
            elevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            ScrollableTabRow(
                selectedTabIndex = activeArchSection,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 6.dp,
                divider = {}
            ) {
                archSections.forEachIndexed { index, title ->
                    Tab(
                        selected = activeArchSection == index,
                        onClick = { activeArchSection = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (activeArchSection == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        when (activeArchSection) {
            0 -> {
                // --- SECTION 0: 5-Tier Decoupled Polyglot Architecture ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 28.dp)
                ) {
                    item {
                        GlassmorphicCard(
                            shape = RoundedCornerShape(20.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            elevation = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("🚀", fontSize = 24.sp)
                                    Column {
                                        Text(
                                            text = "Decoupled Polyglot Architecture",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Next.js 16.3 + Flutter 3.x + NestJS 11 + FastAPI + PostgreSQL 18 DiskANN",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Architecture Tier Selector Row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            techTiers.forEach { tier ->
                                val isSelected = tier.id == selectedTier.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else glass.surface,
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else glass.borderLight),
                                    onClick = { viewModel.selectTechTier(tier) },
                                    modifier = Modifier.testTag("tech_tier_${tier.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(tier.iconEmoji, fontSize = 16.sp)
                                        Column {
                                            Text(
                                                text = tier.title,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = tier.primaryTech.take(18) + "...",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Selected Tier Detailed Specs Card
                    item {
                        GlassmorphicCard(
                            shape = RoundedCornerShape(18.dp),
                            containerColor = glass.surface,
                            elevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(selectedTier.iconEmoji, fontSize = 26.sp)
                                        Column {
                                            Text(
                                                text = selectedTier.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = selectedTier.subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        border = BorderStroke(1.dp, glass.borderLight)
                                    ) {
                                        Text(
                                            text = selectedTier.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(color = glass.borderLight)

                                // Tech & Version Specs
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = glass.surfaceUltraLight,
                                        border = BorderStroke(1.dp, glass.borderLight),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("कोर फ्रेमवर्क व वर्जन", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(selectedTier.version, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = glass.surfaceUltraLight,
                                        border = BorderStroke(1.dp, glass.borderLight),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("SLA व लेटेंसी", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(selectedTier.slaOrLatency, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }

                                // Key Responsibilities
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("मुख्य उत्तरदायित्व (Core Engine)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    selectedTier.responsibilities.forEach { resp ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("•", color = MaterialTheme.colorScheme.primary)
                                            Text(resp, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }

                                // Protocols & Libraries Chips
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("लाइब्रेरीज व प्रोटोकॉल्स (Libraries & Protocols):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        (selectedTier.keyLibraries + selectedTier.protocols).forEach { lib ->
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                                border = BorderStroke(1.dp, glass.borderLight)
                                            ) {
                                                Text(
                                                    text = lib,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Hosting & Offline Box
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SuccessGreen.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                                        Column {
                                            Text("ऑफलाइन व परिनियोजन (Hosting & Offline)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                            Text("${selectedTier.hardwareOrHosting} • ${selectedTier.offlineCapabilities}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // --- SECTION 1: Monorepo Structure & OpenAPI Contracts ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 28.dp)
                ) {
                    item {
                        Text(
                            text = "📁 Monorepo Workspace Directory Layout (/bhashasetu)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Directory Tree Selector
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            monorepoNodes.forEach { node ->
                                val isSelected = node.path == selectedNode.path
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else glass.surface,
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else glass.borderLight),
                                    onClick = { viewModel.selectMonorepoNode(node) },
                                    modifier = Modifier.fillMaxWidth().testTag("monorepo_node_${node.name}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (node.type == "PACKAGE") Icons.Default.Extension else Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = node.path,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            border = BorderStroke(1.dp, glass.borderLight)
                                        ) {
                                            Text(
                                                text = node.techStack.take(15),
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Code / Contract Snippet Preview
                    item {
                        GlassmorphicCard(
                            shape = RoundedCornerShape(16.dp),
                            containerColor = glass.surface,
                            elevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📄 ${selectedNode.name} Contract & Code Spec",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = selectedNode.techStack,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = selectedNode.purpose,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF1E1E24),
                                    border = BorderStroke(1.dp, Color(0xFF3A3A42)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    SelectionContainer {
                                        Text(
                                            text = selectedNode.sampleCodeSnippet,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFFD4D4D8),
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // --- SECTION 2: Multilingual RAG & DiskANN Benchmarks ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 28.dp)
                ) {
                    item {
                        GlassmorphicCard(
                            shape = RoundedCornerShape(18.dp),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            elevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "⚡ Multilingual RAG: BGE-M3 + StreamingDiskANN",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "PostgreSQL 18 pgvectorscale DiskANN minimizes RAM footprint for 25,000+ curriculum chunks while maintaining sub-4ms search latency.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    items(diskAnnBenchmarks) { bench ->
                        GlassmorphicCard(
                            shape = RoundedCornerShape(14.dp),
                            containerColor = glass.surface,
                            elevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = bench.metricName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("StreamingDiskANN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Text(bench.diskAnnValue, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = glass.surfaceUltraLight,
                                        border = BorderStroke(1.dp, glass.borderLight),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("HNSW Index", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(bench.hnswValue, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SuccessGreen.copy(alpha = 0.12f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "✨ लाभ: ${bench.deltaAdvantage}",
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
            }

            3 -> {
                // --- SECTION 3: Live COMETKiwi & XCOMET Quality Estimation Sandbox ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 28.dp)
                ) {
                    item {
                        Text(
                            text = "🎯 Reference-Free Machine Translation Quality Estimation (QE)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Sample Presets Row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            qeSamples.forEachIndexed { idx, sample ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (sample.cometScore == activeQe.cometScore) MaterialTheme.colorScheme.primaryContainer else glass.surface,
                                    border = BorderStroke(1.dp, glass.borderLight),
                                    onClick = { viewModel.selectQeSample(sample) }
                                ) {
                                    Text(
                                        text = "परीक्षण #${idx + 1} (${sample.confidenceTier.take(4)})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Interactive Inputs Card
                    item {
                        GlassmorphicCard(
                            shape = RoundedCornerShape(16.dp),
                            containerColor = glass.surface,
                            elevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = qeSourceInput,
                                    onValueChange = { viewModel.updateQeSourceInput(it) },
                                    label = { Text("स्रोत वाक्य (Hindi Source Sentence)") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = qeTargetInput,
                                    onValueChange = { viewModel.updateQeTargetInput(it) },
                                    label = { Text("अनुवादित वाक्य (Tribal Target Sentence)") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = { viewModel.evaluateCustomQualityEstimation() },
                                    enabled = !isEvaluatingQe && qeSourceInput.isNotBlank() && qeTargetInput.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth().testTag("evaluate_qe_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isEvaluatingQe) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("COMET मॉडल स्कोरिंग जारी...")
                                    } else {
                                        Icon(Icons.Default.Bolt, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("गुणवत्ता मूल्यांकन रन करें (Run QE)")
                                    }
                                }
                            }
                        }
                    }

                    // Active Evaluation Result Card
                    item {
                        val scoreColor = when {
                            activeQe.cometScore >= 0.85f -> SuccessGreen
                            activeQe.cometScore >= 0.75f -> WarningAmber
                            else -> MaterialTheme.colorScheme.error
                        }

                        GlassmorphicCard(
                            shape = RoundedCornerShape(18.dp),
                            containerColor = glass.surface,
                            elevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📊 QE स्कोर व निर्णय", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = scoreColor.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            text = "COMET: ${(activeQe.cometScore * 100).toInt()}%",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = scoreColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "निर्णय: ${activeQe.actionDecision}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = scoreColor
                                )

                                Text(
                                    text = activeQe.explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (activeQe.detectedErrorSpans.isNotEmpty()) {
                                    HorizontalDivider(color = glass.borderLight)
                                    Text("⚠️ XCOMET MQM त्रुटि विश्लेषण (Error Spans):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    activeQe.detectedErrorSpans.forEach { span ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = WarningAmber.copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.3f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text("त्रुटि खंड: ${span.tokenOrSpan} [${span.severity} • ${span.category}]", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = WarningAmber)
                                                Text("सुझाव: ${span.suggestedFix}", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // --- SECTION 4: Low-Memory 2GB RAM Android Tablet Offline Outbox & Sync Cursor ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 28.dp)
                ) {
                    item {
                        GlassmorphicCard(
                            shape = RoundedCornerShape(18.dp),
                            containerColor = glass.surface,
                            elevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("📱 Rural Tablet Edge Node", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(offlineTabletState.tabletId, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SuccessGreen.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, SuccessGreen)
                                    ) {
                                        Text("RAM: ${offlineTabletState.ramAvailableMb} MB / 2 GB", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SuccessGreen, modifier = Modifier.padding(6.dp))
                                    }
                                }

                                HorizontalDivider(color = glass.borderLight)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = glass.surfaceUltraLight,
                                        border = BorderStroke(1.dp, glass.borderLight),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("सिंक कर्सर स्थिति", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("#${offlineTabletState.syncCursorPosition}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = glass.surfaceUltraLight,
                                        border = BorderStroke(1.dp, glass.borderLight),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("लोकल डेटाबेस इंजन", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("SQLite 3 (Room WAL)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, glass.borderLight),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("🔒 Durable Outbox Continuation Strategy:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        Text("• Local-first writing: All teacher approvals & audio transcriptions write instantly to SQLite.", style = MaterialTheme.typography.bodySmall)
                                        Text("• Sync Cursor: When network drops, sync resumes strictly where it left off with zero data loss.", style = MaterialTheme.typography.bodySmall)
                                        Text("• Exponential Backoff: Failed sync batches retry at 5s, 10s, 20s, 60s intervals.", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            5 -> {
                // --- SECTION 5: SIH Traceability Ledger ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 28.dp)
                ) {
                    item {
                        GlassmorphicCard(
                            shape = RoundedCornerShape(18.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            elevation = 3.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🏛️ Functional Traceability Ledger (FTL)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "SIH26042: PRD → TAD → SAD → FSD → Live Code Verification",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    items(PreloadedData.traceabilityLedger) { trace ->
                        GlassmorphicCard(
                            shape = RoundedCornerShape(16.dp),
                            containerColor = glass.surface,
                            elevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${trace.prdId} ➔ ${trace.tadId}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SuccessGreen.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            text = trace.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = trace.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Module Layer: ${trace.sadId} / ${trace.fsdId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    border = BorderStroke(1.dp, glass.borderLight),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "⚡ Target: ${trace.latencyTarget} • Verified: ${trace.verifiedLayer}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
