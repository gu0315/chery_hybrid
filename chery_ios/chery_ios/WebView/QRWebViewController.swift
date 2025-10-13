//
//  WebViewController.swift
//  chery_ios
//
//  Created by 顾钱想 on 10/10/25.
//

import UIKit
import WebKit
import SnapKit
/// 自定义WebView控制器，支持通过URL或JS控制沉浸式状态栏和导航栏
class QRWebViewController: UIViewController {
    
    // MARK: - 公开属性
    /// WebView实例
    var webView: JDWebViewContainer!
    /// 加载的URL
    var urlString: String?

    // MARK: - 布局控制属性
    
    /// 是否隐藏导航栏（默认为false）
    /// 当值变化时，会自动更新导航栏显示状态和WebView布局
    var isNavigationBarHidden: Bool = false {
        didSet {
            updateUIForLayoutChanges()
        }
    }
    
    /// 是否使用沉浸式状态栏（默认为true）
    /// 当值变化时，会自动更新状态栏样式
    var isImmersiveStatusBar: Bool = false {
        didSet {
            updateStatusBarStyle()
        }
    }
    
    /// 是否需要处理底部安全区（默认为false）
    /// 当值变化时，会自动更新WebView布局
    var shouldHandleBottomSafeArea: Bool = false {
        didSet {
            updateWebViewFrame()
        }
    }
    
    
    // MARK: - 生命周期
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // 初始化UI
        setupUI()
        
        // 注册JSBridge插件，用于控制沉浸式状态栏和导航栏
        registerStatusBarPlugin()
        
        // 加载URL
        if let url = urlString {
            loadURL(url)
        }
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
        webView = JDWebViewContainer.init(frame: .init(x: 0, y: 0, width: Const.screenWidth, height: Const.screenHeight), configuration: JDWebViewContainer.defaultConfiguration())
        webView.realWebView.isInspectable = true
        webView.delegate = self
        view.addSubview(webView)
        
        // 添加约束
        setupConstraints()
        // 添加返回按钮
        if navigationController?.viewControllers.count ?? 0 > 1 {
            let backButton = UIBarButtonItem(title: "返回", style: .plain, target: self, action: #selector(backButtonClicked))
            navigationItem.leftBarButtonItem = backButton
        }
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
        webView.jsBridgeManager.registerDefaultPlugin(statusBarPlugin)
    }
    
    // MARK: - 布局更新方法
    
    /// 统一处理布局相关的UI更新
    /// 当导航栏状态变化时，同时更新导航栏显示和WebView布局
    private func updateUIForLayoutChanges() {
        DispatchQueue.main.async {
            // 更新导航栏显示状态
            self.navigationController?.setNavigationBarHidden(self.isNavigationBarHidden, animated: true)
            
            // 更新WebView布局
            self.updateWebViewFrame()
        }
    }
    
    /// 更新WebView的Frame
    /// 根据导航栏状态、沉浸式状态栏和底部安全区设置调整WebView的约束
    private func updateWebViewFrame() {
        // 确保在主队列上执行UI更新
        if Thread.isMainThread {
            performWebViewFrameUpdate()
        } else {
            DispatchQueue.main.async {
                self.performWebViewFrameUpdate()
            }
        }
    }
    
    /// 执行WebView的Frame更新（在主队列上调用）
    private func performWebViewFrameUpdate() {
        // 使用动画平滑过渡布局变化
        UIView.animate(withDuration: 0.3) {
            // 获取顶部偏移量
            let topOffset = self.calculateTopOffset()
            
            // 重新设置WebView的约束
            self.webView.snp.remakeConstraints { make in
                make.top.equalToSuperview().offset(topOffset)
                make.left.right.equalToSuperview()
                
                // 考虑底部安全区
                if self.shouldHandleBottomSafeArea {
                    make.bottom.equalToSuperview().offset(Const.bottomSafeHeight)
                } else {
                    make.bottom.equalToSuperview()
                }
            }
            
            // 强制布局更新
            self.view.layoutIfNeeded()
        }
    }
    
    /// 计算WebView顶部的偏移量
    /// 根据导航栏状态和iOS版本返回合适的顶部偏移值
    /// - Returns: 顶部偏移量
    private func calculateTopOffset() -> CGFloat {
        // 根据导航栏状态决定顶部偏移
        if isNavigationBarHidden {
            // 导航栏隐藏时，考虑状态栏高度
            if #available(iOS 13.0, *) {
                // iOS 13及以上版本使用新的API获取状态栏高度
                let windowScene = UIApplication.shared.connectedScenes
                    .first { $0.activationState == .foregroundActive } as? UIWindowScene
                return windowScene?.statusBarManager?.statusBarFrame.height ?? 0
            } else {
                // 旧版本iOS获取状态栏高度
                return UIApplication.shared.statusBarFrame.height
            }
        } else {
            // 导航栏显示时，WebView从导航栏下方开始
            return Const.navBarHeight
        }
    }
    
    /// 更新状态栏样式
    /// 根据isImmersiveStatusBar的值设置适当的状态栏样式
    private func updateStatusBarStyle() {
        if #available(iOS 13.0, *) {
            // iOS 13及以上版本使用新的API
            updateStatusBarStyleForiOS13AndAbove()
        } else {
            // iOS 13以下版本使用旧的API
            updateStatusBarStyleForiOSBelow13()
        }
    }
    
    /// iOS 13及以上版本的状态栏样式更新
    @available(iOS 13.0, *)
    private func updateStatusBarStyleForiOS13AndAbove() {
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
    }
    
    /// iOS 13以下版本的状态栏样式更新
    private func updateStatusBarStyleForiOSBelow13() {
        UIApplication.shared.statusBarStyle = isImmersiveStatusBar ? .lightContent : .default
    }
    
    /// 清理资源
    private func cleanupResources() {
        
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
        
        // 安全地转换URL字符串为URL
        if let url = URL(string: urlString) {
            webView.load(URLRequest(url: url))
        } else {
            print("无法将字符串转换为有效的URL: \(urlString)")
        }
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
    
    deinit {
    
    }
}

// MARK: - WebViewDelegate实现

extension QRWebViewController: WebViewDelegate {
    
    // 网页开始加载时调用
    func webView(_ webView: JDWebViewContainer, didStartProvisionalNavigation navigation: WKNavigation!) {
        // 开始加载时显示进度条
        
    }
    
    // 网页加载完成时调用
    func webView(_ webView: JDWebViewContainer, didFinish navigation: WKNavigation!) {
        // 隐藏进度条
        

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
        // 重新加载URL
        if let url = urlString {
            loadURL(url)
        }
    }

}

