package com.example.dezhou.servicedemo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.dezhou.servicedemo.BindService.BindService;
import com.example.dezhou.servicedemo.BindService.*;

public class ActivityA extends AppCompatActivity implements View.OnClickListener{
    private BindService service = null;
    private boolean isBind = false;
    private IMyBinder myBinder;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            isBind = true;
            myBinder = (IMyBinder) binder;
            Log.e("Kathy", "ActivityA - onServiceConnected");
            int num = ((IMyBinder) binder).invokeMethodInService();
            Log.e("Kathy", "ActivityA - getRandomNumber = " + num);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBind = false;
            Log.e("Kathy", "ActivityA - onServiceDisconnected");
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a);
        Log.e("Kathy", "ActivityA - onCreate - Thread = " + Thread.currentThread().getName());

        findViewById(R.id.btnA_BindService).setOnClickListener(this);
        findViewById(R.id.btnA_UnbindService).setOnClickListener(this);
        findViewById(R.id.btnA_StartActivityB).setOnClickListener(this);
        findViewById(R.id.btnA_Finish).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnA_BindService) {
            //单击了“bindService”按钮
            Intent intent = new Intent(this, BindService.class);
            intent.putExtra("from", "ActivityA");
            Log.e("Kathy", "----------------------------------------------------------------------");
            Log.e("Kathy", "ActivityA 执行 bindService");
            bindService(intent, conn, BIND_AUTO_CREATE);
        } else if (v.getId() == R.id.btnA_UnbindService) {
            //单击了“unbindService”按钮
            if (isBind) {
                Log.e("Kathy",
                        "----------------------------------------------------------------------");
                Log.e("Kathy", "ActivityA 执行 unbindService");
                unbindService(conn);
            }
        } else if (v.getId() == R.id.btnA_StartActivityB) {
            //单击了“start ActivityB”按钮
            Intent intent = new Intent(this, ActivityB.class);
            Log.e("Kathy",
                    "----------------------------------------------------------------------");
            Log.e("Kathy", "ActivityA 启动 ActivityB");
            startActivity(intent);
        } else if (v.getId() == R.id.btnA_Finish) {
            //单击了“Finish”按钮
            Log.e("Kathy",
                    "----------------------------------------------------------------------");
            Log.e("Kathy", "ActivityA 执行 finish");
            this.finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("Kathy", "ActivityA - onDestroy");
    }
}
