package ru.netology.nerecipe.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
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
import ru.netology.nerecipe.R
import ru.netology.nerecipe.adapter.RecipeAdapter
import ru.netology.nerecipe.clickListeners.RecipeClickListeners
import ru.netology.nerecipe.databinding.FragmentFeedRecipeBinding
import ru.netology.nerecipe.dragAndDropHelpers.MyItemTouchHelperCallback
import ru.netology.nerecipe.dragAndDropHelpers.OnStartDragListener
import ru.netology.nerecipe.recipe.Content
import ru.netology.nerecipe.recipe.Recipe
import ru.netology.nerecipe.viewModel.RecipeViewModel

class FeedFragment : Fragment() {

    var itemTouchHelper: ItemTouchHelper? = null

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

        },
            object : OnStartDragListener {
                override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
                    itemTouchHelper!!.startDrag((viewHolder!!))
                }
            })

        val callback = MyItemTouchHelperCallback(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper!!.attachToRecyclerView(binding.recipeRecyclerView)

        // ADD RECIPE BUTTON
        binding.addRecipeButton.setOnClickListener {
            viewModel.currentRecipe.value = Recipe()
            findNavController().navigate(R.id.action_navigation_book_to_new_recipe_fragment)
        }

        // SHOW CATEGORIES
        fun showDialog() {
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.recipe_filters_layout)
            dialog.show()
            with(dialog) {
                window?.attributes?.windowAnimations = R.style.dialog_animation
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window?.setGravity(Gravity.END)
                window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        binding.recipeRecyclerView.adapter = adapter

        // OBSERVER
        viewModel.data.observe(viewLifecycleOwner) { adapter.submitList(it) }

//        fun searchRecipe() {
//            val search = view?.findViewById<SearchView>(R.id.search)
//            search?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//
//                override fun onQueryTextSubmit(query: String?): Boolean {
//                    viewModel.data.value?.filter { it.title == query }
//                    return false
//                }
//
//                override fun onQueryTextChange(newText: String?): Boolean {
//                    viewModel.data.value?.filter { it.title == newText }
//                    return false
//                }
//            })
//        }

        // APP MENU
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.tools, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.search -> {
                        val search = view?.findViewById<SearchView>(R.id.search)
                        search?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                            override fun onQueryTextSubmit(query: String?): Boolean {
                                viewModel.data.value?.filter { it.title == query }
                                return false
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                viewModel.data.value?.filter { it.title == newText }
                                return false
                            }
                        })
                        true
                    }

                    R.id.filter -> {
                        showDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        generateItem()

        return binding.root
    }

    private fun generateItem() {
        val data: MutableList<Recipe> = ArrayList()
        data.addAll((listOf()))
    }
}