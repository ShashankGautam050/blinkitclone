package com.shashank.userblinkitclone.auth.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.shashank.userblinkitclone.R
import com.shashank.userblinkitclone.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
        private lateinit var bidning : FragmentSplashBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bidning = FragmentSplashBinding.inflate(inflater,container,false)
        Handler(Looper.getMainLooper()).postDelayed({
          findNavController().navigate(R.id.action_splashFragment_to_signinFragment)

        },3000)
        return bidning.root
    }

}