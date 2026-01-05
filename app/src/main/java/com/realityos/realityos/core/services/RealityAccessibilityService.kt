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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class RealityAccessibilityService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private lateinit var windowManager: WindowManager
    private var blockView: android.view.View? = null

    companion object {
        val currentForegroundApp = MutableStateFlow<String?>(null)
        // Greyscale feature is temporarily disabled to prevent crashing.
        // val isGreyscaleActive = MutableStateFlow(false)
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
                // Ignore our own app and common launchers
                val packageName = it.toString()
                if(packageName != "com.realityos.realityos" && !packageName.contains("launcher")) {
                   currentForegroundApp.value = packageName
                }
            }
        }
    }

    private fun observePunishments() {
        // Greyscale observation is removed
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

    // --- GREYSCALE FUNCTIONALITY IS DISABLED TO PREVENT CRASH ---
    // The previous method of using Settings.Secure.putInt is forbidden by Android
    // and was causing a SecurityException crash.
    private fun showGreyscaleOverlay() {
        // Do nothing for now to ensure stability.
    }

    private fun hideGreyscaleOverlay() {
        // Do nothing for now.
    }
    // --- END OF DISABLED GREYSCALE ---

    private fun showBlockOverlay() {
        if (blockView == null) {
            val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            blockView = layoutInflater.inflate(R.layout.block_overlay, null)
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                0, // Intercept all touches
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
        hideBlockOverlay()
    }
}
