package com.relateos.keyboard

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.HorizontalScrollView
import android.content.res.Configuration
import androidx.core.content.ContextCompat
import com.relateos.keyboard.R
import com.relateos.keyboard.databinding.KeyboardLayoutBinding
import android.graphics.drawable.Drawable
import android.view.LayoutInflater

class MyKeyboard : InputMethodService() {

    private lateinit var keyboardBinding: KeyboardLayoutBinding
    private var capsState = 0 // 0: off, 1: next, 2: caps lock
    private var isSymbolsLayout = false // Track the current layout
    private var currentScreen = SCREEN_KEYBOARD_LETTERS
    private var isScreenTogglesVisible = false
    private var currentSuggestions = listOf<String>()

    companion object {
        val letterButtonIds = arrayOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnQ, R.id.btnW, R.id.btnE, R.id.btnR, R.id.btnT, R.id.btnY, R.id.btnU, R.id.btnI, R.id.btnO, R.id.btnP,
            R.id.btnA, R.id.btnS, R.id.btnD, R.id.btnF, R.id.btnG, R.id.btnH, R.id.btnJ, R.id.btnK, R.id.btnL,
            R.id.btnZ, R.id.btnX, R.id.btnC, R.id.btnV, R.id.btnB, R.id.btnN, R.id.btnM, R.id.btnDot, R.id.btnComma,
            R.id.btnSpace, R.id.btnEnter, R.id.btnBackSpace, R.id.btnCaps, R.id.btnSymbols // Add the Caps button
        )

        val symbolButtonIds = arrayOf(
            R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btn0,
            R.id.btnExclamation, R.id.btnAt, R.id.btnHash, R.id.btnDollar, R.id.btnPercent, R.id.btnCaret, R.id.btnAmpersand, R.id.btnAsterisk, R.id.btnLeftParen, R.id.btnRightParen,
            R.id.btnDash, R.id.btnUnderscore, R.id.btnPlus, R.id.btnEquals, R.id.btnLeftBracket, R.id.btnRightBracket, R.id.btnLeftBrace, R.id.btnRightBrace, R.id.btnSemicolon, R.id.btnColon,
            R.id.btnLessThan, R.id.btnGreaterThan, R.id.btnSlash, R.id.btnQuestionMark, R.id.btnQuote, R.id.btnApostrophe,
            R.id.btnSymbolsToggle, R.id.btnSymbolsToggle2, // Add btnSymbolsToggle2
            R.id.btnSymbols, R.id.btnComma, R.id.btnSpace, R.id.btnDot, R.id.btnEnter, R.id.btnBackSpace
        )

