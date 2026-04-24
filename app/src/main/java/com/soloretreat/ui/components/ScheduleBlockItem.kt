package com.soloretreat.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soloretreat.data.local.entity.ScheduleBlock
import com.soloretreat.data.model.ActivityType
import com.soloretreat.util.TimeUtils

@Composable
fun ScheduleBlockItem(
    block: ScheduleBlock,
    onRemove: (() -> Unit)? = null,
    isCurrent: Boolean = false,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        isCurrent -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActivityIcon(activityType = block.activityType)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = block.activityType.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${TimeUtils.formatTime(block.startTime)} - ${TimeUtils.formatTime(block.endTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                block.notes?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove block",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityIcon(activityType: ActivityType, modifier: Modifier = Modifier) {
    val icon = when (activityType) {
        ActivityType.SITTING -> "\uD83E\uDD8B"
        ActivityType.WALKING -> "\uD83C\uDF43"
        ActivityType.CHANTING -> "🙏"
        ActivityType.DHAMMA_TALK -> "\uD83D\udcda"
        ActivityType.MEAL -> "\uD83E\udd63"
        ActivityType.REST -> "\uD83C\uDF3F"
        ActivityType.SLEEP -> "\uD83C\uDF19"
    }
    Text(
        text = icon,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
    )
}