package com.njupt.dezhou.falldetect;

import android.util.Log;

/**
 * Created by dezhou on 2017/11/16.
 */

public class AccelerometerDetection {
    public static double Gravity = 9.8;
    private static final String TAG = "AccelerometerDetection";

    /**
     * 对传进来的合加速度数据进行检测 先判断失重条件 然后再判断超重条件 最后检测有无静止状态
     *
     * @param accelerometerValues 合加速度数据
     * @param accelerometerArray
     * @param accX
     * @return
     */
    public static int accelerometerDetect(float[] accelerometerValues, float[][] accelerometerArray, float accX) {
        boolean isLossWeight = false;
        boolean isOverWeight = false;
        int lossWeightCount = 0;
        int count = 0;
        float sum = 0;
        float slientIndex = 65535;
        float horizonIndex = 65535;
        float sumHorizon = 0;
        boolean isFall = false;

        for (int i = 0; i < accelerometerValues.length; i++) {
            if (MainActivity.DEBUGE) {
                //Log.e(TAG, accelerometerValues[i] + "");
                Log.e(TAG, accelerometerValues[i] + "");
            }

            //判断失重条件 合加速度小于0.8倍的重力加速度
            if (accelerometerValues[i] < 0.8 * Gravity) {
                isLossWeight = true;
                lossWeightCount++;
                if (MainActivity.DEBUGE) {
                    //Log.e(TAG, accelerometerValues[i] + "");
                    Log.e(TAG, "----------失重-----------");
                }
            }

            //判断超重条件 合加速度大于2倍的重力加速度
            if (isLossWeight && accelerometerValues[i] > 1.8 * Gravity) {
                isOverWeight = true;
                if (MainActivity.DEBUGE) {
                    //Log.e(TAG, accelerometerValues[i] + "");
                    Log.e(TAG, "-----------超重-----------");
                }
            }

            //当失重和超重条件都满足时 判断静止状态
            if (isLossWeight && isOverWeight) {
                sum += accelerometerValues[i];
                sumHorizon += Math.abs(accelerometerArray[0][i]);
                count++;
               /* if (accelerometerValues[i] < 1.1 * Gravity && accelerometerValues[i] > 0.9 * Gravity) {
                    slientCount++;
                }*/
                /*if (Math.abs(accelerometerValuesX[i]) < 0.2 * Gravity) {

                    horizonCount++;
                }*/
            }
        }
        if (MainActivity.DEBUGE) {
            if (count != 0) {
                slientIndex = sum / count;
                horizonIndex = sumHorizon / count;
                Log.e(TAG, "----------slient index:" + slientIndex + ", horizon index : " + horizonIndex);
            } else {
                Log.e(TAG, "----------count is zero");
            }
        }

        if (isLossWeight && isOverWeight && slientIndex < 11.0 && horizonIndex < 3) {
            if (MainActivity.DEBUGE) {
                //Log.e(TAG, accelerometerValues[i] + "");
                Log.e(TAG, "加速度检测成功");
            }
            isFall = true;
        }

        if (isFall) {
            return 3;
        } else if (isLossWeight && isOverWeight) {
            return 2;
        } else {
            return 1;
        }
    }
}
