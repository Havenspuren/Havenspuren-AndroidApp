package de.jadehs.vcg.utils;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.Parameters;

import java.io.Closeable;
import java.io.IOException;

import de.jadehs.vcg.utils.data.FileProvider;

public class RouteMemoryCache implements Closeable {
    private static final String TAG = "RouteMemoryCache";
    private static AsyncTask<Void, Void, ResponsePath> calcTask;
    private final FileProvider fileProvider;
    private GraphHopper hopper;
    private String mapName;
    private MutableLiveData<ResponsePath> liveData;

    public RouteMemoryCache(@NonNull Context context, @NonNull String mapName) {
        fileProvider = new FileProvider(context);
        liveData = new MutableLiveData<>();
        this.mapName = mapName;
        loadHopper();
    }

    public LiveData<ResponsePath> getRoute() {
        return liveData;
    }

    private void loadHopper() {
        GraphHopper tmpHopp = new GraphHopper().forMobile();
        tmpHopp.setEncodingManager(EncodingManager.create("car"));
        tmpHopp.setProfiles(new Profile("car").setVehicle("car").setWeighting("fastest"));
        tmpHopp.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));

        boolean result = tmpHopp.load(fileProvider.getHopperFolder(mapName).getAbsolutePath());
        if (result)
            hopper = tmpHopp;
        else
            Log.e(TAG, "Couldn't load graphhopper graph");
    }

    public void calcPath(final double fromLat, final double fromLng, final double toLat, final double toLng) {
        if (hopper != null && (calcTask == null || calcTask.getStatus() == AsyncTask.Status.FINISHED || calcTask.getStatus() == AsyncTask.Status.PENDING)) {
            calcTask = new RouteCalcAsyncTask(hopper, liveData, fromLat, fromLng, toLat, toLng);
            calcTask.execute();
        }
    }

    @Override
    public void close() throws IOException {
        if (this.hopper != null)
            this.hopper.close();
        if (calcTask != null &&
                (calcTask.getStatus() != AsyncTask.Status.FINISHED ||
                        calcTask.isCancelled())
        ) {
            calcTask.cancel(true);
        }
    }


    static class RouteCalcAsyncTask extends AsyncTask<Void, Void, ResponsePath> {
        private final double fromLat;
        private final double fromLng;
        private final double toLat;
        private final double toLng;
        private final GraphHopper hopper;
        private final MutableLiveData<ResponsePath> liveData;

        public RouteCalcAsyncTask(GraphHopper hopper, MutableLiveData<ResponsePath> liveData, double fromLat, double fromLng, double toLat, double toLng) {
            super();
            this.fromLat = fromLat;
            this.fromLng = fromLng;
            this.toLat = toLat;
            this.toLng = toLng;
            this.hopper = hopper;
            this.liveData = liveData;
        }

        protected ResponsePath doInBackground(Void... v) {
            GHRequest req = new GHRequest(fromLat, fromLng, toLat, toLng).setProfile("car");
            req.getHints().putObject(Parameters.Routing.INSTRUCTIONS, true);
            GHResponse resp = hopper.route(req);
            if (resp.getAll().isEmpty())
                return null;
            return resp.getBest();
        }


        protected void onPostExecute(ResponsePath resp) {
            if (resp != null && !resp.hasErrors()) {
                liveData.postValue(resp);
                Log.d(TAG, "onPostExecute: " + resp.getPoints());
                Log.d(TAG, "onPostExecute: " + resp.getInstructions());
            } else {
                Log.e(TAG, "onPostExecute: Errors while calculating the Path");
            }
        }
    }
}
