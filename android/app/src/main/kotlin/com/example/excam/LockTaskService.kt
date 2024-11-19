package com.example.excam

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView


class LockTaskService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var contextView: Context = this

    override fun onCreate() {
        super.onCreate()
        // WindowManager untuk menambahkan View
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

                // Dapatkan ukuran layar
        // Metode 1: Menggunakan Context
        val displayMetrics = contextView.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        
        // Inflate layout for overlay
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
        
        // Get the height of the navigation bar
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val navigationBarHeight = if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
        
        // Set layout parameters for overlay
        val layoutParams = WindowManager.LayoutParams(
            screenWidth, // Full width of the screen
            (screenHeight / 2) + navigationBarHeight, // 50% height of the screen + navigation bar height
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        
        // Set position of the overlay
        layoutParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        
        // Add the overlay view to the window
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(overlayView, layoutParams)        

        // Tambahkan aksi ke elemen UI
        val closeButton: Button = overlayView!!.findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            stopSelf() // Tutup service
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hapus view saat service berhenti
        if (overlayView != null) {
            windowManager?.removeView(overlayView)
            overlayView = null
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        // Tampilkan overlay ketika aplikasi di-close
        val intent = Intent(this,LockTaskService::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startService(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}