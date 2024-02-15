package com.unitytests.virtumarttest.fragments.logregpckg

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.unitytests.virtumarttest.data.User
import com.unitytests.virtumarttest.databinding.FragmentRegisterBinding
import com.unitytests.virtumarttest.util.RegisterValidation
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.viewmodel.registerVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class RegisterFragment: Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel by viewModels<registerVM>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnRegister.setOnClickListener {
                val user = User(
                    regFirstnameEdt.text.toString().trim(),
                    regLastnameEdt.text.toString().trim(),
                    regEmailEdt.text.toString().trim()
                )
                val password =regPasswordEdt.text.toString()
                viewModel.createAccountsWithEmailPass(user, password)
            }
        }
        lifecycleScope.launchWhenStarted{
            viewModel.register.collect{
                when(it){
                    is Resource.Loading -> {
                        binding.btnRegister.startAnimation()
                    }
                    is Resource.Success -> {
                        Log.d("SuccessKey",it.message.toString())
                        binding.btnRegister.revertAnimation()
                    }
                    is Resource.Error -> {
                        Log.e("ErrorKey",it.message.toString())
                        binding.btnRegister.revertAnimation()
                    }
                    else -> Unit
                }
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.validation.collect {validation->
                if(validation.email is RegisterValidation.Failed){
                    withContext(Dispatchers.Main) {
                        binding.regEmailEdt.apply{
                            requestFocus()
                            error=validation.email.message
                        }
                    }
                }
                if(validation.password is RegisterValidation.Failed){
                    withContext(Dispatchers.Main) {
                        binding.regPasswordEdt.apply{
                            requestFocus()
                            error=validation.password.message
                        }
                    }
                }
            }
        }
    }
}