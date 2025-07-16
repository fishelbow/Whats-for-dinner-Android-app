package com.example.possiblythelastnewproject.features.pantry.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    indices = [Index(value = ["uuid"], unique = true)]
)
data class PantryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val quantity: Int,
    val imageUri: String? = null,
    val shouldTrack: Boolean = true,           // Controls whether this item is included in pantry tracking
    val addToShoppingList: Boolean = true,    // Suggests automatic shopping list addition
    val scanCode: String? = null,             // Stores barcode, PLU, or any other scan code
    val category: String = "",
    val uuid: String = UUID.randomUUID().toString()
) {
    val hasScanCode: Boolean // not sure yet been tricked before may come back and delete TODO()
        get() = !scanCode.isNullOrBlank()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PantryItem) return false

        return id == other.id &&
                name == other.name &&
                quantity == other.quantity &&
                imageUri == other.imageUri &&
                shouldTrack == other.shouldTrack &&
                addToShoppingList == other.addToShoppingList &&
                scanCode == other.scanCode &&
                category == other.category &&
                uuid == other.uuid
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + quantity
        result = 31 * result + (imageUri?.hashCode() ?: 0)
        result = 31 * result + shouldTrack.hashCode()
        result = 31 * result + addToShoppingList.hashCode()
        result = 31 * result + (scanCode?.hashCode() ?: 0)
        result = 31 * result + category.hashCode()
        result = 31 * result + uuid.hashCode()
        return result
    }
}

