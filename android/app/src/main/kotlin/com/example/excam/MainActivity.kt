

package com.example.excam
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import kotlin.math.abs
import io.flutter.embedding.engine.FlutterEngine
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class MainActivity : FlutterActivity() {
    private var isAppInBackground = false
    private var isGestureDetectionActive = false
    private lateinit var channel: MethodChannel
    private val CHANNEL = "com.example.app"  
    private var isStarted = false
    
    companion object {
        private const val PREFS_NAME = "AppPrefs"
        private const val IS_STARTED_KEY = "isStarted"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreState()
    }

     override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)

        channel.setMethodCallHandler { call, result ->
            when (call.method) {
                "true" -> {
                   isStarted = true
                   saveState()
                }
                else -> result.notImplemented()
            }
        }
    }


      private fun saveState() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(IS_STARTED_KEY, isStarted)
            apply()
        }
    }

    private fun restoreState() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isStarted = prefs.getBoolean(IS_STARTED_KEY, false)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isStarted) {
            // Membuat intent untuk kembali ke aplikasi
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isStarted) {
            // Membuat notification untuk kembali ke aplikasi
            showNotification()
        }
    }

    private fun showNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Membuat channel notification (diperlukan untuk Android 8.0 ke atas)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "app_channel",
                "App Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "app_channel")
            .setContentTitle("Aplikasi Masih Berjalan")
            .setContentText("Tap untuk kembali ke aplikasi")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1, notification)
    }

 
}

// import android.app.ActivityManager
// import android.content.Context
// import android.content.Intent
// import android.view.GestureDetector
// import android.view.MotionEvent
// import io.flutter.embedding.android.FlutterActivity
// import io.flutter.embedding.engine.FlutterEngine
// import io.flutter.plugin.common.MethodChannel
// import kotlin.math.abs

// class MainActivity: FlutterActivity() {
//     private val CHANNEL = "com.example.excam/app_control"
//     private lateinit var channel: MethodChannel
//     private val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
//         override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
//             if (e1 == null) return false
            
//             val diffY = e2.y - e1.y
//             val diffX = e2.x - e1.x
//             if (abs(diffY) > abs(diffX) && abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
//                 if (diffY < 0) { // Swipe dari bawah ke atas
//                     onSwipeUp()
//                 }
//                 return true
//             }
//             return false
//         }
//     })

//     private fun onSwipeUp() {
//         // Mengirimkan pesan ke Flutter untuk memberi tahu tentang swipe up
//         channel.invokeMethod("onSwipeUp", null)
//         bringAppToForeground()
//     }

//     private fun bringAppToForeground() {
//         val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//         val tasks = am.getRunningTasks(1)
        
//         if (tasks.isNotEmpty() && tasks[0].topActivity?.packageName != packageName) {
//             val intent = Intent(this, MainActivity::class.java).apply {
//                 action = Intent.ACTION_MAIN
//                 addCategory(Intent.CATEGORY_LAUNCHER)
//                 flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
//                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
//                        Intent.FLAG_ACTIVITY_SINGLE_TOP
//             }
//             startActivity(intent)
//         }
//     }

    // override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
    //     super.configureFlutterEngine(flutterEngine)
        // channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)

        // channel.setMethodCallHandler { call, result ->
        //     when (call.method) {
        //         "bringAppToForeground" -> {
        //             bringAppToForeground()
        //             result.success(null)
        //         }
        //         else -> result.notImplemented()
        //     }
        // }
    // }

//     override fun onTouchEvent(event: MotionEvent): Boolean {
//         return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
//     }

//     companion object {
//         private const val SWIPE_THRESHOLD = 100 // Threshold untuk swipe
//         private const val SWIPE_VELOCITY_THRESHOLD = 100 // Kecepatan untuk swipe
//     }
// }
// import android.app.PendingIntent
// import android.app.admin.DevicePolicyManager
// import android.app.admin.SystemUpdatePolicy
// import android.content.ComponentName
// import android.content.Context
// import android.content.Intent
// import android.content.IntentFilter
// import android.os.BatteryManager
// import android.os.Bundle
// import android.os.Handler
// import android.os.UserManager
// import android.provider.Settings
// import android.view.View
// import androidx.appcompat.app.AlertDialog
// import io.flutter.embedding.android.FlutterActivity
// import io.flutter.embedding.engine.FlutterEngine
// import io.flutter.plugin.common.MethodChannel
// import android.widget.Toast
// import android.view.GestureDetector
// import android.view.MotionEvent
// import kotlin.math.abs



// class MainActivity : FlutterActivity(){


//     private lateinit var mAdminComponentName: ComponentName
//     private lateinit var mDevicePolicyManager: DevicePolicyManager
//     private lateinit var alertDialog: AlertDialog
//     private lateinit var mUserManager: UserManager
//     private lateinit var gestureDetector: GestureDetector

   

//     companion object {
//         const val CHANNEL = "kiosk_mode_channel"
//         private const val REQUEST_CODE_ADMIN = 1
//     }

//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
        
//         mAdminComponentName = MyAdminReceiver.getComponentName(this)
//         mDevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//         mUserManager = getSystemService(Context.USER_SERVICE) as UserManager
      
    
//         Toast.makeText(this, "not allow to swipe bottom", Toast.LENGTH_SHORT).show()
//         // Now it's safe to call isAdmin()
//         // val isAdmin = isAdmin()
//         // if (!isAdmin) {
//         //     requestAdminPermission()
//         // } else {
//         //    yesIsAdmin()
//         // }
//     }
     
