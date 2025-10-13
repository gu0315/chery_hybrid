package com.energy.chery_android.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

/**
 * 系统栏工具类
 * 封装状态栏和导航栏相关的功能
 * 包括获取高度、设置沉浸式模式等
 */
object SystemBarUtils {

    private const val TAG = "SystemBarUtils"
    private const val DEFAULT_STATUS_BAR_HEIGHT_DP = 24

    /**
     * 获取状态栏高度（像素）
     * @param context Context对象
     * @return 状态栏高度（像素）
     */
    fun getStatusBarHeightPx(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            return context.resources.getDimensionPixelSize(resourceId)
        }

        // 获取失败时使用默认值
        return dpToPx(context, DEFAULT_STATUS_BAR_HEIGHT_DP)
    }

    /**
     * 获取状态栏高度（dp）
     * @param context Context对象
     * @return 状态栏高度（dp）
     */
    fun getStatusBarHeightDp(context: Context): Float {
        val statusBarHeightPx = getStatusBarHeightPx(context)
        return pxToDp(context, statusBarHeightPx)
    }

    /**
     * 获取导航栏高度（像素）
     * @param context Context对象
     * @return 导航栏高度（像素）
     */
    fun getNavigationBarHeightPx(context: Context): Int {
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
    }

    /**
     * 获取导航栏高度（dp）
     * @param context Context对象
     * @return 导航栏高度（dp）
     */
    fun getNavigationBarHeightDp(context: Context): Float {
        val navBarHeightPx = getNavigationBarHeightPx(context)
        return pxToDp(context, navBarHeightPx)
    }

    /**
     * 获取屏幕高度（包含状态栏和导航栏）
     * @param context Context对象
     * @return 屏幕高度（像素）
     */
    fun getScreenHeightPx(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display = context.display
            display?.getRealMetrics(metrics)
        } else {
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(metrics)
        }
        return metrics.heightPixels
    }

    /**
     * 获取屏幕高度（dp）
     * @param context Context对象
     * @return 屏幕高度（dp）
     */
    fun getScreenHeightDp(context: Context): Float {
        val screenHeightPx = getScreenHeightPx(context)
        return pxToDp(context, screenHeightPx)
    }

    /**
     * 启用沉浸式状态栏（透明状态栏，内容延伸到状态栏区域）
     * @param activity Activity对象
     */
    fun enableImmersiveStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 使用WindowInsetsController
            activity.window.setDecorFitsSystemWindows(false)
            val insetsController = activity.window.insetsController
            if (insetsController != null) {
                insetsController.show(WindowInsets.Type.statusBars())
                insetsController.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
                insetsController.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                )
            }
            activity.window.statusBarColor = android.graphics.Color.TRANSPARENT
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0-10 使用SystemUiVisibility
            @Suppress("InlinedApi")
            val flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            activity.window.decorView.systemUiVisibility = flags
            activity.window.statusBarColor = android.graphics.Color.TRANSPARENT
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Android 4.4-5.1 使用透明状态栏标志
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    /**
     * 禁用沉浸式状态栏，恢复默认状态
     * @param activity Activity对象
     */
    fun disableImmersiveStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.setDecorFitsSystemWindows(true)
            val insetsController = activity.window.insetsController
            if (insetsController != null) {
                insetsController.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
                insetsController.show(WindowInsets.Type.statusBars())
            }
            activity.window.statusBarColor = android.graphics.Color.WHITE
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            activity.window.statusBarColor = android.graphics.Color.WHITE
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = android.graphics.Color.WHITE
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    /**
     * 启用沉浸式导航栏（透明导航栏，内容延伸到导航栏区域）
     * @param activity Activity对象
     */
    fun enableImmersiveNavigationBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 使用WindowInsetsController
            activity.window.setDecorFitsSystemWindows(false)
            val insetsController = activity.window.insetsController
            if (insetsController != null) {
                insetsController.show(WindowInsets.Type.navigationBars())
                // Android 11+支持浅色导航栏图标
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    insetsController.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    )
                }
                insetsController.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                )
            }
            activity.window.navigationBarColor = android.graphics.Color.TRANSPARENT
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0-10 使用SystemUiVisibility
            @Suppress("InlinedApi")
            var flags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            
            // Android 8.0+支持浅色导航栏图标
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            
            activity.window.decorView.systemUiVisibility = flags
            activity.window.navigationBarColor = android.graphics.Color.TRANSPARENT
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Android 4.4-5.1 使用透明导航栏标志
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
    }

    /**
     * 禁用沉浸式导航栏，恢复默认状态
     * @param activity Activity对象
     */
    fun disableImmersiveNavigationBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.setDecorFitsSystemWindows(true)
            val insetsController = activity.window.insetsController
            if (insetsController != null) {
                insetsController.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS)
                insetsController.show(WindowInsets.Type.navigationBars())
            }
            activity.window.navigationBarColor = android.graphics.Color.WHITE
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 清除浅色导航栏标志
            val currentFlags = activity.window.decorView.systemUiVisibility
            val newFlags = currentFlags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            activity.window.decorView.systemUiVisibility = newFlags
            activity.window.navigationBarColor = android.graphics.Color.WHITE
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            activity.window.navigationBarColor = android.graphics.Color.WHITE
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
    }

    /**
     * 启用全屏沉浸式模式（透明状态栏和导航栏，内容延伸到整个屏幕）
     * @param activity Activity对象
     */
    fun enableFullImmersiveMode(activity: Activity) {
        enableImmersiveStatusBar(activity)
        enableImmersiveNavigationBar(activity)
    }

    /**
     * 禁用全屏沉浸式模式，恢复默认状态
     * @param activity Activity对象
     */
    fun disableFullImmersiveMode(activity: Activity) {
        disableImmersiveStatusBar(activity)
        disableImmersiveNavigationBar(activity)
    }

    /**
     * 启用沉浸式模式（可选择启用状态栏和导航栏的沉浸式模式）
     * @param activity Activity对象
     * @param enableStatusBarImmersive 是否启用状态栏沉浸式模式
     * @param enableNavigationBarImmersive 是否启用导航栏沉浸式模式
     */
    fun enableImmersiveMode(activity: Activity, enableStatusBarImmersive: Boolean, enableNavigationBarImmersive: Boolean) {
        if (enableStatusBarImmersive) {
            enableImmersiveStatusBar(activity)
        }
        if (enableNavigationBarImmersive) {
            enableImmersiveNavigationBar(activity)
        }
    }

    /**
     * 禁用沉浸式模式，恢复默认状态栏和导航栏样式
     * @param activity Activity对象
     */
    fun disableImmersiveMode(activity: Activity) {
        disableImmersiveStatusBar(activity)
        disableImmersiveNavigationBar(activity)
    }

    /**
     * dp转px
     * @param context Context对象
     * @param dp dp值
     * @return 转换后的px值
     */
    fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    /**
     * px转dp
     * @param context Context对象
     * @param px px值
     * @return 转换后的dp值
     */
    fun pxToDp(context: Context, px: Int): Float {
        val density = context.resources.displayMetrics.density
        return px / density
    }

    /**
     * 检查导航栏是否可见
     * @param activity Activity对象
     * @return true表示导航栏可见，false表示隐藏
     */
    fun isNavigationBarVisible(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insetsController = activity.window.insetsController
            if (insetsController != null) {
                try {
                    // 使用反射来调用isVisible方法，避免API兼容性问题
                    val isVisibleMethod = insetsController.javaClass.getMethod("isVisible", WindowInsets.Type::class.java)
                    val result = isVisibleMethod.invoke(insetsController, WindowInsets.Type.navigationBars())
                    return result as? Boolean ?: true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to check navigation bar visibility: ${e.message}")
                    // 如果反射调用失败，返回默认值
                    return true
                }
            }
        }
        // 对于旧版本，我们无法直接检查导航栏是否可见，返回默认值
        return true
    }

    /**
     * 创建包含系统栏信息的UserAgent字符串
     * @param context Context对象
     * @param originalUserAgent 原始UserAgent
     * @return 包含系统栏信息的UserAgent
     */
    fun createCustomUserAgent(context: Context, originalUserAgent: String): String {
        val statusBarHeight = getStatusBarHeightDp(context).toInt()
        val navigationBarHeight = getNavigationBarHeightDp(context).toInt()
        
        return "$originalUserAgent StatusBarHeight/$statusBarHeight NavigationBarHeight/$navigationBarHeight"
    }
}