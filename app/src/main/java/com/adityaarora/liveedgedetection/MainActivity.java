package com.adityaarora.liveedgedetection;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.adityaarora.liveedgedetection.activity.ScanActivity;
import com.adityaarora.liveedgedetection.constants.ScanConstants;
import com.adityaarora.liveedgedetection.util.ScanUtils;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 101;
    private ImageView scannedImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scannedImageView = findViewById(R.id.scanned_image);

        // let's see if Crashlytics can catch the error now
        new Handler().postDelayed(this::startScan, 2000);
    }

    private void startScan() {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (null != data && null != data.getExtras()) {
                    String filePath = data.getExtras().getString(ScanConstants.SCANNED_RESULT);
                    Bitmap baseBitmap = ScanUtils.decodeBitmapFromFile(filePath, ScanConstants.IMAGE_NAME);
                    scannedImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    scannedImageView.setImageBitmap(baseBitmap);
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                finish();
            }
        }
    }
}
