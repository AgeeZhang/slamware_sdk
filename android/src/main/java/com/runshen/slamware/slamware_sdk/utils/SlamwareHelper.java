package com.runshen.slamware.slamware_sdk.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.slamtec.slamware.AbstractSlamwarePlatform;
import com.slamtec.slamware.action.ActionStatus;
import com.slamtec.slamware.action.IMoveAction;
import com.slamtec.slamware.action.MoveDirection;
import com.slamtec.slamware.discovery.DeviceManager;
import com.slamtec.slamware.robot.CompositeMap;
import com.slamtec.slamware.robot.DockingStatus;
import com.slamtec.slamware.robot.HealthInfo;
import com.slamtec.slamware.robot.Location;
import com.slamtec.slamware.robot.MoveOption;
import com.slamtec.slamware.robot.Pose;
import com.slamtec.slamware.robot.PowerStatus;
import com.slamtec.slamware.robot.Rotation;
import com.slamtec.slamware.robot.SlamcoreShutdownParam;
import com.slamtec.slamware.robot.SystemParameters;
import com.slamtec.slamware.sdp.CompositeMapHelper;

import org.json.JSONObject;

import java.util.Iterator;

public class SlamwareHelper {

    private final static String TAG = "SlamwareHelper";
    private volatile static SlamwareHelper instance;
    private static AbstractSlamwarePlatform platform;
    private static Context context;
    private static String IP;
    private static int PORT;

    private SlamwareHelper() {

    }

    /**
     * 单例
     *
     * @return
     */
    public static SlamwareHelper getInstance() {
        if (instance == null) {
            instance = new SlamwareHelper();
        }
        return instance;
    }

    private void showMessage(String message) {
        Toast.makeText(this.context, message, Toast.LENGTH_LONG).show();
    }

    public void init(Context context) {
        this.context = context;
        this.IP = "127.0.0.1";
        this.PORT = 1445;
    }

    /**
     * 获取platform （只实例化一次
     *
     * @return
     */
    private AbstractSlamwarePlatform getPlatform() {
        if (platform == null)
            platform = DeviceManager.connect(IP, 1445);
        return platform;
    }

