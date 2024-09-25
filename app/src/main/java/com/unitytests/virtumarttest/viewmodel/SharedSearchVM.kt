package com.unitytests.virtumarttest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.unitytests.virtumarttest.data.Product
import com.unitytests.virtumarttest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SharedSearchVM @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _searchResults = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val searchResults: StateFlow<Resource<List<Product>>> = _searchResults
    private val searchPagingInfoSearch = PagingInfoSearch()

    private val _categories = MutableStateFlow<Resource<List<String>>>(Resource.Unspecified())
    val categories: StateFlow<Resource<List<String>>> = _categories

    // Category and Filter selections
    var selectedCategory: String? = null
    var selectedFilter: String? = null
    var selectedSortOption: String? = null

    init {
        // Fetch categories initially
        fetchCategories()
    }

    fun searchProducts(query: String) {
        // Reset pagination if the query has changed
        if (query != searchPagingInfoSearch.currentQuery) {
            searchPagingInfoSearch.page = 1
            searchPagingInfoSearch.prevSearchProducts = emptyList()
            searchPagingInfoSearch.isPagingEnd = false
            searchPagingInfoSearch.currentQuery = query
        }

        if (!searchPagingInfoSearch.isPagingEnd) {
            viewModelScope.launch {
                _searchResults.emit(Resource.Loading())

                try {
                    var queryRef = firestore.collection("products")
                        .limit(searchPagingInfoSearch.page * 10)

                    // Apply category filter
                    if (!selectedCategory.isNullOrEmpty()) {
                        queryRef = queryRef.whereEqualTo("category", selectedCategory)
                    }

                    // Apply additional filters
                    if (!selectedFilter.isNullOrEmpty()) {
                        when (selectedFilter) {
                            "Free Shipping" -> queryRef = queryRef.whereEqualTo("freeShipping", true)
                            "Deals" -> queryRef = queryRef.whereEqualTo("deals", true)
                            "Featured" -> queryRef = queryRef.whereEqualTo("featured", true)
                        }
                    }

                    // Apply sorting
                    if (!selectedSortOption.isNullOrEmpty()) {
                        when (selectedSortOption) {
                            "Low to High" -> queryRef = queryRef.orderBy("price", Query.Direction.ASCENDING)
                            "High to Low" -> queryRef = queryRef.orderBy("price", Query.Direction.DESCENDING)
                        }
                    }

                    val products = queryRef.get().await().toObjects(Product::class.java)

                    // Filter the results based on the query
                    val results = products.filter {
                        it.productName.contains(query, ignoreCase = true)
                    }

                    // Update paging info
                    searchPagingInfoSearch.isPagingEnd = results == searchPagingInfoSearch.prevSearchProducts
                    searchPagingInfoSearch.prevSearchProducts = results
                    searchPagingInfoSearch.page++

                    // Emit the results
                    _searchResults.emit(Resource.Success(results))
                } catch (e: Exception) {
                    _searchResults.emit(Resource.Error(e.message ?: "Unknown error"))
                }
            }
        }
    }


    fun fetchCategories() {
        viewModelScope.launch {
            _categories.emit(Resource.Loading())

            try {
                val categories = firestore.collection("Categories")
                    .orderBy("rank")
                    .get()
                    .await()
                    .documents
                    .map { it.getString("name") ?: "" }

                _categories.emit(Resource.Success(categories))
            } catch (e: Exception) {
                _categories.emit(Resource.Error(e.message ?: "Unknown error"))
            }
        }
    }

    // Applying filters
    fun applyFilters() {
        searchPagingInfoSearch.page = 1
        searchPagingInfoSearch.prevSearchProducts = emptyList()
        searchPagingInfoSearch.isPagingEnd = false
        searchProducts(searchPagingInfoSearch.currentQuery)
    }

    fun applyClearFilters() {
        selectedCategory = null
        selectedFilter = null
        selectedSortOption = null
        searchPagingInfoSearch.page = 1
        searchPagingInfoSearch.prevSearchProducts = emptyList()
        searchPagingInfoSearch.isPagingEnd = false
        searchProducts(searchPagingInfoSearch.currentQuery)
    }

    fun clearFilters() {
        selectedCategory = null
        selectedFilter = null
        selectedSortOption = null
    }
}

internal data class PagingInfoSearch(
    var page: Long = 1,
    var prevSearchProducts: List<Product> = emptyList(),
    var isPagingEnd: Boolean = false,
    var currentQuery: String = ""
)
