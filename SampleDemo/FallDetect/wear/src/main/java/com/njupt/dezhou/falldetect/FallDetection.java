package com.njupt.dezhou.falldetect;

/**
 * Created by dezhou on 2017-11-23.
 */

public class FallDetection {
    private static int TH1 = 250;
    private static int TH2 = 15;
    static int count = 0;
    static boolean isFall = false;

    /**
     * 阈值检测 当阈值超过TH1时会触发该方法 检测静止状态（阈值小于TH2）
     *
     * @param target 待检测的目标数组
     * @return
     */
    public static boolean FallDetect(float[] target) {
        for (int j = 0; j < MainActivity.WINDOW_SIZE; j++) {
            if (target[j] < TH2) {
                count++;
            }
        }

        if (count > MainActivity.WINDOW_SIZE / 2) {
            isFall = true;
        }

        return isFall;
    }
}
