package com.example.possiblythelastnewproject.features.recipe.ui.componets.mainScreen

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.possiblythelastnewproject.features.recipe.data.RecipeWithIngredients
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

@Composable
fun RecipeScreenWithSearch(
    onRecipeClick: (Recipe) -> Unit,
    onAddClick: () -> Unit,
    viewModel: RecipesViewModel = hiltViewModel()
) {
    val allRecipes by viewModel.recipes.collectAsState()
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val focusManager = LocalFocusManager.current

    val filtered = remember(query.text, allRecipes) {
        if (query.text.isBlank()) allRecipes
        else allRecipes.filter { container ->
            val recipe = container.recipe
            recipe.name.contains(query.text, ignoreCase = true) ||
                    recipe.category.contains(query.text, ignoreCase = true) ||
                    recipe.instructions.contains(query.text, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            RecipeSearchBar(
                query = query.text,
                onQueryChange = { query = TextFieldValue(it) },
                onAddNewRecipe = onAddClick,
                focusManager = focusManager
            )
        }
    ) { innerPadding ->
        RecipeGridScreen(
            recipes = filtered,
            onRecipeClick = onRecipeClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
fun RecipeGridScreen(
    recipes: List<RecipeWithIngredients>,
    onRecipeClick: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.padding(8.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(recipes) { container ->
            RecipeTile(recipe = container.recipe) {
                onRecipeClick(container.recipe)
            }
        }
    }
}

@Composable
fun RecipeTile(recipe: Recipe, onClick: () -> Unit) {
    val cardColor = Color(recipe.color)
    val textColor = if (cardColor.luminance() < 0.5f) Color.White else Color.Black

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() }
            .border(
                width = 2.dp,
                color = cardColor,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            val context = LocalContext.current
            val bitmap by produceState<android.graphics.Bitmap?>(null, recipe.imageUri) {
                value = withContext(Dispatchers.IO) {
                    recipe.imageUri?.let { uriString ->
                        val uri = uriString.toUri()
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            android.graphics.BitmapFactory.decodeStream(stream)
                        }
                    }
                }
            }

            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = cardColor
                )
            }
        }
    }
}