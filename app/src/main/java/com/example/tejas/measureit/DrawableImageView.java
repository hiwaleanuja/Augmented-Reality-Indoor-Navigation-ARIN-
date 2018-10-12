package com.example.tejas.measureit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.opencv.core.Point;

public class DrawableImageView extends AppCompatImageView {


    private static final String TAG = "DrawActivity";

    private Paint InfoPaint;
    private Paint LinePaint;
    private Paint shaderPaint  = new Paint();

    private BitmapShader shader;
    private Bitmap bmp;
    Matrix matrix = new Matrix();

    //Flag to check if Zoomed view is to be displayed.
    boolean ZOOMING = false;

    //This variable will define the angle boundaries in which the line will be treated as perspective.

    private double sensitivityAngle = 5;

    PointF pointA = new PointF(435,550);
    PointF pointB = new PointF(830,550);

    PointF currentPoint = new PointF();
    PointF Midpoint = new PointF(); //Mid point of the line, to set text.
    PointF zoomView = new PointF(); //Point where the zoomed view will be displayed.
    PointF trans = new PointF();

    Double parallelLENGTH;    //Length of Rectangular marker in parallel view.
    Double perspectiveLENGTH; //Length of Rectangular marker in perspective view.
    int[] imgResolution;      //Resolution of the image captured.
    int[] scrResolution;      //Resolution of the Screen.


    //This variable will set the size of touch areas on each vertices.
    float sizeOfRect = 50f;

    //Rectangle to create a touch area around the Point A
    RectF ATouchArea = new RectF(
            pointA.x - sizeOfRect,
            pointA.y - sizeOfRect,
            pointA.x + sizeOfRect,
            pointA.y + sizeOfRect
    );

    //Rectangle to create a touch area around point B
    RectF BTouchArea = new RectF(
            pointB.x - sizeOfRect,
            pointB.y - sizeOfRect,
            pointB.x + sizeOfRect,
            pointB.y + sizeOfRect
    );

    private final int NONE = -1, TOUCH_A = 0, TOUCH_B = 1;
    int currentTouch = NONE;

    RectF src = new RectF();
    RectF dst = new RectF();

    double pixelDistance;       //To store the length of line in screen pixels.
    double centimeterDistance;  //To store the actual length of line in centimeters.

    private void init() {
        InfoPaint = new Paint();
        LinePaint = new Paint();

        //Paint object to display Information
        InfoPaint.setColor(Color.BLUE);
        InfoPaint.setStyle(Paint.Style.STROKE);
        InfoPaint.setStrokeWidth(3);
        InfoPaint.setTextSize(30);
        InfoPaint.setAntiAlias(true);
        InfoPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        //Paint object to draw Line
        LinePaint.setColor(Color.RED);
        LinePaint.setStyle(Paint.Style.STROKE);
        LinePaint.setStrokeWidth(7);
        LinePaint.setTextSize(30);
        LinePaint.setAntiAlias(true);
        LinePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        Log.i(TAG, "Init");
    }

    public DrawableImageView(Context context) {
        super(context);

    }

    public DrawableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Update the touch Areas with respect to the coordinates.
        ATouchArea.left   = pointA.x - sizeOfRect;
        ATouchArea.top    = pointA.y - sizeOfRect;
        ATouchArea.right  = pointA.x + sizeOfRect;
        ATouchArea.bottom = pointA.y + sizeOfRect;

        BTouchArea.left   = pointB.x - sizeOfRect;
        BTouchArea.top    = pointB.y - sizeOfRect;
        BTouchArea.right  = pointB.x + sizeOfRect;
        BTouchArea.bottom = pointB.y + sizeOfRect;

        //Get the pixel length of rectangular markers from MeasureImageActivity.
        perspectiveLENGTH = MeasureImageActivity.getInstance().getPerspectiveLength();
        parallelLENGTH = MeasureImageActivity.getInstance().getParallelLength();

        imgResolution = MeasureImageActivity.getInstance().getImgResolution();
        scrResolution = MeasureImageActivity.getInstance().getScrResolution();

        bmp = MeasureImageActivity.bitmapImage;

        Log.i(TAG, "\nWidth = " + bmp.getWidth());
        Log.i(TAG, "Height = " + bmp.getHeight());

        //As the resolution of image and screen may differ
        //It is necessary to perform scaling of both points accordingly.
        // (X1, Y1), (X2, Y2) Are scaled points.
        float X1 = pointA.x / scrResolution[0] * imgResolution[0];
        float Y1 = pointA.y / scrResolution[1] * imgResolution[1];

        float X2 = pointB.x / scrResolution[0 ] * imgResolution[0];
        float Y2 = pointB.y / scrResolution[1] * imgResolution[1];

