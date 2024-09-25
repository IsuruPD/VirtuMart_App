package com.unitytests.virtumarttest.dialogBoxes

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unitytests.virtumarttest.R

class PhoneNumberBottomSheet : BottomSheetDialogFragment() {

    private lateinit var phoneNumber: String
    private lateinit var onConfirmPhoneNumberListener: (String) -> Unit

    companion object {
        private const val ARG_PHONE_NUMBER = "arg_phone_number"

        fun newInstance(phoneNumber: String, onConfirmPhoneNumber: (String) -> Unit): PhoneNumberBottomSheet {
            val fragment = PhoneNumberBottomSheet()
            fragment.onConfirmPhoneNumberListener = onConfirmPhoneNumber
            val args = Bundle().apply {
                putString(ARG_PHONE_NUMBER, phoneNumber)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            phoneNumber = it.getString(ARG_PHONE_NUMBER, "")
        }
    }

// For styling
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return BottomSheetDialog(requireContext(), R.style.dialogStyle)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_phone_number, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val phoneNumberEditText = view.findViewById<EditText>(R.id.edtPhoneNumberDialog)
        val errorTextView = view.findViewById<TextView>(R.id.textViewContactError)
        phoneNumberEditText.setText(phoneNumber)

        val confirmButton = view.findViewById<Button>(R.id.btnConfirmContactDialog)
        confirmButton.setOnClickListener {
            val updatedPhoneNumber = phoneNumberEditText.text.toString().trim()

            if(updatedPhoneNumber.isEmpty() || updatedPhoneNumber.length != 10){
                errorTextView.visibility = View.VISIBLE
            } else{
                errorTextView.visibility = View.GONE
                onConfirmPhoneNumberListener(updatedPhoneNumber)
                dismiss()
            }
        }
    }
}