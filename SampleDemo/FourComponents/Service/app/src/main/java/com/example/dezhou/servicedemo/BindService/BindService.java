package com.example.dezhou.servicedemo.BindService;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;

/**
 * create by dezhouzhang at 2018-05-31
 * bindService启动服务特点：
 1. bindService启动的服务和调用者之间是典型的client-server模式。调用者是client，service则是server端。
    service只有一个，但绑定到service上面的client可以有一个或很多个。这里所提到的client指的是组件，比如某个Activity。
 2. client可以通过IBinder接口获取Service实例，从而实现在client端直接调用Service中的方法以实现灵活交互，
    这在通过startService方法启动中是无法实现的。
 3. bindService启动服务的生命周期与其绑定的client息息相关。当client销毁时，client会自动与Service解除绑定。
    当然，client也可以明确调用Context的unbindService()方法与Service解除绑定。
    当没有任何client与Service绑定时，Service会自行销毁。
 */
public class BindService extends Service{

    private static final String TAG = "BindService";

    //随机数生成，用于产生一个随机数
    private final Random generator = new Random();

    /**
     * 该类用于在onBind方法执行后返回的对象
     * 该对象对外提供了该服务里的方法
     */
    private class MyBinder extends Binder implements IMyBinder{
        @Override
        public int invokeMethodInService() {
            return methodInMyService();
        }
    }

    //本服务里的方法
    private int methodInMyService()
    {
        Toast.makeText(getApplicationContext(),"BindService 服务里面的方法正在执行...",Toast.LENGTH_LONG).show();
        return generator.nextInt();
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "BindService onCreate Thread: " + Thread.currentThread().getName() );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "BindService - onStartCommand - startId = " + startId + ", Thread = " + Thread.currentThread().getName() );
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "BindService - onBind - Thread = " + Thread.currentThread().getName() );
        return new MyBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "BindService - onUnbind - from = " + intent.getStringExtra("from"));
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "BindService - onDestroy - Thread = " + Thread.currentThread().getName() );
        super.onDestroy();
    }
}
