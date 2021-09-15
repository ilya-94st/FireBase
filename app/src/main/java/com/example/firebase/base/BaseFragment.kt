package com.example.firebase.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.example.firebase.tools.SnackBar
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseFragment<B: ViewDataBinding> : Fragment() {
    lateinit var binding: B

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, getBinding(),container, false)
        return binding.root

    }
    abstract fun getBinding(): Int

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
    fun inVisibleMenu(menuID: Int) {
        requireActivity().findViewById<BottomNavigationView>(menuID).visibility = View.INVISIBLE
    }
    fun visibleMenu(menuID: Int) {
        requireActivity().findViewById<BottomNavigationView>(menuID).visibility = View.VISIBLE
    }
    fun snackBar(text: String) {
        SnackBar(binding.root ,text)
    }
}

