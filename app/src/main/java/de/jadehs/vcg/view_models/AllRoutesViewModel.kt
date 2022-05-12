package de.jadehs.vcg.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import de.jadehs.vcg.data.db.access.RouteDatabase
import de.jadehs.vcg.data.db.pojo.RouteWithWaypoints
import de.jadehs.vcg.utils.ReplaceableLiveData

class AllRoutesViewModel(application: Application) : AndroidViewModel(application) {


    private var database: RouteDatabase = RouteDatabase.getInstance(application.applicationContext)

    private val routes = ReplaceableLiveData<List<RouteWithWaypoints>>()

    init {
        routes.setCurrentSource(database.routeDao().routes)
    }

    fun getRoutes(): LiveData<List<RouteWithWaypoints>> {
        return routes;
    }
}