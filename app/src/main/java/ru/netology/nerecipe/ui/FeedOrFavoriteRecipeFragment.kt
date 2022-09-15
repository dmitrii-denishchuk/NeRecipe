package ru.netology.nerecipe.ui

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.netology.nerecipe.R
import ru.netology.nerecipe.adapter.RecipeAdapter
import ru.netology.nerecipe.clickListeners.RecipeClickListeners
import ru.netology.nerecipe.databinding.FragmentFeedOrFavoriteRecipeBinding
import ru.netology.nerecipe.dragAndDropHelpers.OnStartDragListener
import ru.netology.nerecipe.dragAndDropHelpers.VerticalItemTouchHelperCallback
import ru.netology.nerecipe.recipe.Recipe
import ru.netology.nerecipe.viewModel.ViewModel

class FeedOrFavoriteRecipeFragment : Fragment() {

    lateinit var data: LiveData<List<Recipe>>
    lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder

    // START
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedOrFavoriteRecipeBinding.inflate(
            inflater,
            container,
            false
        )

        val recycler = binding.viewRecipeRecycler
        val viewModel: ViewModel by viewModels(ownerProducer = ::requireParentFragment)

        materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())

        container?.findNavController()?.addOnDestinationChangedListener { _,
                                                                          destination,
                                                                          _ ->
            data =
                if (destination.id == R.id.navigation_book) viewModel.data
                else viewModel.data.map { recipe -> recipe.filter { it.isFavorite } }
            viewModel.checkboxesState = booleanArrayOf()
        }

        // ADAPTER
        val adapter = RecipeAdapter(object : RecipeClickListeners {

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
                    R.id.action_navigation_book_to_edit_recipe_fragment
                )
            }

            override fun clickedRecipe(recipe: Recipe) {
                findNavController().navigate(
                    R.id.action_navigation_book_to_recipe_view_fragment,
                    bundleOf("id" to recipe.id)
                )
            }

        }, object : OnStartDragListener {
            override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
                itemTouchHelper.startDrag((viewHolder!!))
            }
        })

        recycler.adapter = adapter

        val callback: ItemTouchHelper.Callback = VerticalItemTouchHelperCallback(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recycler)

        // ADD RECIPE BUTTON
        binding.addRecipeButton.setOnClickListener {
            viewModel.clickedNewOrEdit(Recipe())
            findNavController().navigate(R.id.action_navigation_book_to_new_recipe_fragment)
        }

        // APP MENU
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.tools, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {

                        R.id.search -> {
                            val search = menuItem.actionView as SearchView
                            search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                                override fun onQueryTextSubmit(query: String?): Boolean {
                                    data.observe(viewLifecycleOwner) {
                                        adapter.submitList(it.filter { recipe ->
                                            recipe.title.contains(query.toString())
                                        })
                                    }
                                    return false
                                }

                                override fun onQueryTextChange(newText: String?): Boolean {
                                    data.observe(viewLifecycleOwner) {
                                        adapter.submitList(it.filter { recipe ->
                                            recipe.title.contains(newText.toString())
                                        })
                                    }
                                    return false
                                }
                            })
                            true
                        }

                        R.id.filter -> {
                            val foodCategory = resources.getStringArray(R.array.food_category)
                            val defaultCheckboxes =
                                booleanArrayOf(true, true, true, true, true, true, true)
                            val checkedCheckboxes =
                                if (viewModel.checkboxesState.isNotEmpty()) viewModel.checkboxesState
                                else defaultCheckboxes
                                    .also { viewModel.filteredRecipes.value = data.value }

                            viewModel.filteredRecipes.observe(viewLifecycleOwner) {
                                adapter.submitList(it)
                            }

                            materialAlertDialogBuilder
                                .setTitle("Категории")
                                .setMultiChoiceItems(
                                    foodCategory,
                                    checkedCheckboxes
                                ) { dialog, which, isChecked ->
                                    val falseCheck = checkedCheckboxes.count { !it }
                                    val category =
                                        data.value?.filter { it.category == foodCategory[which] }
                                    if (isChecked) category?.let { viewModel.plusRecipe(it) }
                                    else {
                                        if (falseCheck == checkedCheckboxes.size) {
                                            checkedCheckboxes[which] = true
                                            (dialog as AlertDialog).listView
                                                .setItemChecked(which, true)
                                            Toast.makeText(
                                                context,
                                                "Должен быть выбран хотя бы один параметр",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else
                                            category?.let { viewModel.minusRecipe(it) }
                                    }
                                }

                                .setPositiveButton("Reset") { _, _ ->
                                    viewModel.checkboxesState = defaultCheckboxes
                                    viewModel.filteredRecipes.value = data.value
                                }

                                .setOnCancelListener {
                                    viewModel.checkboxesState = checkedCheckboxes
                                }
                                .show()
                            true
                        }
                        else -> false
                    }
                }
            }, viewLifecycleOwner, Lifecycle.State.RESUMED
        )

        // OBSERVER
        data.observe(viewLifecycleOwner) { recipes ->
            adapter.submitList(recipes)
            recycler.layoutManager?.smoothScrollToPosition(
                recycler,
                null,
                viewModel.currentStepsList.size
            )
            if (recipes.isNotEmpty()) binding.emptyBackground.visibility = View.INVISIBLE
            else binding.emptyBackground.visibility = View.VISIBLE
        }

        return binding.root
    }
}