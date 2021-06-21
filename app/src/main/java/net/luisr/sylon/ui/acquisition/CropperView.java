package net.luisr.sylon.ui.acquisition;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;

/**
 * Based on SmartCropper by pqpo (https://github.com/pqpo/SmartCropper)
 */
public class CropperView extends androidx.appcompat.widget.AppCompatImageView {
    // TODO: handle rotation of image... see RotationHandler

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

        cornerPoints = new Point[1];  // only one point for testing, later this should be 4
        cornerPoints[0] = new Point(420, 690);
    }

    private void initPaints() {
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(0xFFEEEEEE);  // argb
        pointPaint.setStrokeWidth(dp2px(2));
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

        canvas.drawCircle(getActualX(cornerPoints[0].x), getActualY(cornerPoints[0].y), dp2px(15), pointPaint);

    }

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
            boolean pointIsNearby = false;
            float xEvent = event.getX();
            float yEvent = event.getY();
            float xPoint = p.x * scaleX + actualLeft;
            float yPoint = p.y * scaleY + actualTop;
            double distance =  Math.sqrt(Math.pow(xEvent - xPoint, 2) + Math.pow(yEvent - yPoint, 2));
            if (distance < dp2px(20)) {
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

}
