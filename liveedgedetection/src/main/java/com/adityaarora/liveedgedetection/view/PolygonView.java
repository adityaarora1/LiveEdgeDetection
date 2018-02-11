package com.adityaarora.liveedgedetection.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.adityaarora.liveedgedetection.R;
import com.adityaarora.liveedgedetection.activity.ScanActivity;
import com.adityaarora.liveedgedetection.util.ScanUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class defines polygon for cropping
 */
public class PolygonView extends FrameLayout {

    private static final String TAG = PolygonView.class.getSimpleName();
    private final Context context;
    private Paint paint;
    private ImageView pointer1;
    private ImageView pointer2;
    private ImageView pointer3;
    private ImageView pointer4;
    private ImageView midPointer13;
    private ImageView midPointer12;
    private ImageView midPointer34;
    private ImageView midPointer24;
    private PolygonView polygonView;
    private Paint circleFillPaint;

    public PolygonView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public PolygonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public PolygonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        polygonView = this;
        pointer1 = getImageView(0, 0);
        pointer2 = getImageView(getWidth(), 0);
        pointer3 = getImageView(0, getHeight());
        pointer4 = getImageView(getWidth(), getHeight());
        midPointer13 = getImageViewTransparent(0, getHeight() / 2);
        midPointer13.setOnTouchListener(new MidPointTouchListenerImpl(pointer1, pointer3));

        midPointer12 = getImageViewTransparent(0, getWidth() / 2);
        midPointer12.setOnTouchListener(new MidPointTouchListenerImpl(pointer1, pointer2));

        midPointer34 = getImageViewTransparent(0, getWidth() / 2);
        midPointer34.setOnTouchListener(new MidPointTouchListenerImpl(pointer3, pointer4));

        midPointer24 = getImageViewTransparent(0, getHeight() / 2);
        midPointer24.setOnTouchListener(new MidPointTouchListenerImpl(pointer2, pointer4));

        addView(pointer1);
        addView(pointer2);
        addView(midPointer13);
        addView(midPointer12);
        addView(midPointer34);
        addView(midPointer24);
        addView(pointer3);
        addView(pointer4);

