package com.unitytests.virtumarttest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
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
                    // Perform the paginated query
                    val products = firestore.collection("products")
                        .limit(searchPagingInfoSearch.page * 10)
                        .get()
                        .await()
                        .toObjects(Product::class.java)

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
}

internal data class PagingInfoSearch(
    var page: Long = 1,
    var prevSearchProducts: List<Product> = emptyList(),
    var isPagingEnd: Boolean = false,
    var currentQuery: String = ""
)
