package ru.netology.nerecipe.adapter

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import ru.netology.nerecipe.R
import ru.netology.nerecipe.clickListeners.RecipeClickListeners
import ru.netology.nerecipe.databinding.FragmentViewRecipeBinding
import ru.netology.nerecipe.dragAndDropHelpers.ItemTouchHelperAdapter
import ru.netology.nerecipe.dragAndDropHelpers.OnStartDragListener
import ru.netology.nerecipe.recipe.Recipe
import java.util.*

class RecipeAdapter(
    private val clickListener: RecipeClickListeners,
    private var listener: OnStartDragListener
) : ListAdapter<Recipe, RecipeAdapter.ViewHolder>(DiffCallback), ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FragmentViewRecipeBinding.inflate(
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
        return true
    }

    override fun onItemDismiss(position: Int) {
        val updated = currentList.toMutableList()
        updated.removeAt(position)
        notifyItemRemoved(position)
        submitList(updated)
    }

    class ViewHolder(
        private val binding: FragmentViewRecipeBinding,
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
                            clickListener.clickedNewOrEdit(recipe)
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

            binding.editablePictureRecipe.setOnClickListener {
                clickListener.clickedRecipe(recipe)
            }

            binding.menuButton.setOnClickListener {
                popupMenu.show()
            }
        }

        fun bind(recipe: Recipe) {
            this.recipe = recipe
            with(binding) {
                Glide.with(userAvatar.context)
                    .load("https://loremflickr.com/100/100/face?random=${(1..1000).random()}")
                    .circleCrop()
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            userAvatar.setImageDrawable(resource)
                            ImageViewCompat.setImageTintList(userAvatar, null)
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            userAvatar.setImageResource(R.drawable.ic_baseline_person_24)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
                recipeViewTitle.text = recipe.title
                recipeViewCategory.text = recipe.category
                author.text = recipe.author
                favoriteButton.isChecked = recipe.isFavorite
                if (recipe.picture == "")
                    with(editablePictureRecipe) {
                        setImageResource(R.drawable.ic_launcher_foreground)
                        scaleType = ImageView.ScaleType.FIT_CENTER
                    }
                else
                    editablePictureRecipe.setImageBitmap(BitmapFactory.decodeFile(recipe.picture))
            }
        }
    }
}