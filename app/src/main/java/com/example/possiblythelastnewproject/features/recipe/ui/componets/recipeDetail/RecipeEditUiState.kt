import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.TextFieldValue
import com.example.possiblythelastnewproject.features.recipe.data.RecipeWithIngredients
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.data.entities.RecipePantryItemCrossRef
import com.example.possiblythelastnewproject.features.recipe.ui.componets.recipeCreation.RecipeIngredientUI

data class RecipeEditUiState(
    var name: TextFieldValue = TextFieldValue(""),
    var temp: TextFieldValue = TextFieldValue(""),
    var prepTime: TextFieldValue = TextFieldValue(""),
    var cookTime: TextFieldValue = TextFieldValue(""),
    var category: TextFieldValue = TextFieldValue(""),
    var instructions: TextFieldValue = TextFieldValue(""),
    var cardColor: Color = Color.White,
    var imageUri: String? = null,
    var ingredients: List<RecipeIngredientUI> = emptyList(),
    var newIngredient: String = ""
) {

    fun rollbackFromSnapshot(
        snapshot: RecipeWithIngredients,
        crossRefs: List<RecipePantryItemCrossRef>,
        originalImageUri: String?,
        context: Context,
        deleteFn: (String, Context) -> Unit,
        onImageReset: (String) -> Unit = {},
        resetUnsavedImageChange: () -> Unit = {}
    ): Recipe {
        rollbackImageIfNeeded(originalImageUri, context, deleteFn)
        applyRecipe(snapshot, crossRefs)

        val restoredUri = snapshot.recipe.imageUri.orEmpty()
        onImageReset(restoredUri)
        resetUnsavedImageChange()

        return toRecipeModel(snapshot.recipe)
    }

    fun applyRecipe(recipe: RecipeWithIngredients, crossRefs: List<RecipePantryItemCrossRef>) {
        val newState = snapshotFrom(recipe, crossRefs)
        name = newState.name
        temp = newState.temp
        prepTime = newState.prepTime
        cookTime = newState.cookTime
        category = newState.category
        instructions = newState.instructions
        cardColor = newState.cardColor
        imageUri = newState.imageUri
        ingredients = newState.ingredients
        newIngredient = ""
    }

    fun isChangedFrom(
        original: RecipeWithIngredients,
        crossRefs: List<RecipePantryItemCrossRef>,
        originalImageUri: String? = null
    ): Boolean {
        val snapshot = snapshotFrom(original, crossRefs, overrideImage = originalImageUri)
        return this != snapshot
    }

    private fun snapshotFrom(
        original: RecipeWithIngredients,
        crossRefs: List<RecipePantryItemCrossRef>,
        overrideImage: String? = null
    ): RecipeEditUiState {
        val refMap = crossRefs.associateBy { it.pantryItemId }

        val enrichedIngredients = original.ingredients.mapNotNull { pantry ->
            val ref = refMap[pantry.id]
            ref?.let {
                RecipeIngredientUI(
                    name = pantry.name,
                    pantryItemId = pantry.id,
                    includeInShoppingList = pantry.addToShoppingList,
                    includeInPantry = true,
                    hasScanCode = pantry.scanCode?.isNotBlank() == true,
                    amountNeeded = it.amountNeeded,
                    required = it.required
                )
            }
        }

        return RecipeEditUiState(
            name = TextFieldValue(original.recipe.name),
            temp = TextFieldValue(original.recipe.temp),
            prepTime = TextFieldValue(original.recipe.prepTime),
            cookTime = TextFieldValue(original.recipe.cookTime),
            category = TextFieldValue(original.recipe.category),
            instructions = TextFieldValue(original.recipe.instructions),
            cardColor = Color(original.recipe.color),
            imageUri = overrideImage ?: original.recipe.imageUri,
            ingredients = enrichedIngredients,
            newIngredient = ""
        )
    }

    fun rollbackImageIfNeeded(originalUri: String?, context: Context, deleteFn: (String, Context) -> Unit) {
        if (imageUri != originalUri) {
            imageUri?.let {
                deleteFn(it, context)
                Log.d("ImageCleanup", "🧼 Rolled back → $it")
            }
            imageUri = originalUri
        }
    }

    fun toRecipeModel(original: Recipe): Recipe {
        return original.copy(
            name = name.text.trim(),
            temp = temp.text.trim(),
            prepTime = prepTime.text.trim(),
            cookTime = cookTime.text.trim(),
            category = category.text.trim(),
            instructions = instructions.text.trim(),
            imageUri = imageUri ?: "",
            color = cardColor.toArgb()
        )
    }
}