package ru.netology.nerecipe.dragAndDropHelpers

import androidx.recyclerview.widget.RecyclerView

interface OnStartDragListener {
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder?)
}