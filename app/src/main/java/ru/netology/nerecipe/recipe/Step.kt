package ru.netology.nerecipe.recipe

import kotlinx.serialization.Serializable

@Serializable
data class Step(
    val id: Int = 1000,
    val title: String = "",
    val content: String = "",
    val picture: String = "",
)