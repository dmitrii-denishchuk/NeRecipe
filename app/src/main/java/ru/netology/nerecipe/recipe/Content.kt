package ru.netology.nerecipe.recipe

import kotlinx.serialization.Serializable

@Serializable
data class Content(
    var id: Int,
    val step: String,
    val content: String,
    val picture: String,
)

