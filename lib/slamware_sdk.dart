
import 'dart:async';

import 'package:flutter/services.dart';

class SlamwareSdk {
  static const MethodChannel _channel = MethodChannel('slamware_sdk');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool?> connectAgv(String ip) async {
    final bool? success = await _channel.invokeMethod('connectAgv',{"ip": ip});
    return success;
  }

  static Future<bool?> connect(String ip,String port) async {
    final bool? success = await _channel.invokeMethod('connect',{"ip": ip,"port":port});
    return success;
  }

  static Future<dynamic> get disconnect async {
    await _channel.invokeMethod('disconnect');
  }

  static Future<bool?> get isConnection async {
    final bool? success = await _channel.invokeMethod('isConnection');
    return success;
  }

  static Future<dynamic> uploadMap(String path) async {
    await _channel.invokeMethod('uploadMap',{"path":path});
  }

  static Future<dynamic> setSpeed(String value) async {
    await _channel.invokeMethod('setSpeed',{"value":value});
  }

  static Future<dynamic> setAngularSpeed(String value) async {
    await _channel.invokeMethod('setAngularSpeed',{"value":value});
  }

  static Future<String?> get getInfo async {
    final String? info = await _channel.invokeMethod('getInfo');
    return info;
  }

  static Future<dynamic> get threadInfo async {
    await _channel.invokeMethod('threadInfo');
  }

  static Future<dynamic> get getBackHome async {
    await _channel.invokeMethod('action',{"mode":"backHome"});
  }

  static Future<dynamic> agvMoveTo(double tarX, double tarY, double tarZ) async {
    await _channel.invokeMethod('action',{"mode":"moveTo",'tarX': tarX.toString(), 'tarY': tarY.toString(), 'tarZ': tarZ.toString()});
  }

  static Future<dynamic> get actionCancel async {
    await _channel.invokeMethod('action',{"mode":"cancel"});
  }

  static Future<dynamic> agvMoveBy(String mode) async {
    if (!['moveForward', 'moveBackward', 'turnLeft', 'turnRight']
        .contains(mode)) {
      return;
    }
    await _channel.invokeMethod('action',{"mode":mode});
  }
}
