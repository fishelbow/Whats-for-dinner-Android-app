package com.example.possiblythelastnewproject.features.shoppingList.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.possiblythelastnewproject.features.pantry.data.PantryRepository
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipePantryItemRepository
import com.example.possiblythelastnewproject.features.shoppingList.data.ShoppingListRepository
import com.example.possiblythelastnewproject.features.shoppingList.data.entity.ShoppingList
import com.example.possiblythelastnewproject.features.shoppingList.data.entity.ShoppingListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val repository: ShoppingListRepository,
    private val recipePantryItemRepository: RecipePantryItemRepository,
    private val pantryRepository: PantryRepository
) : ViewModel() {

    val allShoppingLists: StateFlow<List<ShoppingList>> = repository
        .getAllShoppingLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentListId = MutableStateFlow<Long?>(null)
    val currentListId: StateFlow<Long?> = _currentListId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val shoppingItems: StateFlow<List<ShoppingListItem>> = currentListId
        .filterNotNull()
        .flatMapLatest { repository.getShoppingItemsForList(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setActiveList(listId: Long) {
        _currentListId.value = listId
    }

    fun toggleCheck(item: ShoppingListItem) = viewModelScope.launch {
        repository.updateShoppingItem(item.copy(isChecked = !item.isChecked))
    }

    fun addItem(item: ShoppingListItem) = viewModelScope.launch {
        repository.insertShoppingItem(item)
    }

    fun removeItem(item: ShoppingListItem) = viewModelScope.launch {
        repository.deleteShoppingItem(item)
    }

    fun clearCheckedItems() = viewModelScope.launch {
        currentListId.value?.let { repository.clearCheckedItemsInList(it) }
    }

    private fun generateFromSelectedRecipes(selectedRecipeIds: List<Long>) = viewModelScope.launch {
        val listId = currentListId.value ?: return@launch

        repository.deleteGeneratedItemsInList(listId)

        val crossRefs = selectedRecipeIds.flatMap {
            recipePantryItemRepository.getCrossRefsForRecipeOnce(it)
        }

        val pantryItems = pantryRepository.getAllPantryItems().firstOrNull().orEmpty()
        val pantryMap = pantryItems.associateBy { it.id }

        val aggregatedNeeds = crossRefs
            .filter { it.required }
            .groupBy { it.pantryItemId }
            .mapValues { (_, refs) -> refs.sumOf { parseAmount(it.amountNeeded) } }

        val itemsToInsert = aggregatedNeeds.mapNotNull { (pantryId, neededQty) ->
            val ownedQty = pantryMap[pantryId]?.quantity?.toDouble() ?: 0.0
            val toBuy = neededQty - ownedQty

            if (toBuy > 0.0) {
                ShoppingListItem(
                    listId = listId,
                    pantryItemId = pantryId,
                    name = pantryMap[pantryId]?.name ?: "Unknown",
                    quantity = "%.2f".format(toBuy),
                    isChecked = false,
                    isGenerated = true
                )
            } else null
        }

        repository.insertShoppingItems(itemsToInsert)
    }

    fun createListWithRecipes(
        name: String,
        selectedRecipeIds: List<Long>,
        onComplete: (Long) -> Unit
    ) = viewModelScope.launch {
        val newList = ShoppingList(name = name)
        val newListId = repository.insertShoppingList(newList)

        setActiveList(newListId)
        generateFromSelectedRecipes(selectedRecipeIds)

        onComplete(newListId)
    }

    fun deleteListWithItems(list: ShoppingList) = viewModelScope.launch {
        repository.deleteShoppingListWithItems(list)
        if (_currentListId.value == list.id) {
            _currentListId.value = null
        }
    }

    private fun parseAmount(input: String): Double {
        return input.trim()
            .takeWhile { it.isDigit() || it == '.' }
            .toDoubleOrNull() ?: 0.0
    }

    val categorizedItems: StateFlow<List<CategorizedShoppingItem>> =
        combine(
            shoppingItems,
            pantryRepository.getAllPantryItems()
        ) { items, pantryItems ->
            val pantryMap = pantryItems.associateBy { it.id }
            items.map { item ->
                val category = pantryMap[item.pantryItemId]?.category.orEmpty()
                CategorizedShoppingItem(item, category.ifBlank { "Uncategorized" })
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}