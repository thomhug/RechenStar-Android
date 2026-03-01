package ch.rechenstar.app.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

object HapticFeedback {

    private var isEnabled = true

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    private fun vibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun impact(context: Context, durationMs: Long = 20L) {
        if (!isEnabled) return
        val vibrator = vibrator(context)
        if (!vibrator.hasVibrator()) return
        vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun selection(context: Context) {
        if (!isEnabled) return
        val vibrator = vibrator(context)
        if (!vibrator.hasVibrator()) return
        vibrator.vibrate(VibrationEffect.createOneShot(10L, 50))
    }

    fun notification(context: Context, type: NotificationType = NotificationType.SUCCESS) {
        if (!isEnabled) return
        val vibrator = vibrator(context)
        if (!vibrator.hasVibrator()) return
        val effect = when (type) {
            NotificationType.SUCCESS -> VibrationEffect.createOneShot(30L, VibrationEffect.DEFAULT_AMPLITUDE)
            NotificationType.WARNING -> VibrationEffect.createWaveform(longArrayOf(0, 20, 40, 20), -1)
            NotificationType.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 40, 30, 40), -1)
        }
        vibrator.vibrate(effect)
    }

    fun performHapticFeedback(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    enum class NotificationType {
        SUCCESS, WARNING, ERROR
    }
}
