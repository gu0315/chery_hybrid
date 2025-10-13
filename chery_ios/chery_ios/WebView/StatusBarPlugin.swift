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
       
        
        
    }
    
    /// 设置导航栏隐藏
    private func handleSetNavigationBarHidden(params: [AnyHashable : Any]!, callback: JDBridgeCallBack!) {
    
       
    }
    
    /// 获取状态栏信息
    private func handleGetStatusBarInfo(callback: JDBridgeCallBack!) {
        var dic = ["navBarHeight": Const.realNavBarHeight, "statusBarHeight": Const.statusBarHeight]
        dic["screenHeight"] = Const.screenHeight
        dic["bottomSafeHeight"] = Const.bottomSafeHeight
        callback.onSuccess(dic)
    }
    
    /// 获取屏幕信息
    private func handleGetScreenInfo(callback: JDBridgeCallBack!) {
        
    }
}

