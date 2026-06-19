package com.Mayuri.osmosaddemo.utils

import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver

class VisibilityTracker(
    private val onVisible: () -> Unit
) {
    private var hasTriggered = false
    private var scrollListener: ViewTreeObserver.OnScrollChangedListener? = null
    private var layoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    fun attach(view: View) {
        scrollListener = ViewTreeObserver.OnScrollChangedListener {
            checkVisibility(view)
        }
        layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            checkVisibility(view)
        }
        view.viewTreeObserver.addOnScrollChangedListener(scrollListener)
        view.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }

    private fun checkVisibility(view: View) {
        if (hasTriggered) return
        if (!view.isShown) return

        val rect = Rect()
        val isVisible = view.getGlobalVisibleRect(rect)

        if (isVisible) {
            val visibleHeight = rect.height().toFloat()
            val totalHeight = view.height.toFloat()

            if (totalHeight > 0 && (visibleHeight / totalHeight) >= 0.5f) {
                hasTriggered = true
                onVisible()
            }
        }
    }

    fun reset() {
        hasTriggered = false
    }

    fun detach(view: View) {
        scrollListener?.let {
            view.viewTreeObserver.removeOnScrollChangedListener(it)
        }
        layoutListener?.let {
            view.viewTreeObserver.removeOnGlobalLayoutListener(it)
        }
        scrollListener = null
        layoutListener = null
    }
}