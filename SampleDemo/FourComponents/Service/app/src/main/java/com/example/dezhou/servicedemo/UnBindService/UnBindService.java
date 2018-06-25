package com.example.dezhou.servicedemo.UnBindService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * create by dezhouzhang at 2018-05-31
 * 通过startService启动后，service会一直无限期运行下去，
 * 只有外部调用了stopService()或stopSelf()方法时，
 * 该Service才会停止运行并销毁。
 */
public class UnBindService extends Service {
    private static final String TAG = "UnBindService";

    /**
    1.如果Service没有被创建，调用startService()后会执行onCreate()回掉；
    2.如果service已经处于运行中，调用startService()不会执行onCreate()方法。
    也就是说，onCreate()只会在第一次创建service的时候调用，多次执行startService()
    不会重复调用onCreate(),此方法适合完成一些初始化工作
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "UnBindService onCreate - Thread ID = " + Thread.currentThread().getId());
    }

    /**
    如果多次执行了Context的startService()方法，那么Service的onStartCommand()
    方法也会相应的多次调用。onStartCommand()方法很重要，我们在该方法中根据传入
    的Intent参数进行实际的操作，比如会在此处创建一个线程用于下载数据或播放音乐等。
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "UnBindService onStartCommand - Thread ID = " + startId + ", Thread ID = " + Thread.currentThread().getId());
        return super.onStartCommand(intent, flags, startId);
    }

    /**
    Service中的onBind()方法是抽象方法，Service类本身就是抽象类，所以onBind()
    方法是必须重写的，即使我们用不到。
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "UnBindService onBind - Thread ID = " + Thread.currentThread().getId());
        return null;
    }

    /**
    在销毁的时候会执行Service该方法。
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "UnBindService onDestroy - Thread ID = " + Thread.currentThread().getId());
    }
}
