package de.jadehs.vcg.layout.fragments.trophies;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.jadehs.vcg.data.db.access.RouteDatabase;
import de.jadehs.vcg.data.db.models.POIRoute;
import de.jadehs.vcg.data.db.pojo.TrophyWithWaypoint;
import de.jadehs.vcg.utils.ReplaceableLiveData;

public class TrophyMapViewModel extends AndroidViewModel {

    private final RouteDatabase routeDatabase;
    private final ReplaceableLiveData<List<TrophyWithWaypoint>> trophiesLiveData = new ReplaceableLiveData<>();
//    private final ReplaceableLiveData<POIRoute> routeLiveData = new ReplaceableLiveData<>();

    public TrophyMapViewModel(@NonNull @NotNull Application application) {
        super(application);
        routeDatabase = RouteDatabase.getInstance(application);
    }

    public void setRouteOfTrophies(long id){
        trophiesLiveData.setCurrentSource(routeDatabase.trophyDao().getTrophiesOfRoute(id));
//        routeLiveData.setCurrentSource(routeDatabase.routeDao().getRawRoute(id));
    }

    public LiveData<List<TrophyWithWaypoint>> getTrophies(){
        return trophiesLiveData;
    }

    /*public LiveData<POIRoute> getRoute(){
        return routeLiveData;
    }*/

}