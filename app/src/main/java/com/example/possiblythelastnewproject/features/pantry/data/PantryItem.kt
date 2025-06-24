package com.example.possiblythelastnewproject.features.pantry.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PantryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val quantity: Int,
    val imageData: ByteArray? = null,
    val shouldTrack: Boolean = true,           // Controls whether this item is included in pantry tracking
    val addToShoppingList: Boolean = true,    // Suggests automatic shopping list addition
    val scanCode: String? = null,             // Stores barcode, PLU, or any other scan code
    val category: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PantryItem

        if (id != other.id) return false
        if (quantity != other.quantity) return false
        if (shouldTrack != other.shouldTrack) return false
        if (addToShoppingList != other.addToShoppingList) return false
        if (name != other.name) return false
        if (imageData != null) {
            if (other.imageData == null) return false
            if (!imageData.contentEquals(other.imageData)) return false
        } else if (other.imageData != null) return false
        if (scanCode != other.scanCode) return false
        if (category != other.category) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + quantity
        result = 31 * result + shouldTrack.hashCode()
        result = 31 * result + addToShoppingList.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        result = 31 * result + (scanCode?.hashCode() ?: 0)
        result = 31 * result + category.hashCode()
        return result
    }
}