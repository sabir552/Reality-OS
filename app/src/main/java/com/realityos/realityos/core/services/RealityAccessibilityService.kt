package com.realityos.realityos.core.services

import android.accessibilityservice.AccessibilityService
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.realityos.realityos.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow // THE MISSING IMPORT
import kotlinx.coroutines.launch

class RealityAccessibilityService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private lateinit var windowManager: WindowManager
    private var grayscaleView: android.view.View? = null
    private var blockView: android.view.View? = null

    companion object {
        // These are now correctly defined because of the import
        val currentForegroundApp = MutableStateFlow<String?>(null)
        val isGreyscaleActive = MutableStateFlow(false)
        val isBlockActive = MutableStateFlow(false)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        observePunishments()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.packageName?.let {
                // Ignore our own app and the launcher
                if(it.toString() != "com.realityos.realityos" && it.toString() != "com.google.android.apps.nexuslauncher") {
                   currentForegroundApp.value = it.toString()
                }
            }
        }
    }

    private fun observePunishments() {
        scope.launch {
            isGreyscaleActive.collect { isActive ->
                if (isActive) {
                    showGreyscaleOverlay()
                } else {
                    hideGreyscaleOverlay()
                }
            }
        }
        scope.launch {
            isBlockActive.collect { isActive ->
                if (isActive) {
                    showBlockOverlay()
                } else {
                    hideBlockOverlay()
                }
            }
        }
    }

    private fun showGreyscaleOverlay() {
        if (grayscaleView == null) {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.TOP or Gravity.START

            Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer_enabled", 1)
            Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer", 0)

            val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            grayscaleView = layoutInflater.inflate(R.layout.grayscale_overlay, null)
            windowManager.addView(grayscaleView, params)
        }
    }

    private fun hideGreyscaleOverlay() {
        Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer_enabled", 0)
        grayscaleView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // View already gone
            }
            grayscaleView = null
        }
    }

    private fun showBlockOverlay() {
        if (blockView == null) {
            val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            blockView = layoutInflater.inflate(R.layout.block_overlay, null)
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                0,
                PixelFormat.TRANSLUCENT
            )
            windowManager.addView(blockView, params)
        }
    }

    private fun hideBlockOverlay() {
        blockView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // View already gone
            }
            blockView = null
        }
    }

    override fun onInterrupt() {}
    override fun onConfigurationChanged(newConfig: Configuration) {}

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        hideGreyscaleOverlay()
        hideBlockOverlay()
    }
}
