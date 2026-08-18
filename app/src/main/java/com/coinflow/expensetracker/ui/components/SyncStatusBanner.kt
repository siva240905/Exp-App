package com.coinflow.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coinflow.expensetracker.data.model.SyncState
import com.coinflow.expensetracker.ui.theme.ErrorRed
import com.coinflow.expensetracker.ui.theme.PrimaryCyanBright
import com.coinflow.expensetracker.ui.theme.SuccessGreen
import com.coinflow.expensetracker.ui.theme.SurfaceContainer
import com.coinflow.expensetracker.ui.theme.TextVariant

@Composable
fun SyncStatusBanner(
    syncState: SyncState,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (syncState) {
        is SyncState.Synced -> SuccessGreen.copy(alpha = 0.12f)
        is SyncState.Syncing -> PrimaryCyanBright.copy(alpha = 0.12f)
        is SyncState.Failed -> ErrorRed.copy(alpha = 0.12f)
        is SyncState.Idle -> SurfaceContainer
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            when (syncState) {
                is SyncState.Synced -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Synced",
                        tint = SuccessGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "🟢 Synced (${syncState.lastSyncTime.ifBlank { "Just now" }})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        color = SuccessGreen
                    )
                }
                is SyncState.Syncing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = PrimaryCyanBright,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "🔄 Syncing with GitHub Gist...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        color = PrimaryCyanBright
                    )
                }
                is SyncState.Failed -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Sync Failed",
                        tint = ErrorRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "🔴 Sync Failed: ${syncState.error}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        color = ErrorRed,
                        maxLines = 1
                    )
                }
                is SyncState.Idle -> {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Ready to sync",
                        tint = TextVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Ready to Sync",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextVariant
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onRefreshClick() }
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
