package com.adityaarora.liveedgedetection.interfaces;

import android.graphics.Bitmap;

import com.adityaarora.liveedgedetection.enums.ScanHint;

/**
 * Interface between activity and surface view
 */

public interface IScanner {
    void displayHint(ScanHint scanHint);
    void clearAndInvalidateCanvas();
    void invalidateCanvas();
    void onPictureClicked(Bitmap bitmap);
}
