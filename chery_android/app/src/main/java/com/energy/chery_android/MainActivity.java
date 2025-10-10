package com.energy.chery_android;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.energy.chery_android.databinding.ActivityMainBinding;
import com.energy.chery_android.webView.BaseWebView;
import com.energy.chery_android.webView.BaseWebViewActivity;

public class MainActivity extends BaseWebViewActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 使用ViewBinding初始化布局
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 初始化BaseWebView，传入容器ID
        initBaseWebView(R.id.webview_container);
        
        // 设置是否启用沉浸式状态栏
        setImmersiveModeEnabled(false);
        
        // 加载网页，设置显示导航栏
        loadUrl("https://www.baidu.com", true);
        
        // 可以自定义导航栏标题
        setWebViewNavigationBarTitle("百度首页");

        
        // 添加测试按钮来切换导航栏显示/隐藏
        Button toggleNavButton = new Button(this);
        toggleNavButton.setText("切换导航栏");
        toggleNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 使用BaseWebViewActivity提供的方法来切换导航栏显示状态
                boolean currentState = isWebViewNavigationBarVisible();
                setWebViewNavigationBarVisible(!currentState);
            }
        });
        
        // 添加按钮到布局（在WebView容器上方）
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.bottomMargin = dpToPx(50);
        ((FrameLayout)findViewById(android.R.id.content)).addView(toggleNavButton, params);
        
        // 也可以直接调用setWebViewNavigationBarVisible方法来控制导航栏显示
        // setWebViewNavigationBarVisible(true); // 显示导航栏
        // setWebViewNavigationBarVisible(false); // 隐藏导航栏
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