package com.onlive.trackify.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    maxLines: Int = 1,
    minFontSizeMult: Float = 0.5f,
) {
    var multiplier by remember(text) { mutableFloatStateOf(1f) }

    Text(
        text = text,
        modifier = modifier,
        style = style.copy(
            fontSize = style.fontSize * multiplier,
            fontWeight = fontWeight ?: style.fontWeight
        ),
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Visible,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow && (multiplier > minFontSizeMult)) {
                multiplier *= 0.9f
            }
        }
    )
}
