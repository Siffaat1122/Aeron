package com.example.aeron

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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

    // ---------- AERON DESIGN ----------

    private val bg = Color.rgb(3, 6, 16)
    private val surface = Color.rgb(8, 13, 27)
    private val surface2 = Color.rgb(12, 19, 38)
    private val surface3 = Color.rgb(17, 26, 49)

    private val cyan = Color.rgb(0, 225, 255)
    private val blue = Color.rgb(74, 116, 255)
    private val purple = Color.rgb(145, 88, 255)
    private val green = Color.rgb(66, 235, 171)

    private val white = Color.WHITE
    private val text = Color.rgb(226, 233, 247)
    private val muted = Color.rgb(139, 153, 180)
    private val border = Color.rgb(30, 49, 82)

    private lateinit var rootContainer: LinearLayout
    private lateinit var statusText: TextView

    // ---------- ONNX ----------

    private var isListening = false
    private var audioRecord: AudioRecord? = null
    private var detectionThread: Thread? = null
    private var ortEnvironment: OrtEnvironment? = null
    private var ortSession: OrtSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

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

    // =========================================================
    // HOME
    // =========================================================

    private fun showHome() {

        rootContainer = baseScreen()

        rootContainer.addView(homeTopBar())

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 8, 18, 28)
        }

        // Hero

        val hero = panel()

        val coreRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val coreTexts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        coreTexts.addView(
            textView(
                "AERON CORE",
                cyan,
                12f,
                true
            )
        )

        coreTexts.addView(
            textView(
                "Your AI, reimagined.",
                white,
                25f,
                true
            )
        )

        coreTexts.addView(
            textView(
                "Intelligent. Private. Always ready.",
                muted,
                13f,
                false
            )
        )

        coreRow.addView(
            coreTexts,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        val coreBadge = textView(
            "● ONLINE",
            green,
            10f,
            true
        ).apply {
            gravity = Gravity.CENTER
            background = pill(green, 0.12f)
            setPadding(12, 8, 12, 8)
        }

        coreRow.addView(coreBadge)

        hero.addView(coreRow)

        // Orb

        val orbArea = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                -1,
                245
            ).apply {
                topMargin = 8
            }
        }

        val outerOrb = TextView(this).apply {
            text = "✦"
            textSize = 78f
            gravity = Gravity.CENTER
            setTextColor(cyan)
            background = orbBackground(230)
            elevation = 12f
        }

        orbArea.addView(
            outerOrb,
            FrameLayout.LayoutParams(190, 190).apply {
                gravity = Gravity.CENTER
            }
        )

        val orbLabel = textView(
            "AERON",
            white,
            11f,
            true
        ).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.35f
        }

        orbArea.addView(
            orbLabel,
            FrameLayout.LayoutParams(-1, -2).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 8
            }
        )

        hero.addView(orbArea)

        statusText = textView(
            "●  AERON is starting...",
            green,
            13f,
            true
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 2)
        }

        hero.addView(statusText)

        content.addView(
            hero,
            margin(0, 8, 0, 18)
        )

        // Quick Actions

        content.addView(
            sectionTitle(
                "Quick actions",
                "AERON chooses the right tool automatically"
            )
        )

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        actions.addView(
            quickAction("⌕", "Search") {
                showChatWithPrompt("Search the web")
            },
            actionWeight()
        )

        actions.addView(
            quickAction("✦", "Create") {
                showChatWithPrompt("Create an image")
            },
            actionWeight()
        )

        actions.addView(
            quickAction("▶", "Video") {
                showChatWithPrompt("Create a video")
            },
            actionWeight()
        )

        actions.addView(
            quickAction("＋", "File") {
                toast("File picker coming next")
            },
            actionWeight()
        )

        content.addView(
            actions,
            margin(0, 10, 0, 20)
        )

        // Ask AERON

        val ask = panel()

        ask.addView(
            textView(
                "AERON CHAT",
                cyan,
                11f,
                true
            )
        )

        ask.addView(
            textView(
                "What can I help you with?",
                white,
                20f,
                true
            )
        )

        ask.addView(
            textView(
                "Ask anything. AERON will understand your intent.",
                muted,
                13f,
                false
            )
        )

        val input = fakeInput(
            "Message AERON..."
        ) {
            showChat()
        }

        ask.addView(
            input,
            margin(0, 14, 0, 0)
        )

        content.addView(
            ask,
            margin(0, 0, 0, 20)
        )

        // Recent

        content.addView(
            sectionTitle(
                "Recent",
                "Your latest AERON activity"
            )
        )

        content.addView(
            recentItem(
                "Welcome to AERON",
                "Start your first conversation",
                "✦"
            ) {
                showChat()
            }
        )

        content.addView(
            recentItem(
                "Voice Assistant",
                "Hey AERON wake word",
                "◉"
            ) {
                showVoice()
            }
        )

        content.addView(
            recentItem(
                "AERON Core",
                "Customize your assistant",
                "⚙"
            ) {
                showSettings()
            }
        )

        scroll.addView(content)

        rootContainer.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        rootContainer.addView(bottomNav("home"))

        setContentView(rootContainer)
    }

    // =========================================================
    // CHAT
    // =========================================================

    private fun showChat() {
        showChatWithPrompt(null)
    }

    private fun showChatWithPrompt(prompt: String?) {

        rootContainer = baseScreen()

        rootContainer.addView(
            premiumTopBar(
                "AERON",
                "AI CHAT",
                "⌄"
            ) {
                showHome()
            }
        )

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 8, 18, 18)
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        val messages = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 8, 0, 12)
        }

        val welcome = panel()

        welcome.addView(
            textView(
                "AERON CORE",
                cyan,
                11f,
                true
            )
        )

        welcome.addView(
            textView(
                "How can I help?",
                white,
                26f,
                true
            )
        )

        welcome.addView(
            textView(
                "I can understand your request and choose the appropriate capability automatically.",
                muted,
                14f,
                false
            )
        )

        messages.addView(
            welcome,
            margin(0, 4, 0, 14)
        )

        if (prompt != null) {
            messages.addView(
                userMessage(prompt)
            )

            messages.addView(
                aiMessage(
                    "Ready.",
                    "I've understood your request. Connect the corresponding AI service here to complete the action."
                )
            )
        } else {
            messages.addView(
                aiMessage(
                    "Hello 👋",
                    "I'm AERON. Ask me anything."
                )
            )
        }

        // Suggestion chips

        messages.addView(
            textView(
                "Try asking",
                muted,
                12f,
                true
            ),
            margin(0, 10, 0, 5)
        )

        val suggestions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        suggestions.addView(
            chip("Explain something") {
                showChatWithPrompt("Explain something to me")
            }
        )

        suggestions.addView(
            chip("Search web") {
                showChatWithPrompt("Search the web for")
            }
        )

        messages.addView(
            suggestions,
            margin(0, 0, 0, 10)
        )

        scroll.addView(messages)

        content.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        // Composer

        val composer = panel().apply {
            setPadding(12, 10, 12, 10)
        }

        val composerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val attach = iconCircle("＋") {
            toast("File picker coming next")
        }

        composerRow.addView(attach)

        val field = textView(
            "Message AERON...",
            muted,
            15f,
            false
        ).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(12, 0, 8, 0)
            setOnClickListener {
                toast("Message input")
            }
        }

        composerRow.addView(
            field,
            LinearLayout.LayoutParams(0, 52, 1f)
        )

        composerRow.addView(
            iconCircle("🎙") {
                showVoice()
            }
        )

        composerRow.addView(
            iconCircle("↑") {
                toast("Message sent")
            }
        )

        composer.addView(composerRow)

        content.addView(
            composer,
            margin(0, 8, 0, 4)
        )

        rootContainer.addView(content)

        rootContainer.addView(bottomNav("chat"))

        setContentView(rootContainer)
    }

    // =========================================================
    // VOICE
    // =========================================================

    private fun showVoice() {

        rootContainer = baseScreen()

        rootContainer.addView(
            premiumTopBar(
                "AERON",
                "VOICE",
                "◉"
            ) {
                showHome()
            }
        )

        val scroll = ScrollView(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(20, 15, 20, 30)
        }

        content.addView(
            textView(
                "VOICE CONTROL",
                cyan,
                11f,
                true
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        content.addView(
            textView(
                "Talk to AERON",
                white,
                29f,
                true
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        content.addView(
            textView(
                "Say “Hey AERON” to wake your assistant.",
                muted,
                14f,
                false
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        // Large orb

        val orb = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                280,
                280
            ).apply {
                topMargin = 25
                bottomMargin = 25
            }
        }

        val glow = TextView(this).apply {
            text = ""
            background = orbBackground(280)
            alpha = 0.28f
        }

        orb.addView(
            glow,
            FrameLayout.LayoutParams(280, 280)
        )

        val core = TextView(this).apply {
            text = "✦"
            textSize = 92f
            gravity = Gravity.CENTER
            setTextColor(cyan)
            background = orbBackground(210)
            elevation = 15f
        }

        orb.addView(
            core,
            FrameLayout.LayoutParams(210, 210).apply {
                gravity = Gravity.CENTER
            }
        )

        orb.addView(
            textView(
                "AERON",
                white,
                12f,
                true
            ).apply {
                gravity = Gravity.CENTER
                letterSpacing = 0.4f
            },
            FrameLayout.LayoutParams(-1, -2).apply {
                gravity = Gravity.CENTER
            }
        )

        content.addView(orb)

        val state = panel()

        state.addView(
            textView(
                "WAKE WORD STATUS",
                cyan,
                11f,
                true
            )
        )

        val statusRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        statusRow.addView(
            textView(
                "●",
                green,
                20f,
                true
            )
        )

        statusRow.addView(
            textView(
                "Hey AERON",
                white,
                18f,
                true
            ),
            LinearLayout.LayoutParams(0, -2, 1f).apply {
                leftMargin = 10
            }
        )

        statusRow.addView(
            textView(
                "85%",
                cyan,
                13f,
                true
            )
        )

        state.addView(
            statusRow,
            margin(0, 10, 0, 0)
        )

        state.addView(
            textView(
                "ONNX neural wake-word detection is active.",
                muted,
                12f,
                false
            ),
            margin(0, 5, 0, 0)
        )

        content.addView(
            state,
            margin(0, 0, 0, 12)
        )

        val test = bigButton(
            "🎙   Test microphone"
        ) {
            toast(
                if (isListening)
                    "Microphone is listening"
                else
                    "Microphone is starting"
            )
        }

        content.addView(test)

        scroll.addView(content)

        rootContainer.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        rootContainer.addView(bottomNav("voice"))

        setContentView(rootContainer)
    }

    // =========================================================
    // MEMORY
    // =========================================================

    private fun showMemory() {

        rootContainer = baseScreen()

        rootContainer.addView(
            premiumTopBar(
                "AERON",
                "MEMORY VAULT",
                "◈"
            ) {
                showHome()
            }
        )

        val scroll = ScrollView(this)

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 10, 18, 28)
        }

        val hero = panel()

        hero.addView(
            textView(
                "MEMORY VAULT",
                cyan,
                11f,
                true
            )
        )

        hero.addView(
            textView(
                "AERON remembers what matters.",
                white,
                23f,
                true
            )
        )

        hero.addView(
            textView(
                "Your memories stay under your control.",
                muted,
                13f,
                false
            )
        )

        val memoryState = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        memoryState.addView(
            textView(
                "●",
                green,
                17f,
                true
            )
        )

        memoryState.addView(
            textView(
                "Memory protection active",
                text,
                13f,
                true
            ),
            LinearLayout.LayoutParams(0, -2, 1f).apply {
                leftMargin = 8
            }
        )

        memoryState.addView(
            textView(
                "ON",
                green,
                10f,
                true
            ).apply {
                background = pill(green, 0.12f)
                setPadding(10, 6, 10, 6)
            }
        )

        hero.addView(
            memoryState,
            margin(0, 18, 0, 0)
        )

        list.addView(
            hero,
            margin(0, 5, 0, 18)
        )

        list.addView(
            memoryFeature(
                "🧠",
                "Long-term memory",
                "Save useful preferences and information."
            )
        )

        list.addView(
            memoryFeature(
                "🔐",
                "Privacy protection",
                "Sensitive information is protected."
            )
        )

        list.addView(
            memoryFeature(
                "🗑",
                "Memory controls",
                "Review and delete memories whenever you want."
            )
        )

        val empty = panel()

        empty.gravity = Gravity.CENTER

        empty.addView(
            textView(
                "◈",
                muted,
                35f,
                false
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        empty.addView(
            textView(
                "No memories yet",
                white,
                17f,
                true
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        empty.addView(
            textView(
                "Your saved memories will appear here.",
                muted,
                12f,
                false
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        list.addView(
            empty,
            margin(0, 15, 0, 0)
        )

        scroll.addView(list)

        rootContainer.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        rootContainer.addView(bottomNav("memory"))

        setContentView(rootContainer)
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    private fun showSettings() {

        rootContainer = baseScreen()

        rootContainer.addView(
            premiumTopBar(
                "AERON",
                "SETTINGS",
                "⚙"
            ) {
                showHome()
            }
        )

        val scroll = ScrollView(this)

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 6, 18, 28)
        }

        list.addView(
            settingsHeader(
                "Account",
                "Manage your AERON identity"
            )
        )

        list.addView(
            settingsItem(
                "👤",
                "Account",
                "Phone • Google • Facebook • Profile"
            ) {
                showAccount()
            }
        )

        list.addView(
            settingsHeader(
                "AERON",
                "Customize how your assistant behaves"
            )
        )

        list.addView(
            settingsItem(
                "✦",
                "AERON CORE",
                "Personality • Intelligence • Response style"
            ) {
                toast("AERON CORE customization")
            }
        )

        list.addView(
            settingsItem(
                "🎙",
                "Voice & Wake Word",
                "Hey AERON • Voice • Sensitivity"
            ) {
                showVoice()
            }
        )

        list.addView(
            settingsItem(
                "🧠",
                "Memory",
                "Memory Vault • Saved information"
            ) {
                showMemory()
            }
        )

        list.addView(
            settingsItem(
                "◉",
                "Chat History",
                "Search • Rename • Delete • Restore"
            ) {
                toast("Chat History")
            }
        )

        list.addView(
            settingsHeader(
                "Personalization",
                "Make AERON feel like yours"
            )
        )

        list.addView(
            settingsItem(
                "◈",
                "Appearance",
                "Theme • Animations • Orb style"
            ) {
                toast("Appearance settings")
            }
        )

        list.addView(
            settingsItem(
                "🌐",
                "Language & Region",
                "Automatic language • All languages"
            ) {
                toast("Language settings")
            }
        )

        list.addView(
            settingsHeader(
                "Security",
                "Control your privacy"
            )
        )

        list.addView(
            settingsItem(
                "🔒",
                "Privacy & Security",
                "Permissions • Data controls • Privacy"
            ) {
                toast("Privacy settings")
            }
        )

        list.addView(
            settingsHeader(
                "More",
                "Information and support"
            )
        )

        list.addView(
            settingsItem(
                "ⓘ",
                "About AERON",
                "Version 1.0 • Help • Support"
            ) {
                toast("AERON v1.0")
            }
        )

        scroll.addView(list)

        rootContainer.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        rootContainer.addView(bottomNav("settings"))

        setContentView(rootContainer)
    }

    // =========================================================
    // ACCOUNT / LOGIN
    // =========================================================

    private fun showAccount() {

        rootContainer = baseScreen()

        rootContainer.addView(
            premiumTopBar(
                "AERON",
                "ACCOUNT",
                "👤"
            ) {
                showSettings()
            }
        )

        val scroll = ScrollView(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(20, 15, 20, 30)
        }

        val logo = TextView(this).apply {
            text = "✦"
            textSize = 58f
            gravity = Gravity.CENTER
            setTextColor(cyan)
            background = orbBackground(150)
        }

        content.addView(
            logo,
            LinearLayout.LayoutParams(150, 150).apply {
                bottomMargin = 20
            }
        )

        content.addView(
            textView(
                "Welcome to AERON",
                white,
                27f,
                true
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        content.addView(
            textView(
                "Sign in to sync your AERON experience.",
                muted,
                14f,
                false
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        content.addView(
            loginButton(
                "📱",
                "Continue with Phone"
            ) {
                toast("Phone OTP setup comes next")
            },
            margin(0, 28, 0, 0)
        )

        content.addView(
            loginButton(
                "G",
                "Continue with Google"
            )
        )

        content.addView(
            loginButton(
                "f",
                "Continue with Facebook"
            )
        )

        val divider = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        divider.addView(
            line(),
            LinearLayout.LayoutParams(0, 1, 1f)
        )

        divider.addView(
            textView(
                "  OR  ",
                muted,
                11f,
                false
            )
        )

        divider.addView(
            line(),
            LinearLayout.LayoutParams(0, 1, 1f)
        )

        content.addView(
            divider,
            margin(0, 12, 0, 12)
        )

        val guest = bigButton(
            "Continue as Guest"
        ) {
            showHome()
        }

        content.addView(guest)

        content.addView(
            textView(
                "Guest mode keeps your assistant local on this device.",
                muted,
                11f,
                false
            ).apply {
                gravity = Gravity.CENTER
            },
            margin(0, 10, 0, 0)
        )

        scroll.addView(content)

        rootContainer.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        rootContainer.addView(bottomNav("settings"))

        setContentView(rootContainer)
    }

    // =========================================================
    // TOP BAR
    // =========================================================

    private fun homeTopBar(): View {

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(20, 17, 18, 10)
        }

        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val mark = TextView(this).apply {
            text = "✦"
            textSize = 23f
            setTextColor(cyan)
            gravity = Gravity.CENTER
        }

        brand.addView(
            mark,
            LinearLayout.LayoutParams(36, 42)
        )

        brand.addView(
            brandText(
                "AERON",
                "INTELLIGENCE, EVOLVED"
            ),
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        bar.addView(
            brand,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        bar.addView(
            smallIcon("⌕") {
                showChatWithPrompt("Search the web")
            }
        )

        bar.addView(
            smallIcon("⚙") {
                showSettings()
            }
        )

        return bar
    }

    private fun premiumTopBar(
        title: String,
        subtitle: String,
        icon: String,
        back: () -> Unit
    ): View {

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(18, 15, 18, 10)
        }

        val backButton = smallIcon("‹", back)

        bar.addView(backButton)

        bar.addView(
            brandText(title, subtitle),
            LinearLayout.LayoutParams(0, -2, 1f).apply {
                leftMargin = 10
            }
        )

        bar.addView(
            TextView(this).apply {
                text = icon
                textSize = 18f
                gravity = Gravity.CENTER
                setTextColor(cyan)
                background = pill(cyan, 0.08f)
                setPadding(10, 8, 10, 8)
            }
        )

        return bar
    }

    private fun brandText(
        title: String,
        subtitle: String
    ): View {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        box.addView(
            textView(
                title,
                white,
                19f,
                true
            )
        )

        box.addView(
            textView(
                subtitle,
                muted,
                9f,
                true
            )
        )

        return box
    }

    // =========================================================
    // BOTTOM NAV
    // =========================================================

    private fun bottomNav(active: String): View {

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 10)
            background = topBorder()
        }

        nav.addView(
            navItem("⌂", "Home", active == "home") {
                showHome()
            },
            navWeight()
        )

        nav.addView(
            navItem("◉", "Chat", active == "chat") {
                showChat()
            },
            navWeight()
        )

        nav.addView(
            navItem("✦", "Voice", active == "voice") {
                showVoice()
            },
            navWeight()
        )

        nav.addView(
            navItem("◈", "Memory", active == "memory") {
                showMemory()
            },
            navWeight()
        )

        nav.addView(
            navItem("⚙", "Settings", active == "settings") {
                showSettings()
            },
            navWeight()
        )

        return nav
    }

    private fun navItem(
        icon: String,
        title: String,
        active: Boolean,
        click: () -> Unit
    ): View {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(4, 4, 4, 3)
            setOnClickListener {
                click()
            }
        }

        val iconView = textView(
            icon,
            if (active) cyan else muted,
            19f,
            true
        ).apply {
            gravity = Gravity.CENTER
        }

        if (active) {
            iconView.background = pill(cyan, 0.10f)
            iconView.setPadding(12, 5, 12, 5)
        }

        box.addView(iconView)

        box.addView(
            textView(
                title,
                if (active) white else muted,
                9f,
                active
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        return box
    }

    // =========================================================
    // UI COMPONENTS
    // =========================================================

    private fun baseScreen(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
    }

    private fun panel(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            background = rounded(
                surface,
                24f,
                border
            )
            elevation = 2f
        }
    }

    private fun sectionTitle(
        title: String,
        subtitle: String
    ): View {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        box.addView(
            textView(
                title,
                white,
                19f,
                true
            )
        )

        box.addView(
            textView(
                subtitle,
                muted,
                11f,
                false
            )
        )

        return box
    }

    private fun quickAction(
        icon: String,
        title: String,
        click: () -> Unit
    ): View {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(6, 13, 6, 13)
            background = rounded(
                surface2,
                19f,
                border
            )
            setOnClickListener {
                click()
            }
        }

        box.addView(
            textView(
                icon,
                cyan,
                22f,
                true
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        box.addView(
            textView(
                title,
                text,
                10f,
                true
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        return box
    }

    private fun fakeInput(
        hint: String,
        click: () -> Unit
    ): View {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(13, 7, 7, 7)
            background = rounded(
                surface3,
                17f,
                border
            )
            setOnClickListener {
                click()
            }
        }

        box.addView(
            textView(
                hint,
                muted,
                14f,
                false
            ),
            LinearLayout.LayoutParams(0, 50, 1f)
        )

        box.addView(
            iconCircle("↑") {
                click()
            }
        )

        return box
    }

    private fun recentItem(
        title: String,
        subtitle: String,
        icon: String,
        click: () -> Unit
    ): View {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(14, 13, 12, 13)
            background = rounded(
                surface,
                18f,
                border
            )
            setOnClickListener {
                click()
            }
        }

        box.addView(
            TextView(this).apply {
                text = icon
                textSize = 19f
                gravity = Gravity.CENTER
                setTextColor(cyan)
                background = pill(cyan, 0.08f)
                setPadding(10, 8, 10, 8)
            }
        )

        val texts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        texts.addView(
            textView(
                title,
                text,
                14f,
                true
            )
        )

        texts.addView(
            textView(
                subtitle,
                muted,
                11f,
                false
            )
        )

        box.addView(
            texts,
            LinearLayout.LayoutParams(0, -2, 1f).apply {
                leftMargin = 12
            }
        )

        box.addView(
            textView(
                "›",
                muted,
                24f,
                false
            )
        )

        box.layoutParams = margin(0, 7, 0, 0)

        return box
    }

    private fun userMessage(message: String): View {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(15, 12, 15, 12)
            background = rounded(
                Color.rgb(18, 38, 68),
                20f,
                Color.rgb(37, 75, 125)
            )
        }

        card.addView(
            textView(
                "YOU",
                cyan,
                10f,
                true
            )
        )

        card.addView(
            textView(
                message,
                text,
                14f,
                false
            )
        )

        box.addView(card)

        box.layoutParams = margin(35, 5, 0, 5)

        return box
    }

    private fun aiMessage(
        title: String,
        message: String
    ): View {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.TOP
        }

        val mark = TextView(this).apply {
            text = "✦"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(cyan)
            background = pill(cyan, 0.08f)
            setPadding(10, 7, 10, 7)
        }

        box.addView(mark)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(15, 12, 15, 12)
            background = rounded(
                surface,
                20f,
                border
            )
        }

        card.addView(
            textView(
                title,
                cyan,
                11f,
                true
            )
        )

        card.addView(
            textView(
                message,
                text,
                14f,
                false
            )
        )

        box.addView(
            card,
            LinearLayout.LayoutParams(0, -2, 1f).apply {
                leftMargin = 9
            }
        )

        box.layoutParams = margin(0, 5, 35, 5)

        return box
    }

    private fun chip(
        title: String,
        click: () -> Unit
    ): View {

        return TextView(this).apply {
            text = title
            textSize = 10f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(13, 9, 13, 9)
            background = rounded(
                surface2,
                15f,
                border
            )
            setOnClickListener {
                click()
            }
            layoutParams = LinearLayout.LayoutParams(
                -2,
                -2
            ).apply {
                rightMargin = 7
            }
        }
    }

    private fun memoryFeature(
        icon: String,
        title: String,
        subtitle: String
    ): View {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(15, 13, 15, 13)
            background = rounded(
                surface,
                18f,
                border
            )
        }

        box.addView(
            textView(
                icon,
                cyan,
                22f,
                false
            ).apply {
                gravity = Gravity.CENTER
            },
            LinearLayout.LayoutParams(45, 45)
        )

        val texts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        texts.addView(
            textView(
                title,
                white,
                14f,
                true
            )
        )

        texts.addView(
            textView(
                subtitle,
                muted,
                11f,
                false
            )
        )

        box.addView(
            texts,
            LinearLayout.LayoutParams(0, -2, 1f).apply {
                leftMargin = 10
            }
        )

        box.layoutParams = margin(0, 6, 0, 0)

        return box
    }

    private fun settingsHeader(
        title: String,
        subtitle: String
    ): View {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(3, 15, 3, 4)
        }

        box.addView(
            textView(
                title.uppercase(),
                cyan,
                10f,
                true
            )
        )

        box.addView(
            textView(
                subtitle,
                muted,
                10f,
                false
            )
        )

        return box
    }

    private fun settingsItem(
        icon: String,
        title: String,
        subtitle: String,
        click: () -> Unit
    ): View {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(14, 13, 12, 13)
            background = rounded(
                surface,
                19f,
                border
            )
            setOnClickListener {
                click()
            }
        }

        box.addView(
            TextView(this).apply {
                text = icon
                textSize = 19f
                gravity = Gravity.CENTER
                setTextColor(cyan)
                background = pill(cyan, 0.08f)
                setPadding(9, 8, 9, 8)
            }
        )

        val texts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        texts.addView(
            textView(
                title,
                white,
                14f,
                true
            )
        )

        texts.addView(
            textView(
                subtitle,
                muted,
                10f,
                false
            )
        )

        box.addView(
            texts,
            LinearLayout.LayoutParams(0, -2, 1f).apply {
                leftMargin = 11
            }
        )

        box.addView(
            textView(
                "›",
                muted,
                25f,
                false
            )
        )

        box.layoutParams = margin(0, 5, 0, 0)

        return box
    }

    private fun loginButton(
        icon: String,
        title: String,
        click: (() -> Unit)? = null
    ): View {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(15, 12, 13, 12)
            background = rounded(
                surface,
                19f,
                border
            )
            setOnClickListener {
                click?.invoke()
            }
        }

        box.addView(
            textView(
                icon,
                cyan,
                19f,
                true
            ).apply {
                gravity = Gravity.CENTER
            },
            LinearLayout.LayoutParams(45, 45)
        )

        box.addView(
            textView(
                title,
                white,
                14f,
                true
            ),
            LinearLayout.LayoutParams(0, -2, 1f).apply {
                leftMargin = 10
            }
        )

        box.addView(
            textView(
                "›",
                muted,
                24f,
                false
            )
        )

        box.layoutParams = margin(0, 6, 0, 0)

        return box
    }

    private fun bigButton(
        title: String,
        click: () -> Unit
    ): View {

        return TextView(this).apply {
            text = title
            textSize = 14f
            setTextColor(white)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setPadding(15, 15, 15, 15)
            background = rounded(
                surface2,
                18f,
                border
            )
            setOnClickListener {
                click()
            }
        }
    }

    private fun iconCircle(
        symbol: String,
        click: () -> Unit
    ): View {

        return TextView(this).apply {
            text = symbol
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(white)
            background = rounded(
                surface3,
                17f,
                border
            )
            setPadding(9, 5, 9, 5)
            setOnClickListener {
                click()
            }
        }
    }

    private fun smallIcon(
        symbol: String,
        click: () -> Unit
    ): View {

        return TextView(this).apply {
            text = symbol
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = rounded(
                surface2,
                15f,
                border
            )
            setPadding(9, 7, 9, 7)
            setOnClickListener {
                click()
            }
            layoutParams = LinearLayout.LayoutParams(
                42,
                42
            ).apply {
                leftMargin = 5
            }
        }
    }

    private fun textView(
        value: String,
        color: Int,
        size: Float,
        bold: Boolean
    ): TextView {

        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            setPadding(0, 2, 0, 2)

            if (bold) {
                typeface = Typeface.DEFAULT_BOLD
            }
        }
    }

    // =========================================================
    // DRAWABLES
    // =========================================================

    private fun rounded(
        color: Int,
        radius: Float,
        strokeColor: Int
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
            setStroke(1, strokeColor)
        }
    }

    private fun pill(
        color: Int,
        alpha: Float
    ): GradientDrawable {

        val a = (alpha * 255).toInt().coerceIn(0, 255)

        return GradientDrawable().apply {
            setColor(
                Color.argb(
                    a,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
            )
            cornerRadius = 100f
        }
    }

    private fun orbBackground(
        size: Int
    ): GradientDrawable {

        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                Color.rgb(8, 45, 65),
                Color.rgb(21, 27, 68),
                Color.rgb(36, 17, 70)
            )
        ).apply {
            shape = GradientDrawable.OVAL
            setStroke(
                2,
                Color.rgb(0, 170, 220)
            )
        }
    }

    private fun topBorder(): GradientDrawable {

        return GradientDrawable().apply {
            setColor(Color.rgb(5, 10, 22))
            setStroke(
                1,
                Color.rgb(23, 39, 67)
            )
        }
    }

    private fun line(): View {

        return View(this).apply {
            setBackgroundColor(border)
        }
    }

    // =========================================================
    // LAYOUT
    // =========================================================

    private fun margin(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            -1,
            -2
        ).apply {
            setMargins(
                left,
                top,
                right,
                bottom
            )
        }
    }

    private fun actionWeight(): LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            -2,
            1f
        ).apply {
            setMargins(3, 0, 3, 0)
        }
    }

    private fun navWeight(): LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            -2,
            1f
        )
    }

    // =========================================================
    // TOAST
    // =========================================================

    private fun toast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    // =========================================================
    // ONNX / WAKE WORD
    // =========================================================

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
                            ShortArray(WINDOW_SAMPLES)

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

                                if (::statusText.isInitialized) {

                                    statusText.text =
                                        "✦  AERON detected!\n\nHey AERON 👋"

                                    statusText.setTextColor(
                                        cyan
                                    )
                                }
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

            1f /
                    (
                            1f + z
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
