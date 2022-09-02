package ru.netology.nerecipe.clickListeners

import ru.netology.nerecipe.recipe.Content

interface ContentClickListeners {
    fun clickedRemoveOrAdd(content: Content)
    fun clickedAddPicture()
}