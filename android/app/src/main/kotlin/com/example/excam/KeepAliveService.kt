import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.view.GestureDetector
import android.view.MotionEvent
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import kotlin.math.abs

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.example.excam/app_control"
    private lateinit var channel: MethodChannel
    private val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (e1 == null) return false
            
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x
            if (abs(diffY) > abs(diffX) && abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY < 0) { // Swipe dari bawah ke atas
                    onSwipeUp()
                }
                return true
            }
            return false
        }
    })

    private fun onSwipeUp() {
        // Mengirimkan pesan ke Flutter untuk memberi tahu tentang swipe up
        channel.invokeMethod("onSwipeUp", null)
        bringAppToForeground()
    }

    private fun bringAppToForeground() {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = am.getRunningTasks(1)
        
        if (tasks.isNotEmpty() && tasks[0].topActivity?.packageName != packageName) {
            val intent = Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                       Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                       Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)

        channel.setMethodCallHandler { call, result ->
            when (call.method) {
                "bringAppToForeground" -> {
                    bringAppToForeground()
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    companion object {
        private const val SWIPE_THRESHOLD = 100 // Threshold untuk swipe
        private const val SWIPE_VELOCITY_THRESHOLD = 100 // Kecepatan untuk swipe
    }
}