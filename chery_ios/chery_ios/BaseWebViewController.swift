//
//  BaseWebViewController.swift
//  chery_ios
//
//  Created by 顾钱想 on 10/10/25.
//

import UIKit
import JDHybrid
import WebKit
import SnapKit

/// 自定义WebView控制器，支持通过URL或JS控制沉浸式状态栏和导航栏
class BaseWebViewController: UIViewController {
    
    // MARK: - 公开属性
    /// WebView实例
    var webView: JDWebViewContainer!
    /// 加载的URL
    var urlString: String?
    /// 进度条
    private var progressView: UIProgressView!
    
    /// 是否隐藏导航栏（默认为false）
    var isNavigationBarHidden: Bool = false {
        didSet {
            // 确保在主队列上执行UI更新
            DispatchQueue.main.async {
                self.navigationController?.setNavigationBarHidden(self.isNavigationBarHidden, animated: true)
                self.updateWebViewFrame()
            }
        }
    }
    
    /// 是否使用沉浸式状态栏（默认为false）
    var isImmersiveStatusBar: Bool = false {
        didSet {
            updateStatusBarStyle()
        }
    }
    
    /// 是否需要处理底部安全区（默认为true）
    var shouldHandleBottomSafeArea: Bool = true {
        didSet {
            updateWebViewFrame()
        }
    }
    
    // MARK: - 生命周期
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // 初始化UI
        setupUI()
        
        // 加载URL
        if let url = urlString {
            loadURL(url)
        }
        
