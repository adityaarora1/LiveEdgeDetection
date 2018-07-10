package com.adityaarora.liveedgedetection.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
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
import android.view.View;
import android.widget.FrameLayout;

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
import java.util.List;

import static org.opencv.core.CvType.CV_8UC1;

/**
 * This class previews the live images from the camera
 */

public class ScanSurfaceView extends FrameLayout implements SurfaceHolder.Callback {
    private static final String TAG = ScanSurfaceView.class.getSimpleName();
    SurfaceView mSurfaceView;
    private final ScanCanvasView scanCanvasView;

    private final Context context;
    private Camera camera;

    private final IScanner iScanner;
    private CountDownTimer autoCaptureTimer;
    private int secondsLeft;
    private boolean isAutoCaptureScheduled;
    private Camera.Size previewSize;
    private boolean isCapturing = false;

    public ScanSurfaceView(Context context, ScanCanvasView scanCanvasView, IScanner iScanner) {
        super(context);
        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);
        this.context = context;
        this.scanCanvasView = scanCanvasView;
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(this);
        this.iScanner = iScanner;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            requestLayout();
            openCamera();
            this.camera.setPreviewDisplay(holder);
            setPreviewCallback();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void openCamera() {
        if (camera == null) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            int defaultCameraId = 0;
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    defaultCameraId = i;
                }
            }
            camera = Camera.open(defaultCameraId);
            Camera.Parameters cameraParams = camera.getParameters();

            List<String> flashModes = cameraParams.getSupportedFlashModes();
            if (null != flashModes && flashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            }

            if (cameraParams.getSupportedFocusModes() != null
                    && cameraParams.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                cameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (cameraParams.getSupportedFocusModes() != null
                    && cameraParams.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                cameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }

            camera.setParameters(cameraParams);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (previewSize == null) {
            previewSize = camera.getParameters().getPreviewSize();
        }
        Camera.Parameters parameters = camera.getParameters();
        camera.setDisplayOrientation(ScanUtils.configureCameraAngle((Activity) context));
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        camera.setParameters(parameters);
        requestLayout();
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

    public void setPreviewCallback() {
        this.camera.startPreview();
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
        path.moveTo(previewWidth - (float) points[0].y, (float) points[0].x);
        path.lineTo(previewWidth - (float) points[1].y, (float) points[1].x);
        path.lineTo(previewWidth - (float) points[2].y, (float) points[2].x);
        path.lineTo(previewWidth - (float) points[3].y, (float) points[3].x);
        path.close();

        double area = Math.abs(Imgproc.contourArea(approx));
        Log.i(TAG, "Contour Area: " + String.valueOf(area));

        PathShape newBox = new PathShape(path, previewWidth, previewHeight);
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

        if (imgDetectionPropsObj.isDetectedAreaBeyondLimits()) {
            scanHint = ScanHint.FIND_RECT;
            cancelAutoCapture();
        } else if (imgDetectionPropsObj.isDetectedAreaBelowLimits()) {
            cancelAutoCapture();
            if (imgDetectionPropsObj.isEdgeTouching()) {
                scanHint = ScanHint.MOVE_AWAY;
            } else {
                scanHint = ScanHint.MOVE_CLOSER;
            }
        } else if (imgDetectionPropsObj.isDetectedHeightAboveLimit()) {
            cancelAutoCapture();
            scanHint = ScanHint.MOVE_AWAY;
        } else if (imgDetectionPropsObj.isDetectedWidthAboveLimit() || imgDetectionPropsObj.isDetectedAreaAboveLimit()) {
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

                if (!isAutoCaptureScheduled) {
                    scheduleAutoCapture(scanHint);
                }
            }
        }
        Log.i(TAG, "Preview Area 95%: " + 0.95 * previewArea +
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
        autoCaptureTimer = new CountDownTimer(2000, 100) {
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
        if (isCapturing) return;
        if (ScanHint.CAPTURING_IMAGE.equals(scanHint)) {
            try {
                isCapturing = true;
                iScanner.displayHint(ScanHint.CAPTURING_IMAGE);
                camera.setPreviewCallback(null);

                Camera.Parameters params = camera.getParameters();
                Camera.Size size = ScanUtils.determinePictureSize(camera, params.getPreviewSize());
                params.setPictureSize(size.width, size.height);
                params.setPictureFormat(ImageFormat.JPEG);
                camera.setParameters(params);

                camera.takePicture(mShutterCallBack, null, pictureCallback);
                iScanner.displayHint(ScanHint.NO_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void cancelAutoCapture() {
        if (isAutoCaptureScheduled) {
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
                paintColor = Color.argb(30, 255, 38, 0);
                borderColor = Color.rgb(255, 38, 0);
                break;
            case FIND_RECT:
                paintColor = Color.argb(0, 0, 0, 0);
                borderColor = Color.argb(0, 0, 0, 0);
                break;
            case CAPTURING_IMAGE:
                paintColor = Color.argb(30, 38, 216, 76);
                borderColor = Color.rgb(38, 216, 76);
                break;
        }

        paint.setColor(paintColor);
        border.setColor(borderColor);
    }

    private final Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.stopPreview();
            iScanner.displayHint(ScanHint.NO_MESSAGE);

            Bitmap bitmap = ScanUtils.decodeBitmapFromByteArray(data,
                    ScanConstants.HIGHER_SAMPLING_THRESHOLD, ScanConstants.HIGHER_SAMPLING_THRESHOLD);

            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            iScanner.onPictureClicked(bitmap);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    isCapturing = false;
                }
            }, 3000);

        }
    };

    private final Camera.ShutterCallback mShutterCallBack = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            if (context != null) {
                AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                if (null != mAudioManager)
                    mAudioManager.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
            }
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
        previewSize = ScanUtils.getOptimalPreviewSize(camera, width, height);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            final View child = mSurfaceView;

            int width = r - l;
            int height = b - t;

            int previewWidth = width;
            int previewHeight = height;

            if (previewSize != null) {
                previewWidth = previewSize.width;
                previewHeight = previewSize.height;

                int displayOrientation = ScanUtils.configureCameraAngle((Activity) context);
                if (displayOrientation == 90 || displayOrientation == 270) {
                    previewWidth = previewSize.height;
                    previewHeight = previewSize.width;
                }

                Log.d(TAG, "previewWidth:" + previewWidth + " previewHeight:" + previewHeight);
            }

            int nW;
            int nH;
            int top;
            int left;

            float scale = 1.0f;

            // Center the child SurfaceView within the parent.
            if (width * previewHeight < height * previewWidth) {
                Log.d(TAG, "center horizontally");
                int scaledChildWidth = (int) ((previewWidth * height / previewHeight) * scale);
                nW = (width + scaledChildWidth) / 2;
                nH = (int) (height * scale);
                top = 0;
                left = (width - scaledChildWidth) / 2;
            } else {
                Log.d(TAG, "center vertically");
                int scaledChildHeight = (int) ((previewHeight * width / previewWidth) * scale);
                nW = (int) (width * scale);
                nH = (height + scaledChildHeight) / 2;
                top = (height - scaledChildHeight) / 2;
                left = 0;
            }
            child.layout(left, top, nW, nH);

            Log.d("layout", "left:" + left);
            Log.d("layout", "top:" + top);
            Log.d("layout", "right:" + nW);
            Log.d("layout", "bottom:" + nH);
        }
    }
}
