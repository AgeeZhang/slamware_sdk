package com.runshen.slamware.slamware_sdk.thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.runshen.slamware.slamware_sdk.utils.SlamwareHelper;
import com.slamtec.slamware.action.MoveDirection;

import java.util.logging.Logger;

public class WorkerThread extends Thread {

    private final static String TAG = "WorkerThread";

    private static final int ACTION_CANCEL = 0X1010; // 取消操作
    private static final int ACTION_MOVE_BY = 0X2010; //
    private static final int ACTION_MOVE_TO = 0X2014;  //
    private static final int ACTION_MOVE_BACK_HOME = 0X2015; //

    private static final class WorkerThreadHandler extends Handler {

        private WorkerThread mWorkerThread;

        WorkerThreadHandler(WorkerThread thread) {
            this.mWorkerThread = thread;
        }

        public void release() {
            mWorkerThread = null;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (this.mWorkerThread == null) {
                Log.w(TAG, "handler is already released! " + msg.what);
                return;
            }
            switch (msg.what) {
                case ACTION_MOVE_BY:
                    MoveDirection direction = (MoveDirection) msg.obj;
                    mWorkerThread.moveBy(direction);
                    break;
                case ACTION_MOVE_TO:
                    String[] data = (String[])msg.obj;
                    mWorkerThread.moveTo(data[0],data[1],data[2]);
                    break;
                case ACTION_MOVE_BACK_HOME:
                    mWorkerThread.goHome();
                    break;
                case ACTION_CANCEL:
                    mWorkerThread.cancel();
                    break;
            }
        }
    }

    private WorkerThreadHandler mWorkerHandler;

    private boolean mReady;
    private boolean isRun = false;

    public final void waitForReady() {
        while (!mReady) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "wait for " + WorkerThread.class.getSimpleName());
        }
    }

    @Override
    public void run() {
        Log.i(TAG,"start to run");
        Looper.prepare();

        mWorkerHandler = new WorkerThreadHandler(this);

        mReady = true;

        Looper.loop();
    }

    public final void moveBy(MoveDirection direction){
        if (Thread.currentThread() != this) {
            Log.w(TAG,"moveBy() - worker thread asynchronously " + direction);
            Message envelop = new Message();
            envelop.what = ACTION_MOVE_BY;
            envelop.obj = direction;
            mWorkerHandler.sendMessage(envelop);
            return;
        }
        while (isRun) {
            SlamwareHelper.getInstance().moveBy(direction);
        }
    }

    public final void moveTo(String tarX,String tarY,String tarZ){
        if (Thread.currentThread() != this) {
            Log.w(TAG,"moveTo() - worker thread asynchronously " + tarX + " " + tarY + " " + tarZ);
            Message envelop = new Message();
            envelop.what = ACTION_MOVE_TO;
            envelop.obj = new String[]{tarX, tarY,tarZ};
            mWorkerHandler.sendMessage(envelop);
            return;
        }
        SlamwareHelper.getInstance().moveTo(tarX,tarY,tarZ);
    }

    public final void goHome(){
        if (Thread.currentThread() != this) {
            Log.w(TAG,"goHome() - worker thread asynchronously ");
            Message envelop = new Message();
            envelop.what = ACTION_MOVE_BACK_HOME;
            mWorkerHandler.sendMessage(envelop);
            return;
        }
        SlamwareHelper.getInstance().goHome();
    }

    public final void cancel(){
        if (Thread.currentThread() != this) {
            Log.w(TAG,"cancel() - worker thread asynchronously ");
            Message envelop = new Message();
            envelop.what = ACTION_CANCEL;
            mWorkerHandler.sendMessage(envelop);
            return;
        }
        isRun = false;
        SlamwareHelper.getInstance().cancel();
    }




}
