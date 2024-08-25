package com.shashank.userblinkitclone.auth.viewmodel

import android.app.Activity
import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.shashank.userblinkitclone.dataclass.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class OtpUiState {
    data object Idle : OtpUiState()
    data object SendingOtp : OtpUiState()
    data object OtpSent : OtpUiState()
    data object VerifyingOtp : OtpUiState()
    data class OtpVerificationSuccess(val user: User) : OtpUiState()
    data class OtpVerificationFailed(val error: String) : OtpUiState()
    data class OtpSendingFailed(val error: String) : OtpUiState()

}

class OtpViewModel : ViewModel() {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _otpUiState = MutableStateFlow<OtpUiState>(OtpUiState.Idle)
    val otpUiState: StateFlow<OtpUiState> = _otpUiState

    private var countTimer: CountDownTimer? = null

    var otpSentFlag = false // Track if OTP has been sent
    private var storedVerificationId: String? = null

    fun sendOtp(phoneNumber: String, activity: Activity) {
        if (otpSentFlag) return

        _otpUiState.value = OtpUiState.SendingOtp

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential.toString())
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _otpUiState.value = OtpUiState.OtpSendingFailed(e.message ?: "OTP sending failed")
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                _otpUiState.value = OtpUiState.OtpSent
                storedVerificationId = verificationId
                startCountdown() // Start the 60 seconds countdown
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun startCountdown() {
        countTimer?.cancel() // Cancel any existing timer
        countTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _otpUiState.value = OtpUiState.Idle // You can update with remaining time if needed
            }

            override fun onFinish() {
                _otpUiState.value = OtpUiState.Idle
                otpSentFlag = false
            }
        }.start()
    }

    fun signInWithPhoneAuthCredential(otp: String) {
        _otpUiState.value = OtpUiState.VerifyingOtp

        val verificationId = storedVerificationId
        if (verificationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        firebaseUser?.let {
                            val user = User(
                                id = it.uid,
                                number = it.phoneNumber,
                                address = "Ghaziabad"
                            )
                            saveUserToDatabase(user)
                            _otpUiState.value = OtpUiState.OtpVerificationSuccess(user)
                        }
                    } else {
                        _otpUiState.value = OtpUiState.OtpVerificationFailed("OTP verification failed")
                    }
                }
        } else {
            _otpUiState.value = OtpUiState.OtpVerificationFailed("Invalid verification ID")
        }
    }

    private fun saveUserToDatabase(user: User) {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        user.id?.let {
            usersRef.child(it).setValue(user)
                .addOnSuccessListener {
                    // Data saved successfully
                }
                .addOnFailureListener {
                    // Handle any errors
                }
        }
    }
}
