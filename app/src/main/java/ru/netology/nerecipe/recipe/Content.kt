package ru.netology.nerecipe.recipe

import kotlinx.serialization.Serializable
import ru.netology.nerecipe.data.repository.ContentRepository

@Serializable
data class Content(
    val id: Int,
    val step: String,
    val content: String,
    val picture: String,
)

