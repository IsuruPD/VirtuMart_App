package com.unitytests.virtumarttest.fragments.logregpckg

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.activities.ShoppingActivity
import com.unitytests.virtumarttest.databinding.FragmentLoginBinding
import com.unitytests.virtumarttest.databinding.FragmentRegisterBinding
import com.unitytests.virtumarttest.dialogBoxes.setupBottomSheetDialog
import com.unitytests.virtumarttest.notifications.AppNotificationManager
import com.unitytests.virtumarttest.util.RegisterValidation
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.viewmodel.LoginVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class loginFragment: Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel by viewModels<LoginVM>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if a user is already logged in
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User is already logged in, navigate to the ShoppingActivity
            navigateToMainActivity()
        } else {
            // Proceed with the login flow if not logged in
            setupLoginFlow()
        }

//        binding.loginSubTitle.setOnClickListener{
//            findNavController().navigate(R.id.action_loginFragment_to_registerFragment2)
//        }
//
//        binding.apply{
//            btnLogin.setOnClickListener{
//                val email=loginEmailEdt.text.toString()
//                val password=loginPasswordEdt.text.toString()
//
//                viewModel.login(email, password)
//            }
//        }

        lifecycleScope.launchWhenStarted {
            viewModel.resetPassword.collect{
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

        lifecycleScope.launchWhenStarted {
            viewModel.login.collect{
                when(it){
                    is Resource.Loading ->{
                        binding.btnLogin.startAnimation()
                    }
                    is Resource.Success->{
                        binding.btnLogin.revertAnimation()
                        Toast.makeText(requireContext(), "Logged In!",Toast.LENGTH_LONG).show()

                        val notificationManager = AppNotificationManager(requireContext())
                        notificationManager.showLoginNotification()

                        Intent(requireActivity(), ShoppingActivity:: class.java).also { intent ->
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK) // Clears the backstack for LoginRegisterActivity and thus,
                                                                                                                   // trying to come back here(Login) from the ShoppingActivity by
                                                                                                                   // clicking back will not be possible.
                                                                                                                   // Yet, if the user closes the app by going back, they have to come back through login.
                                                                                                                   // Minimizing the app will not require this however.
                            startActivity(intent)
                        }
                    }
                    is Resource.Error->{
                        Toast.makeText(requireContext(),"Invalid Credentials!", Toast.LENGTH_LONG).show()
                        binding.btnLogin.revertAnimation()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.validateLogin.collect { validation ->
                if (validation.email is RegisterValidation.Failed) {
                    binding.loginEmailEdt.apply {
                        requestFocus()
                        error = validation.email.message
                    }
                }
                if (validation.password is RegisterValidation.Failed) {
                    binding.loginPasswordEdt.apply {
                        requestFocus()
                        error = validation.password.message
                    }
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        Intent(requireActivity(), ShoppingActivity::class.java).also { intent ->
            startActivity(intent)
            requireActivity().finish()  // To prevent going back to login screen (Already logged in)
        }
    }

    private fun setupLoginFlow() {

        // Take the user to login if not logged in already
        binding.loginSubTitle.setOnClickListener{
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment2)
        }

        binding.apply {
            btnLogin.setOnClickListener {
                val email = loginEmailEdt.text.toString()
                val password = loginPasswordEdt.text.toString()
                viewModel.login(email, password)
            }
        }

        binding.loginForgotPaswordTxt.setOnClickListener{
            setupBottomSheetDialog { email->
                viewModel.resetPassword(email)
            }
        }
    }
}