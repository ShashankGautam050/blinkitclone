package com.shashank.userblinkitclone.auth.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.shashank.userblinkitclone.R
import com.shashank.userblinkitclone.auth.adapter.SingleImagePagerAdapter
import com.shashank.userblinkitclone.databinding.FragmentSigninBinding
import com.shashank.userblinkitclone.auth.viewmodel.SignInViewModel
import java.util.Timer
import java.util.TimerTask

class SigninFragment : Fragment() {

    private lateinit var binding: FragmentSigninBinding
    private lateinit var signViewModel : SignInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSigninBinding.inflate(inflater, container, false)

        // Ensure the animation XML file exists and is correctly named
        val images : Int = (R.drawable.image_prod)
        val adapter = SingleImagePagerAdapter(images)
        binding.viewpager.adapter = adapter
        
        autoScrollViewPager(binding.viewpager)
        getUserName()
        onContineButtonClicked()
        // Inflate the layout for this fragment
        signViewModel = ViewModelProvider(this)[SignInViewModel::class.java]
        return binding.root
    }

    private fun onContineButtonClicked() {
        binding.continueBtn.setOnClickListener {
            val phoneNumber = binding.phoneNumberEt.text.toString()
            val bundle = Bundle()
            bundle.putString("phoneNumber", phoneNumber)
            findNavController().navigate(R.id.action_signinFragment_to_otpFragment, bundle)

        }
    }

    private fun getUserName() {
        binding.phoneNumberEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {


            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userNumber = signViewModel.getUserNumber(s.toString())
                binding.continueBtn.isEnabled = userNumber.length == 10
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun autoScrollViewPager(viewpager: ViewPager) {
        val handler = Handler(Looper.getMainLooper())
        val update = Runnable {
        val nextItem = viewpager.currentItem + 1
            viewpager.setCurrentItem(nextItem)
        }
        val timer = Timer()
        timer.schedule(object : TimerTask(){
            override fun run() {
                handler.post(update)
            }

        } ,3000,2000)

    }
}
