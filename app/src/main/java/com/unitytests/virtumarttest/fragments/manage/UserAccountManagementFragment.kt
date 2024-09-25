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
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.data.User
import com.unitytests.virtumarttest.databinding.FragmentUserAccountManagementBinding
import com.unitytests.virtumarttest.dialogBoxes.DateOfBirthBottomSheet
import com.unitytests.virtumarttest.dialogBoxes.GenderBottomSheet
import com.unitytests.virtumarttest.dialogBoxes.NICBottomSheet
import com.unitytests.virtumarttest.dialogBoxes.PhoneNumberBottomSheet
import com.unitytests.virtumarttest.dialogBoxes.setupBottomSheetDialog
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
    private lateinit var dob: String
    private lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent>

    private var dobDialog: String? = null
    private var genderDialog: String? = null
    private var nicDialog: String? = null
    private var phoneNumberDialog: String? = null

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
        setupClickListeners()

        binding.btnBackProfileView.setOnClickListener{
            findNavController().navigateUp()
        }

        binding.accountManagementChangePasswordTxt.setOnClickListener{
            setupBottomSheetDialog { email->
                viewModel.resetPassword(email)
            }
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
                        Toast.makeText(requireContext(), "Successfully Updated!", Toast.LENGTH_LONG).show()
                    }
                    is Resource.Error ->{
                        binding.btnUpdateProfile.revertAnimation()
                        Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.changePassword.collect{
                when(it){
                    is Resource.Loading ->{

                    }
                    is Resource.Success->{
                        Snackbar.make(requireView(), "Check your email for the reset link", Snackbar.LENGTH_LONG).show()
                    }
                    is Resource.Error->{
                        Snackbar.make(requireView(), "Error! ${it.message}", Snackbar.LENGTH_LONG).show()
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

            val user = User(
                id = viewModel.getUserId(),
                firstname = firstName,
                lastname = lastName,
                email = email
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
            dobTxt.text= user.dob
            genderTxt.text= user.gender ?: "Set"
            nicTxt.text= user.nic
            contactTxt.text= user.phoneNumber


            // Set date if not available
            dob = user.dob ?: ""

            // Set the class-level variables
            dobDialog = user.dob ?: ""
            genderDialog = user.gender ?: ""
            nicDialog = user.nic ?: ""
            phoneNumberDialog = user.phoneNumber ?: ""

        }
    }

    private fun setupClickListeners() {
        val layoutDOB = view?.findViewById<LinearLayout>(R.id.layoutDOB)
        layoutDOB?.setOnClickListener {
            val dobBottomSheet = DateOfBirthBottomSheet.newInstance(dobDialog ?: "") { updatedDOB ->
                dobDialog = updatedDOB
                binding.dobTxt.text = updatedDOB
                // Update the date of birth in Firebase
                viewModel.updateDOB(updatedDOB)
            }
            dobBottomSheet.show(parentFragmentManager, "DateOfBirthBottomSheet")
        }

        val layoutGender = view?.findViewById<LinearLayout>(R.id.layoutGender)
        layoutGender?.setOnClickListener {
            val genderBottomSheet = GenderBottomSheet.newInstance(genderDialog ?: "Male") { updatedGender ->
                genderDialog = updatedGender
                binding.genderTxt.text = updatedGender
                // Update the gender in Firebase
                viewModel.updateGender(updatedGender)
            }
            genderBottomSheet.show(parentFragmentManager, "GenderBottomSheet")
        }

        val layoutID = view?.findViewById<LinearLayout>(R.id.layoutID)
        layoutID?.setOnClickListener {
            val layoutID = view?.findViewById<LinearLayout>(R.id.layoutID)
            layoutID?.setOnClickListener {
                val nicBottomSheet = NICBottomSheet.newInstance(nicDialog ?: "") { updatedNIC ->
                    nicDialog = updatedNIC
                    binding.nicTxt.text = updatedNIC
                    // Update the NIC in Firebase
                    viewModel.updateNIC(updatedNIC)
                }
                nicBottomSheet.show(parentFragmentManager, "NICBottomSheet")
            }
        }

        val layoutPhoneNumber = view?.findViewById<LinearLayout>(R.id.layoutTelephoneNumber)
        layoutPhoneNumber?.setOnClickListener {
            val phoneNumberBottomSheet = PhoneNumberBottomSheet.newInstance(phoneNumberDialog ?: "") { updatedPhoneNumber ->
                phoneNumberDialog = updatedPhoneNumber
                binding.contactTxt.text = updatedPhoneNumber
                // Update the phone number in Firebase
                viewModel.updatePhoneNumber(updatedPhoneNumber)
            }
            phoneNumberBottomSheet.show(parentFragmentManager, "ContactBottomSheet")
        }
    }

    private fun showUserLoading() {
        binding.apply{
            prgbrProfileView.visibility = View.VISIBLE
            imgUserProfileView.visibility = View.INVISIBLE
            imgEditImageProfileView.visibility = View.INVISIBLE
            imgEditIconProfileView.visibility = View.INVISIBLE
            txtPersonalInfoTitle.visibility = View.INVISIBLE
            txtEmailProfileView.visibility = View.INVISIBLE
            profileFirstNameEdt.visibility = View.INVISIBLE
            profileLastNameEdt.visibility = View.INVISIBLE
            accountManagementChangePasswordTxt.visibility = View.INVISIBLE
            txtOptionalTitle.visibility = View.INVISIBLE
            layoutDOB.visibility = View.INVISIBLE
            layoutGender.visibility = View.INVISIBLE
            layoutID.visibility = View.INVISIBLE
            layoutTelephoneNumber.visibility = View.INVISIBLE
            btnUpdateProfile.visibility = View.INVISIBLE
        }
    }

    private fun hideUserLoading() {
        binding.apply{
            prgbrProfileView.visibility = View.GONE
            imgUserProfileView.visibility = View.VISIBLE
            imgEditImageProfileView.visibility = View.VISIBLE
            imgEditIconProfileView.visibility = View.VISIBLE
            txtPersonalInfoTitle.visibility = View.VISIBLE
            txtEmailProfileView.visibility = View.VISIBLE
            profileFirstNameEdt.visibility = View.VISIBLE
            profileLastNameEdt.visibility = View.VISIBLE
            accountManagementChangePasswordTxt.visibility = View.VISIBLE
            txtOptionalTitle.visibility = View.VISIBLE
            layoutDOB.visibility = View.VISIBLE
            layoutGender.visibility = View.VISIBLE
            layoutID.visibility = View.VISIBLE
            layoutTelephoneNumber.visibility = View.VISIBLE
            btnUpdateProfile.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        hideNavBarVisibility()
    }
}