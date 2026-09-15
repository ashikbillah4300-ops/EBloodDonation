package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// CompositionLocal to track if dark mode is active
val LocalIsDarkMode = staticCompositionLocalOf { false }

// Primary Crimson Red Accents
val CrimsonPrimary = Color(0xFFD32F2F)
val CrimsonPrimaryDark = Color(0xFFB71C1C)
val CrimsonPrimaryLight = Color(0xFFEF5350)

val CrimsonContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkMode.current) Color(0xFF3B1219) else Color(0xFFFDE8EA)

val OnCrimsonContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkMode.current) Color(0xFFFECDD3) else Color(0xFFD32F2F)

// Dynamic Background
val DarkBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkMode.current) Color(0xFF0F172A) else Color(0xFFFAF9F9)

// Dynamic Surface
val DarkSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkMode.current) Color(0xFF1E293B) else Color(0xFFFFFFFF)

// Dynamic Surface Card
val DarkSurfaceCard: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkMode.current) Color(0xFF1E293B) else Color(0xFFFFFFFF)

// Dynamic Surface Elevated
val DarkSurfaceElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkMode.current) Color(0xFF334155) else Color(0xFFFFF7F7)

// Dynamic Surface Border
val DarkSurfaceBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkMode.current) Color(0xFF334155) else Color(0xFFF0DFE2)

// Dynamic Text Colors
val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkMode.current) Color(0xFFF8FAFC) else Color(0xFF1F2937)

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkMode.current) Color(0xFF94A3B8) else Color(0xFF6B7280)

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkMode.current) Color(0xFF64748B) else Color(0xFF9CA3AF)

// Accent Colors
val SuccessGreen = Color(0xFF16A34A)
val SuccessGreenContainer = Color(0xFFDCFCE7)
val WarningAmber = Color(0xFFD97706)
val InfoBlue = Color(0xFF0284C7)


