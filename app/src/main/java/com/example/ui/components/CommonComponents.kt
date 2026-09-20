package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GradeLevel
import com.example.domain.model.SubjectArea
import com.example.domain.model.TargetLanguage
import com.example.ui.theme.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics

/**
 * Modern BhashaSetu Collective Atmospheric Canvas Background.
 * Provides subtle, clean optical backdrop refraction with Educational Teal and Slate tones.
 */
@Composable
fun BhashaSetuBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val topColor = if (isDark) BhashaBackgroundDark else BhashaAtmosphereTop
    val midColor = if (isDark) BhashaSurfaceDark else BhashaAtmosphereMid
    val botColor = if (isDark) BhashaBackgroundDark else BhashaAtmosphereBottom
    val glowTeal = if (isDark) Color(0x182DD4BF) else BhashaAtmosphereTeal
    val glowNavy = if (isDark) Color(0x221E293B) else BhashaAtmosphereNavy

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // Base vertical canvas gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            topColor,
                            midColor,
                            botColor
                        )
                    )
                )
                // Top-right subtle Educational Teal atmospheric glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowTeal, Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.12f),
                        radius = size.width * 0.75f
                    ),
                    radius = size.width * 0.75f,
                    center = Offset(size.width * 0.9f, size.height * 0.12f)
                )
                // Bottom-left subtle Slate Navy atmospheric glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowNavy, Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.88f),
                        radius = size.width * 0.85f
                    ),
                    radius = size.width * 0.85f,
                    center = Offset(size.width * 0.1f, size.height * 0.88f)
                )
            },
        content = content
    )
}


/**
 * Reusable Glassmorphism Card Container.
 * Features clean institutional translucency, dual-layer light-gradient hairline border, and gentle depth elevation.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    containerColor: Color = LocalGlassColors.current.surface,
    borderBrush: Brush = Brush.linearGradient(
        colors = listOf(
            LocalGlassColors.current.borderHighlight,
            LocalGlassColors.current.borderLight,
            LocalGlassColors.current.borderHighlight.copy(alpha = 0.3f)
        )
    ),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 2.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = shape,
        color = containerColor,
        tonalElevation = elevation,
        shadowElevation = elevation,
        border = BorderStroke(borderWidth, borderBrush),
        modifier = modifier.clip(shape)
    ) {
        Column(content = content)
    }
}

/**
 * Reusable Glassmorphic Surface for rows, bars, and compact containers.
 */
@Composable
fun GlassmorphicSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    containerColor: Color = LocalGlassColors.current.surfaceTinted,
    borderColor: Color = LocalGlassColors.current.borderLight,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        shape = shape,
        color = containerColor,
        border = BorderStroke(borderWidth, borderColor),
        modifier = modifier.clip(shape)
    ) {
        Box(content = content)
    }
}


/**
 * Accessible, tactile icon button conforming to UI/UX Pro Max standards.
 * Guarantees minimum 48x48dp interactive touch target bounds, Material 3 ripple,
 * and semantic accessibility content description.
 */
@Composable
fun BhashaIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    size: Dp = 48.dp,
    iconSize: Dp = 22.dp
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .size(size)
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) contentColor else contentColor.copy(alpha = 0.38f),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**
 * Accessible tactile action pill with icon, text, minimum 48dp touch bounds,
 * and smooth interactive state feedback.
 */
