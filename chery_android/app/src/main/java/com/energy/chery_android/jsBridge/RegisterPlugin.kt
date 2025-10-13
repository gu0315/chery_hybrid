package com.energy.chery_android.jsBridge

import android.util.Log
import com.jd.hybrid.JDWebView
import com.jd.jdbridge.base.IBridgePlugin
import com.jd.jdbridge.base.registerDefaultPlugin

/**
 * 插件注册管理器
 * 负责集中注册所有的JS桥接插件
 */
class RegisterPlugin {
    private val TAG = "RegisterPlugin"

    /**
     * 注册所有的Plugin到WebView
     * @param webView 需要注册插件的WebView实例
     */
    fun registerAllPlugins(webView: JDWebView?) {
        // 注册StatusBarPlugin
        webView?.let { registerPlugin(it, StatusBarPlugin.NAME, StatusBarPlugin()) }
        // 注册其他插件（如有）可以在此处添加
        // 例如：registerPlugin(webView, OtherPlugin.NAME, OtherPlugin())
    }

    /**
     * 注册单个Plugin
     * @param webView WebView实例
     * @param name 插件名称
     * @param plugin 插件实例
     */
    private fun registerPlugin(webView: JDWebView, name: String, plugin: IBridgePlugin) {
        showLog("register $name")
        // 根据用户提供的示例，使用registerDefaultPlugin方法注册插件
        webView. registerDefaultPlugin(plugin)
    }

    /**
     * 显示日志
     * @param message 日志消息
     */
    private fun showLog(message: String) {
        Log.d(TAG, message)
    }
}