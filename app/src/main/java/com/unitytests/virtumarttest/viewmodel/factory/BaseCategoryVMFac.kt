package com.unitytests.virtumarttest.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.unitytests.virtumarttest.data.Category
import com.unitytests.virtumarttest.viewmodel.CategoryVM

class BaseCategoryVMFac(
    private val firestore: FirebaseFirestore,
    private val category: Category

):ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CategoryVM(firestore,category) as T
    }
}