        // 注册JSBridge插件，用于控制沉浸式状态栏和导航栏
        registerStatusBarPlugin()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        // 应用导航栏设置
        navigationController?.setNavigationBarHidden(isNavigationBarHidden, animated: animated)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        // 清理资源
        cleanupResources()
    }
    
    // MARK: - 初始化方法
    
    /// 初始化方法 - 通过URL
    /// - Parameter url: 要加载的URL
    convenience init(url: String) {
        self.init()
        self.urlString = url
    }
    
    // MARK: - 私有方法
    
    /// 设置UI
    private func setupUI() {
        // 设置背景色
        view.backgroundColor = .white
        
        // 初始化WebView
        webView = JDWebViewContainer.init(frame: view.bounds)
        webView.delegate = self
        view.addSubview(webView)
        
        // 初始化进度条
        progressView = UIProgressView(progressViewStyle: .default)
        progressView.tintColor = .blue
        progressView.trackTintColor = .clear
        view.addSubview(progressView)
        
        // 添加约束
        setupConstraints()
        
        // 添加返回按钮
        if navigationController?.viewControllers.count ?? 0 > 1 {
            let backButton = UIBarButtonItem(title: "返回", style: .plain, target: self, action: #selector(backButtonClicked))
            navigationItem.leftBarButtonItem = backButton
        }
        
        // 监听进度变化
        webView.addObserver(self, forKeyPath: "estimatedProgress", options: .new, context: nil)
        webView.addObserver(self, forKeyPath: "title", options: .new, context: nil)
    }
    
    /// 添加约束
    private func setupConstraints() {
        // WebView约束
        webView.snp.makeConstraints { make in
            make.top.equalToSuperview()
            make.left.equalToSuperview()
            make.right.equalToSuperview()
            make.bottom.equalToSuperview()
        }
        
        // 进度条约束
        progressView.snp.makeConstraints { make in
            make.top.equalToSuperview()
            make.left.equalToSuperview()
            make.right.equalToSuperview()
            make.height.equalTo(2)
        }
    }
    
    /// 配置WebView
    private func configureWebView() {
        // 设置WebView的基本属性
        webView.isUserInteractionEnabled = true
        webView.realWebView.scrollView.showsVerticalScrollIndicator = false
        webView.realWebView.scrollView.showsHorizontalScrollIndicator = false
        webView.realWebView.scrollView.bounces = true
        
        // 配置缓存策略
        let cacheConfig = URLCache(memoryCapacity: 1024 * 1024 * 10, diskCapacity: 1024 * 1024 * 100, diskPath: "WebCache")
        URLCache.shared = cacheConfig
    }
    
    /// 注册状态栏控制插件
    private func registerStatusBarPlugin() {
        // 创建自定义插件
        let statusBarPlugin = StatusBarPlugin()
        statusBarPlugin.viewController = self
        
        // 注册到JSBridge
        webView.jsBridgeManager.registerDefaultPlugin(statusBarPlugin)
    }
    
    /// 更新WebView的Frame
    private func updateWebViewFrame() {
        // 确保在主队列上执行UI更新
        DispatchQueue.main.async {
            // 优化WebView的布局更新
            UIView.animate(withDuration: 0.3) {
                self.webView.snp.remakeConstraints { make in
                    if self.isNavigationBarHidden {
                        // 导航栏隐藏时，考虑状态栏高度
                        // 即使导航栏隐藏，也要考虑状态栏的高度，确保内容不被遮挡
                        if #available(iOS 13.0, *) {
                            // iOS 13及以上版本使用新的API获取状态栏高度
                            let windowScene = UIApplication.shared.connectedScenes
                                .first { $0.activationState == .foregroundActive } as? UIWindowScene
                            let statusBarHeight = windowScene?.statusBarManager?.statusBarFrame.height ?? 0
                            make.top.equalToSuperview().offset(statusBarHeight)
                        } else {
                            // 旧版本iOS获取状态栏高度
                            make.top.equalToSuperview().offset(UIApplication.shared.statusBarFrame.height)
                        }
                    } else {
                        // 导航栏显示时，WebView从导航栏下方开始
                        // 使用Const中的计算方式，确保在任何情况下都能获取正确的高度
                        make.top.equalToSuperview().offset(Const.navBarHeight)
                    }
                    make.left.equalToSuperview()
                    make.right.equalToSuperview()
                    // 考虑底部安全区
                    if self.shouldHandleBottomSafeArea {
                        make.bottom.equalToSuperview().offset(-Const.bottomSafeHeight)
                    } else {
                        make.bottom.equalToSuperview()
                    }
                }
                // 强制布局更新
                self.view.layoutIfNeeded()
            }
        }
    }
    
    /// 更新状态栏样式
    private func updateStatusBarStyle() {
        if #available(iOS 13.0, *) {
            // iOS 13及以上版本使用新的API
            // 移除旧的状态栏视图
            for subview in view.subviews where subview.frame == UIApplication.shared.statusBarFrame {
                subview.removeFromSuperview()
            }
            // 创建新的状态栏背景视图
            let statusBar = UIView(frame: UIApplication.shared.statusBarFrame)
            statusBar.backgroundColor = isImmersiveStatusBar ? .clear : .white
            view.addSubview(statusBar)
            view.sendSubviewToBack(statusBar)
            // 使用UIWindow的方式设置状态栏样式
            if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
               let keyWindow = windowScene.windows.first(where: { $0.isKeyWindow }) {
               keyWindow.overrideUserInterfaceStyle = isImmersiveStatusBar ? .dark : .light
            }
        } else {
            // iOS 13以下版本使用旧的API
            if isImmersiveStatusBar {
                UIApplication.shared.statusBarStyle = .lightContent
            } else {
                UIApplication.shared.statusBarStyle = .default
            }
        }
    }
    
    /// 清理资源
    private func cleanupResources() {
        // 移除所有临时视图
        for subview in view.subviews where subview != webView && subview != progressView {
            subview.removeFromSuperview()
        }
    }
    
    // MARK: - 事件处理
    
    /// 返回按钮点击事件
    @objc private func backButtonClicked() {
        if webView.canGoBack() {
            webView.goBack()
        } else {
            navigationController?.popViewController(animated: true)
        }
    }
    
    // MARK: - 加载方法
    
    /// 加载URL
    /// - Parameter urlString: URL字符串
    func loadURL(_ urlString: String) {
        // 检查URL中是否包含状态栏和导航栏的控制参数
        parseURLParameters(urlString: urlString)
        
        webView.load(URLRequest(url: URL(string: urlString)!))
    }
    
    /// 解析URL参数
    /// - Parameter urlString: URL字符串
    private func parseURLParameters(urlString: String) {
        guard let url = URL(string: urlString),
              let components = URLComponents(url: url, resolvingAgainstBaseURL: true) else {
            return
        }
        
        // 处理沉浸式状态栏参数
        if let immersiveParam = components.queryItems?.first(where: { $0.name == "immersive" })?.value,
           immersiveParam == "true" {
            isImmersiveStatusBar = true
        }
        
        // 处理导航栏隐藏参数
        if let navHiddenParam = components.queryItems?.first(where: { $0.name == "navHidden" })?.value,
           navHiddenParam == "true" {
            isNavigationBarHidden = true
        }
        
        // 处理状态栏样式参数
        if let statusBarStyleParam = components.queryItems?.first(where: { $0.name == "statusBarStyle" })?.value,
           statusBarStyleParam == "dark" {
            // 可以根据需要添加更多状态栏样式控制
        }
    }
    
    
    // MARK: - KVO监听
    
    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
        if keyPath == "estimatedProgress" {
            if let progress = change?[.newKey] as? Float {
                progressView.progress = progress
                if progress == 1.0 {
                    UIView.animate(withDuration: 0.3, animations: {
                        self.progressView.alpha = 0
                    }, completion: {
                        _ in
                        self.progressView.progress = 0
                        self.progressView.alpha = 1
                    })
                }
            }
        } else if keyPath == "title" {
            if let title = change?[.newKey] as? String {
                navigationItem.title = title
            }
        }
    }
    
    deinit {
        // 移除观察者
        webView.removeObserver(self, forKeyPath: "estimatedProgress")
        webView.removeObserver(self, forKeyPath: "title")
    }
}

