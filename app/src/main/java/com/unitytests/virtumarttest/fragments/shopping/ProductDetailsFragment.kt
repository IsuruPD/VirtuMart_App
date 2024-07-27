package com.unitytests.virtumarttest.fragments.shopping

import android.app.ProgressDialog.show
import android.content.Intent
import android.graphics.Color
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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.unity3d.player.UnityPlayerActivity
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.adapters.ColorsAdapter
import com.unitytests.virtumarttest.adapters.SizesAdapter
import com.unitytests.virtumarttest.adapters.ViewPagerforImagesAdapter
import com.unitytests.virtumarttest.data.CartProducts
import com.unitytests.virtumarttest.data.WishListProducts
import com.unitytests.virtumarttest.databinding.FragmentProductDetailsBinding
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.util.hideNavBarVisibility
import com.unitytests.virtumarttest.viewmodel.DetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductDetailsFragment: Fragment() {
    private val args by navArgs<ProductDetailsFragmentArgs>()
    private lateinit var binding: FragmentProductDetailsBinding
    private val viewPagerImagesAdapter by lazy { ViewPagerforImagesAdapter() }
    private val sizesAdapter by lazy { SizesAdapter() }
    private val colorsAdapter by lazy { ColorsAdapter() }
    private var selectedColor: String? = null
    private var selectedSize: String? = null
    private val viewModel by viewModels<DetailsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        hideNavBarVisibility()
        binding = FragmentProductDetailsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = args.product

        setupRVProductImages()
        setupRVProductColors()
        setupRVProductsSizes()

        binding.btnBackProductDisplay.setOnClickListener {
            findNavController().navigateUp()
        }

        sizesAdapter.onItemClick = {
            selectedSize = it
        }
        colorsAdapter.onItemClick = {
            selectedColor = it
        }

        // Check Product Availability in Wish List
        viewModel.checkIfProductInWishList(product.productId)
        lifecycleScope.launchWhenStarted {
            viewModel.isProductInWishList.collectLatest { isInWishList ->
                if (isInWishList) {
                    binding.btnAddToWishlist.setImageResource(R.drawable.ic_heart_filled)
                } else {
                    binding.btnAddToWishlist.setImageResource(R.drawable.ic_heart)
                }
            }
        }

        // Add to Cart
        binding.btnAddtoCartProductDisplayView.setOnClickListener {
            selectedColor?.let { color ->
                val selectedColorInt = Color.parseColor(color)
                viewModel.addUpdateProductInCart(
                    CartProducts(
                        product,
                        1,
                        selectedColorInt,
                        selectedSize
                    )
                )
            } ?: run {
                Toast.makeText(requireContext(), "Please select a color", Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.addToCart.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.btnAddtoCartProductDisplayView.startAnimation()
                    }

                    is Resource.Success -> {
                        binding.btnAddtoCartProductDisplayView.revertAnimation()
                        Toast.makeText(requireContext(), "Added to cart", Toast.LENGTH_SHORT).show()
                    }

                    is Resource.Error -> {
                        binding.btnAddtoCartProductDisplayView.revertAnimation()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }

        // Add to Wish List
        binding.btnAddToWishlist.setOnClickListener {
            selectedColor?.let { color ->
                val selectedColorInt = Color.parseColor(color)
                viewModel.toggleWishList(
                    WishListProducts(
                        product,
                        1,
                        selectedColorInt,
                        selectedSize
                    )
                )
            } ?: run {
                Toast.makeText(requireContext(), "Please select a specification", Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launchWhenStarted {
            launch {
                viewModel.addToWishList.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.prgbrAddtoWishListDetailsView.visibility = View.VISIBLE
                            binding.btnAddToWishlist.visibility = View.INVISIBLE
                        }

                        is Resource.Success -> {
                            binding.prgbrAddtoWishListDetailsView.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Added to Wishlist",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.btnAddToWishlist.visibility = View.VISIBLE
                        }

                        is Resource.Error -> {
                            binding.prgbrAddtoWishListDetailsView.visibility = View.GONE
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                            binding.btnAddToWishlist.visibility = View.VISIBLE
                        }

                        else -> Unit
                    }
                }
            }

            launch{
                viewModel.removeFromWishList.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.prgbrAddtoWishListDetailsView.visibility = View.VISIBLE
                            binding.btnAddToWishlist.visibility = View.INVISIBLE
                        }

                        is Resource.Success -> {
                            binding.prgbrAddtoWishListDetailsView.visibility = View.GONE
                            Toast.makeText(requireContext(), "Removed from Wishlist", Toast.LENGTH_SHORT).show()
                            binding.btnAddToWishlist.visibility = View.VISIBLE
                        }

                        is Resource.Error -> {
                            binding.prgbrAddtoWishListDetailsView.visibility = View.GONE
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                            binding.btnAddToWishlist.visibility = View.VISIBLE
                        }

                        else -> Unit
                    }
                }
            }
        }


        binding.apply {
            txtTitleProductView.text = product.productName
            txtDescriptionProductView.text = product.description

            if (product.offerPercentage == null || product.offerPercentage.equals(0f)) {
                layoutHOldPriceDisplayPV.visibility = View.INVISIBLE
                txtPriceProductView.text = "Rs.${String.format("%,.2f", product.price)}"
            } else {
                layoutHOldPriceDisplayPV.visibility = View.VISIBLE
                txtPriceProductView.text =
                    "Rs. ${String.format("%,.2f", (1 - product.offerPercentage) * product.price)}"
                txtOldPriceProductView.text = "was Rs. ${String.format("%,.2f", product.price)}"
            }

            viewPagerImagesAdapter.differ.submitList(product.imageURLs)
            product.colors?.let { colorsAdapter.differ.submitList(it) }
            product.size?.let { sizesAdapter.differ.submitList(it) }
        }

        binding.btnViewARProductDisplayView.setOnClickListener {
            val modelUrl: String? = product.modelUrl
            Log.d("TestingModelFetch Model URL", product.modelUrl.toString())
            openUnityActivity(modelUrl)
        }
    }

    private fun setupRVProductImages() {
        binding.apply {
            viewPagerProductDisplay.adapter = viewPagerImagesAdapter
        }
    }

    private fun setupRVProductColors() {
        binding.rvColorProductView.apply {
            adapter = colorsAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupRVProductsSizes() {
        binding.rvSizeProductView.apply {
            adapter = sizesAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun openUnityActivity(modelUrl: String?) {
        try {
            Toast.makeText(requireContext(), "Opening AR View", Toast.LENGTH_LONG).show()
            Log.i("UnityActivityInfo", "Opening AR View with URL: $modelUrl")

            val intent = Intent(requireContext(), UnityPlayerActivity::class.java)
            intent.putExtra("MODEL_URL", modelUrl) // Pass the URL as an extra
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("UnityActivityError", "Error starting UnityPlayerActivity: ${e.message}", e)
        }
    }
}