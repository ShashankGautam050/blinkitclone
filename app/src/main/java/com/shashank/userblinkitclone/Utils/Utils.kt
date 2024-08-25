package com.shashank.userblinkitclone.Utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.shashank.userblinkitclone.databinding.ProgressDialogBinding

private lateinit var binding : ProgressDialogBinding
object Utils {

    fun toast(context: Context, message: String){
       Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private var dialog : AlertDialog? = null
    fun showDialog(context: Context, message: String){
         binding = ProgressDialogBinding.inflate(LayoutInflater.from(context))
        binding.progressText.text = message
        dialog = AlertDialog.Builder(context).setView(binding.root).setCancelable(false).create()
        dialog?.show()

    }

    fun hideDialog(){
        dialog?.dismiss()
        dialog = null
    }

    fun getUserId() : String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }
}