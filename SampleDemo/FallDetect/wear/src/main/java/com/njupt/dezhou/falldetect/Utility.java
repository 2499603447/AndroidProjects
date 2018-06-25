package com.njupt.dezhou.falldetect;

import android.util.Log;

/**
 * Created by dezhou on 2018-01-04.
 */

public class Utility {
    public static double Gravity = 9.8;
    private static final String TAG = "Utility";

    public static boolean LossWeightDetect(float accData) {
        boolean isLossWeight = false;
        if (accData < 0.8 * Gravity) {
            isLossWeight = true;
        }
        return isLossWeight;
    }

    public static boolean OverWeightDetect(float accData) {
        boolean isOverWeight = false;
        if (accData > 1.8 * Gravity) {
            isOverWeight = true;
        }
        return isOverWeight;
    }

    public static boolean GravityDetect(float[] accelerometerValues) {
        boolean isLossWeight = false;
        boolean isOverWeight = false;

        for (int i = 0; i < accelerometerValues.length; i++) {
            if (MainActivity.DEBUGE) {
                //Log.e(TAG, accelerometerValues[i] + "");
                Log.e(TAG, accelerometerValues[i] + "");
            }

            //判断失重条件 合加速度小于0.8倍的重力加速度
            if (accelerometerValues[i] < 0.8 * Gravity) {
                isLossWeight = true;
                if (MainActivity.DEBUGE) {
                    //Log.e(TAG, accelerometerValues[i] + "");
                    Log.e(TAG, "----------失重-----------");
                }
            }

            //判断超重条件 合加速度大于2倍的重力加速度
            if (isLossWeight && accelerometerValues[i] > 1.95 * Gravity) {
                isOverWeight = true;
                if (MainActivity.DEBUGE) {
                    //Log.e(TAG, accelerometerValues[i] + "");
                    Log.e(TAG, "-----------超重-----------");
                }
            }
        }
        return isLossWeight && isOverWeight;
    }

    public boolean SlientDetect(float[] accelerometerValues) {
        int count = 0;
        float sum = 0;
        float slientIndex = 65535;
        for (int i = 0; i < accelerometerValues.length; i++) {
            sum += accelerometerValues[i];
            count++;
        }
        if (count != 0) {
            slientIndex = sum / count;
            Log.e(TAG, "----------slient index:" + slientIndex);
        } else {
            Log.e(TAG, "----------count is zero");
        }
        if (slientIndex < 11.0) {
            if (MainActivity.DEBUGE) {
                //Log.e(TAG, accelerometerValues[i] + "");
                Log.e(TAG, "加速度检测成功");
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean HorizonDetect(float[][] accelerometerArray) {
        int count = 0;
        float horizonIndex = 65535;
        float sumHorizon = 0;
        for (int i = 0; i < accelerometerArray[0].length; i++) {
            sumHorizon += Math.abs(accelerometerArray[0][i]);
            count++;
        }
        if (count != 0) {
            horizonIndex = sumHorizon / count;
            Log.e(TAG, ", horizon index : " + horizonIndex);
        } else {
            Log.e(TAG, "----------count is zero");
        }
        if (horizonIndex < 3) {
            if (MainActivity.DEBUGE) {
                //Log.e(TAG, accelerometerValues[i] + "");
                Log.e(TAG, "加速度检测成功");
            }
            return true;
        } else {
            return false;
        }
    }

    public static int SlientThresholdDetect(float[] threholdValue) {
        int count = 0;
        float horizonIndex = 65535;
        float sumHorizon = 0;
        float sum = 0;
        float silentIndex = 65535;
        for (int i = 0; i < threholdValue.length; i++) {
            sumHorizon += Math.abs(threholdValue[i]);
            count++;
        }
        if (count != 0) {
            silentIndex = sum / count;
            Log.e(TAG, "----------slient index:" + silentIndex);
        } else {
            Log.e(TAG, "----------count is zero");
        }

        if (silentIndex < 20) {
            return 1;//水平
        }
        return 0;
    }

    public static int SilentHorizonDetect(float[] accelerometerValues, float[][] accelerometerArray) {
        int count = 0;
        float horizonIndex = 65535;
        float sumHorizon = 0;
        float sum = 0;
        float silentIndex = 65535;
        for (int i = 0; i < accelerometerArray[0].length; i++) {
            sumHorizon += Math.abs(accelerometerArray[0][i]);
            count++;
        }
        if (count != 0) {
            silentIndex = sum / count;
            horizonIndex = sumHorizon / count;
            Log.e(TAG, "----------slient index:" + silentIndex + ", horizon index : " + horizonIndex);
        } else {
            Log.e(TAG, "----------count is zero");
        }

        if (silentIndex < 11.0 && horizonIndex < 3) {
            if (MainActivity.DEBUGE) {
                //Log.e(TAG, accelerometerValues[i] + "");
                Log.e(TAG, "加速度检测成功");
            }
            return 1;//水平又静止
        } else if (silentIndex < 11.0) {
            return 2;//静止不水平
        } else if (horizonIndex < 3) {
            return 3;//水平不静止
        }
        return 0;//既不水平又不静止
    }
}
