package com.dsb.flashy.screen.dashboard.items

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun MainCardHeading(
    text: String,
    fontWeight: FontWeight = FontWeight.Bold,
    fontSize: TextUnit = 20.sp,
    color: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier
    )
}

@Composable
fun SecondMainCardHeading(
    text: String,
    fontWeight: FontWeight = FontWeight.SemiBold,
    fontSize: TextUnit = 12.sp,
    color: Color = Color.Gray,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier
    )
}