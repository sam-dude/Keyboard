package com.relateos.keyboard

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.content.res.Configuration
import androidx.core.content.ContextCompat
import com.relateos.keyboard.R
import com.relateos.keyboard.databinding.KeyboardLayoutBinding

class MyKeyboard : InputMethodService() {
    
    private lateinit var keyboardBinding: KeyboardLayoutBinding
    
    companion object {
        val buttonIds = arrayOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnQ, R.id.btnW, R.id.btnE, R.id.btnR, R.id.btnT, R.id.btnY, R.id.btnU, R.id.btnI, R.id.btnO, R.id.btnP,
            R.id.btnA, R.id.btnS, R.id.btnD, R.id.btnF, R.id.btnG, R.id.btnH, R.id.btnJ, R.id.btnK, R.id.btnL,
            R.id.btnZ, R.id.btnX, R.id.btnC, R.id.btnV, R.id.btnB, R.id.btnN, R.id.btnM, R.id.btnDot, R.id.btnComma,
            R.id.btnSpace, R.id.btnEnter, R.id.btnBackSpace
        )
    }
    
    override fun onCreateInputView(): View {
        keyboardBinding = KeyboardLayoutBinding.inflate(layoutInflater)
        
        // Apply theme based styling
        applyThemeBasedStyling()
        
        for (buttonId in buttonIds) {
            val button = keyboardBinding.root.findViewById<Button>(buttonId)
            button?.setOnClickListener {
                val inputConnection = currentInputConnection
                inputConnection?.commitText(button.text.toString(), 1)
            }
        }
        
        keyboardBinding.btnBackSpace.setOnClickListener {
            val inputConnection = currentInputConnection
            inputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
            return@setOnClickListener
            
        }
        
        keyboardBinding.btnEnter.setOnClickListener {
            val inputConnection = currentInputConnection
            inputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            return@setOnClickListener
            
        }
        
        keyboardBinding.btnSpace.setOnClickListener {
            val inputConnection = currentInputConnection
            inputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE))
            return@setOnClickListener
            
        }
        return keyboardBinding.root
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyThemeBasedStyling()
    }
    
    private fun applyThemeBasedStyling() {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                // Dark mode is active
                keyboardBinding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
                setButtonStyle(R.style.btnDarkTheme)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                // Light mode is active
                keyboardBinding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                setButtonStyle(R.style.btnLightTheme)
            }
        }
    }
    
    private fun setButtonStyle(styleResId: Int) {
        for (buttonId in buttonIds) {
            val button = keyboardBinding.root.findViewById<Button>(buttonId)
            button?.setTextAppearance(styleResId)
            // Manually set background because textAppearance might not override it
            if (styleResId == R.style.btnDarkTheme) {
                button?.background = ContextCompat.getDrawable(this, R.drawable.btn_ripple)
            } else {
                button?.background = ContextCompat.getDrawable(this, R.drawable.btn_white_ripple)
            }
        }
    }
}










