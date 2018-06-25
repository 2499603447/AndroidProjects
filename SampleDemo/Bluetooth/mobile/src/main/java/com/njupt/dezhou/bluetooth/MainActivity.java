package com.njupt.dezhou.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter bluetoothAdapter;
    TextView localAddress;
    TextView textView;
    Button send;
    Button find;
    private int REQUEST_ENABLE_BT = 1;
    private BluetoothChatUtil mBlthChatUtil;
    private Button stop;
    private static final String TAG = "MainActivity";

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothChatUtil.STATE_CONNECTED:
                    String deviceName = msg.getData().getString(BluetoothChatUtil.DEVICE_NAME);
                    textView.setText("已成功连接到设备" + deviceName);

                    break;
                case BluetoothChatUtil.STATAE_CONNECT_FAILURE:

                    Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothChatUtil.MESSAGE_DISCONNECTED:

                    textView.setText("与设备断开连接");
                    break;
                case BluetoothChatUtil.MESSAGE_READ: {
                    byte[] buf = msg.getData().getByteArray(BluetoothChatUtil.READ_MSG);
                    String str = new String(buf, 0, buf.length);
                    Toast.makeText(getApplicationContext(), "读成功" + str, Toast.LENGTH_SHORT).show();

                    localAddress.setText(localAddress.getText().toString() + "\n" + str);
                    break;
                }
                case BluetoothChatUtil.MESSAGE_WRITE: {
                    byte[] buf = (byte[]) msg.obj;
                    String str = new String(buf, 0, buf.length);
                    Toast.makeText(getApplicationContext(), "发送成功" + str, Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        localAddress = findViewById(R.id.localAddress);
        textView = findViewById(R.id.textView);
        send = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        find = findViewById(R.id.find);

        //蓝牙设备初始化
        initBluetooth();
        mBlthChatUtil = BluetoothChatUtil.getInstance(this);
        mBlthChatUtil.registerHandler(mHandler);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messagesend = "hello,this is server";
                if (null == messagesend || messagesend.length() == 0) {
                    return;
                }
                mBlthChatUtil.write(messagesend.getBytes());
            }
        });

        Visbility();

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBlthChatUtil.getState() != BluetoothChatUtil.STATE_CONNECTED) {
                    //Toast.makeText(this, "蓝牙未连接", Toast.LENGTH_SHORT).show();
                } else {
                    mBlthChatUtil.disconnect();
                }
            }
        });

        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //判断蓝牙是否开启
                if (!bluetoothAdapter.isEnabled()) {//蓝牙未开启
                    Intent enableIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                    //bluetoothAdapter.enable();//此方法直接开启蓝牙，不建议这样用。
                }
                if (bluetoothAdapter.isEnabled()) {
                    if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        Intent discoveryIntent = new Intent(
                                BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoveryIntent.putExtra(
                                BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                        startActivity(discoveryIntent);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "蓝牙未开启，请先开启蓝牙", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void Visbility() {
        //声明一个class类
        Class serviceManager = null;
        try {
            //得到这个class的类
            serviceManager = Class.forName("android.bluetooth.BluetoothAdapter");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //声明一个方法
        Method method = null;
        try {
            //得到指定的类中的方法
            method = serviceManager.getMethod("setDiscoverableTimeout", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            //调用这个方法
            method.invoke(serviceManager.newInstance(), 30);//根据测试，发现这一函数的参数无论传递什么值，都是永久可见的
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {//设备不支持蓝牙
            Toast.makeText(getApplicationContext(), "设备不支持蓝牙", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        //判断蓝牙是否开启
        if (!bluetoothAdapter.isEnabled()) {//蓝牙未开启
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            //bluetoothAdapter.enable();//此方法直接开启蓝牙，不建议这样用。
        }
        //设置蓝牙可见性
        if (bluetoothAdapter.isEnabled()) {
            if (bluetoothAdapter.getScanMode() !=
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                Intent discoverableIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(
                        BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
                startActivity(discoverableIntent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!bluetoothAdapter.isEnabled()) return;
        if (mBlthChatUtil != null) {
            // 只有国家是state_none，我们知道，我们还没有开始
            if (mBlthChatUtil.getState() == BluetoothChatUtil.STATE_NONE) {
                // 启动蓝牙聊天服务
                Log.e(TAG, "onResume: start listen");
                mBlthChatUtil.startListen();
            } else if (mBlthChatUtil.getState() == BluetoothChatUtil.STATE_CONNECTED) {
                BluetoothDevice device = mBlthChatUtil.getConnectedDevice();
                if (null != device && null != device.getName()) {
                    textView.setText("已成功连接到设备" + device.getName());
                } else {
                    textView.setText("已成功连接到设备");
                }
            }
        }
    }
}
