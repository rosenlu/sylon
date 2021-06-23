package net.luisr.sylon.ui.acquisition;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.Nullable;

/**
 * Based on SmartCropper by pqpo (https://github.com/pqpo/SmartCropper)
 */
public class CropperView extends androidx.appcompat.widget.AppCompatImageView {
    // TODO: handle rotation of image... see RotationHandler

    /** Tag for logging. */
    private static final String TAG = "CropperView";

    /** Color variables. */
    private static final int LINE_COLOR = 0xFFEEEEEE;

    /** Distances and widths. All units are dp. */
    private static final float LINE_WIDTH = 2;
    private static final float CORNER_POINT_RADIUS = 15;
    private static final float TOUCH_POINT_CATCH_DISTANCE = 20;

    /** The pixel density of the screen. */
    private float pixelDensity;

    /**
     * Variables with the prefix 'actual' refer to pixels on the screen.
     * Variables with the prefix 'intrinsic' refer to pixels on the image.
     */
    private int actualWidth, actualHeight, actualLeft, actualTop;

    /** The values of the image matrix. */
    private float[] imageMatrixValue = new float[9];

    /** Scale between pixels on the display and pixels on the image. */
    private float scaleX, scaleY;

    /**
     * An array containing all for corner points of the cropper.
     * Note: So far this only contains one point for testing purposes!
     */
    Point[] cornerPoints;

    /** The point that is currently being dragged by the user. */
    Point draggingPoint;

    /** Paints for different parts of the {@link CropperView}. */
    private Paint pointPaint;  // corner points of the cropper

    public CropperView(Context context) {
        this(context, null);
    }

    public CropperView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropperView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        pixelDensity = getResources().getDisplayMetrics().density;
        initPaints();

        cornerPoints = new Point[4];
    }

    private void initPaints() {
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(LINE_COLOR);  // argb
        pointPaint.setStrokeWidth(dp2px(LINE_WIDTH));
        pointPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Drawable drawable = getDrawable();
        if (drawable != null) {
            getImageMatrix().getValues(imageMatrixValue);
            scaleX = imageMatrixValue[Matrix.MSCALE_X];
            scaleY = imageMatrixValue[Matrix.MSCALE_Y];
            int intrinsicWidth = drawable.getIntrinsicWidth();
            int intrinsicHeight = drawable.getIntrinsicHeight();
            actualWidth = Math.round(intrinsicWidth * scaleX);
            actualHeight = Math.round(intrinsicHeight * scaleY);
            actualLeft = (getWidth() - actualWidth) / 2;
            actualTop = (getHeight() - actualHeight) / 2;
        }

        if (pointsAreValid(cornerPoints)) {
            for (Point p : cornerPoints) {
                canvas.drawCircle(getActualX(p.x), getActualY(p.y), dp2px(CORNER_POINT_RADIUS), pointPaint);
            }
        }

    }

    // TODO: also override performClick to allow Accessibility features. See https://stackoverflow.com/questions/27462468/custom-view-overrides-ontouchevent-but-not-performclick
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        boolean handleTouchEvent = true;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                draggingPoint = getNearbyPoint(event);
                if (draggingPoint == null) {
                    handleTouchEvent = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // x and y of Points are in intrinsic system (relative to image edges)
                // x and y of event are in actual system (relative to screen edges)
                int x = (int) ((Math.min(Math.max(event.getX(), actualLeft), actualLeft + actualWidth) - actualLeft) / scaleX);
                int y = (int) ((Math.min(Math.max(event.getY(), actualTop), actualTop + actualHeight) - actualTop) / scaleY);
                draggingPoint.x = x;
                draggingPoint.y = y;
                break;
            case MotionEvent.ACTION_UP:
                draggingPoint = null;
                break;
        }

        invalidate();
        return handleTouchEvent || super.onTouchEvent(event);
    }

    private Point getNearbyPoint(MotionEvent event) {
        for (Point p : cornerPoints) {
            float xEvent = event.getX();
            float yEvent = event.getY();
            float xPoint = p.x * scaleX + actualLeft;
            float yPoint = p.y * scaleY + actualTop;
            double distance =  Math.sqrt(Math.pow(xEvent - xPoint, 2) + Math.pow(yEvent - yPoint, 2));
            if (distance < dp2px(TOUCH_POINT_CATCH_DISTANCE)) {
                return p;
            }
        }
        return null;
    }

    private float getActualX(float intrinsicX) {
        return intrinsicX * scaleX + actualLeft;
    }

    private float getActualY(float intrinsicY) {
        return intrinsicY * scaleY + actualTop;
    }

    private float dp2px(float dp) {
        return dp * pixelDensity;
    }

    public void setCornerPoints(Point[] cornerPoints) {
        if (getDrawable() == null) {
            Log.w(TAG, "Must be called after drawable has been set.");
        } else if (!pointsAreValid(cornerPoints)) {
            Log.w(TAG, "Passed cornerPoints are invalid.");
        } else {
            this.cornerPoints = cornerPoints;
            invalidate();
        }
    }

    private boolean pointsAreValid(Point[] points) {
        return points != null && points.length == 4
                && points[0] != null && points[1] != null && points[2] != null && points[3] != null;
    }

}
