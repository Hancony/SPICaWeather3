package me.spica.spicaweather3.ui.alert

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import me.spica.spicaweather3.R
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * 预警等级颜色映射
 *
 * 兼容中英文颜色代码（Blue/蓝色、Yellow/黄色 等），无法识别时回退到 [fallback]。
 */
fun alertLevelColor(colorCode: String, fallback: Color): Color =
  when (colorCode.trim().lowercase()) {
    "blue", "蓝色" -> Color(0xFF3B82F6)
    "yellow", "黄色" -> Color(0xFFF59E0B)
    "orange", "橙色" -> Color(0xFFF97316)
    "red", "红色" -> Color(0xFFEF4444)
    "white", "白色" -> Color(0xFFE5E7EB)
    "black", "黑色" -> Color(0xFF1F2937)
    "green", "绿色" -> Color(0xFF22C55E)
    else -> fallback
  }

@Composable
fun alertSeverityLabel(raw: String): String =
  when (raw.trim().lowercase()) {
    "minor" -> stringResource(R.string.alert_severity_minor)
    "moderate" -> stringResource(R.string.alert_severity_moderate)
    "severe" -> stringResource(R.string.alert_severity_severe)
    "extreme" -> stringResource(R.string.alert_severity_extreme)
    else -> raw.ifBlank { stringResource(R.string.alert_severity_unknown) }
  }

private val shortAlertTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")

private val fullAlertTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

/**
 * 将接口返回的 ISO 时间格式化为短时间格式（MM-dd HH:mm），解析失败时回退为原始字符串
 */
fun formatAlertTimeShort(raw: String): String = formatAlertTime(raw, shortAlertTimeFormatter)

/**
 * 将接口返回的 ISO 时间格式化为完整时间格式（yyyy-MM-dd HH:mm），解析失败时回退为原始字符串
 */
fun formatAlertTimeFull(raw: String): String = formatAlertTime(raw, fullAlertTimeFormatter)

private fun formatAlertTime(raw: String, formatter: DateTimeFormatter): String {
  if (raw.isBlank()) return raw
  return try {
    OffsetDateTime.parse(raw).format(formatter)
  } catch (_: Exception) {
    try {
      LocalDateTime.parse(raw).format(formatter)
    } catch (_: Exception) {
      raw
    }
  }
}
