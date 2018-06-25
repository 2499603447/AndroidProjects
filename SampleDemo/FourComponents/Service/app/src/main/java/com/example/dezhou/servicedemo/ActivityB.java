package com.example.dezhou.servicedemo;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.dezhou.servicedemo.BindService.BindService;
import com.example.dezhou.servicedemo.BindService.IMyBinder;

public class ActivityB extends AppCompatActivity implements View.OnClickListener{
    private Service service = null;
    private boolean isBind = false;
    private IMyBinder myBinder;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            isBind = true;
            myBinder = (IMyBinder) binder;
            Log.e("Kathy", "ActivityB - onServiceConnected");
            int num = ((IMyBinder) binder).invokeMethodInService();
            Log.e("Kathy", "ActivityB - getRandomNumber = " + num);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBind = false;
            Log.e("Kathy", "ActivityB - onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b);

        findViewById(R.id.btnB_BindService).setOnClickListener(this);
        findViewById(R.id.btnB_UnbindService).setOnClickListener(this);
        findViewById(R.id.btnB_Finish).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnB_BindService){
            //单击了“bindService”按钮
            Intent intent = new Intent(this, BindService.class);
            intent.putExtra("from", "ActivityB");
            Log.e("Kathy", "----------------------------------------------------------------------");
            Log.e("Kathy", "ActivityB 执行 bindService");
            bindService(intent, conn, BIND_AUTO_CREATE);
        }else if(v.getId() == R.id.btnB_UnbindService){
            //单击了“unbindService”按钮
            if(isBind){
                Log.e("Kathy", "----------------------------------------------------------------------");
                Log.e("Kathy", "ActivityB 执行 unbindService");
                unbindService(conn);
            }
        }else if(v.getId() == R.id.btnB_Finish){
            //单击了“Finish”按钮
            Log.e("Kathy", "----------------------------------------------------------------------");
            Log.e("Kathy", "ActivityB 执行 finish");
            this.finish();
        }
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.e("Kathy", "ActivityB - onDestroy");
    }
}
