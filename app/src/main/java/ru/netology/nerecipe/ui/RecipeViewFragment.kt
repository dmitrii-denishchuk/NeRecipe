package ru.netology.nerecipe.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import ru.netology.nerecipe.R
import ru.netology.nerecipe.adapter.ContentAdapter
import ru.netology.nerecipe.adapter.RecipeAdapter
import ru.netology.nerecipe.clickListeners.ContentClickListeners
import ru.netology.nerecipe.clickListeners.RecipeClickListeners
import ru.netology.nerecipe.databinding.FragmentRecipeViewBinding
import ru.netology.nerecipe.dragAndDropHelpers.OnStartDragListener
import ru.netology.nerecipe.recipe.Content
import ru.netology.nerecipe.recipe.Recipe
import ru.netology.nerecipe.viewModel.RecipeViewModel

class RecipeViewFragment : Fragment() {

    var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRecipeViewBinding.inflate(
            inflater,
            container,
            false
        )

        binding.recipeContentLayout.visibility = View.VISIBLE

        val viewModel: RecipeViewModel by viewModels(ownerProducer = ::requireParentFragment)

        val contentAdapter = ContentAdapter(object : ContentClickListeners {
            override fun clickedRemoveOrAdd() {
            }

            override fun clickedAddPicture() {
            }
        },
            object : OnStartDragListener {
                override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
                    itemTouchHelper!!.startDrag((viewHolder!!))
                }
            }
        )

        val recipeViewHolder =
            RecipeAdapter.ViewHolder(binding, object : RecipeClickListeners {
                override fun clickedFavorite(recipe: Recipe) {
                    viewModel.clickedFavorite(recipe)
                }

                override fun clickedRemove(recipe: Recipe) {
                    viewModel.clickedRemove(recipe)
                    findNavController().navigateUp()
                }

                override fun clickedEdit(recipe: Recipe) {
                    viewModel.clickedEdit(recipe)
                    findNavController().navigate(
                        R.id.action_recipe_view_fragment_to_new_recipe_fragment,
                        bundleOf("content" to recipe.content)
                    )
                }

                override fun clickedRecipe(recipe: Recipe) {
                }
            }
        )

        binding.recipeContentLayout.adapter = contentAdapter

        viewModel.data.observe(viewLifecycleOwner) { recipes ->
            val recipe = recipes.firstOrNull { it.id == arguments?.getLong("id") } ?: Recipe()
            recipeViewHolder.bind(recipe)
            contentAdapter.submitList(recipe.content)
        }

        return binding.root
    }
}