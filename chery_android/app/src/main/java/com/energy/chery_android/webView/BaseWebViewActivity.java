package com.energy.chery_android.webView;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.energy.chery_android.R;

/**
 * 基于BaseWebView的基础Activity类
 * 提供沉浸式状态栏和导航栏控制功能
 */
public class BaseWebViewActivity extends AppCompatActivity {
    protected BaseWebView baseWebView;
    private RelativeLayout navigationBar; // 导航栏
    private TextView titleView; // 标题视图
    private ImageView backButton; // 返回按钮
    private ImageView closeButton; // 关闭按钮
    private boolean isNavigationBarVisible = false; // 导航栏可见状态
    private boolean isImmersiveModeEnabled = false; // 沉浸式模式状态

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 在Activity中初始化BaseWebView
     * @param containerId WebView容器的布局ID
     */
    protected void initBaseWebView(int containerId) {
        // 创建BaseWebView实例
        baseWebView = new BaseWebView(this);
        
        // 设置返回按钮点击监听器
        baseWebView.setOnBackClickListener(new BaseWebView.OnBackClickListener() {
            @Override
            public void onBackClick() {
                onWebViewBackPressed();
            }
        });
        
        // 设置关闭按钮点击监听器
        baseWebView.setOnCloseClickListener(new BaseWebView.OnCloseClickListener() {
            @Override
            public void onCloseClick() {
                onWebViewClosePressed();
            }
        });
        
        // 设置自定义UserAgent，添加状态栏和导航栏高度信息
        setupCustomUserAgent();

        // 将BaseWebView添加到布局中
        FrameLayout container = findViewById(containerId);
        if (container != null) {
            // 创建导航栏
            initNavigationBar(container);
            
            // 添加WebView到容器
            FrameLayout.LayoutParams webViewParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            // 如果导航栏可见，调整WebView的位置
            if (isNavigationBarVisible && navigationBar != null) {
                webViewParams.topMargin = dpToPx(48);
            }
            container.addView(baseWebView, webViewParams);
        }
    }
    
