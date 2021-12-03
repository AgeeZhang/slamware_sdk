import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:slamware_sdk/slamware_sdk.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  bool _connectState=false;
  String _ip = '127.0.0.1';
  String _port = '1445';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    bool connectState;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await SlamwareSdk.platformVersion ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: [
            Text('Running on: $_platformVersion\n'),
            Text('连接状态: $_connectState\n'),
            TextField(
                style: const TextStyle(color: Color(0xFFA7ABBB),fontSize: 15),
                keyboardType: TextInputType.numberWithOptions(signed: true),
                controller: TextEditingController.fromValue(TextEditingValue(
                    text: _ip,
                    selection: TextSelection.fromPosition(TextPosition(
                        affinity: TextAffinity.downstream,
                        offset: _ip.length)
                    ))
                ),
                decoration: InputDecoration(
                  counterText: '',
                  filled: true,
                  fillColor: Color(0xFF1A1A1A),
                  hintStyle: const TextStyle(color: Color(0xFFA7ABBB),fontSize: 15),
                  hintText: '请输入IP',
                  contentPadding: EdgeInsets.symmetric(horizontal: 15,vertical: 10),
                  enabledBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(6),borderSide: BorderSide.none),
                  focusedBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(6),borderSide: BorderSide.none),
                  suffixIcon: Container(alignment: Alignment.centerRight,child: Text('XBIT',style: const TextStyle(color: Color(0xFFA7ABBB),fontSize: 15),),margin: EdgeInsets.only(right: 15),),
                  suffixIconConstraints: BoxConstraints(maxWidth: 80)
                ),
                onChanged: (v){
                  _ip = v;
                  setState(() { });
                },
            ),
            TextField(
                style: const TextStyle(color: Color(0xFFA7ABBB),fontSize: 15),
                keyboardType: TextInputType.numberWithOptions(signed: true),
                controller: TextEditingController.fromValue(TextEditingValue(
                    text: _port,
                    selection: TextSelection.fromPosition(TextPosition(
                        affinity: TextAffinity.downstream,
                        offset: _port.length)
                    ))
                ),
                decoration: InputDecoration(
                  counterText: '',
                  filled: true,
                  fillColor: Color(0xFF1A1A1A),
                  hintStyle: const TextStyle(color: Color(0xFFA7ABBB),fontSize: 15),
                  hintText: '请输入端口',
                  contentPadding: EdgeInsets.symmetric(horizontal: 15,vertical: 10),
                  enabledBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(6),borderSide: BorderSide.none),
                  focusedBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(6),borderSide: BorderSide.none),
                  suffixIcon: Container(alignment: Alignment.centerRight,child: Text('XBIT',style: const TextStyle(color: Color(0xFFA7ABBB),fontSize: 15),),margin: EdgeInsets.only(right: 15),),
                  suffixIconConstraints: BoxConstraints(maxWidth: 80)
                ),
                onChanged: (v){
                  _port = v;
                  setState(() { });
                },),
            MaterialButton(
              color: Colors.blue,
              textColor: Colors.white,
              child: new Text('连接'),
              onPressed: (){
                 _connectState = SlamwareSdk.connect(_ip, _port) is bool;
              }),
              MaterialButton(
              color: Colors.blue,
              textColor: Colors.white,
              child: new Text('断开连接'),
              onPressed: (){
                  SlamwareSdk.disconnect;
              }),
              MaterialButton(
              color: Colors.blue,
              textColor: Colors.white,
              child: new Text('查看连接状态'),
              onPressed: (){
                _connectState =  SlamwareSdk.isConnection is bool;
              }),
              MaterialButton(
              color: Colors.blue,
              textColor: Colors.white,
              child: new Text('回窝'),
              onPressed: (){
                SlamwareSdk.getBackHome;
              }),
              MaterialButton(
              color: Colors.blue,
              textColor: Colors.white,
              child: new Text('取消操作'),
              onPressed: (){
                SlamwareSdk.actionCancel;
              }),
            ],
        ),
      ),
    );
  }
}
