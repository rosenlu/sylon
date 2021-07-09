package net.luisr.sylon.ui.acquisition;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.Nullable;

import javax.vecmath.Vector2d;

/**
 * Based on SmartCropper by pqpo (https://github.com/pqpo/SmartCropper)
 */
public class CropperView extends androidx.appcompat.widget.AppCompatImageView {
    /** Tag for logging. */
    private static final String TAG = "CropperView";

    /** Color variables. */
    private static final int LINE_COLOR_LIGHT = 0xFFEEEEEE;  // argb
    private static final int LINE_COLOR_DARK = 0xFF111111;

    /** Distances and widths. All units are dp. */
    private static final float LINE_WIDTH = 2;
    private static final float CORNER_POINT_RADIUS = 15;
    private static final float TOUCH_POINT_CATCH_DISTANCE = 30;

    /** Indices of points in crop */
    private static final int INDEX_NONE = -1;
    private static final int TOP_LEFT = 0;
    private static final int BOTTOM_LEFT = 1;
    private static final int BOTTOM_RIGHT = 2;
    private static final int TOP_RIGHT = 3;

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

    /** Size of the displayed image in intrinsic pixels */
    private Point intrinsicSize;

    /**
     * An array containing all for corner points of the cropper.
     * Note: So far this only contains one point for testing purposes!
     */
    private Point[] cornerPoints;

    /** Path through all 4 {@link CropperView#cornerPoints} */
    private Path framePath;

    /** The index of the point that is currently being dragged by the user. */
    private int draggingPointIndex;

    /** Paints for different parts of the {@link CropperView}. */
    private Paint lightLinePaint, darkLinePaint, darkDottedLinePaint;

    public CropperView(Context context) {
        this(context, null);
    }

    public CropperView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropperView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        intrinsicSize = new Point(0, 0);

        pixelDensity = getResources().getDisplayMetrics().density;
        initPaints();

        cornerPoints = new Point[4];