// MARK: - WebViewDelegate实现

extension BaseWebViewController: WebViewDelegate {
    
    // 网页开始加载时调用
    func webView(_ webView: JDWebViewContainer, didStartProvisionalNavigation navigation: WKNavigation!) {
        // 开始加载时显示进度条
        UIView.animate(withDuration: 0.3) {
            self.progressView.alpha = 1
        }
    }
    
    // 网页加载完成时调用
    func webView(_ webView: JDWebViewContainer, didFinish navigation: WKNavigation!) {
        // 隐藏进度条
        UIView.animate(withDuration: 0.3) {
            self.progressView.alpha = 0
        }
    }
    
    // 网页加载失败时调用
    func webView(_ webView: JDWebViewContainer, didFail navigation: WKNavigation!, withError error: Error) {
        // 显示加载失败提示
    }
    
    // 决定是否允许导航
    func webView(_ webView: JDWebViewContainer, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: ((WKNavigationActionPolicy) -> Void)) {
        // 可以在这里添加导航控制逻辑
        decisionHandler(.allow)
    }
    
    /// 重试加载
    @objc private func retryLoading() {
        // 移除所有错误提示视图
        for subview in view.subviews where subview != webView && subview != progressView {
            subview.removeFromSuperview()
        }
        
        // 重新加载URL
        if let url = urlString {
            loadURL(url)
        }
    }
}

// MARK: - 自定义JSBridge插件：用于控制状态栏和导航栏

/// 状态栏控制插件，继承自JDBridgeBasePlugin
class StatusBarPlugin: JDBridgeBasePlugin {
    
    /// 关联的ViewController
    weak var viewController: BaseWebViewController?
    
    /// 执行JS调用
    /// - Parameters:
    ///   - action: 要执行的动作
    ///   - params: 参数
    ///   - jsBridgeCallback: 回调对象
    /// - Returns: 是否执行成功
    override func excute(_ action: String, params: [AnyHashable : Any], callback: JDBridgeCallBack) -> Bool {
        // 确保视图控制器存在
        guard self.viewController != nil else {
            callback.onFail(NSError(domain: "StatusBarPlugin", code: 1000, userInfo: [NSLocalizedDescriptionKey: "视图控制器已释放"]))
            return false
        }
        // 根据action执行不同的操作
        switch action {
        case "setImmersiveStatusBar":
            // 设置沉浸式状态栏
            handleSetImmersiveStatusBar(params: params, callback: callback)
            return true
        case "setNavigationBarHidden":
            // 设置导航栏隐藏
            handleSetNavigationBarHidden(params: params, callback: callback)
            return true
        case "getStatusBarInfo":
            // 获取状态栏信息
            handleGetStatusBarInfo(callback: callback)
            return true
        case "getScreenInfo":
            // 获取屏幕信息
            handleGetScreenInfo(callback: callback)
            return true
        default:
            callback.onFail(NSError(domain: "StatusBarPlugin", code: 1001, userInfo: [NSLocalizedDescriptionKey: "unknown"]))
            return false
        }
    }
    
    // MARK: - 私有处理方法
    
