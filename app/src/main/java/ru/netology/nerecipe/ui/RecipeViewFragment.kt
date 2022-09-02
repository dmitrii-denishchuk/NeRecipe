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
import ru.netology.nerecipe.adapter.ContentAdapter
import ru.netology.nerecipe.adapter.RecipeAdapter
import ru.netology.nerecipe.clickListeners.RecipeClickListeners
import ru.netology.nerecipe.databinding.FragmentRecipeViewBinding
import ru.netology.nerecipe.dragAndDropHelpers.MyItemTouchHelperCallback
import ru.netology.nerecipe.dragAndDropHelpers.OnStartDragListener
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

        val recycler = binding.recipeContentLayout
        binding.recipeContentLayout.visibility = View.VISIBLE

        val viewModel: RecipeViewModel by viewModels(ownerProducer = ::requireParentFragment)

        val contentAdapter = ContentAdapter(object : OnStartDragListener {
            override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
                itemTouchHelper!!.startDrag((viewHolder!!))
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

                override fun clickedEdit(recipe: Recipe) {
                    viewModel.clickedEdit(recipe)
                    findNavController().navigate(
                        R.id.action_recipe_view_fragment_to_new_recipe_fragment,
                        bundleOf("content" to recipe.content)
                    )
                }

                override fun clickedRecipe(recipe: Recipe) {
                }
            })

        recycler.adapter = contentAdapter

        val callback: ItemTouchHelper.Callback = MyItemTouchHelperCallback(contentAdapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper!!.attachToRecyclerView(recycler)

        viewModel.data.observe(viewLifecycleOwner) { recipes ->
            val recipe = recipes.firstOrNull { it.id == arguments?.getLong("id") } ?: return@observe
            recipeViewHolder.bind(recipe)
            contentAdapter.submitList(recipe.content)
        }

        return binding.root
    }
}