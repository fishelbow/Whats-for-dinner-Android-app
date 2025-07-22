package com.example.possiblythelastnewproject.core.utils

/**
 * Utility for resolving DB image URIs into disk-safe filenames or nested file paths.
 */
object ImagePathResolver {

    /**
     * Converts a DB image URI into a flat, filesystem-safe filename.
     *
     * Example:
     *   Input URI → "recipeImages/recipe_1432_hero"
     *   Output    → "recipeImages_recipe_1432_hero.jpg"
     */
    fun resolveFlatFilename(uri: String, extension: String = ".jpg"): String {
        val safeName = uri.replace("/", "_").trim()
        return "$safeName$extension"
    }

    /**
     * Converts a DB image URI into a nested path structure (for folder-based storage).
     *
     * Example:
     *   Input URI → "recipeImages/recipe_1432/hero"
     *   Output    → "recipeImages/recipe_1432/hero.jpg"
     */
    fun resolveNestedPath(uri: String, extension: String = ".jpg"): String {
        return "${uri.trim()}$extension"
    }
}