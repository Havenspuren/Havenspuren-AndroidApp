package de.jadehs.vcg.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

/**
 * Helper Class which manages one value provider of the MediatorLiveData
 * @param <T> type of the value which gets wrapped by the LiveData instance
 */
public class ReplaceableLiveData<T> extends MediatorLiveData<T> {
    private LiveData<T> source;
    private Observer<T> defaultObserver = new Observer<T>() {
        @Override
        public void onChanged(T value) {
            setValue(value);
        }
    };

    /**
     * removes the current LiveData value provider and sets the given LiveData as value provider
     * @param source the new value provider
     */
    public void setCurrentSource(LiveData<T> source){
        if(this.source != null)
            this.removeSource(this.source);
        this.source = source;
        if(this.source != null)
            this.addSource(this.source, defaultObserver);
    }
}