        initPaint();
    }

    @Override
    protected void attachViewToParent(View child, int index, ViewGroup.LayoutParams params) {
        super.attachViewToParent(child, index, params);
    }

    private void initPaint() {
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.crop_color));
        paint.setStrokeWidth(7);
        paint.setAntiAlias(true);

        circleFillPaint = new Paint();
        circleFillPaint.setStyle(Paint.Style.FILL);
        circleFillPaint.setColor(getResources().getColor(R.color.crop_color));
        circleFillPaint.setAntiAlias(true);
    }

    public Map<Integer, PointF> getPoints() {

        List<PointF> points = new ArrayList<>();
        points.add(new PointF(pointer1.getX(), pointer1.getY()));
        points.add(new PointF(pointer2.getX(), pointer2.getY()));
        points.add(new PointF(pointer3.getX(), pointer3.getY()));
        points.add(new PointF(pointer4.getX(), pointer4.getY()));

        return getOrderedPoints(points);
    }

    private Map<Integer, PointF> getOrderedPoints(List<PointF> points) {

        PointF centerPoint = new PointF();
        int size = points.size();
        for (PointF pointF : points) {
            centerPoint.x += pointF.x / size;
            centerPoint.y += pointF.y / size;
        }
        Map<Integer, PointF> orderedPoints = new HashMap<>();
        for (PointF pointF : points) {
            int index = -1;
            if (pointF.x < centerPoint.x && pointF.y < centerPoint.y) {
                index = 0;
            } else if (pointF.x > centerPoint.x && pointF.y < centerPoint.y) {
                index = 1;
            } else if (pointF.x < centerPoint.x && pointF.y > centerPoint.y) {
                index = 2;
            } else if (pointF.x > centerPoint.x && pointF.y > centerPoint.y) {
                index = 3;
            }
            orderedPoints.put(index, pointF);
        }
        return orderedPoints;
    }

    public void setPoints(Map<Integer, PointF> pointFMap) {
        if (pointFMap.size() == 4) {
            setPointsCoordinates(pointFMap);
        }
    }

    private void setPointsCoordinates(Map<Integer, PointF> pointFMap) {
        pointer1.setX(pointFMap.get(0).x);
        pointer1.setY(pointFMap.get(0).y);

        pointer2.setX(pointFMap.get(1).x);
        pointer2.setY(pointFMap.get(1).y);

        pointer3.setX(pointFMap.get(2).x);
        pointer3.setY(pointFMap.get(2).y);

        pointer4.setX(pointFMap.get(3).x);
        pointer4.setY(pointFMap.get(3).y);

        midPointer13.setX(pointer3.getX() - ((pointer3.getX() - pointer1.getX()) / 2));
        midPointer13.setY(pointer3.getY() - ((pointer3.getY() - pointer1.getY()) / 2));
        midPointer24.setX(pointer4.getX() - ((pointer4.getX() - pointer2.getX()) / 2));
        midPointer24.setY(pointer4.getY() - ((pointer4.getY() - pointer2.getY()) / 2));
        midPointer34.setX(pointer4.getX() - ((pointer4.getX() - pointer3.getX()) / 2));
        midPointer34.setY(pointer4.getY() - ((pointer4.getY() - pointer3.getY()) / 2));
        midPointer12.setX(pointer2.getX() - ((pointer2.getX() - pointer1.getX()) / 2));
        midPointer12.setY(pointer2.getY() - ((pointer2.getY() - pointer1.getY()) / 2));
    }

    public void resetPoints(PolygonPoints polygonPoints) {
        Log.v(TAG, "P1:" + pointer1.getX()+","+pointer1.getY()
                +"\n"+"P2:" + pointer2.getX()+","+pointer2.getY()
                +"\n"+"P3:" + pointer3.getX()+","+pointer3.getY()
                +"\n"+"P4:" + pointer4.getX()+","+pointer4.getY());
        pointer1.setX(polygonPoints.getTopLeftPoint().x);
        pointer1.setY(polygonPoints.getTopLeftPoint().y);

        pointer2.setX(polygonPoints.getTopRightPoint().x);
        pointer2.setY(polygonPoints.getTopRightPoint().y);

        pointer3.setX(polygonPoints.getBottomLeftPoint().x);
        pointer3.setY(polygonPoints.getBottomLeftPoint().y);

        pointer4.setX(polygonPoints.getBottomRightPoint().x);
        pointer4.setY(polygonPoints.getBottomRightPoint().y);

        polygonView.invalidate();
        Log.v(TAG, "P1:" + pointer1.getX()+","+pointer1.getY()
                +"\n"+"P2:" + pointer2.getX()+","+pointer2.getY()
                +"\n"+"P3:" + pointer3.getX()+","+pointer3.getY()
                +"\n"+"P4:" + pointer4.getX()+","+pointer4.getY());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        Paint bgPaint = new Paint();
        bgPaint.setColor(getResources().getColor(R.color.colorBlackThirtyFivePercentAlpha));
        bgPaint.setAntiAlias(true);

        Path path1 = drawOutTopRect(canvas);
        canvas.drawPath(path1, bgPaint);

        Path path4 = drawOutMiddleLeftRect();
        canvas.drawPath(path4, bgPaint);

        Path path5 = drawOutMiddleRightRect(canvas);
        canvas.drawPath(path5, bgPaint);

        Path path6 = drawOutBottomRect(canvas);
        canvas.drawPath(path6, bgPaint);

        canvas.drawLine(pointer1.getX() + (pointer1.getWidth() / 2), pointer1.getY() + (pointer1.getHeight() / 2), pointer3.getX() + (pointer3.getWidth() / 2), pointer3.getY() + (pointer3.getHeight() / 2), paint);
        canvas.drawLine(pointer1.getX() + (pointer1.getWidth() / 2), pointer1.getY() + (pointer1.getHeight() / 2), pointer2.getX() + (pointer2.getWidth() / 2), pointer2.getY() + (pointer2.getHeight() / 2), paint);
        canvas.drawLine(pointer2.getX() + (pointer2.getWidth() / 2), pointer2.getY() + (pointer2.getHeight() / 2), pointer4.getX() + (pointer4.getWidth() / 2), pointer4.getY() + (pointer4.getHeight() / 2), paint);
        canvas.drawLine(pointer3.getX() + (pointer3.getWidth() / 2), pointer3.getY() + (pointer3.getHeight() / 2), pointer4.getX() + (pointer4.getWidth() / 2), pointer4.getY() + (pointer4.getHeight() / 2), paint);
        midPointer13.setX(pointer3.getX() - ((pointer3.getX() - pointer1.getX()) / 2));
        midPointer13.setY(pointer3.getY() - ((pointer3.getY() - pointer1.getY()) / 2));
        midPointer24.setX(pointer4.getX() - ((pointer4.getX() - pointer2.getX()) / 2));
        midPointer24.setY(pointer4.getY() - ((pointer4.getY() - pointer2.getY()) / 2));
        midPointer34.setX(pointer4.getX() - ((pointer4.getX() - pointer3.getX()) / 2));
        midPointer34.setY(pointer4.getY() - ((pointer4.getY() - pointer3.getY()) / 2));
        midPointer12.setX(pointer2.getX() - ((pointer2.getX() - pointer1.getX()) / 2));
        midPointer12.setY(pointer2.getY() - ((pointer2.getY() - pointer1.getY()) / 2));

        int radius = ScanUtils.dp2px(context, 11);
        canvas.drawCircle(pointer1.getX() + (pointer1.getWidth() / 2), pointer1.getY() + (pointer1.getHeight() / 2), radius, circleFillPaint);
        canvas.drawCircle(pointer2.getX() + (pointer2.getWidth() / 2), pointer2.getY() + (pointer2.getHeight() / 2), radius, circleFillPaint);
        canvas.drawCircle(pointer3.getX() + (pointer3.getWidth() / 2), pointer3.getY() + (pointer3.getHeight() / 2), radius, circleFillPaint);
        canvas.drawCircle(pointer4.getX() + (pointer4.getWidth() / 2), pointer4.getY() + (pointer4.getHeight() / 2), radius, circleFillPaint);

        canvas.drawCircle(midPointer13.getX() + (midPointer13.getWidth() / 2), midPointer13.getY() + (midPointer13.getHeight() / 2), radius, circleFillPaint);
        canvas.drawCircle(midPointer24.getX() + (midPointer24.getWidth() / 2), midPointer24.getY() + (midPointer24.getHeight() / 2), radius, circleFillPaint);
        canvas.drawCircle(midPointer34.getX() + (midPointer34.getWidth() / 2), midPointer34.getY() + (midPointer34.getHeight() / 2), radius, circleFillPaint);
        canvas.drawCircle(midPointer12.getX() + (midPointer12.getWidth() / 2), midPointer12.getY() + (midPointer12.getHeight() / 2), radius, circleFillPaint);
    }

    private Path drawOutBottomRect(Canvas canvas) {
        Path path = new Path();
        path.moveTo(0, canvas.getHeight());
        path.lineTo(canvas.getWidth(), canvas.getHeight());
        path.lineTo(canvas.getWidth(), pointer4.getY() + (pointer4.getHeight() / 2));
        path.lineTo(pointer4.getX() + (pointer4.getWidth() / 2), pointer4.getY() + (pointer4.getHeight() / 2));
        path.lineTo(pointer3.getX() + (pointer3.getWidth() / 2), pointer3.getY() + (pointer3.getHeight() / 2));
        path.lineTo(0, pointer3.getY() + (pointer3.getHeight() / 2));
        path.close();
        return path;
    }

    private Path drawOutMiddleRightRect(Canvas canvas) {
        Path path = new Path();
        path.moveTo(pointer2.getX() + (pointer2.getWidth() / 2), pointer2.getY() + (pointer2.getHeight() / 2));
        path.lineTo(canvas.getWidth(), pointer2.getY() + (pointer2.getHeight() / 2));
        path.lineTo(canvas.getWidth(), pointer4.getY() + (pointer4.getHeight() / 2));
        path.lineTo(pointer4.getX() + (pointer4.getWidth() / 2), pointer4.getY() + (pointer4.getHeight() / 2));
        path.close();
        return path;
    }

    private Path drawOutMiddleLeftRect() {
        Path path = new Path();
        path.moveTo(0, pointer1.getY() + (pointer1.getHeight() / 2));
        path.lineTo(pointer1.getX() + (pointer1.getWidth() / 2), pointer1.getY() + (pointer1.getHeight() / 2));
        path.lineTo(pointer3.getX() + (pointer3.getWidth() / 2), pointer3.getY() + (pointer3.getHeight() / 2));
        path.lineTo(0, pointer3.getY() + (pointer3.getHeight() / 2));
        path.close();
        return path;
    }

    private Path drawOutTopRect(Canvas canvas) {
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(canvas.getWidth(), 0);
        path.lineTo(canvas.getWidth(), pointer2.getY() + (pointer2.getHeight() / 2));
        path.lineTo(pointer2.getX() + (pointer2.getWidth() / 2), pointer2.getY() + (pointer2.getHeight() / 2));
        path.lineTo(pointer1.getX() + (pointer1.getWidth() / 2), pointer1.getY() + (pointer1.getHeight() / 2));
        path.lineTo(0, pointer1.getY() + (pointer1.getHeight() / 2));
        path.close();
        return path;
    }

    private ImageView getImageView(int x, int y) {
        ImageView imageView = new ImageView(context);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(layoutParams);
        imageView.setImageResource(R.drawable.circle);
        imageView.setX(x);
        imageView.setY(y);
        imageView.setOnTouchListener(new TouchListenerImpl());
        return imageView;
    }

    private ImageView getImageViewTransparent(int x, int y) {
        ImageView imageView = new ImageView(context);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(layoutParams);
        imageView.setImageResource(R.drawable.circle);
        imageView.setX(x);
        imageView.setY(y);
//        imageView.setOnTouchListener(new MidPointTouchListenerImpl());
        return imageView;
    }

    private class MidPointTouchListenerImpl implements OnTouchListener {

        final PointF DownPT = new PointF(); // Record Mouse Position When Pressed Down
        PointF StartPT = new PointF(); // Record Start Position of 'img'

        private final ImageView mainPointer1;
        private final ImageView mainPointer2;
        PointF latestPoint = new PointF();
        PointF latestPoint1 = new PointF();
        PointF latestPoint2 = new PointF();

        public MidPointTouchListenerImpl(ImageView mainPointer1, ImageView mainPointer2) {
            this.mainPointer1 = mainPointer1;
            this.mainPointer2 = mainPointer2;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int eid = event.getAction();
            switch (eid) {
                case MotionEvent.ACTION_MOVE:
                    PointF mv = new PointF(event.getX() - DownPT.x, event.getY() - DownPT.y);

                    if (Math.abs(mainPointer1.getX() - mainPointer2.getX()) > Math.abs(mainPointer1.getY() - mainPointer2.getY())) {
                        if (((mainPointer2.getY() + mv.y + v.getHeight() < polygonView.getHeight()) && (mainPointer2.getY() + mv.y > 0))) {
                            v.setX((int) (StartPT.y + mv.y));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer2.setY((int) (mainPointer2.getY() + mv.y));
                        }
                        if (((mainPointer1.getY() + mv.y + v.getHeight() < polygonView.getHeight()) && (mainPointer1.getY() + mv.y > 0))) {
                            v.setX((int) (StartPT.y + mv.y));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer1.setY((int) (mainPointer1.getY() + mv.y));
                        }
                    } else {
                        if ((mainPointer2.getX() + mv.x + v.getWidth() < polygonView.getWidth()) && (mainPointer2.getX() + mv.x > 0)) {
                            v.setX((int) (StartPT.x + mv.x));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer2.setX((int) (mainPointer2.getX() + mv.x));
                        }
                        if ((mainPointer1.getX() + mv.x + v.getWidth() < polygonView.getWidth()) && (mainPointer1.getX() + mv.x > 0)) {
                            v.setX((int) (StartPT.x + mv.x));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer1.setX((int) (mainPointer1.getX() + mv.x));
                        }
                    }

                    break;
                case MotionEvent.ACTION_DOWN:
                    ScanActivity.allDraggedPointsStack.push(new PolygonPoints(new PointF(pointer1.getX(), pointer1.getY()),
                            new PointF(pointer2.getX(), pointer2.getY()),
                            new PointF(pointer3.getX(), pointer3.getY()),
                            new PointF(pointer4.getX(), pointer4.getY())));
                    DownPT.x = event.getX();
                    DownPT.y = event.getY();
                    StartPT = new PointF(v.getX(), v.getY());
                    latestPoint = new PointF(v.getX(), v.getY());
                    latestPoint1 = new PointF(mainPointer1.getX(), mainPointer1.getY());
                    latestPoint2 = new PointF(mainPointer2.getX(), mainPointer2.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    int color = 0;
                    if (isValidShape(getPoints()) && isValidPointer1() && isValidPointer2() && isValidPointer3() && isValidPointer4()) {
                        color = getResources().getColor(R.color.crop_color);
                        latestPoint.x = v.getX();
                        latestPoint.y = v.getY();
                        latestPoint1.x = mainPointer1.getX();
                        latestPoint1.y = mainPointer1.getY();
                        latestPoint2.x = mainPointer2.getX();
                        latestPoint2.y = mainPointer2.getY();
                    } else {
                        ScanActivity.allDraggedPointsStack.pop();
                        color = getResources().getColor(R.color.crop_color);
                        v.setX(latestPoint.x);
                        v.setY(latestPoint.y);
                        mainPointer1.setX(latestPoint1.x);
                        mainPointer1.setY(latestPoint1.y);
                        mainPointer2.setX(latestPoint2.x);
                        mainPointer2.setY(latestPoint2.y);
                    }
                    paint.setColor(color);
                    break;
                default:
                    break;
            }
            polygonView.invalidate();
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    private boolean isValidShape(Map<Integer, PointF> pointFMap) {
        return pointFMap.size() == 4;
    }

    private boolean isValidPointer4() {
        return pointer4.getY() > pointer2.getY() && pointer4.getX() > pointer3.getX();
    }

    private boolean isValidPointer3() {
        return pointer3.getY() > pointer1.getY() && pointer3.getX() < pointer4.getX();
    }

    private boolean isValidPointer2() {
        return pointer2.getY() < pointer4.getY() && pointer2.getX() > pointer1.getX();
    }

    private boolean isValidPointer1() {
        return pointer1.getY() < pointer3.getY() && pointer1.getX() < pointer2.getX();
    }

    private boolean isValidMidPointerX() {
        return midPointer24.getX() > midPointer13.getX();
    }

    private boolean isValidMidPointerY() {
        return midPointer34.getY() > midPointer12.getY();
    }

    private class TouchListenerImpl implements OnTouchListener {

        final PointF DownPT = new PointF(); // Record Mouse Position When Pressed Down
        PointF StartPT = new PointF(); // Record Start Position of 'img'
        PointF latestPoint = new PointF();

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int eid = event.getAction();
            switch (eid) {
                case MotionEvent.ACTION_MOVE:
                    PointF mv = new PointF(event.getX() - DownPT.x, event.getY() - DownPT.y);
                    if (((StartPT.x + mv.x + v.getWidth()) < polygonView.getWidth() && (StartPT.y + mv.y + v.getHeight() < polygonView.getHeight())) && ((StartPT.x + mv.x) > 0 && StartPT.y + mv.y > 0)) {
                        v.setX((int) (StartPT.x + mv.x));
                        v.setY((int) (StartPT.y + mv.y));
                        StartPT = new PointF(v.getX(), v.getY());
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    ScanActivity.allDraggedPointsStack.push(new PolygonPoints(new PointF(pointer1.getX(), pointer1.getY()),
                            new PointF(pointer2.getX(), pointer2.getY()),
                            new PointF(pointer3.getX(), pointer3.getY()),
                            new PointF(pointer4.getX(), pointer4.getY())));
                    DownPT.x = event.getX();
                    DownPT.y = event.getY();
                    StartPT = new PointF(v.getX(), v.getY());
                    latestPoint = new PointF(v.getX(), v.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    int color = 0;
                    if (isValidShape(getPoints()) && isValidPointer4() && isValidPointer3() && isValidPointer2() && isValidPointer1()) {
                        color = getResources().getColor(R.color.crop_color);
                        latestPoint.x = v.getX();
                        latestPoint.y = v.getY();
                    } else {
                        ScanActivity.allDraggedPointsStack.pop();
                        color = getResources().getColor(R.color.crop_color);
                        v.setX(latestPoint.x);
                        v.setY(latestPoint.y);
                    }
                    paint.setColor(color);
                    break;
                default:
                    break;
            }
            polygonView.invalidate();
            return true;
        }
    }
}