        //Set the location of zoomed view at the bottom right corner of the screen.
        zoomView.x = scrResolution[0]-150;
        zoomView.y = scrResolution[1]-150;

        trans.x = currentPoint.x / scrResolution[0] * imgResolution[0];
        trans.y = currentPoint.y / scrResolution[1] * imgResolution[1];

        //This if blocks displays a zoomed View of current touch position.
        //Currrently facing issues.

        /*if (ZOOMING) {

         matrix.reset();
            *//*matrix.postScale(2f, 2f, currentPoint.x, currentPoint.y);

            shader = new BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            shaderPaint.setShader(shader);
            shaderPaint.getShader().setLocalMatrix(matrix);*//*


            matrix.reset();

            src.left   = currentPoint.x-5;
            src.top    = currentPoint.y-5;
            src.right  = currentPoint.x+5;
            src.bottom = currentPoint.y+5;

            dst.left   = 0;
            dst.top    = 0;
            dst.right  = 50;
            dst.bottom = 50;

            Log.i(TAG, "CURRENT  = " + currentPoint.toString());
            Log.i(TAG, "SRC = "+ src.toString());

            matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER );
            shader = new BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            shaderPaint.setShader(shader);
            shaderPaint.getShader().setLocalMatrix(matrix);

            canvas.drawCircle(zoomView.x, zoomView.y, 100, LinePaint);

            canvas.drawCircle(zoomView.x, zoomView.y, 100, shaderPaint);

            canvas.drawCircle(zoomView.x, zoomView.y, 100, InfoPaint);
            canvas.drawLine(zoomView.x-15, zoomView.y,zoomView.x+15, zoomView.y, InfoPaint);
            canvas.drawLine(zoomView.x, zoomView.y-15,zoomView.x, zoomView.y+15, InfoPaint);
        }
        else{
            buildDrawingCache();
        }*/


        Midpoint.x = (pointA.x+pointB.x)/2;
        Midpoint.y = (pointA.y+pointB.y)/2;

        Log.i(TAG, "PointA = " + pointA);
        Log.i(TAG, "PointB = " + pointB);
        Log.i(TAG, "Midpoint = " + Midpoint);

        //Actual angle of line.
        double ANGLE = (Math.atan2(pointB.y-pointA.y, pointB.x-pointA.x) * 180 ) / Math.PI;

        //Absolute Angle which will give values only between 0-90 in each quadrant.
        double ANGLEabs = (Math.atan2(Math.abs(Y2-Y1), Math.abs(X2-X1)) * 180 ) / Math.PI;

        double R1 =       sensitivityAngle;
        double R2 =  90 - sensitivityAngle;

        //Flag to detect status of line (For information printing purposes)
        int inRange = 0;


        if(perspectiveLENGTH != -1 && parallelLENGTH != -1){        //Both markers are present in the image
            //If -sensitivityAngle < (Angle of line) < +sensitivityAngle
            //Treat as Perspective, else, Parallel
            if(ANGLEabs >= R1 &&  ANGLEabs <= R2 ){
                Point p1 = new Point(X1, Y1);
                Point p2 = new Point(X2, Y2);
                Point[] line = new Point[]{p1, p2};
                //Apply perspective transform on the line to get the real line length.
                pixelDistance = MeasureImageActivity.getInstance().getPerspectiveLineLength(line);
                //15 is hardcoded as the length of Rectangular Marker.
                centimeterDistance = (pixelDistance * 15) / perspectiveLENGTH;
                inRange = 1;
            }
            else{
                //No need to apply perspective transform in parallel view
                //Euclidean distance can be used.
                pixelDistance = Math.sqrt(
                        Math.pow((X2-X1), 2) +
                                Math.pow((Y2-Y1), 2)
                );
                centimeterDistance = (pixelDistance * 15) / parallelLENGTH;
                inRange = 2;
            }


        } else if(perspectiveLENGTH == -1 && parallelLENGTH == -1){   //Both markers are absent in the image

            pixelDistance = Math.sqrt(
                    Math.pow((X2-X1), 2) +
                            Math.pow((Y2-Y1), 2)
            );
            centimeterDistance = (pixelDistance * 15) / 1;
            inRange = 3;

        } else if(perspectiveLENGTH == -1 && parallelLENGTH != -1){   //perspective absent and parallel present

            pixelDistance = Math.sqrt(
                    Math.pow((X2-X1), 2) +
                            Math.pow((Y2-Y1), 2)
            );
            centimeterDistance = (pixelDistance * 15) / parallelLENGTH;
            inRange = 2;

        }
        else if(perspectiveLENGTH != -1 && parallelLENGTH == -1){     //perspective present and parallel absent
            Point p1 = new Point(X1, Y1);
            Point p2 = new Point(X2, Y2);
            Point[] line = new Point[]{p1, p2};
            pixelDistance = MeasureImageActivity.getInstance().getPerspectiveLineLength(line);
            centimeterDistance = (pixelDistance * 15) / perspectiveLENGTH;
            inRange = 1;
        }

