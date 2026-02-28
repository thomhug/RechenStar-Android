package ch.rechenstar.app.domain.service

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object SoundService {

    private const val SAMPLE_RATE = 44100
    private var isEnabled = true

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    fun playCorrect() {
        if (!isEnabled) return
        playTone(frequency = 880.0, duration = 0.15) // A5
    }

    fun playIncorrect() {
        if (!isEnabled) return
        playTone(frequency = 280.0, duration = 0.25) // tiefer Ton
    }

    fun playOperationHint() {
        if (!isEnabled) return
        playMelody(
            listOf(
                Note(523.25, 0.12),  // C5
                Note(659.25, 0.20)   // E5
            )
        )
    }

    fun playSessionComplete() {
        if (!isEnabled) return
        playMelody(
            listOf(
                Note(523.25, 0.13),  // C5
                Note(659.25, 0.13),  // E5
                Note(783.99, 0.13),  // G5
                Note(1046.50, 0.45)  // C6
            )
        )
    }

    fun playRevenge() {
        if (!isEnabled) return
        playMelody(
            listOf(
                Note(659.25, 0.12),  // E5
                Note(783.99, 0.12),  // G5
                Note(1046.50, 0.30)  // C6
            )
        )
    }

    fun playAchievement() {
        if (!isEnabled) return
        playMelody(
            listOf(
                Note(659.25, 0.1),   // E5
                Note(830.61, 0.1),   // G#5
                Note(987.77, 0.1),   // B5
                Note(1318.51, 0.35)  // E6
            )
        )
    }

    private data class Note(val frequency: Double, val duration: Double)

    private fun playMelody(notes: List<Note>) {
        Thread {
            val pauseDuration = 0.025
            val allSamples = mutableListOf<Float>()

            for (note in notes) {
                val frameCount = (SAMPLE_RATE * note.duration).toInt()
                for (i in 0 until frameCount) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val attack = min(t / 0.008, 1.0).toFloat()
                    val release = min((note.duration - t) / 0.04, 1.0).toFloat()
                    val envelope = max(0f, min(attack, release))
                    val fundamental = sin(2.0 * PI * note.frequency * t).toFloat()
                    val harmonic2 = 0.35f * sin(2.0 * PI * note.frequency * 2.0 * t).toFloat()
                    val harmonic3 = 0.12f * sin(2.0 * PI * note.frequency * 3.0 * t).toFloat()
                    allSamples.add(envelope * (fundamental + harmonic2 + harmonic3))
                }
                val gapFrames = (SAMPLE_RATE * pauseDuration).toInt()
                repeat(gapFrames) { allSamples.add(0f) }
            }

            // Normalize
            val peak = allSamples.maxOfOrNull { abs(it) } ?: 1f
            if (peak > 0) {
                for (i in allSamples.indices) {
                    allSamples[i] = allSamples[i] / peak
                }
            }

            playPcmSamples(allSamples)
        }.start()
    }

    private fun playTone(frequency: Double, duration: Double) {
        Thread {
            val frameCount = (SAMPLE_RATE * duration).toInt()
            val samples = FloatArray(frameCount)
            for (i in 0 until frameCount) {
                val t = i.toDouble() / SAMPLE_RATE
                val envelope = (1.0 - t / duration).toFloat()
                samples[i] = envelope * sin(2.0 * PI * frequency * t).toFloat()
            }
            playPcmSamples(samples.toList())
        }.start()
    }

    private fun playPcmSamples(samples: List<Float>) {
        try {
            val shortSamples = ShortArray(samples.size) { i ->
                val clamped = max(-1f, min(1f, samples[i]))
                (clamped * Short.MAX_VALUE).toInt().toShort()
            }

            val bufferSize = shortSamples.size * 2
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(bufferSize, AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(shortSamples, 0, shortSamples.size)
            audioTrack.setVolume(0.5f)
            audioTrack.play()

            // Wait for playback to finish, then release
            val durationMs = (samples.size.toLong() * 1000) / SAMPLE_RATE + 100
            Thread.sleep(durationMs)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {
            // Silently fail if audio is not available
        }
    }
}
