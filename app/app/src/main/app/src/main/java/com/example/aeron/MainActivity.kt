package com.example.aeron

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.microsoft.onnxruntime.OnnxTensor
import com.microsoft.onnxruntime.OrtEnvironment
import com.microsoft.onnxruntime.OrtSession
import java.io.File
import java.io.FileOutputStream
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    companion object {
        private const val RECORD_AUDIO_REQUEST_CODE = 101
        private const val SAMPLE_RATE = 16000
        private const val WINDOW_SAMPLES = 16000
        private const val N_MELS = 40
        private const val N_FFT = 512
        private const val HOP_LENGTH = 160
        private const val WIN_LENGTH = 400
        private const val FRAMES = 101
        private const val THRESHOLD = 0.85f
    }

    private var isListening = false
    private var audioRecord: AudioRecord? = null
    private var detectionThread: Thread? = null

    private lateinit var statusText: TextView

    private var ortEnvironment: OrtEnvironment? = null
    private var ortSession: OrtSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createInterface()

        copyAssetsIfNeeded()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_REQUEST_CODE
            )
        } else {
            startWakeWordDetection()
        }
    }

    private fun createInterface() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 80, 40, 40)
        }

        val title = TextView(this).apply {
            text = "AERON"
            textSize = 34f
        }

        statusText = TextView(this).apply {
            text = "Starting..."
            textSize = 20f
            setPadding(0, 40, 0, 0)
        }

        layout.addView(title)
        layout.addView(statusText)

        setContentView(layout)
    }

    private fun copyAssetsIfNeeded() {
        val assetNames = arrayOf(
            "wake.int8.onnx",
            "wake.json"
        )

        for (name in assetNames) {
            val file = File(filesDir, name)

            if (!file.exists()) {
                assets.open(name).use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    private fun startWakeWordDetection() {

        try {
            val modelFile = File(filesDir, "wake.int8.onnx")

            if (!modelFile.exists()) {
                updateStatus("Wake-word model not found")
                return
            }

            ortEnvironment = OrtEnvironment.getEnvironment()

            val sessionOptions = OrtSession.SessionOptions()

            ortSession = ortEnvironment!!.createSession(
                modelFile.absolutePath,
                sessionOptions
            )

        } catch (e: Exception) {
            updateStatus("Model error: ${e.message}")
            return
        }

        val minBuffer = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val bufferSize = max(
            minBuffer,
            SAMPLE_RATE / 2
        )

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            audioRecord?.startRecording()

            isListening = true

            updateStatus("Listening...\nSay: Hey AERON")

            detectionThread = Thread {

                val circularBuffer = ShortArray(WINDOW_SAMPLES)
                var position = 0
                var filled = 0

                val readBuffer = ShortArray(320)

                while (isListening) {

                    val count = audioRecord?.read(
                        readBuffer,
                        0,
                        readBuffer.size
                    ) ?: 0

                    if (count <= 0) continue

                    for (i in 0 until count) {

                        circularBuffer[position] = readBuffer[i]

                        position++
                        if (position >= WINDOW_SAMPLES) {
                            position = 0
                        }

                        if (filled < WINDOW_SAMPLES) {
                            filled++
                        }
                    }

                    if (filled >= WINDOW_SAMPLES) {

                        val audio = ShortArray(WINDOW_SAMPLES)

                        var index = position

                        for (i in 0 until WINDOW_SAMPLES) {
                            audio[i] = circularBuffer[index]

                            index++

                            if (index >= WINDOW_SAMPLES) {
                                index = 0
                            }
                        }

                        val probability = runModel(audio)

                        if (probability >= THRESHOLD) {

                            runOnUiThread {
                                statusText.text =
                                    "AERON detected!\n\nHey AERON 👋"
                            }

                            Thread.sleep(1500)

                            runOnUiThread {
                                statusText.text =
                                    "Listening...\nSay: Hey AERON"
                            }
                        }
                    }
                }
            }

            detectionThread?.start()

        } catch (e: Exception) {
            updateStatus("Microphone error: ${e.message}")
        }
    }

    private fun runModel(audio: ShortArray): Float {

        val session = ortSession ?: return 0f
        val environment = ortEnvironment ?: return 0f

        try {

            val mel = createMelSpectrogram(audio)

            val inputTensor = OnnxTensor.createTensor(
                environment,
                FloatBuffer.wrap(mel),
                longArrayOf(1, N_MELS.toLong(), FRAMES.toLong())
            )

            val inputs = mapOf(
                "mel" to inputTensor
            )

            val result = session.run(inputs)

            val output = result[0].value

            val logit = when (output) {
                is FloatArray -> output[0]
                is Array<*> -> {
                    val first = output[0]
                    when (first) {
                        is FloatArray -> first[0]
                        else -> 0f
                    }
                }
                else -> 0f
            }

            inputTensor.close()
            result.close()

            return sigmoid(logit)

        } catch (e: Exception) {
            return 0f
        }
    }

    private fun createMelSpectrogram(audio: ShortArray): FloatArray {

        val result = FloatArray(N_MELS * FRAMES)

        val samples = FloatArray(audio.size)

        var peak = 0f

        for (i in audio.indices) {
            samples[i] = audio[i] / 32768f
            peak = max(peak, kotlin.math.abs(samples[i]))
        }

        if (peak > 0.0001f) {

            val targetPeak = 10f.pow(-3f / 20f)
            val gain = targetPeak / peak

            for (i in samples.indices) {
                samples[i] *= gain
            }
        }

        val melFilters = createMelFilters()

        val fftReal = FloatArray(N_FFT)
        val fftImag = FloatArray(N_FFT)

        for (frame in 0 until FRAMES) {

            val center = frame * HOP_LENGTH

            for (i in 0 until N_FFT) {
                fftReal[i] = 0f
                fftImag[i] = 0f
            }

            for (i in 0 until WIN_LENGTH) {

                val sampleIndex =
                    center + i - WIN_LENGTH / 2

                if (
                    sampleIndex >= 0 &&
                    sampleIndex < samples.size
                ) {

                    val window =
                        0.5f * (
                            1f - cos(
                                2.0 * PI * i /
                                    (WIN_LENGTH - 1)
                            ).toFloat()
                        )

                    fftReal[i] =
                        samples[sampleIndex] * window
                }
            }

            fft(fftReal, fftImag)

            val power = FloatArray(N_FFT / 2 + 1)

            for (k in power.indices) {

                power[k] =
                    (
                        fftReal[k] * fftReal[k] +
                        fftImag[k] * fftImag[k]
                    ) / N_FFT
            }

            for (m in 0 until N_MELS) {

                var energy = 0f

                for (k in power.indices) {
                    energy +=
                        power[k] * melFilters[m][k]
                }

                energy = max(energy, 1e-10f)

                val logEnergy =
                    10f * (ln(energy.toDouble()) /
                        ln(10.0)).toFloat()

                result[m * FRAMES + frame] =
                    max(-80f, logEnergy)
            }
        }

        return result
    }

    private fun createMelFilters(): Array<FloatArray> {

        val filters =
            Array(N_MELS) {
                FloatArray(N_FFT / 2 + 1)
            }

        val lowMel = hzToMel(0f)
        val highMel = hzToMel(8000f)

        val points = IntArray(N_MELS + 2)

        for (i in points.indices) {

            val mel =
                lowMel +
                    (highMel - lowMel) *
                    i /
                    (N_MELS + 1)

            val hz = melToHz(mel)

            points[i] =
                ((N_FFT + 1) * hz /
                    SAMPLE_RATE)
                    .toInt()
        }

        for (m in 1..N_MELS) {

            val left = points[m - 1]
            val center = points[m]
            val right = points[m + 1]

            for (k in left until center) {

                if (center > left) {
                    filters[m - 1][k] =
                        (k - left).toFloat() /
                            (center - left)
                }
            }

            for (k in center until right) {

                if (right > center) {
                    filters[m - 1][k] =
                        (right - k).toFloat() /
                            (right - center)
                }
            }
        }

        return filters
    }

    private fun hzToMel(hz: Float): Float {
        return (
            2595.0 *
                log10(1.0 + hz / 700.0)
            ).toFloat()
    }

    private fun melToHz(mel: Float): Float {
        return (
            700.0 *
                (10.0.pow(mel / 2595.0) - 1.0)
            ).toFloat()
    }

    private fun log10(value: Double): Double {
        return ln(value) / ln(10.0)
    }

    private fun Float.pow(power: Float): Float {
        return kotlin.math.pow(power)
    }

    private fun sigmoid(x: Float): Float {

        return if (x >= 0) {

            val z = exp(-x)
            (1f / (1f + z)).toFloat()

        } else {

            val z = exp(x)
            (z / (1f + z)).toFloat()
        }
    }

    private fun fft(
        real: FloatArray,
        imag: FloatArray
    ) {

        val n = real.size

        var j = 0

        for (i in 1 until n) {

            var bit = n shr 1

            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }

            j = j xor bit

            if (i < j) {

                val tempR = real[i]
                real[i] = real[j]
                real[j] = tempR

                val tempI = imag[i]
                imag[i] = imag[j]
                imag[j] = tempI
            }
        }

        var length = 2

        while (length <= n) {

            val angle =
                -2.0 * PI / length

            val wLenR =
                cos(angle).toFloat()

            val wLenI =
                sin(angle).toFloat()

            var i = 0

            while (i < n) {

                var wR = 1f
                var wI = 0f

                for (k in 0 until length / 2) {

                    val even = i + k
                    val odd =
                        i + k + length / 2

                    val oddR = real[odd]
                    val oddI = imag[odd]

                    val tempR =
                        wR * oddR - wI * oddI

                    val tempI =
                        wR * oddI + wI * oddR

                    real[odd] =
                        real[even] - tempR

                    imag[odd] =
                        imag[even] - tempI

                    real[even] += tempR
                    imag[even] += tempI

                    val nextWR =
                        wR * wLenR - wI * wLenI

                    wI =
                        wR * wLenI + wI * wLenR

                    wR = nextWR
                }

                i += length
            }

            length = length shl 1
        }
    }

    private fun updateStatus(text: String) {

        runOnUiThread {
            statusText.text = text
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            requestCode == RECORD_AUDIO_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] ==
            PackageManager.PERMISSION_GRANTED
        ) {

            startWakeWordDetection()

        } else {

            Toast.makeText(
                this,
                "Microphone permission required",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroy() {

        isListening = false

        try {
            audioRecord?.stop()
        } catch (_: Exception) {
        }

        audioRecord?.release()
        audioRecord = null

        ortSession?.close()
        ortSession = null

        super.onDestroy()
    }
}
