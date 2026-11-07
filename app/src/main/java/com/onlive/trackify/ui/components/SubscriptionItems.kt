package com.onlive.trackify.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath
import com.onlive.trackify.R
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.SubscriptionWithCategory
import com.onlive.trackify.utils.CurrencyFormatter
import com.onlive.trackify.utils.DateUtils
import com.onlive.trackify.utils.LocalLocalizedContext
import com.onlive.trackify.utils.PaymentScheduleCalculator
import com.onlive.trackify.utils.SubscriptionDueStatus
import com.onlive.trackify.utils.pluralStringResource
import com.onlive.trackify.utils.stringResource
import java.time.LocalDate

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
    item: SubscriptionWithCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = remember { LocalDate.now() },
    dueStatus: SubscriptionDueStatus = remember(item.subscription, today) {
        PaymentScheduleCalculator.resolveDueStatus(item.subscription, today)
    }
) {
    val subscription = item.subscription
    val categoryColor = resolveCategoryColor(
        item.categoryColor,
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

    val cardBorder = when (dueStatus) {
        is SubscriptionDueStatus.DueToday -> BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
        )
        is SubscriptionDueStatus.DueTomorrow -> BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f)
        )
        else -> null
    }
    val contentAlpha = if (dueStatus is SubscriptionDueStatus.Expired) 0.65f else 1f

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = RoundedCornerShape(28.dp),
        border = cardBorder,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(contentAlpha)
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
                    text = item.categoryName ?: stringResource(R.string.without_category),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                SubscriptionDueIndicator(
                    dueStatus = dueStatus,
                    today = today
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.formatAmount(subscription.price),
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SubscriptionDueIndicator(
    dueStatus: SubscriptionDueStatus,
    today: LocalDate
) {
    val context = LocalLocalizedContext.current
    val currentLocale = context.resources.configuration.locales[0] ?: LocalLocale.current.platformLocale

    when (dueStatus) {
        is SubscriptionDueStatus.DueToday -> {
            DuePillBadge(
                icon = Icons.Rounded.NotificationsActive,
                text = stringResource(R.string.payment_due_today),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        is SubscriptionDueStatus.DueTomorrow -> {
            DuePillBadge(
                icon = Icons.Rounded.NotificationsActive,
                text = stringResource(R.string.payment_due_tomorrow),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }

        is SubscriptionDueStatus.DueSoon -> {
            DuePillBadge(
                icon = Icons.Rounded.Schedule,
                text = pluralStringResource(
                    R.plurals.payment_due_in_days,
                    dueStatus.daysUntil,
                    dueStatus.daysUntil
                ),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        is SubscriptionDueStatus.Upcoming -> {
            val formattedDate = DateUtils.formatShortDate(dueStatus.date, today, currentLocale)
            val label = if (dueStatus.daysUntil <= 7) {
                pluralStringResource(
                    R.plurals.payment_due_in_days,
                    dueStatus.daysUntil,
                    dueStatus.daysUntil
                )
            } else {
                stringResource(R.string.next_payment_on, formattedDate)
            }
            InlineScheduleRow(
                icon = Icons.Rounded.Event,
                text = label
            )
        }

        is SubscriptionDueStatus.EndingWithoutRenewal -> {
            val formattedEnd = DateUtils.formatShortDate(dueStatus.endDate, today, currentLocale)
            InlineScheduleRow(
                icon = Icons.Rounded.EventBusy,
                text = stringResource(R.string.subscription_ends_on, formattedEnd)
            )
        }

        is SubscriptionDueStatus.Expired -> {
            DuePillBadge(
                icon = Icons.Rounded.EventBusy,
                text = stringResource(R.string.subscription_expired),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DuePillBadge(
    icon: ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMediumEmphasized,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InlineScheduleRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
