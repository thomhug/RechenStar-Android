package ch.rechenstar.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object DateUtils {

    private val germanLocale = Locale("de", "CH")

    private val shortDateFormatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.SHORT)
        .withLocale(germanLocale)

    private val mediumDateFormatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(germanLocale)

    private val dayOfWeekFormatter = DateTimeFormatter
        .ofPattern("EE", germanLocale)

    fun formatShort(date: LocalDate): String = date.format(shortDateFormatter)

    fun formatMedium(date: LocalDate): String = date.format(mediumDateFormatter)

    fun dayOfWeekShort(date: LocalDate): String = date.format(dayOfWeekFormatter)

    fun today(): LocalDate = LocalDate.now()

    fun isToday(date: LocalDate): Boolean = date == today()

    fun isYesterday(date: LocalDate): Boolean = date == today().minusDays(1)

    fun toLocalDate(instant: Instant): LocalDate =
        instant.atZone(ZoneId.systemDefault()).toLocalDate()

    fun startOfDay(date: LocalDate): Instant =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant()

    fun endOfDay(date: LocalDate): Instant =
        date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

    fun formatDuration(seconds: Double): String {
        val totalSeconds = seconds.toInt()
        val minutes = totalSeconds / 60
        val secs = totalSeconds % 60
        return if (minutes > 0) "${minutes}m ${secs}s" else "${secs}s"
    }

    fun formatPercentage(value: Double): String =
        "${(value * 100).toInt()}%"
}
