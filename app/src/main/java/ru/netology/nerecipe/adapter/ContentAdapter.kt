package ru.netology.nerecipe.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nerecipe.R
import ru.netology.nerecipe.clickListeners.ContentClickListeners
import ru.netology.nerecipe.databinding.RecipeContentEditLayoutBinding
import ru.netology.nerecipe.databinding.RecipeContentLayoutBinding
import ru.netology.nerecipe.dragAndDropHelpers.ItemTouchHelperAdapter
import ru.netology.nerecipe.dragAndDropHelpers.OnStartDragListener
import ru.netology.nerecipe.recipe.Content
import java.util.*

class ContentAdapter(
    private val clickListener: ContentClickListeners,
    private var listener: OnStartDragListener
) : ListAdapter<Content, RecyclerView.ViewHolder>(DiffCallback), ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.fragment_recipe_view -> ContentViewHolder(RecipeContentLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false), clickListener)
            else -> ContentEditViewHolder(RecipeContentEditLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ContentViewHolder -> {
                holder.bind(getItem(position))
                holder.itemView.setOnLongClickListener {
                    listener.onStartDrag(holder)
                    false
                }
            }
            is ContentEditViewHolder -> {
                holder.bind(getItem(position))
                holder.itemView.setOnLongClickListener {
                    listener.onStartDrag(holder)
                    false
                }
            }
        }
    }

//    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
//        holder.bind(getItem(position))
//        holder.itemView.setOnLongClickListener {
//            listener.onStartDrag(holder)
//            false
//        }
//    }

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

    class ContentEditViewHolder(
        private val binding: RecipeContentEditLayoutBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var content: Content

        fun bind(content: Content) {
            this.content = content
            with(binding) {
                stepName.setText(content.step)
                viewContent.setText(content.content)
                if (content.picture.isNotBlank())
                    recipeViewBackground.setImageBitmap(BitmapFactory.decodeFile(content.picture))
                else
                    recipeViewBackground.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }
    }

    class ContentViewHolder(
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
                stepName.text = content.step
                viewContent.text = content.content
                if (content.picture.isNotBlank())
                    recipeViewBackground.setImageBitmap(BitmapFactory.decodeFile(content.picture))
                else
                    recipeViewBackground.setImageResource(R.drawable.ic_launcher_foreground)
                viewContent
            }
        }
    }
}