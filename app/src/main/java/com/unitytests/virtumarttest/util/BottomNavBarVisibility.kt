package com.unitytests.virtumarttest.util

import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.activities.ShoppingActivity

fun Fragment.hideNavBarVisibility(){
    // Hide bottom navigation
    val bottomNavBarView = (activity as? ShoppingActivity)?.findViewById<BottomNavigationView>(R.id.bottonNavBar)
    bottomNavBarView?.visibility = View.GONE
}
fun Fragment.showNavBarVisibility(){
    // Hide bottom navigation
    val bottomNavBarView = (activity as? ShoppingActivity)?.findViewById<BottomNavigationView>(R.id.bottonNavBar)
    bottomNavBarView?.visibility = View.VISIBLE
}