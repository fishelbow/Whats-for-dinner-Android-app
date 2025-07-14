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

    private fun formatQuantity(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            amount.toInt().toString()
        } else {
            "%.2f".format(amount)
        }
    }

    private val recipeAddCounts = mutableMapOf<Long, Int>()

    val allPantryItems: StateFlow<List<PantryItem>> = pantryRepository.getAllPantryItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showPantryCreatedDialog = MutableStateFlow(false)
    val showPantryCreatedDialog: StateFlow<Boolean> = _showPantryCreatedDialog.asStateFlow()

    val allShoppingLists: StateFlow<List<ShoppingList>> = repository.getAllShoppingLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentListId = MutableStateFlow<Long?>(null)
    private val currentListId: StateFlow<Long?> = _currentListId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val shoppingItems: StateFlow<List<ShoppingListItem>> =
        currentListId.filterNotNull().flatMapLatest { repository.getShoppingItemsForList(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setActiveList(listId: Long) {
        _currentListId.value = listId
    }

    fun toggleCheck(item: ShoppingListItem) = viewModelScope.launch {
        repository.updateShoppingItem(item.copy(isChecked = !item.isChecked))
    }

    private fun generateFromSelectedRecipes(selectedRecipeIds: List<Long>) = viewModelScope.launch {
        val listId = currentListId.value ?: return@launch

        repository.deleteGeneratedItemsInList(listId)

        val crossRefs = selectedRecipeIds.flatMap {
            recipePantryItemRepository.getCrossRefsForRecipeOnce(it)
        }

        val pantryItems = pantryRepository.getAllPantryItems().firstOrNull().orEmpty()
        val pantryMap = pantryItems.associateBy { it.id }

        val aggregatedNeeds = crossRefs.filter { it.required }.groupBy { it.pantryItemId }
            .mapValues { (_, refs) -> refs.sumOf { parseAmount(it.amountNeeded) } }

        val itemsToInsert = aggregatedNeeds.mapNotNull { (pantryId, neededQty) ->
            val ownedQty = pantryMap[pantryId]?.quantity?.toDouble() ?: 0.0
            val toBuy = neededQty - ownedQty

            if (toBuy > 0.0) {
                ShoppingListItem(
                    listId = listId,
                    pantryItemId = pantryId,
                    name = pantryMap[pantryId]?.name ?: "Unknown",
                    quantity = formatQuantity(toBuy),
                    isChecked = false,
                    isGenerated = true
                )
            } else null
        }

        repository.insertShoppingItems(itemsToInsert)
    }

    fun deleteListWithItems(list: ShoppingList) = viewModelScope.launch {
        repository.deleteShoppingListWithItems(list)
        if (_currentListId.value == list.id) {
            _currentListId.value = null
        }
    }

    private fun parseAmount(input: String): Double {
        return input.trim().takeWhile { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
    }

    fun createListWithRecipesAndIngredients(
        name: String,
        recipeIds: List<Long>,
        ingredientQuantities: Map<Long, String>,
        onComplete: (Long) -> Unit
    ) = viewModelScope.launch {
// 1. Create the new shopping list
        val newList = ShoppingList(name = name)
        val newListId = repository.insertShoppingList(newList)

// 2. Set it as active
        setActiveList(newListId)

// 3. Generate items from selected recipes
        if (recipeIds.isNotEmpty()) {
            generateFromSelectedRecipes(recipeIds)
        }

// 4. Add manually selected ingredients with specified quantities
        if (ingredientQuantities.isNotEmpty()) {
            val pantryItems = pantryRepository.getAllPantryItems().firstOrNull().orEmpty()
            val pantryMap = pantryItems.associateBy { it.id }

            val manualItems = ingredientQuantities.mapNotNull { (id, qtyStr) ->
                val qty = qtyStr.trim().toDoubleOrNull()
                if (qty != null && qty > 0) {
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
                } else null
            }

            repository.insertShoppingItems(manualItems)
        }

// 5. Notify caller
        onComplete(newListId)
    }

    val categorizedItems: StateFlow<List<CategorizedShoppingItem>> = combine(
        shoppingItems, pantryRepository.getAllPantryItems()
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

    fun dismissPantryCreatedDialog() {
        _showPantryCreatedDialog.value = false
    }

    fun addItemByName(name: String, quantity: String) = viewModelScope.launch {

        val cleanedQuantity = quantity.toIntOrNull()?.toString() ?: quantity
        val listId = currentListId.value ?: return@launch
        if (name.isBlank()) return@launch

        val pantryItems = pantryRepository.getAllPantryItems().firstOrNull().orEmpty()
        val existingPantryItem = pantryItems.firstOrNull { it.name.equals(name, ignoreCase = true) }

        val pantryItem = existingPantryItem ?: run {
            val newItem = PantryItem(name = name.trim(), quantity = 0)
            val newId = pantryRepository.insert(newItem)
            _showPantryCreatedDialog.value = true
            newItem.copy(id = newId)
        }

// 🛑 Don’t add if the item is already stocked
        if (pantryItem.quantity > 0) return@launch

        val existingItem = shoppingItems.value.firstOrNull {
            it.name.equals(name, ignoreCase = true)
        }

        if (existingItem != null) {
            val existingQty = existingItem.quantity.toDoubleOrNull() ?: 0.0
            val addedQty = quantity.toDoubleOrNull() ?: 0.0
            val newQty = existingQty + addedQty

            val updatedItem = existingItem.copy(
                quantity = if (newQty > 0.0) formatQuantity(newQty) else existingItem.quantity
            )
            repository.updateShoppingItem(updatedItem)
        } else {
            val shoppingItem = ShoppingListItem(
                id = 0L,
                listId = listId,
                pantryItemId = pantryItem.id,
                recipeId = null,
                name = pantryItem.name,
                quantity = cleanedQuantity,
                isChecked = false,
                isGenerated = false,
                uuid = UUID.randomUUID().toString()
            )
        }
    }

    fun mergeSelectedRecipesIntoActiveList(newSelections: Map<Long, Int>) = viewModelScope.launch {
        val listId = currentListId.value ?: return@launch

// Step 1: Update total intent count per recipe
        newSelections.forEach { (recipeId, count) ->
            val previous = recipeAddCounts[recipeId] ?: 0
            recipeAddCounts[recipeId] = previous + count
        }

// Step 2: Build total pantry item needs from cumulative intent
        val fullNeeds = recipeAddCounts.flatMap { (recipeId, count) ->
            recipePantryItemRepository.getCrossRefsForRecipeOnce(recipeId).filter { it.required }
                .map { crossRef ->
                    crossRef.pantryItemId to parseAmount(crossRef.amountNeeded) * count
                }
        }

        val aggregatedNeeds =
            fullNeeds.groupBy { it.first }.mapValues { (_, entries) -> entries.sumOf { it.second } }

        val pantryItems = pantryRepository.getAllPantryItems().firstOrNull().orEmpty()
        val pantryMap = pantryItems.associateBy { it.id }

        val existingItems = shoppingItems.value
        val existingMap = existingItems.associateBy { it.pantryItemId }

        val itemsToUpdate = mutableListOf<ShoppingListItem>()
        val itemsToInsert = mutableListOf<ShoppingListItem>()

        aggregatedNeeds.forEach { (pantryId, totalNeeded) ->
            val ownedQty = pantryMap[pantryId]?.quantity?.toDouble() ?: 0.0
            val existingQty = existingMap[pantryId]?.quantity?.toDoubleOrNull() ?: 0.0
            val toBuy = (totalNeeded - ownedQty - existingQty).coerceAtLeast(0.0)

            if (toBuy > 0.0) {
                val updatedQty = existingQty + toBuy
                val itemName = pantryMap[pantryId]?.name ?: "Unknown"

                if (existingMap.containsKey(pantryId)) {
                    val existing = existingMap[pantryId]!!
                    itemsToUpdate.add(
                        existing.copy(
                            quantity = formatQuantity(updatedQty),
                            isGenerated = true
                        )
                    )
                } else {
                    itemsToInsert.add(
                        ShoppingListItem(
                            listId = listId,
                            pantryItemId = pantryId,
                            name = itemName,
                            quantity = formatQuantity(toBuy),
                            isChecked = false,
                            isGenerated = true
                        )
                    )
                }
            }
        }

        itemsToUpdate.forEach { repository.updateShoppingItem(it) }
        repository.insertShoppingItems(itemsToInsert)
    }

    fun updateShoppingItem(updatedItem: ShoppingListItem) = viewModelScope.launch {
        repository.updateShoppingItem(updatedItem)
    }

    fun deleteShoppingItem(item: ShoppingListItem) = viewModelScope.launch {
        repository.deleteShoppingItem(item)
    }
}