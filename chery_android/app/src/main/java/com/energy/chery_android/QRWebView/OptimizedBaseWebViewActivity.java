package com.energy.chery_android.QRWebView;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.energy.chery_android.utils.AppConfigManager;
import com.energy.chery_android.utils.LayoutManager;
import com.energy.chery_android.utils.LogManager;
import com.energy.chery_android.utils.StatusBarManager;
import com.energy.chery_android.utils.ThemeManager;
import com.energy.chery_android.utils.WebViewManager;

/**
 * 优化后的BaseWebViewActivity
 * 使用管理器模式，减少代码冗余，提高可维护性
 */
public class OptimizedBaseWebViewActivity extends AppCompatActivity {
    private static final String TAG = "OptimizedBaseWebViewActivity";
    
    // 管理器实例
    private StatusBarManager statusBarManager;
    private WebViewManager webViewManager;
    private LayoutManager layoutManager;
    private AppConfigManager configManager;
    private LogManager logManager;
    private ThemeManager themeManager;
    
    // 配置实例
    private StatusBarManager.StatusBarConfig statusBarConfig;
    private WebViewManager.WebViewConfig webViewConfig;
    private WebViewManager.NavigationBarConfig navigationBarConfig;
    private LayoutManager.LayoutConfig layoutConfig;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化管理器
        initManagers();
        
        // 加载配置
        loadConfigurations();
        
        // 设置状态栏
        setupStatusBar();
        
