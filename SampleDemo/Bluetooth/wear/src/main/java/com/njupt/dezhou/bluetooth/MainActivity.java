package com.njupt.dezhou.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends WearableActivity {

    private TextView mTextView;
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Context mContext;

    private Button send;
    private Button find;

    private TextView text;
    private ProgressDialog mProgressDialog;
    private BluetoothChatUtil mBlthChatUtil;
    private static final String TAG = "MainActivity";
    private BluetoothDevice bindDevice;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothChatUtil.STATE_CONNECTED:
                    String deviceName = msg.getData().getString(BluetoothChatUtil.DEVICE_NAME);
                    //mBtConnectState.setText("已成功连接到设备" + deviceName);

                    break;
                case BluetoothChatUtil.STATAE_CONNECT_FAILURE:

                    Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothChatUtil.MESSAGE_DISCONNECTED:

                    //mBtConnectState.setText("与设备断开连接");
                    break;
                case BluetoothChatUtil.MESSAGE_READ: {
                    byte[] buf = msg.getData().getByteArray(BluetoothChatUtil.READ_MSG);
                    String str = new String(buf, 0, buf.length);
                    Toast.makeText(getApplicationContext(), "读成功" + str, Toast.LENGTH_SHORT).show();

                    text.setText(text.getText().toString() + "\n" + str);
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

        mContext = this;
        initView();
        initBluetooth();
        mBlthChatUtil = BluetoothChatUtil.getInstance(mContext);
        mBlthChatUtil.registerHandler(mHandler);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messagesend = "this is client";
                if (null == messagesend || messagesend.length() == 0) {
                    return;
                }
                mBlthChatUtil.write(messagesend.getBytes());
            }
        });
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //判断蓝牙是否开启
                if (!mBluetoothAdapter.isEnabled()) {//蓝牙未开启
                    Intent enableIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                    //mBluetoothAdapter.enable();此方法直接开启蓝牙，不建议这样用。
                }
                if (mBlthChatUtil.getState() == BluetoothChatUtil.STATE_CONNECTED) {
                    Toast.makeText(mContext, "蓝牙已连接", Toast.LENGTH_SHORT).show();
                } else {
                    discoveryDevices();
                }
            }
        });
    }

    private void initView() {
        send = (Button) findViewById(R.id.send);
        text = (TextView) findViewById(R.id.text);
        find = findViewById(R.id.find);
    }

    private void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {//设备不支持蓝牙
            Toast.makeText(getApplicationContext(), "设备不支持蓝牙",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        //判断蓝牙是否开启
        if (!mBluetoothAdapter.isEnabled()) {//蓝牙未开启
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            //mBluetoothAdapter.enable();此方法直接开启蓝牙，不建议这样用。
        }
        //注册广播接收者，监听扫描到的蓝牙设备
        IntentFilter filter = new IntentFilter();
        //发现设备
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mBluetoothReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBlthChatUtil != null) {
            if (mBlthChatUtil.getState() == BluetoothChatUtil.STATE_CONNECTED) {
                BluetoothDevice device = mBlthChatUtil.getConnectedDevice();
                if (null != device && null != device.getName()) {
                    text.setText("已成功连接到设备" + device.getName());
                } else {
                    text.setText("已成功连接到设备");
                }
            }
        }
    }

    private void discoveryDevices() {
        // 扫描蓝牙设备
        mBluetoothAdapter.startDiscovery();

    }

    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "mBluetoothReceiver action =" + action);
            getBtDeviceInfo();
            mBlthChatUtil.connect(bindDevice);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //获取蓝牙设备
                BluetoothDevice scanDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (scanDevice == null || scanDevice.getName() == null) return;
                Log.e(TAG, "name=" + scanDevice.getName() + "address=" + scanDevice.getAddress());
                //蓝牙设备名称
                String name = scanDevice.getName();
                if (name != null) {
                    mBlthChatUtil.connect(scanDevice);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            }
        }
    };

    @SuppressWarnings("unused")
    private void getBtDeviceInfo() {
        //获取本机蓝牙名称
        String name = mBluetoothAdapter.getName();
        //获取本机蓝牙地址
        String address = mBluetoothAdapter.getAddress();
        Log.e(TAG, "bluetooth name =" + name + " address =" + address);
        //获取已配对蓝牙设备
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        Log.d(TAG, "bonded device size =" + devices.size());
        for (BluetoothDevice bonddevice : devices) {
            Log.d(TAG, "bonded device name =" + bonddevice.getName() +
                    " address" + bonddevice.getAddress());
            bindDevice = bonddevice;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        mBlthChatUtil = null;
        unregisterReceiver(mBluetoothReceiver);
    }
}
