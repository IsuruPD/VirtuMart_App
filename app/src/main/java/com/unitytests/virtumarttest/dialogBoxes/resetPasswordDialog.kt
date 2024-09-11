package com.unitytests.virtumarttest.dialogBoxes

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.unitytests.virtumarttest.R

fun Fragment.setupBottomSheetDialog(onSendClick: (String)->Unit){
    val dialog= BottomSheetDialog(requireContext(), R.style.dialogStyle)
    val view= layoutInflater.inflate(R.layout.reset_password,null)
    dialog.setContentView(view)
    dialog.behavior.state= BottomSheetBehavior.STATE_EXPANDED
    dialog.show()

    val edtEmail=view.findViewById<EditText>(R.id.resetPassEdtTxt)
    val errorEmailReset=view.findViewById<TextView>(R.id.textViewResetPasswordError)
    val btnSend=view.findViewById<Button>(R.id.resetPassSendBtn)
    val btnCancel=view.findViewById<Button>(R.id.resetPassCancelBtn)

    btnSend.setOnClickListener{
        val email=edtEmail.text.toString().trim()
        if(email.isEmpty()){
            errorEmailReset.visibility = View.VISIBLE
        }else{
            errorEmailReset.visibility = View.GONE
            onSendClick(email)
            dialog.dismiss()
        }
    }

    btnCancel.setOnClickListener {
        dialog.dismiss()
    }
}