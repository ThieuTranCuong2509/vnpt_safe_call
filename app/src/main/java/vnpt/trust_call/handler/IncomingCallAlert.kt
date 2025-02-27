package vnpt_trust_call.handler

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView

object BlocklistManager {
    val blockedNumbers = mutableListOf<String>()
    fun addNumber(number: String) {
        if (!blockedNumbers.contains(number)) {
            blockedNumbers.add(number)
        }
    }
}

class IncomingCallAlert {
    companion object {
        private const val WINDOW_WIDTH_RATIO = 0.8f
        private var windowManager: WindowManager? = null
        @SuppressLint("StaticFieldLeak")
        private var windowLayout: ViewGroup? = null
    }
    private var params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        windowType,
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.CENTER
        format = PixelFormat.TRANSLUCENT
    }
    private var x = 0f
    private var y = 0f
    private val windowType: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE
    private val WindowManager.windowWidth: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = currentWindowMetrics
            val insets = metrics.getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            (WINDOW_WIDTH_RATIO * (metrics.bounds.width() - insets.left - insets.right)).toInt()
        } else {
            DisplayMetrics().apply { defaultDisplay?.getMetrics(this) }.run { (WINDOW_WIDTH_RATIO * widthPixels).toInt() }
        }
    @SuppressLint("ClickableViewAccessibility")
    fun showWindow(context: Context, phone: String) {
        if (windowLayout != null) return
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager?.let { wm ->
            windowLayout = View.inflate(context, R.layout.window_call_info, null) as? ViewGroup
            windowLayout?.let { layout ->
                params.width = wm.windowWidth
                val numberTextView = layout.findViewById<TextView>(R.id.number)
                numberTextView.text = phone
                val alertTextView = layout.findViewById<TextView>(R.id.alert)
                if (isSpam(phone)) {
                    alertTextView.visibility = View.VISIBLE
                    alertTextView.text = "Phát hiện cuộc gọi lừa đảo hoặc spam!"
                    alertTextView.setTextColor(Color.RED)
                } else {
                    alertTextView.visibility = View.VISIBLE
                    alertTextView.text = "Cuộc gọi an toàn!"
                    alertTextView.setTextColor(Color.BLUE)
                }
                val acceptButton = layout.findViewById<Button>(R.id.btn_accept)
                val rejectButton = layout.findViewById<Button>(R.id.btn_reject)
                acceptButton.setOnClickListener { closeWindow() }
                rejectButton.setOnClickListener {
                    BlocklistManager.addNumber(phone)
                    closeWindow()
                }
                wm.addView(layout, params)
                setOnTouchListener(layout)
            }
        }
    }
    fun closeWindow() {
        windowLayout?.let { layout ->
            windowManager?.removeView(layout)
        }
        windowManager = null
        windowLayout = null
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setOnTouchListener(layout: View) {
        layout.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.rawX
                    y = event.rawY
                }
                MotionEvent.ACTION_MOVE -> updateWindowLayoutParams(event)
                MotionEvent.ACTION_UP -> view.performClick()
                else -> Unit
            }
            true
        }
    }
    private fun updateWindowLayoutParams(event: MotionEvent) {
        params.x -= (x - event.rawX).toInt()
        params.y -= (y - event.rawY).toInt()
        windowManager?.updateViewLayout(windowLayout, params)
        x = event.rawX
        y = event.rawY
    }
    private fun isSpam(phone: String): Boolean {
        return phone.startsWith("094")
    }
}
