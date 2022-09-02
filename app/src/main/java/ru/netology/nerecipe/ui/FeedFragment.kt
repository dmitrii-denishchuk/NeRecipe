package ru.netology.nerecipe.ui

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.netology.nerecipe.R
import ru.netology.nerecipe.adapter.RecipeAdapter
import ru.netology.nerecipe.clickListeners.RecipeClickListeners
import ru.netology.nerecipe.databinding.FragmentFeedRecipeBinding
import ru.netology.nerecipe.dragAndDropHelpers.MyItemTouchHelperCallback
import ru.netology.nerecipe.dragAndDropHelpers.OnStartDragListener
import ru.netology.nerecipe.recipe.Recipe
import ru.netology.nerecipe.viewModel.RecipeViewModel

class FeedFragment : Fragment() {

    lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder

    // START
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedRecipeBinding.inflate(
            inflater,
            container,
            false
        )
        val recycler = binding.viewRecipeRecycler

        materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())

        // VIEW MODEL
        val viewModel: RecipeViewModel by viewModels(ownerProducer = ::requireParentFragment)

        // ADAPTER
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
                    R.id.action_navigation_book_to_new_recipe_fragment,
                    bundleOf("content" to recipe.content)
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

        val callback: ItemTouchHelper.Callback = MyItemTouchHelperCallback(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recycler)

        // ADD RECIPE BUTTON
        binding.addRecipeButton.setOnClickListener {
            viewModel.currentRecipe.value = Recipe()
            findNavController().navigate(R.id.action_navigation_book_to_new_recipe_fragment)
        }

//        view?.findViewById<MaterialButton>(R.id.navigation_favorite)?.setOnClickListener {
//            viewModel.data.observe(viewLifecycleOwner) {
//                recycler.layoutManager?.smoothScrollToPosition(
//                    recycler,
//                    null,
//                    viewModel.currentContentList.size
//                )
//                if (it != null) binding.emptyBackground.visibility = View.GONE
//                adapter.submitList(
//                    if (parentFragment?.id == 0) it
//                    else it)
//            }
//        }
//
//        view?.findViewById<MaterialButton>(R.id.navigation_book)?.setOnClickListener {
//            viewModel.data.observe(viewLifecycleOwner) {
//                recycler.layoutManager?.smoothScrollToPosition(
//                    recycler,
//                    null,
//                    viewModel.currentContentList.size
//                )
//                if (it != null) binding.emptyBackground.visibility = View.GONE
//                adapter.submitList(
//                    if (parentFragment?.id == 0) it
//                    else it.filter { favoriteRecipes -> favoriteRecipes.isFavorite })
//            }
//        }

//        View.OnClickListener { view ->
//            viewModel.data.observe(viewLifecycleOwner) {
//                adapter.submitList(
//                    when (view.id) {
//                        R.id.navigation_book -> {
//                            it
//                        }
//                        R.id.navigation_favorite -> {
//                            it.filter { favoriteRecipes -> favoriteRecipes.isFavorite }
//                        }
//                        else -> return@observe
//                    }
//                )
//                recycler.layoutManager?.smoothScrollToPosition(
//                    recycler,
//                    null,
//                    viewModel.currentContentList.size
//                )
//                if (it != null) binding.emptyBackground.visibility = View.GONE
//            }
//        }

        // OBSERVER
        viewModel.data.observe(viewLifecycleOwner) {
            val fav = view?.findViewById<MaterialButton>(R.id.navigation_favorite)
            val boo = view?.findViewById<MaterialButton>(R.id.navigation_book)
            recycler.layoutManager?.smoothScrollToPosition(
                recycler,
                null,
                viewModel.currentContentList.size
            )
            if (it != null) binding.emptyBackground.visibility = View.GONE
            adapter.submitList(
                when (boo?.isChecked == true) {
                    (boo?.isChecked == true) -> {
                        it
                    }
                    (fav?.isChecked == true) -> {
                        it.filter { favoriteRecipes -> favoriteRecipes.isFavorite }
                    }
                    else -> it
                })

        }

    // APP MENU
    val menuHost: MenuHost = requireActivity()
    menuHost.addMenuProvider(
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
                            viewModel.data.observe(viewLifecycleOwner) {
                                adapter.submitList(it.filter { recipe ->
                                    recipe.title.contains(query.toString())
                                })
                            }
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            viewModel.data.observe(viewLifecycleOwner) {
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
                        else defaultCheckboxes.also {
                            viewModel.filteredRecipes.value = viewModel.data.value
                        }

                    materialAlertDialogBuilder
                        .setTitle("Категории")
                        .setMultiChoiceItems(
                            foodCategory,
                            checkedCheckboxes
                        ) { _, which, isChecked ->
                            val falseCheck = checkedCheckboxes.count { !it }
                            val category =
                                viewModel.data.value?.filter { it.category == foodCategory[which] }

                            if (isChecked) category?.let { viewModel.plusRecipe(it) }
                            else {
                                if (falseCheck == checkedCheckboxes.size) {
                                    checkedCheckboxes[which] = true
                                    Toast.makeText(
                                        context,
                                        "Должен быть выбран хотя бы один параметр",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else
                                    category?.let { viewModel.minusRecipe(it) }
                            }

                            viewModel.filteredRecipes.observe(viewLifecycleOwner) {
                                adapter.submitList(it)
                            }
                        }

                        .setPositiveButton("Reset") { _, _ ->
                            viewModel.checkboxesState = defaultCheckboxes
                            viewModel.filteredRecipes.value = viewModel.data.value
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
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)

//        generateItem()

    return binding.root
}

//    private fun generateItem() {
//        val data: MutableList<Recipe> = ArrayList()
//        data.addAll((listOf()))
//    }
}