package com.unitytests.virtumarttest.fragments.logregpckg

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.data.User
import com.unitytests.virtumarttest.databinding.FragmentRegisterBinding
import com.unitytests.virtumarttest.util.RegisterValidation
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.viewmodel.RegisterVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: RegisterVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerSubTitle.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.btnRegister.setOnClickListener {
            val firstname = binding.regFirstnameEdt.text.toString().trim()
            val lastname = binding.regLastnameEdt.text.toString().trim()
            val email = binding.regEmailEdt.text.toString().trim()
            val password = binding.regPasswordEdt.text.toString().trim()

            if (firstname.isEmpty() && lastname.isEmpty()){
                Toast.makeText(requireContext(), "What do we call you?", Toast.LENGTH_LONG).show()
            } else if (firstname.isEmpty() || lastname.isEmpty()){
                Toast.makeText(requireContext(), "Full name is required!", Toast.LENGTH_LONG).show()
            } else {
                val user = User("", firstname, lastname, email)

                viewModel.createAccountsWithEmailPass(user, password)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.register.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.btnRegister.startAnimation()
                    }
                    is Resource.Success -> {
                        Log.d("RegisterFragment", "Registration successful")
                        binding.btnRegister.revertAnimation()

                        Toast.makeText(requireContext(),"Registration Successful!", Toast.LENGTH_LONG).show()
                        clearEditTextFields()
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    }
                    is Resource.Error -> {
                        Log.e("RegisterFragment", "Registration error: ${resource.message}")
                        binding.btnRegister.revertAnimation()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.validation.collect { validation ->
                if (validation.email is RegisterValidation.Failed) {
                    withContext(Dispatchers.Main) {
                        binding.regEmailEdt.apply {
                            requestFocus()
                            error = validation.email.message
                        }
                    }
                }
                if (validation.password is RegisterValidation.Failed) {
                    withContext(Dispatchers.Main) {
                        binding.regPasswordEdt.apply {
                            requestFocus()
                            error = validation.password.message
                        }
                    }
                }
            }
        }
    }

    private fun clearEditTextFields() {
        binding.regFirstnameEdt.text.clear()
        binding.regLastnameEdt.text.clear()
        binding.regEmailEdt.text.clear()
        binding.regPasswordEdt.text.clear()
    }
}
