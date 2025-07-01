package com.example.possiblythelastnewproject.features.recipe.data.entities
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.possiblythelastnewproject.features.pantry.data.entities.PantryItem
import kotlinx.serialization.Serializable
import java.util.UUID


@Serializable
@Entity(
    tableName = "RecipePantryItemCrossRef",
    primaryKeys = ["recipeId", "pantryItemId"],
    indices = [
        Index(value = ["pantryItemId"]),
        Index(value = ["uuid"], unique = true)

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
    val amountNeeded: String = "",
    val uuid: String = UUID.randomUUID().toString()
)