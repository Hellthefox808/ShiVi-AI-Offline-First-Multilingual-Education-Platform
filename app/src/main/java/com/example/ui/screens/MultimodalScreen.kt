package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.TargetLanguage
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.LanguageSelectorChipRow
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MultimodalScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val imagePrompt by viewModel.imagePrompt.collectAsState()
    val selectedAspectRatio by viewModel.selectedAspectRatio.collectAsState()
    val selectedImageSize by viewModel.selectedImageSize.collectAsState()
    val generatedImageUrl by viewModel.generatedImageUrl.collectAsState()
    val isGeneratingImage by viewModel.isGeneratingImage.collectAsState()

    val veoPrompt by viewModel.veoPrompt.collectAsState()
    val veoAspectRatio by viewModel.veoAspectRatio.collectAsState()
    val veoStatus by viewModel.veoStatus.collectAsState()
    val isGeneratingVeo by viewModel.isGeneratingVeo.collectAsState()

    val imageAnalysisResult by viewModel.imageAnalysisResult.collectAsState()
    val isAnalyzingImage by viewModel.isAnalyzingImage.collectAsState()

    val searchGroundingQuery by viewModel.searchGroundingQuery.collectAsState()
    val searchGroundingResult by viewModel.searchGroundingResult.collectAsState()
    val isSearchingGrounding by viewModel.isSearchingGrounding.collectAsState()

    val aspectRatios = listOf("1:1", "16:9", "9:16", "4:3", "3:4", "3:2", "2:3", "21:9")
    val imageSizes = listOf("1K", "2K", "4K")

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("🖼️ AI दृश्य (Image/Veo)", "🔍 खोज (Search Grounding)", "📷 छवि विश्लेषण (Analysis)")
    val glass = LocalGlassColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = glass.surfaceFloating,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, glass.borderLight), RoundedCornerShape(16.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // --- TAB 1: High-Quality Educational Flashcard Generator & Veo Video ---
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
                                    title = "AI दृश्य व फ्लैशकार्ड जनरेटर",
                                    subtitle = "gemini-3-pro-image-preview • 1K/2K/4K Resolution",
                                    icon = Icons.Default.Palette
                                )

                                OutlinedTextField(
                                    value = imagePrompt,
                                    onValueChange = { viewModel.imagePrompt.value = it },
                                    label = { Text("पाठ्य सामग्री / दृश्य विवरण (Prompt)") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = glass.surfaceUltraLight,
                                        unfocusedContainerColor = glass.surfaceUltraLight,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = glass.borderLight
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("image_prompt_input"),
                                    shape = RoundedCornerShape(16.dp)
                                )

                                // Aspect Ratio Selector (1:1, 16:9, 9:16, 4:3, etc.)
                                Column {
                                    Text(
                                        text = "पहलू अनुपात (Aspect Ratio):",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        items(aspectRatios) { ratio ->
                                            FilterChip(
                                                selected = ratio == selectedAspectRatio,
                                                onClick = { viewModel.selectedAspectRatio.value = ratio },
                                                shape = RoundedCornerShape(14.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    containerColor = glass.surface,
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                                ),
                                                border = FilterChipDefaults.filterChipBorder(
                                                    enabled = true,
                                                    selected = ratio == selectedAspectRatio,
                                                    borderColor = glass.borderLight,
                                                    selectedBorderColor = MaterialTheme.colorScheme.primary
                                                ),
                                                label = { Text(ratio, fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                }

                                // Image Size Selector (1K, 2K, 4K)
                                Column {
                                    Text(
                                        text = "रिज़ॉल्यूशन (Image Size):",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        imageSizes.forEach { size ->
                                            FilterChip(
                                                selected = size == selectedImageSize,
                                                onClick = { viewModel.selectedImageSize.value = size },
                                                shape = RoundedCornerShape(14.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    containerColor = glass.surface,
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                                ),
                                                border = FilterChipDefaults.filterChipBorder(
                                                    enabled = true,
                                                    selected = size == selectedImageSize,
                                                    borderColor = glass.borderLight,
                                                    selectedBorderColor = MaterialTheme.colorScheme.primary
                                                ),
                                                label = { Text(size, fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = { viewModel.generateFlashcardVisual() },
                                    enabled = !isGeneratingImage && imagePrompt.isNotBlank(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("generate_image_button")
                                ) {
                                    if (isGeneratingImage) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("दृश्य सृजित हो रहा है...")
                                    } else {
                                        Icon(Icons.Default.Image, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("फ्लैशकार्ड चित्र बनाएं (Generate Image)")
                                    }
                                }

                                // Display Generated Image
                                if (generatedImageUrl != null) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.fillMaxWidth().height(220.dp)
                                    ) {
                                        AsyncImage(
                                            model = generatedImageUrl,
                                            contentDescription = "Generated Visual",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- Veo 3 Video Generator Card ---
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
                                    title = "Veo 3.1 शैक्षिक वीडियो एनिमेशन",
                                    subtitle = "veo-3.1-fast-generate-preview • 16:9 Landscape / 9:16 Portrait",
                                    icon = Icons.Default.Movie
                                )

                                OutlinedTextField(
                                    value = veoPrompt,
                                    onValueChange = { viewModel.veoPrompt.value = it },
                                    label = { Text("वीडियो अवधारणा विवरण (Video Prompt)") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = glass.surfaceUltraLight,
                                        unfocusedContainerColor = glass.surfaceUltraLight,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = glass.borderLight
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("veo_prompt_input"),
                                    shape = RoundedCornerShape(16.dp)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("पहलू अनुपात:", style = MaterialTheme.typography.labelSmall)
                                    FilterChip(
                                        selected = veoAspectRatio == "16:9",
                                        onClick = { viewModel.veoAspectRatio.value = "16:9" },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = glass.surface,
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.primary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = veoAspectRatio == "16:9",
                                            borderColor = glass.borderLight,
                                            selectedBorderColor = MaterialTheme.colorScheme.primary
                                        ),
                                        label = { Text("16:9 Landscape") }
                                    )
                                    FilterChip(
                                        selected = veoAspectRatio == "9:16",
                                        onClick = { viewModel.veoAspectRatio.value = "9:16" },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = glass.surface,
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.primary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = veoAspectRatio == "9:16",
                                            borderColor = glass.borderLight,
                                            selectedBorderColor = MaterialTheme.colorScheme.primary
                                        ),
                                        label = { Text("9:16 Portrait") }
                                    )
                                }

                                Button(
                                    onClick = { viewModel.generateVeoVideo() },
                                    enabled = !isGeneratingVeo && veoPrompt.isNotBlank(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("generate_veo_button")
                                ) {
                                    if (isGeneratingVeo) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Veo एनिमेशन प्रारंभ हो रहा है...")
                                    } else {
                                        Icon(Icons.Default.VideoCall, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Veo 3 वीडियो जनरेट करें")
                                    }
                                }

                                if (veoStatus != null) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                        border = BorderStroke(1.dp, glass.borderLight),
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = veoStatus!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // --- TAB 2: Search Grounding with Google ---
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
                                    title = "Google सर्च ग्राउंडिंग (Search Grounding)",
                                    subtitle = "gemini-3.5-flash with googleSearch tool",
                                    icon = Icons.Default.Search
                                )

                                LanguageSelectorChipRow(
                                    selectedLanguage = selectedLanguage,
                                    onLanguageSelected = { viewModel.setLanguage(it) }
                                )

                                OutlinedTextField(
                                    value = searchGroundingQuery,
                                    onValueChange = { viewModel.searchGroundingQuery.value = it },
                                    label = { Text("खोज प्रश्न (उदा. झारखंड सरहुल पर्व का इतिहास व तिथि)") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = glass.surfaceUltraLight,
                                        unfocusedContainerColor = glass.surfaceUltraLight,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = glass.borderLight
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("search_grounding_input"),
                                    shape = RoundedCornerShape(16.dp)
                                )

                                Button(
                                    onClick = { viewModel.executeGoogleSearchGrounding() },
                                    enabled = !isSearchingGrounding && searchGroundingQuery.isNotBlank(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("search_grounding_button")
                                ) {
                                    if (isSearchingGrounding) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("गूगल सर्च ग्राउंडिंग हो रहा है...")
                                    } else {
                                        Icon(Icons.AutoMirrored.Filled.ManageSearch, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("सर्च व मातृभाषा अनुवाद प्राप्त करें")
                                    }
                                }

                                if (searchGroundingResult != null) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Default.TravelExplore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                Text(
                                                    text = "सर्च ग्राउंडेड परिणाम (${selectedLanguage.displayName}):",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = searchGroundingResult!!,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // --- TAB 3: Image & Nature Object Analyzer ---
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
                                    title = "छवि व पाठ्यपुस्तक विश्लेषक (Vision AI)",
                                    subtitle = "gemini-3.1-pro-preview • Multimodal Classroom Understanding",
                                    icon = Icons.Default.DocumentScanner
                                )

                                LanguageSelectorChipRow(
                                    selectedLanguage = selectedLanguage,
                                    onLanguageSelected = { viewModel.setLanguage(it) }
                                )

                                // Nature / Book Diagram Canvas for easy interactive testing
                                val sampleBitmap = remember {
                                    val bmp = Bitmap.createBitmap(300, 200, Bitmap.Config.ARGB_8888)
                                    val canvas = Canvas(bmp)
                                    val paint = Paint().apply {
                                        color = android.graphics.Color.rgb(103, 80, 164)
                                        textSize = 28f
                                        isAntiAlias = true
                                    }
                                    canvas.drawColor(android.graphics.Color.rgb(253, 248, 253))
                                    canvas.drawText("साल का पत्ता (Sarjom Sakam)", 30f, 100f, paint)
                                    bmp
                                }

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, glass.borderLight),
                                    modifier = Modifier.fillMaxWidth().height(140.dp)
                                ) {
                                    Image(
                                        bitmap = sampleBitmap.asImageBitmap(),
                                        contentDescription = "Sample Nature Object",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.analyzeImage(
                                            bitmap = sampleBitmap,
                                            prompt = "इस पौधे/पत्ते का नाम बताएं और संथाली, हो व मुण्डारी में इसका उपयोग समझाएं।"
                                        )
                                    },
                                    enabled = !isAnalyzingImage,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("analyze_image_button")
                                ) {
                                    if (isAnalyzingImage) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("छवि का विश्लेषण हो रहा है...")
                                    } else {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("चित्र पहचानें व शब्दावली निकालें")
                                    }
                                }

                                if (imageAnalysisResult != null) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = "🎯 AI विश्लेषण निष्कर्ष:",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = imageAnalysisResult!!,
                                                style = MaterialTheme.typography.bodyMedium
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
    }
}
