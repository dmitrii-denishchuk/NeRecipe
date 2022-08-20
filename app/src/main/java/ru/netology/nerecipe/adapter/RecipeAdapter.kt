package ru.netology.nerecipe.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nerecipe.R
import ru.netology.nerecipe.clickListeners.RecipeClickListeners
import ru.netology.nerecipe.databinding.FragmentRecipeViewBinding
import ru.netology.nerecipe.dragAndDropHelpers.ItemTouchHelperAdapter
import ru.netology.nerecipe.dragAndDropHelpers.OnStartDragListener
import ru.netology.nerecipe.recipe.Recipe
import java.util.*

class RecipeAdapter(
    private val clickListener: RecipeClickListeners,
    var listener: OnStartDragListener
) : ListAdapter<Recipe, RecipeAdapter.ViewHolder>(DiffCallback), ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FragmentRecipeViewBinding.inflate(
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

    private object DiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem == newItem
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val updated = currentList.toMutableList()
        Collections.swap(updated, fromPosition, toPosition)
        submitList(updated)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
        val updated = currentList.toMutableList()
        updated.removeAt(position)
        submitList(updated)
        notifyItemRemoved(position)
    }

    class ViewHolder(
        private val binding: FragmentRecipeViewBinding,
        clickListener: RecipeClickListeners
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var recipe: Recipe

        private val popupMenu by lazy {
            PopupMenu(itemView.context, binding.menuButton).apply {
                inflate(R.menu.options_recipe)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.removeButton -> {
                            clickListener.clickedRemove(recipe)
                            true
                        }
                        R.id.editButton -> {
                            clickListener.clickedEdit(recipe)
                            true
                        }
                        else -> false
                    }
                }
            }
        }

        init {
            binding.favoriteButton.setOnClickListener {
                clickListener.clickedFavorite(recipe)
            }

            binding.background.setOnClickListener {
                clickListener.clickedRecipe(recipe)
            }

            binding.menuButton.setOnClickListener {
                popupMenu.show()
            }
        }

        fun bind(recipe: Recipe) {
            this.recipe = recipe
            with(binding) {
                previewTitleRecipe.text = recipe.title
                previewCategory.text = recipe.category
                author.text = recipe.author
                favoriteButton.isChecked = recipe.isFavorite
                if (recipe.picture.isNotBlank())
                    background.setImageBitmap(BitmapFactory.decodeFile(recipe.picture))
                else
                    background.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }
    }
}