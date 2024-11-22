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
    private var overlayView: View? = null
    private var contextView: Context = this
    private var windowManager: WindowManager? = null


    override fun onCreate() {
        super.onCreate()
           windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
          // Dapatkan ukuran layar
          val displayMetrics = contextView.resources.displayMetrics
          val screenWidth = displayMetrics.widthPixels
          val screenHeight = displayMetrics.heightPixels
         
          // Inflate layout for overlay
          overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
  
          // Set layout parameters for overlay
          val layoutParams = WindowManager.LayoutParams(
              screenWidth, // Full width of the screen
              //(screenHeight * 0.50).toInt(),
               screenHeight, // Half height of the screen
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                  WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
              else
                  WindowManager.LayoutParams.TYPE_PHONE,
              WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
              WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
              PixelFormat.TRANSLUCENT
          )
  
          // Set position of the overlay
          layoutParams.gravity = Gravity.BOTTOM 
  
          // Add the overlay view to the window
          val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
          windowManager.addView(overlayView, layoutParams)      

          // Simpan nama aktivitas saat ini
          val prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)
          val editor = prefs.edit()
          editor.putString("last_activity", MainActivity::class.java.name)
          editor.apply()
  
          // Tambahkan aksi ke elemen UI
          val closeButton: Button = overlayView!!.findViewById(R.id.closeButton)
          closeButton.setOnClickListener {
              // Ambil nama aktivitas terakhir dari SharedPreferences
            val prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)
            val lastActivity = prefs.getString("last_activity", null)

            // Jika ada aktivitas terakhir, mulai aktivitas tersebut
            if (lastActivity != null) {
                val intent = Intent()
                intent.setClassName(this, lastActivity)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Tambahkan flag untuk memulai aktivitas dari service
                startActivity(intent)
            }

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

        val prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        prefs.edit().apply {
            remove("last_activity")
        }.apply()
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