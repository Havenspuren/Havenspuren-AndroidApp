package de.jadehs.vcg.view_models.factories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.jadehs.vcg.view_models.DetailActivityViewModel

class DetailActivityFactory(private val application: Application,private val id:Long): ViewModelProvider.Factory{


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailActivityViewModel::class.java)){
            return DetailActivityViewModel(application, id) as T
        }else{
            throw IllegalArgumentException("the Viemodel isn't a DetailactivityVeiwModel")
        }
    }
}