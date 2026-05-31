package util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun View.applyPaddingSystemBarsBottom() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
        v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, bottom)
        insets
    }
    ViewCompat.requestApplyInsets(this)
}

fun View.applyPaddingSystemBarsTop() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
        v.setPadding(v.paddingLeft, top, v.paddingRight, v.paddingBottom)
        insets
    }
    ViewCompat.requestApplyInsets(this)
}