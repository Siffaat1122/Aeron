package com.example.aeron

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.io.File
import java.io.FileOutputStream
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

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

    private val bg = Color.rgb(4, 8, 20)
    private val card = Color.rgb(10, 20, 39)
    private val card2 = Color.rgb(13, 28, 53)
    private val cyan = Color.rgb(0, 220, 255)
    private val green = Color.rgb(70, 230, 170)
    private val white = Color.WHITE
    private val muted = Color.rgb(155, 170, 195)

    private var isListening = false
    private var audioRecord: AudioRecord? = null
    private var detectionThread: Thread? = null

    private lateinit var statusText: TextView
    private lateinit var rootContainer: LinearLayout

    private var ortEnvironment: OrtEnvironment? = null
    private var ortSession: OrtSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(2, 5, 14)
        window.navigationBarColor = Color.rgb(2, 5, 14)

        copyAssetsIfNeeded()
        showHome()

        if (
            ContextCompat.checkSelfPermission(
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

    // =========================
    // HOME
    // =========================

    private fun showHome() {

        rootContainer = baseScreen()

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(20, 18, 20, 10)
        }

        top.addView(
            verticalText(
                "AERON",
                "YOUR AI ASSISTANT",
                23f
            ),
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        top.addView(
            iconButton("⌕") {
                toast("AERON Search")
            }
        )

        top.addView(
            iconButton("⚙") {
                showSettings()
            }
        )

        rootContainer.addView(top)

        val scroll = ScrollView(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 10, 18, 24)
        }

        val hero = cardView()

        hero.addView(
            label(
                "AERON CORE",
                cyan,
                true,
                13f
            )
        )

        hero.addView(
            label(
                "Hey AERON 👋",
                white,
                true,
                28f
            )
        )

        hero.addView(
            label(
                "Your intelligent assistant is ready.",
                muted,
                false,
                15f
            )
        )

        val logo = ImageView(this).apply {
            setImageResource(R.drawable.aeron_icon_512)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(55, 24, 55, 20)
        }

        hero.addView(
            logo,
            LinearLayout.LayoutParams(-1, 190)
        )

        statusText = label(
            "●  Starting AERON...",
            green,
            true,
            14f
        )

        hero.addView(statusText)

        content.addView(
            hero,
            marginParams(0, 8, 0, 18)
        )

        content.addView(
            label(
                "Quick Actions",
                white,
                true,
                19f
            )
        )

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 10, 0, 14)
        }

        actions.addView(
            actionCard("⌕", "Search") {
                toast("AERON Search")
            },
            weight()
        )

        actions.addView(
            actionCard("▣", "Image") {
                toast("Image generation")
            },
            weight()
        )

        actions.addView(
            actionCard("▶", "Video") {
                toast("Video generation")
            },
            weight()
        )

        actions.addView(
            actionCard("＋", "File") {
                toast("File upload")
            },
            weight()
        )

        content.addView(actions)

        val chat = cardView()

        chat.addView(
            label(
                "AERON CHAT",
                cyan,
                true,
                13f
            )
        )

        chat.addView(
            label(
                "Ask AERON anything...",
                muted,
                false,
                16f
            )
        )

        val chatRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 14, 0, 0)
        }

        chatRow.addView(
            iconButton("🎙") {
                showVoice()
            },
            weight()
        )

        chatRow.addView(
            iconButton("➤") {
                showChat()
            },
            weight()
        )

        chat.addView(chatRow)

        content.addView(
            chat,
            marginParams(0, 0, 0, 18)
        )

        content.addView(
            label(
                "Recent Chats",
                white,
                true,
                19f
            )
        )

        content.addView(
            simpleCard(
                "Welcome to AERON",
                "Start your first conversation"
            ) {
                showChat()
            }
        )

        content.addView(
            simpleCard(
                "AERON Core",
                "Configure your assistant"
            ) {
                showSettings()
            }
        )

        content.addView(
            simpleCard(
                "Voice Assistant",
                "Hey AERON wake word"
            ) {
                showVoice()
            }
        )

        scroll.addView(content)

        rootContainer.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        rootContainer.addView(
            bottomNav("home")
        )

        setContentView(rootContainer)
    }

    // =========================
    // CHAT
    // =========================

    private fun showChat() {

        rootContainer = baseScreen()

        rootContainer.addView(
            topBar("Chat") {
                showHome()
            }
        )

        val scroll = ScrollView(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 10, 18, 20)
        }

        val welcome = cardView()

        welcome.addView(
            label(
                "AERON CORE",
                cyan,
                true,
                13f
            )
        )

        welcome.addView(
            label(
                "How can I help?",
                white,
                true,
                25f
            )
        )

        welcome.addView(
            label(
                "Ask anything. AERON will choose the right capability automatically.",
                muted,
                false,
                14f
            )
        )

        content.addView(
            welcome,
            marginParams(0, 5, 0, 12)
        )

        content.addView(
            messageCard(
                "AERON",
                "I'm ready. Start a conversation whenever you want.",
                true
            )
        )

        val input = cardView()

        input.addView(
            label(
                "Message AERON...",
                muted,
                false,
                16f
            )
        )

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 12, 0, 0)
        }

        row.addView(
            iconButton("📎") {
                toast("File picker coming next")
            },
            weight()
        )

        row.addView(
            iconButton("🎙") {
                showVoice()
            },
            weight()
        )

        row.addView(
            iconButton("➤") {
                toast("Message sent")
            },
            weight()
        )

        input.addView(row)

        content.addView(
            input,
            marginParams(0, 12, 0, 0)
        )

        scroll.addView(content)

        rootContainer.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        rootContainer.addView(
            bottomNav("chat")
        )

        setContentView(rootContainer)
    }

    // =========================
    // VOICE
    // =========================

    private fun showVoice() {

        rootContainer = baseScreen()

        rootContainer.addView(
            topBar("Voice Assistant") {
                showHome()
            }
        )

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(22, 30, 22, 20)
        }

        val orb = TextView(this).apply {
            text = "✦"
            textSize = 90f
            setTextColor(cyan)
            gravity = Gravity.CENTER
            background = rounded(card2, 180f)
        }

        content.addView(
            orb,
            LinearLayout.LayoutParams(210, 210)
        )

        content.addView(
            label(
                "Hey AERON",
                white,
                true,
                28f
            )
        )

        content.addView(
            label(
                "Wake word detection is active.",
                muted,
                false,
                15f
            )
        )

        val state = cardView()

        state.addView(
            label(
                "WAKE WORD",
                cyan,
                true
            )
        )

        state.addView(
            label(
                "Hey AERON",
                white,
                true,
                20f
            )
        )

        state.addView(
            label(
                "Sensitivity: 85%",
                muted,
                false,
                14f
            )
        )

        content.addView(
            state,
            marginParams(0, 30, 0, 12)
        )

        content.addView(
            actionCard(
                "🎙",
                "Test microphone"
            ) {
                toast(
                    if (isListening)
                        "Microphone is listening"
                    else
                        "Microphone is starting"
                )
            },
            LinearLayout.LayoutParams(-1, -2)
        )

        rootContainer.addView(
            content,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        rootContainer.addView(
            bottomNav("voice")
        )

        setContentView(rootContainer)
    }

    // =========================
    // MEMORY
    // =========================

    private fun showMemory() {

        rootContainer = baseScreen()

        rootContainer.addView(
            topBar("Memory Vault") {
                showHome()
            }
        )

        val scroll = ScrollView(this)

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 10, 18, 25)
        }

        list.addView(
            infoCard(
                "🧠",
                "Long-term Memory",
                "AERON can remember useful preferences with your control."
            )
        )

        list.addView(
            infoCard(
                "🔐",
                "Privacy Protected",
                "Sensitive information should never be saved automatically."
            )
        )

        list.addView(
            infoCard(
                "🗑",
                "Memory Controls",
                "View, delete individual memories, or clear everything."
            )
        )

        val empty = cardView()

        empty.addView(
            label(
                "No memories shown yet",
                white,
                true,
                17f
            )
        )

        empty.addView(
            label(
                "Your Memory Vault will appear here.",
                muted,
                false,
                13f
            )
        )

        list.addView(
            empty,
            marginParams(0, 8, 0, 0)
        )

        scroll.addView(list)

        rootContainer.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        rootContainer.addView(
            bottomNav("memory")
        )

        setContentView(rootContainer)
    }

    // =========================
    // SETTINGS
    // =========================

    private fun showSettings() {

        rootContainer = baseScreen()

        rootContainer.addView(
            topBar("Settings") {
                showHome()
            }
        )

        val scroll = ScrollView(this)

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 5, 18, 28)
        }

        list.addView(
            infoCard(
                "👤",
                "Account",
                "Phone • Google • Facebook • Profile"
            ) {
                showAccount()
            }
        )

        list.addView(
            infoCard(
                "✦",
                "AERON CORE",
                "Personality • Intelligence • Response style"
            )
        )

        list.addView(
            infoCard(
                "🎙",
                "Voice & Wake Word",
                "Hey AERON • Voice • Sensitivity"
            ) {
                showVoice()
            }
        )

        list.addView(
            infoCard(
                "🧠",
                "Memory",
                "Memory Vault • Saved information"
            ) {
                showMemory()
            }
        )

        list.addView(
            infoCard(
                "◈",
                "Chat History",
                "Search • Rename • Delete • Restore"
            ) {
                toast("Chat History")
            }
        )

        list.addView(
            infoCard(
                "🎨",
                "Appearance",
                "Theme • Animations • Orb style"
            ) {
                toast("Appearance settings")
            }
        )

        list.addView(
            infoCard(
                "🌐",
                "Language & Region",
                "Automatic language detection • All languages"
            ) {
                toast("Language settings")
            }
        )

        list.addView(
            infoCard(
                "🔒",
                "Privacy & Security",
                "Permissions • Data controls • Privacy"
            ) {
                toast("Privacy settings")
            }
        )

        list.addView(
            infoCard(
                "ⓘ",
                "About AERON",
                "Version • Help • Support"
            ) {
                toast("AERON v1.0")
            }
        )

        scroll.addView(list)

        rootContainer.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        rootContainer.addView(
            bottomNav("settings")
        )

        setContentView(rootContainer)
    }

    // =========================
    // ACCOUNT
    // =========================

    private fun showAccount() {

        rootContainer = baseScreen()

        rootContainer.addView(
            topBar("Account") {
                showSettings()
            }
        )

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 12, 18, 20)
        }

        val profile = cardView()

        profile.addView(
            label(
                "AERON ACCOUNT",
                cyan,
                true
            )
        )

        profile.addView(
            label(
                "Welcome to AERON",
                white,
                true,
                24f
            )
        )

        profile.addView(
            label(
                "Sign in to sync your account and preferences.",
                muted,
                false,
                14f
            )
        )

        content.addView(
            profile,
            marginParams(0, 5, 0, 12)
        )

        content.addView(
            loginButton(
                "📱",
                "Continue with Phone"
            ) {
                toast("Phone login setup next")
            }
        )

        content.addView(
            loginButton(
                "G",
                "Continue with Google"
            ) {
                toast("Google login setup next")
            }
        )

        content.addView(
            loginButton(
                "f",
                "Continue with Facebook"
            ) {
                toast("Facebook login setup next")
            }
        )

        val guest = cardView()

        guest.addView(
            label(
                "Continue as Guest",
                white,
                true,
                16f
            )
        )

        guest.addView(
            label(
                "Use AERON locally without signing in.",
                muted,
                false,
                13f
            )
        )

        guest.setOnClickListener {
            showHome()
        }

        content.addView(
            guest,
            marginParams(0, 10, 0, 0)
        )

        rootContainer.addView(
            content,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        rootContainer.addView(
            bottomNav("settings")
        )

        setContentView(rootContainer)
    }

    // =========================
    // UI HELPERS
    // =========================

    private fun topBar(
        title: String,
        back: () -> Unit
    ): View {

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(18, 18, 18, 12)
        }

        bar.addView(
            iconButton("‹", back)
        )

        bar.addView(
            label(
                title,
                white,
                true,
                23f
            ),
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        return bar
    }

    private fun bottomNav(active: String): View {

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(6, 8, 6, 10)
            setBackgroundColor(Color.rgb(7, 14, 29))
        }

        nav.addView(
            navItem(
                "⌂",
                "Home",
                active == "home"
            ) {
                showHome()
            },
            weight()
        )

        nav.addView(
            navItem(
                "◉",
                "Chat",
                active == "chat"
            ) {
                showChat()
            },
            weight()
        )

        nav.addView(
            navItem(
                "🎙",
                "Voice",
                active == "voice"
            ) {
                showVoice()
            },
            weight()
        )

        nav.addView(
            navItem(
                "◈",
                "Memory",
                active == "memory"
            ) {
                showMemory()
            },
            weight()
        )

        nav.addView(
            navItem(
                "⚙",
                "Settings",
                active == "settings"
            ) {
                showSettings()
            },
            weight()
        )

        return nav
    }

    private fun navItem(
        icon: String,
        name: String,
        active: Boolean,
        click: () -> Unit
    ): View {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(2, 3, 2, 3)

            setOnClickListener {
                click()
            }
        }

        box.addView(
            label(
                icon,
                if (active) cyan else muted,
                true,
                20f
            )
        )

        box.addView(
            label(
                name,
                if (active) white else muted,
                false,
                10f
            )
        )

        return box
    }

    private fun baseScreen(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
    }

    private fun cardView(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            background = rounded(card, 22f)
        }
    }

    private fun simpleCard(
        title: String,
        subtitle: String,
        click: () -> Unit
    ): View {

        val box = cardView()

        box.addView(
            label(
                title,
                white,
                true,
                16f
            )
        )

        box.addView(
            label(
                subtitle,
                muted,
                false,
                12f
            )
        )

        box.setOnClickListener {
            click()
        }

        box.layoutParams =
            marginParams(0, 7, 0, 0)

        return box
    }

    private fun messageCard(
        title: String,
        message: String,
        ai: Boolean
    ): View {

        val box = cardView()

        box.addView(
            label(
                title,
                if (ai) cyan else white,
                true,
                13f
            )
        )

        box.addView(
            label(
                message,
                white,
                false,
                15f
            )
        )

        box.layoutParams =
            marginParams(0, 7, 0, 0)

        return box
    }

    private fun infoCard(
        icon: String,
        title: String,
        subtitle: String,
        click: (() -> Unit)? = null
    ): View {

        val box = cardView()

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        row.addView(
            label(
                icon,
                cyan,
                true,
                24f
            ),
            LinearLayout.LayoutParams(48, 58)
        )

        val texts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        texts.addView(
            label(
                title,
                white,
                true,
                16f
            )
        )

        texts.addView(
            label(
                subtitle,
                muted,
                false,
                12f
            )
        )

        row.addView(
            texts,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        row.addView(
            label(
                "›",
                muted,
                false,
                27f
            )
        )

        box.addView(row)

        if (click != null) {
            box.setOnClickListener {
                click()
            }
        }

        box.layoutParams =
            marginParams(0, 7, 0, 0)

        return box
    }

    private fun actionCard(
        icon: String,
        title: String,
        click: () -> Unit
    ): View {

        val box = cardView()

        box.gravity = Gravity.CENTER

        box.addView(
            label(
                icon,
                cyan,
                true,
                24f
            )
        )

        box.addView(
            label(
                title,
                white,
                true,
                11f
            )
        )

        box.setOnClickListener {
            click()
        }

        return box
    }

    private fun loginButton(
        icon: String,
        title: String,
        click: () -> Unit
    ): View {

        val box = cardView()

        box.orientation = LinearLayout.HORIZONTAL
        box.gravity = Gravity.CENTER_VERTICAL

        box.addView(
            label(
                icon,
                cyan,
                true,
                20f
            ),
            LinearLayout.LayoutParams(45, 55)
        )

        box.addView(
            label(
                title,
                white,
                true,
                15f
            ),
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        box.addView(
            label(
                "›",
                muted,
                false,
                25f
            )
        )

        box.setOnClickListener {
            click()
        }

        box.layoutParams =
            marginParams(0, 7, 0, 0)

        return box
    }

    private fun verticalText(
        title: String,
        subtitle: String,
        size: Float
    ): View {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        box.addView(
            label(
                title,
                white,
                true,
                size
            )
        )

        box.addView(
            label(
                subtitle,
                muted,
                false,
                11f
            )
        )

        return box
    }

    private fun iconButton(
        symbol: String,
        click: () -> Unit
    ): TextView {

        return TextView(this).apply {
            text = symbol
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(white)
            setPadding(12, 8, 12, 8)
            background = rounded(card2, 18f)

            setOnClickListener {
                click()
            }
        }
    }

    private fun label(
        value: String,
        color: Int,
        bold: Boolean,
        size: Float = 15f
    ): TextView {

        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            setPadding(0, 3, 0, 3)

            if (bold) {
                typeface = Typeface.DEFAULT_BOLD
            }
        }
    }

    private fun rounded(
        color: Int,
        radius: Float
    ): android.graphics.drawable.GradientDrawable {

        return android.graphics.drawable.GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
            setStroke(
                1,
                Color.rgb(25, 65, 110)
            )
        }
    }

    private fun weight(): LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            -2,
            1f
        ).apply {
            setMargins(4, 0, 4, 0)
        }
    }

    private fun marginParams(
        l: Int,
        t: Int,
        r: Int,
        b: Int
    ): LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            -1,
            -2
        ).apply {
            setMargins(l, t, r, b)
        }
    }

    private fun toast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    // =========================
    // ONNX / WAKE WORD
    // =========================

    private fun copyAssetsIfNeeded() {

        val assetNames = arrayOf(
            "wake.int8.onnx",
            "wake.json"
        )

        for (name in assetNames) {

            val file = File(
                filesDir,
                name
            )

            if (!file.exists()) {

                try {

                    assets.open(name).use { input ->

                        FileOutputStream(file).use { output ->

                            input.copyTo(output)
                        }
                    }

                } catch (_: Exception) {
                }
            }
        }
    }

    private fun startWakeWordDetection() {

        try {

            val modelFile =
                File(
                    filesDir,
                    "wake.int8.onnx"
                )

            if (!modelFile.exists()) {

                updateStatus(
                    "Wake-word model not found"
                )

                return
            }

            ortEnvironment =
                OrtEnvironment.getEnvironment()

            ortSession =
                ortEnvironment!!.createSession(
                    modelFile.absolutePath,
                    OrtSession.SessionOptions()
                )

        } catch (e: Exception) {

            updateStatus(
                "Model error: ${e.message}"
            )

            return
        }

        val minBuffer =
            AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

        val bufferSize =
            max(
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

            audioRecord =
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

            audioRecord?.startRecording()

            isListening = true

            updateStatus(
                "●  AERON is listening\nSay: Hey AERON"
            )

            detectionThread = Thread {

                val circularBuffer =
                    ShortArray(WINDOW_SAMPLES)

                var position = 0
                var filled = 0

                val readBuffer =
                    ShortArray(320)

                while (isListening) {

                    val count =
                        audioRecord?.read(
                            readBuffer,
                            0,
                            readBuffer.size
                        ) ?: 0

                    if (count <= 0) continue

                    for (i in 0 until count) {

                        circularBuffer[position] =
                            readBuffer[i]

                        position++

                        if (position >= WINDOW_SAMPLES) {
                            position = 0
                        }

                        if (filled < WINDOW_SAMPLES) {
                            filled++
                        }
                    }

                    if (filled >= WINDOW_SAMPLES) {

                        val audio =
                            ShortArray(
                                WINDOW_SAMPLES
                            )

                        var index = position

                        for (i in 0 until WINDOW_SAMPLES) {

                            audio[i] =
                                circularBuffer[index]

                            index++

                            if (index >= WINDOW_SAMPLES) {
                                index = 0
                            }
                        }

                        val probability =
                            runModel(audio)

                        if (
                            probability >= THRESHOLD
                        ) {

                            runOnUiThread {

                                statusText.text =
                                    "✦  AERON detected!\n\nHey AERON 👋"

                                statusText.setTextColor(
                                    cyan
                                )
                            }

                            Thread.sleep(1500)

                            updateStatus(
                                "●  AERON is listening\nSay: Hey AERON"
                            )
                        }
                    }
                }
            }

            detectionThread?.start()

        } catch (e: Exception) {

            updateStatus(
                "Microphone error: ${e.message}"
            )
        }
    }

    private fun runModel(
        audio: ShortArray
    ): Float {

        val session =
            ortSession ?: return 0f

        val environment =
            ortEnvironment ?: return 0f

        try {

            val mel =
                createMelSpectrogram(audio)

            val inputTensor =
                OnnxTensor.createTensor(
                    environment,
                    FloatBuffer.wrap(mel),
                    longArrayOf(
                        1,
                        N_MELS.toLong(),
                        FRAMES.toLong()
                    )
                )

            val inputs =
                mapOf(
                    "mel" to inputTensor
                )

            val result =
                session.run(inputs)

            val output =
                result[0].value

            val logit =
                when (output) {

                    is FloatArray ->
                        output[0]

                    is Array<*> -> {

                        val first =
                            output[0]

                        when (first) {

                            is FloatArray ->
                                first[0]

                            else ->
                                0f
                        }
                    }

                    else ->
                        0f
                }

            inputTensor.close()
            result.close()

            return sigmoid(logit)

        } catch (_: Exception) {

            return 0f
        }
    }

    private fun createMelSpectrogram(
        audio: ShortArray
    ): FloatArray {

        val result =
            FloatArray(
                N_MELS * FRAMES
            )

        val samples =
            FloatArray(audio.size)

        var peak = 0f

        for (i in audio.indices) {

            samples[i] =
                audio[i] / 32768f

            peak =
                max(
                    peak,
                    kotlin.math.abs(
                        samples[i]
                    )
                )
        }

        if (peak > 0.0001f) {

            val targetPeak =
                10f.pow(
                    -3f / 20f
                )

            val gain =
                targetPeak / peak

            for (i in samples.indices) {
                samples[i] *= gain
            }
        }

        val melFilters =
            createMelFilters()

        val fftReal =
            FloatArray(N_FFT)

        val fftImag =
            FloatArray(N_FFT)

        for (frame in 0 until FRAMES) {

            val center =
                frame * HOP_LENGTH

            for (i in 0 until N_FFT) {
                fftReal[i] = 0f
                fftImag[i] = 0f
            }

            for (i in 0 until WIN_LENGTH) {

                val sampleIndex =
                    center +
                            i -
                            WIN_LENGTH / 2

                if (
                    sampleIndex >= 0 &&
                    sampleIndex < samples.size
                ) {

                    val window =
                        0.5f *
                                (
                                        1f -
                                                cos(
                                                    2.0 *
                                                            PI *
                                                            i /
                                                            (WIN_LENGTH - 1)
                                                ).toFloat()
                                        )

                    fftReal[i] =
                        samples[sampleIndex] *
                                window
                }
            }

            fft(
                fftReal,
                fftImag
            )

            val power =
                FloatArray(
                    N_FFT / 2 + 1
                )

            for (k in power.indices) {

                power[k] =
                    (
                            fftReal[k] *
                                    fftReal[k] +
                                    fftImag[k] *
                                    fftImag[k]
                            ) / N_FFT
            }

            for (m in 0 until N_MELS) {

                var energy = 0f

                for (k in power.indices) {

                    energy +=
                        power[k] *
                                melFilters[m][k]
                }

                energy =
                    max(
                        energy,
                        1e-10f
                    )

                val logEnergy =
                    10f *
                            (
                                    ln(
                                        energy.toDouble()
                                    ) /
                                            ln(10.0)
                                    ).toFloat()

                result[
                    m * FRAMES + frame
                ] =
                    max(
                        -80f,
                        logEnergy
                    )
            }
        }

        return result
    }

    private fun createMelFilters():
            Array<FloatArray> {

        val filters =
            Array(N_MELS) {
                FloatArray(
                    N_FFT / 2 + 1
                )
            }

        val lowMel =
            hzToMel(0f)

        val highMel =
            hzToMel(8000f)

        val points =
            IntArray(
                N_MELS + 2
            )

        for (i in points.indices) {

            val mel =
                lowMel +
                        (highMel - lowMel) *
                        i /
                        (N_MELS + 1)

            val hz =
                melToHz(mel)

            points[i] =
                (
                        (N_FFT + 1) *
                                hz /
                                SAMPLE_RATE
                        ).toInt()
        }

        for (m in 1..N_MELS) {

            val left =
                points[m - 1]

            val center =
                points[m]

            val right =
                points[m + 1]

            for (k in left until center) {

                if (
                    center > left &&
                    k in filters[m - 1].indices
                ) {

                    filters[m - 1][k] =
                        (
                                k - left
                                ).toFloat() /
                                (
                                        center - left
                                        )
                }
            }

            for (k in center until right) {

                if (
                    right > center &&
                    k in filters[m - 1].indices
                ) {

                    filters[m - 1][k] =
                        (
                                right - k
                                ).toFloat() /
                                (
                                        right - center
                                        )
                }
            }
        }

        return filters
    }

    private fun hzToMel(
        hz: Float
    ): Float {

        return (
                2595.0 *
                        log10(
                            1.0 +
                                    hz /
                                    700.0
                        )
                ).toFloat()
    }

    private fun melToHz(
        mel: Float
    ): Float {

        return (
                700.0 *
                        (
                                10.0.pow(
                                    mel /
                                            2595.0
                                ) - 1.0
                                )
                ).toFloat()
    }

    private fun log10(
        value: Double
    ): Double {

        return ln(value) /
                ln(10.0)
    }

    private fun sigmoid(
        x: Float
    ): Float {

        return if (x >= 0) {

            val z =
                exp(-x)

            (
                    1f /
                            (
                                    1f + z
                                    )
                    )

        } else {

            val z =
                exp(x)

            z /
                    (
                            1f + z
                            )
        }
    }

    private fun fft(
        real: FloatArray,
        imag: FloatArray
    ) {

        val n =
            real.size

        var j = 0

        for (i in 1 until n) {

            var bit =
                n shr 1

            while (
                j and bit != 0
            ) {

                j =
                    j xor bit

                bit =
                    bit shr 1
            }

            j =
                j xor bit

            if (i < j) {

                val tempR =
                    real[i]

                real[i] =
                    real[j]

                real[j] =
                    tempR

                val tempI =
                    imag[i]

                imag[i] =
                    imag[j]

                imag[j] =
                    tempI
            }
        }

        var length = 2

        while (length <= n) {

            val angle =
                -2.0 *
                        PI /
                        length

            val wLenR =
                cos(angle)
                    .toFloat()

            val wLenI =
                sin(angle)
                    .toFloat()

            var i = 0

            while (i < n) {

                var wR = 1f
                var wI = 0f

                for (
                    k in 0 until length / 2
                ) {

                    val even =
                        i + k

                    val odd =
                        i +
                                k +
                                length / 2

                    val oddR =
                        real[odd]

                    val oddI =
                        imag[odd]

                    val tempR =
                        wR *
                                oddR -
                                wI *
                                oddI

                    val tempI =
                        wR *
                                oddI +
                                wI *
                                oddR

                    real[odd] =
                        real[even] -
                                tempR

                    imag[odd] =
                        imag[even] -
                                tempI

                    real[even] +=
                        tempR

                    imag[even] +=
                        tempI

                    val nextWR =
                        wR *
                                wLenR -
                                wI *
                                wLenI

                    wI =
                        wR *
                                wLenI +
                                wI *
                                wLenR

                    wR =
                        nextWR
                }

                i += length
            }

            length =
                length shl 1
        }
    }

    private fun updateStatus(
        text: String
    ) {

        runOnUiThread {

            if (::statusText.isInitialized) {

                statusText.text =
                    text

                statusText.setTextColor(
                    green
                )
            }
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
            requestCode ==
            RECORD_AUDIO_REQUEST_CODE &&
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
