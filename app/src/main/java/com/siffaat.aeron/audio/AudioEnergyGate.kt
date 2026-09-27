package com.siffaat.aeron.audio

import kotlin.math.log10

class AudioEnergyGate(private val thresholdDb: Float = -40.0f) {

    /**
     * Calculates RMS (Root Mean Square) energy in dB and checks if sound exceeds threshold.
     */
    fun isSpeechDetected(audioBuffer: FloatArray): Boolean {
        var sum = 0.0
        for (sample in audioBuffer) {
            sum += sample * sample
        }
        val rms = Math.sqrt(sum / audioBuffer.size)
        val db = 20 * log10(rms.coerceAtLeast(1e-6))

        return db > thresholdDb
    }
}
