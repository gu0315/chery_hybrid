//
//  StatusBarPlugin.swift
//  chery_ios
//
//  Created by 顾钱想 on 10/11/25.
//

import Foundation
// MARK: - 自定义JSBridge插件：用于控制状态栏和导航栏

/// 状态栏控制插件，继承自JDBridgeBasePlugin
@objc class StatusBarPlugin: JDBridgeBasePlugin {
    /// 执行JS调用
    /// - Parameters:
    ///   - action: 要执行的动作
    ///   - params: 参数
    ///   - jsBridgeCallback: 回调对象
    /// - Returns: 是否执行成功
    @objc override func excute(_ action: String, params: [AnyHashable : Any], callback: JDBridgeCallBack) -> Bool {
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
        guard let immersive = params["immersive"] as? Bool else {
            callback.onFail(NSError(domain: "StatusBarPlugin", code: 1002, userInfo: [NSLocalizedDescriptionKey: "缺少immersive参数"]))
            return
        }
        
        DispatchQueue.main.async {
            // 通知WebView控制器更新状态栏样式
            NotificationCenter.default.post(
                name: NSNotification.Name("UpdateImmersiveStatusBar"),
                object: nil,
                userInfo: ["immersive": immersive]
            )
            
            callback.onSuccess(["success": true, "immersive": immersive])
        }
    }
    
    /// 设置导航栏隐藏
    private func handleSetNavigationBarHidden(params: [AnyHashable : Any]!, callback: JDBridgeCallBack!) {
        guard let hidden = params["hidden"] as? Bool else {
            callback.onFail(NSError(domain: "StatusBarPlugin", code: 1003, userInfo: [NSLocalizedDescriptionKey: "缺少hidden参数"]))
            return
        }
        
        let animated = params["animated"] as? Bool ?? true
        
        DispatchQueue.main.async {
            // 通知WebView控制器更新导航栏状态
            NotificationCenter.default.post(
                name: NSNotification.Name("UpdateNavigationBarHidden"),
                object: nil,
                userInfo: ["hidden": hidden, "animated": animated]
            )
            callback.onSuccess(["success": true, "hidden": hidden])
        }
    }
    
    /// 获取状态栏信息
    private func handleGetStatusBarInfo(callback: JDBridgeCallBack!) {
        var dic: [String: CGFloat] = [:]
        dic["navBarHeight"] = Const.realNavBarHeight
        dic["statusBarHeight"] = Const.statusBarHeight
        dic["screenHeight"] = Const.screenHeight
        dic["bottomSafeHeight"] = Const.bottomSafeHeight
        callback.onSuccess(dic)
    }
    
    /// 获取屏幕信息
    private func handleGetScreenInfo(callback: JDBridgeCallBack!) {
        let screenInfo = [
            "screenWidth": Const.screenWidth,
            "screenHeight": Const.screenHeight,
            "statusBarHeight": Const.statusBarHeight,
            "navBarHeight": Const.navBarHeight,
            "bottomSafeHeight": Const.bottomSafeHeight,
            "isIphoneX": Const.isIphoneX,
            "deviceModel": Const.appPlatform(),
            "systemVersion": Const.OSVersion(),
            "appVersion": Const.appVersion(),
        ] as [String : Any]
        callback.onSuccess(screenInfo)
    }
}

