package net.luisr.sylon.ui.acquisition;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

/**
 * Based on SmartCropper by pqpo (https://github.com/pqpo/SmartCropper)
 */
public class CropperView extends androidx.appcompat.widget.AppCompatImageView {

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

        canvas.drawCircle(getActualX(420), getActualY(690), dp2px(15), pointPaint);

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
