package com.unitytests.virtumarttest.dialogBoxes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unitytests.virtumarttest.R
import java.util.Calendar

class DateOfBirthBottomSheet : BottomSheetDialogFragment() {

    private var dob: String? = null
    private lateinit var onConfirmDOBListener: (String) -> Unit

    companion object {
        private const val ARG_DOB = "arg_dob"

        fun newInstance(dob: String, onConfirmDOB: (String) -> Unit): DateOfBirthBottomSheet {
            val fragment = DateOfBirthBottomSheet()
            fragment.onConfirmDOBListener = onConfirmDOB
            val args = Bundle().apply {
                putString(ARG_DOB, dob)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dob = it.getString(ARG_DOB)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_dob, container, false)
    }

// For styling
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return BottomSheetDialog(requireContext(), R.style.dialogStyle)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val datePicker = view.findViewById<DatePicker>(R.id.datePickerDOBDialog)
        val errorTextView = view.findViewById<TextView>(R.id.textViewDOBError)

        // Set the initial value of the date picker with the dob value if it's not null
        val calendar = Calendar.getInstance()
        dob?.let {
            val dobParts = it.split("-")
            if (dobParts.size == 3) {
                calendar.set(Calendar.YEAR, dobParts[0].toInt())
                calendar.set(Calendar.MONTH, dobParts[1].toInt() - 1)
                calendar.set(Calendar.DAY_OF_MONTH, dobParts[2].toInt())
            }
        }

        datePicker.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
            null
        )

        val confirmButton = view.findViewById<Button>(R.id.btnConfirmDOBDialog)
        confirmButton.setOnClickListener {
            val day = datePicker.dayOfMonth
            val month = datePicker.month + 1
            val year = datePicker.year

            // Get the selected date
            val selectedDate = Calendar.getInstance().apply {
                set(year, month - 1, day)
            }
            // Get the current date
            val currentDate = Calendar.getInstance()

            // Check if the selected date is earlier than the current date
            if (selectedDate.after(currentDate)) {
                errorTextView.text = "Selected date cannot be in the future"
                errorTextView.visibility = View.VISIBLE
            } else {
                errorTextView.visibility = View.GONE

                val formattedDOB = String.format("%04d-%02d-%02d", year, month, day)
                onConfirmDOBListener(formattedDOB)
                dismiss()
            }
        }
    }
}
