package de.jadehs.vcg.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import de.jadehs.vcg.R;

public class DottedLine extends View {
    private boolean reverse = false;
    private int direction = 0;
    private Paint dottedPaint;
    private float strokeWidth = 50;
    private float marginDots = 120;
    private int color = Color.BLACK;

    public DottedLine(Context context) {
        super(context);
        init(context, null);
    }

    public DottedLine(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DottedLine(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public DottedLine(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.DottedLine,
                    0, 0);

            try {
                reverse = a.getBoolean(R.styleable.DottedLine_reverse, reverse);
                direction = a.getInteger(R.styleable.DottedLine_direction, direction);
                strokeWidth = a.getDimension(R.styleable.DottedLine_dotsSize, strokeWidth);
                marginDots = a.getDimension(R.styleable.DottedLine_marginDots, marginDots);
                color = a.getColor(R.styleable.DottedLine_dotsColor,color);
            } finally {
                a.recycle();
            }
        }


        this.dottedPaint = new Paint();
        this.dottedPaint.setAntiAlias(true);
        dottedPaint.setColor(color);
        dottedPaint.setStrokeWidth(strokeWidth);
        dottedPaint.setStrokeCap(Paint.Cap.ROUND);
        dottedPaint.setStyle(Paint.Style.STROKE);
        dottedPaint.setPathEffect(new DashPathEffect(new float[]{0f, marginDots + strokeWidth}, 0));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float hh = getHeight() / 2F;
        float hw = getWidth() / 2F;

        float xStart, yStart, xEnd, yEnd;

        float xStartPadding = 0, yStartPadding = 0;

        if (direction == 0) {
            xStart = hw;
            xEnd = hw;
            yStart = 0;
            yEnd = getHeight();
            yStartPadding = strokeWidth / 2;
        } else {
            xStart = 0;
            xEnd = getWidth();
            yStart = hh;
            yEnd = hh;
            xStartPadding = strokeWidth / 2;
        }


        if (reverse) {
            float tmp = xStart;
            xStart = xEnd;
            xEnd = tmp;
            tmp = yStart;
            yStart = yEnd;
            yEnd = tmp;

            xStartPadding *= -1;
            yStartPadding *= -1;
        }

        xStart += xStartPadding;
        yStart += yStartPadding;


        canvas.drawLine(xStart, yStart, xEnd, yEnd, this.dottedPaint);
    }
}
