package de.jadehs.vcg.layout.behaviours;

import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import de.jadehs.vcg.R;

public class BasicBottomSheetController {

    private BottomSheetBehavior<View> bottomSheetBehavior;
    private View bottomSheet;
    private FrameLayout fragmentContainer;
    private Fragment currentFragment;
    private FragmentManager fragmentManager;

    public BasicBottomSheetController(FragmentManager fragmentManager, View bottomSheet) {
        this.bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        this.bottomSheet = bottomSheet;
        this.fragmentContainer = bottomSheet.findViewById(R.id.bottom_sheet_fragment_container);
        this.fragmentManager = fragmentManager;
    }



    public void open(final Fragment newFragment) {
        currentFragment = newFragment;
        fragmentManager.beginTransaction().replace(fragmentContainer.getId(),newFragment).setReorderingAllowed(true).runOnCommit(new Runnable() {
            @Override
            public void run() {
                open();
            }
        }).commit();
    }

    private void open(){
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED && bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private void removeCurrentFragment(){
        if(currentFragment != null){
            fragmentManager.beginTransaction().remove(currentFragment).setReorderingAllowed(true).commit();
            currentFragment = null;
        }
    }


    public void close() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    }

    public boolean isOpen() {
        return bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED;
    }


    private class BottomSheetCallbacks extends BottomSheetBehavior.BottomSheetCallback{

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if(newState == BottomSheetBehavior.STATE_HIDDEN){
                removeCurrentFragment();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    }

    protected BottomSheetBehavior<View> getBottomSheetBehavior() {
        return bottomSheetBehavior;
    }
}
