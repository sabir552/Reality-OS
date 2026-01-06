package com.realityos.realityos.core.services

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class RealityAccessibilityService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private var windowManager: WindowManager? = null
    private var blockView: View? = null

    companion object {
        val currentForegroundApp = MutableStateFlow<String?>(null)
        val isBlockActive = MutableStateFlow(false)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("RealityOS_Service", "Service Connected")
        windowManager = getSystemService(WINDOW_SERVICE) as? WindowManager
        if (windowManager == null) {
            Log.e("RealityOS_Service", "FATAL: WindowManager is null.")
            return
        }
        observeBlocker()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.packageName?.toString()?.let { packageName ->
                if (packageName != application.packageName && !packageName.contains("launcher")) {
                    currentForegroundApp.value = packageName
                    Log.d("RealityOS_Service", "Foreground App: $packageName")
                }
            }
        }
    }

    private fun observeBlocker() {
        scope.launch {
            isBlockActive.collect { isActive ->
                Log.d("RealityOS_Service", "isBlockActive received: $isActive")
                if (isActive) {
                    showBlockOverlay()
                } else {
                    hideBlockOverlay()
                }
            }
        }
    }

    private fun showBlockOverlay() {
        if (windowManager == null) {
            Log.e("RealityOS_Service", "Cannot show overlay, WindowManager is null.")
            return
        }
        if (blockView == null) {
            Log.d("RealityOS_Service", "Attempting to show block overlay...")
            blockView = View(this).apply {
                setBackgroundColor(Color.BLACK)
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.CENTER
            try {
                windowManager?.addView(blockView, params)
                Log.d("RealityOS_Service", "Block overlay added successfully.")
            } catch (e: Exception) {
                Log.e("RealityOS_Service", "Error adding block overlay view", e)
            }
        }
    }

    private fun hideBlockOverlay() {
        blockView?.let { view ->
            Log.d("RealityOS_Service", "Attempting to hide block overlay...")
            try {
                windowManager?.removeView(view)
                Log.d("RealityOS_Service", "Block overlay removed successfully.")
            } catch (e: Exception) {
                Log.e("RealityOS_Service", "Error removing block overlay view", e)
            }
            blockView = null
        }
    }

    override fun onInterrupt() {
        Log.w("RealityOS_Service", "Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("RealityOS_Service", "Service Destroyed")
        job.cancel()
        hideBlockOverlay()
    }
}
