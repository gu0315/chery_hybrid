//
//  AppDelegate.swift
//  chery_ios
//
//  Created by 顾钱想 on 10/10/25.
//

import UIKit
import Foundation
@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // 初始化应用
        setupApplication()
        
        // 创建窗口
        self.window = UIWindow(frame: UIScreen.main.bounds)
        self.window?.backgroundColor = .white
        
        // 显示启动屏幕
        showLaunchScreen()
        
        print("应用启动完成")
        return true
    }
    
    /// 显示启动屏幕
    private func showLaunchScreen() {
        // 暂时直接设置主界面，稍后添加启动屏幕
        setupRootViewController()
    }
    
    // MARK: - 私有方法
    
    /// 初始化应用配置
    private func setupApplication() {
        // 配置网络缓存
        setupURLCache()
        
        // 检查应用版本更新
        checkAppVersion()
        
        // 初始化用户偏好设置
        // UserDefaultsManager.shared.isFirstLaunch = false
    }
    
    /// 配置URL缓存
    private func setupURLCache() {
        let cache = URLCache(
            memoryCapacity: 50 * 1024 * 1024,  // 50MB内存缓存
            diskCapacity: 200 * 1024 * 1024,   // 200MB磁盘缓存
            diskPath: "WebCache"
        )
        URLCache.shared = cache
    }
    
    /// 检查应用版本
    private func checkAppVersion() {
        let currentVersion = Const.appVersion()
        let savedVersion = UserDefaults.standard.string(forKey: "appVersion") ?? ""
        
        if currentVersion != savedVersion {
            print("应用版本更新: \(savedVersion) -> \(currentVersion)")
            UserDefaults.standard.set(currentVersion, forKey: "appVersion")
        }
    }
    
    /// 设置根视图控制器
    private func setupRootViewController() {
        let rootViewController = QRWebViewController()
        
        // 配置WebView控制器
        rootViewController.isNavigationBarHidden = true
        rootViewController.urlString = getInitialURL()
        
        // 创建导航控制器
        let navigationController = UINavigationController(rootViewController: rootViewController)
        navigationController.navigationBar.isTranslucent = false
        navigationController.navigationBar.barTintColor = .white
        navigationController.navigationBar.tintColor = .systemBlue
        
        self.window?.rootViewController = navigationController
    }
    
    /// 获取初始URL
    private func getInitialURL() -> String {
        // 优先使用本地HTML文件（开发环境）
//        if let htmlPath = Bundle.main.path(forResource: "jdbridge_demo", ofType: "html") {
//            let htmlURL = URL(fileURLWithPath: htmlPath)
//            print("使用本地HTML文件: \(htmlURL.absoluteString)")
//            return htmlURL.absoluteString
//        }
//        
        // 使用远程URL（生产环境）
        let remoteURL = "http://192.168.31.137:5173/"
        print("使用远程URL: \(remoteURL)")
        return remoteURL
    }
}

