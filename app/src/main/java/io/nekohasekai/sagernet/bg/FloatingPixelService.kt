package io.nekohasekai.sagernet.bg

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore
import kotlinx.coroutines.*

class FloatingPixelService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private lateinit var params: WindowManager.LayoutParams

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private val settingsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "io.nekohasekai.sagernet.action.UPDATE_FLOATING_PIXEL_POSITION" -> {
                    updatePixelPosition()
                }
                "io.nekohasekai.sagernet.action.STOP_FLOATING_PIXEL" -> {
                    stopSelf()
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val intentFilter = IntentFilter().apply {
            addAction("io.nekohasekai.sagernet.action.UPDATE_FLOATING_PIXEL_POSITION")
            addAction("io.nekohasekai.sagernet.action.STOP_FLOATING_PIXEL")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(settingsReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(settingsReceiver, intentFilter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Permission not granted
            stopSelf()
            return START_NOT_STICKY
        }

        if (floatingView == null) {
            showFloatingPixel()
        }

        // Start in foreground to prevent being killed
        val notification = NotificationCompat.Builder(this, "service")
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Floating Pixel is running")
            .setSmallIcon(R.drawable.ic_service_active)
            .build()
        startForeground(1, notification)


        return START_STICKY
    }

    private fun showFloatingPixel() {
        floatingView = View(this).apply {
            setBackgroundColor(Color.argb(128, 255, 0, 0)) // Semi-transparent red
        }

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            1,
            1,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = DataStore.floatingPixelX
            y = DataStore.floatingPixelY
        }

        try {
            windowManager.addView(floatingView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updatePixelPosition() {
        floatingView?.let {
            params.x = DataStore.floatingPixelX
            params.y = DataStore.floatingPixelY
            try {
                windowManager.updateViewLayout(it, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        unregisterReceiver(settingsReceiver)
        floatingView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}