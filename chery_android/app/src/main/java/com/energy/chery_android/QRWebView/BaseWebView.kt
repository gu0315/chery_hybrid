package com.energy.chery_android.QRWebView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import com.energy.chery_android.Plugins.RegisterPlugin
import com.jd.hybrid.JDWebView

/**
 * 基于JDWebView封装的基础WebView组件 - Kotlin版本
 * 专注于WebView核心功能实现
 */
class BaseWebView : RelativeLayout {
    private var webView: JDWebView? = null
    private var onBackClickListener: OnBackClickListener? = null
    private var onCloseClickListener: OnCloseClickListener? = null

    val TAG: String = "BaseWebView"

    /**
     * 构造函数
     * @param context 上下文
     */
    constructor(context: Context) : super(context) {
        init(context)
    }

    /**
     * 构造函数
     * @param context 上下文
     * @param attrs 属性集
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    /**
     * 构造函数
     * @param context 上下文
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    /**
     * 初始化BaseWebView
     * @param context 上下文
     */
    private fun init(context: Context) {
        // 创建WebView
        webView = JDWebView(context)
        initWebViewSettings()

        val registerPlugin = RegisterPlugin()
        registerPlugin.registerAllPlugins(webView)
        // 设置WebView布局参数
        val webViewParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        addView(webView, webViewParams)
    }

    /**
     * 初始化WebView设置
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebViewSettings() {
        webView?.apply {
            // 基础设置
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true

            // 设置WebViewClient
            webViewClient = object : WebViewClient() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    // 在WebView中加载链接，而不是使用外部浏览器
                    view.loadUrl(request.url.toString())
                    return true
                }

                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    // 可以在这里添加加载开始时的逻辑
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    // 可以在这里添加加载完成时的逻辑
                    Log.d(TAG, url)
                }
            }

            // 设置WebChromeClient
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    // 可以在这里添加进度变化时的逻辑
                }
            }
        }
    }

    /**
     * 加载URL
     * @param url 要加载的URL
     */
    fun loadUrl(url: String) {
        webView?.loadUrl(url)
    }

    /**
     * 设置返回按钮点击监听器
     * @param listener 监听器
     */
    fun setOnBackClickListener(listener: OnBackClickListener?) {
        this.onBackClickListener = listener
    }

    /**
     * 设置关闭按钮点击监听器
     * @param listener 监听器
     */
    fun setOnCloseClickListener(listener: OnCloseClickListener?) {
        this.onCloseClickListener = listener
    }

    /**
     * 获取内部的JDWebView实例
     * @return JDWebView实例
     */
    fun getWebView(): JDWebView? {
        return webView
    }

    /**
     * 检查WebView是否可以返回上一页
     * @return 是否可以返回
     */
    fun canGoBack(): Boolean {
        return webView?.canGoBack() ?: false
    }

    /**
     * 返回上一页
     */
    fun goBack() {
        if (canGoBack()) {
            webView?.goBack()
        }
    }

    /**
     * 停止加载
     */
    fun stopLoading() {
        webView?.stopLoading()
    }

    /**
     * 销毁WebView
     */
    fun destroy() {
        webView?.apply {
            stopLoading()
            destroy()
        }
        webView = null
    }

    /**
     * 处理WebView生命周期 - onResume
     */
    fun onResume() {
        webView?.onResume()
    }

    /**
     * 处理WebView生命周期 - onPause
     */
    fun onPause() {
        webView?.onPause()
    }

    /**
     * 返回按钮点击监听器接口
     */
    interface OnBackClickListener {
        fun onBackClick()
    }

    /**
     * 关闭按钮点击监听器接口
     */
    interface OnCloseClickListener {
        fun onCloseClick()
    }
}
