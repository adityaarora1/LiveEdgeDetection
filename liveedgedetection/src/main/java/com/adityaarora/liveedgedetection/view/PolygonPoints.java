package com.adityaarora.liveedgedetection.view;

import android.graphics.PointF;

/**
 * Created by Aditya_Arora03 on 10/23/2017.
 */

public class PolygonPoints {
    PointF topLeftPoint;
    PointF topRightPoint;
    PointF bottomLeftPoint;
    PointF bottomRightPoint;

    public PolygonPoints(PointF topLeftPoint, PointF topRightPoint, PointF bottomLeftPoint, PointF bottomRightPoint) {
        this.topLeftPoint = topLeftPoint;
        this.topRightPoint = topRightPoint;
        this.bottomLeftPoint = bottomLeftPoint;
        this.bottomRightPoint = bottomRightPoint;
    }

    public PointF getTopLeftPoint() {
        return topLeftPoint;
    }

    public PointF getTopRightPoint() {
        return topRightPoint;
    }

    public PointF getBottomLeftPoint() {
        return bottomLeftPoint;
    }

    public PointF getBottomRightPoint() {
        return bottomRightPoint;
    }
}
