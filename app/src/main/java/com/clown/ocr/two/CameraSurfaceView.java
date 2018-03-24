package com.clown.ocr.two;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Clown on 2017/07/27.
 */

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback {

    private static final String TAG = "CameraSurfaceView";

    private Context mContext;
    private SurfaceHolder holder;
    private Camera mCamera;

    private int mScreenWidth;
    private int mScreenHeight;
    private CameraTopRectView topView;

    public CameraSurfaceView(Context context) {
        this(context, null);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        getScreenMetrix(context);
        topView = new CameraTopRectView(context, attrs);

        initView();

    }

    //拿到手机屏幕大小
    private void getScreenMetrix(Context context) {
        WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        WM.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
        mScreenHeight = outMetrics.heightPixels;
        Log.e("[][][][]", "weigh:" + mScreenWidth + " height:" + mScreenHeight);
    }

    private void initView() {
        holder = getHolder();//获得surfaceHolder引用
        holder.addCallback(this);
        // holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//设置类型
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.w(TAG, "surfaceCreated");
        if (mCamera == null) {
            mCamera = Camera.open(); // 开启相机
            try {
                mCamera.setPreviewDisplay(holder); // 摄像头画面显示在Surface上
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.w(TAG, "surfaceChanged");
        setCameraParams(mScreenWidth, mScreenHeight);
        //setCameraParams(mCamera, mScreenWidth, mScreenHeight);
        mCamera.startPreview();
        // mCamera.takePicture(null, null, jpeg);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.w(TAG, "surfaceDestroyed");
        mCamera.stopPreview(); // 停止预览
        mCamera.release(); // 释放相机资源
        mCamera = null;
        holder = null;
    }

    @Override
    public void onAutoFocus(boolean success, Camera Camera) {
        if (success) {
            Log.w(TAG, "onAutoFocus success=" + success);
            System.out.println(success);
        }
    }

    private void setCameraParams(int width, int height) {

        Camera.Parameters parameters = mCamera.getParameters();

        List<Size> resolution = new ArrayList<>();

        List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
        for (Camera.Size size : pictureSizeList) {
            Log.w(TAG, "pictureSizeList size.width=" + size.width + "  size.height=" + size.height);
            resolution.add(new Size(size.width, size.height));
        }
        Camera.Size pSize = parameters.getPictureSize();

        pSize = getCameraParams(resolution,pSize,width,height);

        Log.w(TAG, "pitSize.width=" + pSize.width + "  preSize.height=" + pSize.height);

        parameters.setPictureSize(pSize.width, pSize.height);

        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();

        for (Camera.Size size : previewSizeList) {
            Log.w(TAG, "previewSizeList size.width=" + size.width + "  size.height=" + size.height);
            resolution.add(new Size(size.width, size.height));
        }

        pSize = getCameraParams(resolution,pSize,width,height);

        Log.w(TAG, "preSize.width=" + pSize.width + "  preSize.height=" + pSize.height);

        parameters.setPreviewSize(pSize.width, pSize.height);

        parameters.setJpegQuality(100); // 设置照片质量
        if (parameters.getSupportedFocusModes().contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            // 连续对焦模式
            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        mCamera.cancelAutoFocus(); // 自动对焦。
        // 设置PreviewDisplay的方向，效果就是将捕获的画面旋转多少度显示
        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(parameters);

    }

    private Camera.Size getCameraParams(List<Size> resolution, Camera.Size pSize, int width, int height) {

        Collections.sort(resolution);
        Collections.reverse(resolution);

        int original = width + height,
                lastTwo = 0, twoWidth = 0, twoHeight = 0,
                lastOne = 0, oneWidth = 0, oneHeight = 0;

        for (Size size : resolution) {
            if (size.height - width > 80) {
                continue;
            }
            if (lastTwo == 0) {
                lastTwo = Math.abs(size.width + size.height - original);
                twoWidth = size.width;
                twoHeight = size.height;
            } else if (lastOne == 0) {
                lastOne = Math.abs(size.width + size.height - original);
                oneWidth = size.width;
                oneHeight = size.height;
            } else {
                int differ = Math.abs(size.width + size.height - original);
                if (lastOne <= lastTwo && lastOne <= differ) {
                    pSize.width = oneWidth;
                    pSize.height = oneHeight;
                    break;
                } else if (lastTwo <= lastOne && lastTwo <= differ) {
                    pSize.width = twoWidth;
                    pSize.height = twoHeight;
                    break;
                } else {
                    lastTwo = lastOne;
                    twoWidth = oneWidth;
                    twoHeight = oneHeight;

                    lastOne = differ;
                    oneWidth = size.width;
                    oneHeight = size.height;
                }
            }
        }

        return pSize;
    }

    private void setCameraParams(Camera camera, int width, int height) {

        Camera.Parameters parameters = mCamera.getParameters();
        // 获取摄像头支持的PictureSize列表
        List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
        for (Camera.Size size : pictureSizeList) {
            Log.w(TAG, "pictureSizeList size.width=" + size.width + "  size.height=" + size.height);
        }
        // 从列表中选取合适的分辨率
        Camera.Size picSize = getProperSize(pictureSizeList, ((float) height / width));
        if (null == picSize) {
            Log.w(TAG, "null == picSize");
            picSize = parameters.getPictureSize();
        }
        Log.w(TAG, "picSize.width=" + picSize.width + "  picSize.height=" + picSize.height);
        // 根据选出的PictureSize重新设置SurfaceView大小
        float w = picSize.width;
        float h = picSize.height;
        parameters.setPictureSize(picSize.width, picSize.height);
        this.setLayoutParams(new FrameLayout.LayoutParams((int) (height * (h / w)), height));

        // 获取摄像头支持的PreviewSize列表
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();

        for (Camera.Size size : previewSizeList) {
            Log.w(TAG, "previewSizeList size.width=" + size.width + "  size.height=" + size.height);
        }
        Camera.Size preSize = getProperSize(previewSizeList, ((float) height) / width);
        if (null != preSize) {
            Log.w(TAG, "preSize.width=" + preSize.width + "  preSize.height=" + preSize.height);
            parameters.setPreviewSize(preSize.width, preSize.height);
        }
        /*if (null == preSize) {
            Log.w(TAG, "null == preSize");
            preSize = parameters.getPreviewSize();
        }
        Log.w(TAG, "preSize.width=" + preSize.width + "  preSize.height=" + preSize.height);
        parameters.setPreviewSize(preSize.width, preSize.height);*/

        parameters.setJpegQuality(100); // 设置照片质量
        if (parameters.getSupportedFocusModes().contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            // 连续对焦模式
            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        mCamera.cancelAutoFocus(); // 自动对焦。
        // 设置PreviewDisplay的方向，效果就是将捕获的画面旋转多少度显示
        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(parameters);

    }

    /**
     * 从列表中选取合适的分辨率
     * 默认w:h = 4:3
     * <p>注意：这里的w对应屏幕的height
     * h对应屏幕的width<p/>
     */
    private Camera.Size getProperSize(List<Camera.Size> pictureSizeList, float screenRatio) {
        Log.w(TAG, "screenRatio=" + screenRatio);
        Camera.Size result = null;
        for (Camera.Size size : pictureSizeList) {
            float currentRatio = ((float) size.width) / size.height;
            if (currentRatio - screenRatio == 0) {
                result = size;
                break;
            }
        }

        if (null == result) {
            for (Camera.Size size : pictureSizeList) {
                float curRatio = ((float) size.width) / size.height;
                if (curRatio == 4f / 3) {// 默认w:h = 4:3
                    result = size;
                    break;
                }
            }
        }

        return result;
    }


    // 拍照瞬间调用
    private Camera.ShutterCallback shutter = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            Log.w(TAG, "shutter");
            System.out.println("执行了吗+1");
        }
    };

    // 获得没有压缩过的图片数据
    private Camera.PictureCallback raw = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera Camera) {
            Log.w(TAG, "raw");
            System.out.println("执行了吗+2");
        }
    };

    //创建jpeg图片回调数据对象
    private Camera.PictureCallback jpeg = new Camera.PictureCallback() {

        private Bitmap bitmap;

        @Override
        public void onPictureTaken(byte[] data, Camera Camera) {

            if (null == data || data.length == 0) {
                Toast.makeText(mContext, "照相机抓拍出错！", Toast.LENGTH_SHORT).show();
                return;
            }

            topView.draw(new Canvas());

            BufferedOutputStream bos = null;
            Bitmap bm = null;

            try {
                // 获得图片
                bm = BitmapFactory.decodeByteArray(data, 0, data.length);

                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    // String filePath = "/sdcard/dyk" + System.currentTimeMillis() + ".JPEG";
                    String filePath = Environment.getExternalStorageDirectory() + "/Bitmap3" + ".jpg";

                    // 图片存储前旋转
                    Matrix m = new Matrix();
                    int height = bm.getHeight();
                    int width = bm.getWidth();
                    m.setRotate(90);
                    // 旋转后的图片
                    bitmap = Bitmap.createBitmap(bm, 0, 0, width, height, m, true);

                    data = new DisplayUtil().Bitmap2Bytes(bitmap);
                    File bitmapFile = new File(Environment.getExternalStorageDirectory(), "Bitmap1" + ".jpg");
                    new DisplayUtil().clearIOStream(new FileOutputStream(bitmapFile), data);

                    System.out.println("执行了吗+3");
                    File file = new File(filePath);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    bos = new BufferedOutputStream(new FileOutputStream(file));

                    // Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,data.length);
                    Bitmap sizeBitmap = Bitmap.createScaledBitmap(
                            bitmap,
                            topView.getViewWidth(),
                            topView.getViewHeight(),
                            true);

                    data = new DisplayUtil().Bitmap2Bytes(sizeBitmap);
                    File sizeFile = new File(Environment.getExternalStorageDirectory(), "Bitmap2" + ".jpg");
                    new DisplayUtil().clearIOStream(new FileOutputStream(sizeFile), data);

                    // 截取
                    bm = Bitmap.createBitmap(
                            sizeBitmap,
                            topView.getRectLeft(),
                            topView.getRectTop(),
                            topView.getRectRight() - topView.getRectLeft(),
                            topView.getRectBottom() - topView.getRectTop());

                    bm.compress(Bitmap.CompressFormat.JPEG, 100, bos); // 将图片压缩到流中

                } else {
                    Toast.makeText(mContext, "没有检测到内存卡", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bos != null) {
                        bos.flush();
                        bos.close();
                        bm.recycle();
                    }
                    mCamera.stopPreview();
                    mCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void takePicture() {
        // 设置参数,并拍照
        setCameraParams(mScreenWidth, mScreenHeight);
        //setCameraParams(mCamera, mScreenWidth, mScreenHeight);
        // 当调用camera.takePiture方法后，camera关闭了预览，这时需要调用startPreview()来重新开启预览
        mCamera.takePicture(null, null, jpeg);
    }

    /*public void setAutoFocus(){
        mCamera.autoFocus(this);
    }*/

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
