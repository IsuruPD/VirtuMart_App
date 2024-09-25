package com.unitytests.virtumarttest.fragments.shopping

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.unitytests.virtumarttest.BuildConfig
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.activities.LoginRegisterActivity
import com.unitytests.virtumarttest.databinding.FragmentUserProfileOptionsBinding
import com.unitytests.virtumarttest.notifications.AppNotificationManager
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.util.showNavBarVisibility
import com.unitytests.virtumarttest.viewmodel.UserOptionsVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ProfileFragment: Fragment() {

    private lateinit var binding: FragmentUserProfileOptionsBinding
    val viewModel by viewModels<UserOptionsVM>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserProfileOptionsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Load the saved notification preference
        val isNotificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true)
        binding.switchNotification.isChecked = isNotificationsEnabled

        // Set up the notification switch toggle
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("notifications_enabled", isChecked)
            editor.apply()
            Toast.makeText(requireContext(), if (isChecked) "Notifications Enabled" else "Notifications Disabled", Toast.LENGTH_SHORT).show()
        }


        binding.txtEditProfileUserOptions.setOnClickListener{
            findNavController().navigate(R.id.action_profileFragment_to_userAccountFragment)
        }
        binding.imgProfileUserOptions.setOnClickListener{
            findNavController().navigate(R.id.action_profileFragment_to_userAccountFragment)
        }
        binding.layoutWishListUserOptions.setOnClickListener{
            findNavController().navigate(R.id.action_profileFragment_to_wishListFragment)
        }
        binding.layoutMyOrdersUserOptions.setOnClickListener{
            findNavController().navigate(R.id.action_profileFragment_to_userOrderManagementFragment)
        }
        binding.layoutToBePaidUserOptions.setOnClickListener{
//            val action= ProfileFragmentDirections.actionProfileFragmentToOrderConfirmationFragment(0f, emptyArray())
//            findNavController().navigate(action)
            findNavController().navigate(R.id.action_profileFragment_to_userDisputeManagementFragment)
        }
        binding.layoutShippingUserOptions.setOnClickListener{
            findNavController().navigate(R.id.action_profileFragment_to_shippingAddressManagementFragment)
        }


        binding.Logout.setOnClickListener {
            // Inflate the custom layout for the dialog
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_logout, null)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.logout()
                    val intent = Intent(requireActivity(), LoginRegisterActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

                    // Notify Logout
                    val notificationManager = AppNotificationManager(requireContext())
                    notificationManager.showLogoutNotification()

                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("No") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .create()
            dialog.show()
        }



        binding.txtAppVersion.text= "Version ${BuildConfig.VERSION_CODE}"



        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                when(it) {
                    is Resource.Loading ->{
                        binding.prgbrUserOptions.visibility = View.VISIBLE
                    }
                    is Resource.Success ->{
                        binding.prgbrUserOptions.visibility = View.GONE

                        Glide.with(requireView()).load(it.data!!.imagePath).error(R.drawable.noprofileicon).into(binding.imgProfileUserOptions)
                        binding.txtNameUserOptions.text = "${it.data.firstname} ${it.data.lastname}"
                    }
                    is Resource.Error ->{
                        binding.prgbrUserOptions.visibility = View.GONE
                        Toast.makeText(requireContext(),it.message.toString(), Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        showNavBarVisibility()
    }
}