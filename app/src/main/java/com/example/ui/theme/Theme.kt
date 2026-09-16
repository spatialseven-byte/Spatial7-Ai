package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
  primary = NexusCyan,
  onPrimary = NexusBackground,
  primaryContainer = NexusSurfaceVariant,
  onPrimaryContainer = NexusCyanLight,
  secondary = NexusAmber,
  onSecondary = NexusBackground,
  secondaryContainer = NexusSurfaceVariant,
  onSecondaryContainer = NexusAmber,
  tertiary = NexusEmerald,
  onTertiary = NexusBackground,
  background = NexusBackground,
  onBackground = TextPrimary,
  surface = NexusSurface,
  onSurface = TextPrimary,
  surfaceVariant = NexusSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = NexusCardBorder,
  error = NexusCoral,
  onError = TextPrimary
)

@Composable
fun GeoNexusTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MetricPill(
  label: String,
  value: String,
  color: Color = NexusCyan,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    color = NexusSurfaceVariant,
    shape = RoundedCornerShape(8.dp),
    border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary,
        maxLines = 1
      )
    }
  }
}


