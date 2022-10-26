package com.udacity.chatto.home

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.udacity.chatto.utils.FirebaseUserLiveData

class HomeViewModel : ViewModel() {
    val uid=Transformations.map(FirebaseUserLiveData()){
        if(it==null)
            null
        else{
            it.uid
        }
    }
}