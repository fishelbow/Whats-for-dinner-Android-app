package com.example.possiblythelastnewproject.features.recipe.ui.componets.mainScreen

import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.possiblythelastnewproject.features.recipe.data.RecipeWithIngredients
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel
import androidx.core.net.toUri
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun RecipeScreenWithSearch(
    onRecipeClick: (Recipe) -> Unit,
    onAddClick: () -> Unit,
    viewModel: RecipesViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val focusManager = LocalFocusManager.current
    val pagedRecipes = viewModel.pagedRecipes.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            RecipeSearchBar(
                query = query.text,
                onQueryChange = {
                    query = TextFieldValue(it)
                    viewModel.updateQuery(it) // 🔄 Trigger paging flow
                },
                onAddNewRecipe = onAddClick,
                focusManager = focusManager
            )
        }
    ) { innerPadding ->
        RecipeGridScreen(
            recipes = pagedRecipes,
            onRecipeClick = onRecipeClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
fun RecipeGridScreen(
    recipes: LazyPagingItems<RecipeWithIngredients>,
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
        items(recipes.itemCount) { index ->
            val container = recipes[index]
            container?.let {
                RecipeTile(recipe = it.recipe) {
                    onRecipeClick(it.recipe)
                }
            }
        }
    }
}

@Composable
fun RecipeTile(recipe: Recipe, onClick: () -> Unit) {
    val cardColor = Color(recipe.color)

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

            recipe.imageUri?.let { uriString ->
                val uri = uriString.toUri()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(Color.LightGray) // Your placeholder and fallback color
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Recipe image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
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
}