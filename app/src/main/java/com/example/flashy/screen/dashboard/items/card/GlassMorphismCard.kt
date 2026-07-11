package com.dsb.flashy.screen.dashboard.items.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dsb.flashy.ui.theme.GlassBorderBot
import com.dsb.flashy.ui.theme.GlassBorderTop
import com.dsb.flashy.ui.theme.GlassSurface
import com.dsb.flashy.ui.theme.GlassSurfaceMid

@Composable
fun GlassMorphismCard(
    modifier: Modifier = Modifier,
    accentGlow: Color = Color.Transparent,
    cornerRadius: Dp = 20.dp,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (accentGlow != Color.Transparent) {
                    Modifier.drawBehind {
                        drawCircle(
                            color = accentGlow.copy(alpha = 0.18f),
                            radius = size.maxDimension * 0.55f,
                            center = Offset(size.width / 2f, size.height / 2f)
                        )
                    }
                } else Modifier
            )
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(GlassSurfaceMid, GlassSurface),
                    start = Offset(0f, 0f),
                    end = Offset(800f, 800f)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(GlassBorderTop, GlassBorderBot),
                    start = Offset(0f, 0f),
                    end = Offset(800f, 800f)
                ),
                shape = shape
            )
            .drawWithContent {
                drawContent()
                // Inner top-edge highlight — simulates frosted glass light reflection
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x28FFB300),
                            Color(0x18FFFFFF),
                            Color(0x28FFB300),
                            Color.Transparent
                        )
                    ),
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, 1.5.dp.toPx())
                )
            }
            .then(modifier)
    ) {
        content()
    }
}
