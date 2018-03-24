package com.clown.ocr.two;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.clown.ocr.R;

/**
 * Created by Clown on 2017/07/27.
 */
public class MainActivity extends AppCompatActivity {

    private Button button;
    private CameraSurfaceView mCameraSurfaceView;
    // private RectOnCamera rectOnCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        // 全屏显示
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.cameraSurfaceView);
        button = (Button) findViewById(R.id.takePic);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCameraSurfaceView.takePicture();
                Toast.makeText(MainActivity.this, "抓拍！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*public void autoFocus() {
        mCameraSurfaceView.setAutoFocus();
    }*/

}
