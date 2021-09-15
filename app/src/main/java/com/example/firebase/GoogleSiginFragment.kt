package com.example.firebase

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.firebase.base.BaseFragment
import com.example.firebase.databinding.FragmentGoogleSiginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

const val REQUEST_CODE_GOOLE =0

class GoogleSiginFragment : BaseFragment<FragmentGoogleSiginBinding>() {
    lateinit var auth: FirebaseAuth
    override fun getBinding() = R.layout.fragment_google_sigin
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        binding.buttonSignin.setOnClickListener {
            val options = GoogleSignInOptions.Builder()
                .requestIdToken(resources.getString(R.string.webclient_id))
                .requestEmail()
                .build()

            val googleSigin = GoogleSignIn.getClient(context as Activity, options)
            googleSigin.signInIntent.also {
                startActivityForResult(it, REQUEST_CODE_GOOLE)
            }
        }
    }
    private fun googleAuthForFireBase(account: GoogleSignInAccount) {
        val credentonal = GoogleAuthProvider.getCredential(account.idToken, null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
auth.signInWithCredential(credentonal).await()
                withContext(Dispatchers.Main){
                    snackBar("succsseful")
                }
            }catch (e: Exception){
              withContext(Dispatchers.Main){
                  snackBar("${e}")
              }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_GOOLE){
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
            account?.let {
                googleAuthForFireBase(it)
            }
        }
    }

}