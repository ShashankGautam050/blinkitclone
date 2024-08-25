package com.shashank.userblinkitclone.auth.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputFilter
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.shashank.userblinkitclone.Utils.Utils
import com.shashank.userblinkitclone.auth.viewmodel.OtpUiState
import com.shashank.userblinkitclone.auth.viewmodel.OtpViewModel
import com.shashank.userblinkitclone.databinding.FragmentOtpBinding
import kotlinx.coroutines.launch

class OtpFragment : Fragment() {

    private lateinit var binding: FragmentOtpBinding
    private val otpViewModel: OtpViewModel by viewModels()
    private lateinit var phoneNumber: String
    private var countDownTimer: CountDownTimer? = null
    private var isCountdownRunning = false
    private var countDownTime: Long = 60000 // Set default countdown time to 60 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Restore state if it exists
        savedInstanceState?.let {
            otpViewModel.otpSentFlag = it.getBoolean("otpSentFlag", otpViewModel.otpSentFlag)
            isCountdownRunning = it.getBoolean("isCountdownRunning", isCountdownRunning)
            countDownTime = it.getLong("countDownTime", countDownTime)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOtpBinding.inflate(inflater, container, false)
        phoneNumber = arguments?.getString("phoneNumber").toString()
        binding.phoneNumber.text = "+91-$phoneNumber"

        // Only send OTP if it hasn't been sent already and the timer is not running
        if (!otpViewModel.otpSentFlag && !isCountdownRunning) {
            sendOtp(phoneNumber)
        }

        setEditTextListeners(binding.otp1, binding.otp2, null)
        setEditTextListeners(binding.otp2, binding.otp3, binding.otp1)
        setEditTextListeners(binding.otp3, binding.otp4, binding.otp2)
        setEditTextListeners(binding.otp4, binding.otp5, binding.otp3)
        setEditTextListeners(binding.otp5, binding.otp6, binding.otp4)
        setEditTextListeners(binding.otp6, null, binding.otp5)

        lifecycleScope.launch {
            otpViewModel.otpUiState.collect { state ->
                when (state) {
                    is OtpUiState.SendingOtp -> {
                        Utils.showDialog(requireContext(), "Sending OTP")
                        binding.resendOtp.visibility = View.GONE
                        binding.tvTimer.visibility = View.VISIBLE
                    }
                    is OtpUiState.OtpSent -> {
                        Utils.hideDialog()
                        Utils.toast(requireContext(), "OTP sent successfully")
                        if (!isCountdownRunning) { // Start countdown only if it's not already running
                            startCountdown() // Start countdown only after OTP is sent successfully
                        }
                    }
                    is OtpUiState.VerifyingOtp -> Utils.showDialog(requireContext(), "Verifying OTP")
                    is OtpUiState.OtpVerificationSuccess -> {
                        Utils.hideDialog()
                        Utils.toast(requireContext(), "OTP verified")
                        // Navigate to the next screen or handle successful OTP verification
                    }
                    is OtpUiState.OtpVerificationFailed -> {
                        Utils.hideDialog()
                        Utils.toast(requireContext(), state.error)
                    }
                    is OtpUiState.OtpSendingFailed -> {
                        Utils.hideDialog()
                        Utils.toast(requireContext(), state.error)
                        resetResendButton() // Reset the resend button on failure
                    }
                    OtpUiState.Idle -> {
                        // Do nothing or handle idle state
                    }
                }
            }
        }

        binding.verifyBtn.setOnClickListener {
            verifyOTP()
        }

        binding.resendOtp.setOnClickListener {
            resendOtp()
        }

        return binding.root
    }

    private fun sendOtp(phoneNumber: String) {
        otpViewModel.sendOtp(phoneNumber, requireActivity())
        binding.resendOtp.visibility = View.GONE
        binding.tvTimer.visibility = View.VISIBLE
        otpViewModel.otpSentFlag = true // Mark OTP as sent
    }

    private fun verifyOTP() {
        val otp = listOf(
            binding.otp1.text.toString(),
            binding.otp2.text.toString(),
            binding.otp3.text.toString(),
            binding.otp4.text.toString(),
            binding.otp5.text.toString(),
            binding.otp6.text.toString()
        ).joinToString("")

        if (otp.length == 6) {
            otpViewModel.signInWithPhoneAuthCredential(otp)
        } else {
            Utils.toast(requireContext(), "Please enter a valid 6-digit OTP")
        }
    }

    private fun startCountdown() {
        isCountdownRunning = true // Mark that the countdown is running

        countDownTimer = object : CountDownTimer(countDownTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.tvTimer.text = "Resend OTP in $secondsRemaining seconds"
            }

            override fun onFinish() {
                binding.tvTimer.visibility = View.GONE
                binding.resendOtp.visibility = View.VISIBLE
                isCountdownRunning = false // Reset the flag when the countdown finishes
            }
        }
        countDownTimer?.start()
    }

    private fun resendOtp() {
        if (!isCountdownRunning) { // Ensure OTP is only sent when the countdown has finished
            sendOtp(phoneNumber)
        }
    }

    private fun resetResendButton() {
        binding.tvTimer.visibility = View.GONE
        binding.resendOtp.visibility = View.VISIBLE
        isCountdownRunning = false // Reset the flag if there's an error
    }

    private fun setEditTextListeners(
        currentET: EditText,
        nextET: EditText?,
        previousET: EditText?
    ) {
        currentET.filters = arrayOf(InputFilter.LengthFilter(1))

        currentET.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (currentET.text.isEmpty() && previousET != null) {
                    previousET.requestFocus()
                }
            } else if (event.action == KeyEvent.ACTION_UP && currentET.text.length == 1 && nextET != null) {
                nextET.requestFocus()
            }
            false
        }

        currentET.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && currentET.text.length == 1 && nextET != null) {
                nextET.requestFocus()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the state to retain it across configuration changes
        outState.putBoolean("otpSentFlag", otpViewModel.otpSentFlag)
        outState.putBoolean("isCountdownRunning", isCountdownRunning)
        outState.putLong("countDownTime", countDownTime)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel() // Cancel countdown timer to avoid memory leaks
    }
}
