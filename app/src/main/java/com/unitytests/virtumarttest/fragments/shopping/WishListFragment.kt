package com.unitytests.virtumarttest.fragments.shopping

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.adapters.WishListAdapter
import com.unitytests.virtumarttest.databinding.FragmentWishlistBinding
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.util.showNavBarVisibility
import com.unitytests.virtumarttest.viewmodel.WishListVM
import kotlinx.coroutines.flow.collectLatest

class WishListFragment : Fragment() {

    private lateinit var binding: FragmentWishlistBinding
    private val wishListAdapter by lazy { WishListAdapter() }
    private val viewModel by activityViewModels<WishListVM>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWishlistBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWishListRV()

        // Setup total price
        var totalCost = 0f //To be sent to order confirmation page
//        lifecycleScope.launchWhenStarted {
//            viewModel.productsCost.collectLatest {cost->
//                cost?.let{
//                    totalCost = it
//                    //binding.txtAmountTotalCartView.text= "Rs. ${String.format("%,.2f",cost)}"
//                }
//            }
//        }

        //Go to product view on click
        wishListAdapter.onProductClick = {
            val bundle = Bundle().apply { putParcelable("product", it.product) }
            findNavController().navigate(
                R.id.action_wishListFragment_to_productDetailsFragment,
                bundle
            )
        }

        // Navigate to back
        binding.btnBackWishListView.setOnClickListener {
            findNavController().navigateUp()
        }

        // Remove product from cart
        lifecycleScope.launchWhenStarted {
            viewModel.deleteCartItem.collectLatest {
                val viewDeleteCartItemDialog = AlertDialog.Builder(requireContext()).apply {
                    setTitle("Remove product")
                    setMessage("Do you want to remove the item from the cart?")
                    setNegativeButton("No") { dialogBox, _ ->
                        dialogBox.dismiss()
                    }
                    setPositiveButton("Yes") { dialogBox, _ ->
                        //viewModel.deleteCartItem(it)
                        dialogBox.dismiss()
                    }
                }
                viewDeleteCartItemDialog.create()
                viewDeleteCartItemDialog.show()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.wishList.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.prgbrWishListView.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.prgbrWishListView.visibility = View.INVISIBLE
                        if (it.data!!.isEmpty()) {
                            displayWishListView()
                        } else {
                            hideEmptyWishListView()
                            wishListAdapter.differ.submitList(it.data)
                        }
                    }

                    is Resource.Error -> {
                        binding.prgbrWishListView.visibility = View.INVISIBLE
                        Toast.makeText(
                            requireContext(),
                            "Error occurred: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun displayWishListView() {
        binding.apply {
            emptyWishListDisplayView.visibility = View.VISIBLE
            rvProductItemsWishListView.visibility = View.INVISIBLE
        }
    }

    private fun hideEmptyWishListView() {
        binding.apply {
            emptyWishListDisplayView.visibility = View.GONE
            rvProductItemsWishListView.visibility = View.VISIBLE
        }
    }

    private fun setupWishListRV() {
        binding.rvProductItemsWishListView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = wishListAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        showNavBarVisibility()
    }
}