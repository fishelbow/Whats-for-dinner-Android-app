package com.example.possiblythelastnewproject.features.shoppingList.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.possiblythelastnewproject.features.pantry.data.PantryRepository
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.recipe.data.repository.RecipePantryItemRepository
import com.example.possiblythelastnewproject.features.shoppingList.data.ShoppingListRepository
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



    val allPantryItems: StateFlow<List<PantryItem>> = pantryRepository
        .getAllPantryItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    private val _showPantryCreatedDialog = MutableStateFlow(false)
    val showPantryCreatedDialog: StateFlow<Boolean> = _showPantryCreatedDialog.asStateFlow()

    val allShoppingLists: StateFlow<List<ShoppingList>> = repository
        .getAllShoppingLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentListId = MutableStateFlow<Long?>(null)
    private val currentListId: StateFlow<Long?> = _currentListId.asStateFlow()



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
                            quantity = "%.2f".format(qty),
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

    fun dismissPantryCreatedDialog() {
        _showPantryCreatedDialog.value = false
    }

    fun addItemByName(name: String, quantity: String) = viewModelScope.launch {

        val cleanedQuantity = quantity.toIntOrNull()?.toString() ?: quantity
        val listId = currentListId.value ?: return@launch
        if (name.isBlank()) return@launch

        val existingPantryItem = pantryRepository.getAllPantryItems()
            .firstOrNull()
            ?.firstOrNull { it.name.equals(name, ignoreCase = true) }

        val pantryItem = existingPantryItem ?: run {
            val newItem = com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem(
                name = name.trim(),
                quantity = 1
            )
            val newId = pantryRepository.insert(newItem)
            _showPantryCreatedDialog.value = true
            newItem.copy(id = newId)
        }

        val existingItem = shoppingItems.value.firstOrNull {
            it.name.equals(name, ignoreCase = true)
        }

        if (existingItem != null) {
            val existingQty = existingItem.quantity.toDoubleOrNull() ?: 0.0
            val addedQty = quantity.toDoubleOrNull() ?: 0.0
            val newQty = existingQty + addedQty

            val updatedItem = existingItem.copy(
                quantity = if (newQty > 0.0) "%.2f".format(newQty) else existingItem.quantity
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
            repository.insertShoppingItem(shoppingItem)
        }
    }
}