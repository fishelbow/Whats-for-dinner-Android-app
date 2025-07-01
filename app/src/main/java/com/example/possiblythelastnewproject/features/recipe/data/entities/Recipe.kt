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
    val imageData: ByteArray? = null,
    @ColorInt val color: Int = 0,
    val uuid: String = UUID.randomUUID().toString()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Recipe

        if (id != other.id) return false
        if (color != other.color) return false
        if (name != other.name) return false
        if (temp != other.temp) return false
        if (prepTime != other.prepTime) return false
        if (cookTime != other.cookTime) return false
        if (category != other.category) return false
        if (instructions != other.instructions) return false
        if (imageData != null) {
            if (other.imageData == null) return false
            if (!imageData.contentEquals(other.imageData)) return false
        } else if (other.imageData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + color
        result = 31 * result + name.hashCode()
        result = 31 * result + temp.hashCode()
        result = 31 * result + prepTime.hashCode()
        result = 31 * result + cookTime.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + instructions.hashCode()
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        return result
    }
}