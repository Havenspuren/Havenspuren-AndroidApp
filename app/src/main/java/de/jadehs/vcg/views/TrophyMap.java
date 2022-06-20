package de.jadehs.vcg.views;

import android.animation.PointFEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

import java.util.LinkedList;
import java.util.List;

import de.jadehs.vcg.utils.RectHelpers;

public class TrophyMap extends View implements GestureDetector.OnGestureListener {

    public static abstract class Adapter {
        private TrophyMap map;

        public abstract int getCount();

        public abstract Bitmap getBitmapOfPosition(int position);

        public abstract Point getPositionOfPosition(int position, float width, float height);

        public abstract boolean getGreyScaleOfPosition(int position);

        public abstract void onClickAtPosition(int position);

        public TrophyMap getMap() {
            return map;
        }

        public void setMap(TrophyMap map) {
            this.map = map;
        }

        public final void notifyDataSetHasChanged() {
            if (getMap() != null) {
                getMap().notifyDataSetHasChanged();
            }
        }

        public final void notifyDataSetHasChanged(int position) {
            if (getMap() != null) {
                getMap().notifyDataSetHasChanged(position);
            }
        }
    }

    public class TrophyEntry {
        private Bitmap bitmap;
        private Point point;
        private boolean greyScale;


        public TrophyEntry(Point point, Bitmap bitmap, boolean greyScale) {
            this.point = point;
            this.bitmap = bitmap;
            this.greyScale = greyScale;
        }

        public boolean isGreyScale() {
            return greyScale;
        }

        public void setGreyScale(boolean greyScale) {
            this.greyScale = greyScale;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            if (bitmap != null) {
                this.bitmap = bitmap;
            }

        }

        public Point getPoint() {
            return point;
        }

        public void setPoint(Point point) {
            if (point != null) {
                this.point = point;
            }
        }

        /**
         * does set the corners of the given RectF to the bounds the bitmap needs when drawn to a canvas
         *
         * @param x     the x coordinate
         * @param y     the y coordinate
         * @param rectF the rectf instance which will get changed
         */
        public void setBoundsAt(float x, float y, RectF rectF) {
            float hHeight = bitmap.getHeight() / 2f;
            float hWidth = bitmap.getWidth() / 2f;
            rectF.set(
                    x - hWidth,
                    y - hHeight,
                    x + hWidth,
                    y + hHeight);
        }
    }

    public interface FocusChangedListener {
        /**
         * is called if the focused index has changed, or the contents of the trophies has changed
         *
         * @param index
         */
        void onFocusChangedListener(int index);
    }

    private static final long ANIMATION_DURATION = 1000;

    private static final String TAG = "TrophyMap";
    /**
     * background drawable
     */
    private Drawable backgroundDrawable;
    private RectF baseBackgroundBounds;
    /**
     * bounds of the background, after scaling and transforming
     */
    private RectF currentBackgroundBounds;
    /**
     * universal use, tmp rect
     */
    private Rect tmpRect;
    private RectF tmpRectf;
    private float[] tmpFloats;
    private Paint greyScalePaint;
    private Matrix baseMatrix;

    private GestureDetectorCompat gestureDetector;
    /**
     * coordinate of the current view
     */
    private PointF center;
    private PointF maxCoord;
    private PointF minCoord;
    private float zoom;
    private float minZoom = 1f;

    ValueAnimator animator;


    Adapter adapter;
    TrophyEntry[] trophies;
    int currentFocusedPosition;
    List<FocusChangedListener> listenerList;


    public TrophyMap(Context context) {
        super(context);
        init();
    }

