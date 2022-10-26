package com.udacity.chatto.utils

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.udacity.chatto.models.User
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

fun Bitmap.convertToByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 100, stream)
    val image = stream.toByteArray()
    //return bitmap's pixels
    return image
}
class FirebaseUserLiveData : LiveData<FirebaseUser?>() {
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        value=firebaseAuth.currentUser
    }
    override fun onActive() {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onInactive() {
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}
class FirebaseCloudUser(val document: String?): LiveData<User?>() {
    private val db=Firebase.firestore
    private var lithner: ListenerRegistration? = null

    override fun onActive() {
        if(lithner==null&&!document.isNullOrEmpty()) {
            lithner = db.collection("users").document(document)
                .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
                    if (e != null) {
                        Log.e(Constants.Firebase, e.message!!)
                        value = null
                    } else {
                        value = snapshot?.toObject<User>()
                    }
                }
        }
    }
    override fun onInactive() {
       lithner?.apply {
           remove()
       }
    }

}