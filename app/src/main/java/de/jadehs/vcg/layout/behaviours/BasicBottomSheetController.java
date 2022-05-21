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
    private Runnable onOpenCallback;
    private Runnable onCloseCallback;

    public BasicBottomSheetController(FragmentManager fragmentManager, View bottomSheet) {
        this.bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        this.bottomSheet = bottomSheet;

        this.bottomSheetBehavior.addBottomSheetCallback(new BottomSheetCallbacks());
        this.fragmentContainer = bottomSheet.findViewById(R.id.bottom_sheet_fragment_container);
        this.fragmentManager = fragmentManager;
    }

    public void open(final Fragment newFragment) {
        this.open(newFragment, null);
    }


    public void open(final Fragment newFragment, Runnable onOpenCallback) {
        this.onOpenCallback = onOpenCallback;
        currentFragment = newFragment;
        fragmentManager.beginTransaction().replace(fragmentContainer.getId(),newFragment).setReorderingAllowed(true).runOnCommit(new Runnable() {
            @Override
            public void run() {
                open();
            }
        }).commit();
    }

    private void open(){
        if (!this.isOpen()){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }else{
            notifyOnOpenCallback();
        }
    }

    private void removeCurrentFragment(){
        if(currentFragment != null){
            fragmentManager.beginTransaction().remove(currentFragment).setReorderingAllowed(true).commit();
            currentFragment = null;
        }
    }

    private void notifyOnOpenCallback(){
        if(onOpenCallback != null){
            onOpenCallback.run();
            onOpenCallback = null;
        }
    }

    private void notifyOnCloseCallback(){
        if(onCloseCallback != null){
            onCloseCallback.run();
            onCloseCallback = null;
        }
    }


    public void close(){
        this.close(null);
    }

    public void close(Runnable onCloseCallback) {
        this.onCloseCallback = onCloseCallback;
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
                notifyOnCloseCallback();
            }

            if(newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_EXPANDED){
                notifyOnOpenCallback();
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
