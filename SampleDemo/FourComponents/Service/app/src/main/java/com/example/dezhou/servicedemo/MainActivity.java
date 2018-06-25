package com.example.dezhou.servicedemo;

/**
*created by dezhouzhang at 2018-05-31
*/

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.dezhou.servicedemo.UnBindService.UnBindService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e(TAG,"Thread ID：" + Thread.currentThread().getId());
        Log.e(TAG, "onCreate: before start service" );
        /**
         * 非绑定方式启动Service
         */
        //连续启动Service
        Intent intentUnBind1 = new Intent(this, UnBindService.class);
        startService(intentUnBind1);
        Intent intentUnBind2 = new Intent(this, UnBindService.class);
        startService(intentUnBind2);
        Intent intentUnBind3 = new Intent(this, UnBindService.class);
        startService(intentUnBind3);

        //停止Service
        Intent intentUnBind4 = new Intent(this, UnBindService.class);
        stopService(intentUnBind4);

        //再次启动Service
        Intent intentUnBind5 = new Intent(this, UnBindService.class);
        startService(intentUnBind5);

        Log.i("Kathy", "after StartService");

        //绑定方式启动Service
    }
}