        // Screen types
        const val SCREEN_KEYBOARD_LETTERS = 0
        const val SCREEN_KEYBOARD_SYMBOLS = 1
        const val SCREEN_CHAT_ASSISTANT = 2
        const val SCREEN_CONVERSATION = 3
        const val SCREEN_CLIPBOARD = 4
        const val SCREEN_SETTINGS = 5
        const val SCREEN_MENU = 6
    }

    override fun onCreateInputView(): View {
        val baseView = layoutInflater.inflate(R.layout.keyboard_base_layout, null)

        val keyboardContainer = baseView.findViewById<FrameLayout>(R.id.keyboardContainer)
        val keyboardView = layoutInflater.inflate(R.layout.keyboard_layout, keyboardContainer, false)
        keyboardContainer.addView(keyboardView)

        keyboardBinding = KeyboardLayoutBinding.bind(keyboardView)

        // Apply theme based styling
        applyThemeBasedStyling()
        setButtonStyle(if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) 
            R.style.btnDarkTheme else R.style.btnLightTheme)

        // Set up the initial layout (letter layout)
        setupButtonListeners(letterButtonIds)

        // Set up top section toggle
        setupTopSectionToggle(baseView)

        // Set up screen toggle buttons
        setupScreenToggleButtons(baseView)

        return baseView
    }

    private fun setupTopSectionToggle(baseView: View) {
        val btnToggleScreens = baseView.findViewById<ImageButton>(R.id.btnToggleScreens)
        val autoSuggestionsLayout = baseView.findViewById<LinearLayout>(R.id.autoSuggestionsLayout)
        val screenTogglesLayout = baseView.findViewById<HorizontalScrollView>(R.id.screenTogglesLayout)

        btnToggleScreens.setOnClickListener {
            isScreenTogglesVisible = !isScreenTogglesVisible
            if (isScreenTogglesVisible) {
                autoSuggestionsLayout.visibility = View.GONE
                screenTogglesLayout.visibility = View.VISIBLE
            } else {
                autoSuggestionsLayout.visibility = View.VISIBLE
                screenTogglesLayout.visibility = View.GONE
            }
        }
    }

    private fun setupScreenToggleButtons(baseView: View) {
        val keyboardContainer = baseView.findViewById<FrameLayout>(R.id.keyboardContainer)

        baseView.findViewById<ImageButton>(R.id.btnChatAssistant)?.setOnClickListener {
            switchToScreen(SCREEN_CHAT_ASSISTANT, keyboardContainer)
        }

        baseView.findViewById<ImageButton>(R.id.btnConversation)?.setOnClickListener {
            switchToScreen(SCREEN_CONVERSATION, keyboardContainer)
        }

        baseView.findViewById<ImageButton>(R.id.btnClipboard)?.setOnClickListener {
            switchToScreen(SCREEN_CLIPBOARD, keyboardContainer)
        }

        baseView.findViewById<ImageButton>(R.id.btnSettings)?.setOnClickListener {
            switchToScreen(SCREEN_SETTINGS, keyboardContainer)
        }

        baseView.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            switchToScreen(SCREEN_MENU, keyboardContainer)
        }
    }

    private fun switchToScreen(screenType: Int, container: FrameLayout) {
        currentScreen = screenType
        container.removeAllViews()

        val screenView = when (screenType) {
            SCREEN_KEYBOARD_LETTERS -> {
                isSymbolsLayout = false
                val view = layoutInflater.inflate(R.layout.keyboard_layout, container, false)
                setupButtonListeners(letterButtonIds)
                view
            }
            SCREEN_KEYBOARD_SYMBOLS -> {
                isSymbolsLayout = true
                val view = layoutInflater.inflate(R.layout.keyboard_layout_symbols, container, false)
                setupButtonListeners(symbolButtonIds)
                view
            }
            SCREEN_CHAT_ASSISTANT -> {
                val view = layoutInflater.inflate(R.layout.keyboard_chat_assistant, container, false)
                setupChatAssistantScreen(view)
                view
            }
            SCREEN_CONVERSATION -> {
                val view = layoutInflater.inflate(R.layout.keyboard_conversation, container, false)
                setupConversationScreen(view)
                view
            }
            SCREEN_CLIPBOARD -> {
                val view = layoutInflater.inflate(R.layout.keyboard_clipboard, container, false)
                setupClipboardScreen(view)
                view
            }
            SCREEN_SETTINGS -> {
                val view = layoutInflater.inflate(R.layout.keyboard_settings, container, false)
                setupSettingsScreen(view)
                view
            }
            SCREEN_MENU -> {
                val view = layoutInflater.inflate(R.layout.keyboard_menu, container, false)
                setupMenuScreen(view)
                view
            }
            else -> layoutInflater.inflate(R.layout.keyboard_layout, container, false)
        }

        container.addView(screenView)

        if (screenType == SCREEN_KEYBOARD_LETTERS || screenType == SCREEN_KEYBOARD_SYMBOLS) {
            val isNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            val styleResId = if (isNightMode) R.style.btnDarkTheme else R.style.btnLightTheme
            setButtonStyle(styleResId)
        }

        val baseView = container.parent as View
        val topSection = baseView.findViewById<FrameLayout>(R.id.topSectionContainer)
        topSection.visibility = if (screenType == SCREEN_KEYBOARD_LETTERS || screenType == SCREEN_KEYBOARD_SYMBOLS) 
            View.VISIBLE else View.GONE
    }

    private fun setupChatAssistantScreen(view: View) {
        val btnBackToKeyboard = view.findViewById<Button>(R.id.btnBackToKeyboard)
        btnBackToKeyboard.setOnClickListener {
            val container = view.parent as FrameLayout
            switchToScreen(SCREEN_KEYBOARD_LETTERS, container)
        }

        val btnSendPrompt = view.findViewById<Button>(R.id.btnSendPrompt)
        btnSendPrompt.setOnClickListener {
            // TODO: Implement API call to chat service
        }
    }

    private fun setupConversationScreen(view: View) {
        // Add back button functionality
        val btnBackToKeyboard = view.findViewById<Button>(R.id.btnBackToKeyboard)
        btnBackToKeyboard.setOnClickListener {
            val container = view.parent as FrameLayout
            switchToScreen(SCREEN_KEYBOARD_LETTERS, container)
        }
        
        // If we have a KeyboardDataManager, use it to get/set conversation data
        val keyboardDataManager = KeyboardDataManager(this)
        
        // Any conversation history RecyclerView setup would go here
        // For example:
        /*
        val conversationRecyclerView = view.findViewById<RecyclerView>(R.id.conversationRecyclerView)
        val layoutManager = LinearLayoutManager(this)
        conversationRecyclerView.layoutManager = layoutManager
        
        // Adapter setup with data from KeyboardDataManager
        val conversations = keyboardDataManager.getConversations()
        val adapter = ConversationAdapter(conversations) { text ->
            // When a conversation item is clicked, insert it
            currentInputConnection?.commitText(text, 1)
        }
        conversationRecyclerView.adapter = adapter
        */
        
        // Handle sending messages
        val btnSendMessage = view.findViewById<Button>(R.id.btnSendMessage)
        val etMessage = view.findViewById<EditText>(R.id.etMessage)
        
        btnSendMessage?.setOnClickListener {
            val message = etMessage?.text?.toString() ?: ""
            if (message.isNotBlank()) {
                // Save the message
                // keyboardDataManager.saveConversation(message)
                
                // Clear input field
                etMessage?.text?.clear()
                
                // Update UI
                // adapter.updateData(keyboardDataManager.getConversations())
                
                // Optional: Insert into current text field
                currentInputConnection?.commitText(message, 1)
            }
        }
    }

    private fun setupClipboardScreen(view: View) {
        val btnBackToKeyboard = view.findViewById<Button>(R.id.btnBackToKeyboard)
        btnBackToKeyboard.setOnClickListener {
            val container = view.parent as FrameLayout
            switchToScreen(SCREEN_KEYBOARD_LETTERS, container)
        }
        
        val btnClearClipboard = view.findViewById<Button>(R.id.btnClearClipboard)
        btnClearClipboard.setOnClickListener {
            // TODO: Implement clipboard clearing functionality
            // keyboardDataManager.clearClipboard()
        }
        
        // TODO: Set up the RecyclerView for displaying clipboard items
    }

    private fun setupSettingsScreen(view: View) {
        val btnBackToKeyboard = view.findViewById<Button>(R.id.btnBackToKeyboard)
        btnBackToKeyboard.setOnClickListener {
            val container = view.parent as FrameLayout
            switchToScreen(SCREEN_KEYBOARD_LETTERS, container)
        }
        
        val btnSaveSettings = view.findViewById<Button>(R.id.btnSaveSettings)
        btnSaveSettings.setOnClickListener {
            // TODO: Save settings logic
            // Get values from UI elements and save them
            val container = view.parent as FrameLayout
            switchToScreen(SCREEN_KEYBOARD_LETTERS, container)
        }
        
        // TODO: Initialize settings UI with saved values
    }

    private fun setupMenuScreen(view: View) {
        val btnBackToKeyboard = view.findViewById<Button>(R.id.btnBackToKeyboard)
        btnBackToKeyboard.setOnClickListener {
            val container = view.parent as FrameLayout
            switchToScreen(SCREEN_KEYBOARD_LETTERS, container)
        }
        
        view.findViewById<Button>(R.id.btnMenuChatAssistant).setOnClickListener {
            val container = view.parent as FrameLayout
            switchToScreen(SCREEN_CHAT_ASSISTANT, container)
        }
        
        view.findViewById<Button>(R.id.btnMenuConversation).setOnClickListener {
            val container = view.parent as FrameLayout
            switchToScreen(SCREEN_CONVERSATION, container)
        }
        
        view.findViewById<Button>(R.id.btnMenuClipboard).setOnClickListener {
            val container = view.parent as FrameLayout
            switchToScreen(SCREEN_CLIPBOARD, container)
        }
        
        view.findViewById<Button>(R.id.btnMenuSettings).setOnClickListener {
            val container = view.parent as FrameLayout
            switchToScreen(SCREEN_SETTINGS, container)
        }
    }

    private fun setupButtonListeners(buttonIds: Array<Int>) {
        for (buttonId in buttonIds) {
            val button = keyboardBinding.root.findViewById<Button>(buttonId)
            button?.setOnClickListener {
                val inputConnection = currentInputConnection
                if (inputConnection != null) {
                    if (buttonId == R.id.btnCaps) {
                        capsState = (capsState + 1) % 3 // Cycle through 0, 1, 2
                        updateCapsButtonIcon()
                        setButtonStyle(if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) 
                            R.style.btnDarkTheme else R.style.btnLightTheme)
                    } else if (buttonId == R.id.btnSymbols || buttonId == R.id.btnSymbolsToggle || buttonId == R.id.btnSymbolsToggle2) {
                        toggleKeyboardLayout()
                    } else if (buttonId == R.id.btnBackSpace) {
                        inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                    } else if (buttonId == R.id.btnEnter) {
                        inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                    } else if (buttonId == R.id.btnSpace) {
                        inputConnection.commitText(" ", 1)
                    } else {
                        var text = button.text.toString()
                        if (capsState == 1) {
                            text = text.uppercase()
                            capsState = 0 // Reset to off after one character
                            updateCapsButtonIcon()
                            setButtonStyle(if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) 
                                R.style.btnDarkTheme else R.style.btnLightTheme)
                        } else if (capsState == 2) {
                            text = text.uppercase()
                        }
                        inputConnection.commitText(text, 1)
                    }
                }
            }
        }
    }

    private fun toggleKeyboardLayout() {
        val container = keyboardBinding.root.parent as FrameLayout
        
        // Store current theme state
        val isNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val currentTheme = if (isNightMode) R.style.btnDarkTheme else R.style.btnLightTheme
        val currentBackground = if (isNightMode) R.color.black else R.color.white
        
        if (isSymbolsLayout) {
            currentScreen = SCREEN_KEYBOARD_LETTERS
            isSymbolsLayout = false
            
            container.removeAllViews()
            val view = layoutInflater.inflate(R.layout.keyboard_layout, container, false)
            container.addView(view)
            
            // This is critical - rebind to the new view
            keyboardBinding = KeyboardLayoutBinding.bind(view)
            
            // Setup button listeners with current view context
            setupButtonListeners(letterButtonIds)
            
            // Apply existing style without recalculating theme
            keyboardBinding.root.setBackgroundColor(ContextCompat.getColor(this, currentBackground))
        } else {
            currentScreen = SCREEN_KEYBOARD_SYMBOLS
            isSymbolsLayout = true
            
            container.removeAllViews()
            val view = layoutInflater.inflate(R.layout.keyboard_layout_symbols, container, false) 
            container.addView(view)
            
            // This is critical - rebind to the new view
            keyboardBinding = KeyboardLayoutBinding.bind(view)
            
            // Setup button listeners with current view context  
            setupButtonListeners(symbolButtonIds)
            
            // Apply existing style without recalculating theme
            keyboardBinding.root.setBackgroundColor(ContextCompat.getColor(this, currentBackground))
        }
        
        // Apply consistent style to buttons without changing theme properties
        setButtonStyle(currentTheme)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyThemeBasedStyling()
    }

    private fun applyThemeBasedStyling() {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                keyboardBinding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
                setButtonStyle(R.style.btnDarkTheme)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                keyboardBinding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                setButtonStyle(R.style.btnLightTheme)
            }
        }
    }

    private fun setButtonStyle(styleResId: Int) {
        val currentButtonIds = if (isSymbolsLayout) symbolButtonIds else letterButtonIds

        for (buttonId in currentButtonIds) {
            val button = keyboardBinding.root.findViewById<Button>(buttonId)
            button?.setTextAppearance(styleResId)
            if (styleResId == R.style.btnDarkTheme) {
                button?.background = ContextCompat.getDrawable(this, R.drawable.btn_ripple)
            } else {
                button?.background = ContextCompat.getDrawable(this, R.drawable.btn_white_ripple)
            }

            val buttonText = button?.text?.toString()
            if ((capsState == 1 || capsState == 2) && buttonId != R.id.btnSymbols && buttonId != R.id.btnSpace && buttonId != R.id.btnEnter && buttonId != R.id.btnBackSpace && buttonId != R.id.btnCaps) {
                if (buttonText?.length == 1 && buttonText[0].isLetter()) {
                    button.text = buttonText.uppercase()
                }
            } else {
                val originalText = when (buttonId) {
                    R.id.btnQ -> "q"
                    R.id.btnW -> "w"
                    R.id.btnE -> "e"
                    R.id.btnR -> "r"
                    R.id.btnT -> "t"
                    R.id.btnY -> "y"
                    R.id.btnU -> "u"
                    R.id.btnI -> "i"
                    R.id.btnO -> "o"
                    R.id.btnP -> "p"
                    R.id.btnA -> "a"
                    R.id.btnS -> "s"
                    R.id.btnD -> "d"
                    R.id.btnF -> "f"
                    R.id.btnG -> "g"
                    R.id.btnH -> "h"
                    R.id.btnJ -> "j"
                    R.id.btnK -> "k"
                    R.id.btnL -> "l"
                    R.id.btnZ -> "z"
                    R.id.btnX -> "x"
                    R.id.btnC -> "c"
                    R.id.btnV -> "v"
                    R.id.btnB -> "b"
                    R.id.btnN -> "n"
                    R.id.btnM -> "m"
                    R.id.btnSymbols -> "!#1"
                    R.id.btnSpace -> "English"
                    R.id.btnEnter -> "Enter"
                    R.id.btnDot -> "."
                    R.id.btnComma -> ","
                    R.id.btnSymbolsToggle -> "ABC"
                    R.id.btnExclamation -> "!"
                    R.id.btnAt -> "@"
                    R.id.btnHash -> "#"
                    R.id.btnDollar -> "$"
                    R.id.btnPercent -> "%"
                    R.id.btnCaret -> "^"
                    R.id.btnAmpersand -> "&"
                    R.id.btnAsterisk -> "*"
                    R.id.btnLeftParen -> "("
                    R.id.btnRightParen -> ")"
                    R.id.btnDash -> "-"
                    R.id.btnUnderscore -> "_"
                    R.id.btnPlus -> "+"
                    R.id.btnEquals -> "="
                    R.id.btnLeftBracket -> "["
                    R.id.btnRightBracket -> "]"
                    R.id.btnLeftBrace -> "{"
                    R.id.btnRightBrace -> "}"
                    R.id.btnSemicolon -> ";"
                    R.id.btnColon -> ":"
                    R.id.btnLessThan -> "<"
                    R.id.btnGreaterThan -> ">"
                    R.id.btnSlash -> "/"
                    R.id.btnQuestionMark -> "?"
                    R.id.btnQuote -> "\""
                    R.id.btnApostrophe -> "'"
                    else -> button?.text?.toString()
                }
                button?.text = originalText
            }
        }
    }

    private fun updateCapsButtonIcon() {
        val capsButton = keyboardBinding.root.findViewById<Button>(R.id.btnCaps)
        val icon: Drawable? = when (capsState) {
            0 -> ContextCompat.getDrawable(this, R.drawable.ic_keyboard_caps_outline)
            1 -> ContextCompat.getDrawable(this, R.drawable.ic_keyboard_caps_underlined)
            2 -> ContextCompat.getDrawable(this, R.drawable.ic_keyboard_caps)
            else -> ContextCompat.getDrawable(this, R.drawable.ic_keyboard_caps_outline)
        }
        capsButton?.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
    }

    private fun updateSuggestions(suggestions: List<String>) {
        currentSuggestions = suggestions

        val baseView = keyboardBinding.root.parent.parent as View
        val suggestionContainer = baseView.findViewById<LinearLayout>(R.id.suggestionContainer)
        suggestionContainer.removeAllViews()

        for (suggestion in suggestions) {
            val suggestionButton = layoutInflater.inflate(
                R.layout.suggestion_button, suggestionContainer, false) as Button
            suggestionButton.text = suggestion
            suggestionButton.setOnClickListener {
                currentInputConnection?.commitText(suggestion, 1)
            }
            suggestionContainer.addView(suggestionButton)
        }
    }

    // This is a simple word prediction system - in a real app you'd use a more sophisticated algorithm
    // or connect to an API
    private fun predictWords(text: String): List<String> {
        if (text.isEmpty()) return emptyList()
        
        val commonWords = listOf(
            "the", "be", "to", "of", "and", "a", "in", "that", "have", "I", 
            "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
            "hello", "thanks", "please", "good", "nice", "great", "awesome"
        )
        
        val lastWord = text.split(" ").lastOrNull()?.lowercase() ?: ""
        if (lastWord.isEmpty()) return emptyList()
        
        return commonWords
            .filter { it.startsWith(lastWord) && it != lastWord }
            .take(5)
            .toMutableList()
            .apply { if (isNotEmpty() && !contains(lastWord)) add(0, lastWord) }
    }

    // Override onUpdateSelection to update suggestions
    override fun onUpdateSelection(oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        
        // Get current text in the editor
        val inputConnection = currentInputConnection
        if (inputConnection != null) {
            val textBeforeCursor = inputConnection.getTextBeforeCursor(100, 0)?.toString() ?: ""
            val predictions = predictWords(textBeforeCursor)
            updateSuggestions(predictions)
        }
    }
}










