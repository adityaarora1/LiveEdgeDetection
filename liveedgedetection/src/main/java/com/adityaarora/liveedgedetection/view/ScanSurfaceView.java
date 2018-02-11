package com.adityaarora.liveedgedetection.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.shapes.PathShape;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.adityaarora.liveedgedetection.constants.ScanConstants;
import com.adityaarora.liveedgedetection.enums.ScanHint;
import com.adityaarora.liveedgedetection.interfaces.IScanner;
import com.adityaarora.liveedgedetection.util.ImageDetectionProperties;
import com.adityaarora.liveedgedetection.util.ScanUtils;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.opencv.core.CvType.CV_8UC1;

/**
 * This class previews the live images from the camera
 */

public class ScanSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = ScanSurfaceView.class.getSimpleName();
    private final ScanCanvasView scanCanvasView;

    private final Context context;
    private Camera camera;

    private List<Camera.Size> previewSizeList;
    private List<Camera.Size> pictureSizeList;

    private final IScanner iScanner;
    private CountDownTimer autoCaptureTimer;
    private int secondsLeft;
    private boolean isAutoCaptureScheduled;

    public ScanSurfaceView(Context context, ScanCanvasView scanCanvasView,
                           IScanner iScanner) {
        super(context);
        this.context = context;
        this.scanCanvasView = scanCanvasView;
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        this.iScanner = iScanner;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            openCamera();
            this.camera.setPreviewDisplay(holder);
            this.camera.startPreview();
            setPreviewCallback();
            Camera.Parameters cameraParams = camera.getParameters();

            previewSizeList = cameraParams.getSupportedPreviewSizes();
            pictureSizeList = cameraParams.getSupportedPictureSizes();
            Collections.sort(pictureSizeList, new Comparator<Camera.Size>() {

                public int compare(final Camera.Size a, final Camera.Size b) {
                    return b.width * b.height - a.width * a.height;
                }
            });

            List<String> flashModes = cameraParams.getSupportedFlashModes();
            if (null != flashModes && flashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            }

            if (cameraParams.getSupportedFocusModes() != null && cameraParams.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                cameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (cameraParams.getSupportedFocusModes() != null && cameraParams.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                cameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            }

            camera.setParameters(cameraParams);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void openCamera() {
        if(camera == null) {
            camera = Camera.open();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Size previewSize = ScanUtils.getOptimalPreviewSize(width, height, previewSizeList);
        if (previewSize == null) {
            previewSize = camera.getParameters().getPreviewSize();
        }
        Camera.Size pictureSize = ScanUtils.determinePictureSize(previewSize, pictureSizeList);
        if (pictureSize == null) {
            pictureSize = camera.getParameters().getPictureSize();
        }

        this.getHolder().setFixedSize(previewSize.height, previewSize.width);

        Camera.Parameters cameraParams = camera.getParameters();

        camera.setDisplayOrientation(ScanUtils.configureCameraAngle((Activity) context));

        cameraParams.setPreviewSize(previewSize.width, previewSize.height);
        cameraParams.setPictureSize(pictureSize.width, pictureSize.height);

        camera.setParameters(cameraParams);
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPreviewAndFreeCamera();
    }

    private void stopPreviewAndFreeCamera() {

        if (camera != null) {
            // Call stopPreview() to stop updating the preview surface.
            camera.stopPreview();
            camera.setPreviewCallback(null);
            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            camera.release();
            camera = null;
        }
    }

    private void setPreviewCallback() {
        this.camera.setPreviewCallback(previewCallback);
    }

    private final Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (null != camera) {
                try {
                    Camera.Size pictureSize = camera.getParameters().getPreviewSize();
                    Log.d(TAG, "onPreviewFrame - received image " + pictureSize.width + "x" + pictureSize.height);

                    Mat yuv = new Mat(new Size(pictureSize.width, pictureSize.height * 1.5), CV_8UC1);
                    yuv.put(0, 0, data);

                    Mat mat = new Mat(new Size(pictureSize.width, pictureSize.height), CvType.CV_8UC4);
                    Imgproc.cvtColor(yuv, mat, Imgproc.COLOR_YUV2BGR_NV21, 4);
                    yuv.release();

                    Size originalPreviewSize = mat.size();
                    int originalPreviewArea = mat.rows() * mat.cols();

                    Quadrilateral largestQuad = ScanUtils.detectLargestQuadrilateral(mat);
                    iScanner.clearAndInvalidateCanvas();

                    mat.release();

                    if (null != largestQuad) {
                            drawLargestRect(largestQuad.contour, largestQuad.points, originalPreviewSize, originalPreviewArea);
                    } else {
                        showFindingReceiptHint();
                    }
                } catch (Exception e) {
                    showFindingReceiptHint();
                }
            }
        }
    };

    private void drawLargestRect(MatOfPoint2f approx, Point[] points, Size stdSize, int previewArea) {
        Path path = new Path();
        // ATTENTION: axis are swapped
        float previewWidth = (float) stdSize.height;
        float previewHeight = (float) stdSize.width;

        Log.i(TAG, "previewWidth: " + String.valueOf(previewWidth));
        Log.i(TAG, "previewHeight: " + String.valueOf(previewHeight));

        //Points are drawn in anticlockwise direction
        path.moveTo( previewWidth - (float) points[0].y, (float) points[0].x );
        path.lineTo( previewWidth - (float) points[1].y, (float) points[1].x );
        path.lineTo( previewWidth - (float) points[2].y, (float) points[2].x );
        path.lineTo( previewWidth - (float) points[3].y, (float) points[3].x );
        path.close();

        double area = Math.abs(Imgproc.contourArea(approx));
        Log.i(TAG, "Contour Area: " + String.valueOf(area));

        PathShape newBox = new PathShape(path , previewWidth , previewHeight);
        Paint paint = new Paint();
        Paint border = new Paint();

        //Height calculated on Y axis
        double resultHeight = points[1].x - points[0].x;
        double bottomHeight = points[2].x - points[3].x;
        if (bottomHeight > resultHeight)
            resultHeight = bottomHeight;

        //Width calculated on X axis
        double resultWidth = points[3].y - points[0].y;
        double bottomWidth = points[2].y - points[1].y;
        if (bottomWidth > resultWidth)
            resultWidth = bottomWidth;

        Log.i(TAG, "resultWidth: " + String.valueOf(resultWidth));
        Log.i(TAG, "resultHeight: " + String.valueOf(resultHeight));

        ImageDetectionProperties imgDetectionPropsObj
                = new ImageDetectionProperties(previewWidth, previewHeight, resultWidth, resultHeight,
                previewArea, area, points[0], points[1], points[2], points[3]);

        final ScanHint scanHint;

        if(imgDetectionPropsObj.isDetectedAreaBeyondLimits()) {
            scanHint = ScanHint.FIND_RECT;
            cancelAutoCapture();
        } else if (imgDetectionPropsObj.isDetectedAreaBelowLimits()) {
            cancelAutoCapture();
            if(imgDetectionPropsObj.isEdgeTouching()) {
                scanHint = ScanHint.MOVE_AWAY;
            } else {
                scanHint = ScanHint.MOVE_CLOSER;
            }
        } else if(imgDetectionPropsObj.isDetectedHeightAboveLimit()) {
            cancelAutoCapture();
            scanHint = ScanHint.MOVE_AWAY;
        } else if(imgDetectionPropsObj.isDetectedWidthAboveLimit() || imgDetectionPropsObj.isDetectedAreaAboveLimit()) {
            cancelAutoCapture();
            scanHint = ScanHint.MOVE_AWAY;
        } else {
            if (imgDetectionPropsObj.isEdgeTouching()) {
                cancelAutoCapture();
                scanHint = ScanHint.MOVE_AWAY;
            } else if (imgDetectionPropsObj.isAngleNotCorrect(approx)) {
                cancelAutoCapture();
                scanHint = ScanHint.ADJUST_ANGLE;
            } else {
                Log.i(TAG, "GREEN" + "(resultWidth/resultHeight) > 4: " + (resultWidth / resultHeight) +
                        " points[0].x == 0 && points[3].x == 0: " + points[0].x + ": " + points[3].x +
                        " points[2].x == previewHeight && points[1].x == previewHeight: " + points[2].x + ": " + points[1].x +
                        "previewHeight: " + previewHeight);
                scanHint = ScanHint.CAPTURING_IMAGE;
                iScanner.clearAndInvalidateCanvas();

                if(!isAutoCaptureScheduled) {
                    scheduleAutoCapture(scanHint);
                }
            }
        }
        Log.i(TAG,"Preview Area 95%: " + 0.95 * previewArea +
                " Preview Area 20%: " + 0.20 * previewArea +
                " Area: " + String.valueOf(area) +
                " Label: " + scanHint.toString());

        border.setStrokeWidth(12);
        iScanner.displayHint(scanHint);
        setPaintAndBorder(scanHint, paint, border);
        scanCanvasView.clear();
        scanCanvasView.addShape(newBox, paint, border);
        iScanner.invalidateCanvas();
    }

    private void scheduleAutoCapture(final ScanHint scanHint) {
        isAutoCaptureScheduled = true;
        secondsLeft = 0;
        autoCaptureTimer = new CountDownTimer(3000, 100) {
            public void onTick(long millisUntilFinished) {
                if (Math.round((float) millisUntilFinished / 1000.0f) != secondsLeft) {
                    secondsLeft = Math.round((float) millisUntilFinished / 1000.0f);
                }
                Log.v(TAG, "" + millisUntilFinished / 1000);
                switch (secondsLeft) {
                    case 1:
                        autoCapture(scanHint);
                        break;
                    default:
                        break;
                }
            }

            public void onFinish() {
                isAutoCaptureScheduled = false;
            }
        };
        autoCaptureTimer.start();
    }

    private void autoCapture(ScanHint scanHint) {
        if(ScanHint.CAPTURING_IMAGE.equals(scanHint)) {
            camera.setPreviewCallback(null);
            camera.takePicture(mShutterCallBack, null, pictureCallback);
            iScanner.displayHint(ScanHint.NO_MESSAGE);
        }
    }

    private void cancelAutoCapture() {
        if(isAutoCaptureScheduled) {
            isAutoCaptureScheduled = false;
            if (null != autoCaptureTimer) {
                autoCaptureTimer.cancel();
            }
        }
    }

    private void showFindingReceiptHint() {
        iScanner.displayHint(ScanHint.FIND_RECT);
        iScanner.clearAndInvalidateCanvas();
    }

    private void setPaintAndBorder(ScanHint scanHint, Paint paint, Paint border) {
        int paintColor = 0;
        int borderColor = 0;

        switch (scanHint) {
            case MOVE_CLOSER:
            case MOVE_AWAY:
            case ADJUST_ANGLE:
                paintColor  = Color.argb(30, 255, 38, 0);
                borderColor = Color.rgb(255, 38, 0);
                break;
            case FIND_RECT:
                paintColor  = Color.argb(0, 0, 0, 0);
                borderColor = Color.argb(0, 0, 0, 0);
                break;
            case CAPTURING_IMAGE:
                paintColor  = Color.argb(30, 38, 216, 76);
                borderColor = Color.rgb(38, 216, 76);
                break;
        }

        paint.setColor(paintColor);
        border.setColor(borderColor);
    }

    private final Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);

            Bitmap bitmap = ScanUtils.loadEfficientBitmap(data,
                    ScanConstants.LOWER_SAMPLING_THRESHOLD, ScanConstants.HIGHER_SAMPLING_THRESHOLD);

            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap = ScanUtils.resize(bitmap,
                    ScanConstants.LOWER_SAMPLING_THRESHOLD, ScanConstants.HIGHER_SAMPLING_THRESHOLD);

            iScanner.onPictureClicked(bitmap);

        }
    };

    private final Camera.ShutterCallback mShutterCallBack = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            if (context != null) {
                AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                if(null != mAudioManager)
                    mAudioManager.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
            }
        }
    };
}