//      override fun onTouchEvent(event: MotionEvent): Boolean {
//         gestureDetector.onTouchEvent(event)
//         return super.onTouchEvent(event)
//     }

//     override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
//         super.configureFlutterEngine(flutterEngine)


//         val isAdmin = isAdmin() // This should be safe now
//         MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
//             when (call.method) {
//                 "startKioskMode" -> {
//                    // setKioskPolicies(true, isAdmin)
                   
//                     result.success(null)
//                     Toast.makeText(this, "Admin Device start", Toast.LENGTH_SHORT).show()
//                 }
//                 "stopKioskMode" -> {
//                    // setKioskPolicies(false, isAdmin)
                   
//                     result.success(null)
//                     Toast.makeText(this, "Admin Device stop", Toast.LENGTH_SHORT).show()
//                 }
//                 else -> result.notImplemented()
//             }
//         }
//     }
     
    


//     private fun requestAdminPermission() {
//         val  builder  =  AlertDialog.Builder(this)
//         builder.setTitle("Admin Permission Required")
//              .setMessage("This app requires admin permissions to run in kiosk mode.")
//              .setPositiveButton("Yes") { _, _ ->
//                  val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
//                  intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminComponentName)
//                  startActivityForResult(intent, REQUEST_CODE_ADMIN)
//              }
//              .setNegativeButton("Cancel") { dialog, _ ->
//                  dialog.dismiss()
//                  finish()
//              }
//         val  alertDialog = builder.create()
//              alertDialog.setTitle("Admin Permission Required")
//              alertDialog.show()
//     }

//     private fun isAdmin(): Boolean {
//         // Check if mDevicePolicyManager is initialized before accessing it
//         return if (::mDevicePolicyManager.isInitialized) {
//             mDevicePolicyManager.isDeviceOwnerApp(packageName)
//         } else {
//             false
//         }
//     }

//     private fun setKioskPolicies(enable: Boolean, isAdmin: Boolean) {
//         if (isAdmin) {
//             setRestrictions(enable)
//             enableStayOnWhilePluggedIn(enable)
//             setUpdatePolicy(enable)
//             setAsHomeApp(enable)
//             setKeyGuardEnabled(enable)
//         }
//         setLockTask(enable, isAdmin)
//         setImmersiveMode(enable)
//     }

//     private fun enableStayOnWhilePluggedIn(active: Boolean) {
//         val settingValue = if (active) {
//             (BatteryManager.BATTERY_PLUGGED_AC or BatteryManager.BATTERY_PLUGGED_USB or BatteryManager.BATTERY_PLUGGED_WIRELESS).toString()
//         } else {
//             "0"
//         }
//         mDevicePolicyManager.setGlobalSetting(mAdminComponentName, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, settingValue)
//     }

//     private fun yesIsAdmin() {
//         val admin = isAdmin()
//         val builder: AlertDialog.Builder = AlertDialog.Builder(this)
//         builder.setMessage("is admin on ")
//             .setTitle("admin on : $admin ")
//             .setCancelable(false)

//         alertDialog = builder.create()
//         alertDialog.show()

//         Handler().postDelayed({
//             if (alertDialog.isShowing) {
//                 alertDialog.dismiss()
//             }
//         }, 3000)
//     }

//     private fun setAsHomeApp(enable: Boolean) {
//         if (enable) {
//             val intentFilter = IntentFilter(Intent.ACTION_MAIN).apply {
//                 addCategory(Intent.CATEGORY_HOME)
//                 addCategory(Intent.CATEGORY_DEFAULT)
//             }
//             mDevicePolicyManager.addPersistentPreferredActivity(
//                 mAdminComponentName, intentFilter, ComponentName(packageName, MainActivity::class.java.name)
//             )
//         } else {
//             mDevicePolicyManager.clearPackagePersistentPreferredActivities(mAdminComponentName, packageName)
//         }
//     }

//     private fun setLockTask(start: Boolean, isAdmin: Boolean) {
//         if (isAdmin) {
//             mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, if (start) arrayOf(packageName) else arrayOf())
//         }
//         if (start) {
//             startLockTask()
//         } else {
//             stopLockTask()
//         }
//     }

//     @Suppress("DEPRECATION")
//     private fun setImmersiveMode(enable: Boolean) {
//         val flags = if (enable) {
//             (View.SYSTEM_UI_FLAG_LAYOUT_STABLE 
//             or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION 
//             or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN 
//             or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION 
//             or View.SYSTEM_UI_FLAG_FULLSCREEN 
//             or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
//         } else {
//             (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
//         }
//         window.decorView.systemUiVisibility = flags
//     }

//     private fun setRestrictions(disallow: Boolean) {
//         mUserManager.setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, disallow)
//         mUserManager.setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, disallow)
//         mUserManager.setUserRestriction(UserManager.DISALLOW_ADD_USER, disallow)
//         mUserManager.setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, disallow)
//         mUserManager.setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, disallow)
//         mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, disallow)
//     }

//     private fun setKeyGuardEnabled(enable: Boolean) {
//         mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, !enable)
//     }

//     private fun setUpdatePolicy(enable: Boolean) {
//         if (enable) {
//             mDevicePolicyManager.setSystemUpdatePolicy(
//                 mAdminComponentName,
//                 SystemUpdatePolicy.createWindowedInstallPolicy(60, 120)
//             )
//         } else {
//             mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName, null)
//         }
//     }
// }


