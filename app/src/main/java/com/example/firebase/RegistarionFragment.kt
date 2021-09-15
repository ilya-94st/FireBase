package com.example.firebase

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.firebase.base.BaseFragment
import com.example.firebase.databinding.FragmentRegistarionBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.lang.Exception


class RegistarionFragment : BaseFragment<FragmentRegistarionBinding>(){
    lateinit var auth:FirebaseAuth
    private val PHOTO_CAMERA = 1
    private var landPhoto: Bitmap? = null
    private val PHOTO_KEY = "keyPhoto"
    override fun getBinding() = R.layout.fragment_registarion
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    auth = FirebaseAuth.getInstance()
        binding.btnRegister.setOnClickListener {
            registerUser()
        }
        binding.btnLogin.setOnClickListener {
            loginUser()
        }
        binding.ivProfilePicture.setOnClickListener {
            camera()
        }
       binding.btnUpdateProfile.setOnClickListener {
           updateProfile()
       }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateProfile() {
        auth.currentUser?.let {user->
            val username = binding.etUsername.text.toString()
            val uri = requireContext().drawableToUri(R.drawable.cat)
            val userPhoto = Uri.parse(uri.toString())
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                  .setPhotoUri(userPhoto) // закидываем наше фото через Uri.parse
                .build()

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    user.updateProfile(profileUpdates).await()
                    checkLoggedInState()
                    withContext(Dispatchers.Main){
                        snackBar("succseful")
                    }
                }catch (e: Exception){
                    withContext(Dispatchers.Main){
                        snackBar("${e}")
                    }
                }
            }
        }
    }
    fun Context.drawableToUri(drawable: Int):Uri{
        return Uri.parse("android.resource://$packageName/$drawable")
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun encodePhoto(photo: Bitmap?): String? {
        val bos = ByteArrayOutputStream()
        photo?.compress(Bitmap.CompressFormat.PNG, 0, bos)
        val byteArray: ByteArray = bos.toByteArray()
        return java.util.Base64.getEncoder().encodeToString(byteArray)
    }

fun camera() {
        when {
            ContextCompat.checkSelfPermission(
                context as Context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                try {
                    startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE),PHOTO_CAMERA)
                }catch (e:Exception){
                    Toast.makeText(context, "No camera", Toast.LENGTH_SHORT).show()
                } // Будет срабатывать каждый раз как мы потвердили свое согласие
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                Manifest.permission.CAMERA
            ) -> {
                view?.let {
                    binding.registarionFragment.showSnackbar(
                        it,
                        getString(R.string.permission_required),   //В строке обесняемае последствия включение Permision рекомендует Гугл
                        Snackbar.LENGTH_INDEFINITE,
                        getString(R.string.ok) // Строка ок
                    ) {
                        requestPermissionLauncher.launch(
                            Manifest.permission.CAMERA
                        )
                    }
                }
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA // Если отконили
                )
            }
        }
}

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(context, "Granted", Toast.LENGTH_SHORT).show()
                val takePhoto = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                try {
                    startActivityForResult(takePhoto,PHOTO_CAMERA)
                }catch (e:Exception){
                    Toast.makeText(context, "No camera", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Dineted", Toast.LENGTH_SHORT).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==PHOTO_CAMERA && resultCode == Activity.RESULT_OK){
            landPhoto = data?.extras?.get("data") as Bitmap
            snackBar("ok")
            binding.ivProfilePicture.setImageBitmap(landPhoto)
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(PHOTO_KEY,landPhoto)
    }

    private fun registerUser() {
        val email = binding.etEmailRegister.text.toString()
        val password = binding.etPasswordRegister.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.createUserWithEmailAndPassword(email, password).await()
                    withContext(Dispatchers.Main) {
                        checkLoggedInState()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun loginUser() {
        val email = binding.etEmailLogin.text.toString()
        val password = binding.etPasswordLogin.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.signInWithEmailAndPassword(email, password).await()
                    withContext(Dispatchers.Main) {
                        checkLoggedInState()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    fun View.showSnackbar( // Снек Бар
        view: View,
        msg: String,
        length: Int,
        actionMessage: CharSequence?,
        action: (View) -> Unit
    ) {
        val snackbar = Snackbar.make(view, msg, length)
        if (actionMessage != null) {
            snackbar.setAction(actionMessage) {
                action(this)
            }.show()
        } else {
            snackbar.show()
        }
    }
    private fun checkLoggedInState() {
        val user = auth.currentUser
        if (auth.currentUser == null) { // not logged in
            binding.tvLoggedIn.text = "You are not logged in"
        } else {
            binding.tvLoggedIn.text = "You are logged in!"
            binding.etUsername.setText(user?.displayName)
            binding.ivProfilePicture.setImageURI(user?.photoUrl) // устанавливаем наше фото загружанное в firebase
        }
    }

    override fun onStart() {
        super.onStart()
        checkLoggedInState()
    }
}