package com.runshen.slamware.slamware_sdk;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.runshen.slamware.slamware_sdk.thread.WorkerThread;
import com.runshen.slamware.slamware_sdk.utils.SlamwareHelper;
import com.slamtec.slamware.action.MoveDirection;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * SlamwareSdkPlugin
 */
public class SlamwareSdkPlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private WorkerThread mWorkerThread;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        SlamwareHelper.getInstance().init(flutterPluginBinding.getApplicationContext());
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "slamware_sdk");
        channel.setMethodCallHandler(this);
        initWorkerThread();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("connectAgv")) {
            String ip = call.argument("ip");
            boolean success = SlamwareHelper.getInstance().connect(ip, 1445);
            result.success(success);
        } else if (call.method.equals("connect")) {
            String ip = call.argument("ip");
            int port = Integer.valueOf(call.argument("port"));
            boolean success = SlamwareHelper.getInstance().connect(ip, port);
            result.success(success);
        } else if (call.method.equals("disconnect")) {
            SlamwareHelper.getInstance().disconnect();
        } else if (call.method.equals("isConnection")) {
            boolean success = SlamwareHelper.getInstance().isConnection();
            result.success(success);
        } else if (call.method.equals("action")) {
            String mode = call.argument("mode");
            if (!TextUtils.isEmpty(mode)) {
                getWorkerThread().cancel(0);
                switch (mode) {
                    case "moveForward":
                        getWorkerThread().moveBy(MoveDirection.FORWARD);
                        break;
                    case "moveBackward":
                        getWorkerThread().moveBy(MoveDirection.BACKWARD);
                        break;
                    case "turnLeft":
                        getWorkerThread().moveBy(MoveDirection.TURN_LEFT);
                        break;
                    case "turnRight":
                        getWorkerThread().moveBy(MoveDirection.TURN_RIGHT);
                        break;
                    case "moveTo":
                        String tarX = call.argument("tarX");
                        String tarY = call.argument("tarY");
                        String tarZ = call.argument("tarZ");
                        if (TextUtils.isEmpty(tarZ)) {
                            tarZ = "0";
                        }
                        if (tarX != null && tarY != null) {
                            getWorkerThread().moveTo(tarX, tarY, tarZ);
                        }
                        break;
                    case "backHome":
                        getWorkerThread().goHome();
                        break;
                    case "cancel":
                        getWorkerThread().cancel(0);
                        break;
                    default:
                        break;
                }
            }
        } else if (call.method.equals("uploadMap")) {
            String mapPath = call.argument("path");
            SlamwareHelper.getInstance().uploadMap(mapPath);
        } else if (call.method.equals("getInfo")) {
            String str = SlamwareHelper.getInstance().getPos();
            result.success(str);
        } else if (call.method.equals("threadInfo")) {
            Log.i("Thread", "back home: " + getWorkerThread().isAlive());
        } else if (call.method.equals("setSpeed")) {
            String value = call.argument("value");
            SlamwareHelper.getInstance().setSpeed(value);
        } else if (call.method.equals("setAngularSpeed")) {
            String value = call.argument("value");
            SlamwareHelper.getInstance().setAngularSpeed(value);
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    public synchronized void initWorkerThread() {
        if (mWorkerThread == null) {
            mWorkerThread = new WorkerThread();
            mWorkerThread.start();
            mWorkerThread.waitForReady();
        }
    }

    public synchronized WorkerThread getWorkerThread() {
        return mWorkerThread;
    }

}
