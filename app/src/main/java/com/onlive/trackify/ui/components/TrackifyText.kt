package com.onlive.trackify.ui.components

import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

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
    val maxFontSize: TextUnit = if (style.fontSize.isSpecified) style.fontSize else 24.sp
    val minFontSize: TextUnit = maxFontSize * minFontSizeMult.coerceIn(0.1f, 1f)

    Text(
        text = text,
        modifier = modifier,
        style = style.copy(
            fontWeight = fontWeight ?: style.fontWeight
        ),
        color = color,
        maxLines = maxLines,
        softWrap = maxLines > 1,
        overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(
            minFontSize = minFontSize,
            maxFontSize = maxFontSize,
            stepSize = 1.sp
        )
    )
}
