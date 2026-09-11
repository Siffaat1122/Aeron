package com.example.aeron

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val bgColor = Color.rgb(5, 10, 24)
    private val cardColor = Color.rgb(12, 25, 47)
    private val blue = Color.rgb(45, 105, 255)
    private val cyan = Color.rgb(0, 210, 255)
    private val white = Color.WHITE
    private val muted = Color.rgb(150, 165, 190)

    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(3, 7, 18)
        window.navigationBarColor = Color.rgb(3, 7, 18)

        showHome()
    }

    private fun showHome() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgColor)
        }

        // TOP BAR
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(24, 22, 24, 16)
        }

        val titleBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        titleBox.addView(text(
            "AERON",
            24f,
            white,
            true
        ))

        titleBox.addView(text(
            "Your AI Assistant",
            12f,
            muted,
            false
        ))

        top.addView(
            titleBox,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        top.addView(
            actionButton("⌕") {
                showMessage("Search coming soon")
            }
        )

        top.addView(
            actionButton("⚙") {
                showSettings()
            }
        )

        root.addView(top)

        // MAIN SCROLL AREA
        val scroll = ScrollView(this)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 10, 20, 20)
        }

        // AERON CORE CARD
        val coreCard = card()

        coreCard.addView(
            text(
                "AERON CORE",
                13f,
                cyan,
                true
            )
        )

        coreCard.addView(
            text(
                "Hey AERON 👋",
                28f,
                white,
                true
            )
        )

        coreCard.addView(
            text(
                "I'm here. How can I help you today?",
                15f,
                muted,
                false
            )
        )

        val orb = TextView(this).apply {
            text = "✦"
            textSize = 54f
            setTextColor(cyan)
            gravity = Gravity.CENTER
            setPadding(0, 22, 0, 18)
        }

        coreCard.addView(orb)

        coreCard.addView(
            text(
                "●  AERON is ready",
                14f,
                Color.rgb(70, 230, 170),
                true
            )
        )

        content.addView(
            coreCard,
            marginParams(0, 8, 0, 18)
        )

        // QUICK ACTIONS
        content.addView(
            text(
                "Quick Actions",
                18f,
                white,
                true
            )
        )

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 10, 0, 18)
        }

        actions.addView(
            quickAction("⌕", "Search") {
                showMessage("AERON Search")
            },
            weightParams()
        )

        actions.addView(
            quickAction("▣", "Image") {
                showMessage("Image generation")
            },
            weightParams()
        )

        actions.addView(
            quickAction("▶", "Video") {
                showMessage("Video generation")
            },
            weightParams()
        )

        actions.addView(
            quickAction("＋", "File") {
                showMessage("File upload")
            },
            weightParams()
        )

        content.addView(actions)

        // CHAT INPUT
        val inputCard = card()

        inputCard.addView(
            text(
                "Ask AERON anything...",
                15f,
                muted,
                false
            )
        )

        val sendRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 14, 0, 0)
        }

        val mic = actionButton("🎙") {
            showMessage("Listening...")
        }

        val send = actionButton("➤") {
            showMessage("Type your message first")
        }

        sendRow.addView(
            mic,
            LinearLayout.LayoutParams(0, 50, 1f)
        )

        sendRow.addView(
            send,
            LinearLayout.LayoutParams(0, 50, 1f)
        )

        inputCard.addView(sendRow)

        content.addView(
            inputCard,
            marginParams(0, 0, 0, 20)
        )

        // RECENT CHATS
        content.addView(
            text(
                "Recent Chats",
                18f,
                white,
                true
            )
        )

        addChat("Welcome to AERON", "Start your first conversation")
        addChat("AERON Core", "Configure your AI assistant")
        addChat("Voice Assistant", "Hey AERON wake word")

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        // BOTTOM NAV
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 10)
            setBackgroundColor(Color.rgb(7, 15, 30))
        }

        nav.addView(
            navItem("⌂", "Home") {
                showHome()
            },
            weightParams()
        )

        nav.addView(
            navItem("◉", "Chat") {
                showMessage("Chat interface coming next")
            },
            weightParams()
        )

        nav.addView(
            navItem("🎙", "Voice") {
                showMessage("Voice assistant")
            },
            weightParams()
        )

        nav.addView(
            navItem("◈", "Memory") {
                showMessage("Memory Vault")
            },
            weightParams()
        )

        nav.addView(
            navItem("⚙", "Settings") {
                showSettings()
            },
            weightParams()
        )

        root.addView(nav)

        setContentView(root)
    }

    private fun showSettings() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgColor)
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(20, 24, 20, 18)
        }

        top.addView(
            actionButton("‹") {
                showHome()
            }
        )

        top.addView(
            text(
                "Settings",
                25f,
                white,
                true
            ),
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        root.addView(top)

        val scroll = ScrollView(this)

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 4, 18, 30)
        }

        list.addView(
            settingCard(
                "👤",
                "Account",
                "Profile • Phone • Google • Facebook"
            )
        )

        list.addView(
            settingCard(
                "✦",
                "AERON CORE",
                "Personality • Intelligence • Memory"
            )
        )

        list.addView(
            settingCard(
                "🎙",
                "Voice & Wake Word",
                "Voice • Hey AERON • Sensitivity"
            )
        )

        list.addView(
            settingCard(
                "◉",
                "Appearance",
                "Theme • Animations • AERON Orb"
            )
        )

        list.addView(
            settingCard(
                "▣",
                "Memory",
                "Memory Vault • Saved information"
            )
        )

        list.addView(
            settingCard(
                "◈",
                "Chat History",
                "Search • Rename • Delete • Restore"
            )
        )

        list.addView(
            settingCard(
                "🌐",
                "Language & Region",
                "Automatic language detection"
            )
        )

        list.addView(
            settingCard(
                "🔒",
                "Privacy & Security",
                "Permissions • Data • Privacy controls"
            )
        )

        list.addView(
            settingCard(
                "ⓘ",
                "About AERON",
                "Version • Help • Support"
            )
        )

        scroll.addView(list)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun settingCard(
        icon: String,
        title: String,
        subtitle: String
    ): View {

        val box = card()

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        row.addView(
            text(
                icon,
                25f,
                cyan,
                false
            ),
            LinearLayout.LayoutParams(50, 60)
        )

        val labels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        labels.addView(
            text(
                title,
                17f,
                white,
                true
            )
        )

        labels.addView(
            text(
                subtitle,
                12f,
                muted,
                false
            )
        )

        row.addView(
            labels,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        row.addView(
            text("›", 28f, muted, false)
        )

        box.addView(row)

        return box
    }

    private fun addChat(title: String, subtitle: String) {

        val box = card()

        box.addView(
            text(
                title,
                15f,
                white,
                true
            )
        )

        box.addView(
            text(
                subtitle,
                12f,
                muted,
                false
            )
        )

        content.addView(
            box,
            marginParams(0, 8, 0, 0)
        )
    }

    private fun quickAction(
        icon: String,
        name: String,
        click: () -> Unit
    ): View {

        val box = card()

        box.setOnClickListener {
            click()
        }

        box.addView(
            text(
                icon,
                25f,
                cyan,
                true
            )
        )

        box.addView(
            text(
                name,
                12f,
                white,
                true
            )
        )

        return box
    }

    private fun navItem(
        icon: String,
        name: String,
        click: () -> Unit
    ): View {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(4, 4, 4, 4)
            setOnClickListener {
                click()
            }
        }

        box.addView(
            text(
                icon,
                20f,
                cyan,
                true
            )
        )

        box.addView(
            text(
                name,
                10f,
                muted,
                false
            )
        )

        return box
    }

    private fun actionButton(
        symbol: String,
        click: () -> Unit
    ): View {

        val button = TextView(this).apply {
            text = symbol
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(white)
            setPadding(12, 8, 12, 8)
            background = rounded(Color.rgb(15, 32, 60), 18f)
            setOnClickListener {
                click()
            }
        }

        return button
    }

    private fun card(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            background = rounded(cardColor, 22f)
        }
    }

    private fun rounded(
        color: Int,
        radius: Float
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
            setStroke(1, Color.rgb(25, 80, 145))
        }
    }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean
    ): TextView {

        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)

            if (bold) {
                typeface = Typeface.DEFAULT_BOLD
            }

            setPadding(0, 3, 0, 3)
        }
    }

    private fun weightParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1f
        ).apply {
            setMargins(4, 0, 4, 0)
        }
    }

    private fun marginParams(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            -1,
            -2
        ).apply {
            setMargins(left, top, right, bottom)
        }
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(
            this,
            message,
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}
