//
//  QRWebViewController.swift
//  chery_ios
//
//  Created by 顾钱想 on 10/10/25.
//

import UIKit
import WebKit
import SnapKit
import Lottie

/// 自定义WebView控制器，支持通过URL或JS控制沉浸式状态栏和导航栏
/// 优化版本：提升性能、用户体验和代码可维护性
class QRWebViewController: UIViewController {
    
    // MARK: - 公开属性
    /// WebView实例
    var webView: JDWebViewContainer!
    /// 加载的URL
    var urlString: String?
    
    // MARK: - 私有属性
    /// Lottie动画加载视图
    private var lottieLoadingView: LottieAnimationView?
    /// 原生加载指示器
    private var nativeLoadingView: UIView?
    /// 是否正在加载
    private var isLoading: Bool = false
    /// 加载开始时间
    private var loadStartTime: Date?
    /// 最大加载超时时间（秒）
    private let maxLoadTimeout: TimeInterval = 30.0
    /// 加载超时任务（可取消）
    private var loadTimeoutWorkItem: DispatchWorkItem?
    /// 下拉刷新控件
    private var refreshControl: UIRefreshControl?
    /// 错误覆盖层
    private var errorOverlay: UIView?

    // MARK: - 布局控制属性
    
    /// 是否隐藏导航栏（默认为false）
    /// 当值变化时，会自动更新导航栏显示状态和WebView布局
    var isNavigationBarHidden: Bool = false {
        didSet {
            guard oldValue != isNavigationBarHidden else { return }
            updateUIForLayoutChanges()
        }
    }
    
    /// 是否使用沉浸式状态栏（默认为false）
    /// 当值变化时，会自动更新状态栏样式
    var isImmersiveStatusBar: Bool = false {
        didSet {
            guard oldValue != isImmersiveStatusBar else { return }
            updateStatusBarStyle()
        }
    }
    
