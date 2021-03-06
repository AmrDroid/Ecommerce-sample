package com.amrmustafa.ecommerce.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amrmustafa.ecommerce.data.ProductRepository

/**
 * Factory for [ProductViewModel] class
 */
class ViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