        // 记录应用启动
        logManager.logAppStart();
    }
    
    /**
     * 初始化所有管理器
     */
    private void initManagers() {
        // 初始化日志管理器
        logManager = LogManager.getInstance(this);
        
        // 初始化配置管理器
        configManager = AppConfigManager.getInstance(this);
        
        // 初始化状态栏管理器
        statusBarManager = new StatusBarManager(this);
        setupStatusBarListeners();
        
        // 初始化WebView管理器
        webViewManager = new WebViewManager(this);
        setupWebViewListeners();
        
        // 初始化布局管理器
        layoutManager = new LayoutManager(this);
        layoutManager.setWebViewManager(webViewManager);
        setupLayoutListeners();
        
        // 初始化主题管理器
        themeManager = ThemeManager.getInstance(this);
        
        logManager.d(TAG, "All managers initialized successfully");
    }
    
    /**
     * 加载配置
     */
    private void loadConfigurations() {
        // 加载状态栏配置
        statusBarConfig = configManager.getStatusBarConfig();
        
        // 加载WebView配置
        webViewConfig = configManager.getWebViewConfig();
        
        // 加载导航栏配置
        navigationBarConfig = configManager.getNavigationBarConfig();
        
        // 加载布局配置
        layoutConfig = configManager.getLayoutConfig();
        
        logManager.d(TAG, "Configurations loaded successfully");
    }
    
    /**
     * 设置状态栏
     */
    private void setupStatusBar() {
        if (statusBarConfig == null) {
            statusBarConfig = StatusBarManager.getDefaultStatusBarConfig();
        }
        
        // 根据主题设置状态栏文字颜色
        boolean isDarkMode = themeManager.isDarkTheme();
        statusBarConfig.isLightText = !isDarkMode;
        
        if (configManager.isImmersiveModeEnabled()) {
            statusBarManager.enableImmersiveStatusBar(statusBarConfig);
        } else {
            statusBarManager.disableImmersiveStatusBar(statusBarConfig);
        }
        
        logManager.logStatusBarChange(
            statusBarManager.isImmersiveModeEnabled(),
            statusBarConfig.statusBarColor,
            statusBarConfig.isLightText
        );
    }
    
    /**
     * 设置状态栏监听器
     */
    private void setupStatusBarListeners() {
        statusBarManager.setOnStatusBarChangeListener(new StatusBarManager.OnStatusBarChangeListener() {
            @Override
            public void onStatusBarChanged(boolean isImmersive, StatusBarManager.StatusBarConfig config) {
                logManager.logStatusBarChange(isImmersive, config.statusBarColor, config.isLightText);
                
                // 调整布局
                layoutManager.adjustLayout(isImmersive, layoutConfig);
            }
            
            @Override
            public void onAnimationStart() {
                logManager.d(TAG, "Status bar animation started");
            }
            
            @Override
            public void onAnimationEnd() {
                logManager.d(TAG, "Status bar animation ended");
            }
        });
    }
    
    /**
     * 设置WebView监听器
     */
    private void setupWebViewListeners() {
        webViewManager.setOnWebViewEventListener(new WebViewManager.OnWebViewEventListener() {
            @Override
            public void onBackClick() {
                handleWebViewBackClick();
            }
            
            @Override
            public void onCloseClick() {
                handleWebViewCloseClick();
            }
            
            @Override
            public void onPageStarted(String url) {
                logManager.logWebViewLoad(url);
            }
            
            @Override
            public void onPageFinished(String url) {
                logManager.d(TAG, "WebView page finished: " + url);
            }
            
            @Override
            public void onReceivedError(String error) {
                logManager.e(TAG, "WebView error: " + error);
            }
        });
    }
    
    /**
     * 设置布局监听器
     */
    private void setupLayoutListeners() {
        layoutManager.setOnLayoutChangeListener(new LayoutManager.OnLayoutChangeListener() {
            @Override
            public void onLayoutChanged(boolean isImmersive, LayoutManager.LayoutConfig config) {
                logManager.d(TAG, "Layout changed: immersive=" + isImmersive);
            }
            
            @Override
            public void onStatusBarHeightChanged(int height) {
                logManager.d(TAG, "Status bar height changed: " + height);
            }
            
            @Override
            public void onNavigationBarHeightChanged(int height) {
                logManager.d(TAG, "Navigation bar height changed: " + height);
            }
        });
    }
    
    /**
     * 初始化WebView
     */
    protected void initBaseWebView(int containerId) {
        initBaseWebView(containerId, webViewConfig, navigationBarConfig);
    }
    
    /**
     * 初始化WebView（带配置）
     */
    protected void initBaseWebView(int containerId, WebViewManager.WebViewConfig webViewConfig, 
                                   WebViewManager.NavigationBarConfig navigationBarConfig) {
        if (webViewConfig == null) {
            webViewConfig = AppConfigManager.getDefaultWebViewConfig();
        }
        if (navigationBarConfig == null) {
            navigationBarConfig = AppConfigManager.getDefaultNavigationBarConfig();
        }
        
        // 初始化WebView
        webViewManager.initWebView(containerId, webViewConfig);
        
        // 设置导航栏配置
        if (navigationBarConfig.visible) {
            webViewManager.setNavigationBarVisible(true);
            webViewManager.setNavigationBarTitle(navigationBarConfig.title);
            webViewManager.setNavigationBarBackgroundColor(navigationBarConfig.backgroundColor);
            webViewManager.setNavigationBarTitleColor(navigationBarConfig.titleColor);
        }
        
        logManager.d(TAG, "WebView initialized with container ID: " + containerId);
    }
    
    /**
     * 加载URL
     */
    protected void loadUrl(String url) {
        webViewManager.loadUrl(url);
        logManager.logWebViewLoad(url);
    }
    
    /**
     * 加载URL并控制导航栏显示
     */
    protected void loadUrl(String url, boolean showNavigationBar) {
        webViewManager.loadUrl(url);
        webViewManager.setNavigationBarVisible(showNavigationBar);
        logManager.logWebViewLoad(url);
    }
    
    /**
     * 设置沉浸式状态栏
     */
    protected void setImmersiveStatusBar(boolean enabled, int statusBarColor, boolean isLightText) {
        StatusBarManager.StatusBarConfig config = new StatusBarManager.StatusBarConfig(statusBarColor, isLightText);
        config.enableAnimation = configManager.isAnimationEnabled();
        config.animationDuration = configManager.getAnimationDuration();
        
        if (enabled) {
            statusBarManager.enableImmersiveStatusBar(config);
        } else {
            statusBarManager.disableImmersiveStatusBar(config);
        }
        
        // 保存配置
        configManager.setImmersiveModeEnabled(enabled);
        configManager.saveStatusBarConfig(config);
    }
    
    /**
     * 设置WebView导航栏可见性
     */
    protected void setWebViewNavigationBarVisible(boolean visible) {
        webViewManager.setNavigationBarVisible(visible);
        logManager.d(TAG, "Navigation bar visibility set to: " + visible);
    }
    
    /**
     * 设置WebView导航栏标题
     */
    protected void setWebViewNavigationBarTitle(String title) {
        webViewManager.setNavigationBarTitle(title);
    }
    
    /**
     * 设置WebView导航栏背景颜色
     */
    protected void setWebViewNavigationBarBackgroundColor(int color) {
        webViewManager.setNavigationBarBackgroundColor(color);
    }
    
    /**
     * 设置WebView导航栏标题颜色
     */
    protected void setWebViewNavigationBarTitleColor(int color) {
        webViewManager.setNavigationBarTitleColor(color);
    }
    
    /**
     * 处理WebView返回按钮点击
     */
    protected void handleWebViewBackClick() {
        if (webViewManager.canGoBack()) {
            webViewManager.goBack();
        } else {
            onBackPressed();
        }
    }
    
    /**
     * 处理WebView关闭按钮点击
     */
    protected void handleWebViewCloseClick() {
        finish();
    }
    
    /**
     * 检查导航栏是否可见
     */
    protected boolean isWebViewNavigationBarVisible() {
        return webViewManager.isNavigationBarVisible();
    }
    
    /**
     * 获取状态栏管理器
     */
    protected StatusBarManager getStatusBarManager() {
        return statusBarManager;
    }
    
    /**
     * 获取WebView管理器
     */
    protected WebViewManager getWebViewManager() {
        return webViewManager;
    }
    
    /**
     * 获取布局管理器
     */
    protected LayoutManager getLayoutManager() {
        return layoutManager;
    }
    
    /**
     * 获取配置管理器
     */
    protected AppConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * 获取日志管理器
     */
    protected LogManager getLogManager() {
        return logManager;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        webViewManager.onResume();
        logManager.d(TAG, "Activity resumed");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        webViewManager.onPause();
        logManager.d(TAG, "Activity paused");
    }
    
    @Override
    protected void onDestroy() {
        webViewManager.destroy();
        logManager.logAppStop();
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {
        if (webViewManager.canGoBack()) {
            webViewManager.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    /**
     * 获取当前沉浸式模式状态
     */
    protected boolean isImmersiveModeEnabled() {
        return statusBarManager.isImmersiveModeEnabled();
    }
    
    /**
     * 切换沉浸式模式
     */
    protected void toggleImmersiveMode() {
        boolean currentMode = statusBarManager.isImmersiveModeEnabled();
        StatusBarManager.StatusBarConfig config = currentMode ? 
            new StatusBarManager.StatusBarConfig(Color.WHITE, true) :
            new StatusBarManager.StatusBarConfig(Color.TRANSPARENT, false);
        
        statusBarManager.toggleImmersiveStatusBar(config, config);
        configManager.setImmersiveModeEnabled(!currentMode);
        
        logManager.d(TAG, "Immersive mode toggled to: " + !currentMode);
    }
    
    /**
     * 重新加载WebView
     */
    protected void reload() {
        if (webViewManager != null) {
            webViewManager.reload();
            logManager.d(TAG, "WebView reloaded");
        } else {
            logManager.e(TAG, "WebViewManager is null, cannot reload");
        }
    }
    
    /**
     * 获取WebView实例
     */
    protected com.energy.chery_android.QRWebView.BaseWebView getBaseWebView() {
        if (webViewManager != null) {
            return webViewManager.getBaseWebView();
        }
        return null;
    }
    
    /**
     * 检查WebView是否可用
     */
    protected boolean isWebViewAvailable() {
        return webViewManager != null && webViewManager.getBaseWebView() != null;
    }
}
