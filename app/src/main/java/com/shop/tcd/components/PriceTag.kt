package com.shop.tcd.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen() {
    DrawLine()
}

@Composable
fun DrawLine() {
    val instaColors = listOf(Color.Yellow, Color.Red, Color.Magenta)
    Canvas(modifier = Modifier.size(300.dp)) {
        val height = size.height
        val width = size.width

        drawRoundRect(
            brush = Brush.linearGradient(colors = instaColors),
            cornerRadius = CornerRadius(60f, 60f),
            style = Stroke(width = 15f, cap = StrokeCap.Round)
        )
        drawCircle(
            brush = Brush.linearGradient(colors = instaColors),
            radius = 45f,
            style = Stroke(width = 15f, cap = StrokeCap.Round)
        )
        drawCircle(
            brush = Brush.linearGradient(colors = instaColors),
            radius = 13f,
            center = Offset(this.size.width * .80f, this.size.height * 0.20f),
        )
    }
}

@Composable
fun DrawRect() {
    Canvas(modifier = Modifier.size(300.dp)) {

        val size = Size(
            width = 280.dp.toPx(),
            height = 200.dp.toPx())

        drawRoundRect(
            color = Color.Blue,
            size = size,
            topLeft = Offset(20f, 20f),
            style = Stroke(width = 8.dp.toPx()),
            cornerRadius = CornerRadius(
                x = 30.dp.toPx(),
                y = 30.dp.toPx()
            )
        )
    }
}