    /// 是否需要处理底部安全区（默认为false）
    /// 当值变化时，会自动更新WebView布局
    var shouldHandleBottomSafeArea: Bool = false {
        didSet {
            guard oldValue != shouldHandleBottomSafeArea else { return }
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
        
        // 设置通知监听
        setupNotificationObservers()
        
        // 加载URL
        if let url = urlString {
            loadURL(url)
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        // 应用导航栏设置
        navigationController?.setNavigationBarHidden(isNavigationBarHidden, animated: animated)
        
        // 更新状态栏样式
        updateStatusBarStyle()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        // 清理资源
        cleanupResources()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        // 确保WebView布局正确
        updateWebViewFrame()
    }

    override var preferredStatusBarStyle: UIStatusBarStyle {
        if isImmersiveStatusBar {
            return .lightContent
        } else {
            if #available(iOS 13.0, *) {
                return .darkContent
            } else {
                return .default
            }
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // 低内存时尝试释放缓存
        clearWebViewCache()
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
        view.backgroundColor = .systemBackground
        
        // 初始化WebView
        setupWebView()
        
        // 设置原生风格加载指示器
        setupNativeLoadingView()
        
        // 添加约束
        setupConstraints()
        
        // 添加返回按钮
        setupNavigationBar()
    }
    
    /// 设置WebView
    private func setupWebView() {
        let configuration = JDWebViewContainer.defaultConfiguration()
        
        // 优化WebView配置
        configuration.allowsInlineMediaPlayback = true
        configuration.mediaTypesRequiringUserActionForPlayback = []
        configuration.suppressesIncrementalRendering = false
        
        webView = JDWebViewContainer(
            frame: .init(x: 0, y: 0, width: Const.screenWidth, height: Const.screenHeight),
            configuration: configuration
        )
        
        webView.realWebView.isInspectable = true
        webView.delegate = self
        webView.backgroundColor = .systemBackground
        
        // 优化滚动性能
        webView.realWebView.scrollView.decelerationRate = .normal
        webView.realWebView.scrollView.bounces = true
        webView.realWebView.scrollView.showsVerticalScrollIndicator = true
        webView.realWebView.scrollView.showsHorizontalScrollIndicator = false
        
        view.addSubview(webView)
        
        // 下拉刷新
        let refresh = UIRefreshControl()
        refresh.addTarget(self, action: #selector(handlePullToRefresh), for: .valueChanged)
        webView.realWebView.scrollView.refreshControl = refresh
        self.refreshControl = refresh
    }
    
    /// 设置原生风格加载指示器
    private func setupNativeLoadingView() {
        // 创建Lottie动画加载视图
        setupLottieLoadingView()
        
        // 创建原生加载指示器作为备选
        setupNativeLoadingIndicator()
    }
    
    /// 设置Lottie动画加载视图
    private func setupLottieLoadingView() {
        // 创建Lottie动画视图
        lottieLoadingView = LottieAnimationView(name: "loading") // 需要添加loading.json文件
        lottieLoadingView?.contentMode = .scaleAspectFit
        lottieLoadingView?.loopMode = .loop
        lottieLoadingView?.alpha = 0.0
        
        if let lottieView = lottieLoadingView {
            view.addSubview(lottieView)
            // 确保在屏幕中心显示
            lottieView.snp.makeConstraints { make in
                make.center.equalToSuperview()
                make.width.height.equalTo(80)
            }
        }
    }
    
    /// 设置原生加载指示器
    private func setupNativeLoadingIndicator() {
        nativeLoadingView = UIView()
        nativeLoadingView?.backgroundColor = UIColor.systemBackground.withAlphaComponent(0.95)
        nativeLoadingView?.layer.cornerRadius = 12
        nativeLoadingView?.alpha = 0.0
        
        // 创建加载指示器
        let indicator = UIActivityIndicatorView(style: .large)
        indicator.color = .systemBlue
        indicator.startAnimating()
        
        // 创建加载文本
        let label = UILabel()
        label.text = "加载中..."
        label.textAlignment = .center
        label.font = .systemFont(ofSize: 16, weight: .medium)
        label.textColor = .label
        
        nativeLoadingView?.addSubview(indicator)
        nativeLoadingView?.addSubview(label)
        view.addSubview(nativeLoadingView!)
        
        // 确保在屏幕中心显示
        nativeLoadingView?.snp.makeConstraints { make in
            make.center.equalToSuperview()
            make.width.equalTo(120)
            make.height.equalTo(100)
        }
        
        indicator.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalToSuperview().offset(20)
        }
        
        label.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(indicator.snp.bottom).offset(12)
            make.left.right.equalToSuperview().inset(16)
        }
    }
    
    
    /// 设置导航栏
    private func setupNavigationBar() {
        if navigationController?.viewControllers.count ?? 0 > 1 {
            let backButton = UIBarButtonItem(
                image: UIImage(systemName: "chevron.left"),
                style: .plain,
                target: self,
                action: #selector(backButtonClicked)
            )
            navigationItem.leftBarButtonItem = backButton
        }
        
        // 设置导航栏样式
        navigationController?.navigationBar.prefersLargeTitles = false
        navigationController?.navigationBar.isTranslucent = false
    }
    
    /// 添加约束
    private func setupConstraints() {
        // WebView约束
        webView.snp.makeConstraints { make in
            make.top.equalTo(view.safeAreaLayoutGuide.snp.top)
            make.left.equalToSuperview()
            make.right.equalToSuperview()
            make.bottom.equalTo(view.safeAreaLayoutGuide.snp.bottom)
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
    
    /// 设置通知监听
    private func setupNotificationObservers() {
        // 监听状态栏更新通知
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleImmersiveStatusBarUpdate),
            name: NSNotification.Name("UpdateImmersiveStatusBar"),
            object: nil
        )
        
        // 监听导航栏更新通知
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleNavigationBarUpdate),
            name: NSNotification.Name("UpdateNavigationBarHidden"),
            object: nil
        )
    }
    
    @objc private func handleImmersiveStatusBarUpdate(_ notification: Notification) {
        if let userInfo = notification.userInfo,
           let immersive = userInfo["immersive"] as? Bool {
           isImmersiveStatusBar = immersive
        }
    }
    
    @objc private func handleNavigationBarUpdate(_ notification: Notification) {
        if let userInfo = notification.userInfo,
           let hidden = userInfo["hidden"] as? Bool,
           let animated = userInfo["animated"] as? Bool {
           isNavigationBarHidden = hidden
        }
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
        UIView.animate(withDuration: 0.25) {
            self.webView.snp.remakeConstraints { make in
                if self.isNavigationBarHidden && self.isImmersiveStatusBar {
                    make.top.equalTo(self.view.snp.top)
                } else {
                    make.top.equalTo(self.view.safeAreaLayoutGuide.snp.top)
                }
                make.left.equalToSuperview()
                make.right.equalToSuperview()
                if self.shouldHandleBottomSafeArea {
                    make.bottom.equalTo(self.view.safeAreaLayoutGuide.snp.bottom)
                } else {
                    make.bottom.equalToSuperview()
                }
            }
            self.view.layoutIfNeeded()
        }
    }
    
    /// 更新状态栏样式（通过系统首选项）
    private func updateStatusBarStyle() {
        navigationController?.navigationBar.barStyle = isImmersiveStatusBar ? .black : .default
        setNeedsStatusBarAppearanceUpdate()
    }
    
    /// 清理资源
    private func cleanupResources() {
        // 停止加载
        if isLoading {
            webView.stopLoading()
        }
        
        // 清理WebView缓存
        clearWebViewCache()
        
        // 移除通知监听
        NotificationCenter.default.removeObserver(self)
        
        print("QRWebViewController资源清理完成")
    }
    
    /// 清理WebView缓存
    private func clearWebViewCache() {
        let dataStore = webView.realWebView.configuration.websiteDataStore
        let dataTypes = WKWebsiteDataStore.allWebsiteDataTypes()
        let date = Date(timeIntervalSince1970: 0)
        
        dataStore.removeData(ofTypes: dataTypes, modifiedSince: date) {
            print("WebView缓存清理完成")
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
        
        // 安全地转换URL字符串为URL
        guard let url = URL(string: urlString) else {
            showError("无效的URL地址: \(urlString)")
            return
        }
        
        // 记录加载开始时间
        loadStartTime = Date()
        isLoading = true
        
        // 显示原生加载指示器
        showNativeLoading()
        
        // 创建请求
        let request = URLRequest(url: url)
        webView.load(request)
        
        print("开始加载URL: \(urlString)")
    }
    
    /// 显示原生加载指示器
    private func showNativeLoading() {
        // 显示Lottie动画（如果可用）
        showLottieLoading()
        
        // 延迟显示原生指示器作为备选
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            if self.isLoading {
                self.showNativeLoadingIndicator()
            }
        }
    }
    
    /// 隐藏原生加载指示器
    private func hideNativeLoading() {
        hideLottieLoading()
        hideNativeLoadingIndicator()
    }
    
    /// 显示Lottie动画
    private func showLottieLoading() {
        lottieLoadingView?.alpha = 0.0
        lottieLoadingView?.play()
        UIView.animate(withDuration: 0.3) {
            self.lottieLoadingView?.alpha = 1.0
        }
    }
    
    /// 隐藏Lottie动画
    private func hideLottieLoading() {
        lottieLoadingView?.stop()
        UIView.animate(withDuration: 0.3) {
            self.lottieLoadingView?.alpha = 0.0
        }
    }
    
    /// 显示原生加载指示器
    private func showNativeLoadingIndicator() {
        nativeLoadingView?.alpha = 0.0
        UIView.animate(withDuration: 0.3) {
            self.nativeLoadingView?.alpha = 1.0
        }
    }
    
    /// 隐藏原生加载指示器
    private func hideNativeLoadingIndicator() {
        UIView.animate(withDuration: 0.3) {
            self.nativeLoadingView?.alpha = 0.0
        }
    }
    
    /// 显示错误信息
    private func showError(_ message: String) {
        // 非阻塞错误覆盖层
        showErrorOverlay(message: message)
    }

    /// 显示错误覆盖层
    private func showErrorOverlay(message: String) {
        hideErrorOverlay()
        let container = UIView()
        container.backgroundColor = UIColor.black.withAlphaComponent(0.05)
        container.layer.cornerRadius = 12
        container.clipsToBounds = true
        
        let label = UILabel()
        label.text = message
        label.textAlignment = .center
        label.numberOfLines = 0
        label.textColor = .label
        label.font = .systemFont(ofSize: 14)
        
        let button = UIButton(type: .system)
        button.setTitle("重试", for: .normal)
        button.addTarget(self, action: #selector(retryLoading), for: .touchUpInside)
        
        container.addSubview(label)
        container.addSubview(button)
        view.addSubview(container)
        
        container.snp.makeConstraints { make in
            make.center.equalToSuperview()
            make.left.greaterThanOrEqualTo(view.snp.left).offset(24)
            make.right.lessThanOrEqualTo(view.snp.right).offset(-24)
        }
        label.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(16)
            make.left.equalToSuperview().offset(16)
            make.right.equalToSuperview().offset(-16)
        }
        button.snp.makeConstraints { make in
            make.top.equalTo(label.snp.bottom).offset(12)
            make.centerX.equalToSuperview()
            make.bottom.equalToSuperview().offset(-16)
        }
        
        self.errorOverlay = container
    }
    
    private func hideErrorOverlay() {
        errorOverlay?.removeFromSuperview()
        errorOverlay = nil
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
        // 移除通知监听
        NotificationCenter.default.removeObserver(self)
        print("QRWebViewController已释放")
    }
}

