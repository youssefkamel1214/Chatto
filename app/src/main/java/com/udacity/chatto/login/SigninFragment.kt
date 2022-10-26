package com.udacity.chatto.login

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.udacity.chatto.MainViewmodel
import com.udacity.chatto.R
import com.udacity.chatto.databinding.FragmentSigninBinding
import com.udacity.chatto.utils.Constants
import java.util.*


class SigninFragment : Fragment() {
    private lateinit var viewModel: SigninViewModel
    private lateinit var _binding: FragmentSigninBinding
    val  callbackManager = CallbackManager.Factory.create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSigninBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(SigninViewModel::class.java)
        checkifloggedin()
        _binding.lifecycleOwner=this
        _binding.viewModel=viewModel
        _binding.pickgallery.setOnClickListener {
            pick_image_from_gallery()
        }
        _binding.pickcamera.setOnClickListener {
            pick_image_from_camera()
        }
        _binding.googleButton.setOnClickListener {
            google_signin()
        }
        _binding.facebookButton.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email","public_profile"))
        }
        handlefacebooklogin()
        viewModel.imageerror.observe(viewLifecycleOwner) { value ->
            if (value) {
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.profile_image_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        viewModel.navigate_to_home_fragment.observe(viewLifecycleOwner){
            if(it) {
                val mainveiwmodel=ViewModelProvider(requireActivity()).get(MainViewmodel::class.java)
                mainveiwmodel.uid.value=Firebase.auth.uid
                viewModel.clear_everything()
                findNavController().navigate(SigninFragmentDirections.actionSigninFragmentToHomeFragment())
            }
        }
    }

    private fun handlefacebooklogin() {
        // Callback registration
        // Callback registration
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    loginResult?.apply {
                        accessToken?.apply {
                            viewModel.signinwithfacebook(this)
                        }
                    }
                }

                override fun onCancel() {
                    Toast.makeText(requireContext(),getString(R.string.facebook_cancel),Toast.LENGTH_LONG).show()
                }

                override fun onError(exception: FacebookException) {
                    Log.e(Constants.signin_frag_debug,exception.message.toString())
                }
            })
    }
        private fun checkifloggedin() {
        val auth: FirebaseAuth = Firebase.auth
        if(auth.currentUser!=null){
            val mainveiwmodel=ViewModelProvider(requireActivity()).get(MainViewmodel::class.java)
            mainveiwmodel.uid.value=Firebase.auth.uid
            findNavController().navigate(SigninFragmentDirections.actionSigninFragmentToHomeFragment())
        }
    }

    private fun pick_image_from_camera() {
        if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, Constants.Pick_Image_camera_Code)
        }
        else{
            requestPermissions(arrayOf(Manifest.permission.CAMERA), Constants.camera_permissio);
        }


    }
    private fun google_signin(){
        val googleSignInOptions = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        ).requestIdToken(requireContext().getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(
            requireActivity(), googleSignInOptions
        )
        startActivityForResult(googleSignInClient.signInIntent,Constants.Google_Signin)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode== Constants.camera_permissio){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                pick_image_from_camera()
            }
            else{
                Toast.makeText(requireContext(),requireContext().getString(R.string.denied_camera),Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pick_image_from_gallery() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, Constants.Pick_Image_Code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
               Constants.Pick_Image_Code   -> {
                   if (resultCode == RESULT_OK) {
                       val imageurl = data?.data!!
                       val inputStream = requireContext().contentResolver.openInputStream(imageurl)
                       val selectedImage = BitmapFactory.decodeStream(inputStream)
                       viewModel.pickimage(selectedImage)
                   }
               }
              Constants.Pick_Image_camera_Code->{
                  if(resultCode== RESULT_OK){
                      val photo =data?.extras?.get("data") as Bitmap
                      viewModel.pickimage(photo)
                  }
              }
               Constants.Google_Signin->{
                   if(resultCode== RESULT_OK)
                       handleGoogleSignin(data)
               }
           }
       }

    private fun handleGoogleSignin(data: Intent?) {
        data?.apply {
            val signInAccountTask: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            if(signInAccountTask.isSuccessful) {
                try {
                    val googleSignInAccount = signInAccountTask
                        .getResult(ApiException::class.java)
                    if(googleSignInAccount!=null){
                        viewModel.signwithgoogle(googleSignInAccount)
                    }
                }catch (e:ApiException){
                    Log.e(Constants.signin_frag_debug,e.message.toString())
                }
            }
            else if(signInAccountTask.isCanceled){
                Toast.makeText(requireContext(),getString(R.string.google_sign_in_fail),Toast.LENGTH_LONG).show()
            }
            else{
                Log.e(Constants.signin_frag_debug,signInAccountTask.exception!!.message.toString())
            }

        }
    }
}
