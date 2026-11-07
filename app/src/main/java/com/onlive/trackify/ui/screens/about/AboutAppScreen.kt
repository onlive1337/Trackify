package com.onlive.trackify.ui.screens.about

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.onlive.trackify.BuildConfig
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.ui.components.segmentedListItemShapes
import com.onlive.trackify.utils.CrashLogger
import com.onlive.trackify.utils.LocalLocalizedContext
import com.onlive.trackify.utils.stringResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(
    onNavigateBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val localizedContext = LocalLocalizedContext.current

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.about_app),
                showBackButton = true,
                onBackClick = onNavigateBack,
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMediumEmphasized,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "${stringResource(R.string.app_version)} ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Text(
                    text = stringResource(R.string.app_description),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.links),
                style = MaterialTheme.typography.titleMediumEmphasized,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 4.dp, bottom = 12.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                AboutLinkItem(
                    icon = Icons.AutoMirrored.Rounded.Chat,
                    label = stringResource(R.string.telegram),
                    index = 0,
                    count = 4,
                    onClick = { runCatching { uriHandler.openUri("https://t.me/onswix") } }
                )

                AboutLinkItem(
                    icon = Icons.Rounded.Code,
                    label = stringResource(R.string.github),
                    index = 1,
                    count = 4,
                    onClick = { runCatching { uriHandler.openUri("https://github.com/onlive1337/Trackify") } }
                )

                AboutLinkItem(
                    icon = Icons.Rounded.Security,
                    label = stringResource(R.string.privacy_policy),
                    index = 2,
                    count = 4,
                    onClick = { runCatching { uriHandler.openUri("https://github.com/onlive1337/Trackify/blob/master/PRIVACY_POLICY.md") } }
                )

                AboutLinkItem(
                    icon = Icons.Rounded.BugReport,
                    label = stringResource(R.string.send_log),
                    index = 3,
                    count = 4,
                    onClick = { shareLatestLog(context, localizedContext) }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.made_by_footer),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

private fun shareLatestLog(context: Context, localizedContext: Context) {
    val log = CrashLogger.getLatestLog(context)
    if (log == null || !log.exists()) {
        Toast.makeText(context, localizedContext.getString(R.string.send_log_no_logs), Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", log)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, localizedContext.getString(R.string.send_log_subject))
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(sendIntent, localizedContext.getString(R.string.send_log_chooser))
        )
    } catch (_: Exception) {
        Toast.makeText(context, localizedContext.getString(R.string.send_log_error), Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AboutLinkItem(
    icon: ImageVector,
    label: String,
    index: Int,
    count: Int,
    onClick: () -> Unit
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = segmentedListItemShapes(index = index, count = count),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            leadingContentColor = MaterialTheme.colorScheme.primary
        ),
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmallEmphasized
        )
    }
}
