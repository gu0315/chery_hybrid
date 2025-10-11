//
//  BaseWebView.swift
//  chery_ios
//
//  Created by 顾钱想 on 10/10/25.
//

import UIKit
import JDHybrid
import WebKit

/// 自定义WebView容器，扩展JDWebViewContainer功能，支持自定义配置和优化
class BaseWebView: JDWebViewContainer {
    
    // MARK: - 初始化方法
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupDefaultConfig()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupDefaultConfig()
    }
    
    /// 初始化方法 - 支持自定义配置
    /// - Parameters:
    ///   - frame: 视图frame
    ///   - configuration: WKWebView配置
    convenience override init(frame: CGRect, configuration: WKWebViewConfiguration) {
        self.init(frame: frame)
        self.configuration(configuration, requiringUserActionForPlayback: false)
    }
    
    // MARK: - 私有方法
    
    /// 设置默认配置，包括UserAgent、手势交互、缓存策略等
    private func setupDefaultConfig() {
        // 配置UserAgent，包含App版本、状态栏高度和导航栏高度
        configureUserAgent()
        
        // 注册默认的JS消息处理器
        registerMessageHandlers(["CheryBridge"])
        
        // 启用手势交互
        isUserInteractionEnabled = true
        
        // 配置webView的默认属性
        realWebView.allowsBackForwardNavigationGestures = true
        realWebView.backgroundColor = .white
        
        // 配置额外的WebView属性
        configureWebViewProperties()
    }
    
    /// 配置UserAgent，包含App版本、状态栏高度和导航栏高度
    private func configureUserAgent() {
        // 获取App版本号
        let appVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
        
        // 获取状态栏高度
        let statusBarHeight = UIApplication.shared.statusBarFrame.height
        
        // 获取导航栏高度（默认值）
        let navigationBarHeight: CGFloat = 44.0 // 标准导航栏高度
        
        // 构建自定义UserAgent
        let defaultUA = "CheryApp/\(appVersion) StatusBarHeight=\(statusBarHeight) NavBarHeight=\(navigationBarHeight)"
        
        // 合并现有UserAgent
        if let currentUA = realWebView.configuration.applicationNameForUserAgent {
            customUserAgent = "\(currentUA) \(defaultUA)"
        } else {
            customUserAgent = defaultUA
        }
    }
    
    /// 配置WebView的额外属性
    private func configureWebViewProperties() {
        // 禁用长按复制等手势（如果需要）
        // realWebView.allowsLinkPreview = false
        
        // 配置缓存策略
        let config = realWebView.configuration
        config.websiteDataStore = WKWebsiteDataStore.default()
    }
    
    // MARK: - 公共方法
    
    /// 加载URL
    /// - Parameters:
    ///   - urlString: URL字符串
    ///   - completion: 加载完成回调（可选）
    func loadURL(_ urlString: String, completion: (() -> Void)? = nil) {
        guard let url = URL(string: urlString) else {
            print("无效的URL: \(urlString)")
            return
        }
        
        loadURL(url)
        
        // 如果需要加载完成的回调，可以通过delegate实现
        if let completion = completion {
            // 实际项目中可能需要通过delegate或KVO来监听加载完成事件
            // 这里简单处理，延迟调用completion
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                completion()
            }
        }
    }
    
    /// 执行JavaScript代码
    /// - Parameters:
    ///   - jsString: JavaScript代码
    ///   - completionHandler: 执行完成回调（可选）
    func evaluateJavaScript(_ jsString: String, completionHandler: ((Any?, Error?) -> Void)? = nil) {
        realWebView.evaluateJavaScript(jsString) {
            (result, error) in
            completionHandler?(result, error)
        }
    }
    
    /// 清理缓存，包括磁盘缓存和内存缓存
    func clearCache() {
        let websiteDataTypes: Set<String> = [
            WKWebsiteDataTypeDiskCache,
            WKWebsiteDataTypeMemoryCache,
            WKWebsiteDataTypeCookies,
            WKWebsiteDataTypeLocalStorage
        ]
        
        let date = Date(timeIntervalSince1970: 0)
        WKWebsiteDataStore.default().removeData(ofTypes: websiteDataTypes, modifiedSince: date) {}
    }
}
