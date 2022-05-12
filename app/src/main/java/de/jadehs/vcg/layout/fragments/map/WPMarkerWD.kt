package de.jadehs.vcg.layout.fragments.map

import android.view.MotionEvent
import de.jadehs.vcg.data.db.pojo.POIWaypointWithMedia
import de.jadehs.vcg.layout.behaviours.BottomSheetController


class WPMarkerWD : WPMarker()  {

    var waypoint: POIWaypointWithMedia?=null
    set(value) {
        field = value
        this.position = value!!.geoPosition
    }

    /*override fun onTapOnMarker(e: MotionEvent?,mapView: MapView?): Boolean {
        if(info != null){
            if(info!!.isOpen()){
                if(info!!.attachedTo == this){
                    info?.close()
                }else{
                    openInfoWindow(mapView);
                }

            }else{
                openInfoWindow(mapView);
            }
            return true
        }
        return false;
    }

    fun openInfoWindow(mapView: MapView?){
        info?.attachedTo = this
        info?.open()
        mapView?.controller?.animateTo(waypoint?.geoPosition,18.0,null)
    }*/


}