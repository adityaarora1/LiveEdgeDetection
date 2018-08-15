package com.adityaarora.liveedgedetection.interfaces;

import android.graphics.Bitmap;

import com.adityaarora.liveedgedetection.enums.ScanHint;

/**
 * Interface between activity and surface view
 */

public interface IScanner {
    void displayHint(ScanHint scanHint);
    void onPictureClicked(Bitmap bitmap);
}
