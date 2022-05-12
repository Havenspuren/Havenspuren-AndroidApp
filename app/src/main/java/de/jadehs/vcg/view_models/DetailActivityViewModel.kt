package de.jadehs.vcg.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import de.jadehs.vcg.data.db.access.RouteDatabase
import de.jadehs.vcg.data.db.pojo.POIWaypointWithMedia
import de.jadehs.vcg.utils.ReplaceableLiveData

class DetailActivityViewModel(application: Application,id:Long) : AndroidViewModel(application) {



    private val database: RouteDatabase = RouteDatabase.getInstance(application.applicationContext)
    private val mediatorLiveData: ReplaceableLiveData<POIWaypointWithMedia> = ReplaceableLiveData()

    /**
     * the waypoint retrieved by the intent
     */
    val waypoint:LiveData<POIWaypointWithMedia>
        get() = mediatorLiveData


    init {
        this.setWaypoint(id)
    }

    private fun setWaypoint(waypoint: POIWaypointWithMedia){
        mediatorLiveData.postValue(waypoint)
    }
    private fun setWaypoint(id:Long){
        mediatorLiveData.setCurrentSource(database.wayPointDao().getWaypoint(id))
    }









}