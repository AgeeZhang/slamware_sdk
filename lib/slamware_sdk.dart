import 'dart:async';

import 'package:flutter/services.dart';

class SlamwareSdk {
  static const MethodChannel _channel = MethodChannel('slamware_sdk');

  // 获取平台版本用于验证插件是否可用
  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  // 连接到底盘适配老的工程
  static Future<bool?> connectAgv(String ip) async {
    final bool? success = await _channel.invokeMethod('connectAgv', {"ip": ip});
    return success;
  }

  // 连接到底盘可修改端口
  static Future<bool?> connect(String ip, String port) async {
    final bool? success =
        await _channel.invokeMethod('connect', {"ip": ip, "port": port});
    return success;
  }

  // 与底盘断开连接
  static Future<bool?> get disconnect async {
    final bool? success = await _channel.invokeMethod('disconnect');
    return success;
  }

  // 校验底盘连接状态
  static Future<bool?> get isConnection async {
    final bool? success = await _channel.invokeMethod('isConnection');
    return success;
  }

  // 上传地图墙
  static Future<bool?> uploadMap(String path) async {
    final bool? success = await _channel.invokeMethod('uploadMap', {"path": path});
    return success;
  }

  // 设置最大线速度
  static Future<bool?> setSpeed(String value) async {
    final bool? success = await _channel.invokeMethod('setSpeed', {"value": value});
    return success;
  }

  // 设置最大角速度
  static Future<bool?> setAngularSpeed(String value) async {
    final bool? success = await _channel.invokeMethod('setAngularSpeed', {"value": value});
    return success;
  }

  // 获取底盘信息
  static Future<String?> get getInfo async {
    final String? info = await _channel.invokeMethod('getInfo');
    return info;
  }

  // 查询线程状态
  @Deprecated("线程重新管理后不再需要此方法")
  static Future<bool?> get threadInfo async {
    final bool? success = await _channel.invokeMethod('threadInfo');
    return success;
  }

  // 控制底盘回到充电桩
  static Future<dynamic> get getBackHome async {
    await _channel.invokeMethod('action', {"mode": "backHome"});
  }

  // 底盘移动到指定区域
  static Future<dynamic> agvMoveTo(
      double tarX, double tarY, double tarZ) async {
    await _channel.invokeMethod('action', {
      "mode": "moveTo",
      'tarX': tarX.toString(),
      'tarY': tarY.toString(),
      'tarZ': tarZ.toString()
    });
  }

  // 底盘暂停移动
  static Future<dynamic> get actionCancel async {
    await _channel.invokeMethod('action', {"mode": "cancel"});
  }

  // 指定底盘移动方向并移动
  static Future<dynamic> agvMoveBy(String mode) async {
    if (!['moveForward', 'moveBackward', 'turnLeft', 'turnRight']
        .contains(mode)) {
      return;
    }
    await _channel.invokeMethod('action', {"mode": mode});
  }

  // 底盘关机
  static Future<bool?> shutdown(String restartTimeIntervalMinute, String shutdownTimeIntervalMinute) async {
    final bool? success = await _channel.invokeMethod('shutdown', {"restartTimeIntervalMinute": restartTimeIntervalMinute,"shutdownTimeIntervalMinute":shutdownTimeIntervalMinute});
    return success;
  }
}
