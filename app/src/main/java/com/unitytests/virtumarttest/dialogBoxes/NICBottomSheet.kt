package com.unitytests.virtumarttest.dialogBoxes

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unitytests.virtumarttest.R

class NICBottomSheet : BottomSheetDialogFragment() {

    private lateinit var nic: String
    private lateinit var onConfirmNICListener: (String) -> Unit

    companion object {
        private const val ARG_NIC = "arg_nic"

        fun newInstance(nic: String, onConfirmNIC: (String) -> Unit): NICBottomSheet {
            val fragment = NICBottomSheet()
            fragment.onConfirmNICListener = onConfirmNIC
            val args = Bundle().apply {
                putString(ARG_NIC, nic)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            nic = it.getString(ARG_NIC, "")
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
        return inflater.inflate(R.layout.bottom_sheet_identity_number, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nicEditText = view.findViewById<EditText>(R.id.edtNicDialog)
        val errorTextView = view.findViewById<TextView>(R.id.textViewNICError)
        nicEditText.setText(nic)

        val confirmButton = view.findViewById<Button>(R.id.btnConfirmNICDialog)
        confirmButton.setOnClickListener {
            val updatedNIC = nicEditText.text.toString().trim()

            // Validation check
            val nicRegex = "^[0-9]{12}$|^[0-9]{9}[Vv]$".toRegex()

            if (!nicRegex.matches(updatedNIC)) {
                errorTextView.text = "Invalid NIC format. Use 12 digits or 9 digits followed by 'v'."
                errorTextView.visibility = View.VISIBLE
            } else {
                errorTextView.visibility = View.GONE
                onConfirmNICListener(updatedNIC)
                dismiss()
            }
        }
    }
}