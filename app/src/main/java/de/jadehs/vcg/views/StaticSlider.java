package de.jadehs.vcg.views;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.material.slider.Slider;

/**
 * Does prevent any click/touch/key events to the slider to prevent manually changing the slider
 */
public class StaticSlider extends Slider {
    public StaticSlider(@NonNull Context context) {
        super(context);
    }

    public StaticSlider(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StaticSlider(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isClickable() {
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int getFocusable() {
        return NOT_FOCUSABLE;
    }

    @Override
    public boolean hasExplicitFocusable() {
        return false;
    }

    @Override
    public boolean hasFocusable() {
        return false;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        return false;
    }
}