    /**
     * 初始化导航栏
     * @param container 容器布局
     */
    private void initNavigationBar(FrameLayout container) {
        // 创建导航栏
        navigationBar = new RelativeLayout(this);
        navigationBar.setVisibility(isNavigationBarVisible ? View.VISIBLE : View.GONE);
        
        // 设置导航栏背景色，确保可见
        navigationBar.setBackgroundColor(Color.WHITE);
        // 添加导航栏底部边框
        navigationBar.setPadding(0, 0, 0, 1);
        navigationBar.setBackgroundResource(android.R.drawable.divider_horizontal_bright);

        // 创建标题视图
        titleView = new TextView(this);
        titleView.setTextSize(16);
        titleView.setTextColor(Color.BLACK);
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        navigationBar.addView(titleView, titleParams);

        // 创建返回按钮
        backButton = new ImageView(this);
        // 设置默认返回图标
        backButton.setImageResource(R.drawable.ic_arrow_back);
        // 设置图标颜色为黑色
        backButton.setColorFilter(Color.BLACK, android.graphics.PorterDuff.Mode.SRC_IN);
        backButton.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        RelativeLayout.LayoutParams backParams = new RelativeLayout.LayoutParams(
                dpToPx(48),
                dpToPx(48)
        );
        backParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        backParams.addRule(RelativeLayout.CENTER_VERTICAL);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onWebViewBackPressed();
            }
        });
        navigationBar.addView(backButton, backParams);

        // 创建关闭按钮
        closeButton = new ImageView(this);
        // 设置默认关闭图标
        closeButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        // 设置图标颜色为黑色
        closeButton.setColorFilter(Color.BLACK, android.graphics.PorterDuff.Mode.SRC_IN);
        // 设置与返回按钮相同的padding，保持视觉一致性
        closeButton.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        RelativeLayout.LayoutParams closeParams = new RelativeLayout.LayoutParams(
                dpToPx(48),
                dpToPx(48)
        );
        closeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        closeParams.addRule(RelativeLayout.CENTER_VERTICAL);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onWebViewClosePressed();
            }
        });

        navigationBar.addView(closeButton, closeParams);

        // 设置导航栏布局参数
        FrameLayout.LayoutParams navParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dpToPx(48)
        );
        container.addView(navigationBar, navParams);

        navigationBar.setBackgroundColor(Color.TRANSPARENT);
        titleView.setTextColor(Color.BLACK);
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
     * 获取状态栏高度（物理像素）
     * @return 状态栏高度（像素值）
     */
    private float getStatusBarHeight() {
        float statusBarHeightPx = 0;
        @SuppressLint("InternalInsetResource") int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) statusBarHeightPx = getResources().getDimensionPixelSize(resourceId);
        // 若获取失败，使用默认值 24dp 转 px
        if (statusBarHeightPx == 0) statusBarHeightPx = 24 * getResources().getDisplayMetrics().density + 0.5f;
        // 将 px 转换为 dp
        float density = getResources().getDisplayMetrics().density;
        return statusBarHeightPx / density;
    }
    /**
     * 设置WebView的UserAgent，添加状态栏和导航栏高度信息
     */
    private void setupCustomUserAgent() {
        if (baseWebView != null && baseWebView.getWebView() != null) {
            // 获取原始UserAgent
            String originalUserAgent = baseWebView.getWebView().getSettings().getUserAgentString();
            // 获取状态栏高度（像素）
            int statusBarHeight = (int) getStatusBarHeight();
            // 导航栏高度（像素）
            int navigationBarHeight = 48;
            // 构建包含状态栏和导航栏高度信息的新UserAgent
            String customUserAgent = originalUserAgent + " StatusBarHeight/" + statusBarHeight + " NavigationBarHeight/" + navigationBarHeight;
            Log.d( "setupCustomUserAgent: ", customUserAgent);
            // 设置自定义UserAgent
            baseWebView.getWebView().getSettings().setUserAgentString(customUserAgent);
        }
    }

    /**
     * 加载URL
     * @param url 要加载的URL
     */
    protected void loadUrl(String url) {
        if (baseWebView != null) {
            baseWebView.loadUrl(url);
        }
    }
    
    /**
     * 加载URL并控制导航栏显示
     * @param url 要加载的URL
     * @param showNavigationBar 是否显示导航栏
     */
    protected void loadUrl(String url, boolean showNavigationBar) {
        if (baseWebView != null) {
            baseWebView.loadUrl(url);
            setWebViewNavigationBarVisible(showNavigationBar);
        }
    }

    /**
     * 设置是否启用沉浸式状态栏
     * @param enabled 是否启用
     */
    protected void setImmersiveModeEnabled(boolean enabled) {
        isImmersiveModeEnabled = enabled;
        updateImmersiveMode();
    }

    /**
     * 更新沉浸式模式状态
     */
    private void updateImmersiveMode() {
        if (isImmersiveModeEnabled) {
            enableImmersiveStatusBar();
        } else {
            disableImmersiveStatusBar();
        }
    }

    /**
     * 启用沉浸式状态栏（显示状态栏时间和内容，同时保持透明效果）
     */
    private void enableImmersiveStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上版本
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                // 显示状态栏但设置为透明
                insetsController.show(WindowInsets.Type.statusBars());
                insetsController.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, // 浅色状态栏文字
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
            // 设置状态栏颜色为透明
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0到Android 10版本
            // 设置状态栏文字为浅色以确保可见性
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // 浅色状态栏文字
            );
            // 设置状态栏颜色为透明
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            // Android 4.4到Android 5.1版本
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    /**
     * 禁用沉浸式状态栏，恢复默认状态
     */
    private void disableImmersiveStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上版本
            getWindow().setDecorFitsSystemWindows(true);
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
            }
            // 恢复状态栏颜色为应用主题颜色
            getWindow().setStatusBarColor(Color.WHITE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0到Android 10版本
            getWindow().getDecorView().setSystemUiVisibility(0);
            // 恢复状态栏颜色为应用主题颜色
            getWindow().setStatusBarColor(Color.WHITE);
        } else {
            // Android 4.4到Android 5.1版本
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            // 恢复状态栏颜色为应用主题颜色
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.WHITE);
            }
        }
    }

    /**
     * WebView返回按钮点击处理
     */
    protected void onWebViewBackPressed() {
        if (baseWebView != null && baseWebView.canGoBack()) {
            baseWebView.goBack();
        } else {
            onBackPressed();
        }
    }

    /**
     * WebView关闭按钮点击处理
     */
    protected void onWebViewClosePressed() {
        finish();
    }

    /**
     * 设置WebView导航栏可见性
     * @param visible 是否可见
     */
    protected void setWebViewNavigationBarVisible(boolean visible) {
        isNavigationBarVisible = visible;
        if (navigationBar != null) {
            navigationBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        
        // 调整WebView的位置
        FrameLayout container = null;
        if (baseWebView != null && baseWebView.getParent() instanceof FrameLayout) {
            container = (FrameLayout) baseWebView.getParent();
        }
        
        if (container != null) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) baseWebView.getLayoutParams();
            if (params != null) {
                params.topMargin = visible ? dpToPx(48) : 0;
                baseWebView.requestLayout();
            }
        }
    }
    
    /**
     * 获取导航栏当前可见状态
     * @return true表示导航栏可见，false表示隐藏
     */
    protected boolean isWebViewNavigationBarVisible() {
        return isNavigationBarVisible;
    }

    /**
     * 设置WebView导航栏标题
     * @param title 标题文本
     */
    protected void setWebViewNavigationBarTitle(String title) {
        if (titleView != null) {
            titleView.setText(title);
        }
    }
    
    /**
     * 设置WebView导航栏背景颜色
     * @param color 颜色值，如Color.BLUE或0xFF0000FF
     */
    protected void setWebViewNavigationBarBackgroundColor(int color) {
        if (navigationBar != null) {
            navigationBar.setBackgroundColor(color);
        }
    }
    
    /**
     * 设置WebView导航栏标题文字颜色
     * @param color 颜色值，如Color.WHITE或0xFFFFFFFF
     */
    protected void setWebViewNavigationBarTitleColor(int color) {
        if (titleView != null) {
            titleView.setTextColor(color);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (baseWebView != null) {
            baseWebView.onResume();
        }
        // 确保沉浸式模式在resume后正确应用
        updateImmersiveMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (baseWebView != null) {
            baseWebView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        if (baseWebView != null) {
            baseWebView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (baseWebView != null && baseWebView.canGoBack()) {
            baseWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}