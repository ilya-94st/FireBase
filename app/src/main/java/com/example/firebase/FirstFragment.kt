package com.example.firebase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.firebase.base.BaseFragment
import com.example.firebase.databinding.FragmentFirstBinding

class FirstFragment : BaseFragment<FragmentFirstBinding>(){
    override fun getBinding() = R.layout.fragment_first
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.button3.setOnClickListener {
            findNavController().navigate(R.id.action_firstFragment_to_registarionFragment)
        }
        binding.buttonGoogleSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_firstFragment_to_googleSiginFragment)
        }
        binding.buttonSaveData.setOnClickListener {
            findNavController().navigate(R.id.action_firstFragment_to_saveDataFragment)
        }
        binding.buttonUploadFileImage.setOnClickListener {
            findNavController().navigate(R.id.action_firstFragment_to_uploadFileFragment)
        }
    }

}