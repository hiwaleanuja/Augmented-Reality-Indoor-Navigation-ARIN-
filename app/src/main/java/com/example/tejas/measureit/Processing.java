package com.example.tejas.measureit;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;

public class Processing {

    private Mat hsv;
    private Mat mask1;
    private Mat mask2;
    private Mat mask;
    private MatOfPoint2f ROI;

    public static final int RED = 1;
    public static final int GREEN = 2;
    public static final int SKEWED = 1;
    public static final int ROTATED = 2;

    private static final String TAG = "Processing";

    public Processing(){
        this.hsv = new Mat();
        this.mask = new Mat();
        this.mask1 = new Mat();
        this.mask2 = new Mat();
        this.ROI = new MatOfPoint2f();
    }

    public Mat findROI(Mat inputMat, int color){

        if(color == RED) {
            //Define a range to subtract Red Color from image.
            Imgproc.cvtColor(inputMat, hsv, Imgproc.COLOR_RGB2HSV);
            Core.inRange(hsv, new Scalar(0, 70, 50), new Scalar(10, 255, 255), mask1);
            Core.inRange(hsv, new Scalar(170, 70, 50), new Scalar(180, 255, 255), mask2);
            Core.add(mask1, mask2, mask);
            Log.i(TAG, "Mask Created");
        }
        else if(color == GREEN){
            Imgproc.cvtColor(inputMat, hsv, Imgproc.COLOR_RGB2HSV);
            Core.inRange(hsv, new Scalar(35,100,50), new Scalar(85,255,255), mask);
        }

        //Noise reduction.
        Mat kernelOpen = Mat.ones(new Size(10,10), CvType.CV_8U);
        Mat kernelClose = Mat.ones(new Size(20,20), CvType.CV_8U);

        Mat opening = new Mat();

        Imgproc.morphologyEx(mask, opening, Imgproc.MORPH_OPEN, kernelOpen);
        Imgproc.morphologyEx(opening, mask, Imgproc.MORPH_CLOSE, kernelClose);
        Log.i(TAG, "Noise Removed");

        return mask;
    }

    public MatOfPoint findROIContours(Mat Mask){

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Log.i(TAG, "Init");
        Imgproc.findContours(
                Mask,
                contours,
                hierarchy,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_NONE
        );



        return contours.get(0);
    }

    public Point[] getRectangle(MatOfPoint ROIContours, int orientation){
        Point[] rect = null;
        MatOfPoint2f contours = new MatOfPoint2f(ROIContours.toArray());

        if(orientation == SKEWED){

            double epsilon = 0.1 * Imgproc.arcLength(contours,true);
            Imgproc.approxPolyDP(contours, ROI, epsilon, true);

            MatOfPoint poly = new MatOfPoint();
            ROI.convertTo(poly, CvType.CV_32S);

            rect = orderPoints(poly.toArray());

            Log.i(TAG, "Rectangle Returned");

        }else if(orientation == ROTATED){
            RotatedRect rotatedRect = Imgproc.minAreaRect(contours);
            Log.i(TAG, "Rectangle Found");

            Point[] vertices = new Point[4];
            rotatedRect.points(vertices);

            rect = vertices;
        }

        return rect;
    }

    public Point[] getRectangleSkewed(MatOfPoint ROIContours){

        MatOfPoint2f contours = new MatOfPoint2f(ROIContours.toArray());

        double epsilon = 0.1 * Imgproc.arcLength(contours,true);
        Imgproc.approxPolyDP(contours, ROI, epsilon, true);

        MatOfPoint poly = new MatOfPoint();
        ROI.convertTo(poly, CvType.CV_32S);

        Point[] rect = orderPoints(poly.toArray());

        Log.i(TAG, "Rectangle Returned");

        return rect;
    }

    public Point[] getRectangleRotated(MatOfPoint ROIContours){

        MatOfPoint2f ROI = new MatOfPoint2f(ROIContours.toArray());
        RotatedRect rotatedRect = Imgproc.minAreaRect(ROI);
        Log.i(TAG, "Rectangle Found");

        Point[] vertices = new Point[4];
        rotatedRect.points(vertices);

        return vertices;
    }

