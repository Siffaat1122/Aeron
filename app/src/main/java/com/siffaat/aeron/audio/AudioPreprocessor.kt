package com.siffaat.aeron.audio

import kotlin.math.abs
import kotlin.math.max

class AudioPreprocessor(
    private val sampleRate: Int = 16000,
    private val cutoffFrequency: Float = 80.0f
) {
    private var lastInput = 0.0f
    private var lastOutput = 0.0f

    // High-pass filter coefficient calculation
    private val alpha: Float
        get() {
            val dt = 1.0f / sampleRate
            val rc = 1.0f / (2.0f * Math.PI.toFloat() * cutoffFrequency)
            return rc / (rc + dt)
        }

    /**
     * Applies High-Pass Filter and peak normalization on raw PCM ShortArray.
     */
    fun process(pcmData: ShortArray): FloatArray {
        val filtered = FloatArray(pcmData.size)
        var maxAmplitude = 0.0f

        // Step 1: High-pass filtering
        val a = alpha
        for (i in pcmData.indices) {
            val input = pcmData[i] / 32768.0f
            val output = a * (lastOutput + input - lastInput)
            lastInput = input
            lastOutput = output
            filtered[i] = output
            maxAmplitude = max(maxAmplitude, abs(output))
        }

        // Step 2: Normalization (Scale to avoid clipping)
        if (maxAmplitude > 0.01f) {
            val scaleFactor = 0.9f / maxAmplitude
            for (i in filtered.indices) {
                filtered[i] *= scaleFactor
            }
        }

        return filtered
    }
}
