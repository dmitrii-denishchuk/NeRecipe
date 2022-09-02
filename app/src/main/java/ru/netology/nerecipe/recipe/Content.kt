package ru.netology.nerecipe.recipe

import kotlinx.serialization.Serializable

@Serializable
data class Content(
    var id: Int,
    var step: String,
    val content: String,
    val picture: String,
)

