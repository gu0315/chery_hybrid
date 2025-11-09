//
//  QXTestModule.m
//  QXTestUniPlugin
//
//  Created by 顾钱想 on 11/9/25.
//

#import "QXTestModule.h"

@implementation QXTestModule

// 通过宏 UNI_EXPORT_METHOD 将异步方法暴露给 js 端
UNI_EXPORT_METHOD(@selector(testAsyncFunc:callback:))

// 通过宏 UNI_EXPORT_METHOD_SYNC 将同步方法暴露给 js 端
UNI_EXPORT_METHOD_SYNC(@selector(testSyncFunc:))

/// 异步方法（注：异步方法会在主线程（UI线程）执行）
/// @param options js 端调用方法时传递的参数   支持：String、Number、Boolean、JsonObject 类型
/// @param callback 回调方法，回传参数给 js 端   支持： NSString、NSDictionary（只能包含基本数据类型）、NSNumber 类型
- (void)testAsyncFunc:(NSDictionary *)options callback:(UniModuleKeepAliveCallback)callback {
    NSLog(@"gu-----111-----%@",options);
    // options 为 js 端调用此方法时传递的参数 NSLog(@"%@",options); // 可以在该方法中实现原生能力，然后通过 callback 回调到 js

   if (callback) {
       // 第一个参数为回传给js端的数据，第二个参数为标识，表示该回调方法是否支持多次调用，如果原生端需要多次回调js端则第二个参数传 YES;
        callback(@"success",NO);

    }
}
/// 同步方法（注：同步方法会在 js 线程执行）
/// @param options js 端调用方法时传递的参数   支持：String、Number、Boolean、JsonObject 类型
- (NSString *)testSyncFunc:(NSDictionary *)options {
    // options 为 js 端调用此方法时传递的参数
    NSLog(@"gu----------%@",options);
    /*
     可以在该方法中实现原生功能，然后直接通过 return 返回参数给 js
     */

    // 同步返回参数给 js 端  支持：NSString、NSDictionary（只能包含基本数据类型）、NSNumber 类型
    return @"success";
}


@end
