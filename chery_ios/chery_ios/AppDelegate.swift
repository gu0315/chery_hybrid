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
        let rootViewController = QRWebViewController()
        
        // 设置加载本地jdbridge_demo.html文件
        /*if let htmlPath = Bundle.main.path(forResource: "jdbridge_demo", ofType: "html") {
            let htmlURL = URL(fileURLWithPath: htmlPath)
            rootViewController.urlString = htmlURL.absoluteString
        } else {
            print("无法找到jdbridge_demo.html文件")
        }*/
        rootViewController.isNavigationBarHidden = true
        rootViewController.urlString = "http://192.168.31.137:5173/"

        // 创建UINavigationController并设置rootViewController
        let navigationController = UINavigationController(rootViewController: rootViewController)
        self.window?.rootViewController = navigationController
        window?.makeKeyAndVisible()
        return true
    }
}

