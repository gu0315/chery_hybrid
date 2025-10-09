package com.example.chery

import android.annotation.SuppressLint
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * 应用的主Activity，作为应用的入口界面
 * Activity是Android应用中最基本的组件，代表一个具有用户界面的单一屏幕
 * 这个类继承自AppCompatActivity，这是AndroidX库提供的兼容性Activity基类
 */
class MainActivity : AppCompatActivity() {
    /**
     * Activity创建时调用的第一个方法
     * savedInstanceState: 用于保存和恢复Activity状态的数据
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // 切换到正常应用主题
        // R.style.Theme_App 是在res/values/themes.xml中定义的主题样式
        setTheme(R.style.Theme_App)
        
        // 调用父类的onCreate方法，这是必须的步骤
        super.onCreate(savedInstanceState)
        
        // 设置Activity的布局文件
        // R.layout.activity_main 指向res/layout/activity_main.xml文件
        setContentView(R.layout.activity_main)
        
        // 初始化和配置WebView
        initWebView()
        
        // 调用自定义方法隐藏系统导航栏
        hideNavigationBar()
    }
    
    /**
     * 初始化和配置WebView组件
     * 这个方法负责设置WebView的各种属性并加载默认网页
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {

        // 从布局文件中获取WebView实例
        val webView = findViewById<WebView>(R.id.webview)
        
        // 获取WebView的设置对象
        val webSettings: WebSettings = webView.settings
        
        // 启用JavaScript支持
        webSettings.javaScriptEnabled = true
        
        // 启用DOM存储API
        webSettings.domStorageEnabled = true
        
        // 设置WebViewClient，使网页在WebView内部打开而不是跳转到浏览器
        webView.webViewClient = object : WebViewClient() {
            // 当网页开始加载时调用
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // 可以在这里添加加载指示器
            }
            
            // 当网页加载完成时调用
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 可以在这里隐藏加载指示器
            }
            
            // 处理URL加载，特别是非HTTP/HTTPS协议
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return super.shouldOverrideUrlLoading(view, url)
            }
        }
        webView.loadUrl("https://test.platform.apecar.cn/mobile/businessMgmt")
    }
    
    /**
     * 设置状态栏透明并显示时间信息，同时隐藏导航栏
     * 这个方法实现了状态栏透明但内容从状态栏下方开始布局，同时隐藏导航栏
     */
    private fun hideNavigationBar() {
        // 设置状态栏背景为透明色
        // Color.TRANSPARENT 是Android系统定义的透明颜色常量
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        
        // 获取当前窗口的装饰视图
        // decorView是整个窗口的根视图，包含状态栏、导航栏和应用内容
        val decorView = window.decorView
        
        // 清除全屏标志，确保状态栏显示时间信息
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
        
        // 清除布局无限制标志，确保内容从状态栏下方开始布局
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        
        // 设置系统UI可见性标志
        // 使用or运算符组合多个标志
        decorView.systemUiVisibility = (
            // 基础布局标志，保持布局稳定
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            // 布局时考虑导航栏的隐藏
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            // 布局时考虑状态栏的隐藏
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            // 实际隐藏导航栏
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            // 沉浸式粘性模式：
            // 1. 用户从边缘滑动时会临时显示UI元素
            // 2. 一段时间后UI元素会自动隐藏
            // 3. 不会触发onSystemUiVisibilityChange回调
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            // 让状态栏内容（如时间、电池）显示为深色
            // 这在浅色背景上能提高可读性，确保时间显示为黑色
            or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        )
    }
    
    /**
     * 当窗口焦点改变时调用
     * hasFocus: 当前窗口是否获得焦点
     * 重写此方法是为了在用户与应用交互（如点击屏幕）后，重新隐藏导航栏
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        // 调用父类方法
        super.onWindowFocusChanged(hasFocus)
        
        // 当窗口获得焦点时，重新应用沉浸式模式
        if (hasFocus) {
            hideNavigationBar()
        }
    }
}