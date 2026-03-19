package dev.anthropic.installreferrerdemo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.anthropic.installreferrerdemo.data.ReferrerInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReferrerInfoCard(info: ReferrerInfo, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Attribution Data",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(label = "Referrer URL", value = info.referrerUrl.ifEmpty { "N/A" })
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            InfoRow(
                label = "Click Timestamp",
                value = formatTimestamp(info.referrerClickTimestamp)
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            InfoRow(
                label = "Install Begin Timestamp",
                value = formatTimestamp(info.installBeginTimestamp)
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            InfoRow(
                label = "Server Click Timestamp",
                value = formatTimestamp(info.referrerClickTimestampServer)
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            InfoRow(
                label = "Server Install Timestamp",
                value = formatTimestamp(info.installBeginTimestampServer)
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            InfoRow(label = "Install Version", value = info.installVersion)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            InfoRow(
                label = "Google Play Instant",
                value = if (info.googlePlayInstantParam) "Yes" else "No"
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1.5f)
        )
    }
}

private fun formatTimestamp(seconds: Long): String {
    if (seconds == 0L) return "N/A"
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date(seconds * 1000))
}
