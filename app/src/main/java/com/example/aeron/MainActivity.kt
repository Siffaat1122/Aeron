             package com.example.aeron

import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val bg = Color.rgb(3, 6, 16)
    private val card = Color.rgb(9, 14, 28)
    private val card2 = Color.rgb(13, 20, 38)

    private val cyan = Color.rgb(0, 225, 255)
    private val blue = Color.rgb(74, 105, 255)
    private val purple = Color.rgb(157, 82, 255)
    private val green = Color.rgb(66, 235, 166)

    private val white = Color.WHITE
    private val primary = Color.rgb(225, 232, 247)
    private val secondary = Color.rgb(139, 151, 180)

    private lateinit var root: FrameLayout
    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = 0

        buildShell()
        showHome()
    }

    // =========================================================
    // SHELL
    // =========================================================

    private fun buildShell() {

        root = FrameLayout(this)
        root.setBackgroundColor(bg)

        val main = LinearLayout(this)
        main.orientation = LinearLayout.VERTICAL
        main.setBackgroundColor(bg)

        root.addView(
            main,
            FrameLayout.LayoutParams(-1, -1)
        )

        // PREMIUM HEADER
        val header = LinearLayout(this)
        header.orientation = LinearLayout.HORIZONTAL
        header.gravity = Gravity.CENTER_VERTICAL
        header.setPadding(dp(18), dp(10), dp(18), dp(8))

        val logo = TextView(this)
        logo.text = "A"
        logo.gravity = Gravity.CENTER
        logo.textSize = 17f
        logo.setTextColor(white)
        logo.typeface = Typeface.DEFAULT_BOLD
        logo.background = gradientCircle(cyan, blue)

        header.addView(
            logo,
            LinearLayout.LayoutParams(dp(42), dp(42))
        )

        header.addView(space(12, 1))

        val brand = LinearLayout(this)
        brand.orientation = LinearLayout.VERTICAL

        brand.addView(
            tv("AERON", 19f, white, Typeface.BOLD)
        )

        brand.addView(
            tv("AI COMPANION  •  CORE", 8f, cyan, Typeface.BOLD).apply {
                letterSpacing = .16f
            }
        )

        header.addView(
            brand,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        val sparkle = TextView(this)
        sparkle.text = "✦"
        sparkle.gravity = Gravity.CENTER
        sparkle.textSize = 19f
        sparkle.setTextColor(cyan)
        sparkle.background = circleBg()

        header.addView(
            sparkle,
            LinearLayout.LayoutParams(dp(42), dp(42))
        )

        header.addView(space(8, 1))

        val profile = TextView(this)
        profile.text = "A"
        profile.gravity = Gravity.CENTER
        profile.textSize = 15f
        profile.setTextColor(white)
        profile.typeface = Typeface.DEFAULT_BOLD
        profile.background = circleBg()

        profile.setOnClickListener {
            showAccount()
        }

        header.addView(
            profile,
            LinearLayout.LayoutParams(dp(42), dp(42))
        )

        main.addView(
            header,
            LinearLayout.LayoutParams(-1, dp(64))
        )

        // CONTENT
        content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(dp(18), dp(10), dp(18), dp(25))

        val scroll = ScrollView(this)
        scroll.isFillViewport = true
        scroll.setBackgroundColor(bg)
        scroll.addView(content)

        main.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        main.addView(bottomNav())

        setContentView(root)
    }

    // =========================================================
    // HOME
    // =========================================================

    private fun showHome() {

        clear()

        content.addView(
            tv("Good day.", 14f, cyan, Typeface.BOLD)
        )

        content.addView(space(6))

        content.addView(
            tv(
                "How can AERON help?",
                29f,
                white,
                Typeface.BOLD
            )
        )

        content.addView(space(20))

        // HERO ORB
        val hero = FrameLayout(this)

        hero.background = roundedGradient(
            intArrayOf(
                Color.rgb(7, 26, 53),
                Color.rgb(24, 13, 54),
                Color.rgb(4, 8, 20)
            ),
            30
        )

        hero.layoutParams = LinearLayout.LayoutParams(-1, dp(300))

        // outer rings
        for (size in listOf(220, 185, 150)) {

            val ring = TextView(this)

            ring.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.TRANSPARENT)
                setStroke(
                    dp(1),
                    when (size) {
                        220 -> Color.rgb(0, 100, 180)
                        185 -> Color.rgb(82, 80, 220)
                        else -> Color.rgb(0, 220, 255)
                    }
                )
            }

            hero.addView(
                ring,
                FrameLayout.LayoutParams(dp(size), dp(size)).apply {
                    gravity = Gravity.CENTER
                }
            )
        }

        val orb = TextView(this)
        orb.text = "A"
        orb.gravity = Gravity.CENTER
        orb.textSize = 55f
        orb.setTextColor(white)
        orb.typeface = Typeface.DEFAULT_BOLD

        orb.background = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(cyan, blue, purple)
        ).apply {
            shape = GradientDrawable.OVAL
            setStroke(dp(2), Color.WHITE)
        }

        orb.elevation = dp(15).toFloat()

        hero.addView(
            orb,
            FrameLayout.LayoutParams(dp(138), dp(138)).apply {
                gravity = Gravity.CENTER
            }
        )

        val core = tv(
            "AERON CORE",
            10f,
            cyan,
            Typeface.BOLD
        )

        core.gravity = Gravity.CENTER
        core.letterSpacing = .25f

        hero.addView(
            core,
            FrameLayout.LayoutParams(-1, -2).apply {
                gravity = Gravity.BOTTOM
                bottomMargin = dp(23)
            }
        )

        content.addView(hero)

        content.addView(space(14))

        // STATUS GLASS CARD
        val status = LinearLayout(this)
        status.orientation = LinearLayout.VERTICAL
        status.setPadding(dp(16), dp(14), dp(16), dp(14))

        status.background = rounded(
            card,
            19,
            dp(1),
            Color.rgb(30, 55, 88)
        )

        val statusTop = LinearLayout(this)
        statusTop.orientation = LinearLayout.HORIZONTAL
        statusTop.gravity = Gravity.CENTER_VERTICAL

        statusTop.addView(
            tv("●", 11f, green, Typeface.BOLD)
        )

        statusTop.addView(space(8))

        statusTop.addView(
            tv("AERON is ready", 13f, primary, Typeface.BOLD)
        )

        status.addView(statusTop)

        status.addView(space(7))

        status.addView(
            tv(
                "Wake word: Hey AERON",
                11f,
                secondary,
                Typeface.NORMAL
            )
        )

        status.addView(space(8))

        val wave = TextView(this)
        wave.text = "▁▂▃▅▃▂▁▂▅▃▂▁▃▅▂▁"
        wave.textSize = 14f
        wave.setTextColor(cyan)
        wave.gravity = Gravity.CENTER

        status.addView(wave)

        content.addView(status)

        content.addView(space(23))

        content.addView(
            tv("QUICK ACTIONS", 10f, secondary, Typeface.BOLD).apply {
                letterSpacing = .18f
            }
        )

        content.addView(space(10))

        // 2x2 QUICK ACTIONS
        val row1 = LinearLayout(this)
        row1.orientation = LinearLayout.HORIZONTAL

        row1.addView(
            quickCard(
                "⌕",
                "Search",
                "Find anything"
            ) {
                showChat("Search the web")
            }
        )

        row1.addView(space(10, 1))

        row1.addView(
            quickCard(
                "◇",
                "Create",
                "Generate media"
            ) {
                showCreate()
            }
        )

        content.addView(row1)

        content.addView(space(10))

        val row2 = LinearLayout(this)
        row2.orientation = LinearLayout.HORIZONTAL

        row2.addView(
            quickCard(
                "◉",
                "Voice",
                "Talk to AERON"
            ) {
                showVoice()
            }
        )

        row2.addView(space(10, 1))

        row2.addView(
            quickCard(
                "▣",
                "Files",
                "Analyze files"
            ) {
                toast("File analysis ready")
            }
        )

        content.addView(row2)

        content.addView(space(24))

        content.addView(
            tv("RECENT", 10f, secondary, Typeface.BOLD).apply {
                letterSpacing = .18f
            }
        )

        content.addView(space(10))

        recent("Welcome to AERON", "Your AI companion", "Now")
        recent("AERON CORE", "Ready for your next command", "Today")
    }

    // =========================================================
    // CHAT
    // =========================================================

    private fun showChat(message: String = "") {

        clear()

        content.addView(
            tv("CHAT", 28f, white, Typeface.BOLD)
        )

        content.addView(
            tv(
                "Intelligent conversation",
                12f,
                secondary,
                Typeface.NORMAL
            )
        )

        content.addView(space(20))

        val panel = panel()

        panel.addView(
            tv("✦", 25f, cyan, Typeface.BOLD)
        )

        panel.addView(space(8))

        panel.addView(
            tv(
                if (message.isEmpty())
                    "I'm ready. What would you like to do?"
                else
                    message,
                16f,
                primary,
                Typeface.NORMAL
            )
        )

        content.addView(panel)

        content.addView(space(15))

        suggestion("Ask anything", "Get an intelligent answer") {
            toast("Type your message below")
        }

        suggestion("Search the web", "Get current information") {
            toast("Web search ready")
        }

        suggestion("Create media", "Generate images and video") {
            showCreate()
        }

        content.addView(space(12))

        val composer = LinearLayout(this)
        composer.orientation = LinearLayout.HORIZONTAL
        composer.gravity = Gravity.CENTER_VERTICAL

        val input = EditText(this)
        input.hint = "Message AERON..."
        input.setHintTextColor(secondary)
        input.setTextColor(primary)
        input.textSize = 15f
        input.setPadding(dp(16), 0, dp(10), 0)

        input.background = rounded(
            card2,
            27,
            dp(1),
            Color.rgb(39, 57, 90)
        )

        composer.addView(
            input,
            LinearLayout.LayoutParams(0, dp(54), 1f)
        )

        composer.addView(space(8, 1))

        val send = circleAction("↑")

        send.setOnClickListener {
            val value = input.text.toString().trim()

            if (value.isNotEmpty()) {
                toast("AERON received your message")
                input.text.clear()
            }
        }

        composer.addView(send)

        content.addView(composer)
    }

    // =========================================================
    // VOICE
    // =========================================================

    private fun showVoice() {

        clear()

        content.addView(
            tv("VOICE", 28f, white, Typeface.BOLD)
        )

        content.addView(
            tv(
                "Talk naturally with AERON",
                12f,
                secondary,
                Typeface.NORMAL
            )
        )

        content.addView(space(22))

        val box = FrameLayout(this)

        box.background = roundedGradient(
            intArrayOf(
                Color.rgb(7, 25, 51),
                Color.rgb(26, 13, 56),
                Color.rgb(5, 8, 20)
            ),
            30
        )

        box.layoutParams = LinearLayout.LayoutParams(-1, dp(390))

        val ring = TextView(this)

        ring.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.TRANSPARENT)
            setStroke(dp(2), cyan)
        }

        box.addView(
            ring,
            FrameLayout.LayoutParams(dp(205), dp(205)).apply {
                gravity = Gravity.CENTER
            }
        )

        val orb = TextView(this)

        orb.text = "A"
        orb.gravity = Gravity.CENTER
        orb.textSize = 54f
        orb.setTextColor(white)
        orb.typeface = Typeface.DEFAULT_BOLD
        orb.background = gradientCircle(cyan, purple)

        box.addView(
            orb,
            FrameLayout.LayoutParams(dp(140), dp(140)).apply {
                gravity = Gravity.CENTER
            }
        )

        val label = tv(
            "AERON LISTENING",
            11f,
            cyan,
            Typeface.BOLD
        )

        label.gravity = Gravity.CENTER
        label.letterSpacing = .18f

        box.addView(
            label,
            FrameLayout.LayoutParams(-1, -2).apply {
                gravity = Gravity.BOTTOM
                bottomMargin = dp(45)
            }
        )

        content.addView(box)

        content.addView(space(18))

        val wake = panel()

        wake.addView(
            tv("WAKE WORD", 10f, cyan, Typeface.BOLD).apply {
                letterSpacing = .16f
            }
        )

        wake.addView(space(7))

        wake.addView(
            tv("Hey AERON", 21f, white, Typeface.BOLD)
        )

        wake.addView(
            tv(
                "Background wake-word detection is configured with your AERON model.",
                12f,
                secondary,
                Typeface.NORMAL
            )
        )

        content.addView(wake)
    }

    // =========================================================
    // CREATE
    // =========================================================

    private fun showCreate() {

        clear()

        content.addView(
            tv("CREATE", 28f, white, Typeface.BOLD)
        )

        content.addView(
            tv(
                "Bring your ideas to life",
                12f,
                secondary,
                Typeface.NORMAL
            )
        )

        content.addView(space(20))

        createCard("✦", "Image", "Generate stunning visuals")
        createCard("▶", "Video", "Create AI video")
        createCard("◇", "Edit", "Transform your media")
    }

    // =========================================================
    // MEMORY
    // =========================================================

    private fun showMemory() {

        clear()

        content.addView(
            tv("MEMORY", 28f, white, Typeface.BOLD)
        )

        content.addView(
            tv(
                "Your AERON memory vault",
                12f,
                secondary,
                Typeface.NORMAL
            )
        )

        content.addView(space(20))

        val vault = panel()

        vault.addView(
            tv("MEMORY VAULT", 10f, cyan, Typeface.BOLD)
        )

        vault.addView(space(8))

        vault.addView(
            tv(
                "Manage information AERON remembers about your conversations and preferences.",
                14f,
                primary,
                Typeface.NORMAL
            )
        )

        content.addView(vault)

        content.addView(space(12))

        settingItem("Preferences", "Personal preferences")
        settingItem("Conversations", "Remembered context")
        settingItem("Privacy", "Control what AERON remembers")
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    private fun showSettings() {

        clear()

        content.addView(
            tv("SETTINGS", 28f, white, Typeface.BOLD)
        )

        content.addView(
            tv(
                "Customize your AERON experience",
                12f,
                secondary,
                Typeface.NORMAL
            )
        )

        content.addView(space(15))

        section("ACCOUNT")

        settingItem(
            "Account",
            "Google, Facebook or phone"
        ) {
            showAccount()
        }

        section("AERON CORE")

        settingItem(
            "AERON CORE",
            "Personality and intelligence"
        ) {
            toast("AERON CORE")
        }

        section("VOICE & WAKE WORD")

        settingItem(
            "Voice & Wake Word",
            "Hey AERON detection"
        ) {
            showVoice()
        }

        section("APPEARANCE")

        settingItem(
            "Appearance",
            "Theme and visual experience"
        )

        settingItem(
            "Memory",
            "Manage Memory Vault"
        ) {
            showMemory()
        }

        settingItem(
            "Chat History",
            "Saved conversations"
        )

        section("LANGUAGE & REGION")

        settingItem(
            "Language & Region",
            "App and speech language"
        )

        section("PRIVACY & SECURITY")

        settingItem(
            "Privacy & Security",
            "Data and permissions"
        )

        section("ABOUT")

        settingItem(
            "About AERON",
            "Version and information"
        ) {
            toast("AERON v1.0")
        }
    }

    // =========================================================
    // ACCOUNT
    // =========================================================

    private fun showAccount() {

        clear()

        content.addView(
            tv("AERON", 14f, cyan, Typeface.BOLD)
        )

        content.addView(space(5))

        content.addView(
            tv("Your AI Assistant", 28f, white, Typeface.BOLD)
        )

        content.addView(space(8))

        content.addView(
            tv(
                "Sign in to sync your AERON experience.",
                13f,
                secondary,
                Typeface.NORMAL
            )
        )

        content.addView(space(25))

        loginButton("G", "Continue with Google")
        loginButton("f", "Continue with Facebook")
        loginButton("☎", "Continue with Phone")

        content.addView(space(18))

        val guest = TextView(this)
        guest.text = "Continue as Guest"
        guest.gravity = Gravity.CENTER
        guest.textSize = 13f
        guest.setTextColor(cyan)

        guest.setOnClickListener {
            showHome()
        }

        content.addView(
            guest,
            LinearLayout.LayoutParams(-1, dp(50))
        )
    }

    // =========================================================
    // BOTTOM NAV
    // =========================================================

    private fun bottomNav(): View {

        val nav = LinearLayout(this)
        nav.orientation = LinearLayout.HORIZONTAL
        nav.gravity = Gravity.CENTER
        nav.setPadding(dp(8), dp(5), dp(8), dp(8))
        nav.setBackgroundColor(bg)

        nav.addView(navItem("⌂", "Home") { showHome() })
        nav.addView(navItem("✦", "Chat") { showChat() })
        nav.addView(navItem("◉", "Voice") { showVoice() })
        nav.addView(navItem("◇", "Memory") { showMemory() })
        nav.addView(navItem("☰", "Settings") { showSettings() })

        return nav
    }

    private fun navItem(
        icon: String,
        label: String,
        action: () -> Unit
    ): View {

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.gravity = Gravity.CENTER
        box.setPadding(dp(5), dp(4), dp(5), dp(3))

        box.setOnClickListener {
            action()
        }

        val i = tv(icon, 20f, cyan, Typeface.BOLD)
        i.gravity = Gravity.CENTER

        val t = tv(label, 9f, secondary, Typeface.BOLD)
        t.gravity = Gravity.CENTER

        box.addView(i)
        box.addView(t)

        box.layoutParams = LinearLayout.LayoutParams(
            0,
            dp(58),
            1f
        )

        return box
    }

    // =========================================================
    // QUICK CARD
    // =========================================================

    private fun quickCard(
        icon: String,
        title: String,
        subtitle: String,
        action: () -> Unit
    ): View {

        val cardView = LinearLayout(this)
        cardView.orientation = LinearLayout.VERTICAL
        cardView.setPadding(dp(15), dp(14), dp(15), dp(13))

        cardView.background = rounded(
            card,
            19,
            dp(1),
            Color.rgb(28, 48, 80)
        )

        cardView.setOnClickListener {
            action()
        }

        cardView.addView(
            tv(icon, 24f, cyan, Typeface.BOLD)
        )

        cardView.addView(space(9))

        cardView.addView(
            tv(title, 14f, white, Typeface.BOLD)
        )

        cardView.addView(
            tv(subtitle, 10f, secondary, Typeface.NORMAL)
        )

        cardView.layoutParams = LinearLayout.LayoutParams(
            0,
            dp(118),
            1f
        )

        return cardView
    }

    // =========================================================
    // RECENT
    // =========================================================

    private fun recent(
        title: String,
        subtitle: String,
        time: String
    ) {

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(dp(14), dp(13), dp(14), dp(13))

        row.background = rounded(
            card,
            17,
            dp(1),
            Color.rgb(25, 43, 73)
        )

        val icon = tv("✦", 18f, cyan, Typeface.BOLD)

        row.addView(icon)
        row.addView(space(11))

        val textBox = LinearLayout(this)
        textBox.orientation = LinearLayout.VERTICAL

        textBox.addView(
            tv(title, 13f, primary, Typeface.BOLD)
        )

        textBox.addView(
            tv(subtitle, 10f, secondary, Typeface.NORMAL)
        )

        row.addView(
            textBox,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        row.addView(
            tv(time, 9f, secondary, Typeface.NORMAL)
        )

        val p = LinearLayout.LayoutParams(-1, -2)
        p.bottomMargin = dp(8)

        content.addView(row, p)
    }

    // =========================================================
    // SUGGESTION
    // =========================================================

    private fun suggestion(
        title: String,
        subtitle: String,
        action: () -> Unit
    ) {

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(dp(15), dp(13), dp(15), dp(13))

        row.background = rounded(
            card,
            17,
            dp(1),
            Color.rgb(27, 45, 76)
        )

        row.setOnClickListener {
            action()
        }

        row.addView(
            tv("✦", 18f, purple, Typeface.BOLD)
        )

        row.addView(space(11))

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL

        box.addView(
            tv(title, 14f, primary, Typeface.BOLD)
        )

        box.addView(
            tv(subtitle, 10f, secondary, Typeface.NORMAL)
        )

        row.addView(box)

        val p = LinearLayout.LayoutParams(-1, -2)
        p.bottomMargin = dp(8)

        content.addView(row, p)
    }

    // =========================================================
    // CREATE CARD
    // =========================================================

    private fun createCard(
        icon: String,
        title: String,
        subtitle: String
    ) {

        val item = panel()

        item.setOnClickListener {
            toast("$title selected")
        }

        item.addView(
            tv(icon, 27f, cyan, Typeface.BOLD)
        )

        item.addView(space(4))

        item.addView(
            tv(title, 19f, white, Typeface.BOLD)
        )

        item.addView(
            tv(subtitle, 12f, secondary, Typeface.NORMAL)
        )

        val p = LinearLayout.LayoutParams(-1, dp(110))
        p.bottomMargin = dp(12)

        content.addView(item, p)
    }

    // =========================================================
    // SETTINGS ITEM
    // =========================================================

    private fun settingItem(
        title: String,
        subtitle: String,
        action: (() -> Unit)? = null
    ) {

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(dp(16), dp(14), dp(12), dp(14))

        row.background = rounded(
            card,
            18,
            dp(1),
            Color.rgb(27, 44, 74)
        )

        if (action != null) {
            row.setOnClickListener {
                action()
            }
        }

        val textBox = LinearLayout(this)
        textBox.orientation = LinearLayout.VERTICAL

        textBox.addView(
            tv(title, 14f, primary, Typeface.BOLD)
        )

        textBox.addView(
            tv(subtitle, 10f, secondary, Typeface.NORMAL)
        )

        row.addView(
            textBox,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        row.addView(
            tv("›", 25f, secondary, Typeface.NORMAL)
        )

        val p = LinearLayout.LayoutParams(-1, -2)
        p.bottomMargin = dp(8)

        content.addView(row, p)
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private fun loginButton(
        icon: String,
        title: String
    ) {

        val button = LinearLayout(this)
        button.orientation = LinearLayout.HORIZONTAL
        button.gravity = Gravity.CENTER_VERTICAL
        button.setPadding(dp(18), 0, dp(18), 0)

        button.background = rounded(
            card2,
            18,
            dp(1),
            Color.rgb(39, 57, 91)
        )

        val i = tv(icon, 19f, white, Typeface.BOLD)
        i.gravity = Gravity.CENTER

        button.addView(
            i,
            LinearLayout.LayoutParams(dp(38), dp(55))
        )

        button.addView(space(10))

        button.addView(
            tv(title, 14f, primary, Typeface.BOLD)
        )

        val p = LinearLayout.LayoutParams(-1, dp(55))
        p.bottomMargin = dp(10)

        content.addView(button, p)
    }

    // =========================================================
    // PANEL
    // =========================================================

    private fun panel(): LinearLayout {

        val p = LinearLayout(this)
        p.orientation = LinearLayout.VERTICAL
        p.setPadding(dp(16), dp(16), dp(16), dp(16))

        p.background = rounded(
            card,
            20,
            dp(1),
            Color.rgb(29, 48, 81)
        )

        return p
    }

    // =========================================================
    // SECTION
    // =========================================================

    private fun section(title: String) {

        content.addView(space(13))

        content.addView(
            tv(title, 9f, cyan, Typeface.BOLD).apply {
                letterSpacing = .18f
            }
        )

        content.addView(space(7))
    }

    // =========================================================
    // GRAPHICS
    // =========================================================

    private fun gradientCircle(
        c1: Int,
        c2: Int
    ): GradientDrawable {

        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(c1, c2)
        ).apply {
            shape = GradientDrawable.OVAL
        }
    }

    private fun circleBg(): GradientDrawable {

        return rounded(
            Color.rgb(12, 21, 39),
            50,
            dp(1),
            Color.rgb(38, 57, 90)
        )
    }

    private fun circleAction(symbol: String): TextView {

        val v = TextView(this)
        v.text = symbol
        v.gravity = Gravity.CENTER
        v.textSize = 18f
        v.setTextColor(white)
        v.typeface = Typeface.DEFAULT_BOLD
        v.background = gradientCircle(blue, purple)

        v.layoutParams = LinearLayout.LayoutParams(
            dp(52),
            dp(52)
        )

        return v
    }

    private fun roundedGradient(
        colors: IntArray,
        radius: Int
    ): GradientDrawable {

        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            colors
        ).apply {
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), Color.rgb(31, 60, 96))
        }
    }

    private fun rounded(
        color: Int,
        radius: Int,
        stroke: Int,
        strokeColor: Int
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()

            if (stroke > 0) {
                setStroke(stroke, strokeColor)
            }
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private fun tv(
        value: String,
        size: Float,
        color: Int,
        style: Int
    ): TextView {

        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            typeface = Typeface.create("sans-serif", style)
            includeFontPadding = false
        }
    }

    private fun space(
        size: Int,
        weight: Int = 0
    ): View {

        return Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                dp(size),
                if (weight == 1) 1 else dp(size)
            )
        }
    }

    private fun clear() {
        content.removeAllViews()
    }

    private fun dp(value: Int): Int {
        return (
            value * resources.displayMetrics.density
        ).toInt()
    }

    private fun toast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
