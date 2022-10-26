package com.udacity.chatto

import android.graphics.Bitmap
import androidx.constraintlayout.widget.ConstraintSet.Transform
import androidx.lifecycle.*
import com.udacity.chatto.models.User
import com.udacity.chatto.utils.FirebaseCloudUser

class MainViewmodel: ViewModel()  {
    val uid=MutableLiveData("")
    private val _homeframgent=MutableLiveData(true)

    val search=MutableLiveData("")
    val user=Transformations.map(uid){
        FirebaseCloudUser(it).map {
            if(it==null)
                User(uid.value!!,"","", emptyList(), emptyList())
            else
                it
        }
    }
    val homefragment:LiveData<Boolean>
        get() =_homeframgent


}