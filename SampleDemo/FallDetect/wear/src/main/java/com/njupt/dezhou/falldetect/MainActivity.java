package com.njupt.dezhou.falldetect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
//import sintef.android.controller.AlarmView;


public class MainActivity extends WearableActivity implements SensorEventListener {
    private static final String TAG = "TEST_1";

    public static boolean DEBUGE = false;
    // 传感器实例
    private SensorManager sManager;
    private TextView valueText;
    private Sensor accelerometer; // 加速度传感器

    //private SampleRate sampleRate;
    private ScheduledExecutorService scheduledExecutorService;
    private boolean isRecord = false;
    private int TH1 = 220;
    private int TH2 = 20;
    private int commonCount = 0;
    private int overThresholdCount = 0;
    private boolean isFall = false;

    //private AlarmView mAlarmView;
    private static Vibrator sVibrator;

    public static int WINDOW_SIZE = 100;

    private float[] threholdValue = new float[WINDOW_SIZE];
    private boolean isThresholdFall = false;

    private float[][] accelerometerArray = new float[3][WINDOW_SIZE]; //存储加速度的二维数组
    private Button connectDevice;
    private FileOutputStream acc_fos;//文件输出流
    private String TESTER = "_subject_05";
    private int accelerometerDataNum = 0;
    private long startTime = 0;  //获取开始时间
    private long lastTime = 0;  //获取开始时间
    private boolean isVibrate = true;
    private boolean isWriting = false;
    float[][] dif = new float[3][WINDOW_SIZE];//保存相邻两个加速度数据的差
    //private float[] accData = new float[WINDOW_SIZE];//保存合加速度的值
    private float[] accelerometerValues = new float[3];//X Y Z加速度值
    float[] squaresum = new float[3], sum = new float[3];
    float[] lastValues = new float[3];//前一个加速度值
    float vigTarget = 0;//方差阈值
    float resultantAccData = 0;//X Y Z三方向的加速度平方和
    private int sampleInterval = 50;
    private SampleRate sampleRate;

    private boolean isGravity = false;
    private int HorizonAndSilent = 1;
    private boolean isCount = true;//当前是否记录阈值超过峰值的个数
    private int intervalCount = 0;//相邻两个峰值之间的计数值
    private int peakCount = 0;//在此之前峰值的个数
    private boolean isOverTherohold = false;
    private int slientCount = 0;
    private boolean isSlientDetect = false;
    private int slientNumber = 0;
    long currentTime = 0;
    private int lossWeightCount = 0;
    private int overWeightCount = 0;
    private boolean isLossWeight = false;
    private boolean isOverWeight = false;

    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private TextView device_bluetooth;
    String address = "";
    private Context mContext;
    private BluetoothChatUtil mBlthChatUtil;
    private BluetoothDevice bindDevice;
    private AlarmView mAlarmView;
    public static final long[] ALARM_VIBRATION_PATTERN_ON_WATCH = {0, 100, 1000};
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothChatUtil.STATE_CONNECTED:
                    String deviceName = msg.getData().getString(BluetoothChatUtil.DEVICE_NAME);
                    //mBtConnectState.setText("已成功连接到设备" + deviceName);
                    Toast.makeText(getApplicationContext(), "已成功连接到设备" + deviceName, Toast.LENGTH_SHORT).show();
                    connectDevice.setBackground(getResources().getDrawable(R.drawable.bluetooth_connected));

