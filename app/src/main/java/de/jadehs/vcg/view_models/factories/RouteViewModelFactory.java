package de.jadehs.vcg.view_models.factories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

public class RouteViewModelFactory implements ViewModelProvider.Factory {

    private static final String DEFAULT_KEY = RouteViewModelFactory.class.getCanonicalName() + ".Default_Key";


    private final Application application;
    private final long routeId;

    public RouteViewModelFactory(@NonNull Application application, long routeId) {
        this.application = application;
        this.routeId = routeId;
    }

    public <T extends ViewModel> T getViewModel(ViewModelStoreOwner storeOwner, Class<T> viewModelClazz) {
        return new ViewModelProvider(storeOwner, this)
                .get(
                        this.getViewModelKey(viewModelClazz.getCanonicalName()),
                        viewModelClazz
                );
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

    /**
     * returns a key, which identifies the viewmodel created by this factory with the given factory arguments.
     *
     * @return
     */
    public String getViewModelKey(String className) {
        return String.format(Locale.ROOT, "%s:%s.%d", RouteViewModelFactory.class.getName(), className, this.routeId);
    }

    public String getViewModelKey() {
        return this.getViewModelKey(DEFAULT_KEY);
    }
}