// MARK: - WebViewDelegate实现

extension QRWebViewController: WebViewDelegate {
    
    // 网页开始加载时调用
    func webView(_ webView: JDWebViewContainer, didStartProvisionalNavigation navigation: WKNavigation!) {
        print("网页开始加载")
        isLoading = true
        showNativeLoading()
        
        // 启动超时检测
        startLoadTimeoutTimer()
        // 隐藏错误覆盖层
        hideErrorOverlay()
    }
    
    // 网页加载完成时调用
    func webView(_ webView: JDWebViewContainer, didFinish navigation: WKNavigation!) {
        print("网页加载完成")
        isLoading = false
        hideNativeLoading()
        refreshControl?.endRefreshing()
        
        // 计算加载时间
        if let startTime = loadStartTime {
            let loadTime = Date().timeIntervalSince(startTime)
            print("页面加载耗时: \(String(format: "%.2f", loadTime))秒")
        }
        
        // 停止超时检测
        stopLoadTimeoutTimer()
    }
    
    // 网页加载失败时调用
    func webView(_ webView: JDWebViewContainer, didFail navigation: WKNavigation!, withError error: Error) {
        print("网页加载失败: \(error.localizedDescription)")
        isLoading = false
        hideNativeLoading()
        refreshControl?.endRefreshing()
        
        // 停止超时检测
        stopLoadTimeoutTimer()
        
        // 显示错误信息
        showError("网页加载失败: \(error.localizedDescription)")
    }
    
