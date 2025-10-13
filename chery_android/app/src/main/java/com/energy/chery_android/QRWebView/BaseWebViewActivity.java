package com.energy.chery_android.QRWebView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
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
     * 启用沉浸式状态栏（只让状态栏透明，保持底部导航栏正常）
     */
    private void enableImmersiveStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Android 5.0及以上版本 - 使用标准方法
            Window window = getWindow();
            
            // 清除透明状态栏标志
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 添加绘制系统栏背景标志
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            
            // 设置状态栏为透明
            window.setStatusBarColor(Color.TRANSPARENT);
            
            // 设置系统UI可见性，让内容延伸到状态栏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6.0及以上版本
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | 
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR  // 设置状态栏文字为深色（黑色）
                );
            }
        } else {
            // Android 4.4到Android 4.4版本
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        
        // 确保布局适配沉浸式状态栏
        adjustLayoutForImmersiveMode();
    }

    /**
     * 禁用沉浸式状态栏，恢复默认状态
     */
    private void disableImmersiveStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Android 5.0及以上版本
            Window window = getWindow();
            
            // 恢复系统UI可见性
            window.getDecorView().setSystemUiVisibility(0);
            
            // 恢复状态栏颜色为应用主题颜色
            window.setStatusBarColor(Color.WHITE);
            
            // 不设置底部导航栏颜色，保持系统默认
        } else {
            // Android 4.4到Android 4.4版本
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        
        // 恢复布局适配
        adjustLayoutForNormalMode();
    }
    
    /**
     * 调整布局以适配沉浸式模式
     */
    private void adjustLayoutForImmersiveMode() {
        // 沉浸式模式下，布局应该从状态栏开始，不需要添加状态栏高度的边距
        // 调整导航栏位置 - 从状态栏开始
        if (navigationBar != null) {
            FrameLayout.LayoutParams navParams = (FrameLayout.LayoutParams) navigationBar.getLayoutParams();
            if (navParams != null) {
                navParams.topMargin = 0; // 沉浸式模式下从顶部开始
                navigationBar.requestLayout();
            }
        }
        
        // 调整WebView位置 - 从状态栏开始，延伸到底部
        if (baseWebView != null && baseWebView.getParent() instanceof FrameLayout) {
            FrameLayout container = (FrameLayout) baseWebView.getParent();
            FrameLayout.LayoutParams webViewParams = (FrameLayout.LayoutParams) baseWebView.getLayoutParams();
            if (webViewParams != null) {
                // 沉浸式模式下，WebView从状态栏开始，如果导航栏可见则从导航栏下方开始
                int topMargin = isNavigationBarVisible ? dpToPx(48) : 0;
                webViewParams.topMargin = topMargin;
                
                // 移除底部边距，让WebView延伸到底部
                webViewParams.bottomMargin = 0;
                
                baseWebView.requestLayout();
            }
        }
        
        // 更新UserAgent中的状态栏高度信息
        updateUserAgentWithStatusBarHeight();
    }
    
    /**
     * 更新UserAgent中的状态栏高度信息
     */
    private void updateUserAgentWithStatusBarHeight() {
        if (baseWebView != null && baseWebView.getWebView() != null) {
            // 获取原始UserAgent
            String originalUserAgent = baseWebView.getWebView().getSettings().getUserAgentString();
            // 获取状态栏高度（像素）
            int statusBarHeight = getStatusBarHeightPx();
            // 导航栏高度（像素）
            int navigationBarHeight = 48;
            // 构建包含状态栏和导航栏高度信息的新UserAgent
            String customUserAgent = originalUserAgent + " StatusBarHeight/" + statusBarHeight + " NavigationBarHeight/" + navigationBarHeight + " ImmersiveMode/" + (isImmersiveModeEnabled ? "true" : "false");
            Log.d("BaseWebViewActivity", "Updated UserAgent: " + customUserAgent);
            // 设置自定义UserAgent
            baseWebView.getWebView().getSettings().setUserAgentString(customUserAgent);
        }
    }
    
    /**
     * 调整布局以适配普通模式
     */
    private void adjustLayoutForNormalMode() {
        // 普通模式下，布局从状态栏下方开始
        int statusBarHeight = getStatusBarHeightPx();
        
        // 调整导航栏位置 - 从状态栏下方开始
        if (navigationBar != null) {
            FrameLayout.LayoutParams navParams = (FrameLayout.LayoutParams) navigationBar.getLayoutParams();
            if (navParams != null) {
                navParams.topMargin = statusBarHeight;
                navigationBar.requestLayout();
            }
        }
        
        // 调整WebView位置 - 从状态栏下方开始，延伸到底部
        if (baseWebView != null && baseWebView.getParent() instanceof FrameLayout) {
            FrameLayout container = (FrameLayout) baseWebView.getParent();
            FrameLayout.LayoutParams webViewParams = (FrameLayout.LayoutParams) baseWebView.getLayoutParams();
            if (webViewParams != null) {
                int topMargin = isNavigationBarVisible ? statusBarHeight + dpToPx(48) : statusBarHeight;
                webViewParams.topMargin = topMargin;
                
                // 移除底部边距，让WebView延伸到底部
                webViewParams.bottomMargin = 0;
                
                baseWebView.requestLayout();
            }
        }
        
        // 更新UserAgent中的状态栏高度信息
        updateUserAgentWithStatusBarHeight();
    }
    
    /**
     * 获取状态栏高度（像素）
     * @return 状态栏高度（像素值）
     */
    private int getStatusBarHeightPx() {
        int statusBarHeightPx = 0;
        @SuppressLint("InternalInsetResource") int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeightPx = getResources().getDimensionPixelSize(resourceId);
        }
        // 若获取失败，使用默认值 24dp 转 px
        if (statusBarHeightPx == 0) {
            statusBarHeightPx = dpToPx(24);
        }
        return statusBarHeightPx;
    }
    
    /**
     * 获取导航栏高度（像素）
     * @return 导航栏高度（像素值）
     */
    private int getNavigationBarHeight() {
        int navigationBarHeightPx = 0;
        @SuppressLint("InternalInsetResource") int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeightPx = getResources().getDimensionPixelSize(resourceId);
        }
        // 若获取失败，使用默认值 48dp 转 px
        if (navigationBarHeightPx == 0) {
            navigationBarHeightPx = dpToPx(48);
        }
        return navigationBarHeightPx;
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
    
    /**
     * 设置状态栏颜色
     * @param color 状态栏颜色
     */
    protected void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(color);
        }
    }
    
    /**
     * 设置状态栏文字颜色（浅色或深色）
     * @param isLight 是否为浅色文字（true为浅色，false为深色）
     */
    protected void setStatusBarTextColor(boolean isLight) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上版本
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                if (isLight) {
                    // 浅色文字（白色）- 用于深色背景
                    insetsController.setSystemBarsAppearance(
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    );
                } else {
                    // 深色文字（黑色）- 用于浅色背景
                    insetsController.setSystemBarsAppearance(
                            0,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    );
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0到Android 10版本
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            if (isLight) {
                // 浅色文字（白色）- 用于深色背景
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                // 深色文字（黑色）- 用于浅色背景
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }
    
    /**
     * 设置沉浸式状态栏并指定颜色和文字颜色
     * @param enabled 是否启用沉浸式模式
     * @param statusBarColor 状态栏颜色
     * @param isLightText 状态栏文字是否为浅色
     */
    protected void setImmersiveStatusBar(boolean enabled, int statusBarColor, boolean isLightText) {
        isImmersiveModeEnabled = enabled;
        if (enabled) {
            // 先设置状态栏颜色和文字颜色
            setStatusBarColor(statusBarColor);
            setStatusBarTextColor(isLightText);
            // 然后启用沉浸式状态栏
            enableImmersiveStatusBar();
        } else {
            disableImmersiveStatusBar();
        }
    }
    
    /**
     * 获取当前沉浸式模式状态
     * @return true表示沉浸式模式已启用
     */
    protected boolean isImmersiveModeEnabled() {
        return isImmersiveModeEnabled;
    }
    
    /**
     * 带动画效果的沉浸式状态栏切换
     * @param enabled 是否启用沉浸式模式
     * @param statusBarColor 状态栏颜色
     * @param isLightText 状态栏文字是否为浅色
     * @param duration 动画持续时间（毫秒）
     */
    protected void setImmersiveStatusBarWithAnimation(boolean enabled, int statusBarColor, boolean isLightText, long duration) {
        if (enabled == isImmersiveModeEnabled) {
            return; // 状态相同，无需切换
        }
        
        // 获取当前状态栏高度
        int currentStatusBarHeight = isImmersiveModeEnabled ? getStatusBarHeightPx() : 0;
        int targetStatusBarHeight = enabled ? getStatusBarHeightPx() : 0;
        
        // 创建动画
        ValueAnimator animator = ValueAnimator.ofInt(currentStatusBarHeight, targetStatusBarHeight);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                updateLayoutMargins(animatedValue);
            }
        });
        
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                // 动画开始时设置状态栏
                if (enabled) {
                    enableImmersiveStatusBar();
                    setStatusBarColor(statusBarColor);
                    setStatusBarTextColor(isLightText);
                } else {
                    disableImmersiveStatusBar();
                }
            }
            
            @Override
            public void onAnimationEnd(Animator animation) {
                // 动画结束时更新状态
                isImmersiveModeEnabled = enabled;
                updateUserAgentWithStatusBarHeight();
            }
        });
        
        animator.start();
    }
    
    /**
     * 更新布局边距
     * @param statusBarHeight 状态栏高度
     */
    private void updateLayoutMargins(int statusBarHeight) {
        // 调整导航栏位置
        if (navigationBar != null) {
            FrameLayout.LayoutParams navParams = (FrameLayout.LayoutParams) navigationBar.getLayoutParams();
            if (navParams != null) {
                // 如果状态栏高度为0，说明是沉浸式模式，从顶部开始
                // 如果状态栏高度大于0，说明是普通模式，从状态栏下方开始
                navParams.topMargin = statusBarHeight;
                navigationBar.requestLayout();
            }
        }
        
        // 调整WebView位置
        if (baseWebView != null && baseWebView.getParent() instanceof FrameLayout) {
            FrameLayout.LayoutParams webViewParams = (FrameLayout.LayoutParams) baseWebView.getLayoutParams();
            if (webViewParams != null) {
                int topMargin = isNavigationBarVisible ? statusBarHeight + dpToPx(48) : statusBarHeight;
                webViewParams.topMargin = topMargin;
                // 确保WebView延伸到底部
                webViewParams.bottomMargin = 0;
                baseWebView.requestLayout();
            }
        }
    }
    
    /**
     * 带动画效果的沉浸式状态栏切换（使用默认动画时间）
     * @param enabled 是否启用沉浸式模式
     * @param statusBarColor 状态栏颜色
     * @param isLightText 状态栏文字是否为浅色
     */
    protected void setImmersiveStatusBarWithAnimation(boolean enabled, int statusBarColor, boolean isLightText) {
        setImmersiveStatusBarWithAnimation(enabled, statusBarColor, isLightText, 300); // 默认300ms动画
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