                    break;
                case BluetoothChatUtil.STATAE_CONNECT_FAILURE:

                    Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
                    connectDevice.setBackground(getResources().getDrawable(R.drawable.bluetooth_disconnect));
                    break;
                case BluetoothChatUtil.MESSAGE_DISCONNECTED:
                    Toast.makeText(getApplicationContext(), "与设备断开连接", Toast.LENGTH_SHORT).show();
                    //mBtConnectState.setText("与设备断开连接");
                    break;
                case BluetoothChatUtil.MESSAGE_READ: {
                    byte[] buf = msg.getData().getByteArray(BluetoothChatUtil.READ_MSG);
                    String str = new String(buf, 0, buf.length);
                    Toast.makeText(getApplicationContext(), "读成功" + str, Toast.LENGTH_SHORT).show();
                    if (str.equals("stop")) {
                        if (sVibrator != null) {
                            sVibrator.cancel();
                        }
                        mAlarmView.stopAlarmWithoutNotify();
                    }
                    //text.setText(text.getText().toString() + "\n" + str);
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
        setContentView(R.layout.rect_activity_main);
        mContext = this;
        final int width = getWindowManager().getDefaultDisplay().getWidth();
        final int height = getWindowManager().getDefaultDisplay().getHeight();
        startTime = System.currentTimeMillis();   //获取开始时间
        lastTime = System.currentTimeMillis();   //获取开始时间
        //setAmbientEnabled();
        sampleRate = new SampleRate();

        // 获取传感器实例
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // 初始化加速度传感器
        accelerometer = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.e(TAG, "onCreate: accelerometer.MinDelay();" + accelerometer.getMinDelay() + "  accelerometer.MaxDelay();" + accelerometer.getMaxDelay());
        sVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        valueText = (TextView) findViewById(R.id.valueText);
        connectDevice = (Button) findViewById(R.id.connectDevice);

        mAlarmView = (AlarmView) findViewById(R.id.alarmView);

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
                if (sVibrator != null) sVibrator.cancel();
            }
        });

        initBluetooth();
        mBlthChatUtil = BluetoothChatUtil.getInstance(mContext);
        mBlthChatUtil.registerHandler(mHandler);

        connectDevice.setOnClickListener(new View.OnClickListener() {
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
                    mBluetoothAdapter.startDiscovery();
                }
            }
        });

        //每10s产生一次点击事件，点击的点坐标为(0.2W - 0.8W,0.2H - 0.8 H),W/H为手机分辨率的宽高.
        new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    //生成点击坐标
                    int x = (int) (width * 0.9);
                    int y = (int) (height * 0.9);
                    //利用ProcessBuilder执行shell命令
                    String[] order = {
                            "input",
                            "tap",
                            "" + x,
                            "" + y
                    };
                    try {
                        new ProcessBuilder(order).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //线程睡眠10s
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
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

    /**
     * 写一个文件到SDCard
     *
     * @throws IOException
     */
    private void writeFileToSDCard() throws IOException {
        // 比如可以将一个文件作为普通的文档存储，那么先获取系统默认的文档存放根目录
        File parent_path = Environment.getExternalStorageDirectory();

        // 可以建立一个子目录专门存放自己专属文件
        File dir = new File(parent_path.getAbsoluteFile(), "FallDetection");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String local_file_acc = dir.getAbsolutePath() + "/Accelerometer_" + GetCurrentTime() + TESTER + ".csv";

        File file_acc = new File(local_file_acc);

        try {
            if (!file_acc.createNewFile()) {
                System.out.println("File already exists");
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }

        acc_fos = new FileOutputStream(file_acc, true);
    }

    /**
     * 记录数据 只记录当前的加速度数据
     */
    private void RecorrdSensorData() {
        String acc_data = (System.currentTimeMillis() - startTime) + "," + (accelerometerDataNum++) + "," + accelerometerValues[0] + "," + accelerometerValues[1] + "," + accelerometerValues[2];
        byte[] acc_buffer = acc_data.getBytes();

        try {
            // 开始写入数据到这个文件。
            acc_fos.write(acc_buffer, 0, acc_buffer.length);
            acc_fos.write("\r\n".getBytes());
            acc_fos.flush();

        } catch (Exception e) {
        }
    }

    /**
     * 记录数据 根据传进来的参数记录数据
     *
     * @param accelerometerArray 将数组中的数据写入到文件中
     */
    private void RecorrdSensorData(float[][] accelerometerArray) {
        String acc_data;
        for (int j = 0; j < WINDOW_SIZE; j++) {
            acc_data = (System.currentTimeMillis() - startTime) + "," + (accelerometerDataNum++) + "," + accelerometerArray[0][j] + "," + accelerometerArray[1][j] + "," + accelerometerArray[2][j];
            byte[] acc_buffer = acc_data.getBytes();

            try {
                // 开始写入数据到这个文件。
                acc_fos.write(acc_buffer, 0, acc_buffer.length);
                acc_fos.write("\r\n".getBytes());
                acc_fos.flush();

            } catch (Exception e) {
            }
        }
    }

    /**
     * 获取当前程序执行的时间
     *
     * @return
     */
    private String GetCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间

        return formatter.format(curDate);
    }

    /**
     * 计算加速度各个方向的倾斜角
     */
    private float[] calculateAngle(float[] accelerometerValues) {
        float[] angles = new float[3];
        //x y z 代表X Y Z三个方向的加速度
        float x = accelerometerValues[0];
        float y = accelerometerValues[1];
        float z = accelerometerValues[2];
        angles[0] = (float) Math.atan(x / Math.sqrt(Math.pow(y, 2) + Math.pow(z, 2)));
        angles[1] = (float) Math.atan(y / Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2)));
        angles[2] = (float) Math.atan(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) / z);
        //Log.e(TAG, "calculateOrientation: x:" + x + " y:" + y + " z:" + z);
        return angles;
    }

    /**
     * 计算方差阈值 并对数据进行加速度检测
     */
    private void ThresholdAndAccelerometerDetection() {
        if (isWriting) {
            RecorrdSensorData();
        }
        vigTarget = 0;
        resultantAccData = 0;

        //法1.论文中计算倾斜角的方法
        float[] values = calculateAngle(accelerometerValues);
        //Log.e(TAG, "calculateOrientation: values[0]:" + values[0] + " values[1]:" + values[1] + " values[2]:" + values[2]);
        //Log.e(TAG, "values[0]:" + values[0] + " values[1]:" + values[1] + " values[2]:" + values[2]);

        //法2.通过加速度传感器和磁力传感器得到的旋转矩阵计算出的倾斜角
       /* float[] values = new float[3];
        float[] R = new float[9];

        SensorManager.getRotationMatrix(R, null, accelerometerValues,
                magneticFieldValues);
        SensorManager.getOrientation(R, values);
        values[0] = (float) Math.toDegrees(values[0]);
        values[1] = (float) Math.toDegrees(values[1]);
        values[2] = (float) Math.toDegrees(values[2]);*/

        //计算X Y Z三个方向的梯度和
        //方差的计算方式采用“加1减1”的方式
        //即每次加上dif数组最后的一个数据dif[*][WINDOW_SIZE - 1] 减去dif数组第一个数据dif[*][0]
        for (int j = 0; j < 3; j++) {
            //减去dif[*][*]数组的第一个数值
            sum[j] -= dif[j][0];
            squaresum[j] -= dif[j][0] * dif[j][0];

            //对数组中的数据进行前移 即dif[j][i] = dif[j][i + 1];
            for (int i = 0; i < WINDOW_SIZE - 1; i++) {
                dif[j][i] = dif[j][i + 1];
                //accelerometerArray[j][i] = accelerometerArray[j][i + 1];
               /* if (j == 0) {
                    threholdValue[i] = threholdValue[i + 1];
                    //accData[i] = accData[i + 1];
                }*/
            }
            dif[j][WINDOW_SIZE - 1] = (values[j] - lastValues[j]) / (float) 0.05;
            //accelerometerArray[j][WINDOW_SIZE - 1] = accelerometerValues[j];
            resultantAccData += Math.pow(accelerometerValues[j], 2);

            //计算方差 vigTarget
            sum[j] += dif[j][WINDOW_SIZE - 1];
            squaresum[j] += dif[j][WINDOW_SIZE - 1] * dif[j][WINDOW_SIZE - 1];

            vigTarget += squaresum[j] / dif[j].length - (sum[j] / dif[j].length) * (sum[j] / dif[j].length);
            lastValues[j] = values[j];
        }
        // Log.e(TAG, "calculateOrientation: vigTarget  " + vigTarget);
        valueText.setText(vigTarget + "");

        //Log.e(TAG, "MainActivity: " + accelerometerArray[WINDOW_SIZE - 1]);
        //threholdValue[WINDOW_SIZE - 1] = vigTarget;
        //Log.e(TAG, "合加速度: " + Math.sqrt(resultantAccData));
        //accData[WINDOW_SIZE - 1] = (float) Math.sqrt(resultantAccData);
        FallDetect(vigTarget);
    }

    private void FallDetect(float target) {
        if (target > TH1) {
            if (!isOverTherohold) {
                overThresholdCount = 0;
                peakCount++;
            }
           /* if (commonCount < 20 && slientCount < 20) {

            }*/
            isOverTherohold = true;
            slientCount = 0;
            commonCount = 0;
            isFall = true;
            overThresholdCount++;
            if (!isGravity) {
                //阈值检测条件通过，进行加速度检测
                //isGravity = Utility.GravityDetect(accData);
            }
        } else if (isFall && target < TH1 && target > TH2) {
            //Log.e(TAG, "overThresholdCount： " + overThresholdCount);
            if (overThresholdCount > 100) {
                isFall = false;
                isGravity = false;
                peakCount = 0;
            }
            isVibrate = true;
            commonCount++;
        } else if (isFall && target < TH2) {
            slientCount++;
        }
        //当前不处在高阈值当中
        if (target < TH1) {
            isOverTherohold = false;
        }
        //Log.e(TAG, "commonCount: " + commonCount + " slientCount:" + slientCount);
        //表明用户是可以活动的
        if (commonCount > 120) {
            isFall = false;
            isGravity = false;
            peakCount = 0;
        }

        //Log.e(TAG, "peakCount: " + peakCount);
/*
        if (target < 5 && commonCount < 60) {
            slientCount++;
        }*/
        //Log.e(TAG, "accelerometerValues[0]" + accelerometerValues[0]);
        if (isFall && slientCount > 100 && Math.abs(accelerometerValues[0]) < 2) {
           //sVibrator.vibrate(4000);
            sVibrator.vibrate(ALARM_VIBRATION_PATTERN_ON_WATCH, 0);
            mAlarmView.startAlarm();
            //发送报警信息到手机
            String messagesend = "alarm";
            if (null == messagesend || messagesend.length() == 0) {
                return;
            }
            mBlthChatUtil.write(messagesend.getBytes());

            isFall = false;
            isGravity = false;
            peakCount = 0;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        currentTime = System.currentTimeMillis();
        //Log.e(TAG, "onSensorChanged: ");
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && currentTime - lastTime > 25) {
            accelerometerValues = event.values;
            //Log.e(TAG, "onSensorChanged: X:" + accelerometerValues[0] + " Y:" + accelerometerValues[1] + " Z:" + accelerometerValues[2]);
            //Log.e(TAG, "onSensorChanged::" + Math.sqrt(Math.pow(accelerometerValues[0], 2) + Math.pow(accelerometerValues[1], 2) + Math.pow(accelerometerValues[2], 2)));
            ThresholdAndAccelerometerDetection();
            lastTime = currentTime;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        if (mBlthChatUtil != null) {
            if (mBlthChatUtil.getState() == BluetoothChatUtil.STATE_CONNECTED) {
                BluetoothDevice device = mBlthChatUtil.getConnectedDevice();
                if (null != device && null != device.getName()) {
                    //text.setText("已成功连接到设备" + device.getName());
                } else {
                    //text.setText("已成功连接到设备");
                }
            }
        }
    }

    /**
     * 换行切换任务
     *
     * @author Administrator
     */
    private class ScrollTask implements Runnable {

        public void run() {
            isRecord = true;
        }
    }

    @Override
    public void onStart() {
        if (DEBUGE) {
            Log.e(TAG, "onStart");
        }
        //创建视图时开始计时
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        // 当Activity显示出来后，
        scheduledExecutorService.scheduleAtFixedRate(new ScrollTask(), 50, 50, TimeUnit.MILLISECONDS);
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sVibrator != null) sVibrator.cancel();
        if (acc_fos != null) {
            try {
                acc_fos.close();
                isWriting = false;
            } catch (Exception e) {

            }
        }
        sManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (sVibrator != null) sVibrator.cancel();
        if (acc_fos != null) {
            try {
                acc_fos.close();
                isWriting = false;
            } catch (Exception e) {

            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sVibrator != null) sVibrator.cancel();
        if (acc_fos != null) {
            try {
                acc_fos.close();
                isWriting = false;
            } catch (Exception e) {
            }
        }
        sManager.unregisterListener(this);
        mBlthChatUtil = null;
        unregisterReceiver(mBluetoothReceiver);
    }
}
