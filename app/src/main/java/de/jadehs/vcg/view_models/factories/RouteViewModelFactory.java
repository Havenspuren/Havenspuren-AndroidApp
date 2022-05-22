package de.jadehs.vcg.view_models.factories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class RouteViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;

    private final long routeId;

    public RouteViewModelFactory(@NonNull Application application, long routeId) {
        this.application = application;
        this.routeId = routeId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        try {
            Constructor<T> constructor = modelClass.getConstructor(Application.class, Long.TYPE);
            return constructor.newInstance(application, routeId);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Couldn't instantiate the viewmodel", e);
        }
    }
}
