package de.jadehs.vcg.layout.behaviours;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewNestedScrollBehaviour extends CoordinatorLayout.Behavior<RecyclerView> {

    View other;

    public RecyclerViewNestedScrollBehaviour() {
    }

    public RecyclerViewNestedScrollBehaviour(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull RecyclerView child, @NonNull View dependency) {
        if (other != null && other != dependency) {
            throw new IllegalStateException("already found another window");
        }
        other = dependency;
        return true;
    }




    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull RecyclerView child, int layoutDirection) {
        parent.onLayoutChild(child,layoutDirection);
        child.setTop(other.getMeasuredHeight());
        return true;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull RecyclerView child, @NonNull View dependency) {
        int bottomOther = other.getMeasuredHeight() + other.getTop();
        if(child.getTop() != bottomOther){
            child.setTop(bottomOther);
        }

        return true;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull RecyclerView child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0 && directTargetChild instanceof RecyclerView;
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull RecyclerView child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (dy <= 0)
            return;

        int top = other.getTop();
        if (other.getMeasuredHeight() >= top * -1) {
            int toConsume = Math.min(other.getMeasuredHeight() - top, dy);
            other.setTop(top - toConsume);
            consumed[1] = dy;
        }
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull RecyclerView child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        if (dyUnconsumed >= 0)
            return;

        int top = other.getTop();
        if (top < 0) {
            int toConsume = Math.max(top, dyUnconsumed);
            other.setTop(top + toConsume * -1);
            consumed[1] = toConsume;
        }
    }
}
