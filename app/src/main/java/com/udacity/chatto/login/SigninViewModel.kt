package com.udacity.chatto.login

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.udacity.chatto.utils.Constants
import com.udacity.chatto.utils.convertToByteArray
import com.udacity.chatto.models.User
import com.udacity.chatto.utils.FirebaseUserLiveData
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern

class SigninViewModel : ViewModel() {
    private  var auth: FirebaseAuth= Firebase.auth
    val db = Firebase.firestore
    val storage=Firebase.storage
    private val _modesignin= MutableLiveData(true)
    val modesignin: LiveData<Boolean>
        get() = _modesignin
    private val _selctedimage=MutableLiveData<Bitmap?>()
    val selctedimage:LiveData<Bitmap?>
         get() = _selctedimage
    val name= MutableLiveData("")
    val email= MutableLiveData("")
    val password= MutableLiveData("")
    //handle error statements
    val nameerror= MutableLiveData(0)
    val emailerror= MutableLiveData(0)
    val passworderror= MutableLiveData(0)
    private  val _imageerror= MutableLiveData(false)
    val imageerror:LiveData<Boolean>
        get()=_imageerror
    private val _loading=MutableLiveData(false)
    val loading:LiveData<Boolean>
        get() =_loading
    private val _navigate_to_home_fragment=MutableLiveData(false)
    val verified= Transformations.map(FirebaseUserLiveData()){
        !(it==null||!it.isEmailVerified)
    }
    val navigate_to_home_fragment:LiveData<Boolean>
        get() = _navigate_to_home_fragment
    fun authintication_state_change(){
        _modesignin.value= _modesignin.value!!.not()
    }

    fun validate_and_sumbit(){

       if( valdiate())
           sumbit()
    }

    private fun sumbit() {
        viewModelScope.launch {
            _loading.value=true
            if(_modesignin.value!!){
                try_sign_in()
            }
            else{
                try_sign_up()
            }
            _loading.value=false
        }
    }

    private suspend fun try_sign_up() {
        try {
            val res=auth.createUserWithEmailAndPassword(email.value!!,password.value!!).await()
            res.user?.apply {
                var ref= storage.getReference().child("userimages")
                ref=ref.child(this.uid+".jpg")
                val tasksnapshot= ref.putBytes(_selctedimage.value!!.convertToByteArray()).await()
                val uri=tasksnapshot.storage.downloadUrl.await()
                val user= User(email!!,uri.toString(),name.value!!, emptyList(), emptyList())
                db.collection("users").document(res.user!!.uid).set(user).await()
                _navigate_to_home_fragment.value = true
            }
        }catch (e:Exception){
            if(auth.currentUser!=null){
                auth.currentUser!!.delete().await()
            }
            e.message?.apply {
                if(this.contains("no user record corresponding")){
                    emailerror.value=2
                }
                else if(this.contains("The password is invalid")){
                    passworderror.value=2
                }
                else if(this.contains("The email address is already in use by another account"))
                {
                    emailerror.value=3
                }
                Log.e(Constants.Firebase,e.message.toString())
            }
        }

    }

    private suspend fun try_sign_in() {
        try {
            val res=  auth.signInWithEmailAndPassword(email.value!!,password.value!!).await()
            _navigate_to_home_fragment.value=true
        }catch (e:Exception){
            e.message?.apply {
                if(this.contains("no user record corresponding")){
                   emailerror.value=2
                }
                else if(this.contains("The password is invalid")){
                   passworderror.value=2
                }
                Log.e(Constants.Firebase,e.message.toString())
            }
        }
    }

    private fun valdiate():Boolean {
        var haserror=false
        var regex = ("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")
        val emailpattern = Pattern.compile(regex)
        regex="^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,20}$"
        val passwordpattern = Pattern.compile(regex)
        if(!_modesignin.value!!){
           if( name.value.isNullOrEmpty()){
               nameerror.value=1
               haserror=true
           }else{
               nameerror.value=0
           }
            if(_selctedimage.value==null){
                _imageerror.value=true
                haserror=true
            }
            else{
                _imageerror.value=false
            }
        }
        if(email.value.isNullOrEmpty()||!emailpattern.matcher(email.value!!).matches()){
            emailerror.value=1
            haserror=true
        }
        else{
            emailerror.value=0
        }
        if(password.value.isNullOrEmpty()||!passwordpattern.matcher(password.value!!).matches()){
            passworderror.value=1
            haserror=true
        }
        else{
            passworderror.value=0
        }
        return !haserror
    }
    fun pickimage( selectedimage:Bitmap){
        viewModelScope.launch {
            var baos = ByteArrayOutputStream()
            var qauility=55
            selectedimage.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            while (baos.toByteArray().size > 9 * 1024) {
                baos = ByteArrayOutputStream()
                selectedimage.compress(Bitmap.CompressFormat.JPEG, qauility, baos)
                qauility -= 5
                if (qauility <= 5) break
            }

            _selctedimage.value=selectedimage
        }
    }
    fun clear_passworderror(){
        passworderror.value=0
    }
    fun clear_nameerror(){
        nameerror.value=0
    }
    fun clear_emailerror(){
        emailerror.value=0
    }
    fun clear_everything() {
        name.value=""
        password.value=""
        email.value=""
        _navigate_to_home_fragment.value=false
        _selctedimage.value=null
        _modesignin.value=true
        nameerror.value=0
        passworderror.value=0
        emailerror.value=0
    }
    fun signwithgoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _loading.value=true
            try {
                val credintal=GoogleAuthProvider.getCredential(account.idToken,null)
                auth.signInWithCredential(credintal).await()
                val res= db.collection("users").document(auth.currentUser!!.uid).get().await()
                if (!res.exists()){
                    val user=User(auth.currentUser!!.email,auth.currentUser!!.photoUrl.toString()
                        ,auth.currentUser!!.displayName, emptyList(), emptyList())
                    db.collection("users").document(auth.currentUser!!.uid).set(user).await()
                }
                _navigate_to_home_fragment.value=true
            }catch (e:Exception){
                Log.e(Constants.Firebase,e.message.toString())
            }
            _loading.value=false
        }
    }
}