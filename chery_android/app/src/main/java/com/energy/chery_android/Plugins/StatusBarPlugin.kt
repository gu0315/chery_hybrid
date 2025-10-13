package com.energy.chery_android.Plugins

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.jd.jdbridge.base.IBridgeCallback
import com.jd.jdbridge.base.IBridgePlugin
import com.jd.jdbridge.base.IBridgeWebView
import com.jd.jdcache.util.log
import org.json.JSONObject

/**
 * 状态栏相关功能插件
 * 提供状态栏高度、导航栏高度、屏幕高度等信息给 JavaScript 调用
 */
class StatusBarPlugin : IBridgePlugin {

    companion object {
        const val NAME = "StatusBarPlugin"
    }
    private val TAG = "StatusBarPlugin"
    override fun execute(
        webView: IBridgeWebView?,
        method: String?,
        params: String?,
        callback: IBridgeCallback?
    ): Boolean {
        val context = webView?.view?.context
        if (context !is Activity) {
            callback?.onError("Context is not Activity")
            return false
        }
        val statusBarHeightPx = getStatusBarHeight(context)
        val navBarHeightPx = getNavigationBarHeight(context)
        val screenHeightPx = getScreenHeight(context)
        val statusBarHeightDp = px2dp(context, statusBarHeightPx)
        val navBarHeightDp = px2dp(context, navBarHeightPx)
        val screenHeightDp = px2dp(context, screenHeightPx)

        val paramsMap = mutableMapOf<String, Any>(
            "statusBarHeight" to statusBarHeightDp,
            "navBarHeight" to navBarHeightDp,
            "screenHeight" to screenHeightDp
        )
        val jsonString = JSONObject(paramsMap as Map<*, *>).toString()
        Log.d(TAG, "execute: ${jsonString}")
        callback?.onSuccess(jsonString)
        return true
    }


    private fun px2dp(context: Context, pxValue: Int): Float {
        val scale = context.resources.displayMetrics.density
        return pxValue / scale
    }


    /** 获取状态栏高度 */
    private fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
    }

    /** 获取导航栏高度 */
    private fun getNavigationBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
    }

    /** 获取屏幕高度（包含状态栏+导航栏） */
    private fun getScreenHeight(context: Context): Int {
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
}
