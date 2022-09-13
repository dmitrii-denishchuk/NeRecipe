package ru.netology.nerecipe.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nerecipe.R
import ru.netology.nerecipe.adapter.ContentAdapter
import ru.netology.nerecipe.adapter.RecipeAdapter
import ru.netology.nerecipe.clickListeners.RecipeClickListeners
import ru.netology.nerecipe.databinding.FragmentViewRecipeBinding
import ru.netology.nerecipe.dragAndDropHelpers.OnStartDragListener
import ru.netology.nerecipe.recipe.Recipe
import ru.netology.nerecipe.viewModel.RecipeViewModel

class ViewRecipeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentViewRecipeBinding.inflate(
            inflater,
            container,
            false
        )

        val recycler = binding.recipeContentLayout
        val viewModel: RecipeViewModel by viewModels(ownerProducer = ::requireParentFragment)

        binding.root.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        recycler.visibility = View.VISIBLE

        val contentAdapter = ContentAdapter(object : OnStartDragListener {
            override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
            }
        })

        val recipeViewHolder =
            RecipeAdapter.ViewHolder(binding, object : RecipeClickListeners {
                override fun clickedFavorite(recipe: Recipe) {
                    viewModel.clickedFavorite(recipe)
                }

                override fun clickedRemove(recipe: Recipe) {
                    viewModel.clickedRemove(recipe)
                    findNavController().navigateUp()
                }

                override fun clickedNewOrEdit(recipe: Recipe) {
                    viewModel.clickedNewOrEdit(recipe)
                    findNavController().navigate(
                        R.id.action_recipe_view_fragment_to_edit_recipe_fragment)
                }

                override fun clickedRecipe(recipe: Recipe) {
                }
            })

        recycler.adapter = contentAdapter

        viewModel.data.observe(viewLifecycleOwner) { recipes ->
            val recipe = recipes.firstOrNull { it.id == arguments?.getLong("id") } ?: return@observe
            recipeViewHolder.bind(recipe)
            contentAdapter.submitList(recipe.steps)
        }

        return binding.root
    }
}