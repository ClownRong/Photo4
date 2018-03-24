package com.clown.ocr.one;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.clown.ocr.R;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ResultActivity extends AppCompatActivity {

    ImageView imageView;
    Bitmap photo, name, gender, people, date, address, number;
    EditText nameText, genderText, peopleText, dateText, addressText, numberText;
    String nameResult, genderResult, peopleResult, dateResult, addressResult, numberResult;
    ProgressDialog dialog;

    Handler handler;

    void test() {

        Bitmap picture = ImageFilter.decodeFile(Environment.getExternalStorageDirectory() + "/身份证" + ".jpg");

        Bitmap test = Bitmap.createBitmap(picture, 90, 106, 73, 73);

        byte[] data = Bitmap2Bytes(test);
        File testFile = new File(Environment.getExternalStorageDirectory(), "性别" + ".jpg");
        try {
            clearIOStream(new FileOutputStream(testFile), data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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

    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        /*if (name == null) {
            test();
            return;
        }*/

        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                dialog.cancel();

                nameText.setText(nameResult);
                genderText.setText(genderResult);
                peopleText.setText(peopleResult);
                dateText.setText(dateResult);
                addressText.setText(addressResult);
                numberText.setText(numberResult);

            }
        };

        photo = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/身份证" + ".jpg");
        imageView = (ImageView) findViewById(R.id.photo);
        imageView.setImageBitmap(photo);

        nameText = (EditText) findViewById(R.id.name);
        genderText = (EditText) findViewById(R.id.gender);
        peopleText = (EditText) findViewById(R.id.people);
        dateText = (EditText) findViewById(R.id.date);
        addressText = (EditText) findViewById(R.id.address);
        numberText = (EditText) findViewById(R.id.number);

        dialog = new ProgressDialog(this);
        dialog.setMessage("识别中");
        dialog.show();

        // tesseract .jpg output_1 -l eng

        new Thread(new Runnable() {
            @Override
            public void run() {

                name = ImageFilter.gray2Binary(ImageFilter.decodeFile
                        (Environment.getExternalStorageDirectory() + "/姓名" + ".jpg"), 105);
                gender = ImageFilter.gray2Binary(ImageFilter.decodeFile
                        (Environment.getExternalStorageDirectory() + "/性别" + ".jpg"), 105);
                people = ImageFilter.gray2Binary(ImageFilter.decodeFile
                        (Environment.getExternalStorageDirectory() + "/民族" + ".jpg"), 115);
                date = ImageFilter.gray2Binary(ImageFilter.lineGrey(ImageFilter.decodeFile
                        (Environment.getExternalStorageDirectory() + "/日期" + ".jpg")), 105);
                address = ImageFilter.gray2Binary(ImageFilter.decodeFile
                        (Environment.getExternalStorageDirectory() + "/住址" + ".jpg"), 105);
                number = ImageFilter.gray2Binary(ImageFilter.decodeFile
                        (Environment.getExternalStorageDirectory() + "/身份证号" + ".jpg"), 105);

                nameResult = doOcr(name, "/chi_sim");
                genderResult = doOcr(gender, "/chi_sim");
                peopleResult = doOcr(people, "/chi_sim");
                dateResult = doOcr(date, "/chi_sim");
                addressResult = doOcr(address, "/chi_sim");
                numberResult = doOcr(number, "/chi_sim");

                if (nameResult != null
                        && genderResult != null
                        && peopleResult != null
                        && dateResult != null
                        && addressResult != null
                        && numberResult != null) {

                    Log.e("[][][][]", "姓名:" + nameResult);
                    Log.e("[][][][]", "性别:" + genderResult);
                    Log.e("[][][][]", "民族:" + peopleResult);
                    Log.e("[][][][]", "日期:" + dateResult);
                    Log.e("[][][][]", "住址:" + addressResult);
                    Log.e("[][][][]", "证号:" + numberResult);

                    Message msg = new Message();
                    handler.sendMessage(msg);
                } else Toast.makeText(ResultActivity.this, "识别出错！", Toast.LENGTH_SHORT).show();
            }
        }).start();
    }

    /**
     * 进行图片识别
     *
     * @param bitmap   待识别图片
     * @param language 识别语言
     * @return 识别结果字符串
     */
    public String doOcr(Bitmap bitmap, String language) {
        TessBaseAPI baseApi = new TessBaseAPI();

        baseApi.init(getSDPath(), language);

        // 必须加此行，tess-two要求BMP必须为此配置
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        baseApi.setImage(bitmap);

        String text = baseApi.getUTF8Text();

        baseApi.clear();
        baseApi.end();

        return text;
    }

    /**
     * 获取sd卡的路径
     *
     * @return 路径的字符串
     */
    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        }
        return sdDir.toString();
    }

}
