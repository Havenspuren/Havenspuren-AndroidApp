package de.jadehs.vcg.layout.behaviours;

import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import de.jadehs.vcg.data.db.pojo.POIWaypointWithMedia;
import de.jadehs.vcg.layout.fragments.bottom_sheet.LongPoiInfo;
import de.jadehs.vcg.layout.fragments.bottom_sheet.ShortPoiInfo;

public class BottomSheetController extends BasicBottomSheetController {

    private static final String TAG = "BottomSheetController";
    private final OnBackPressedCallback onBackPressedListener;

    private POIWaypointWithMedia attachedTo;

    public BottomSheetController(FragmentManager fragmentManager, View bottomSheet) {
        super(fragmentManager, bottomSheet);
        setupView();
        this.onBackPressedListener = new OnBackPressedCallback(false){
            @Override
            public void handleOnBackPressed() {
                if(isOpen()){
                    close();
                }
            }
        };
    }

    public void setupView(){
        BottomSheetBehavior<View> bottomSheetBehavior = getBottomSheetBehavior();


        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setPeekHeight(400);
    }

    public void open(){
        if(attachedTo != null){
            Fragment f;
            if(needsDetailedInfoWindow()){
                f = LongPoiInfo.newInstance(attachedTo);
            }else{
                f = ShortPoiInfo.newInstance(attachedTo);
            }
            open(f);
        }
    }

    @Override
    public void close(Runnable onCloseCallback) {
        super.close(onCloseCallback);
        this.onBackPressedListener.setEnabled(false);
    }

    @Override
    public void open(Fragment newFragment, Runnable onOpenCallback) {
        super.open(newFragment, onOpenCallback);
        this.onBackPressedListener.setEnabled(true);
    }

    //    private Button detailButton;


    public POIWaypointWithMedia getAttachedTo() {
        return attachedTo;
    }

    public void setAttachedTo(POIWaypointWithMedia attachedTo) {
        this.attachedTo = attachedTo;
    }

    /**
     *
     * @return wether the currently attached waypoint needs the full poi info side or not. If no Marker is attached, false is returned
     */
    private boolean needsDetailedInfoWindow(){
        return attachedTo != null && attachedTo.isVisited();
    }

    public OnBackPressedCallback getOnBackPressedListener() {
        return onBackPressedListener;
    }
}
