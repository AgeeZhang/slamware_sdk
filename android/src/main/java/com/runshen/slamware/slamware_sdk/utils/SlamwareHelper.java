package com.runshen.slamware.slamware_sdk.utils;

import android.util.Log;

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
import com.slamtec.slamware.sdp.CompositeMapHelper;

import org.json.JSONObject;

import java.util.Iterator;

public class SlamwareHelper {

    private final static String TAG ="SlamwareHelper";
    private volatile static SlamwareHelper instance;
    private static AbstractSlamwarePlatform platform;
    private static String IP;
    private static Integer PORT;

    private SlamwareHelper() {

    }

    /**
     * 单例
     * @return
     */
    public static SlamwareHelper getInstance() {
        if (instance == null) {
            instance = new SlamwareHelper();
        }
        return instance;
    }

    /**
     * 获取platform （只实例化一次
     * @return
     */
    private AbstractSlamwarePlatform getPlatform(){
        if(platform == null)
            platform = DeviceManager.connect(IP,PORT);
        return platform;
    }

    /**
     * 初始化连接
     * @param ip
     * @param port
     */
    public boolean connect(String ip,int port){
        this.IP = ip;
        this.PORT = port;
        platform = DeviceManager.connect(ip, port);
        if(platform != null){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 断开连接
     */
    public void disconnect(){
        if(platform!=null) {
            platform.disconnect();
            platform = null;
            this.IP = "";
            this.PORT = 0;
        }
    }

    /**
     * 判断底盘连接是否正常
     * @return
     */
    public boolean isConnection(){
        boolean flag = true;
        if(platform == null ) {
            return false;
        }
        try {
            HealthInfo healthInfo = getPlatform().getRobotHealth();
            Iterator iterator = healthInfo.getErrors().iterator();
            while (iterator.hasNext()){
                HealthInfo.BaseError baseError = (HealthInfo.BaseError) iterator.next();
                if(baseError.getErrorComponent() == HealthInfo.BaseError.BaseComponentErrorTypeSystemCtrlBusDisconnected){
                    flag = false;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG,e.getMessage());
        }
        return flag;
    }

    /**
     * 获取设备信息
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
            data.put("isCharging",isCharging);
            data.put("isDocking",isDocking);
            return data.toString();
        } catch (Exception e) {
            Log.i(TAG, "获取信息出错");
            return "";
        }
    }

    /**
     * 上传虚拟地图
     * @param mapPath
     */
    public void uploadMap(String mapPath){
        try {
            Pose pose = getPlatform().getPose();
            CompositeMapHelper compositeMapHelper = new CompositeMapHelper();
            CompositeMap localCompositeMap = compositeMapHelper.loadFile(mapPath);
            getPlatform().setCompositeMap(localCompositeMap, pose);
        }catch (Exception e){
            Log.e(TAG,"");
        }
    }

    /**
     * 移动到指定位置
     * @param tarX
     * @param tarY
     * @param tarZ
     */
    public void moveTo(String tarX,String tarY,String tarZ){
        try {
            IMoveAction moveAction;
            MoveOption moveOption = new MoveOption();
            moveOption.setPrecise(true);
            moveOption.setMilestone(false);
            Location target = new Location(Float.parseFloat(tarX), Float.parseFloat(tarY), 0);
            moveAction = getPlatform().moveTo(target, moveOption, 0);
            Log.i("moveTo", "move start");
            moveAction.waitUntilDone();
            if (moveAction.getStatus() == ActionStatus.FINISHED) {
                Log.i("moveTo", "move done");
            }
            moveAction = getPlatform().rotateTo(new Rotation(Float.parseFloat(tarZ), 0, 0));
            moveAction.waitUntilDone();
            if (moveAction.getStatus() == ActionStatus.FINISHED) {
                Log.i("moveTo", "rotate done");
            }
        } catch (Exception e) {
            Log.i("Error", e.toString());
        }
    }

    /**
     * 朝着指定方向移动
     * @param direction
     */
    public void moveBy(MoveDirection direction){
        try {
            IMoveAction moveAction = getPlatform().moveBy(direction);
        }catch (Exception e){
            Log.e(TAG,"");
        }
    }

    /**
     * 回到充电桩
     */
    public void goHome(){
        try {
            IMoveAction moveAction = getPlatform().goHome();
            moveAction.waitUntilDone();
            if (moveAction.getStatus() == ActionStatus.FINISHED) {
                Log.i(TAG, "Home Sweet Home");
            }
        }catch (Exception e){
            Log.e(TAG,"");
        }
    }

    public void cancel(){
        try{
            getPlatform().getCurrentAction().cancel();
        }catch (Exception e){
            Log.e(TAG,"");
        }
    }


}
