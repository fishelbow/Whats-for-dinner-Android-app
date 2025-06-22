package com.example.possiblythelastnewproject.features.recipe.data.entities
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.possiblythelastnewproject.features.pantry.data.PantryItem

@Entity(
    tableName = "RecipePantryItemCrossRef",
    primaryKeys = ["recipeId", "pantryItemId"],
    indices = [
        // index pantryItemId so lookups and deletes cascade efficiently
        Index(value = ["pantryItemId"]),
        // (optional) if you ever look up by recipeId alone, you can index it too:
        // Index(value = ["recipeId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PantryItem::class,
            parentColumns = ["id"],
            childColumns = ["pantryItemId"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class RecipePantryItemCrossRef(
    val recipeId: Long,
    val pantryItemId: Long,
    val required: Boolean = false,
    val amountNeeded: String = ""
)