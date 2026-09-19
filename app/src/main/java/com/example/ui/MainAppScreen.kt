package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppUserMode
import com.example.domain.model.TargetLanguage
import com.example.ui.components.BhashaSetuBackground
import com.example.ui.components.UserProfileSheet
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

sealed class NavigationTab(
    val route: String,
    val title: String,
    val shortLabel: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object CurriculumBrowser : NavigationTab("curriculum", "पाठ्यक्रम", "पाठ्यक्रम", Icons.AutoMirrored.Filled.LibraryBooks, Icons.AutoMirrored.Outlined.LibraryBooks)
    object LessonStudio : NavigationTab("studio", "पाठ स्टूडियो", "स्टूडियो", Icons.Filled.School, Icons.Outlined.School)
    object LiveVoice : NavigationTab("voice", "लाइव अनुवाद", "अनुवाद", Icons.Filled.RecordVoiceOver, Icons.Outlined.RecordVoiceOver)
    object Chatbot : NavigationTab("chatbot", "गुरुमित्र AI", "AI चैट", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat)
    object Practice : NavigationTab("practice", "बाल संसार", "अभ्यास", Icons.AutoMirrored.Filled.Assignment, Icons.AutoMirrored.Outlined.Assignment)
    object Multimodal : NavigationTab("media", "मीडिया व Veo", "मीडिया", Icons.Filled.AutoAwesomeMotion, Icons.Outlined.AutoAwesomeMotion)
    object GlossarySync : NavigationTab("glossary", "शब्दकोश व सिंक", "सिंक व RAG", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf<NavigationTab>(NavigationTab.LiveVoice) }
    var previousTabIndex by remember { mutableIntStateOf(0) }
    var isMoreToolsMenuOpen by remember { mutableStateOf(false) }

    val pendingOutboxCount by viewModel.pendingOutboxCount.collectAsState()
    val isOfflineSimulated by viewModel.isOfflineSimulated.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val appUserMode by viewModel.appUserMode.collectAsState()
    val isAuthSheetOpen by viewModel.isAuthSheetOpen.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current

    val glass = LocalGlassColors.current

    // Adaptive Bottom Navigation Pillars based on Active User Mode
    val primaryTabs = when (appUserMode) {
        AppUserMode.TEACHER -> listOf(
            NavigationTab.LiveVoice,
            NavigationTab.LessonStudio,
            NavigationTab.CurriculumBrowser,
            NavigationTab.Chatbot,
            NavigationTab.Practice
        )
        AppUserMode.STUDENT -> listOf(
            NavigationTab.Practice,
            NavigationTab.LiveVoice,
            NavigationTab.Multimodal,
            NavigationTab.CurriculumBrowser
        )
        AppUserMode.COMMUNITY -> listOf(
            NavigationTab.CurriculumBrowser,
            NavigationTab.LiveVoice,
            NavigationTab.GlossarySync,
            NavigationTab.Chatbot
        )
    }

    val allTabs = listOf(
        NavigationTab.CurriculumBrowser,
        NavigationTab.LessonStudio,
        NavigationTab.LiveVoice,
        NavigationTab.Chatbot,
        NavigationTab.Practice,
        NavigationTab.Multimodal,
        NavigationTab.GlossarySync
    )

    fun tabIndex(tab: NavigationTab): Int = allTabs.indexOf(tab).coerceAtLeast(0)

    if (isAuthSheetOpen) {
        UserProfileSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.openAuthSheet(false) }
        )
    }

    // Entrance fade-in animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    // Synchronize active navigation tab when user persona changes
    LaunchedEffect(appUserMode) {
        if (currentTab !in primaryTabs) {
            currentTab = when (appUserMode) {
                AppUserMode.STUDENT -> NavigationTab.Practice
                AppUserMode.TEACHER -> NavigationTab.LiveVoice
                AppUserMode.COMMUNITY -> NavigationTab.CurriculumBrowser
            }
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(400)),
    ) {
        BhashaSetuBackground(modifier = modifier) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    Surface(
                        color = glass.surfaceFloating,
                        border = BorderStroke(1.dp, glass.borderLight),
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TopAppBar(
                            title = {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Hub Brand Icon (from Stitch Design System)
                                        Icon(
                                            imageVector = Icons.Default.Hub,
                                            contentDescription = "BhashaSetu Hub",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Text(
                                            text = "भाषासेतु AI",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        // 1-Tap Persona Switcher Badge
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = when (appUserMode) {
                                                AppUserMode.TEACHER -> MaterialTheme.colorScheme.primaryContainer
                                                AppUserMode.STUDENT -> WarningAmber.copy(alpha = 0.18f)
                                                AppUserMode.COMMUNITY -> SuccessGreen.copy(alpha = 0.18f)
                                            },
                                            border = BorderStroke(
                                                1.dp,
                                                when (appUserMode) {
                                                    AppUserMode.TEACHER -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                                    AppUserMode.STUDENT -> WarningAmber.copy(alpha = 0.6f)
                                                    AppUserMode.COMMUNITY -> SuccessGreen.copy(alpha = 0.6f)
                                                }
                                            ),
                                            modifier = Modifier
                                                .heightIn(min = 36.dp)
                                                .minimumInteractiveComponentSize()
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable {
                                                    viewModel.toggleAppUserMode()
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                                .testTag("app_mode_toggle_button")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = when (appUserMode) {
                                                        AppUserMode.TEACHER -> Icons.Default.School
                                                        AppUserMode.STUDENT -> Icons.Default.Face
                                                        AppUserMode.COMMUNITY -> Icons.Default.Groups
                                                    },
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = appUserMode.shortLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.SwapHoriz,
                                                    contentDescription = "Switch Persona",
                                                    modifier = Modifier.size(12.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = when (appUserMode) {
                                            AppUserMode.TEACHER -> "शिक्षक मंच • NIPUN FLN & लाइव रिले"
                                            AppUserMode.STUDENT -> "बाल संसार • खेल-पहेली व सचित्र शब्द कार्ड"
                                            AppUserMode.COMMUNITY -> "सांस्कृतिक मंच • संथाली, हो, मुण्डारी"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            actions = {
                                // 1-Tap Tribal Language Cycle Pill (Ol Chiki, Warang Chiti, Devanagari)
                                val nativeScriptName = when (selectedLanguage) {
                                    TargetLanguage.SANTHALI -> "ᱥᱟᱱᱛᱟᱲᱤ"
                                    TargetLanguage.HO -> "𑢹𑣉𑣉"
                                    TargetLanguage.MUNDARI -> "मुण्डारी"
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .heightIn(min = 36.dp)
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            val nextLang = when (selectedLanguage) {
                                                TargetLanguage.SANTHALI -> TargetLanguage.HO
                                                TargetLanguage.HO -> TargetLanguage.MUNDARI
                                                TargetLanguage.MUNDARI -> TargetLanguage.SANTHALI
                                            }
                                            viewModel.setLanguage(nextLang)
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = nativeScriptName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Outbox / Offline Status Pill
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isOfflineSimulated) WarningAmber.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                    border = BorderStroke(1.dp, if (isOfflineSimulated) WarningAmber.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .heightIn(min = 36.dp)
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.toggleOfflineSimulation(!isOfflineSimulated)
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        .semantics { contentDescription = if (isOfflineSimulated) "Offline mode active, tap to connect" else "Online synced, tap to test offline" }
                                        .testTag("offline_status_toggle_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isOfflineSimulated) Icons.Default.CloudOff else Icons.Default.CloudDone,
                                            contentDescription = null,
                                            tint = if (isOfflineSimulated) WarningAmber else MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = if (pendingOutboxCount > 0) "$pendingOutboxCount" else "Room",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOfflineSimulated) WarningAmber else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(2.dp))

                                // More Tools Menu Button (Quick Launcher for all 7 modules)
                                Box {
                                    IconButton(
                                        onClick = { isMoreToolsMenuOpen = true },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Apps,
                                            contentDescription = "सभी मॉड्यूल (All Modules)",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = isMoreToolsMenuOpen,
                                        onDismissRequest = { isMoreToolsMenuOpen = false }
                                    ) {
                                        Text(
                                            text = "सभी मॉड्यूल (Modules)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                        HorizontalDivider()
                                        allTabs.forEach { tab ->
                                            val isSelected = currentTab == tab
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = tab.title,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = tab.selectedIcon,
                                                        contentDescription = tab.title,
                                                        tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                },
                                                onClick = {
                                                    previousTabIndex = tabIndex(currentTab)
                                                    currentTab = tab
                                                    isMoreToolsMenuOpen = false
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                            )
                                        }
                                    }
                                }

                                // User Profile / Auth Avatar Button
                                IconButton(
                                    onClick = { viewModel.openAuthSheet(true) },
                                    modifier = Modifier.size(36.dp).testTag("user_profile_avatar_button")
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        border = BorderStroke(1.dp, glass.borderLight),
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(appUserMode.iconEmoji, fontSize = 16.sp)
                                        }
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                },
                bottomBar = {
                    Surface(
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        color = glass.surfaceFloating,
                        tonalElevation = 4.dp,
                        shadowElevation = 8.dp,
                        border = BorderStroke(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(
                                    glass.borderHighlight,
                                    glass.borderLight
                                )
                            )
                        ),
                        modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            primaryTabs.forEach { tab ->
                                val isSelected = currentTab == tab
                                val labelText = when (tab) {
                                    NavigationTab.Practice -> if (appUserMode == AppUserMode.STUDENT) "बाल संसार" else "छात्र रिपोर्ट"
                                    else -> tab.shortLabel
                                }
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        previousTabIndex = tabIndex(currentTab)
                                        currentTab = tab
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = labelText,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.secondary,
                                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier
                                        .testTag("nav_tab_${tab.route}")
                                        .semantics { contentDescription = tab.title }
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    val currentIndex = tabIndex(currentTab)
                    val isForward = currentIndex >= previousTabIndex

                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            val enterOffset = if (isForward) 30 else -30
                            val exitOffset = if (isForward) -30 else 30

                            (slideInHorizontally(
                                initialOffsetX = { enterOffset },
                                animationSpec = tween(280)
                            ) + fadeIn(animationSpec = tween(280))) togetherWith
                                    (slideOutHorizontally(
                                        targetOffsetX = { exitOffset },
                                        animationSpec = tween(280)
                                    ) + fadeOut(animationSpec = tween(200)))
                        },
                        label = "TabTransition"
                    ) { tab ->
                        when (tab) {
                            NavigationTab.CurriculumBrowser -> CurriculumBrowserScreen(
                                viewModel = viewModel,
                                onNavigateToStudio = {
                                    previousTabIndex = tabIndex(currentTab)
                                    currentTab = NavigationTab.LessonStudio
                                }
                            )
                            NavigationTab.LessonStudio -> LessonStudioScreen(viewModel = viewModel)
                            NavigationTab.LiveVoice -> VoiceTranslateScreen(viewModel = viewModel)
                            NavigationTab.Chatbot -> GeminiChatbotScreen(viewModel = viewModel)
                            NavigationTab.Multimodal -> MultimodalScreen(viewModel = viewModel)
                            NavigationTab.Practice -> StudentPracticeScreen(viewModel = viewModel)
                            NavigationTab.GlossarySync -> GlossaryAndSyncScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
