package com.example.possiblythelastnewproject.features.recipe.ui.componets.ingredientChips

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun <T> LazyFlowRow(
    modifier: Modifier = Modifier,
    items: List<T>,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    itemContent: @Composable (T) -> Unit
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val horizontalSpacingPx = horizontalSpacing.roundToPx()
        val verticalSpacingPx = verticalSpacing.roundToPx()
        val maxWidth = constraints.maxWidth

        val placeables = mutableListOf<PlaceableWithRow>()

        var rowWidth = 0
        var rowHeight = 0
        var currentRow = 0
        var yOffset = 0

        val rowHeights = mutableListOf<Int>()
        val rowOffsets = mutableListOf<Int>()

        items.forEachIndexed { index, item ->
            val placeable = subcompose(index) {
                itemContent(item)
            }.first().measure(Constraints())

            if (rowWidth + placeable.width > maxWidth && rowWidth > 0) {
                rowHeights.add(rowHeight)
                rowOffsets.add(yOffset)
                yOffset += rowHeight + verticalSpacingPx
                rowWidth = 0
                rowHeight = 0
                currentRow++
            }

            placeables.add(
                PlaceableWithRow(
                    placeable = placeable,
                    x = rowWidth,
                    y = yOffset,
                    row = currentRow
                )
            )

            rowWidth += placeable.width + horizontalSpacingPx
            rowHeight = max(rowHeight, placeable.height)
        }

        rowHeights.add(rowHeight)
        rowOffsets.add(yOffset)

        val totalHeight = yOffset + rowHeight

        layout(width = maxWidth, height = totalHeight) {
            placeables.forEach {
                it.placeable.placeRelative(it.x, it.y)
            }
        }
    }
}

private data class PlaceableWithRow(
    val placeable: androidx.compose.ui.layout.Placeable,
    val x: Int,
    val y: Int,
    val row: Int
)