        //Draw the measuring line.
        canvas.drawLine(pointA.x,pointA.y,pointB.x,pointB.y, LinePaint);

        //Draw circular points on the vertices.
        canvas.drawCircle(pointA.x, pointA.y, 4,LinePaint);
        canvas.drawCircle(pointB.x, pointB.y, 4,LinePaint);

        //Draw circles around vertices.
        canvas.drawCircle(pointA.x, pointA.y, 20,InfoPaint);
        canvas.drawCircle(pointB.x, pointB.y, 20,InfoPaint);

        //Printing stuff on screen according to the status of line.
        if(inRange == 1){
            canvas.drawText("LENGTH = "+Integer.toString((int)centimeterDistance)+" cm", 20, 50, InfoPaint);
            canvas.drawText("ANGLEabs  = "+Double.toString(ANGLEabs), 20, 90, InfoPaint);
            canvas.drawText("ANGLE  = "+Double.toString(ANGLE), 20, 130, InfoPaint);
            canvas.drawText("PERSPECTIVE", 20, 170, InfoPaint);

            canvas.save();
            canvas.rotate((float) ANGLE,Midpoint.x, Midpoint.y);
            canvas.drawText(Integer.toString((int)centimeterDistance)+" cm", Midpoint.x, Midpoint.y, InfoPaint);
            canvas.restore();

        }else if(inRange == 2){
            canvas.drawText("LENGTH = "+Integer.toString((int)centimeterDistance)+" cm", 20, 50, InfoPaint);
            canvas.drawText("ANGLEabs  = "+Double.toString(ANGLEabs), 20, 90, InfoPaint);
            canvas.drawText("ANGLE  = "+Double.toString(ANGLE), 20, 130, InfoPaint);
            canvas.drawText("PARALLEL", 20, 170, InfoPaint);

            canvas.save();
            canvas.rotate((float) ANGLE,Midpoint.x, Midpoint.y);
            canvas.drawText(Integer.toString((int)centimeterDistance)+" cm", Midpoint.x, Midpoint.y, InfoPaint);
            canvas.restore();

        }else if(inRange == 3){
            canvas.drawText("No Marker Found", 20, 50, InfoPaint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i(TAG, "event");
        Log.i(TAG, Double.toString(pixelDistance));

        currentPoint.x = event.getX();
        currentPoint.y = event.getY();

        switch (event.getAction()) {
            //When touched, set currently touch (A or B)
            //Set zooming to true.
            case MotionEvent.ACTION_DOWN:
                ZOOMING = true;
                Log.i(TAG, "ACTION_DOWN");
                if (ATouchArea.contains(event.getX(), event.getY())) {
                    currentTouch = TOUCH_A;
                    Log.i(TAG, "TOUCH_A");
                } else if (BTouchArea.contains(event.getX(),event.getY())) {
                    currentTouch = TOUCH_B;
                    Log.i(TAG, "TOUCH_A");
                } else {
                    return false; //Return false if user touches none of the corners
                }

                return true;
            //When touch moved, Update the currently touched point.
            case MotionEvent.ACTION_MOVE:
                Log.i(TAG, "ACTION_MOVE");
                ZOOMING = true;

                switch (currentTouch) {
                    case TOUCH_A: {

                        pointA.x = event.getX();
                        pointA.y = event.getY();
                        Log.i(TAG, "currentTouchTOUCH_A");
                        invalidate();
                        return true;
                    }
                    case TOUCH_B: {

                        pointB.x = event.getX();
                        pointB.y = event.getY();
                        Log.i(TAG, "currentTouchTOUCH_B");
                        invalidate();
                        return true;
                    }
                }

                return false;

            case MotionEvent.ACTION_UP:
                //Set zooming to false
                //Update the coordinates.
                Log.i(TAG, "ACTION_UP");
                ZOOMING = false;
                switch (currentTouch) {
                    case TOUCH_A: {

                        pointA.x = event.getX();
                        pointA.y = event.getY();

                        invalidate();
                        currentTouch = NONE;
                        return true;
                    }
                    case TOUCH_B: {

                        pointB.x = event.getX();
                        pointB.y = event.getY();

                        invalidate();
                        currentTouch = NONE;
                        return true;
                    }
                }
                return false;
        }
        return true;
    }
}
