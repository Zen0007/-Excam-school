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

    override fun onCreate() {
        super.onCreate()
        // WindowManager untuk menambahkan View
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Inflate layout untuk overlay
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        // Set layout parameters untuk overlay
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.BOTTOM 
        // Tambahkan view ke WindowManager
        windowManager?.addView(overlayView, layoutParams)

        // Tambahkan aksi ke elemen UI
        val closeButton: Button = overlayView!!.findViewById(R.id.close_button)
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