package com.unitytests.virtumarttest.dialogBoxes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unitytests.virtumarttest.R

class GenderBottomSheet : BottomSheetDialogFragment() {

    private var gender: String? = null
    private lateinit var onConfirmGenderListener: (String) -> Unit

    companion object {
        private const val ARG_GENDER = "arg_gender"

        fun newInstance(gender: String, onConfirmGender: (String) -> Unit): GenderBottomSheet {
            val fragment = GenderBottomSheet()
            fragment.onConfirmGenderListener = onConfirmGender
            val args = Bundle().apply {
                putString(ARG_GENDER, gender)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gender = it.getString(ARG_GENDER) ?: "Male" // Default to "Male" if gender is null
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
        return inflater.inflate(R.layout.bottom_sheet_gender, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupGenderDialog)

        when (gender) {
            "Male" -> radioGroup.check(R.id.radioMale)
            "Female" -> radioGroup.check(R.id.radioFemale)
            "Other" -> radioGroup.check(R.id.radioOther)
        }

        val confirmButton = view.findViewById<Button>(R.id.btnConfirmGenderDialog)
        confirmButton.setOnClickListener {
            val selectedGender = when (radioGroup.checkedRadioButtonId) {
                R.id.radioMale -> "Male"
                R.id.radioFemale -> "Female"
                R.id.radioOther -> "Other"
                else -> gender ?: "Male"
            }
            onConfirmGenderListener(selectedGender)
            dismiss()
        }
    }
}
