package com.example.possiblythelastnewproject.features.recipe.ui.componets.mainScreen

import android.graphics.BitmapFactory
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.possiblythelastnewproject.features.recipe.data.RecipeWithIngredients
import com.example.possiblythelastnewproject.features.recipe.data.entities.Recipe
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel

import kotlin.math.roundToInt

// Add this to the bottom of the file that defines RecipeScreenWithSearch:

@Composable
fun RecipeScreenWithSearch(
    onRecipeClick: (Recipe) -> Unit,
    onAddClick: () -> Unit,
    viewModel: RecipesViewModel = hiltViewModel(),
    onCardCycle: (() -> Unit)? = null,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            RecipeStackScreen(
                recipes = filtered,
                onCardCycle = onCardCycle,
                onCardClick = onRecipeClick
            )
        }
    }
}



// --- Helper composable for the consistent upside down polaroid card content ---
// It accepts a cardColor to color the top caption area. The text color is computed for contrast.
@Composable
fun PolaroidCardContent(recipe: Recipe, cardColor: Color) {
// Compute caption text color based on cardColor luminance – if dark then white, otherwise black.
    val captionTextColor = if (cardColor.luminance() < 0.5f) Color.White else Color.Black

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top caption area uses the cardColor.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(cardColor)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.titleLarge,
                color = captionTextColor
            )
        }
        // No spacer here so that the image directly touches the caption area.
        // Image section fills the remaining space.
        if (recipe.imageData != null && recipe.imageData!!.isNotEmpty()) {
            val bitmap = remember(recipe.imageData) {
                BitmapFactory.decodeByteArray(recipe.imageData, 0, recipe.imageData!!.size)
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Recipe Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(
                            // Only bottom corners are rounded so that the overall card remains cohesive.
                            RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                        )
                )
            }
        }
    }
}

// --- Swipeable card with polaroid styling ---
@Composable
fun SwipeableCardVertical(
    recipe: Recipe,
// cardColor is passed in from recipe; it colors the top caption.
    cardColor: Color,
    defaultTargetOffset: Float,
    onSwiped: () -> Unit,
    onReverseSwiped: () -> Unit,
    onCardClick: (() -> Unit)? = null
) {
    val offsetY = remember { Animatable(0f) }
    val zIndex = remember { mutableFloatStateOf(1f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

// Drag thresholds (in pixels)
    val removalThreshold = -100f
    val reverseThreshold = 100f
    val bump = with(density) { 50.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(280.dp) // Extra vertical space for the polaroid effect.
            .padding(4.dp)
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .zIndex(zIndex.floatValue)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch { offsetY.snapTo(offsetY.value + dragAmount.y) }
                    },
                    onDragEnd = {
                        scope.launch {
                            when {
                                offsetY.value < removalThreshold -> {
                                    offsetY.animateTo(
                                        targetValue = defaultTargetOffset,
                                        animationSpec = tween(150, easing = FastOutSlowInEasing)
                                    )
                                    zIndex.floatValue = -100f
                                    offsetY.animateTo(0f, tween(300))
                                    onSwiped()
                                    zIndex.floatValue = 1f
                                }
                                offsetY.value > reverseThreshold -> {
                                    offsetY.animateTo(bump, tween(150, easing = FastOutSlowInEasing))
                                    offsetY.animateTo(0f, tween(300))
                                    onReverseSwiped()
                                }
                                else -> offsetY.animateTo(0f, tween(300))
                            }
                        }
                    }
                )
            }
            .clickable { onCardClick?.invoke() }
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface),
            // Use transparent container so the outer border is the visual tie-in.
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            PolaroidCardContent(recipe = recipe, cardColor = cardColor)
        }
    }
}

// --- RecipeStackScreen with consistent card styling ---
@Composable
fun RecipeStackScreen(
    recipes: List<RecipeWithIngredients>,
    onCardCycle: (() -> Unit)? = null,
    onCardClick: (Recipe) -> Unit = {}
) {
    val deck = remember(recipes) {
        mutableStateListOf<RecipeWithIngredients>().apply { addAll(recipes) }
    }

    var reverseIncomingCard by remember { mutableStateOf<RecipeWithIngredients?>(null) }

    val maxSpacing = 24.dp
    val spacing = if (deck.size > 10) maxSpacing * (10f / deck.size) else maxSpacing

    val cardHeight = 280.dp
    val extraMargin = 200.dp
    val clearancePx = with(LocalDensity.current) {
        (cardHeight + spacing + extraMargin).toPx()
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        deck.forEachIndexed { index, container ->
            val recipe = container.recipe
            key(recipe.id) {
                val cardColor = Color(recipe.color)
                val offsetY by animateDpAsState(targetValue = -(index * spacing))

                if (index == 0) {
                    SwipeableCardVertical(
                        recipe = recipe,
                        cardColor = cardColor,
                        defaultTargetOffset = -clearancePx,
                        onSwiped = {
                            val top = deck.removeAt(0)
                            deck.add(top)
                            onCardCycle?.invoke()
                        },
                        onReverseSwiped = {
                            if (deck.isNotEmpty()) {
                                val last = deck.removeAt(deck.lastIndex)
                                reverseIncomingCard = last
                                onCardCycle?.invoke()
                            }
                        },
                        onCardClick = { onCardClick(recipe) }
                    )
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(cardHeight)
                            .padding(4.dp)
                            .offset(y = offsetY)
                            .zIndex(-index.toFloat())
                            .clickable { onCardClick(recipe) },
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        PolaroidCardContent(recipe = recipe, cardColor = cardColor)
                    }
                }
            }
        }

        reverseIncomingCard?.let { container ->
            val recipe = container.recipe
            val cardColor = Color(recipe.color)
            val animOffset: Animatable<Dp, AnimationVector1D> = remember {
                Animatable((-300).dp, Dp.VectorConverter)
            }

            LaunchedEffect(container) {
                animOffset.animateTo(
                    targetValue = 0.dp,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
                deck.add(0, container)
                reverseIncomingCard = null
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(cardHeight)
                    .padding(4.dp)
                    .offset(y = animOffset.value)
                    .zIndex(10f)
                    .clickable { onCardClick(recipe) },
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                PolaroidCardContent(recipe = recipe, cardColor = cardColor)
            }
        }
    }
}


