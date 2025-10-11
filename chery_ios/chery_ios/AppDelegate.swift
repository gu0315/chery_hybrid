//
//  AppDelegate.swift
//  chery_ios
//
//  Created by 顾钱想 on 10/10/25.
//

import UIKit

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        self.window = UIWindow(frame: UIScreen.main.bounds)
        self.window?.backgroundColor = .white
        let rootViewController = BaseWebViewController()
        rootViewController.urlString = "https://www.baidu.com"
        // 创建UINavigationController并设置rootViewController
        let navigationController = UINavigationController(rootViewController: rootViewController)
        self.window?.rootViewController = navigationController
        window?.makeKeyAndVisible()
        return true
    }


    
    
}

