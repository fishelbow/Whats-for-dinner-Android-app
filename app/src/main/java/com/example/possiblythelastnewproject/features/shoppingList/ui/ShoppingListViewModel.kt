package com.example.possiblythelastnewproject.features.shoppingList.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.possiblythelastnewproject.features.shoppingList.data.ShoppingListItem
import com.example.possiblythelastnewproject.features.shoppingList.data.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val repository: ShoppingListRepository
) : ViewModel() {

    val shoppingItems: StateFlow<List<ShoppingListItem>> = repository
        .getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleCheck(item: ShoppingListItem) = viewModelScope.launch {
        repository.update(item.copy(isChecked = !item.isChecked))
    }

    fun addItem(item: ShoppingListItem) = viewModelScope.launch {
        repository.insert(item)
    }

    fun remove(item: ShoppingListItem) = viewModelScope.launch {
        repository.delete(item)
    }

    fun clearChecked() = viewModelScope.launch {
        repository.clearChecked()
    }
}