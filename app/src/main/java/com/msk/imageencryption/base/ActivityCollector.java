package com.msk.imageencryption.base;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * author   : 陈龙江
 * time     : 2019/4/3 14:45
 * desc     :
 * version  : 1.0
 */
public class ActivityCollector {

    private static List<Activity> activityList = new ArrayList<>();

    public static void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activityList.remove(activity);
    }

    public static void finishAll() {
        for (Activity activity : activityList) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        activityList.clear();
    }

}
