package com.example.aeron

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin

class MainActivity : AppCompatActivity() {

    // ─────────────────────────────────────────────
    // AERON PREMIUM PALETTE
    // ─────────────────────────────────────────────

    private val bg = Color.rgb(3, 7, 18)
    private val panel = Color.rgb(9, 15, 29)
    private val panel2 = Color.rgb(12, 19, 37)
    private val panel3 = Color.rgb(15, 23, 45)

    private val cyan = Color.rgb(0, 229, 168)
    private val blue = Color.rgb(72, 104, 255)
    private val purple = Color.rgb(139, 76, 255)

    private val white = Color.rgb(242, 246, 255)
    private val muted = Color.rgb(139, 151, 177)

    private lateinit var root: FrameLayout
    private lateinit var content: LinearLayout
    private lateinit var title: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        showHome()
    }

    // ─────────────────────────────────────────────
    // BASE SCREEN
    // ─────────────────────────────────────────────

    private fun baseScreen(screenTitle: String) {

        root = FrameLayout(this)
        root.setBackgroundColor(bg)

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            overScrollMode = View.OVER_SCROLL_NEVER
            setBackgroundColor(bg)
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(104)
            )
        }

        scroll.addView(content)

        root.addView(
            scroll,
            FrameLayout.LayoutParams(
                -1,
                -1
            )
        )

        val bottom = bottomNav()

        root.addView(
            bottom,
            FrameLayout.LayoutParams(
                -1,
                dp(76),
                Gravity.BOTTOM
            ).apply {
                leftMargin = dp(10)
                rightMargin = dp(10)
                bottomMargin = dp(8)
            }
        )

        setContentView(root)

        if (screenTitle.isNotEmpty()) {

            title = tv(
                screenTitle,
                22f,
                white,
                true
            )

            content.addView(
                title,
                LinearLayout.LayoutParams(
                    -1,
                    -2
                ).apply {
                    bottomMargin = dp(18)
                }
            )
        }
    }

    // ─────────────────────────────────────────────
    // HOME
    // ─────────────────────────────────────────────

    private fun showHome() {

        baseScreen("")

        // Premium AERON header
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            setPadding(
                dp(14),
                dp(10),
                dp(10),
                dp(10)
            )

            background = rounded(panel2, 28)
        }

        header.addView(
            textCircle(
                "A",
                38,
                cyan
            )
        )

        val headText = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(11), 0, 0, 0)
        }

        headText.addView(
            tv(
                "AERON",
                15f,
                white,
                true
            )
        )

        headText.addView(
            tv(
                "AI COMPANION  •  CORE",
                9f,
                cyan,
                false
            )
        )

        header.addView(
            headText,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        header.addView(
            tv(
                "✦",
                22f,
                white,
                true
            ),
            LinearLayout.LayoutParams(
                dp(34),
                dp(34)
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        header.addView(
            textCircle(
                "A",
                32,
                blue
            )
        )

        content.addView(
            header,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                bottomMargin = dp(28)
            }
        )

        // Greeting
        content.addView(
            tv(
                "Good day.",
                15f,
                muted,
                false
            )
        )

        content.addView(
            tv(
                "How can AERON help?",
                28f,
                white,
                true
            ),
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                topMargin = dp(3)
                bottomMargin = dp(18)
            }
        )

        // CORE orb
        val orb = CoreOrbView(this)

        content.addView(
            orb,
            LinearLayout.LayoutParams(
                -1,
                dp(286)
            ).apply {
                bottomMargin = dp(12)
            }
        )

        // Status glass card
        val ready = card()

        ready.setPadding(
            dp(16),
            dp(14),
            dp(16),
            dp(14)
        )

        val readyTop = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        readyTop.addView(dot(cyan))

        readyTop.addView(
            tv(
                "AERON is ready",
                14f,
                white,
                true
            ),
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            ).apply {
                leftMargin = dp(8)
            }
        )

        readyTop.addView(
            tv(
                "ONLINE",
                9f,
                cyan,
                true
            )
        )

        ready.addView(readyTop)

        ready.addView(
            tv(
                "Wake word:  Hey AERON",
                11f,
                muted,
                false
            ),
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                topMargin = dp(7)
            }
        )

        ready.addView(
            WaveView(this),
            LinearLayout.LayoutParams(
                -1,
                dp(30)
            ).apply {
                topMargin = dp(6)
            }
        )

        content.addView(
            ready,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                bottomMargin = dp(24)
            }
        )

        // Quick actions
        content.addView(sectionTitle("QUICK ACTIONS"))

        val grid = GridLayout(this).apply {
            columnCount = 2
            rowCount = 2
        }

        grid.addView(
            action(
                "⌕",
                "Search",
                "Find anything"
            ) {
                showChat("Search")
            }
        )

        grid.addView(
            action(
                "✦",
                "Create",
                "Generate media"
            ) {
                showCreate()
            }
        )

        grid.addView(
            action(
                "◉",
                "Voice",
                "Talk to AERON"
            ) {
                showVoice()
            }
        )

        grid.addView(
            action(
                "▣",
                "Files",
                "Analyze files"
            ) {
                showChat("Files")
            }
        )

        content.addView(
            grid,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                bottomMargin = dp(26)
            }
        )

        // Recent
        content.addView(sectionTitle("RECENT"))

        recent(
            "Welcome to AERON",
            "Start a conversation with your AI companion."
        )

        recent(
            "AERON CORE",
            "Your intelligent assistant is ready."
        )

        recent(
            "Voice assistant",
            "Wake word: Hey AERON"
        )
    }

    // ─────────────────────────────────────────────
    // CHAT
    // ─────────────────────────────────────────────

    private fun showChat(mode: String = "") {

        baseScreen("AERON")

        content.addView(
            tv(
                if (mode.isEmpty())
                    "What would you like to know?"
                else
                    "$mode with AERON",
                26f,
                white,
                true
            )
        )

        content.addView(
            tv(
                "Ask anything. AERON will choose the right tools automatically.",
                13f,
                muted,
                false
            ),
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                topMargin = dp(8)
                bottomMargin = dp(26)
            }
        )

        val bubble = card()

        bubble.setPadding(
            dp(16),
            dp(16),
            dp(16),
            dp(16)
        )

        bubble.addView(
            tv(
                "AERON",
                11f,
                cyan,
                true
            )
        )

        bubble.addView(
            tv(
                "I'm ready. Tell me what you need.",
                16f,
                white,
                false
            ),
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                topMargin = dp(8)
            }
        )

        content.addView(
            bubble,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                bottomMargin = dp(18)
            }
        )

        val input = EditText(this).apply {

            hint = "Message AERON…"

            setHintTextColor(muted)

            setTextColor(white)

            textSize = 15f

            inputType =
                InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE

            maxLines = 4
            minLines = 1

            minHeight = dp(58)

            setPadding(
                dp(18),
                dp(12),
                dp(18),
                dp(12)
            )

            background = rounded(
                panel2,
                20
            )
        }

        content.addView(
            input,
            LinearLayout.LayoutParams(
                -1,
                dp(62)
            ).apply {
                bottomMargin = dp(14)
            }
        )

        val send = Button(this).apply {

            text = "SEND  ›"

            textSize = 12f

            setTextColor(bg)

            background = rounded(
                cyan,
                18
            )

            setOnClickListener {

                Toast.makeText(
                    this@MainActivity,
                    "Message ready for AERON",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        content.addView(
            send,
            LinearLayout.LayoutParams(
                -1,
                dp(52)
            )
        )
    }

    // ─────────────────────────────────────────────
    // VOICE
    // ─────────────────────────────────────────────

    private fun showVoice() {

        baseScreen("AERON LISTENING")

        content.addView(
            tv(
                "Speak naturally. AERON is listening.",
                14f,
                muted,
                false
            )
        )

        content.addView(
            CoreOrbView(
                this,
                true
            ),
            LinearLayout.LayoutParams(
                -1,
                dp(330)
            ).apply {
                topMargin = dp(12)
                bottomMargin = dp(14)
            }
        )

        val c = card()

        c.setPadding(
            dp(18),
            dp(18),
            dp(18),
            dp(18)
        )

        c.addView(
            tv(
                "WAKE WORD",
                10f,
                cyan,
                true
            )
        )

        c.addView(
            tv(
                "Hey AERON",
                22f,
                white,
                true
            ),
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                topMargin = dp(5)
            }
        )

        c.addView(
            tv(
                "The assistant can stay ready in the background when wake-word detection is active.",
                12f,
                muted,
                false
            ),
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                topMargin = dp(7)
            }
        )

        content.addView(c)
    }

    // ─────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────

    private fun showCreate() {

        baseScreen("CREATE")

        content.addView(
            tv(
                "Generate images, video and creative content with AERON.",
                14f,
                muted,
                false
            ),
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                bottomMargin = dp(24)
            }
        )

        bigAction(
            "✦",
            "Image Generation",
            "Create visuals from your idea."
        )

        bigAction(
            "▶",
            "Video Generation",
            "Turn ideas into moving scenes."
        )

        bigAction(
            "⌁",
            "Edit Media",
            "Transform an existing image or file."
        )
    }

    // ─────────────────────────────────────────────
    // MEMORY
    // ─────────────────────────────────────────────

    private fun showMemory() {

        baseScreen("MEMORY & PRIVACY")

        bigAction(
            "◈",
            "Memory Vault",
            "Control what AERON remembers."
        )

        bigAction(
            "⌁",
            "Chat History",
            "Review or manage conversations."
        )

        bigAction(
            "◉",
            "Privacy",
            "Manage data and personalization."
        )
    }

    // ─────────────────────────────────────────────
    // SETTINGS
    // ─────────────────────────────────────────────

    private fun showSettings() {

        baseScreen("SETTINGS")

        bigAction(
            "◉",
            "AERON CORE",
            "Assistant intelligence and behavior."
        )

        bigAction(
            "◌",
            "Voice & Wake Word",
            "Voice output and Hey AERON."
        )

        bigAction(
            "◐",
            "Appearance",
            "Theme and visual experience."
        )

        bigAction(
            "文",
            "Language & Region",
            "Choose your language."
        )

        bigAction(
            "ⓘ",
            "About AERON",
            "Version and project information."
        )
    }

    // ─────────────────────────────────────────────
    // ACCOUNT
    // ─────────────────────────────────────────────

    private fun showAccount() {

        baseScreen("ACCOUNT")

        val c = card()

        c.setPadding(
            dp(18),
            dp(18),
            dp(18),
            dp(18)
        )

        c.addView(
            textCircle(
                "A",
                58,
                cyan
            )
        )

        c.addView(
            tv(
                "AERON USER",
                20f,
                white,
                true
            ),
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                topMargin = dp(14)
            }
        )

        c.addView(
            tv(
                "Your personal AI companion",
                12f,
                muted,
                false
            ),
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                topMargin = dp(4)
            }
        )

        content.addView(c)
    }

    // ─────────────────────────────────────────────
    // BOTTOM NAV
    // ─────────────────────────────────────────────

    private fun bottomNav(): View {

        val bar = LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL

            gravity = Gravity.CENTER

            setPadding(
                dp(8),
                dp(8),
                dp(8),
                dp(8)
            )

            background = rounded(
                Color.rgb(6, 12, 24),
                24
            )
        }

        navItem(
            bar,
            "⌂",
            "Home"
        ) {
            showHome()
        }

        navItem(
            bar,
            "⌕",
            "Chat"
        ) {
            showChat()
        }

        navItem(
            bar,
            "✦",
            "Create"
        ) {
            showCreate()
        }

        navItem(
            bar,
            "◈",
            "Memory"
        ) {
            showMemory()
        }

        navItem(
            bar,
            "⚙",
            "Settings"
        ) {
            showSettings()
        }

        return bar
    }

    private fun navItem(
        parent: LinearLayout,
        icon: String,
        label: String,
        click: () -> Unit
    ) {

        val b = TextView(this).apply {

            text = "$icon\n$label"

            gravity = Gravity.CENTER

            textSize = 10f

            setTextColor(muted)

            setPadding(
                0,
                dp(3),
                0,
                dp(2)
            )

            setOnClickListener {
                click()
            }
        }

        parent.addView(
            b,
            LinearLayout.LayoutParams(
                0,
                -1,
                1f
            )
        )
    }

    // ─────────────────────────────────────────────
    // QUICK ACTION
    // ─────────────────────────────────────────────

    private fun action(
        icon: String,
        name: String,
        sub: String,
        click: () -> Unit
    ): View {

        val container = FrameLayout(this).apply {

            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = dp(88)

                columnSpec =
                    GridLayout.spec(
                        GridLayout.UNDEFINED,
                        1f
                    )

                setMargins(
                    dp(4),
                    dp(4),
                    dp(4),
                    dp(4)
                )
            }
        }

        val c = card().apply {

            setPadding(
                dp(14),
                dp(13),
                dp(12),
                dp(13)
            )

            setOnClickListener {
                click()
            }
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        row.addView(
            tv(
                icon,
                22f,
                cyan,
                true
            ),
            LinearLayout.LayoutParams(
                dp(32),
                -1
            )
        )

        val text = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        text.addView(
            tv(
                name,
                14f,
                white,
                true
            )
        )

        text.addView(
            tv(
                sub,
                10f,
                muted,
                false
            ),
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                topMargin = dp(3)
            }
        )

        row.addView(
            text,
            LinearLayout.LayoutParams(
                0,
                -1,
                1f
            )
        )

        c.addView(row)

        container.addView(
            c,
            FrameLayout.LayoutParams(
                -1,
                -1
            )
        )

        return container
    }

    // ─────────────────────────────────────────────
    // BIG SETTINGS ACTION
    // ─────────────────────────────────────────────

    private fun bigAction(
        icon: String,
        name: String,
        sub: String
    ) {

        val c = card().apply {
            setPadding(
                dp(16),
                dp(15),
                dp(16),
                dp(15)
            )
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        row.addView(
            tv(
                icon,
                25f,
                cyan,
                true
            ),
            LinearLayout.LayoutParams(
                dp(42),
                dp(48)
            )
        )

        val t = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        t.addView(
            tv(
                name,
                15f,
                white,
                true
            )
        )

        t.addView(
            tv(
                sub,
                11f,
                muted,
                false
            ),
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                topMargin = dp(4)
            }
        )

        row.addView(
            t,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        row.addView(
            tv(
                "›",
                24f,
                muted,
                false
            )
        )

        c.addView(row)

        content.addView(
            c,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                bottomMargin = dp(10)
            }
        )
    }

    // ─────────────────────────────────────────────
    // RECENT
    // ─────────────────────────────────────────────

    private fun recent(
        name: String,
        sub: String
    ) {

        val c = card().apply {
            setPadding(
                dp(14),
                dp(13),
                dp(14),
                dp(13)
            )
        }

        c.addView(
            tv(
                name,
                14f,
                white,
                true
            )
        )

        c.addView(
            tv(
                sub,
                11f,
                muted,
                false
            ),
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                topMargin = dp(4)
            }
        )

        content.addView(
            c,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                bottomMargin = dp(8)
            }
        )
    }

    // ─────────────────────────────────────────────
    // UI HELPERS
    // ─────────────────────────────────────────────

    private fun sectionTitle(
        s: String
    ): TextView =
        tv(
            s,
            10f,
            muted,
            true
        ).apply {

            letterSpacing = 0.14f

            setPadding(
                dp(2),
                0,
                0,
                dp(6)
            )
        }

    private fun card(): LinearLayout =
        LinearLayout(this).apply {

            orientation =
                LinearLayout.VERTICAL

            background =
                rounded(
                    panel,
                    20
                )
        }

    private fun dot(
        color: Int
    ): View =
        View(this).apply {

            background =
                rounded(
                    color,
                    50
                )

            layoutParams =
                LinearLayout.LayoutParams(
                    dp(8),
                    dp(8)
                )
        }

    private fun textCircle(
        text: String,
        size: Int,
        color: Int
    ): TextView =
        TextView(this).apply {

            this.text = text

            textSize =
                size * 0.40f

            gravity =
                Gravity.CENTER

            setTextColor(bg)

            typeface =
                Typeface.DEFAULT_BOLD

            background =
                rounded(
                    color,
                    50
                )

            layoutParams =
                LinearLayout.LayoutParams(
                    dp(size),
                    dp(size)
                )
        }

    private fun tv(
        text: String,
        size: Float,
        color: Int,
        bold: Boolean
    ): TextView =
        TextView(this).apply {

            this.text = text

            textSize = size

            setTextColor(color)

            typeface =
                if (bold)
                    Typeface.DEFAULT_BOLD
                else
                    Typeface.DEFAULT
        }

    private fun rounded(
        color: Int,
        radius: Int
    ): GradientDrawable =
        GradientDrawable().apply {

            setColor(color)

            cornerRadius =
                dp(radius).toFloat()
        }

    private fun dp(
        value: Int
    ): Int =
        (value * resources.displayMetrics.density)
            .toInt()

    // ─────────────────────────────────────────────
    // PREMIUM AERON CORE ORB
    // ─────────────────────────────────────────────

    private class CoreOrbView(
        context: Context,
        private val listening: Boolean = false
    ) : View(context) {

        private val paint =
            Paint(Paint.ANTI_ALIAS_FLAG)

        private var phase = 0f

        private val cyan =
            Color.rgb(0, 229, 168)

        private val blue =
            Color.rgb(67, 100, 255)

        private val purple =
            Color.rgb(143, 70, 255)

        init {

            setLayerType(
                View.LAYER_TYPE_SOFTWARE,
                null
            )

            ValueAnimator.ofFloat(
                0f,
                360f
            ).apply {

                duration = 9000

                repeatCount =
                    ValueAnimator.INFINITE

                addUpdateListener {

                    phase =
                        it.animatedValue as Float

                    invalidate()
                }

                start()
            }
        }

        override fun onDraw(
            c: Canvas
        ) {

            super.onDraw(c)

            val cx =
                width / 2f

            val cy =
                height / 2f

            val r =
                min(
                    width,
                    height
                ) * 0.27f

            // Glow
            paint.style =
                Paint.Style.FILL

            paint.shader =
                RadialGradient(
                    cx,
                    cy,
                    r * 2.0f,
                    intArrayOf(
                        Color.argb(
                            95,
                            40,
                            90,
                            255
                        ),
                        Color.argb(
                            35,
                            0,
                            229,
                            168
                        ),
                        Color.TRANSPARENT
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )

            c.drawCircle(
                cx,
                cy,
                r * 2.0f,
                paint
            )

            paint.shader = null

            // Orb rings
            paint.style =
                Paint.Style.STROKE

            for (i in 0..4) {

                val rr =
                    r + i * 18f

                paint.strokeWidth =
                    if (i == 0)
                        2.5f
                    else
                        1.1f

                paint.color =
                    if (i % 2 == 0)
                        Color.argb(
                            170,
                            0,
                            229,
                            168
                        )
                    else
                        Color.argb(
                            110,
                            92,
                            108,
                            255
                        )

                paint.setShadowLayer(
                    13f,
                    0f,
                    0f,
                    paint.color
                )

                val sweep =
                    250f + i * 25f

                c.save()

                c.rotate(
                    phase *
                        if (i % 2 == 0)
                            1f
                        else
                            -0.7f,
                    cx,
                    cy
                )

                c.drawArc(
                    cx - rr,
                    cy - rr,
                    cx + rr,
                    cy + rr,
                    -45f,
                    sweep,
                    false,
                    paint
                )

                c.restore()

                paint.clearShadowLayer()
            }

            // Main orb
            paint.style =
                Paint.Style.FILL

            paint.shader =
                LinearGradient(
                    cx - r,
                    cy - r,
                    cx + r,
                    cy + r,
                    intArrayOf(
                        blue,
                        purple,
                        Color.rgb(
                            24,
                            47,
                            110
                        )
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )

            paint.setShadowLayer(
                35f,
                0f,
                0f,
                Color.argb(
                    180,
                    54,
                    91,
                    255
                )
            )

            c.drawCircle(
                cx,
                cy,
                r,
                paint
            )

            paint.clearShadowLayer()

            paint.shader = null

            // AERON A
            paint.color =
                Color.WHITE

            paint.textAlign =
                Paint.Align.CENTER

            paint.typeface =
                Typeface.DEFAULT_BOLD

            paint.textSize =
                r * 0.72f

            c.drawText(
                "A",
                cx,
                cy + r * 0.25f,
                paint
            )

            // Core label
            paint.textSize = 11f

            paint.color =
                cyan

            c.drawText(
                if (listening)
                    "AERON LISTENING"
                else
                    "AERON CORE",
                cx,
                cy + r + 48f,
                paint
            )
        }
    }

    // ─────────────────────────────────────────────
    // ANIMATED WAVEFORM
    // ─────────────────────────────────────────────

    private class WaveView(
        context: Context
    ) : View(context) {

        private val p =
            Paint(Paint.ANTI_ALIAS_FLAG)

        private var phase =
            0f

        init {

            p.strokeWidth = 3f

            p.strokeCap =
                Paint.Cap.ROUND

            ValueAnimator.ofFloat(
                0f,
                6.28f
            ).apply {

                duration = 1200

                repeatCount =
                    ValueAnimator.INFINITE

                addUpdateListener {

                    phase =
                        it.animatedValue as Float

                    invalidate()
                }

                start()
            }
        }

        override fun onDraw(
            c: Canvas
        ) {

            super.onDraw(c)

            p.color =
                Color.rgb(
                    0,
                    229,
                    168
                )

            p.style =
                Paint.Style.STROKE

            val cy =
                height / 2f

            var lastX =
                0f

            var lastY =
                cy

            for (i in 0..80) {

                val x =
                    width * i / 80f

                val amp =
                    3f +
                        8f *
                        abs(
                            sin(
                                i * 0.42f +
                                    phase
                            )
                        )

                val y =
                    cy +
                        sin(
                            i * 0.55f +
                                phase
                        ) *
                        amp

                if (i > 0) {

                    c.drawLine(
                        lastX,
                        lastY,
                        x,
                        y,
                        p
                    )
                }

                lastX = x
                lastY = y
            }
        }
    }
}
