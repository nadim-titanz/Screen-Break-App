package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Nothing-Inspired UI Design System Tokens
// Monochromatic OLED canvas with precision red (#D71921) accents and Swiss typography
// ============================================================================

val NothingBlack = Color(0xFF000000)          // Pure OLED Black
val NothingSurface = Color(0xFF101216)        // Technical Dark Disc/Card Surface
val NothingSurfaceElevated = Color(0xFF161920) // Elevated Surface / Interactive State
val NothingCardContainer = Color(0xFF101216)
val NothingOutline = Color(0xFF222630)        // Hairline Precision Border
val NothingOutlineVariant = Color(0xFF2E3442) // Distinct Border Separator

val NothingWhite = Color(0xFFFFFFFF)          // Display text & hero readouts (100%)
val NothingTextPrimary = Color(0xFFEBEBF0)    // High-contrast body text (90%)
val NothingTextSecondary = Color(0xFF8E8E93)  // Technical labels, metadata (60%)
val NothingTextMuted = Color(0xFF505561)      // Timestamps, hints, inactive (40%)
val NothingTextDisabled = Color(0xFF40444F)   // Disabled interactive elements

// Nothing Signature Accent: Crimson Red (#D71921)
val NothingRed = Color(0xFFD71921)            // Signature Nothing Red / Interrupt
val NothingRedGlow = Color(0xFFFF2E38)        // High-energy luminous dial glow
val NothingRedMuted = Color(0x33D71921)       // Subtle red tinted backing (20% alpha)

// Data status indicators
val NothingGreen = Color(0xFF30D158)          // Data status: active / success
val NothingAmber = Color(0xFFFF9F0A)          // Data status: warning

// Backward-compatible Brick aliases mapped to Nothing Design System
val BrickBackground = NothingBlack
val BrickSurface = NothingSurface
val BrickSurfaceVariant = NothingSurfaceElevated
val BrickCardContainer = NothingCardContainer
val BrickOutline = NothingOutline
val BrickOutlineVariant = NothingOutlineVariant

val BrickPrimary = NothingWhite
val BrickOnPrimary = NothingBlack
val BrickPrimaryContainer = NothingSurfaceElevated
val BrickOnPrimaryContainer = NothingWhite

val BrickBarFill = NothingRed
val BrickBarTrack = NothingOutline

val BrickSecondary = NothingTextSecondary
val BrickOnSecondary = NothingBlack
val BrickSecondaryContainer = NothingSurfaceElevated
val BrickOnSecondaryContainer = NothingTextPrimary

val BrickTertiary = Color(0xFFD0D7DE)
val BrickOnTertiary = Color(0xFF1C1E21)
val BrickTertiaryContainer = Color(0xFF383C42)
val BrickOnTertiaryContainer = Color(0xFFF0F3F6)

val BrickTextPrimary = NothingWhite
val BrickTextSecondary = NothingTextSecondary
val BrickTextMuted = NothingTextMuted
val BrickAccentDot = NothingRed
val BrickAvgBadge = NothingSurfaceElevated
val BrickAvgDashed = NothingOutline
val AppleSystemGreen = NothingGreen
val AppleSystemOrange = NothingAmber
val AppleSystemRed = NothingRed

// Backward compatible aliases
val SophisticatedBg = NothingBlack
val SophisticatedSurface = NothingSurface
val SophisticatedSurfaceVariant = NothingSurfaceElevated
val SophisticatedCardContainer = NothingCardContainer
val SophisticatedOutline = NothingOutline
val SophisticatedOutlineVariant = NothingOutlineVariant

val SophisticatedPrimary = NothingRed
val SophisticatedOnPrimary = NothingWhite
val SophisticatedPrimaryContainer = NothingSurfaceElevated
val SophisticatedOnPrimaryContainer = NothingWhite

val SophisticatedSecondary = NothingTextSecondary
val SophisticatedOnSecondary = NothingBlack
val SophisticatedSecondaryContainer = NothingSurfaceElevated
val SophisticatedOnSecondaryContainer = NothingTextPrimary

val SophisticatedTertiary = BrickTertiary
val SophisticatedOnTertiary = BrickOnTertiary
val SophisticatedTertiaryContainer = BrickTertiaryContainer
val SophisticatedOnTertiaryContainer = BrickOnTertiaryContainer

val SophisticatedTextPrimary = NothingWhite
val SophisticatedTextSecondary = NothingTextSecondary
val SophisticatedTextMuted = NothingTextMuted

val IndigoPrimaryLight = SophisticatedPrimary
val IndigoSecondaryLight = SophisticatedSecondary
val AmberTertiaryLight = SophisticatedTertiary
val IndigoPrimaryDark = SophisticatedPrimary
val IndigoSecondaryDark = SophisticatedSecondary
val AmberTertiaryDark = SophisticatedTertiary
val SlateBackgroundLight = SophisticatedBg
val SlateSurfaceLight = SophisticatedSurface
val SlateSurfaceVariantLight = SophisticatedSurfaceVariant
val SlateBackgroundDark = SophisticatedBg
val SlateSurfaceDark = SophisticatedSurface
val SlateSurfaceVariantDark = SophisticatedSurfaceVariant
