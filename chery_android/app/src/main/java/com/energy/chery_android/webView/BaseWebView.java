package com.energy.chery_android.webView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.jd.hybrid.JDWebView;

/**
 * 基于JDWebView封装的基础WebView组件
 * 专注于WebView核心功能实现
 */
public class BaseWebView extends RelativeLayout {
    private JDWebView webView;
    private OnBackClickListener onBackClickListener;
    private OnCloseClickListener onCloseClickListener;

    /**
     * 构造函数
     * @param context 上下文
     */
    public BaseWebView(Context context) {
        super(context);
        init(context);
    }

    /**
     * 构造函数
     * @param context 上下文
     * @param attrs 属性集
     */
    public BaseWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * 构造函数
     * @param context 上下文
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public BaseWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化BaseWebView
     * @param context 上下文
     */
    private void init(Context context) {
        // 创建WebView
        webView = new JDWebView(context);
        initWebViewSettings();

        // 设置WebView布局参数
        RelativeLayout.LayoutParams webViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        addView(webView, webViewParams);
    }



    /**
     * 初始化WebView设置
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebViewSettings() {
        // 基础设置
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);

        // 设置WebViewClient
        webView.setWebViewClient(new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // 在WebView中加载链接，而不是使用外部浏览器
                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // 可以在这里添加加载开始时的逻辑
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 可以在这里添加加载完成时的逻辑
            }
        });


        // 设置WebChromeClient
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                // 可以在这里添加进度变化时的逻辑
            }
        });
    }

    /**
     * 加载URL
     * @param url 要加载的URL
     */
    public void loadUrl(String url) {
        if (webView != null) {
            webView.loadUrl(url);
        }
    }

    /**
     * 设置返回按钮点击监听器
     * @param listener 监听器
     */
    public void setOnBackClickListener(OnBackClickListener listener) {
        this.onBackClickListener = listener;
    }

    /**
     * 设置关闭按钮点击监听器
     * @param listener 监听器
     */
    public void setOnCloseClickListener(OnCloseClickListener listener) {
        this.onCloseClickListener = listener;
    }

    /**
     * 获取内部的JDWebView实例
     * @return JDWebView实例
     */
    public JDWebView getWebView() {
        return webView;
    }

    /**
     * 检查WebView是否可以返回上一页
     * @return 是否可以返回
     */
    public boolean canGoBack() {
        return webView != null && webView.canGoBack();
    }

    /**
     * 返回上一页
     */
    public void goBack() {
        if (canGoBack()) {
            webView.goBack();
        }
    }

    /**
     * 停止加载
     */
    public void stopLoading() {
        if (webView != null) {
            webView.stopLoading();
        }
    }

    /**
     * 销毁WebView
     */
    public void destroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
    }

    /**
     * 处理WebView生命周期 - onResume
     */
    public void onResume() {
        if (webView != null) {
            webView.onResume();
        }
    }

    /**
     * 处理WebView生命周期 - onPause
     */
    public void onPause() {
        if (webView != null) {
            webView.onPause();
        }
    }



    /**
     * 返回按钮点击监听器接口
     */
    public interface OnBackClickListener {
        void onBackClick();
    }

    /**
     * 关闭按钮点击监听器接口
     */
    public interface OnCloseClickListener {
        void onCloseClick();
    }
}