package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val ReadexProFontFamily = FontFamily(
    Font(R.font.readex_pro, FontWeight.Normal),
    Font(R.font.readex_pro, FontWeight.Medium),
    Font(R.font.readex_pro, FontWeight.SemiBold),
    Font(R.font.readex_pro, FontWeight.Bold)
)

val XtvTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = ReadexProFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        color = XtvTextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = ReadexProFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        color = XtvTextPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = ReadexProFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = XtvTextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = ReadexProFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 23.sp,
        color = XtvTextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = ReadexProFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        color = XtvTextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = ReadexProFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = XtvTextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = ReadexProFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = XtvTextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = ReadexProFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        color = XtvTextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = ReadexProFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = XtvTextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = ReadexProFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        color = XtvTextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = ReadexProFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        color = XtvTextSecondary
    )
)
