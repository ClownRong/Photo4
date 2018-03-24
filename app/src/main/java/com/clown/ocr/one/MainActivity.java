package com.clown.ocr.one;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.clown.ocr.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private CameraPreview surfaceview;
    private Camera camera;
    private Button take;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

            /*
            // 设置全屏显示
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            */

        surfaceview = (CameraPreview) findViewById(R.id.surfaceview);
        take = (Button) findViewById(R.id.take);

        SurfaceHolder holder = surfaceview.getHolder();
        holder.setFixedSize(176, 155);// 设置分辨率
        holder.setKeepScreenOn(true);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // SurfaceView只有当activity显示到了前台，该控件才会被创建  因此需要监听surfaceview的创建
        holder.addCallback(new MySurfaceCallback());


        // 拍照按钮
        take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                takepicture();

            }
        });

    }

    // 点击事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // 对焦
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {
                camera.cancelAutoFocus();
            }
        });

        return super.onTouchEvent(event);
    }

    /**
     * 监听surfaceview的创建
     *
     * @author Administrator
     *         Surfaceview只有当activity显示到前台，该空间才会被创建
     */
    private final class MySurfaceCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

            try {

                // 当surfaceview创建就去打开相机
                camera = Camera.open();
                Camera.Parameters params = camera.getParameters();
                // Log.i("i", params.flatten());
                int[] result = parameters(camera); // 获取图片size
                params.setJpegQuality(80);  // 设置照片的质量
                params.setPreviewSize(result[0], result[1]);
                params.setPictureSize(1024, 768);
                params.setPreviewFrameRate(5);  // 预览帧率
                camera.setParameters(params); // 将参数设置给相机
                // 右旋90度，将预览调正
                camera.setDisplayOrientation(90);
                // 设置预览显示
                camera.setPreviewDisplay(surfaceview.getHolder());
                // 开启预览
                camera.startPreview();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (camera != null) {
                camera.release();
                camera = null;
            }
        }
    }

    private int[] parameters(Camera camera) {
        int[] result = {1280, 720, 1280, 720};
        List<Camera.Size> pictureSizes = camera.getParameters().getSupportedPictureSizes();
        List<Camera.Size> previewSizes = camera.getParameters().getSupportedPreviewSizes();
        Camera.Size psize = null;
        for (int i = 0; i < previewSizes.size(); i++) {
            psize = previewSizes.get(i);
        }
        if (psize != null) {
            result[0] = psize.width;
            result[1] = psize.height;
        }
        for (int i = 0; i < pictureSizes.size(); i++) {
            psize = pictureSizes.get(i);
        }
        if (psize != null) {
            result[2] = psize.width;
            result[3] = psize.height;
        }
        Log.e("[][][][]", Arrays.toString(result));
        return result;
    }

    // 拍照的函数
    public void takepicture() {
        /*
         * shutter:快门被按下
         * raw:相机所捕获的原始数据
         * jpeg:相机处理的数据
         */
        camera.takePicture(null, null, new MyPictureCallback());
    }

    // byte转Bitmap
    public Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    // bitmap转byte
    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 将彩色图转换为黑白图
     */

    // 照片回调函数，其实是处理照片的
    private final class MyPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            if (null == data || data.length == 0) {
                Toast.makeText(MainActivity.this, "照相机抓拍出错！", Toast.LENGTH_SHORT).show();
                return;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

            Matrix m = new Matrix();
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            m.setRotate(90); // 将照片右旋90度

            options.inJustDecodeBounds = false;
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);

            Log.d("TAG", "width " + width);
            Log.d("TAG", "height " + height);

            // 截取透明框内照片(身份证)
            Bitmap ID = Bitmap.createBitmap(bitmap, 50, 270, 680, 450);

            Bitmap name = Bitmap.createBitmap(ID, 105, 50, 110, 55);

            Bitmap gender = Bitmap.createBitmap(ID, 90, 106, 73, 73);

            Bitmap people = Bitmap.createBitmap(ID, 248, 110, 65, 65);

            Bitmap date = Bitmap.createBitmap(ID, 95, 160, 300, 65);

            Bitmap address = Bitmap.createBitmap(ID, 90, 225, 320, 125);

            Bitmap number = Bitmap.createBitmap(ID, 195, 350, 400, 60);

            try {

                data = Bitmap2Bytes(ID);
                File IDFile = new File(Environment.getExternalStorageDirectory(), "身份证" + ".jpg");
                clearIOStream(new FileOutputStream(IDFile), data);

                data = Bitmap2Bytes(name);
                File nameFile = new File(Environment.getExternalStorageDirectory(), "姓名" + ".jpg");
                clearIOStream(new FileOutputStream(nameFile), data);

                data = Bitmap2Bytes(gender);
                File genderFile = new File(Environment.getExternalStorageDirectory(), "性别" + ".jpg");
                clearIOStream(new FileOutputStream(genderFile), data);

                data = Bitmap2Bytes(people);
                File peopleFile = new File(Environment.getExternalStorageDirectory(), "民族" + ".jpg");
                clearIOStream(new FileOutputStream(peopleFile), data);

                data = Bitmap2Bytes(date);
                File dateFile = new File(Environment.getExternalStorageDirectory(), "日期" + ".jpg");
                clearIOStream(new FileOutputStream(dateFile), data);

                data = Bitmap2Bytes(address);
                File addressFile = new File(Environment.getExternalStorageDirectory(), "住址" + ".jpg");
                clearIOStream(new FileOutputStream(addressFile), data);

                data = Bitmap2Bytes(number);
                File numFile = new File(Environment.getExternalStorageDirectory(), "身份证号" + ".jpg");
                clearIOStream(new FileOutputStream(numFile), data);

                // 在拍照的时候相机是被占用的,拍照之后需要重新预览
                camera.startPreview();

                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void clearIOStream(FileOutputStream stream, byte[] data) {
        try {
            stream.write(data);
            stream.flush();
            stream.close();
            stream = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
