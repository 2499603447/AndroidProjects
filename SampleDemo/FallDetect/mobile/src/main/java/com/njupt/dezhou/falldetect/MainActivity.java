package com.njupt.dezhou.falldetect;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main Activity";

    private BluetoothAdapter bluetoothAdapter;
    String phoneNum = "";
    String messageHeader = "你的亲人/朋友:";
    String messageName = "";
    String messageBody = "";
    String messageTail = ",自己不能够站起，请及时前往查看，谢谢！";
    private static Vibrator sVibrator;
    private AlarmView mAlarmView;

    private boolean start_alarm = false;
    ArrayList<String> list;
    SmsManager manager;

    private int REQUEST_ENABLE_BT = 1;
    private BluetoothChatUtil mBlthChatUtil;
    public Toolbar toolbar;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothChatUtil.STATE_CONNECTED:
                    String deviceName = msg.getData().getString(BluetoothChatUtil.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "已成功连接到设备" + deviceName, Toast.LENGTH_SHORT).show();

                    break;
                case BluetoothChatUtil.STATAE_CONNECT_FAILURE:

                    Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothChatUtil.MESSAGE_DISCONNECTED:
                    Toast.makeText(getApplicationContext(), "与设备断开连接", Toast.LENGTH_SHORT).show();

                    break;
                case BluetoothChatUtil.MESSAGE_READ: {
                    byte[] buf = msg.getData().getByteArray(BluetoothChatUtil.READ_MSG);
                    String str = new String(buf, 0, buf.length);
                    Toast.makeText(getApplicationContext(), "读成功" + str, Toast.LENGTH_SHORT).show();
                    if (str.equals("alarm")) {
                        //MakeMessage();
                        start_alarm = true;
                        //MakeCall();
                        sVibrator.vibrate(Constants.ALARM_VIBRATION_PATTERN_ON_WATCH, 0);
                        mAlarmView.startAlarm();
                    } else if (str.equals("stop")) {
                        if (sVibrator != null) {
                            sVibrator.cancel();
                        }
                        mAlarmView.stopAlarmWithoutNotify();
                    }

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
        setContentView(R.layout.main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // getSupportActionBar().setIcon(R.drawable.ic_launcher);
        PreferencesHelper.initializePreferences(this);

        manager = SmsManager.getDefault();

        sVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mAlarmView = (AlarmView) findViewById(R.id.alarm_view);

        //蓝牙设备初始化
        initBluetooth();
        mBlthChatUtil = BluetoothChatUtil.getInstance(this);
        mBlthChatUtil.registerHandler(mHandler);

        mAlarmView.setOnStopListener(new AlarmView.OnStopListener() {
            @Override
            public void onStop() {
                if (sVibrator != null) sVibrator.cancel();
                mAlarmView.stopAlarmWithoutNotify();
                String messagesend = "stop";
                if (null == messagesend || messagesend.length() == 0) {
                    return;
                }
                mBlthChatUtil.write(messagesend.getBytes());
            }
        });

        mAlarmView.setOnAlarmListener(new AlarmView.OnAlarmListener() {
            @Override
            public void onAlarm() {
                //MakeCall();
                MakeMessage();
                if (sVibrator != null) sVibrator.cancel();
            }
        });

        if (start_alarm) {
            mAlarmView.startAlarm();
            start_alarm = true;
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

    private void MakeCall() {
        // TODO Auto-generated method stub
        //EditText et_phonenumber = (EditText)findViewById(R.id.phonenumber);

        //String number = et_phonenumber.getText().toString();
        //用intent启动拨打电话
        /*Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        startActivity(intent);*/
        phoneNum = PreferencesHelper.getString(Constants.PREFS_NEXT_OF_KIN_TELEPHONE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) ==

                PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNum));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(callIntent);
            sVibrator.cancel();
        }
    }

    private void MakeMessage() {
        phoneNum = PreferencesHelper.getString(Constants.PREFS_NEXT_OF_KIN_TELEPHONE);
        messageName = PreferencesHelper.getString(Constants.PREFS_NEXT_OF_KIN_NAME);
        messageBody = getBestLocation();
        list = manager.divideMessage(messageHeader + messageName + ",在" + messageBody + messageTail);  //因为一条短信有字数限制，因此要将长短信拆分

        for (String text : list) {
            manager.sendTextMessage(phoneNum, null, text, null, null);
        }
        Toast.makeText(getApplicationContext(), "发送完毕", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
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
                    //textView.setText("已成功连接到设备" + device.getName());
                } else {
                    //textView.setText("已成功连接到设备");
                }
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void finish() {
        super.finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_ken:
                new NextOfKinDialog(this);
                return true;
            case R.id.action_bluetooth:
                if (bluetoothAdapter.isEnabled()) {
                    if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        Intent discoveryIntent = new Intent(
                                BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoveryIntent.putExtra(
                                BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                        startActivity(discoveryIntent);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "蓝牙未开启，请先打开蓝牙", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_help:

                return true;
            case R.id.action_about:

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 通过GPS获取定位信息
     */
    public String getGPSLocation() {
        Location gps = LocationUtils.getGPSLocation(this);
        if (gps == null) {
            //设置定位监听，因为GPS定位，第一次进来可能获取不到，通过设置监听，可以在有效的时间范围内获取定位信息
            LocationUtils.addLocationListener(this, LocationManager.GPS_PROVIDER, new LocationUtils.ILocationListener() {
                @Override
                public void onSuccessLocation(Location location) {
                    if (location != null) {
                        Toast.makeText(MainActivity.this, "gps onSuccessLocation location:  lat==" + location.getLatitude() + "     lng==" + location.getLongitude(), Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(MainActivity.this, "gps location is null", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return "XXXX";
        } else {
            Toast.makeText(this, "gps location: lat==" + gps.getLatitude() + "  lng==" + gps.getLongitude(), Toast.LENGTH_SHORT).show();
            return "gps location: lat==" + gps.getLatitude() + "  lng==" + gps.getLongitude();
        }
    }

    /**
     * 通过网络等获取定位信息
     */
    private String getNetworkLocation() {
        Location net = LocationUtils.getNetWorkLocation(this);
        if (net == null) {
            Toast.makeText(this, "net location is null", Toast.LENGTH_SHORT).show();
            return "XXXX";
        } else {
            Toast.makeText(this, "network location: lat==" + net.getLatitude() + "  lng==" + net.getLongitude(), Toast.LENGTH_SHORT).show();
            return "network location: lat==" + net.getLatitude() + "  lng==" + net.getLongitude();
        }
    }

    /**
     * 采用最好的方式获取定位信息
     */
    private String getBestLocation() {
        Criteria c = new Criteria();//Criteria类是设置定位的标准信息（系统会根据你的要求，匹配最适合你的定位供应商），一个定位的辅助信息的类
        c.setPowerRequirement(Criteria.POWER_LOW);//设置低耗电
        c.setAltitudeRequired(true);//设置需要海拔
        c.setBearingAccuracy(Criteria.ACCURACY_COARSE);//设置COARSE精度标准
        c.setAccuracy(Criteria.ACCURACY_LOW);//设置低精度
        //... Criteria 还有其他属性，就不一一介绍了
        Location best = LocationUtils.getBestLocation(this, c);
        if (best == null) {
            Toast.makeText(this, " best location is null", Toast.LENGTH_SHORT).show();
            return "XXXX";
        } else {
            Toast.makeText(this, "best location: lat==" + best.getLatitude() + " lng==" + best.getLongitude(), Toast.LENGTH_SHORT).show();
            return "best location: lat==" + best.getLatitude() + " lng==" + best.getLongitude();
        }
    }
}
