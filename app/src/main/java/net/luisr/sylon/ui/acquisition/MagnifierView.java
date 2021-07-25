package net.luisr.sylon.ui.acquisition;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class MagnifierView extends androidx.appcompat.widget.AppCompatImageView {

    private static final int LINE_COLOR_DARK = 0xFF111111;

    private static final float LINE_WIDTH = 2;

    private final Paint crosshairPaint;

    public MagnifierView(Context context) {
        this(context, null);
    }

    public MagnifierView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MagnifierView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        crosshairPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        crosshairPaint.setColor(LINE_COLOR_DARK);
        crosshairPaint.setStrokeWidth(LINE_WIDTH * getResources().getDisplayMetrics().density);
        crosshairPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Drawable drawable = getDrawable();
        if (drawable != null) {
            canvas.drawLine(getWidth() * 0.5f, 0f, getWidth() * 0.5f, getHeight(), crosshairPaint);
            canvas.drawLine(0f, getHeight() * 0.5f, getWidth(), getHeight() * 0.5f, crosshairPaint);
        }

    }
}
