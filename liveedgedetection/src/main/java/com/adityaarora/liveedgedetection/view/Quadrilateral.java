package com.adityaarora.liveedgedetection.view;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

/**
 * This class defines detected quadrilateral
 */
public class Quadrilateral {
    public final MatOfPoint2f contour;
    public final Point[] points;

    public Quadrilateral(MatOfPoint2f contour, Point[] points) {
        this.contour = contour;
        this.points = points;
    }
}