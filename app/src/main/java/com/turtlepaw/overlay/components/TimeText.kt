package com.turtlepaw.overlay.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TimeText(
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    color: Color = Color.White,
    textAlign: TextAlign = TextAlign.Unspecified
) {
    val formatter = DateTimeFormatter.ofPattern("h:mm")
    var currentTime by remember { mutableStateOf(LocalTime.now().format(formatter)) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            currentTime = LocalTime.now().format(formatter)
        }
    }

    Text(
        text = currentTime.toString(),
        modifier = modifier,
        style = style.copy(
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.5f),
                blurRadius = 4f
            )
        ),
        color = color,
        textAlign = textAlign
    )
}