        framePath = new Path();
    }

    private void initPaints() {
        lightLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lightLinePaint.setColor(LINE_COLOR_LIGHT);
        lightLinePaint.setStrokeWidth(dp2px(LINE_WIDTH));
        lightLinePaint.setStyle(Paint.Style.STROKE);

        darkLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        darkLinePaint.setColor(LINE_COLOR_DARK);
        darkLinePaint.setStrokeWidth(dp2px(LINE_WIDTH));
        darkLinePaint.setStyle(Paint.Style.STROKE);

        darkDottedLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        darkDottedLinePaint.setColor(LINE_COLOR_DARK);
        darkDottedLinePaint.setStrokeWidth(dp2px(LINE_WIDTH));
        darkDottedLinePaint.setStyle(Paint.Style.STROKE);
        darkDottedLinePaint.setPathEffect(new DashPathEffect(new float[] {dp2px(LINE_WIDTH*5), dp2px(LINE_WIDTH*5)}, 0f));
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
            if (!intrinsicSize.equals(intrinsicWidth, intrinsicHeight)) {
                intrinsicSize.set(intrinsicWidth, intrinsicHeight);
                // do stuff on image load
            }

            actualWidth = Math.round(intrinsicWidth * scaleX);
            actualHeight = Math.round(intrinsicHeight * scaleY);
            actualLeft = (getWidth() - actualWidth) / 2;
            actualTop = (getHeight() - actualHeight) / 2;
        }

        if (pointsAreValid(cornerPoints)) {
            for (Point p : cornerPoints) {
                canvas.drawCircle(getActualX(p.x), getActualY(p.y), dp2px(CORNER_POINT_RADIUS), lightLinePaint);
                canvas.drawCircle(getActualX(p.x), getActualY(p.y), dp2px(CORNER_POINT_RADIUS + LINE_WIDTH), darkLinePaint);
            }

            updatePath();

            canvas.drawPath(framePath, lightLinePaint);
            canvas.drawPath(framePath, darkDottedLinePaint);
        }

    }

    // TODO: also override performClick to allow Accessibility features. See https://stackoverflow.com/questions/27462468/custom-view-overrides-ontouchevent-but-not-performclick
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        boolean handleTouchEvent = true;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                draggingPointIndex = getNearbyPointIndex(event);
                if (draggingPointIndex == INDEX_NONE) {
                    handleTouchEvent = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // x and y of Points are in intrinsic system (relative to image edges)
                // x and y of event are in actual system (relative to screen edges)
                int newX = (int) ((Math.min(Math.max(event.getX(), actualLeft), actualLeft + actualWidth) - actualLeft) / scaleX);
                int newY = (int) ((Math.min(Math.max(event.getY(), actualTop), actualTop + actualHeight) - actualTop) / scaleY);

                Point newP = getClosestAllowedPoint(newX, newY);
                if (newP != null) {
                    cornerPoints[draggingPointIndex].x = newP.x;
                    cornerPoints[draggingPointIndex].y = newP.y;
                }
                break;
            case MotionEvent.ACTION_UP:
                draggingPointIndex = INDEX_NONE;
                break;
        }

        invalidate();
        return handleTouchEvent || super.onTouchEvent(event);
    }

    /**
     * Find the index of the first point that is within the {@link CropperView#TOUCH_POINT_CATCH_DISTANCE}.
     * @param event The MotionEvent.
     * @return the index of the point.
     */
    private int getNearbyPointIndex(MotionEvent event) {
        for (int i=0; i<cornerPoints.length; i++) {
            float xEvent = event.getX();
            float yEvent = event.getY();
            float xPoint = cornerPoints[i].x * scaleX + actualLeft;
            float yPoint = cornerPoints[i].y * scaleY + actualTop;
            double distance =  Math.sqrt(Math.pow(xEvent - xPoint, 2) + Math.pow(yEvent - yPoint, 2));
            if (distance < dp2px(TOUCH_POINT_CATCH_DISTANCE)) {
                return i;
            }
        }
        return INDEX_NONE;
    }

    /**
     * Get the closest allowed point from the new X and Y coordinates. If new X and Y are within the
     * allowed region, the closes allowed point is simply new X and Y.
     * TODO: returning exactly p1/p2/p3 results in PolyToPoly transformation being impossible! Maybe move points a tiny bit in bisecting direction.
     * @param newX the new X coordinate of the dragging point.
     * @param newY the new Y coordinate of the dragging point.
     * @return The closest allowed point.
     */
    private Point getClosestAllowedPoint(int newX, int newY) {
        if (draggingPointIndex == INDEX_NONE) {
            return null;
        }

        // the remaining 3 points starting clockwise from the dragging point
        Point p1 = cornerPoints[(draggingPointIndex + 1) % 4];
        Point p2 = cornerPoints[(draggingPointIndex + 2) % 4];
        Point p3 = cornerPoints[(draggingPointIndex + 3) % 4];

        // calculate unit vectors between the lines
        Vector2d v12 = new Vector2d(p2.x - p1.x, p2.y - p1.y);
        Vector2d v23 = new Vector2d(p3.x - p2.x, p3.y - p2.y);
        Vector2d v13 = new Vector2d(p3.x - p1.x, p3.y - p1.y);
        v12.normalize();
        v23.normalize();
        v13.normalize();

        // calculate perpendicular vectors
        @SuppressWarnings("SuspiciousNameCombination")
        Vector2d perpendicularToV12 = new Vector2d(- v12.y, v12.x);
        @SuppressWarnings("SuspiciousNameCombination")
        Vector2d perpendicularToV23 = new Vector2d(- v23.y, v23.x);
        @SuppressWarnings("SuspiciousNameCombination")
        Vector2d perpendicularToV13 = new Vector2d(- v13.y, v13.x);

        // calculate some booleans used below
        boolean isLeftOfV12 = (newY - p1.y) * (p2.x - p1.x) - (newX - p1.x) * (p2.y - p1.y) < 0;
        boolean isLeftOfV23 = (newY - p2.y) * (p3.x - p2.x) - (newX - p2.x) * (p3.y - p2.y) < 0;
        boolean isLeftOfV13 = (newY - p1.y) * (p3.x - p1.x) - (newX - p1.x) * (p3.y - p1.y) < 0;
        boolean isRightOfPerpendicularToV12InP1 = (newY - p1.y) * (p1.x + perpendicularToV12.x - p1.x) - (newX - p1.x) * (p1.y + perpendicularToV12.y - p1.y) > 0;
        boolean isRightOfPerpendicularToV13InP1 = (newY - p1.y) * (p1.x + perpendicularToV13.x - p1.x) - (newX - p1.x) * (p1.y + perpendicularToV13.y - p1.y) > 0;
        boolean isRightOfPerpendicularToV13InP3 = (newY - p3.y) * (p3.x + perpendicularToV13.x - p3.x) - (newX - p3.x) * (p3.y + perpendicularToV13.y - p3.y) > 0;
        boolean isRightOfPerpendicularToV23InP3 = (newY - p3.y) * (p3.x + perpendicularToV23.x - p3.x) - (newX - p3.x) * (p3.y + perpendicularToV23.y - p3.y) > 0;

        // check which line (p1 -> p2, p2 -> p3 or p1 -> p3) should be used to get closest allowed point
        Point baseline1;
        Point baseline2;

        if (isLeftOfV12 && isLeftOfV23 && isLeftOfV13) {  // within allowed region
            return new Point(newX, newY);
        } else if (isRightOfPerpendicularToV12InP1) {  // baseline p1->p2
            baseline1 = p1;
            baseline2 = p2;
        } else if (isRightOfPerpendicularToV13InP1) {  // dead zone of p1
            return p1;
        } else if (isRightOfPerpendicularToV13InP3) {  // baseline p1->p3
            baseline1 = p1;
            baseline2 = p3;
        } else if (isRightOfPerpendicularToV23InP3) {  // dead zone of p3
            return p3;
        } else {  // baseline p2->p3
            baseline1 = p2;
            baseline2 = p3;
        }

        // calculate the closest allowed point by finding the perpendicular intersection of
        // (newX, newY) with the baseline
        double k = ((baseline2.y - baseline1.y) * (newX - baseline1.x) - (baseline2.x - baseline1.x) * (newY - baseline1.y))
                / (Math.pow(baseline2.y - baseline1.y, 2) + Math.pow(baseline2.x - baseline1.x, 2));
        return new Point(
                (int) (newX - k * (baseline2.y - baseline1.y)),
                (int) (newY + k * (baseline2.x - baseline1.x))
        );
    }

    /**
     * Update the path that is spanned by the cornerPoints.
     */
    private void updatePath() {
        framePath.reset();
        framePath.moveTo(getActualX(cornerPoints[0].x), getActualY(cornerPoints[0].y));
        framePath.lineTo(getActualX(cornerPoints[1].x), getActualY(cornerPoints[1].y));
        framePath.lineTo(getActualX(cornerPoints[2].x), getActualY(cornerPoints[2].y));
        framePath.lineTo(getActualX(cornerPoints[3].x), getActualY(cornerPoints[3].y));
        framePath.close();
    }

    /**
     * Check weather a point array is valid, i.e. has a length of 4 and no nulls.
     * @param points The point array to check.
     * @return weather the point array is valid.
     */
    private boolean pointsAreValid(Point[] points) {
        return points != null && points.length == 4
                && points[0] != null && points[1] != null && points[2] != null && points[3] != null;
    }

    /**
     * Get the X value in regard to the view/screen (actual X) from an X value in the image (instrinsic X).
     * @param intrinsicX The X value in the image.
     * @return The X value in the view/screen.
     */
    private float getActualX(float intrinsicX) {
        return intrinsicX * scaleX + actualLeft;
    }

    /**
     * Get the Y value in regard to the view/screen (actual Y) from an Y value in the image (instrinsic Y).
     * @param intrinsicY The Y value in the image.
     * @return The Y value in the view/screen.
     */
    private float getActualY(float intrinsicY) {
        return intrinsicY * scaleY + actualTop;
    }

    /**
     * Convert from density-independent pixels (dp) to actual pixels (px).
     * @param dp The value in dp.
     * @return The value in px.
     */
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

    public Point[] getCornerPoints() {
        return cornerPoints;
    }

}
