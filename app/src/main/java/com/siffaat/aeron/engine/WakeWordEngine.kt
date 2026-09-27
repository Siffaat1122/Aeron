package com.siffaat.aeron.engine

import android.content.Context
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.nio.FloatBuffer

class WakeWordEngine(private val context: Context) {

    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            ortEnv = OrtEnvironment.getEnvironment()
            val modelBytes = context.assets.open("wake.int8.onnx").readBytes()
            ortSession = ortEnv?.createSession(modelBytes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun detect(audioData: FloatArray): Float {
        if (ortSession == null || ortEnv == null) return 0.0f

        return try {
            val shape = longArrayOf(1, audioData.size.toLong())
            val buffer = FloatBuffer.wrap(audioData)
            val tensor = OnnxTensor.createTensor(ortEnv, buffer, shape)

            val inputName = ortSession?.inputNames?.iterator()?.next() ?: "input"
            val results = ortSession?.run(mapOf(inputName to tensor))

            val outputTensor = results?.get(0) as? OnnxTensor
            val outputArray = outputTensor?.floatBuffer?.array()

            outputArray?.get(0) ?: 0.0f
        } catch (e: Exception) {
            e.printStackTrace()
            0.0f
        }
    }

    fun close() {
        ortSession?.close()
        ortEnv?.close()
    }
}