@Composable
fun BhashaActionPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isSelected: Boolean = false,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = LocalGlassColors.current.surface,
    borderColor: Color = LocalGlassColors.current.borderLight,
    contentDescription: String? = null
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) selectedColor.copy(alpha = 0.14f) else containerColor,
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) selectedColor else borderColor
        ),
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        modifier = modifier
            .heightIn(min = 44.dp)
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .semantics {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
                this.role = Role.Button
                this.selected = isSelected
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun LanguageSelectorChipRow(
    selectedLanguage: TargetLanguage,
    onLanguageSelected: (TargetLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "अनुवाद की भाषा चुनें (Select Target Language)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = "हिंदी ➔ ${selectedLanguage.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Text(
            text = "कक्षा संवाद की मातृभाषा चुनें (Hindi ➔ Santhali / Ho / Mundari):",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TargetLanguage.entries.forEach { lang ->
                val isSelected = lang == selectedLanguage
                val accentColor = when (lang) {
                    TargetLanguage.SANTHALI -> SanthaliAccent
                    TargetLanguage.HO -> HoAccent
                    TargetLanguage.MUNDARI -> MundariAccent
                }
                val scriptName = when (lang) {
                    TargetLanguage.SANTHALI -> "Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)"
                    TargetLanguage.HO -> "Warang Chiti (𑢹𑣉𑣉)"
                    TargetLanguage.MUNDARI -> "Devanagari (देवनागरी)"
                }
                
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) accentColor.copy(alpha = 0.16f) else glass.surface,
                    border = if (isSelected) {
                        BorderStroke(2.dp, accentColor)
                    } else {
                        BorderStroke(1.dp, glass.borderLight)
                    },
                    shadowElevation = if (isSelected) 3.dp else 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 58.dp)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onLanguageSelected(lang) }
                        .semantics {
                            contentDescription = "${lang.displayName} (${lang.nativeName}), ${if (isSelected) "selected" else "not selected"}"
                            role = Role.RadioButton
                            selected = isSelected
                        }
                        .testTag("lang_chip_${lang.code}")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = accentColor,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(
                                text = lang.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = lang.nativeName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = scriptName.substringBefore(" "),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (isSelected) accentColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GradeSelectorChipRow(
    selectedGrade: GradeLevel,
    onGradeSelected: (GradeLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "कक्षा स्तर (FLN Grade):",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            GradeLevel.entries.take(3).forEach { grade ->
                val isSelected = grade == selectedGrade
                FilterChip(
                    selected = isSelected,
                    onClick = { onGradeSelected(grade) },
                    label = {
                        Text(
                            text = grade.label.substringBefore(" ("),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = glass.surface,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = glass.borderLight,
                        selectedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .minimumInteractiveComponentSize()
                        .semantics {
                            contentDescription = "${grade.label}, ${if (isSelected) "selected" else "not selected"}"
                            role = Role.RadioButton
                            selected = isSelected
                        }
                        .testTag("grade_chip_${grade.name}")
                )
            }
        }
    }
}

@Composable
fun QualityScoreBadge(
    qualityScore: Float,
    groundingScore: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SuccessGreen.copy(alpha = 0.12f))
            .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Verified,
            contentDescription = "Grounded Verified",
            tint = SuccessGreen,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "Quality: ${(qualityScore * 100).toInt()}% • RAG Grounding: ${(groundingScore * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = SuccessGreen
        )
    }
}

@Composable
fun LatencyBadge(
    latencyMs: Long,
    modifier: Modifier = Modifier
) {
    val isBudgetMet = latencyMs <= 3000L
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isBudgetMet) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f) else WarningAmber.copy(alpha = 0.15f))
            .border(
                1.dp,
                if (isBudgetMet) MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f) else WarningAmber.copy(alpha = 0.4f),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "Latency",
            tint = if (isBudgetMet) MaterialTheme.colorScheme.secondary else WarningAmber,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "$latencyMs ms (${if (isBudgetMet) "≤3s Target Met" else "High latency"})",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isBudgetMet) MaterialTheme.colorScheme.secondary else WarningAmber
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (icon != null) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, glass.borderLight),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Stitch Status Dot Badge (from Stitch Design System).
 * Renders a compact badge with a 6dp circular status dot and bold label.
 * Example: ● Ready, ● Room DB रेडी, ● 100% ऑफ़लाइन.
 */
@Composable
fun StitchStatusDotBadge(
    label: String,
    statusColor: Color = BhashaTealSecondary,
    containerColor: Color = statusColor.copy(alpha = 0.12f),
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = statusColor,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Stitch Tag Pill for pedagogical metadata (JCERT LO, Bloom's level, Cultural context).
 */
@Composable
fun StitchTagPill(
    text: String,
    icon: ImageVector? = null,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    borderColor: Color = color.copy(alpha = 0.25f),
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

/**
 * Stitch Metric Card for display strips in hero sections.
 */
@Composable
fun StitchStatMetric(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White.copy(alpha = 0.15f),
    borderColor: Color = Color.White.copy(alpha = 0.25f),
    textColor: Color = Color.White
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = textColor.copy(alpha = 0.85f),
                maxLines = 1
            )
        }
    }
}

