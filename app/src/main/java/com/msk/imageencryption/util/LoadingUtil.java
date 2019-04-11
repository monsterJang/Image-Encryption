package com.msk.imageencryption.util;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.msk.imageencryption.R;

/**
 * author   : 陈龙江
 * time     : 2019/4/7 22:23
 * desc     : 加载时的遮罩处理
 * version  : 1.0
 */
public class LoadingUtil {

    private static Dialog loadingDialog;

    /**
     * author : 陈龙江
     * time   : 2019/4/7 22:59
     * desc   : 返回一个会话，可以根据会话来判断当前是否处于加载状态
     */
    public static void createLoadingDialog(Context context, String msg) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_dialog_item, null);
        LinearLayout layout = view.findViewById(R.id.layout_dialog_loading);
        TextView tvTip = view.findViewById(R.id.tv_loading_tip);
        tvTip.setText(msg);

        loadingDialog = new Dialog(context);
        loadingDialog.setCancelable(false);  // 按“返回键”消失
        loadingDialog.setCanceledOnTouchOutside(false);  // 点击加载框以外的区域
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        // 封装显示Dialog的方法
        Window window = loadingDialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setGravity(Gravity.CENTER);  // 设置dialog显示的位置
        window.setWindowAnimations(R.style.PopWindowAnimStyle);  // 添加动画
        loadingDialog.show();
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/7 22:59
     * desc   : 关闭会话，需要传入会话用于判断
     */
    public static void clossDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

}
