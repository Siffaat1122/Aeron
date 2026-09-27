package com.siffaat.aeron.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.siffaat.aeron.audio.AudioEnergyGate
import com.siffaat.aeron.audio.AudioPreprocessor
import com.siffaat.aeron.engine.WakeWordEngine
import kotlin.concurrent.thread

class WakeWordService : Service() {

    private var isListening = false
    private lateinit var wakeWordEngine: WakeWordEngine
    private val preprocessor = AudioPreprocessor()
    private val energyGate = AudioEnergyGate()

    override fun onCreate() {
        super.onCreate()
        wakeWordEngine = WakeWordEngine(this)
        startForegroundServiceNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isListening) {
            isListening = true
            startAudioRecording()
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startAudioRecording() {
        thread {
            val sampleRate = 16000
            val bufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            val audioBuffer = ShortArray(bufferSize / 2)
            audioRecord.startRecording()

            while (isListening) {
                val readSize = audioRecord.read(audioBuffer, 0, audioBuffer.size)
                if (readSize > 0) {
                    val processedAudio = preprocessor.process(audioBuffer)
                    if (energyGate.isSpeechDetected(processedAudio)) {
                        val score = wakeWordEngine.detect(processedAudio)
                        if (score > 0.75f) {
                            // Wake word detected! Trigger event/action here
                        }
                    }
                }
            }

            audioRecord.stop()
            audioRecord.release()
        }
    }

    private fun startForegroundServiceNotification() {
        val channelId = "AeronWakeWordChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Aeron Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Aeron Listening")
            .setContentText("Wake word engine active hai")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        isListening = false
        wakeWordEngine.close()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