    /// 设置沉浸式状态栏
    private func handleSetImmersiveStatusBar(params: [AnyHashable : Any]!, callback: JDBridgeCallBack!) {
        guard let viewController = self.viewController else {
            callback.onFail(NSError(domain: "StatusBarPlugin", code: 1000, userInfo: [NSLocalizedDescriptionKey: "视图控制器已释放"]))
            return
        }
        
        // 检查参数
        guard let immersive = params?["immersive"] as? Bool else {
            callback.onFail(NSError(domain: "StatusBarPlugin", code: 1002, userInfo: [NSLocalizedDescriptionKey: "缺少immersive参数"]))
            return
        }
        
        // 在主线程更新UI
        DispatchQueue.main.async {
            viewController.isImmersiveStatusBar = immersive
            
            // 构建成功响应
            let result: [String: Any] = [
                "success": true,
                "immersive": immersive,
                "message": "设置沉浸式状态栏成功"
            ]
            
            callback.onSuccess(result)
        }
    }
    
    /// 设置导航栏隐藏
    private func handleSetNavigationBarHidden(params: [AnyHashable : Any]!, callback: JDBridgeCallBack!) {
        guard let viewController = self.viewController else {
            callback.onFail(NSError(domain: "StatusBarPlugin", code: 1000, userInfo: [NSLocalizedDescriptionKey: "视图控制器已释放"]))
            return
        }
        
        // 检查参数
        guard let hidden = params?["hidden"] as? Bool else {
            callback.onFail(NSError(domain: "StatusBarPlugin", code: 1002, userInfo: [NSLocalizedDescriptionKey: "缺少hidden参数"]))
            return
        }
        
        // 获取是否使用动画
        let animated = params?["animated"] as? Bool ?? true
        
        // 在主线程更新UI
        DispatchQueue.main.async {
            viewController.isNavigationBarHidden = hidden
            
            // 构建成功响应
            let result: [String: Any] = [
                "success": true,
                "hidden": hidden,
                "animated": animated,
                "message": "设置导航栏隐藏成功"
            ]
            
            callback.onSuccess(result)
        }
    }
    
    /// 获取状态栏信息
    private func handleGetStatusBarInfo(callback: JDBridgeCallBack!) {
        guard let viewController = self.viewController else {
            callback.onFail(NSError(domain: "StatusBarPlugin", code: 1000, userInfo: [NSLocalizedDescriptionKey: "视图控制器已释放"]))
            return
        }
        // 获取状态栏高度
        let statusBarHeight = UIApplication.shared.statusBarFrame.height
        // 获取导航栏高度
        let navigationBarHeight = viewController.navigationController?.navigationBar.frame.height ?? 44.0
        // 构建状态栏信息
        let statusBarInfo: [String: Any] = [
            "success": true,
            "statusBarHeight": statusBarHeight,
            "navigationBarHeight": navigationBarHeight,
            "isImmersiveStatusBar": viewController.isImmersiveStatusBar,
            "isNavigationBarHidden": viewController.isNavigationBarHidden,
            "screenWidth": UIScreen.main.bounds.width,
            "screenHeight": UIScreen.main.bounds.height
        ]
        // 返回状态栏信息
        callback.onSuccess(statusBarInfo)
    }
    
    /// 获取屏幕信息
    private func handleGetScreenInfo(callback: JDBridgeCallBack!) {
        // 获取屏幕尺寸
        let screenBounds = UIScreen.main.bounds
        let screenWidth = screenBounds.width
        let screenHeight = screenBounds.height
        // 获取安全区域信息
        var safeAreaInsets = UIEdgeInsets.zero
        if #available(iOS 11.0, *) {
            if let window = UIApplication.shared.keyWindow {
                safeAreaInsets = window.safeAreaInsets
            }
        }
        // 构建屏幕信息
        let screenInfo: [String: Any] = [
            "success": true,
            "screenWidth": screenWidth,
            "screenHeight": screenHeight,
            "safeAreaInsets": [
                "top": safeAreaInsets.top,
                "left": safeAreaInsets.left,
                "bottom": safeAreaInsets.bottom,
                "right": safeAreaInsets.right
            ],
            "scale": UIScreen.main.scale
        ]
        
        // 返回屏幕信息
        callback.onSuccess(screenInfo)
    }
}

