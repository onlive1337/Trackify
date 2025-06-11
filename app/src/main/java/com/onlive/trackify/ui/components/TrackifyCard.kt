package com.onlive.trackify.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TrackifyCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Int = 0,
    useOutline: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation.dp
        ),
        shape = MaterialTheme.shapes.large,
        border = if (useOutline) {
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        } else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (!title.isNullOrEmpty()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            content()
        }
    }
}

@Composable
fun TrackifyOutlinedCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    TrackifyCard(
        modifier = modifier,
        title = title,
        backgroundColor = MaterialTheme.colorScheme.surface,
        elevation = 0,
        useOutline = true,
        content = content
    )
}