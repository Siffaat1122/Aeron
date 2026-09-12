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
    // COLORS
    // ─────────────────────────────────────────────

    private val bg = Color.rgb(3, 7, 18)
    private val panel = Color.rgb(9, 15, 29)
    private val panel2 = Color.rgb(12, 19, 37)

    private val cyan = Color.rgb(0, 229, 168)
    private val blue = Color.rgb(72, 104, 255)
    private val purple = Color.rgb(139, 76, 255)

    private val white = Color.rgb(242, 246, 255)
    private val muted = Color.rgb(139, 151, 177)

    // ─────────────────────────────────────────────
    // MAIN UI
    // ─────────────────────────────────────────────

    private lateinit var root: FrameLayout
    private lateinit var content: LinearLayout

    // Drawer
    private var drawerOpen = false
    private lateinit var drawerOverlay: View
    private lateinit var drawerContainer: LinearLayout

    // Chat
    private lateinit var chatMessages: LinearLayout
    private lateinit var chatScroll: ScrollView

    // Conversations
    private val conversations =
        mutableListOf<Conversation>()

    private var currentConversation = 0

    data class Message(
        val text: String,
        val isUser: Boolean
    )

    data class Conversation(
        var title: String,
        val messages: MutableList<Message>
    )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        // Default conversation
        conversations.add(
            Conversation(
                "New Conversation",
                mutableListOf(
                    Message(
                        "I'm ready. Tell me what you need.",
                        false
                    )
                )
            )
        )

        showHome()
    }

    // ─────────────────────────────────────────────
    // BASE SCREEN
    // ─────────────────────────────────────────────

    private fun baseScreen(
        screenTitle: String
    ) {

        root = FrameLayout(this)

        root.setBackgroundColor(bg)

        val scroll =
            ScrollView(this).apply {

                isFillViewport = true

                overScrollMode =
                    View.OVER_SCROLL_NEVER

                setBackgroundColor(bg)
            }

        content =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(18),
                    dp(18),
                    dp(18),
                    dp(110)
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

        // Bottom navigation
        root.addView(
            bottomNav(),
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

            val top =
                LinearLayout(this).apply {

                    orientation =
                        LinearLayout.HORIZONTAL

                    gravity =
                        Gravity.CENTER_VERTICAL
                }

            val menu =
                TextView(this).apply {

                    text = "☰"

                    textSize = 24f

                    gravity =
                        Gravity.CENTER

                    setTextColor(white)

                    setOnClickListener {

                        openDrawer()
                    }
                }

            top.addView(
                menu,
                LinearLayout.LayoutParams(
                    dp(42),
                    dp(48)
                )
            )

            val title =
                tv(
                    screenTitle,
                    22f,
                    white,
                    true
                )

            top.addView(
                title,
                LinearLayout.LayoutParams(
                    0,
                    -2,
                    1f
                )
            )

            content.addView(
                top,
                LinearLayout.LayoutParams(
                    -1,
                    -2
                ).apply {

                    bottomMargin =
                        dp(18)
                }
            )
        }
    }

    // ─────────────────────────────────────────────
    // DRAWER
    // ─────────────────────────────────────────────

    private fun openDrawer() {

        if (drawerOpen) return

        drawerOpen = true

        drawerOverlay =
            View(this).apply {

                setBackgroundColor(
                    Color.argb(
                        170,
                        0,
                        0,
                        0
                    )
                )

                setOnClickListener {

                    closeDrawer()
                }
            }

        root.addView(
            drawerOverlay,
            FrameLayout.LayoutParams(
                -1,
                -1
            )
        )

        drawerContainer =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(18),
                    dp(24),
                    dp(18),
                    dp(24)
                )

                background =
                    rounded(
                        panel2,
                        0
                    )
            }

        // Header
        val header =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        header.addView(
            textCircle(
                "A",
                48,
                cyan
            )
        )

        val headerText =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(12),
                    0,
                    0,
                    0
                )
            }

        headerText.addView(
            tv(
                "AERON",
                18f,
                white,
                true
            )
        )

        headerText.addView(
            tv(
                "YOUR AI COMPANION",
                9f,
                cyan,
                true
            )
        )

        header.addView(
            headerText,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        val close =
            tv(
                "×",
                30f,
                muted,
                false
            ).apply {

                gravity =
                    Gravity.CENTER

                setOnClickListener {

                    closeDrawer()
                }
            }

        header.addView(
            close,
            LinearLayout.LayoutParams(
                dp(40),
                dp(40)
            )
        )

        drawerContainer.addView(
            header,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {

                bottomMargin =
                    dp(24)
            }
        )

        // New chat
        val newChat =
            TextView(this).apply {

                text =
                    "＋   New Chat"

                textSize =
                    15f

                gravity =
                    Gravity.CENTER_VERTICAL

                setTextColor(bg)

                typeface =
                    Typeface.DEFAULT_BOLD

                setPadding(
                    dp(18),
                    0,
                    dp(18),
                    0
                )

                background =
                    rounded(
                        cyan,
                        18
                    )

                setOnClickListener {

                    createNewChat()

                    closeDrawer()

                    showChat()
                }
            }

        drawerContainer.addView(
            newChat,
            LinearLayout.LayoutParams(
                -1,
                dp(52)
            ).apply {

                bottomMargin =
                    dp(22)
            }
        )

        drawerContainer.addView(
            sectionTitle(
                "RECENT CHATS"
            ),
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {

                bottomMargin =
                    dp(6)
            }
        )

        // Recent chats
        addRecentChatsToDrawer()

        val space =
            Space(this)

        drawerContainer.addView(
            space,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        drawerItem(
            "⌂",
            "Home"
        ) {

            closeDrawer()
            showHome()
        }

        drawerItem(
            "◉",
            "Voice"
        ) {

            closeDrawer()
            showVoice()
        }

        drawerItem(
            "✦",
            "Create"
        ) {

            closeDrawer()
            showCreate()
        }

        drawerItem(
            "◈",
            "Memory"
        ) {

            closeDrawer()
            showMemory()
        }

        drawerItem(
            "⚙",
            "Settings"
        ) {

            closeDrawer()
            showSettings()
        }

        root.addView(
            drawerContainer,
            FrameLayout.LayoutParams(
                dp(310),
                -1,
                Gravity.START
            )
        )
    }

    private fun closeDrawer() {

        if (!drawerOpen) return

        drawerOpen = false

        root.removeView(
            drawerOverlay
        )

        root.removeView(
            drawerContainer
        )
    }

    private fun drawerItem(
        icon: String,
        text: String,
        click: () -> Unit
    ) {

        val item =
            TextView(this).apply {

                this.text =
                    "$icon   $text"

                textSize =
                    15f

                gravity =
                    Gravity.CENTER_VERTICAL

                setTextColor(white)

                setPadding(
                    dp(14),
                    0,
                    dp(14),
                    0
                )

                setOnClickListener {

                    click()
                }
            }

        drawerContainer.addView(
            item,
            LinearLayout.LayoutParams(
                -1,
                dp(52)
            )
        )
    }

    private fun addRecentChatsToDrawer() {

        conversations
            .reversed()
            .take(8)
            .forEach { conversation ->

                val item =
                    TextView(this).apply {

                        text =
                            "◌   ${conversation.title}"

                        textSize =
                            13f

                        gravity =
                            Gravity.CENTER_VERTICAL

                        setTextColor(muted)

                        setPadding(
                            dp(12),
                            0,
                            dp(8),
                            0
                        )

                        setOnClickListener {

                            val index =
                                conversations.indexOf(
                                    conversation
                                )

                            if (index >= 0) {

                                currentConversation =
                                    index
                            }

                            closeDrawer()

                            showChat()
                        }
                    }

                drawerContainer.addView(
                    item,
                    LinearLayout.LayoutParams(
                        -1,
                        dp(46)
                    )
                )
            }
    }

    // ─────────────────────────────────────────────
    // HOME
    // ─────────────────────────────────────────────

    private fun showHome() {

        baseScreen("")

        // Header
        val header =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    dp(14),
                    dp(10),
                    dp(10),
                    dp(10)
                )

                background =
                    rounded(
                        panel2,
                        28
                    )
            }

        val menu =
            TextView(this).apply {

                text = "☰"

                textSize = 23f

                gravity =
                    Gravity.CENTER

                setTextColor(white)

                setOnClickListener {

                    openDrawer()
                }
            }

        header.addView(
            menu,
            LinearLayout.LayoutParams(
                dp(42),
                dp(42)
            )
        )

        header.addView(
            textCircle(
                "A",
                38,
                cyan
            )
        )

        val headText =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(11),
                    0,
                    0,
                    0
                )
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
                "AI COMPANION • CORE",
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

                bottomMargin =
                    dp(28)
            }
        )

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

                topMargin =
                    dp(3)

                bottomMargin =
                    dp(18)
            }
        )

        content.addView(
            CoreOrbView(this),
            LinearLayout.LayoutParams(
                -1,
                dp(286)
            ).apply {

                bottomMargin =
                    dp(12)
            }
        )

        val ready =
            card()

        ready.setPadding(
            dp(16),
            dp(14),
            dp(16),
            dp(14)
        )

        val row =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        row.addView(
            dot(cyan)
        )

        row.addView(
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

                leftMargin =
                    dp(8)
            }
        )

        row.addView(
            tv(
                "ONLINE",
                9f,
                cyan,
                true
            )
        )

        ready.addView(row)

        ready.addView(
            tv(
                "Wake word: Hey AERON",
                11f,
                muted,
                false
            ),
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {

                topMargin =
                    dp(7)
            }
        )

        ready.addView(
            WaveView(this),
            LinearLayout.LayoutParams(
                -1,
                dp(30)
            ).apply {

                topMargin =
                    dp(6)
            }
        )

        content.addView(
            ready,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {

                bottomMargin =
                    dp(24)
            }
        )

        content.addView(
            sectionTitle(
                "QUICK ACTIONS"
            )
        )

        val grid =
            GridLayout(this).apply {

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

                bottomMargin =
                    dp(26)
            }
        )

        content.addView(
            sectionTitle(
                "RECENT CHATS"
            )
        )

        conversations
            .reversed()
            .take(5)
            .forEach {

                recent(
                    it.title,
                    "Continue this conversation."
                )
            }
    }

    // ─────────────────────────────────────────────
    // CHAT
    // ─────────────────────────────────────────────

    private fun showChat(
        mode: String = ""
    ) {

        baseScreen("")

        val header =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    0,
                    0,
                    0,
                    dp(14)
                )
            }

        val menu =
            TextView(this).apply {

                text = "☰"

                textSize = 25f

                gravity =
                    Gravity.CENTER

                setTextColor(white)

                setOnClickListener {

                    openDrawer()
                }
            }

        header.addView(
            menu,
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        val headerText =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL
            }

        headerText.addView(
            tv(
                "AERON",
                19f,
                white,
                true
            )
        )

        headerText.addView(
            tv(
                "ONLINE • AI COMPANION",
                9f,
                cyan,
                true
            )
        )

        header.addView(
            headerText,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        val newChat =
            TextView(this).apply {

                text = "＋"

                textSize = 25f

                gravity =
                    Gravity.CENTER

                setTextColor(cyan)

                setOnClickListener {

                    createNewChat()

                    showChat()
                }
            }

        header.addView(
            newChat,
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        content.addView(header)

        if (mode.isNotEmpty()) {

            content.addView(
                tv(
                    "$mode with AERON",
                    24f,
                    white,
                    true
                ),
                LinearLayout.LayoutParams(
                    -1,
                    -2
                ).apply {

                    topMargin =
                        dp(10)

                    bottomMargin =
                        dp(12)
                }
            )
        }

        // Message area
        chatScroll =
            ScrollView(this).apply {

                overScrollMode =
                    View.OVER_SCROLL_NEVER
            }

        chatMessages =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    0,
                    dp(10),
                    0,
                    dp(12)
                )
            }

        chatScroll.addView(chatMessages)

        content.addView(
            chatScroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        // Load current conversation
        loadConversation()

        // Input row
        val inputRow =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                background =
                    rounded(
                        panel2,
                        24
                    )

                setPadding(
                    dp(8),
                    dp(6),
                    dp(8),
                    dp(6)
                )
            }

        val input =
            EditText(this).apply {

                hint =
                    "Message AERON..."

                setHintTextColor(muted)

                setTextColor(white)

                textSize =
                    15f

                inputType =
                    InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE

                maxLines = 4

                minLines = 1

                setPadding(
                    dp(14),
                    0,
                    dp(8),
                    0
                )

                setBackgroundColor(
                    Color.TRANSPARENT
                )
            }

        inputRow.addView(
            input,
            LinearLayout.LayoutParams(
                0,
                dp(56),
                1f
            )
        )

        val send =
            TextView(this).apply {

                text = "↑"

                textSize = 25f

                gravity =
                    Gravity.CENTER

                setTextColor(bg)

                background =
                    rounded(
                        cyan,
                        50
                    )

                setOnClickListener {

                    val message =
                        input.text
                            .toString()
                            .trim()

                    if (
                        message.isNotEmpty()
                    ) {

                        sendMessage(
                            message
                        )

                        input.setText("")
                    }
                }
            }

        inputRow.addView(
            send,
            LinearLayout.LayoutParams(
                dp(46),
                dp(46)
            )
        )

        content.addView(
            inputRow,
            LinearLayout.LayoutParams(
                -1,
                dp(68)
            ).apply {

                topMargin =
                    dp(8)
            }
        )
    }

    private fun loadConversation() {

        chatMessages.removeAllViews()

        val conversation =
            conversations[
                currentConversation
            ]

        conversation.messages.forEach {

            if (it.isUser) {

                addUserBubble(
                    it.text
                )

            } else {

                addAeronBubble(
                    it.text
                )
            }
        }

        chatScroll.post {

            chatScroll.fullScroll(
                View.FOCUS_DOWN
            )
        }
    }

    private fun sendMessage(
        text: String
    ) {

        val conversation =
            conversations[
                currentConversation
            ]

        // Title from first user message
        if (
            conversation.title ==
            "New Conversation"
        ) {

            conversation.title =
                if (
                    text.length > 28
                ) {

                    text.substring(
                        0,
                        28
                    ) + "..."

                } else {

                    text
                }
        }

        conversation.messages.add(
            Message(
                text,
                true
            )
        )

        addUserBubble(text)

        chatScroll.post {

            chatScroll.fullScroll(
                View.FOCUS_DOWN
            )
        }

        // Temporary AI response
        val reply =
            temporaryAeronReply(
                text
            )

        conversation.messages.add(
            Message(
                reply,
                false
            )
        )

        chatMessages.postDelayed(
            {

                addAeronBubble(
                    reply
                )

                chatScroll.post {

                    chatScroll.fullScroll(
                        View.FOCUS_DOWN
                    )
                }

            },
            500
        )
    }

    // ─────────────────────────────────────────────
    // TEMPORARY AI RESPONSE
    // Google AI Studio API later here
    // ─────────────────────────────────────────────

    private fun temporaryAeronReply(
        message: String
    ): String {

        return when {

            message.contains(
                "hello",
                true
            ) ||
                message.contains(
                    "hi",
                    true
                ) ->

                "Hello. I'm AERON. How can I help you today?"

            message.contains(
                "hey aeron",
                true
            ) ->

                "Yes. I'm listening."

            else ->

                "I received your message: \"$message\"\n\nMy real AI connection will be connected here in the next phase."
        }
    }

    // ─────────────────────────────────────────────
    // CHAT BUBBLES
    // ─────────────────────────────────────────────

    private fun addUserBubble(
        text: String
    ) {

        val wrapper =
            LinearLayout(this).apply {

                gravity =
                    Gravity.END

                orientation =
                    LinearLayout.HORIZONTAL
            }

        val bubble =
            TextView(this).apply {

                this.text = text

                textSize =
                    15f

                setTextColor(white)

                setPadding(
                    dp(16),
                    dp(12),
                    dp(16),
                    dp(12)
                )

                background =
                    rounded(
                        Color.rgb(
                            29,
                            55,
                            94
                        ),
                        20
                    )
            }

        wrapper.addView(
            bubble,
            LinearLayout.LayoutParams(
                -2,
                -2
            )
        )

        chatMessages.addView(
            wrapper,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {

                topMargin =
                    dp(8)

                bottomMargin =
                    dp(8)
            }
        )
    }

    private fun addAeronBubble(
        text: String
    ) {

        val wrapper =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL
            }

        val label =
            tv(
                "AERON",
                10f,
                cyan,
                true
            )

        wrapper.addView(label)

        val bubble =
            TextView(this).apply {

                this.text = text

                textSize =
                    15f

                setTextColor(white)

                setPadding(
                    dp(16),
                    dp(13),
                    dp(16),
                    dp(13)
                )

                background =
                    rounded(
                        panel,
                        20
                    )
            }

        wrapper.addView(
            bubble,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {

                topMargin =
                    dp(5)
            }
        )

        chatMessages.addView(
            wrapper,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {

                topMargin =
                    dp(10)

                bottomMargin =
                    dp(10)
            }
        )
    }

    // ─────────────────────────────────────────────
    // NEW CHAT
    // ─────────────────────────────────────────────

    private fun createNewChat() {

        conversations.add(
            Conversation(
                "New Conversation",
                mutableListOf(
                    Message(
                        "I'm ready. Tell me what you need.",
                        false
                    )
                )
            )
        )

        currentConversation =
            conversations.lastIndex
    }

    // ─────────────────────────────────────────────
    // VOICE
    // ─────────────────────────────────────────────

    private fun showVoice() {

        baseScreen(
            "AERON LISTENING"
        )

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

                topMargin =
                    dp(12)

                bottomMargin =
                    dp(14)
            }
        )

        val c =
            card()

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
            )
        )

        c.addView(
            tv(
                "Wake-word detection will be connected to your trained AERON model.",
                12f,
                muted,
                false
            ),
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {

                topMargin =
                    dp(7)
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

                bottomMargin =
                    dp(24)
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

        baseScreen(
            "MEMORY & PRIVACY"
        )

        bigAction(
            "◈",
            "Memory Vault",
            "Control what AERON remembers."
        )

        bigAction(
            "⌁",
            "Chat History",
            "Review and manage conversations."
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
    // BOTTOM NAV
    // ─────────────────────────────────────────────

    private fun bottomNav(): View {

        val bar =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    dp(8),
                    dp(8),
                    dp(8),
                    dp(8)
                )

                background =
                    rounded(
                        Color.rgb(
                            6,
                            12,
                            24
                        ),
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

        val b =
            TextView(this).apply {

                text =
                    "$icon\n$label"

                gravity =
                    Gravity.CENTER

                textSize =
                    10f

                setTextColor(muted)

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

        val container =
            FrameLayout(this).apply {

                layoutParams =
                    GridLayout.LayoutParams().apply {

                        width = 0

                        height =
                            dp(88)

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

        val c =
            card().apply {

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

        val row =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
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

        val text =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER_VERTICAL
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
            )
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

        container.addView(c)

        return container
    }

    // ─────────────────────────────────────────────
    // BIG ACTION
    // ─────────────────────────────────────────────

    private fun bigAction(
        icon: String,
        name: String,
        sub: String
    ) {

        val c =
            card().apply {

                setPadding(
                    dp(16),
                    dp(15),
                    dp(16),
                    dp(15)
                )
            }

        val row =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
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

        val t =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL
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
            )
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

                bottomMargin =
                    dp(10)
            }
        )
    }

    // ─────────────────────────────────────────────
    // RECENT CARD
    // ─────────────────────────────────────────────

    private fun recent(
        name: String,
        sub: String
    ) {

        val c =
            card().apply {

                setPadding(
                    dp(14),
                    dp(13),
                    dp(14),
                    dp(13)
                )

                setOnClickListener {

                    showChat()
                }
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
            )
        )

        content.addView(
            c,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {

                bottomMargin =
                    dp(8)
            }
        )
    }

    // ─────────────────────────────────────────────
    // HELPERS
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

            letterSpacing =
                0.14f

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
        (
            value *
                resources.displayMetrics.density
            ).toInt()

    // ─────────────────────────────────────────────
    // AERON CORE ORB
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
                    r * 2f,
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
                r * 2f,
                paint
            )

            paint.shader = null

            // Rings
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

                val sweep =
                    250f +
                        i * 25f

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

            // A
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

            // Label
            paint.textSize = 11f

            paint.color = cyan

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
    // WAVEFORM
    // ─────────────────────────────────────────────

    private class WaveView(
        context: Context
    ) : View(context) {

        private val p =
            Paint(Paint.ANTI_ALIAS_FLAG)

        private var phase =
            0f

        init {

            p.strokeWidth =
                3f

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
                    width *
                        i /
                        80f

                val amp =
                    3f +
                        8f *
                        abs(
                            sin(
                                i *
                                    0.42f +
                                    phase
                            )
                        )

                val y =
                    cy +
                        sin(
                            i *
                                0.55f +
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