    public TrophyMap(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TrophyMap(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TrophyMap(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    public void addFocusChangedListener(FocusChangedListener listener) {
        if (!listenerList.contains(listener)) {
            listenerList.add(listener);
            listener.onFocusChangedListener(currentFocusedPosition);
        }

    }

    public void removeFocusChangedListener(FocusChangedListener listener) {
        listenerList.remove(listener);
    }

    protected void callFocusChangedListeners(int position) {
        for (FocusChangedListener l : listenerList) {
            l.onFocusChangedListener(position);
        }
    }


    /**
     * paint which is used to draw greyscale contents
     *
     * @return the paint instance which is used
     */
    public Paint getGreyScalePaint() {
        return greyScalePaint;
    }

    /**
     * does change the paint, which is used to draw grey scale contents (for example not already unlocked trophies)
     *
     * @param greyScalePaint the new grey scale paint which will get used. If null every grey scale content is drawn normally
     */
    public void setGreyScalePaint(Paint greyScalePaint) {
        this.greyScalePaint = greyScalePaint;
    }

    public void setZoom(float zoomLvl) {
        if (zoomLvl >= minZoom) {
            this.zoom = zoomLvl;
            redraw();
        }
    }

    public float getZoom() {
        return zoom;
    }

    public float getMinZoom() {
        return minZoom;
    }

    /**
     * does set the new minimal zoom level
     * <p>
     * normal zoom gets changed to the minZoom value, if it is smaller than the min zoom level
     *
     * @param minZoom new min Zoom level
     */
    private void setMinZoom(float minZoom) {
        this.minZoom = minZoom;
        if (getZoom() < minZoom) {
            setZoom(minZoom);
        }
    }

    /**
     * does calculate the min Zoom level.
     *
     * @param w the width of the view
     * @param h the height of the view
     * @return the new min zoom level
     */
    private float calculateMinZoom(int w, int h) {
        //calculate the current min zoom level
        float vHeight = baseBackgroundBounds.height(); // 1200
        float vWidth = baseBackgroundBounds.width();
        float hDiff = h / vHeight;
        float wDiff = w / vWidth;
        return Math.max(hDiff, wDiff);
    }

    /**
     * sets the new center location, the coordinates are
     *
     * @param center new center location
     */
    public void setCenter(PointF center) {
        setCenter(center.x, center.y);
    }

    public void setCenter(float x, float y) {
        center.set(x, y);
        moveToClosestValidPoint(center);
        redraw();
    }

    /**
     * does call Matrix#setTranslate(float,float) to by the current center values
     *
     * @param matrix the Matrix instance which will get modified
     * @param zoom   value which is used to multiply the center values
     */
    private void translateToCenter(Matrix matrix, float zoom) {
        matrix.setTranslate(-((center.x - minCoord.x) * zoom), -((center.y - minCoord.y) * zoom));
    }


    private void convertToBaseCoords(PointF point) {
        point.x /= zoom;
        point.y /= zoom;
        point.offset(center.x - minCoord.x, center.y - minCoord.y);
    }

    /**
     * Does call animateTo(long,float,float) with the default value as duration
     *
     * @param x the x and
     * @param y y coordinate
     */
    public void animateTo(float x, float y) {
        animateTo(ANIMATION_DURATION, x, y);
    }

    public void animateTo(long duration, float x, float y) {
        if (animator.isRunning()) {
            animator.cancel();
        }
        PointF destination = new PointF(x, y);
        moveToClosestValidPoint(destination);
        animator.setObjectValues(new PointF(center.x, center.y), destination);
        animator.setEvaluator(new PointFEvaluator(new PointF()));
        animator.setDuration(duration);
        animator.start();
    }

    public boolean isAnimating() {
        return animator.isRunning();
    }


    public void stopAnimation() {
        animator.end();
    }


    /**
     * the min coordinates the center can have. Always bottom right.
     *
     * @return the min coordinates
     */
    public PointF getMinCenter() {
        return minCoord;
    }

    /**
     * the max coordinates the center can have. Always bottom right.
     *
     * @return the max coordinates
     */
    public PointF getMaxCenter() {
        return maxCoord;
    }


    /**
     * does change the given point coordinates to x any values which are valid values within the max and min coordinates
     *
     * @param input the input point, which gets changed to be a valid coordinate
     */
    private void moveToClosestValidPoint(PointF input) {
        if (input.x < minCoord.x) {
            input.x = minCoord.x;
        } else if (input.x > maxCoord.x) {
            input.x = maxCoord.x;
        }
        if (input.y < minCoord.y) {
            input.y = minCoord.y;
        } else if (input.y > maxCoord.y) {
            input.y = maxCoord.y;
        }
    }

    /**
     * does calculate the max and min values for the x and y coordinate.
     * <p>
     * The min x and y values are chosen that the background image is always completely filling the background of the view
     */
    private void recalculateMinMaxCoord() {
        float viewCenterWidth = getWidth() / 2f;
        float viewCenterHeight = getHeight() / 2f;
        // revert zooming
        float unzoomedViewCenterHeight = viewCenterHeight / zoom;
        float unzoomedViewCenterWidth = viewCenterWidth / zoom;

        minCoord.set(unzoomedViewCenterWidth, unzoomedViewCenterHeight);
        maxCoord.set(baseBackgroundBounds.width() - unzoomedViewCenterWidth, baseBackgroundBounds.height() - unzoomedViewCenterHeight);
    }

    public void setAdapter(Adapter adapter) {
        if (adapter != null) {
            adapter.setMap(this);
            this.adapter = adapter;
            loadAllTrophies();
            if (currentFocusedPosition >= this.adapter.getCount()) {
                currentFocusedPosition = 0;
            }
            callFocusChangedListeners(currentFocusedPosition);
        }
    }

    public Adapter getAdapter() {
        return this.adapter;
    }


    public int getCurrentFocusedPosition() {
        return currentFocusedPosition;
    }


    public void setCurrentBackgroundBounds(int currentFocusedPosition) {
        setCurrentFocusedPosition(currentFocusedPosition, false);
    }

    public void setCurrentFocusedPosition(int currentFocusedPosition, boolean animated) {
        this.currentFocusedPosition = currentFocusedPosition;
        callFocusChangedListeners(this.currentFocusedPosition);
        if (animated) {
            focusOnTrophy(this.currentFocusedPosition);
        } else {
            try {
                TrophyEntry entry = trophies[currentFocusedPosition];
                Point point = entry.getPoint();
                setCenter(point.x, point.y);
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                Log.e(TAG, "setCurrentFocusedPosition: ", e);
            }
        }
    }

    public void focusNextTrophy() {
        if (getAdapter() != null) {
            if (hasNextTrophyToFocus()) {
                setCurrentFocusedPosition(++currentFocusedPosition, true);
            }
        }
    }

    public boolean hasNextTrophyToFocus() {
        if (getAdapter() != null) {
            return currentFocusedPosition + 1 < getAdapter().getCount();
        }
        return false;
    }

    public void focusPreviousTrophy() {
        if (getAdapter() != null) {
            if (hasPreviousTrophyToFocus()) {
                setCurrentFocusedPosition(--currentFocusedPosition, true);
            }
        }
    }

    public boolean hasPreviousTrophyToFocus() {
        if (getAdapter() != null) {
            return currentFocusedPosition > 0;
        }
        return false;
    }

    private void focusOnTrophy(int index) {
        TrophyEntry entry = trophies[index];
        animateTo(entry.getPoint().x, entry.getPoint().y);
    }

    private void loadTrophy(int position) {
        Adapter adapter = getAdapter();
        if (adapter != null && adapter.getCount() > position) {
            Bitmap bitmap = adapter.getBitmapOfPosition(position);
            Point point = adapter.getPositionOfPosition(position, baseBackgroundBounds.width(), baseBackgroundBounds.height());
            trophies[position] = new TrophyEntry(point, bitmap, adapter.getGreyScaleOfPosition(position));
        }
    }

    private void loadAllTrophies() {
        Adapter adapter = getAdapter();
        if (adapter != null) {
            trophies = new TrophyEntry[adapter.getCount()];
            for (int i = 0; i < trophies.length; i++) {
                loadTrophy(i);
            }
        }

        if (trophies.length > 0) {
            if (currentFocusedPosition >= trophies.length) {
                currentFocusedPosition = 0;
            }
            Point point = trophies[currentFocusedPosition].getPoint();

            setCenter(point.x, point.y);
        }
        invalidate();
    }

    public void notifyDataSetHasChanged() {
        loadAllTrophies();
        callFocusChangedListeners(currentFocusedPosition);
    }

    public void notifyDataSetHasChanged(int position) {
        loadTrophy(position);
        invalidate();
    }

    public void setTrophyBackground(Drawable background) {
        this.backgroundDrawable = background;
        updateBaseBounds();
        recalculateBounds(this.getWidth(), this.getHeight());
    }

    private void updateBaseBounds() {
        if (backgroundDrawable != null) {
            baseBackgroundBounds.set(0, 0, backgroundDrawable.getIntrinsicWidth(), backgroundDrawable.getIntrinsicHeight());
        }
    }


    /**
     * initializes everything except Graphics elements. Calls initGraphics()
     */
    private void init() {
        gestureDetector = new GestureDetectorCompat(getContext(), this);
        listenerList = new LinkedList<>();
        greyScalePaint = new Paint();
        ColorMatrix greyMatrix = new ColorMatrix();
        greyMatrix.setSaturation(0);
        greyScalePaint.setColorFilter(new ColorMatrixColorFilter(greyMatrix));
        greyScalePaint.setAlpha(180);
        center = new PointF();
        minCoord = new PointF();
        maxCoord = new PointF();
        baseMatrix = new Matrix();
        baseBackgroundBounds = new RectF();
        currentBackgroundBounds = new RectF();
        tmpRect = new Rect();
        tmpRectf = new RectF();
        tmpFloats = new float[2];
        animator = new ValueAnimator();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (animation.getAnimatedValue() instanceof PointF) {
                    setCenter((PointF) animation.getAnimatedValue());
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (backgroundDrawable != null)
            backgroundDrawable.draw(canvas);
        drawAllTrophies(canvas);
    }


    private void drawAllTrophies(Canvas canvas) {
        // TODO maybe merge all bitmaps into one, for performance
        if (trophies != null) {
            for (TrophyEntry entry : trophies) {
                tmpFloats[0] = entry.point.x;
                tmpFloats[1] = entry.point.y;
                baseMatrix.mapPoints(tmpFloats);
                entry.setBoundsAt(tmpFloats[0], tmpFloats[1], tmpRectf);
                canvas.drawBitmap(
                        entry.bitmap,
                        tmpRectf.left,
                        tmpRectf.top,
                        entry.greyScale ? greyScalePaint : null
                );
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        recalculateBounds(w, h);
        redraw();
    }

    private void recalculateBounds(int width, int height) {
        setMinZoom(calculateMinZoom(width, height));
        recalculateMinMaxCoord();
        moveToClosestValidPoint(center);
    }

    /**
     * does set the new bounds and does apply the matrix
     */
    protected void redraw() {
        if (backgroundDrawable != null) {
            translateToCenter(baseMatrix, 1);

            baseMatrix.postScale(zoom, zoom);
            // calculate current bounds
            baseMatrix.mapRect(currentBackgroundBounds, baseBackgroundBounds);
            // convert to int rect
            RectHelpers.toRect(currentBackgroundBounds, tmpRect);

            backgroundDrawable.setBounds(tmpRect);
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (trophies != null && getAdapter() != null) {
            for (int i = 0; i < trophies.length; i++) {
                TrophyEntry entry = trophies[i];
                tmpFloats[0] = entry.point.x;
                tmpFloats[1] = entry.point.y;
                baseMatrix.mapPoints(tmpFloats);
                entry.setBoundsAt(tmpFloats[0], tmpFloats[1], tmpRectf);
                if (tmpRectf.contains(e.getX(), e.getY())) {
                    getAdapter().onClickAtPosition(i);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.setCurrentFocusedPosition(ss.currentLocation, false);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState state = new SavedState(superState);
        state.currentLocation = this.currentFocusedPosition;
        return state;
    }

    static class SavedState extends BaseSavedState {
        int currentLocation;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.currentLocation = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.currentLocation);
        }

        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
