package com.example.possiblythelastnewproject.features.shoppingList.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.possiblythelastnewproject.features.pantry.data.PantryRepository
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipePantryItemRepository
import com.example.possiblythelastnewproject.features.shoppingList.data.repositories.ShoppingListRepository
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingList
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val repository: ShoppingListRepository,
    private val recipePantryItemRepository: RecipePantryItemRepository,
    private val pantryRepository: PantryRepository
) : ViewModel() {

    // UI State
    private val _showPantryCreatedDialog = MutableStateFlow(false)
    val showPantryCreatedDialog: StateFlow<Boolean> = _showPantryCreatedDialog.asStateFlow()

    private val _currentListId = MutableStateFlow<Long?>(null)
    private val currentListId: StateFlow<Long?> = _currentListId.asStateFlow()

    // Live Data
    val allPantryItems: StateFlow<List<PantryItem>> = pantryRepository.getAllPantryItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allShoppingLists: StateFlow<List<ShoppingList>> = repository.getAllShoppingLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val shoppingItems: StateFlow<List<ShoppingListItem>> =
        currentListId.filterNotNull().flatMapLatest { repository.getShoppingItemsForList(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categorizedItems: StateFlow<List<CategorizedShoppingItem>> = combine(
        shoppingItems, allPantryItems
    ) { items, pantryItems ->
        val pantryMap = pantryItems.associateBy { it.id }
        items.map { item ->
            val category = pantryMap[item.pantryItemId]?.category.orEmpty()
            CategorizedShoppingItem(item, category.ifBlank { "Uncategorized" })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Internal
    private val recipeAddCounts = mutableMapOf<Long, Int>()

    // List control
    fun setActiveList(listId: Long) {
        _currentListId.value = listId
    }

    fun deleteListWithItems(list: ShoppingList) = viewModelScope.launch {
        repository.deleteShoppingListWithItems(list)
        if (_currentListId.value == list.id) _currentListId.value = null
    }

    fun toggleCheck(item: ShoppingListItem) = viewModelScope.launch {
        repository.updateShoppingItem(item.copy(isChecked = !item.isChecked))
    }

    private fun updateShoppingItem(updatedItem: ShoppingListItem) = viewModelScope.launch {
        repository.updateShoppingItem(updatedItem)
    }

    fun dismissPantryCreatedDialog() {
        _showPantryCreatedDialog.value = false
    }

    // Manual ingredient entry
    fun addItemByName(name: String, quantity: String) = viewModelScope.launch {
        val listId = currentListId.value ?: return@launch
        val cleanedQty = quantity.toDoubleOrNull()?.takeIf { it > 0 } ?: 1.0

        val pantryItem = allPantryItems.value
            .firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?: PantryItem(name = name.trim(), quantity = 0).let {
                val id = pantryRepository.insert(it)
                _showPantryCreatedDialog.value = true
                it.copy(id = id)
            }

        val existingItem = shoppingItems.value
            .firstOrNull { it.pantryItemId == pantryItem.id && it.listId == listId }

        if (existingItem != null) {
            val newQty = (existingItem.quantity.toDoubleOrNull() ?: 0.0) + cleanedQty
            val updatedItem = existingItem.copy(quantity = formatQuantity(newQty))
            updateShoppingItem(updatedItem)
        } else {
            val newItem = ShoppingListItem(
                id = 0L,
                listId = listId,
                pantryItemId = pantryItem.id,
                recipeId = null,
                name = pantryItem.name,
                quantity = formatQuantity(cleanedQty),
                isChecked = false,
                isGenerated = false,
                uuid = UUID.randomUUID().toString()
            )
            repository.insertShoppingItems(listOf(newItem))
        }
    }

    // Recipe merge logic
    fun mergeSelectedRecipesIntoActiveList(newSelections: Map<Long, Int>) = viewModelScope.launch {
        val listId = currentListId.value ?: return@launch

        newSelections.forEach { (recipeId, count) ->
            recipeAddCounts[recipeId] = (recipeAddCounts[recipeId] ?: 0) + count
        }

        val fullNeeds = recipeAddCounts.flatMap { (recipeId, count) ->
            recipePantryItemRepository.getCrossRefsForRecipeOnce(recipeId)
                .filter { it.required }
                .map { it.pantryItemId to parseAmount(it.amountNeeded) * count }
        }

        val aggregatedNeeds = fullNeeds.groupBy { it.first }
            .mapValues { (_, entries) -> entries.sumOf { it.second } }

        val pantryMap = allPantryItems.value.associateBy { it.id }
        val existingMap = shoppingItems.value.associateBy { it.pantryItemId }

        val (itemsToUpdate, itemsToInsert) = aggregatedNeeds.mapNotNull { (pantryId, totalNeeded) ->
            val owned = pantryMap[pantryId]?.quantity?.toDouble() ?: 0.0
            val current = existingMap[pantryId]?.quantity?.toDoubleOrNull() ?: 0.0
            val toBuy = (totalNeeded - owned - current).coerceAtLeast(0.0)
            if (toBuy <= 0.0) return@mapNotNull null

            val itemName = pantryMap[pantryId]?.name ?: "Unknown"
            val newQty = formatQuantity(current + toBuy)

            if (existingMap.containsKey(pantryId)) {
                existingMap[pantryId]!!.copy(quantity = newQty, isGenerated = true) to null
            } else {
                null to ShoppingListItem(
                    listId = listId,
                    pantryItemId = pantryId,
                    name = itemName,
                    quantity = formatQuantity(toBuy),
                    isChecked = false,
                    isGenerated = true
                )
            }
        }.unzip()

        itemsToUpdate.filterNotNull().forEach { repository.updateShoppingItem(it) }
        repository.insertShoppingItems(itemsToInsert.filterNotNull())
    }

    // Utility functions
    private fun formatQuantity(amount: Double): String =
        if (amount % 1.0 == 0.0) amount.toInt().toString() else "%.2f".format(amount)

    private fun parseAmount(input: String): Double =
        input.trim().takeWhile { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0

    fun createListWithRecipesAndIngredients(
        name: String,
        recipeIds: List<Long>,
        ingredientQuantities: Map<Long, String>,
        onComplete: (Long) -> Unit
    ) = viewModelScope.launch {
        val newListId = repository.insertShoppingList(ShoppingList(name = name))
        setActiveList(newListId)

        if (recipeIds.isNotEmpty()) generateFromSelectedRecipes(recipeIds)

        if (ingredientQuantities.isNotEmpty()) {
            val pantryMap = allPantryItems.value.associateBy { it.id }
            val manualItems = ingredientQuantities.mapNotNull { (id, qtyStr) ->
                val qty =
                    qtyStr.trim().toDoubleOrNull()?.takeIf { it > 0 } ?: return@mapNotNull null
                pantryMap[id]?.let { item ->
                    ShoppingListItem(
                        listId = newListId,
                        pantryItemId = item.id,
                        name = item.name,
                        quantity = formatQuantity(qty),
                        isChecked = false,
                        isGenerated = false
                    )
                }
            }
            repository.insertShoppingItems(manualItems)
        }

        onComplete(newListId)
    }

    private fun generateFromSelectedRecipes(selectedRecipeIds: List<Long>) = viewModelScope.launch {
        val listId = currentListId.value ?: return@launch
        repository.deleteGeneratedItemsInList(listId)

        val crossRefs = selectedRecipeIds.flatMap {
            recipePantryItemRepository.getCrossRefsForRecipeOnce(it)
        }

        val pantryMap = allPantryItems.value.associateBy { it.id }
        val aggregatedNeeds = crossRefs.filter { it.required }
            .groupBy { it.pantryItemId }
            .mapValues { (_, refs) -> refs.sumOf { parseAmount(it.amountNeeded) } }

        val itemsToInsert = aggregatedNeeds.mapNotNull { (pantryId, neededQty) ->
            val ownedQty = pantryMap[pantryId]?.quantity?.toDouble() ?: 0.0
            val toBuy = (neededQty - ownedQty).coerceAtLeast(0.0)
            if (toBuy <= 0.0) return@mapNotNull null

            ShoppingListItem(
                listId = listId,
                pantryItemId = pantryId,
                name = pantryMap[pantryId]?.name ?: "Unknown",
                quantity = formatQuantity(toBuy),
                isChecked = false,
                isGenerated = true
            )
        }

        repository.insertShoppingItems(itemsToInsert)


    }
}
