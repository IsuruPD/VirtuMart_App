package com.unitytests.virtumarttest.fragments.manage

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.unitytests.virtumarttest.data.User
import com.unitytests.virtumarttest.databinding.FragmentUserAccountManagementBinding
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.viewmodel.ProfileVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class UserAccountManagementFragment: Fragment() {

    private lateinit var binding: FragmentUserAccountManagementBinding
    private val viewModel by viewModels<ProfileVM>()
    private var imageUri: Uri?= null
    private lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                imageUri = it.data?.data
                Glide.with(this).load(imageUri).into(binding.imgUserProfileView)
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
            binding.apply{
                val email = txtEmailProfileView.text.toString()
                val firstName = profileFirstNameEdt.text.toString().trim()
                val lastName = profileLastNameEdt.text.toString().trim()
                val user = User(firstName,lastName,email)
                viewModel.updateUserDetails(user, imageUri)
            }
        }
    }

    private fun showUserInformation(data: User) {
        binding.apply {
            Glide.with(this@UserAccountManagementFragment).load(data.imagePath).error(ColorDrawable(Color.DKGRAY)).into(imgUserProfileView)
            txtEmailProfileView.text = data.email
            profileFirstNameEdt.setText(data.firstname)
            profileLastNameEdt.setText(data.lastname)
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

}