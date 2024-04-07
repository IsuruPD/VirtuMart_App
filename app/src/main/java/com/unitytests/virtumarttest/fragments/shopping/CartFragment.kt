package com.unitytests.virtumarttest.fragments.shopping

import android.app.AlertDialog
import android.app.Dialog
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
import com.unitytests.virtumarttest.adapters.CartProductsAdapter
import com.unitytests.virtumarttest.databinding.FragmentCartBinding
import com.unitytests.virtumarttest.firebase.FirebaseCommonClass
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.util.showNavBarVisibility
import com.unitytests.virtumarttest.viewmodel.CartVM
import kotlinx.coroutines.flow.collectLatest

class CartFragment: Fragment(R.layout.fragment_cart) {

    private lateinit var binding: FragmentCartBinding
    private val cartProductsAdapter by lazy {CartProductsAdapter()}
    private val viewModel by activityViewModels<CartVM>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCartRV()

        // Setup total price
        var totalCost = 0f //To be sent to order confirmation page
        lifecycleScope.launchWhenStarted {
            viewModel.productsCost.collectLatest {cost->
                cost?.let{
                    totalCost = it
                    binding.txtAmountTotalCartView.text= "Rs. ${String.format("%,.2f",cost)}"
                }
            }
        }

        //Go to product view on click
        cartProductsAdapter.onProductClick = {
            val bundle = Bundle().apply { putParcelable("product", it.product) }
            findNavController().navigate(R.id.action_cartFragment_to_productDetailsFragment, bundle)
        }

        // Change Quantity
        cartProductsAdapter.onProductAddClick = {
            viewModel.changingQuantity(it, FirebaseCommonClass.QuantityChanging.INCREASE)
        }
        cartProductsAdapter.onProductRemoveClick = {
            viewModel.changingQuantity(it, FirebaseCommonClass.QuantityChanging.DECREASE)
        }

        // Navigate to order confirmation page
        binding.btnCheckoutCartView.setOnClickListener{
            val action = CartFragmentDirections.actionCartFragmentToOrderConfirmationFragment(totalCost, cartProductsAdapter.differ.currentList.toTypedArray())
            findNavController().navigate(action)
        }

        // Remove product from cart
        lifecycleScope.launchWhenStarted {
            viewModel.deleteCartItem.collectLatest {
                val viewDeleteCartItemDialog = AlertDialog.Builder(requireContext()).apply {
                    setTitle("Remove product")
                    setMessage("Do you want to remove the item from the cart?")
                    setNegativeButton("No") {dialogBox,_ ->
                        dialogBox.dismiss()
                    }
                    setPositiveButton("Yes"){dialogBox,_ ->
                        viewModel.deleteCartItem(it)
                        dialogBox.dismiss()
                    }
                }
                viewDeleteCartItemDialog.create()
                viewDeleteCartItemDialog.show()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.cartProductsSF.collectLatest {
                when(it){
                    is Resource.Loading ->{
                        binding.prgbrCartView.visibility = View.VISIBLE
                    }
                    is Resource.Success ->{
                        binding.prgbrCartView.visibility = View.INVISIBLE
                        if(it.data!!.isEmpty()){
                            displayEmptyCartView()
                        }else{
                            hideEmptyCartView()
                            cartProductsAdapter.differ.submitList(it.data)
                        }
                    }
                    is Resource.Error ->{
                        binding.prgbrCartView.visibility = View.INVISIBLE
                        Toast.makeText(requireContext(), "Error occurred: ${it.message}", Toast.LENGTH_LONG).show()
                    }else -> Unit
                }
            }
        }
    }

    private fun displayEmptyCartView() {
        binding.apply{
            emptyCartDisplayCartView.visibility = View.VISIBLE
            rvProductItemsCartView.visibility= View.INVISIBLE
            CartOverviewDisplayCartView.visibility = View.INVISIBLE
            btnCheckoutCartView.isEnabled=false
        }
    }

    private fun hideEmptyCartView() {
        binding.apply{
            emptyCartDisplayCartView.visibility = View.GONE
            rvProductItemsCartView.visibility= View.VISIBLE
            CartOverviewDisplayCartView.visibility = View.VISIBLE
            btnCheckoutCartView.isEnabled=true
        }
    }

    private fun setupCartRV() {
        binding.rvProductItemsCartView.apply{
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter= cartProductsAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        showNavBarVisibility()
    }
}