    /**
     * 初始化连接
     *
     * @param ip
     * @param port
     */
    public boolean connect(String ip, int port) {
        this.IP = ip;
        this.PORT = port;
        try {
            Log.i(TAG, "IP:" + ip + "____PORT:" + port);
            platform = DeviceManager.connect(ip, port);
            if (platform == null) {
                showMessage("连接失败，请输入正确的IP地址");
                Log.e(TAG, "连接失败，请输入正确的IP地址");
                return false;
            }
            Log.e(TAG, "连接成功");
            showMessage("连接成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("连接失败，请输入正确的IP地址");
            Log.e(TAG, "连接失败，请输入正确的IP地址");
        }
        return false;
    }

    /**
     * 断开连接
     */
    public boolean disconnect() {
        if (platform != null) {
            platform.disconnect();
            platform = null;
            this.IP = "";
            this.PORT = 0;
            return true;
        }
        return false;
    }

    /**
     * 判断底盘连接是否正常
     *
     * @return
     */
    public boolean isConnection() {
        boolean flag = true;
        if (platform == null) {
            Log.e(TAG, "底盘未连接");
            return false;
        }
        try {
            HealthInfo healthInfo = getPlatform().getRobotHealth();
            Iterator iterator = healthInfo.getErrors().iterator();
            while (iterator.hasNext()) {
                HealthInfo.BaseError baseError = (HealthInfo.BaseError) iterator.next();
                if (baseError.getErrorComponent() == HealthInfo.BaseError.BaseComponentErrorTypeSystemCtrlBusDisconnected) {
                    flag = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return flag;
    }

    /**
     * 获取设备信息
     *
     * @return
     */
    public String getPos() {
        try {
            Pose pose = getPlatform().getPose();
            String location_x = Float.toString(pose.getX());
            String location_y = Float.toString(pose.getY());
            String location_raw = Float.toString(pose.getYaw());
            int battery = getPlatform().getBatteryPercentage();
            String battery_percentage = Integer.toString(battery);
            PowerStatus powerStatus = getPlatform().getPowerStatus();
            boolean isCharging = powerStatus.isCharging();
            DockingStatus dockingStatus = powerStatus.getDockingStatus();
            String isDocking;
            switch (dockingStatus) {
                case OnDock:
                    isDocking = "已回到桩";
                    break;
                case NotOnDock:
                    isDocking = "不在桩上";
                    break;
                case Unknown:
                    isDocking = "未知状态";
                    break;
                default:
                    isDocking = "状态错误";
                    break;
            }
            JSONObject data = new JSONObject();
            data.put("x", location_x);
            data.put("y", location_y);
            data.put("raw", location_raw);
            data.put("battery", battery_percentage);
            data.put("isCharging", isCharging);
            data.put("isDocking", isDocking);
            return data.toString();
        } catch (Exception e) {
            Log.i(TAG, "获取信息出错");
            return "";
        }
    }

    /**
     * 上传虚拟地图
     *
     * @param mapPath
     */
    public boolean uploadMap(String mapPath) {
        try {
            Pose pose = getPlatform().getPose();
            CompositeMapHelper compositeMapHelper = new CompositeMapHelper();
            CompositeMap localCompositeMap = compositeMapHelper.loadFile(mapPath);
            getPlatform().setCompositeMap(localCompositeMap, pose);
            CompositeMap map = getPlatform().getCompositeMap();
            return map != null;
        } catch (Exception e) {
            Log.e(TAG, "上传地图失败");
            return false;
        }
    }

    /**
     * 移动到指定位置
     *
     * @param tarX
     * @param tarY
     * @param tarZ
     */
    public void moveTo(String tarX, String tarY, String tarZ) {
        try {
            IMoveAction moveAction;
            MoveOption moveOption = new MoveOption();
            moveOption.setPrecise(true);
            moveOption.setMilestone(false);
            Location target = new Location(Float.parseFloat(tarX), Float.parseFloat(tarY), 0);
            moveAction = getPlatform().moveTo(target, moveOption, 0);
            Log.i(TAG, "moveTo move start");
            moveAction.waitUntilDone();
            if (moveAction.getStatus() == ActionStatus.FINISHED) {
                Log.i(TAG, "moveTo move done");
            }
            moveAction = getPlatform().rotateTo(new Rotation(Float.parseFloat(tarZ), 0, 0));
            moveAction.waitUntilDone();
            if (moveAction.getStatus() == ActionStatus.FINISHED) {
                Log.i(TAG, "moveTo rotate done");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "移动失败");
        }
    }

    /**
     * 朝着指定方向移动
     *
     * @param direction
     */
    public void moveBy(MoveDirection direction) {
        try {
            IMoveAction moveAction = getPlatform().moveBy(direction);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "前行失败");
        }
    }

    /**
     * 回到充电桩
     */
    public void goHome() {
        try {
            IMoveAction moveAction = getPlatform().goHome();
            moveAction.waitUntilDone();
            if (moveAction.getStatus() == ActionStatus.FINISHED) {
                Log.i(TAG, "Home Sweet Home");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "回桩失败");
        }
    }

    /**
     * 取消当前事务
     */
    public void cancel() {
        try {
            getPlatform().getCurrentAction().cancel();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "取消事务异常");
        }
    }

    /**
     * 设置最大线速度 最大值1 精度6位
     *
     * @param value
     */
    public boolean setSpeed(String value) {
        try {
            String oldVal = getPlatform().getSystemParameter(SystemParameters.SYSPARAM_ROBOT_SPEED);
            Log.i(TAG, "原最大线速度：" + oldVal);
            getPlatform().setSystemParameter(SystemParameters.SYSPARAM_ROBOT_SPEED, value);
            String newVal = getPlatform().getSystemParameter(SystemParameters.SYSPARAM_ROBOT_SPEED);
            Log.i(TAG, "更新后最大线速度：" + newVal);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 设置最大角速度 最大值2 精度6位
     *
     * @param value
     */
    public boolean setAngularSpeed(String value) {
        try {
            String oldVal = getPlatform().getSystemParameter(SystemParameters.SYSPARAM_ROBOT_ANGULAR_SPEED);
            Log.i(TAG, "原最大角速度：" + oldVal);
            getPlatform().setSystemParameter(SystemParameters.SYSPARAM_ROBOT_ANGULAR_SPEED, value);
            String newVal = getPlatform().getSystemParameter(SystemParameters.SYSPARAM_ROBOT_ANGULAR_SPEED);
            Log.i(TAG, "更新后最大角速度：" + newVal);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 设置关机时间
     *
     * @param restartTimeIntervalMinute  重启时间
     * @param shutdownTimeIntervalMinute 关机时间
     */
    public boolean setShutdownTimeIntervalMinute(int restartTimeIntervalMinute, int shutdownTimeIntervalMinute) {
        try {
            SlamcoreShutdownParam slamcoreShutdownParam = new SlamcoreShutdownParam();
            slamcoreShutdownParam.setShutdownTimeIntervalMinute(shutdownTimeIntervalMinute);
            slamcoreShutdownParam.setRestartTimeIntervalMinute(restartTimeIntervalMinute);
            getPlatform().shutdownSlamcore(slamcoreShutdownParam);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 设置机器人当前位置
     *
     * @param x X值
     * @param y Y值
     * @param z Z值
     */
    public boolean setLocation(float x, float y, float z) {
        try {
            Pose pose = new Pose();
            pose.setLocation(new Location(x, y, 0f));
            pose.setRotation(new Rotation(z, 0f, 0f));
            getPlatform().setPose(pose);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
