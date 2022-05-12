package de.jadehs.vcg.layout.behaviours;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * This behavior does hide or show the floating action button whether the set layout anchor, which has a bottom sheet behavior attached to it is visible on screen or not
 */
public class FABBottomSheetVisibilityBehaviour extends CoordinatorLayout.Behavior<FloatingActionButton> {
    private static final String TAG = "FABBottomSheetVisibilit";
    private int lastState;


    public FABBottomSheetVisibilityBehaviour(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull final FloatingActionButton child, @NonNull View dependency) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) dependency.getLayoutParams();
        CoordinatorLayout.LayoutParams paramsChild = (CoordinatorLayout.LayoutParams) child.getLayoutParams();

        return params.getBehavior() instanceof BottomSheetBehavior && paramsChild.getAnchorId() == dependency.getId();
    }


    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull FloatingActionButton child, @NonNull View dependency) {

        BottomSheetBehavior<NestedScrollView> behavior = BottomSheetBehavior.from((NestedScrollView)dependency);


        if(behavior.getState() == BottomSheetBehavior.STATE_HIDDEN && lastState != behavior.getState()) child.hide();

        if(behavior.getState() == BottomSheetBehavior.STATE_SETTLING || behavior.getState() == BottomSheetBehavior.STATE_DRAGGING){
            float expandedPercent = (parent.getHeight() - dependency.getTop()) / (float) dependency.getHeight();
            if (expandedPercent < behavior.getPeekHeight()/((float)dependency.getHeight()*2))
                child.hide();
            else child.show();
        }
        lastState = behavior.getState();
        return super.onDependentViewChanged(parent, child, dependency);

    }
}
