package com.energy.chery_android;

import android.graphics.Color;
import android.os.Bundle;

import com.energy.chery_android.databinding.ActivityMainBinding;
import com.energy.chery_android.QRWebView.BaseWebViewActivity;

public class MainActivity extends BaseWebViewActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 使用ViewBinding初始化布局
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 启用沉浸式状态栏，透明背景，深色文字（黑色）
        setImmersiveStatusBar(true, Color.TRANSPARENT, false);
        
        // 初始化BaseWebView，传入容器ID
        initBaseWebView(R.id.webview_container);
        // 设置是否启用沉浸式状态栏
        setWebViewNavigationBarVisible(true);
        // 加载网页，设置显示导航栏
        loadUrl("http://192.168.5.43:5173/", false);
    }
    
    /**
     * dp转px
     * @param dp dp值
     * @return px值
     */
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
    
    /**
     * 可以重写此方法自定义WebView返回按钮点击行为
     */
    @Override
    protected void onWebViewBackPressed() {
        super.onWebViewBackPressed();
        // 这里可以添加自定义的返回逻辑
    }
    
    /**
     * 可以重写此方法自定义WebView关闭按钮点击行为
     */
    @Override
    protected void onWebViewClosePressed() {
        super.onWebViewClosePressed();
        // 这里可以添加自定义的关闭逻辑
    }
}