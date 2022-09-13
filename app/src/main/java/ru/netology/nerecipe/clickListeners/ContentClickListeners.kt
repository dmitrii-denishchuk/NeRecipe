package ru.netology.nerecipe.clickListeners

import ru.netology.nerecipe.recipe.Step

interface ContentClickListeners {
    fun clickedAddOrRemoveStep(step: Step)
    fun clickedAddPicture(step: Step)
    fun clickedEnterText(step: Step)
}