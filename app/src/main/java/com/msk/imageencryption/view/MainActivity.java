package com.msk.imageencryption.view;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.msk.imageencryption.R;
import com.msk.imageencryption.base.BaseActivity;
import com.msk.imageencryption.util.ArrayUtil;
import com.msk.imageencryption.util.LoadingUtil;
import com.msk.imageencryption.util.LogUtil;
import com.msk.imageencryption.util.ScramblingUtil;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    private ImageView ivDisplay;
    private EditText etKey;
    private Button btnEnc;
    private Button btnDec;

    public static final int CHOOSE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivDisplay = findViewById(R.id.iv_main_display);
        etKey = findViewById(R.id.et_main_key);
        btnEnc = findViewById(R.id.btn_main_enc);
        btnDec = findViewById(R.id.btn_main_dec);
        ivDisplay.setOnClickListener(this);
        btnEnc.setOnClickListener(this);
        btnDec.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_main_display:
                LogUtil.d(TAG, "onClick: R.id.iv_main_display");
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }
                break;
            case R.id.btn_main_enc:
                if (ivDisplay.getDrawable() == null) {
                    Toast.makeText(this, "请选择一张图片", Toast.LENGTH_LONG).show();
                    return ;
                }
                if(etKey.getText().toString().length() == 0) {
                    Toast.makeText(this, "请输入密钥", Toast.LENGTH_LONG).show();
                    return ;
                }
                {
                    String strKey = etKey.getText().toString();
                    double dbKey = Double.valueOf(strKey);
                    if (dbKey <= 0 || dbKey >= 1) {
                        Toast.makeText(this, "密钥应为0~1之间的任意小数(不包括0与1)", Toast.LENGTH_LONG).show();
                        return ;
                    }
                    LoadingUtil.createLoadingDialog(MainActivity.this, "加密中...");
                    final double dbKeyFinal = dbKey;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            encrypt(dbKeyFinal);
                            etKey.setText("");
                            LoadingUtil.clossDialog();
                        }
                    }).start();
                }
                break;
            case R.id.btn_main_dec:
                if (ivDisplay.getDrawable() == null) {
                    Toast.makeText(this, "请选择一张图片", Toast.LENGTH_LONG).show();
                    return ;
                }
                if(etKey.getText().toString().length() == 0) {
                    Toast.makeText(this, "请输入密钥", Toast.LENGTH_LONG).show();
                    return ;
                }
                {
                    String strKey = etKey.getText().toString();
                    double dbKey = Double.valueOf(strKey);
                    if (dbKey <= 0 || dbKey >= 1) {
                        Toast.makeText(this, "密钥应为0~1之间的任意小数(不包括0与1)", Toast.LENGTH_LONG).show();
                        return ;
                    }
                    LoadingUtil.createLoadingDialog(MainActivity.this, "解密中...");
                    final double dbKeyFinal = dbKey;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            decrypt(dbKeyFinal);
                            etKey.setText("");
                            LoadingUtil.clossDialog();
                        }
                    }).start();
                }
                break;
            default:
                break;
        }
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/3 23:01
     * desc   : 权限申请结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/3 22:57
     * desc   : 打开相册
     */
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/3 23:02
     * desc   : 活动返回结果处理
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (data != null) {
                    handleImage(data);
                }
                break;
        }
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/3 23:04
     * desc   : 相册图片路径处理
     */
    private void handleImage(@NonNull Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];  // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径
            imagePath = uri.getPath();
        }
        displayImage(imagePath);
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/3 23:09
     * desc   : 通过Uri和selection来获取真实的图片路径
     */
    private  String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/7 20:43
     * desc   : 显示图片
     */
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            ivDisplay.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/3 16:20
     * desc   : 加密，并将加密后的图像显示出来
     */
    private void encrypt(double key) {
        LogUtil.d(TAG, "encrypt: begin");
        // 获取图像像素矩阵的行数与列数
        Bitmap bmpDisplay = ((BitmapDrawable)ivDisplay.getDrawable()).getBitmap();
        int M = bmpDisplay.getHeight();
        int N = bmpDisplay.getWidth();
        LogUtil.d(TAG, "Image ==> height: " + M + ", width: " + N);
        // 获取图像像素矩阵
        int []pixel = new int[M * N];
        bmpDisplay.getPixels(pixel, 0, N, 0, 0, N, M);
        // 像素矩阵转二维
        int [][]pixel2D = new int[M][N];
        ArrayUtil.change1Dto2D(pixel, pixel2D, M, N);
        // 加密
        ScramblingUtil.encrypt(key, pixel2D, M, N);
        // 加密后矩阵转一维
        ArrayUtil.change2Dto1D(pixel2D, pixel, M, N);
        // 生成加密后的图像
        Bitmap bmpEncrypt = Bitmap.createBitmap(pixel, 0, N, N, M, Bitmap.Config.ARGB_8888);
        // 显示加密后的图像
        ivDisplay.setImageBitmap(bmpEncrypt);
        LogUtil.d(TAG, "encrypt: end");
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/4 2:33
     * desc   : 解密，并将解密后的图像显示出来
     */
    private void decrypt(double key) {
        LogUtil.d(TAG, "decrypt: begin");
        // 获取图像像素矩阵的行数与列数
        Bitmap bmpDisplay = ((BitmapDrawable)ivDisplay.getDrawable()).getBitmap();
        int M = bmpDisplay.getHeight();
        int N = bmpDisplay.getWidth();
        // 获取图像像素矩阵
        int []pixel = new int[M * N];
        bmpDisplay.getPixels(pixel, 0, N, 0, 0, N, M);
        // 像素矩阵转二维
        int [][]pixel2D = new int[M][N];
        ArrayUtil.change1Dto2D(pixel, pixel2D, M, N);
        // 解密
        ScramblingUtil.decrypt(key, pixel2D, M, N);
        // 加密后矩阵转一维
        ArrayUtil.change2Dto1D(pixel2D, pixel, M, N);
        // 生成解密后的图像
        Bitmap bmpDecrypt = Bitmap.createBitmap(pixel, 0, N, N, M, Bitmap.Config.ARGB_8888);
        // 显示解密后的图像
        ivDisplay.setImageBitmap(bmpDecrypt);
        LogUtil.d(TAG, "decrypt: end");
    }

}