    // 网页加载进度更新（现在用于优化加载体验）
    func webView(_ webView: JDWebViewContainer, didUpdateProgress progress: Float) {
        // 当进度达到一定值时，可以切换到更精细的加载指示器
        if progress > 0.8 {
            hideLottieLoading()
            showNativeLoadingIndicator()
        }
    }
    
    // 决定是否允许导航
    func webView(_ webView: JDWebViewContainer, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: ((WKNavigationActionPolicy) -> Void)) {
        guard let url = navigationAction.request.url else {
            decisionHandler(.cancel)
            return
        }
        
        // 处理 target=_blank 的情况：在当前视图中打开
        if navigationAction.targetFrame == nil {
            webView.load(URLRequest(url: url))
            decisionHandler(.cancel)
            return
        }
        
        // 处理外部 scheme
        let scheme = url.scheme?.lowercased() ?? ""
        let allowedSchemes: Set<String> = ["http", "https"]
        if !allowedSchemes.contains(scheme) {
            if UIApplication.shared.canOpenURL(url) {
                UIApplication.shared.open(url, options: [:], completionHandler: nil)
            }
            decisionHandler(.cancel)
            return
        }
        
        decisionHandler(.allow)
    }
    
    /// 重试加载
    @objc private func retryLoading() {
        // 重新加载URL
        if let url = urlString {
            loadURL(url)
        }
    }
    
    // MARK: - 超时检测
    
    /// 启动加载超时检测
    private func startLoadTimeoutTimer() {
        loadTimeoutWorkItem?.cancel()
        let workItem = DispatchWorkItem { [weak self] in
            guard let self = self, self.isLoading else { return }
            self.handleLoadTimeout()
        }
        loadTimeoutWorkItem = workItem
        DispatchQueue.main.asyncAfter(deadline: .now() + maxLoadTimeout, execute: workItem)
    }
    
    /// 停止加载超时检测
    private func stopLoadTimeoutTimer() {
        loadTimeoutWorkItem?.cancel()
        loadTimeoutWorkItem = nil
    }
    
    /// 处理加载超时
    private func handleLoadTimeout() {
        print("页面加载超时")
        isLoading = false
        hideNativeLoading()
        
        // 停止当前加载
        webView.stopLoading()
        
        // 显示超时错误
        showError("页面加载超时，请检查网络连接")
    }

    // MARK: - Actions
    @objc private func handlePullToRefresh() {
        hideErrorOverlay()
        if webView.realWebView.url != nil {
            webView.realWebView.reload()
        } else if let url = urlString {
            loadURL(url)
        } else {
            refreshControl?.endRefreshing()
        }
    }

}

