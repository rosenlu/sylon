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

    private float scaleX, scaleY;
    private int actualWidth, actualHeight, actualLeft, actualTop;

    private float[] matrixValue = new float[9];

    private Paint pointPaint;

    public CropperView(Context context) {
        this(context, null);
    }

    public CropperView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropperView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initPaints();
    }

    private void initPaints() {
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Drawable drawable = getDrawable();
        if (drawable != null) {
            getImageMatrix().getValues(matrixValue);
            scaleX = matrixValue[Matrix.MSCALE_X];
            scaleY = matrixValue[Matrix.MSCALE_Y];
            int intrinsicWidth = drawable.getIntrinsicWidth();
            int intrinsicHeight = drawable.getIntrinsicHeight();
            actualWidth = Math.round(intrinsicWidth * scaleX);
            actualHeight = Math.round(intrinsicHeight * scaleY);
            actualLeft = (getWidth() - actualWidth) / 2;
            actualTop = (getHeight() - actualHeight) / 2;
        }

        pointPaint.setColor(0xFF0000FF);
        pointPaint.setStrokeWidth(5);
        pointPaint.setStyle(Paint.Style.STROKE);

        canvas.drawCircle(getActualX(420), getActualY(69), 50, pointPaint);

    }

    private float getActualX(float intrinsicX) {
        return intrinsicX * scaleX + actualLeft;
    }

    private float getActualY(float intrinsicY) {
        return intrinsicY * scaleY + actualTop;
    }

}
