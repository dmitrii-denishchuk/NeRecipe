package ru.netology.nerecipe.recipe

import kotlinx.serialization.Serializable
import ru.netology.nerecipe.data.repository.ContentRepository

@Serializable
data class Content(
    val id: Double,
    val content: String,
    val picture: String,
)

