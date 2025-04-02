package com.relateos.keyboard

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manager class to handle data persistence and API communications
 * for the keyboard's extended functionality
 */
class KeyboardDataManager(context: Context) {
    private val preferences: SharedPreferences = 
        context.getSharedPreferences("keyboard_data", Context.MODE_PRIVATE)
    
    // Chat Assistant Section
    suspend fun getChatResponse(prompt: String): String {
        // TODO: Implement actual API call
        return withContext(Dispatchers.IO) {
            // Simulate API delay
            Thread.sleep(500)
            "This is a simulated response to: $prompt"
        }
    }
    
    fun saveRecentPrompt(prompt: String) {
        val recentPrompts = getRecentPrompts().toMutableList()
        if (prompt in recentPrompts) {
            recentPrompts.remove(prompt)
        }
        recentPrompts.add(0, prompt)
        if (recentPrompts.size > MAX_RECENT_ITEMS) {
            recentPrompts.removeAt(recentPrompts.lastIndex)
        }
        
        preferences.edit()
            .putString(KEY_RECENT_PROMPTS, recentPrompts.joinToString(SEPARATOR))
            .apply()
    }
    
    fun getRecentPrompts(): List<String> {
        val saved = preferences.getString(KEY_RECENT_PROMPTS, "") ?: ""
        return if (saved.isBlank()) emptyList() else saved.split(SEPARATOR)
    }
    
    // Clipboard Section
    fun saveClipboardItem(text: String) {
        val clipboardItems = getClipboardItems().toMutableList()
        if (text in clipboardItems) {
            clipboardItems.remove(text)
        }
        clipboardItems.add(0, text)
        if (clipboardItems.size > MAX_CLIPBOARD_ITEMS) {
            clipboardItems.removeAt(clipboardItems.lastIndex)
        }
        
        preferences.edit()
            .putString(KEY_CLIPBOARD_ITEMS, clipboardItems.joinToString(SEPARATOR))
            .apply()
    }
    
    fun getClipboardItems(): List<String> {
        val saved = preferences.getString(KEY_CLIPBOARD_ITEMS, "") ?: ""
        return if (saved.isBlank()) emptyList() else saved.split(SEPARATOR)
    }
    
    fun clearClipboard() {
        preferences.edit()
            .putString(KEY_CLIPBOARD_ITEMS, "")
            .apply()
    }
    
    // Settings
    fun saveSetting(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }
    
    fun getSetting(key: String, defaultValue: String = ""): String {
        return preferences.getString(key, defaultValue) ?: defaultValue
    }
    
    companion object {
        private const val SEPARATOR = "|||"
        private const val MAX_RECENT_ITEMS = 10
        private const val MAX_CLIPBOARD_ITEMS = 20
        
        // Storage keys
        private const val KEY_RECENT_PROMPTS = "recent_prompts"
        private const val KEY_CLIPBOARD_ITEMS = "clipboard_items"
        
        // Settings keys (can be used with saveSetting/getSetting)
        const val SETTING_THEME = "theme"
        const val SETTING_AUTO_CORRECT = "auto_correct"
        const val SETTING_AUTO_CAPS = "auto_capitalization"
        const val SETTING_API_KEY = "api_key"
        
        // Theme values
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
    }
}