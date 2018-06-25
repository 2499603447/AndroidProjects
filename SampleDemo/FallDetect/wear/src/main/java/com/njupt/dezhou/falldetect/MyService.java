package com.njupt.dezhou.falldetect;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by dezhou on 2017-10-12.
 */

public class MyService extends Service implements SensorEventListener {

    private static final String TAG = "TEST_2";
    public static final float STANDARD_GRAVITY = 9.80665f;  // 重力加速度大小

    private SensorManager sManager;
    private Sensor aSensor;
    //private Sensor gSensor;

    @Override
    public void onCreate() {
        sManager 	= (SensorManager) getSystemService(SENSOR_SERVICE);
        aSensor 	= sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //gSensor 	= sManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sManager.registerListener(this, aSensor, SensorManager.SENSOR_DELAY_GAME);

        Toast.makeText(this, "My Service created", Toast.LENGTH_LONG).show();
        Log.i(TAG, "onCreate");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this, "My Service Start", Toast.LENGTH_LONG).show();
        Log.i(TAG, "onStart");
        //super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        //super.onDestroy();
        Toast.makeText(this, "My Service Stoped", Toast.LENGTH_LONG).show();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        //System.out.println("进来了2！");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "value size: " + event.values.length);
        float xValue = event.values[0];
        float yValue = event.values[1];
        float zValue = event.values[2];
        Log.d(TAG, "" + xValue + "," + yValue + "," + zValue + "," + event.timestamp);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}

