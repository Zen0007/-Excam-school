package com.example.excam

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import io.flutter.embedding.engine.FlutterEngine
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.widget.Button
import android.widget.TextView

class MainActivity : FlutterActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var showDialogRunnable: Runnable
    private var isDialogShowing = false // Track if the dialog is currently showing
    private val REQUEST_CODE = 1

    companion object {
        const val CHANNEL = "kiosk_mode_channel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

          // Check SYSTEM_ALERT_WINDOW permission
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Log.d("MainActivity", "Requesting overlay permission")
                // Delay showing the dialog to ensure the activity is fully created
                showOverlayPermissionDialog(this.context)
            }
        }
      
    }
    
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "startKioskMode" -> {
                    isDialogShowing = true 
                }
                "stopKioskMode" -> {
                    isDialogShowing = false 
                }
                else -> result.notImplemented()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!checkDrawOverlayPermission()) {
                    Toast.makeText(this, "Izin tidak diberikan. Silakan aktifkan izin overlay.", Toast.LENGTH_SHORT).show()

                    AlertDialog.Builder(this)
                    .setTitle("Izin Overlay Diperlukan")
                    .setMessage("Aplikasi ini memerlukan izin untuk menampilkan overlay. Silakan aktifkan di pengaturan.")
                    .setPositiveButton("Buka Pengaturan") { _, _ ->
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$packageName"))
                        startActivity(intent)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
                }
            }
        }
    }

    private fun checkDrawOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true // Untuk versi di bawah Marshmallow
        }
    }
    
    private fun startOverlayService() {
        // Start overlay service if needed
        val serviceIntent = Intent(this, LockTaskService::class.java)
       startService(serviceIntent)
    }
    
    private fun showOverlayPermissionDialog(context: Context) {
        Log.d("MainActivity", "Attempting to show overlay permission dialog")
    
        // Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_overlay_permission, null)
    
        // Create the dialog
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false) // Dialog tidak bisa ditutup tanpa aksi
            .create()
        
        dialogView.findViewById<Button>(R.id.button_ok).setOnClickListener {
            if (!checkDrawOverlayPermission()) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"))
                startActivityForResult(intent, REQUEST_CODE)
                dialog.dismiss()
            }else{
                dialog.dismiss()
            }
        }
    
        dialogView.findViewById<Button>(R.id.button_cancel).setOnClickListener {
            Toast.makeText(this, "Izin tidak diberikan. Silakan aktifkan izin overlay.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            finish() // Close the app if permission is not granted
        }
    
        // Show the dialog
        dialog.show()
    }

    override fun onStop() {
        super.onStop()
        Toast.makeText(this, "Exiting the app", Toast.LENGTH_SHORT).show()
    
        // Simpan aktivitas terakhir saat aplikasi berhenti
        saveLastActivity()
    
        // Jika dialog masih menunjukkan, mulai service overlay
        if (isDialogShowing) {
            startOverlayService()
        }
    }
    
    override fun onBackPressed() {
        // Simpan aktivitas terakhir
        saveLastActivity()
    
        // Tampilkan toast saat tombol kembali ditekan
        Toast.makeText(this, "Back to menu the app", Toast.LENGTH_SHORT).show()
    
        // Jika dialog masih menunjukkan, mulai service overlay
        if (isDialogShowing) {
            startOverlayService()
        }
    
        // Ambil nama aktivitas terakhir dari SharedPreferences
        val prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val lastActivity = prefs.getString("last_activity", null)
    
        // Jika ada aktivitas terakhir, mulai aktivitas tersebut
        if (lastActivity != null) {
            try {
                // Gunakan Class.forName untuk mendapatkan kelas aktivitas
                val activityClass = Class.forName(lastActivity)
                
                // Buat intent dengan cara yang lebih aman
                val intent = Intent(this, activityClass)
                
                // Tambahkan flag untuk menghindari restart
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or 
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                )
                
                // Mulai aktivitas
                startActivity(intent)
                
                // Pastikan aktivitas saat ini ditutup
                finish()
            } catch (e: ClassNotFoundException) {
                // Tangani kesalahan jika kelas aktivitas tidak ditemukan
                Log.e("BackPressed", "Aktivitas tidak ditemukan: $lastActivity", e)
                super.onBackPressed()
            }
        } else {
            // Jika tidak ada aktivitas terakhir, gunakan perilaku default
            super.onBackPressed()
        }
    }
    
    override fun onPause() {
        super.onPause()
        saveLastActivity()  // Simpan aktivitas terakhir saat aplikasi dipause
    
        // Tampilkan toast saat aplikasi berada di background
        Toast.makeText(this, "App is in the background", Toast.LENGTH_SHORT).show()
    
        // Jika dialog masih menunjukkan, mulai service overlay
        if (isDialogShowing) {
            startOverlayService()
        }
    }
    
    // Fungsi untuk menyimpan aktivitas terakhir ke SharedPreferences
    private fun saveLastActivity() {
        val prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("last_activity", MainActivity::class.java.name)
        editor.apply()
    }
    
    // Menangani status saat jendela fokus berubah
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
    
        if (!hasFocus) {
            // Simpan aktivitas terakhir saat kehilangan fokus
            saveLastActivity()
    
            // Mencegah aplikasi masuk ke background dengan memindahkan task ke home screen
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
    
            // Jika dialog masih menunjukkan, mulai service overlay
            if (isDialogShowing) {
                startOverlayService()
            }
    
            Toast.makeText(this, "App is not focused", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "App is focused", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Menyimpan data aktivitas saat keadaan instance disimpan
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    
        // Simpan nama aktivitas terakhir yang relevan
        outState.putString("last_activity", MainActivity::class.java.name)
    }
    
    // Mengambil data aktivitas yang disimpan saat instance dipulihkan
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    
        // Mengambil nama aktivitas terakhir dari savedInstanceState
        val lastActivity = savedInstanceState.getString("last_activity")
        if (lastActivity != null) {
            // Jika ada nama aktivitas yang disimpan, jalankan aktivitas tersebut
            val intent = Intent()
            intent.setClassName(this, lastActivity)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
    
    // Mengambil aktivitas terakhir saat aplikasi kembali dari latar belakang (di onResume)
    override fun onResume() {
        super.onResume()
    
        // Mengambil nama aktivitas terakhir dari SharedPreferences
        val prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val lastActivity = prefs.getString("last_activity", null)
    
        if (lastActivity != null && lastActivity != MainActivity::class.java.name) {
            val intent = Intent()
            intent.setClassName(this, lastActivity)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
    
}








// import android.annotation.SuppressLint
// import android.app.Activity
// import android.app.admin.DevicePolicyManager
// import android.app.admin.SystemUpdatePolicy
// import android.app.PendingIntent
// import android.content.ComponentName
// import android.content.Context
// import android.content.Intent
// import android.content.IntentFilter
// import android.graphics.PixelFormat
// import android.net.Uri
// import android.os.BatteryManager
// import android.os.Build
// import android.os.Bundle
// import android.os.Handler
// import android.os.Looper
// import android.provider.Settings
// import android.view.*
// import android.widget.Toast
// import androidx.appcompat.app.AlertDialog
// import androidx.core.view.WindowInsetsControllerCompat
// import androidx.core.view.WindowInsetsCompat
// import io.flutter.embedding.android.FlutterActivity
// import io.flutter.embedding.engine.FlutterEngine
// import io.flutter.plugin.common.MethodChannel
// import android.app.admin.DeviceAdminReceiver
// import android.content.pm.PackageManager
// import android.os.UserManager



// class GestureBlockerManager(private val activity: Activity) {
//     private var overlayView: View? = null
//     private var windowManager: WindowManager? = null
//     private var isGestureBlocked = false

//     fun blockGestures() {
//         if (!isGestureBlocked) {
//             createOverlayView()
//             blockSystemNavigation()
//             isGestureBlocked = true
//         }
//     }

//     fun unblockGestures() {
//         if (isGestureBlocked) {
//             removeOverlayView()
//             restoreSystemNavigation()
//             isGestureBlocked = false
//         }
//     }

//     @SuppressLint("ClickableViewAccessibility")
//     private fun createOverlayView() {
//         // Hapus overlay sebelumnya jika ada
//         removeOverlayView()

//         // Buat WindowManager
//         windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager

//         // Buat overlay view
//         overlayView = View(activity).apply {
//             setBackgroundColor(android.graphics.Color.TRANSPARENT)
//             // Cegah semua sentuhan
//             setOnTouchListener { _, _ -> true } // Mencegah interaksi pengguna
//         }

//         // Parameter untuk overlay
//         val params = WindowManager.LayoutParams(
//             WindowManager.LayoutParams.MATCH_PARENT,
//             WindowManager.LayoutParams.MATCH_PARENT,
//             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//                 WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
//             else
//                 WindowManager.LayoutParams.TYPE_PHONE,
//             WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
//                     WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
//                     WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
//             PixelFormat.TRANSLUCENT
//         )

//         // Tambahkan posisi
//         params.gravity = Gravity.TOP or Gravity.START
//         params.x = 0
//         params.y = 0

//         // Pasang overlay
//         try {
//             windowManager?.addView(overlayView, params)
//         } catch (e: Exception) {
//             e.printStackTrace()
//         }
//     }

//     private fun removeOverlayView() {
//         overlayView?.let {
//             try {
//                 windowManager?.removeView(it)
//             } catch (e: Exception) {
//                 e.printStackTrace()
//             }
//             overlayView = null
//         }
//     }

//     private fun blockSystemNavigation() {
//         // Sembunyikan navigation bar
//         val windowInsetsController = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
//         windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
//         windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//     }

//     private fun restoreSystemNavigation() {
//         // Kembalikan navigation bar
//         val windowInsetsController = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
//         windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
//     }

//     fun isGesturesBlocked(): Boolean = isGestureBlocked
// }




// class MainActivity : FlutterActivity() {

//     private lateinit var mAdminComponentName: ComponentName
//     private lateinit var mDevicePolicyManager: DevicePolicyManager
//     private lateinit var alertDialog: AlertDialog
//     private lateinit var mUserManager: UserManager
//     private lateinit var gestureBlockerManager: GestureBlockerManager


//     companion object {
//         const val CHANNEL = "kiosk_mode_channel"
//         private const val REQUEST_CODE_ADMIN = 1
//         private const val REQUEST_CODE_OVERLAY_PERMISSION = 100
//     }

//     private var isKioskModeActive = false

//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)

//         mAdminComponentName = MyAdminReceiver.getComponentName(this)
//         mDevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//         mUserManager = getSystemService(Context.USER_SERVICE) as UserManager

//         val isAdmin = isAdmin()
//         if (! isAdmin) {
//             requestAdminPermission()
//             checkOverlayPermission()
//         } else {
//             yesIsAdmin()
           
//         }
//     }

//     override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
//         super.configureFlutterEngine(flutterEngine)
//         gestureBlockerManager = GestureBlockerManager(this)


//         val isAdmin = isAdmin()
//         MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
//             when (call.method) {
//                 "startKioskMode" -> {
//                     setKioskPolicies(true, isAdmin)
//                     result.success(null)
//                     Toast.makeText(this, "Admin Device start", Toast.LENGTH_SHORT).show()
//                     gestureBlockerManager.blockGestures()
                    
//                 }
//                 "stopKioskMode" -> {
//                     setKioskPolicies(false, isAdmin)
//                     result.success(null)
//                     stopGestureLock()
//                     Toast.makeText(this, "Admin Device stop", Toast.LENGTH_SHORT).show()
//                     gestureBlockerManager.unblockGestures()
//                 }
//                 else -> result.notImplemented()
//             }
//         }
//     }


//     private fun requestAdminPermission() {
//         window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE

//         val builder: AlertDialog.Builder = AlertDialog.Builder(this)
//         builder.setTitle("Admin Permission Required")
//             .setMessage("This app requires admin permissions to run in kiosk mode.")
//             .setPositiveButton("Yes") { _, _ ->
//                 val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
//                 intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminComponentName)
//                 startActivityForResult(intent, REQUEST_CODE_ADMIN)
//             }
//             .setNegativeButton("Cancel") { dialog, _ ->
//                 dialog.dismiss()
//                 finish()
//             }
//         alertDialog = builder.create()
//         alertDialog.show()
//     }

//     private fun isAdmin(): Boolean {
//         return if (::mDevicePolicyManager.isInitialized) {
//             mDevicePolicyManager.isDeviceOwnerApp(packageName)
//         } else {
//             false
//         }
//     }


//     private fun onPermissionGranted() {
//         // This function is called when overlay permission is granted
//         Toast.makeText(this, "Overlay permission granted", Toast.LENGTH_SHORT).show()
        
//         // Start any functionality that requires the overlay permission here
//         gestureBlockerManager.blockGestures()
        
//         // Check if kiosk mode should be enabled
//         if (isAdmin()) {
//             setKioskPolicies(true, true) // Enable kiosk mode
//         }
//     }


//     private fun checkOverlayPermission() {
//         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//             if (!Settings.canDrawOverlays(this)) {
//                 // Jika izin belum diberikan, arahkan pengguna ke pengaturan
//                 Toast.makeText(this, "Please allow overlay permission", Toast.LENGTH_SHORT).show()
//                 val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
//                 startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION)
//             } else {
//                 // Izin sudah diberikan, lanjutkan untuk menampilkan overlay
//                 onPermissionGranted()
//             }
//        }else {
//             // Untuk versi Android sebelum Marshmallow, izin sudah otomatis diberikan
//             onPermissionGranted()
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
//         if (enable) {
//             lockGestureNavigation(true)
//         } else {
//             lockGestureNavigation(false)
//         }
//         isKioskModeActive = enable
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

//     private fun setImmersiveMode(enable: Boolean) {
//         val flags = if (enable) {
//             (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
//              View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
//              View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
//         } else {
//             (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
//              View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
//         }
//         window.decorView.systemUiVisibility = flags


//         window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
//             if ((visibility and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
//                 setImmersiveMode(true)
//             }
//         }
//     }

//     private fun stopGestureLock() {
//         lockGestureNavigation(false)
//     }

//     private fun lockGestureNavigation(enable: Boolean) {
//         val params = window.attributes
//         if (enable) {
//             // Prevent navigation gestures but allow touch interactions on screen
//             params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
//         } else {
//             // Remove the flag and restore normal interaction
//             params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
//         }
//         window.attributes = params
//     }

   
//     override fun onResume() {
//         super.onResume()
//         if (isKioskModeActive) {
//             setImmersiveMode(true)  // Ensures immersive mode is active when kiosk mode is enabled
//         }
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
