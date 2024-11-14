package com.example.excam

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class LockTaskService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 1000L // Interval pengecekan setiap 1 detik

    private val runnable = object : Runnable {
        override fun run() {
            if (!isAppInForeground()) {
                // Bawa aplikasi kembali ke latar depan jika keluar dari foreground
                bringAppToForeground()
            }
            handler.postDelayed(this, checkInterval)
        }
    }

    override fun onCreate() {
        super.onCreate()
        handler.post(runnable) // Mulai pengecekan ketika layanan dimulai
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable) // Hentikan pengecekan jika layanan dihentikan
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // Fungsi untuk memeriksa apakah aplikasi berada di latar depan
    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                appProcess.processName == packageName
            ) {
                return true
            }
        }
        return false
    }

    // Fungsi untuk membawa aplikasi kembali ke latar depan
    private fun bringAppToForeground() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }
}
