package ru.netology.nerecipe.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nerecipe.R
import ru.netology.nerecipe.adapter.RecipeAdapter
import ru.netology.nerecipe.clickListeners.RecipeClickListeners
import ru.netology.nerecipe.databinding.FragmentFeedRecipeBinding
import ru.netology.nerecipe.dragAndDropHelpers.OnStartDragListener
import ru.netology.nerecipe.recipe.Recipe
import ru.netology.nerecipe.viewModel.RecipeViewModel

class FavoriteFragment : Fragment() {

    var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentFeedRecipeBinding.inflate(inflater, container, false)
        val viewModel: RecipeViewModel by viewModels(ownerProducer = ::requireParentFragment)

        val adapter = RecipeAdapter(object : RecipeClickListeners {
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
                    R.id.action_navigation_favorite_to_new_recipe_fragment,
                    bundleOf("content" to recipe.content)
                )
            }

            override fun clickedRecipe(recipe: Recipe) {
                findNavController().navigate(
                    R.id.action_navigation_favorite_to_recipe_view_fragment,
                    bundleOf("id" to  recipe.id)
                )
            }
        },
            object : OnStartDragListener {
                override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
                    itemTouchHelper!!.startDrag((viewHolder!!))
                }
            })

        binding.viewRecipeRecycler.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) { recipes ->
            adapter.submitList(recipes.filter { it.isFavorite })
        }

        return binding.root
    }
}