package com.unitytests.virtumarttest.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.databinding.ActivityShoppingBinding
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.viewmodel.CartVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import com.unity3d.player.UnityPlayerActivity

@AndroidEntryPoint
class ShoppingActivity : AppCompatActivity() {

    val binding by lazy{
        ActivityShoppingBinding.inflate(layoutInflater)
    }

    val viewModel by viewModels<CartVM>(

    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navController = findNavController(R.id.shoppingHostFragment)
        binding.bottonNavBar.setupWithNavController(navController)

        lifecycleScope.launchWhenStarted {
            viewModel.cartProductsSF.collectLatest {
                when(it){
                    is Resource.Success ->{
                        val count = it.data?.size?: 0
                        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottonNavBar)
                        bottomNavigation.getOrCreateBadge(R.id.cartFragment).apply{
                            number = count
                            backgroundColor = resources.getColor(R.color.g_blue)
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

//    override fun onBackPressed() {
//        // Move the task to the background instead of finishing it (Yet back button from later fragments also takes the user to the background)
//        moveTaskToBack(true)
//    }
}