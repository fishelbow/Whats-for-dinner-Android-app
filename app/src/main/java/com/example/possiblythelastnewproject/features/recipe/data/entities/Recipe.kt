package com.example.possiblythelastnewproject.features.recipe.data.entities

import androidx.annotation.ColorInt
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID


@Serializable
@Entity(
    indices = [Index(value = ["uuid"], unique = true)]
)
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "recipe_name") val name: String,
    val temp: String,
    val prepTime: String,
    val cookTime: String,
    val category: String,
    val instructions: String,
    val imageUri: String? = null,
    @ColorInt val color: Int = 0,
    val uuid: String = UUID.randomUUID().toString()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Recipe) return false

        return id == other.id &&
                name == other.name &&
                temp == other.temp &&
                prepTime == other.prepTime &&
                cookTime == other.cookTime &&
                category == other.category &&
                instructions == other.instructions &&
                imageUri == other.imageUri &&
                color == other.color &&
                uuid == other.uuid
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + temp.hashCode()
        result = 31 * result + prepTime.hashCode()
        result = 31 * result + cookTime.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + instructions.hashCode()
        result = 31 * result + (imageUri?.hashCode() ?: 0)
        result = 31 * result + color
        result = 31 * result + uuid.hashCode()
        return result
    }
}