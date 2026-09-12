package com.example.aeron

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // ---------- AERON COLORS ----------
    private val bg = Color.rgb(3, 6, 16)
    private val surface = Color.rgb(9, 14, 28)
    private val surface2 = Color.rgb(14, 21, 40)
    private val cyan = Color.rgb(0, 225, 255)
    private val blue = Color.rgb(78, 112, 255)
    private val purple = Color.rgb(153, 86, 255)
    private val white = Color.WHITE
    private val text = Color.rgb(225, 232, 247)
    private val muted = Color.rgb(137, 151, 179)
    private val green = Color.rgb(70, 235, 170)

    private lateinit var root: FrameLayout
    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        buildShell()
        showHome()
    }

    // ============================================================
    // SHELL
    // ============================================================
private fun buildShell() {

    root = FrameLayout(this)
    root.setBackgroundColor(bg)

    val main = LinearLayout(this)
    main.orientation = LinearLayout.VERTICAL
    main.setBackgroundColor(bg)

    root.addView(
        main,
        FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
    )

    // AERON TOP BAR — transparent, no white box
    val header = LinearLayout(this)
    header.orientation = LinearLayout.HORIZONTAL
    header.gravity = Gravity.CENTER_VERTICAL
    header.setPadding(dp(20), dp(10), dp(20), dp(8))
    header.setBackgroundColor(Color.TRANSPARENT)

    val brand = LinearLayout(this)
    brand.orientation = LinearLayout.VERTICAL

    val title = tv(
        "AERON",
        20f,
        white,
        Typeface.BOLD
    )

    val subtitle = tv(
        "AI ASSISTANT",
        9f,
        cyan,
        Typeface.BOLD
    )
    subtitle.letterSpacing = 0.18f

    brand.addView(title)
    brand.addView(subtitle)

    header.addView(
        brand,
        LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
    )

    val profile = circleButton("A")
    profile.setOnClickListener {
        showAccount()
    }

    header.addView(profile)

    main.addView(
        header,
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(62)
        )
    )

    // CONTENT
    content = LinearLayout(this)
    content.orientation = LinearLayout.VERTICAL
    content.setPadding(
        dp(18),
        dp(8),
        dp(18),
        dp(12)
    )

    val scroll = ScrollView(this)
    scroll.isFillViewport = true
    scroll.setBackgroundColor(Color.TRANSPARENT)
    scroll.addView(content)

    main.addView(
        scroll,
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        )
    )

    // BOTTOM NAVIGATION
    main.addView(bottomNavigation())

    setContentView(root)
}
    (

    // ============================================================
    // HOME
    // ============================================================

    private fun showHome() {

    clear()

    // Greeting
    content.addView(
        tv(
            "Hey AERON",
            14f,
            cyan,
            Typeface.BOLD
        )
    )

    content.addView(space(5))

    content.addView(
        tv(
            "How can I help you today?",
            27f,
            white,
            Typeface.BOLD
        )
    )

    content.addView(space(20))

    // AERON CORE ORB
    val orbBox = FrameLayout(this)

    orbBox.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        dp(285)
    )

    orbBox.background = GradientDrawable(
        GradientDrawable.Orientation.TL_BR,
        intArrayOf(
            Color.rgb(8, 30, 58),
            Color.rgb(25, 12, 55),
            Color.rgb(4, 9, 22)
        )
    ).apply {
        cornerRadius = dp(30).toFloat()
        setStroke(dp(1), Color.rgb(35, 70, 115))
    }

    // Outer glow ring
    val glowRing = TextView(this)
    glowRing.background = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(Color.TRANSPARENT)
        setStroke(dp(2), Color.rgb(0, 190, 255))
    }

    orbBox.addView(
        glowRing,
        FrameLayout.LayoutParams(
            dp(185),
            dp(185)
        ).apply {
            gravity = Gravity.CENTER
        }
    )

    // Main orb
    val orb = TextView(this)
    orb.text = "A"
    orb.gravity = Gravity.CENTER
    orb.textSize = 60f
    orb.setTextColor(white)
    orb.typeface = Typeface.DEFAULT_BOLD

    orb.background = GradientDrawable(
        GradientDrawable.Orientation.TL_BR,
        intArrayOf(
            Color.rgb(0, 190, 255),
            Color.rgb(90, 70, 255),
            Color.rgb(160, 60, 255)
        )
    ).apply {
        shape = GradientDrawable.OVAL
        setStroke(dp(2), Color.WHITE)
    }

    orb.elevation = dp(20).toFloat()

    orbBox.addView(
        orb,
        FrameLayout.LayoutParams(
            dp(145),
            dp(145)
        ).apply {
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
    core.letterSpacing = 0.22f

    orbBox.addView(
        core,
        FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM
            bottomMargin = dp(25)
        }
    )

    content.addView(orbBox)

    content.addView(space(18))

    // STATUS
    val status = LinearLayout(this)
    status.orientation = LinearLayout.HORIZONTAL
    status.gravity = Gravity.CENTER_VERTICAL
    status.setPadding(dp(16), dp(13), dp(16), dp(13))

    status.background = rounded(
        surface,
        18,
        dp(1),
        Color.rgb(28, 52, 82)
    )

    val dot = tv(
        "●",
        11f,
        green,
        Typeface.BOLD
    )

    status.addView(dot)
    status.addView(space(8))

    status.addView(
        tv(
            "AERON is ready",
            13f,
            text,
            Typeface.BOLD
        )
    )

    val wake = tv(
        "Hey AERON",
        11f,
        muted,
        Typeface.NORMAL
    )
    wake.gravity = Gravity.CENTER_VERTICAL

    status.addView(
        wake,
        LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ).apply {
            gravity = Gravity.END
        }
    )

    content.addView(status)

    content.addView(space(22))

    // QUICK ACTIONS
    content.addView(
        tv(
            "QUICK ACTIONS",
            10f,
            muted,
            Typeface.BOLD
        ).apply {
            letterSpacing = 0.18f
        }
    )

    content.addView(space(10))

    val actions = LinearLayout(this)
    actions.orientation = LinearLayout.HORIZONTAL

    actions.addView(
        actionCard(
            "⌕",
            "Search",
            "Web",
        ) {
            showChat("Search the web for ")
        }
    )

    actions.addView(space(8))

    actions.addView(
        actionCard(
            "◇",
            "Image",
            "Create",
        ) {
            showCreate()
        }
    )

    actions.addView(space(8))

    actions.addView(
        actionCard(
            "▶",
            "Video",
            "Create",
        ) {
            showCreate()
        }
    )

    actions.addView(space(8))

    actions.addView(
        actionCard(
            "▣",
            "File",
            "Analyze",
        ) {
            toast("File analysis coming next")
        }
    )

    content.addView(actions)

    content.addView(space(22))

    // RECENT
    content.addView(
        tv(
            "RECENT CHATS",
            10f,
            muted,
            Typeface.BOLD
        ).apply {
            letterSpacing = 0.18f
        }
    )

    content.addView(space(10))

    recent(
        "Welcome to AERON",
        "Your futuristic AI companion",
        "Today"
    )

    recent(
        "AERON CORE",
        "Ready for your next command",
        "Today"
    )
    }


    // ============================================================
    // CHAT
    // ============================================================

    private fun showChat(prompt: String = "") {

        clear()

        content.addView(
            tv(
                "AERON",
                28f,
                white,
                Typeface.BOLD
            )
        )

        content.addView(
            tv(
                "Intelligent conversation",
                12f,
                muted,
                Typeface.NORMAL
            )
        )

        content.addView(space(22))

        val welcome = panel()

        welcome.addView(
            tv(
                "✦",
                24f,
                cyan,
                Typeface.BOLD
            )
        )

        welcome.addView(space(8))

        welcome.addView(
            tv(
                if (prompt.isEmpty())
                    "I'm ready. What would you like to do?"
                else
                    prompt,
                17f,
                text,
                Typeface.NORMAL
            )
        )

        content.addView(welcome)

        content.addView(space(16))

        val suggestions = LinearLayout(this)
        suggestions.orientation = LinearLayout.VERTICAL

        suggestion("Explain something", "Ask AERON anything") {
            toast("Ask your question in the composer")
        }

        suggestion("Search the web", "Get current information") {
            toast("Web search ready")
        }

        suggestion("Create something", "Images, video and more") {
            showCreate()
        }

        content.addView(suggestions)

        content.addView(space(18))

        val composer = LinearLayout(this)
        composer.orientation = LinearLayout.HORIZONTAL
        composer.gravity = Gravity.CENTER_VERTICAL

        val input = EditText(this)
        input.hint = "Message AERON..."
        input.setHintTextColor(muted)
        input.setTextColor(text)
        input.textSize = 15f
        input.setPadding(dp(16), 0, dp(10), 0)
        input.background = rounded(surface2, 26, dp(1), Color.rgb(36, 53, 85))

        composer.addView(
            input,
            LinearLayout.LayoutParams(
                0,
                dp(54),
                1f
            )
        )

        composer.addView(space(8))

        val send = circleButton("↑")
        send.setOnClickListener {
            val value = input.text.toString().trim()
            if (value.isNotEmpty()) {
                toast("AERON received: $value")
                input.text.clear()
            }
        }

        composer.addView(send)

        content.addView(composer)
    }

    // ============================================================
    // VOICE
    // ============================================================

    private fun showVoice() {

        clear()

        content.addView(
            tv(
                "VOICE",
                28f,
                white,
                Typeface.BOLD
            )
        )

        content.addView(
            tv(
                "Speak naturally with AERON",
                12f,
                muted,
                Typeface.NORMAL
            )
        )

        content.addView(space(30))

        val voiceBox = FrameLayout(this)
        voiceBox.background = rounded(
            surface,
            30,
            dp(1),
            Color.rgb(30, 47, 80)
        )

        voiceBox.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(390)
        )

        val voiceOrb = TextView(this)
        voiceOrb.text = "A"
        voiceOrb.gravity = Gravity.CENTER
        voiceOrb.textSize = 58f
        voiceOrb.setTextColor(white)
        voiceOrb.typeface = Typeface.DEFAULT_BOLD

        val orbBg = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(cyan, purple)
        )
        orbBg.shape = GradientDrawable.OVAL
        orbBg.setStroke(dp(3), Color.WHITE)
        voiceOrb.background = orbBg

        voiceBox.addView(
            voiceOrb,
            FrameLayout.LayoutParams(
                dp(145),
                dp(145)
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        val listening = tv(
            "LISTENING READY",
            11f,
            cyan,
            Typeface.BOLD
        )
        listening.gravity = Gravity.CENTER
        listening.letterSpacing = 0.16f

        voiceBox.addView(
            listening,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM
                bottomMargin = dp(48)
            }
        )

        content.addView(voiceBox)

        content.addView(space(20))

        val wake = panel()

        wake.addView(
            tv(
                "WAKE WORD",
                11f,
                muted,
                Typeface.BOLD
            ).apply {
                letterSpacing = 0.15f
            }
        )

        wake.addView(
            tv(
                "Hey AERON",
                21f,
                white,
                Typeface.BOLD
            )
        )

        wake.addView(
            tv(
                "AERON can listen for your custom wake phrase.",
                12f,
                muted,
                Typeface.NORMAL
            )
        )

        content.addView(wake)
    }

    // ============================================================
    // CREATE
    // ============================================================

    private fun showCreate() {

        clear()

        content.addView(
            tv(
                "CREATE",
                28f,
                white,
                Typeface.BOLD
            )
        )

        content.addView(
            tv(
                "Bring your ideas to life",
                12f,
                muted,
                Typeface.NORMAL
            )
        )

        content.addView(space(22))

        createCard(
            "✦",
            "Image",
            "Generate stunning visuals"
        )

        createCard(
            "▶",
            "Video",
            "Create AI video"
        )

        createCard(
            "◇",
            "Creative",
            "Turn ideas into something new"
        )
    }

    // ============================================================
    // MEMORY
    // ============================================================

    private fun showMemory() {

        clear()

        content.addView(
            tv(
                "MEMORY",
                28f,
                white,
                Typeface.BOLD
            )
        )

        content.addView(
            tv(
                "Your AERON memory vault",
                12f,
                muted,
                Typeface.NORMAL
            )
        )

        content.addView(space(20))

        val vault = panel()

        vault.addView(
            tv(
                "MEMORY VAULT",
                11f,
                cyan,
                Typeface.BOLD
            ).apply {
                letterSpacing = 0.15f
            }
        )

        vault.addView(space(8))

        vault.addView(
            tv(
                "Your saved preferences and useful information will appear here.",
                15f,
                text,
                Typeface.NORMAL
            )
        )

        content.addView(vault)

        content.addView(space(14))

        memoryItem("Preferences", "Personal preferences")
        memoryItem("Conversations", "Important remembered context")
        memoryItem("Privacy", "Control what AERON remembers")
    }

    // ============================================================
    // SETTINGS
    // ============================================================

    private fun showSettings() {

        clear()

        content.addView(
            tv(
                "SETTINGS",
                28f,
                white,
                Typeface.BOLD
            )
        )

        content.addView(
            tv(
                "Customize your AERON experience",
                12f,
                muted,
                Typeface.NORMAL
            )
        )

        content.addView(space(22))

        settingSection("ACCOUNT")
        settingItem("Account", "Sign in with Google, Facebook or phone") {
            showAccount()
        }

        settingSection("AERON CORE")
        settingItem("AERON CORE", "AI personality and intelligence") {
            toast("AERON CORE settings")
        }

        settingSection("VOICE & WAKE WORD")
        settingItem("Voice & Wake Word", "Hey AERON detection") {
            showVoice()
        }

        settingSection("PERSONALIZATION")
        settingItem("Appearance", "Theme and visual experience") {
            toast("Appearance settings")
        }

        settingItem("Memory", "Manage AERON memory") {
            showMemory()
        }

        settingItem("Chat History", "Saved conversations") {
            toast("Chat history")
        }

        settingSection("REGION")
        settingItem("Language & Region", "Choose your language") {
            toast("Language settings")
        }

        settingSection("SECURITY")
        settingItem("Privacy & Security", "Your data and permissions") {
            toast("Privacy settings")
        }

        settingSection("ABOUT")
        settingItem("About AERON", "Version and information") {
            toast("AERON v1.0")
        }
    }

    // ============================================================
    // ACCOUNT
    // ============================================================

    private fun showAccount() {

        clear()

        content.addView(
            tv(
                "WELCOME TO AERON",
                25f,
                white,
                Typeface.BOLD
            )
        )

        content.addView(
            tv(
                "Sign in to sync your AERON experience.",
                13f,
                muted,
                Typeface.NORMAL
            )
        )

        content.addView(space(25))

        loginButton(
            "G",
            "Continue with Google"
        ) {
            toast("Google sign-in will be connected next")
        }

        loginButton(
            "f",
            "Continue with Facebook"
        ) {
            toast("Facebook sign-in will be connected next")
        }

        loginButton(
            "☎",
            "Continue with phone"
        ) {
            toast("Phone verification will be connected next")
        }

        content.addView(space(25))

        val note = panel()

        note.addView(
            tv(
                "AERON ACCOUNT",
                11f,
                cyan,
                Typeface.BOLD
            )
        )

        note.addView(
            tv(
                "Login UI is ready. Real authentication will be connected separately.",
                13f,
                muted,
                Typeface.NORMAL
            )
        )

        content.addView(note)
    }

    // ============================================================
    // BOTTOM NAVIGATION
    // ============================================================

    private fun bottomNavigation(): View {

        val nav = LinearLayout(this)
        nav.orientation = LinearLayout.HORIZONTAL
        nav.gravity = Gravity.CENTER
        nav.setPadding(dp(10), dp(8), dp(10), dp(10))
        nav.background = rounded(surface, 0, 0, Color.TRANSPARENT)

        nav.addView(navButton("⌂", "Home") {
            showHome()
        })

        nav.addView(navButton("✦", "Chat") {
            showChat()
        })

        nav.addView(navButton("◉", "Voice") {
            showVoice()
        })

        nav.addView(navButton("◇", "Memory") {
            showMemory()
        })

        nav.addView(navButton("☰", "Settings") {
            showSettings()
        })

        return nav
    }

    private fun navButton(
        icon: String,
        label: String,
        action: () -> Unit
    ): View {

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.gravity = Gravity.CENTER
        box.setPadding(dp(8), dp(6), dp(8), dp(4))
        box.setOnClickListener { action() }

        val i = tv(
            icon,
            20f,
            cyan,
            Typeface.BOLD
        )
        i.gravity = Gravity.CENTER

        val t = tv(
            label,
            9f,
            muted,
            Typeface.BOLD
        )
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

    // ============================================================
    // UI COMPONENTS
    // ============================================================

    private fun panel(): LinearLayout {

        val p = LinearLayout(this)
        p.orientation = LinearLayout.VERTICAL
        p.setPadding(dp(16), dp(16), dp(16), dp(16))
        p.background = rounded(
            surface,
            20,
            dp(1),
            Color.rgb(29, 46, 78)
        )

        p.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        return p
    }

    private fun actionCard(
        icon: String,
        title: String,
        subtitle: String,
        action: () -> Unit
    ): View {

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(dp(15), dp(15), dp(15), dp(14))
        card.background = rounded(
            surface,
            20,
            dp(1),
            Color.rgb(28, 46, 79)
        )
        card.setOnClickListener { action() }

        val iconView = tv(
            icon,
            25f,
            cyan,
            Typeface.BOLD
        )

        card.addView(iconView)

        card.addView(space(10))

        card.addView(
            tv(
                title,
                15f,
                white,
                Typeface.BOLD
            )
        )

        card.addView(
            tv(
                subtitle,
                11f,
                muted,
                Typeface.NORMAL
            )
        )

        card.layoutParams = LinearLayout.LayoutParams(
            0,
            dp(125),
            1f
        )

        return card
    }

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
            surface,
            16,
            dp(1),
            Color.rgb(24, 39, 67)
        )

        val icon = tv(
            "✦",
            18f,
            cyan,
            Typeface.BOLD
        )

        row.addView(icon)
        row.addView(space(12))

        val texts = LinearLayout(this)
        texts.orientation = LinearLayout.VERTICAL

        texts.addView(
            tv(
                title,
                14f,
                text,
                Typeface.BOLD
            )
        )

        texts.addView(
            tv(
                subtitle,
                11f,
                muted,
                Typeface.NORMAL
            )
        )

        row.addView(
            texts,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        row.addView(
            tv(
                time,
                10f,
                muted,
                Typeface.NORMAL
            )
        )

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = dp(8)

        content.addView(row, params)
    }

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
            surface,
            16,
            dp(1),
            Color.rgb(27, 43, 72)
        )
        row.setOnClickListener { action() }

        val icon = tv(
            "✦",
            18f,
            purple,
            Typeface.BOLD
        )

        row.addView(icon)
        row.addView(space(12))

        val textBox = LinearLayout(this)
        textBox.orientation = LinearLayout.VERTICAL

        textBox.addView(
            tv(title, 14f, text, Typeface.BOLD)
        )

        textBox.addView(
            tv(subtitle, 11f, muted, Typeface.NORMAL)
        )

        row.addView(textBox)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = dp(8)

        content.addView(row, params)
    }

    private fun createCard(
        icon: String,
        title: String,
        subtitle: String
    ) {

        val card = panel()
        card.setOnClickListener {
            toast("$title creation ready")
        }

        card.addView(
            tv(
                icon,
                28f,
                cyan,
                Typeface.BOLD
            )
        )

        card.addView(
            tv(
                title,
                19f,
                white,
                Typeface.BOLD
            )
        )

        card.addView(
            tv(
                subtitle,
                12f,
                muted,
                Typeface.NORMAL
            )
        )

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(110)
        )
        params.bottomMargin = dp(12)

        content.addView(card, params)
    }

    private fun memoryItem(
        title: String,
        subtitle: String
    ) {

        settingItem(title, subtitle) {
            toast(title)
        }
    }

    private fun settingSection(title: String) {

        content.addView(space(15))

        val t = tv(
            title,
            10f,
            cyan,
            Typeface.BOLD
        )
        t.letterSpacing = 0.18f

        content.addView(t)
        content.addView(space(7))
    }

    private fun settingItem(
        title: String,
        subtitle: String,
        action: () -> Unit
    ) {

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(dp(16), dp(14), dp(12), dp(14))
        row.background = rounded(
            surface,
            18,
            dp(1),
            Color.rgb(28, 44, 73)
        )
        row.setOnClickListener { action() }

        val textBox = LinearLayout(this)
        textBox.orientation = LinearLayout.VERTICAL

        textBox.addView(
            tv(
                title,
                15f,
                text,
                Typeface.BOLD
            )
        )

        textBox.addView(
            tv(
                subtitle,
                11f,
                muted,
                Typeface.NORMAL
            )
        )

        row.addView(
            textBox,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        row.addView(
            tv(
                "›",
                27f,
                muted,
                Typeface.NORMAL
            )
        )

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = dp(8)

        content.addView(row, params)
    }

    private fun loginButton(
        icon: String,
        title: String,
        action: () -> Unit
    ) {

        val button = LinearLayout(this)
        button.orientation = LinearLayout.HORIZONTAL
        button.gravity = Gravity.CENTER_VERTICAL
        button.setPadding(dp(18), 0, dp(18), 0)
        button.background = rounded(
            surface2,
            18,
            dp(1),
            Color.rgb(39, 57, 91)
        )
        button.setOnClickListener { action() }

        val i = tv(
            icon,
            20f,
            white,
            Typeface.BOLD
        )
        i.gravity = Gravity.CENTER

        button.addView(
            i,
            LinearLayout.LayoutParams(dp(35), dp(55))
        )

        button.addView(space(10))

        button.addView(
            tv(
                title,
                14f,
                text,
                Typeface.BOLD
            )
        )

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(55)
        )
        params.bottomMargin = dp(10)

        content.addView(button, params)
    }

    private fun circleButton(symbol: String): TextView {

        val v = TextView(this)
        v.text = symbol
        v.gravity = Gravity.CENTER
        v.textSize = 17f
        v.setTextColor(white)
        v.typeface = Typeface.DEFAULT_BOLD
        v.background = rounded(
            surface2,
            50,
            dp(1),
            Color.rgb(37, 55, 89)
        )

        v.layoutParams = LinearLayout.LayoutParams(
            dp(44),
            dp(44)
        )

        return v
    }

    private fun tv(
        value: String,
        size: Float,
        color: Int,
        style: Int
    ): TextView {

        val v = TextView(this)
        v.text = value
        v.textSize = size
        v.setTextColor(color)
        v.typeface = Typeface.create("sans-serif", style)
        v.includeFontPadding = false

        return v
    }

    private fun rounded(
        color: Int,
        radius: Int,
        strokeWidth: Int,
        strokeColor: Int
    ): GradientDrawable {

        val g = GradientDrawable()
        g.setColor(color)
        g.cornerRadius = dp(radius).toFloat()

        if (strokeWidth > 0) {
            g.setStroke(strokeWidth, strokeColor)
        }

        return g
    }

    private fun space(px: Int): View {

        return Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                dp(px),
                dp(px)
            )
        }
    }

    private fun clear() {
        content.removeAllViews()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
