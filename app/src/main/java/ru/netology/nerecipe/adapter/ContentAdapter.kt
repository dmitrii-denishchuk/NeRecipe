package ru.netology.nerecipe.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nerecipe.R
import ru.netology.nerecipe.clickListeners.ContentClickListeners
import ru.netology.nerecipe.databinding.RecipeContentLayoutBinding
import ru.netology.nerecipe.dragAndDropHelpers.ItemTouchHelperAdapter
import ru.netology.nerecipe.dragAndDropHelpers.OnStartDragListener
import ru.netology.nerecipe.recipe.Content
import java.util.*

class ContentAdapter(
    private val clickListener: ContentClickListeners,
    var listener: OnStartDragListener
) : ListAdapter<Content, ContentAdapter.ViewHolder>(DiffCallback), ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecipeContentLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.setOnLongClickListener {
            listener.onStartDrag(holder)
            false
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Content>() {
        override fun areItemsTheSame(oldItem: Content, newItem: Content) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Content, newItem: Content) = oldItem == newItem
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(currentList, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
        currentList.removeAt(position)
        notifyItemRemoved(position)
    }

    class ViewHolder(
        private val binding: RecipeContentLayoutBinding,
        clickListener: ContentClickListeners
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var content: Content

        private val popupMenu by lazy {
            PopupMenu(itemView.context, binding.menuButton).apply {
                inflate(R.menu.options_recipe)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.removeButton -> {
                            clickListener.clickedRemove(content)
                            true
                        }
                        R.id.editButton -> {
                            clickListener.clickedEdit(content)
                            true
                        }
                        else -> false
                    }
                }
            }
        }

        init {
            binding.menuButton.setOnClickListener {
                popupMenu.show()
            }
        }

        fun bind(content: Content) {
            this.content = content
            with(binding) {
                viewContent.text = content.content
//                if (recipe.picture != "") background.setImageURI(recipe.picture.toUri())
//                else return
            }
        }
    }
}