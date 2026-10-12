package com.example.actividad_12

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Pads this view so its content is not drawn under the system bars or the keyboard
 * (edge-to-edge is enforced when targeting API 35+).
 */
fun View.applySystemBarInsets() {
    val left = paddingLeft
    val top = paddingTop
    val right = paddingRight
    val bottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val bars = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime(),
        )
        view.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom)
        WindowInsetsCompat.CONSUMED
    }
}
