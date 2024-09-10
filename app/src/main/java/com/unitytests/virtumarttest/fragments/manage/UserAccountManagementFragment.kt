package com.unitytests.virtumarttest.fragments.manage

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.data.User
import com.unitytests.virtumarttest.databinding.FragmentUserAccountManagementBinding
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.util.hideNavBarVisibility
import com.unitytests.virtumarttest.viewmodel.ProfileVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar

@AndroidEntryPoint
class UserAccountManagementFragment: Fragment() {

    private lateinit var binding: FragmentUserAccountManagementBinding
    private val viewModel by viewModels<ProfileVM>()
    private var imageUri: Uri?= null
    private var gender: String = "Male"
    private val calendar = Calendar.getInstance()
    private lateinit var dob: String
    private lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    // Get the selected image URI
                    imageUri = result.data?.data

                    // Load the selected image into imgUserProfileView
                    Glide.with(this).load(imageUri).into(binding.imgUserProfileView)
                } else {
                    // No image selected, set default image
                    Glide.with(this).load(R.drawable.noprofileicon).into(binding.imgUserProfileView)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserAccountManagementBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideNavBarVisibility()

        binding.btnBackProfileView.setOnClickListener{
            findNavController().navigateUp()
        }

        // Set Date Picker for Date of Birth
        binding.btnPickDob.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                    dob = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                    binding.btnPickDob.text = dob
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        // Set Gender Radio Group Listener
        binding.genderRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = view.findViewById<RadioButton>(checkedId)
            gender = radioButton.text.toString()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                when(it){
                    is Resource.Loading ->{
                        showUserLoading()
                    }
                    is Resource.Success ->{
                        hideUserLoading()
                        showUserInformation(it.data!!)
                    }
                    is Resource.Error ->{
                        Toast.makeText(requireContext(), "Error occurred: ${it.message}", Toast.LENGTH_LONG).show()
                    }else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.updateDetails.collectLatest {
                when(it){
                    is Resource.Loading ->{
                        binding.btnUpdateProfile.startAnimation()
                    }
                    is Resource.Success ->{
                        binding.btnUpdateProfile.revertAnimation()
                        findNavController().navigateUp()
                    }
                    is Resource.Error ->{
                        binding.btnUpdateProfile.revertAnimation()
                        Toast.makeText(requireContext(), "Error occurred: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }

        binding.imgEditImageProfileView.setOnClickListener{
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type="image/*"
            imageActivityResultLauncher.launch(intent)
        }

        binding.btnUpdateProfile.setOnClickListener(){
            updateUserProfile()
        }

    }

    private fun updateUserProfile() {
        binding.apply {
            val email = txtEmailProfileView.text.toString()
            val firstName = profileFirstNameEdt.text.toString().trim()
            val lastName = profileLastNameEdt.text.toString().trim()
            val nic = edtNic.text.toString().trim()
            val phoneNumber = edtPhoneNumber.text.toString().trim()

            val user = User(
                id = viewModel.getUserId(),
                firstname = firstName,
                lastname = lastName,
                email = email,
                dob = dob,
                gender = gender,
                nic = nic,
                phoneNumber = phoneNumber
            )
            viewModel.updateUserDetails(user, imageUri)
        }
    }

    private fun showUserInformation(user: User) {
        binding.apply {

            // Check if the imagePath is null or empty
            val imagePath = user.imagePath
            if (imagePath.isNullOrEmpty()) {
                // Load default profile icon if imagePath is null or empty
                Glide.with(this@UserAccountManagementFragment)
                    .load(R.drawable.noprofileicon)
                    .into(imgUserProfileView)
            } else {
                // Load user's profile image from Firestore
                Glide.with(this@UserAccountManagementFragment)
                    .load(imagePath)
                    .into(imgUserProfileView)
            }


            txtEmailProfileView.text = user.email
            profileFirstNameEdt.setText(user.firstname)
            profileLastNameEdt.setText(user.lastname)
            btnPickDob.text = user.dob
            edtNic.setText(user.nic)
            edtPhoneNumber.setText(user.phoneNumber)

            // Set date if not available
            dob = user.dob ?: ""
            btnPickDob.text = user.dob

            // Set gender selection
            when (user.gender) {
                "Male" -> genderRadioGroup.check(R.id.rbMale)
                "Female" -> genderRadioGroup.check(R.id.rbFemale)
                "Other" -> genderRadioGroup.check(R.id.rbOther)
            }
        }
    }

    private fun showUserLoading() {
        binding.apply{
            prgbrProfileView.visibility = View.VISIBLE
            imgUserProfileView.visibility = View.INVISIBLE
            imgEditImageProfileView.visibility = View.INVISIBLE
            imgEditIconProfileView.visibility = View.INVISIBLE
            txtEmailProfileView.visibility = View.INVISIBLE
            profileFirstNameEdt.visibility = View.INVISIBLE
            profileLastNameEdt.visibility = View.INVISIBLE
            btnUpdateProfile.visibility = View.INVISIBLE
            txtBtnChangePasswordProfileView.visibility = View.INVISIBLE
        }
    }

    private fun hideUserLoading() {
        binding.apply{
            prgbrProfileView.visibility = View.GONE
            imgUserProfileView.visibility = View.VISIBLE
            imgEditImageProfileView.visibility = View.VISIBLE
            imgEditIconProfileView.visibility = View.VISIBLE
            txtEmailProfileView.visibility = View.VISIBLE
            profileFirstNameEdt.visibility = View.VISIBLE
            profileLastNameEdt.visibility = View.VISIBLE
            btnUpdateProfile.visibility = View.VISIBLE
            txtBtnChangePasswordProfileView.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        hideNavBarVisibility()
    }
}