package com.example.possiblythelastnewproject.features.pantry.ui.componets

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.tooling.preview.Preview
import com.example.possiblythelastnewproject.core.utils.truncateWithEllipsis

@Composable
fun IngredientCard(
    ingredient: String,
    quantity: Int = 0,
    defaultImage: ImageVector = Icons.Default.Image,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    category: String? = null,
    imageData: ByteArray? = null // move this to the end and give it a default value
){
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f), // Ensures the card is square.
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Display the image area.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageData != null) {
                        AsyncImage(
                            model = imageData,
                            contentDescription = "Ingredient Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = defaultImage,
                            contentDescription = "Ingredient Image",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .fillMaxSize()
                        )
                    }
                }
                // Ingredient name.
                val displayName = truncateWithEllipsis(ingredient)
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip, // ellipsis is now manual
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .fillMaxWidth()
                )
            }
            // Quantity overlay in the bottom right corner.
            Text(
                text = "x$quantity",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                ),
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IngredientCardPreview() {
    MaterialTheme {
        IngredientCard(
            ingredient = "Eggshfgkjhgfljhfgl;jhfkjghfghdgflhjlkgh;;;;;;;gjhl;kglgghllllllllllllllllllllllllllllllllllllllllll",
            quantity = 12,
            imageData = null
        )
    }
}