    public Mat getTransformation(Point[] rect){
        Point topLeft     = rect[0];
        Point topRight    = rect[1];
        Point bottomRight = rect[2];
        Point bottomLeft  = rect[3];

        double widthA = eucDistance(topRight, topLeft);
        double widthB = eucDistance(bottomRight, bottomLeft);
        double maxWidth = Math.max(widthA, widthB);

        double heightA = eucDistance(topLeft, bottomLeft);
        double heightB = eucDistance(topRight, bottomRight);
        double maxHeight = Math.max(heightA, heightB);

        List<Point> src = new ArrayList<>();
        for(int i = 0; i < 4 ; i++){
            src.add(rect[i]);
        }

        List<Point> dst = new ArrayList<>();
        dst.add(new Point(0,0));
        dst.add(new Point(maxWidth-1,0));
        dst.add(new Point(maxWidth-1,maxHeight-1));
        dst.add(new Point(0,maxHeight));


        Mat srcMat = Converters.vector_Point2f_to_Mat(src);
        Mat dstMat = Converters.vector_Point2f_to_Mat(dst);

        Mat M = Imgproc.getPerspectiveTransform(srcMat, dstMat);

        return M;
    }

    public double getMarkerLength(Point[] rect){
        Point topLeft     = rect[0];
        Point topRight    = rect[1];
        Point bottomRight = rect[2];
        Point bottomLeft  = rect[3];

        double widthA = eucDistance(topRight, topLeft);
        double widthB = eucDistance(bottomRight, bottomLeft);
        double maxWidth = Math.max(widthA, widthB);

        double heightA = eucDistance(topLeft, bottomLeft);
        double heightB = eucDistance(topRight, bottomRight);
        double maxHeight = Math.max(heightA, heightB);

        return maxWidth;
    }

    public Point[] transformPoints(Point[] points, Mat M){


        Mat src = new Mat(new Size(1,(points.length)), CvType.CV_32FC2);
        Mat dst = new Mat(new Size(1,(points.length)), CvType.CV_32FC2);
        for( int i = 0; i < points.length; i++){
            src.put(i, 0, new double[]{points[i].x, points[i].y});
        }

        Core.perspectiveTransform(src, dst, M);

        Point p1 = new Point(dst.get(0,0)[0], dst.get(0,0)[1]);
        Point p2 = new Point(dst.get(1,0)[0], dst.get(1,0)[1]);
        Point[] convertedPoints = new Point[]{p1, p2};


        return convertedPoints;
    }

    private Point[] orderPoints(Point[] rect){
        //This functions makes sure that the ordering of points is according to the convention.
        Point[] orderedRect = new Point[4];
        int A = 0, B = 0, C = 0, D = 0;

        double minSum = rect[0].x + rect[0].y;
        double maxSum = rect[0].x + rect[0].y;

        double minDiff = rect[0].y - rect[0].x;
        double maxDiff = rect[0].y - rect[0].x;

        double currentSum = 0;
        double currentDiff = 0;

        for(int i=0; i<4; i++){
            currentSum = rect[i].x + rect[i].y;
            currentDiff = rect[i].y - rect[i].x;

            if(currentSum <= minSum){
                minSum = currentSum;
                A = i;
            }
            if(currentSum > maxSum){
                maxSum = currentSum;
                C = i;
            }

            if(currentDiff <= minDiff){
                maxDiff = currentDiff;
                B = i;
            }
            if(currentDiff > maxDiff){
                maxDiff = currentDiff;
                D = i;
            }
        }

        orderedRect[0] = new Point(rect[A].x, rect[A].y);   //TopLeft
        orderedRect[1] = new Point(rect[B].x, rect[B].y);   //TopRight
        orderedRect[2] = new Point(rect[C].x, rect[C].y);   //BottomLeft
        orderedRect[3] = new Point(rect[D].x, rect[D].y);   //BottomRight

        Log.i(TAG, "Points Ordered");

        return orderedRect;
    }

    public double eucDistance(Point A, Point B){
        return Math.sqrt(
                Math.pow((A.x - B.x) , 2) + Math.pow((A.y - B.y) , 2)
        );
    }


}
