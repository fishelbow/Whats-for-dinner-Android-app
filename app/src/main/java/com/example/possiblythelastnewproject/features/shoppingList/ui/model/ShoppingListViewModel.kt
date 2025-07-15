package com.example.possiblythelastnewproject.features.shoppingList.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.possiblythelastnewproject.features.pantry.data.PantryRepository
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.RecipeSelection
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingList
import com.example.possiblythelastnewproject.features.shoppingList.data.entities.ShoppingListItem
import com.example.possiblythelastnewproject.features.shoppingList.data.repositories.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val repository: ShoppingListRepository,
    private val pantryRepository: PantryRepository
) : ViewModel() {

    // ── Active Shopping List ID ────────────────────────────────────────────────
    private val _activeListId = MutableStateFlow<Long?>(null)
    val activeListId: StateFlow<Long?> = _activeListId.asStateFlow()

    fun setActiveListId(id: Long) {
        _activeListId.value = id
    }

    // ── All Shopping Lists Overview ──────────────────────────────────────────
    val allShoppingLists: StateFlow<List<ShoppingList>> =
        repository.getAllShoppingLists()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Recipe Selections for Active List ────────────────────────────────────
    @OptIn(ExperimentalCoroutinesApi::class)
    val recipeSelections: StateFlow<List<RecipeSelection>> =
        activeListId
            .filterNotNull()
            .flatMapLatest { repository.getSelectionsForListFlow(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setHideFound(b: Boolean) {
        _hideFound.value = b
    }

    // ── Raw Shopping Items from Repository ────────────────────────────────────
    @OptIn(ExperimentalCoroutinesApi::class)
    private val shoppingItems: StateFlow<List<ShoppingListItem>> =
        activeListId
            .filterNotNull()
            .flatMapLatest { repository.getShoppingItemsForList(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Pantry Items Reference ────────────────────────────────────────────────
    val pantryItems: StateFlow<List<PantryItem>> =
        pantryRepository.getAllPantryItems()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Categorized Items (Item + Category) ──────────────────────────────────
    private val categorizedItems: StateFlow<List<CategorizedShoppingItem>> =
        combine(shoppingItems, pantryItems) { items, pantry ->
            val lookup = pantry.associateBy { it.id }
            items.map { item ->
                val category = lookup[item.pantryItemId]?.category.orEmpty()
                CategorizedShoppingItem(item, category.ifBlank { "Uncategorized" })
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Hide Found Toggle & Visible Items ────────────────────────────────────
    private val _hideFound = MutableStateFlow(false)
    val hideFound: StateFlow<Boolean> = _hideFound.asStateFlow()

    val visibleItems: StateFlow<List<CategorizedShoppingItem>> =
        combine(categorizedItems, hideFound) { items, hide ->
            val visible = if (hide) items.filter { !it.item.isChecked } else items

            visible
                .groupBy { it.item.name to it.category }
                .map { (_, group) ->
                    val first = group.first()
                    val totalQty = group.sumOf { it.item.quantity.toDoubleOrNull() ?: 0.0 }
                    val mergedItem = first.item.copy(quantity = formatQty(totalQty))
                    CategorizedShoppingItem(mergedItem, first.category)
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Create / Delete Shopping Lists ────────────────────────────────────────
    fun createNewList(name: String, onComplete: (Long) -> Unit) = viewModelScope.launch {
        val newId = repository.insertShoppingList(ShoppingList(name = name))
        _activeListId.value = newId
        onComplete(newId)
    }

    fun deleteList(list: ShoppingList) = viewModelScope.launch {
        repository.deleteShoppingListWithItems(list)
        if (_activeListId.value == list.id) {
            _activeListId.value = null
        }
    }

    // ── Recipe Selection Actions ──────────────────────────────────────────────
    fun addRecipe(recipeId: Long) = viewModelScope.launch {
        _activeListId.value?.let { repository.addRecipeToList(it, recipeId) }
    }

    fun removeRecipe(recipeId: Long) = viewModelScope.launch {
        _activeListId.value?.let { repository.removeRecipeInstance(it, recipeId) }
    }

    // ── Manual Ingredient Dialog Control ─────────────────────────────────────
    private val _showCreatePantryItemDialog = MutableStateFlow<PantryItem?>(null)
    val showCreatePantryItemDialog: StateFlow<PantryItem?> = _showCreatePantryItemDialog.asStateFlow()

    fun addIngredientByName(name: String, quantity: String) = viewModelScope.launch {
        val listId = _activeListId.value ?: return@launch
        val parsedQty = quantity.toDoubleOrNull()?.takeIf { it > 0 } ?: 1.0
        val existing = pantryItems.value.firstOrNull { it.name.equals(name, ignoreCase = true) }

        val tempPantry = existing ?: PantryItem(name = name.trim(), quantity = 0, addToShoppingList = true)
            .also { _showCreatePantryItemDialog.value = it }

        val item = ShoppingListItem(
            listId = listId,
            pantryItemId = tempPantry.id.takeIf { it != 0L },
            name = tempPantry.name,
            quantity = formatQty(parsedQty),
            isGenerated = false,
            isChecked = false
        )

        repository.insertShoppingItems(listOf(item))
    }

    fun confirmCreatePantryItem(item: PantryItem) = viewModelScope.launch {
        val newId = pantryRepository.insert(item)
        _showCreatePantryItemDialog.value = null
        addIngredientByName(item.name, "1")
    }

    fun cancelCreatePantryItem() {
        _showCreatePantryItemDialog.value = null
    }

    // ── Ingredient Control: Delete & Toggle Found ───────────────────────────
    fun deleteItem(item: ShoppingListItem) = viewModelScope.launch {
        repository.deleteManualIngredient(item)

        if (item.isGenerated && item.recipeId != null) {
            cleanupEmptyRecipesFromDb()
        }
    }

    fun toggleItemChecked(uuid: String, checked: Boolean) = viewModelScope.launch {
        repository.toggleFoundStatus(uuid, checked)
    }

    // ── Undo / Redo ──────────────────────────────────────────────────────────
    fun undoLast() = viewModelScope.launch {
        _activeListId.value?.let { repository.undoLastAction(it) }
    }

    // ── Utility ──────────────────────────────────────────────────────────────
    private fun formatQty(q: Double): String =
        if (q % 1.0 == 0.0) q.toInt().toString() else "%.2f".format(q)

    fun getGeneratedIngredientsForRecipe(recipeId: Long): List<ShoppingListItem> {
        return categorizedItems.value
            .map { it.item }
            .filter { it.isGenerated && it.recipeId == recipeId }
    }

    private suspend fun cleanupEmptyRecipesFromDb() {
        val listId = _activeListId.value ?: return

        val selections = recipeSelections.value
        val allItems = repository.getShoppingItemsForList(listId).first() // Direct pull from DB

        val grouped = allItems.filter { it.isGenerated && it.recipeId != null }
            .groupBy { it.recipeId }

        selections.forEach { selection ->
            val recipeId = selection.recipeId
            val items = grouped[recipeId] ?: emptyList()

            if (items.isEmpty()) {
                removeRecipe(recipeId)
            }
        }
    }

}



