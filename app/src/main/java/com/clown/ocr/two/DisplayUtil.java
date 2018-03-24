package com.clown.ocr.two;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Clown on 2017/07/27.
 */

public class DisplayUtil {

    /**
     * 将px装换成dp，保证尺寸不变
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dp(Context context, float pxValue) {
        float density = context.getResources().getDisplayMetrics().density; // 得到设备的密度
        return (int) (pxValue / density + 0.5f);
    }

    public static int dp2px(Context context, float dpValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        float scaleDensity = context.getResources().getDisplayMetrics().scaledDensity; // 缩放密度
        return (int) (pxValue / scaleDensity + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        float scaleDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * scaleDensity + 0.5f);
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

    public void clearIOStream(FileOutputStream stream, byte[] data) {
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
