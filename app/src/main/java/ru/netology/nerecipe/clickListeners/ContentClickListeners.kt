package ru.netology.nerecipe.clickListeners

import ru.netology.nerecipe.recipe.Content

interface ContentClickListeners {
    fun clickedRemove(content: Content)
    fun clickedEdit(content: Content)
}