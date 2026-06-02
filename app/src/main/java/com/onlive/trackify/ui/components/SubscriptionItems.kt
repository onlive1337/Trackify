package com.onlive.trackify.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material3.*
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.utils.CurrencyFormatter
import com.onlive.trackify.utils.LocalLocalizedContext
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.R
import androidx.core.graphics.toColorInt

internal fun resolveCategoryColor(colorCode: String?, fallback: Color): Color {
    return try {
        colorCode?.let { Color(it.toColorInt()) } ?: fallback
    } catch (_: Exception) {
        fallback
    }
}

private fun contentColorOn(color: Color): Color =
    if (color.luminance() > 0.5f) Color.Black.copy(alpha = 0.82f) else Color.White

private class MorphShape(
    private val morph: Morph,
    private val progress: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = morph.toPath(progress).asComposePath()
        val matrix = Matrix().apply { scale(x = size.width, y = size.height) }
        path.transform(matrix)
        path.translate(size.center - path.getBounds().center)
        return Outline.Generic(path)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CategoryAvatar(
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.Autorenew,
    size: Dp = 48.dp,
    morphProgress: Float = 0f
) {
    val morph = remember { Morph(MaterialShapes.Sunny, MaterialShapes.Cookie9Sided) }
    val avatarShape = remember(morphProgress) { MorphShape(morph, morphProgress) }

    Box(
        modifier = modifier
            .size(size)
            .clip(avatarShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColorOn(color),
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SubscriptionListItem(
    subscription: Subscription,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalLocalizedContext.current
    val categoryColor = resolveCategoryColor(
        subscription.categoryColor,
        MaterialTheme.colorScheme.surfaceVariant
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "SubscriptionCardScale"
    )
    val morphProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "SubscriptionAvatarMorph"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryAvatar(color = categoryColor, morphProgress = morphProgress)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subscription.categoryName ?: stringResource(R.string.without_category),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.formatAmount(context, subscription.price),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = when (subscription.billingFrequency) {
                        BillingFrequency.MONTHLY -> "/${stringResource(R.string.month)}"
                        BillingFrequency.YEARLY -> "/${stringResource(R.string.year)}"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
