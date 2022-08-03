package ru.netology.nerecipe.ui

import android.R
import android.os.Bundle
import android.text.Selection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ru.netology.nerecipe.adapter.ContentAdapter
import ru.netology.nerecipe.clickListeners.ContentClickListeners
import ru.netology.nerecipe.databinding.FragmentNewRecipeBinding
import ru.netology.nerecipe.databinding.RecipeContentEditLayoutBinding
import ru.netology.nerecipe.dragAndDropHelpers.OnStartDragListener
import ru.netology.nerecipe.recipe.Content
import ru.netology.nerecipe.recipe.Recipe
import ru.netology.nerecipe.utils.StringArg
import ru.netology.nerecipe.utils.hideKeyboard
import ru.netology.nerecipe.utils.showKeyboard
import ru.netology.nerecipe.viewModel.RecipeViewModel

class NewRecipeFragment : Fragment() {

    var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bindingTitle = FragmentNewRecipeBinding.inflate(inflater, container, false)
        val bindingContent = RecipeContentEditLayoutBinding.inflate(inflater, container, false)

        val recipeViewModel: RecipeViewModel by viewModels(ownerProducer = ::requireParentFragment)

        val contentAdapter = ContentAdapter(object : ContentClickListeners {
            override fun clickedRemove(content: Content) {
                TODO("Not yet implemented")
            }

            override fun clickedEdit(content: Content) {
                TODO("Not yet implemented")
            }

        },
            object : OnStartDragListener {
                override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
                    itemTouchHelper!!.startDrag((viewHolder!!))
                }
            })

        arguments?.textArg.let(bindingContent.viewContent::setText)

        val spinner = bindingTitle.newRecipeLayout.spinner
        ArrayAdapter.createFromResource(
            requireContext(), ru.netology.nerecipe.R.array.food_category,
            R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        val itemSelectedListener: AdapterView.OnItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {

                    val item = parent.getItemAtPosition(position) as String
                    bindingTitle.newRecipeLayout.category.text = item
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        spinner.onItemSelectedListener = itemSelectedListener

        val image = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            val path = it
            bindingTitle.newRecipeLayout.background.tag = path
            bindingTitle.newRecipeLayout.background.setImageURI(path)
        }

        bindingTitle.newRecipeLayout.preview.setOnClickListener {
            image.launch(arrayOf("image/*"))
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (recipeViewModel.currentRecipe.value?.let { recipeViewModel.data.value?.contains(it) } == true) {
                recipeViewModel.currentRecipe.value = null
                bindingContent.viewContent.setText("")
            } else recipeViewModel.currentRecipe.value =
                Recipe(content = recipeViewModel.currentContentList)
            bindingContent.viewContent.hideKeyboard()
            findNavController().navigateUp()
        }

        bindingTitle.newRecipeLayout.okButton.setOnClickListener {
            while (bindingTitle.newRecipeLayout.titleRecipe.text.isNullOrBlank()) {
                Snackbar.make(bindingTitle.root, "Заполни заголовок", 1000).show()
                return@setOnClickListener
            }
            recipeViewModel.clickedSave(
                Recipe(
                    content = recipeViewModel.currentContentList,
                    title = bindingTitle.newRecipeLayout.titleRecipe.text.toString(),
                    category = bindingTitle.newRecipeLayout.category.text.toString(),
                    picture =
                    if (bindingTitle.newRecipeLayout.background.tag == null) ""
                    else bindingTitle.newRecipeLayout.background.tag.toString()
                )
            )
            bindingTitle.newRecipeLayout.titleRecipe.hideKeyboard()
            findNavController().navigateUp()
        }

        bindingTitle.newRecipeLayout.cancelButton.setOnClickListener {
            recipeViewModel.currentRecipe.value = null
            bindingTitle.newRecipeLayout.titleRecipe.hideKeyboard()
            findNavController().navigateUp()
        }

        bindingTitle.newRecipeLayout.addButton.setOnClickListener {
            while (bindingContent.viewContent.text.isNullOrBlank()) {
                Snackbar.make(bindingTitle.root, "Заполните этот шаг", 1000).show()
                return@setOnClickListener
            }
            recipeViewModel.clickedSaveContent(
                Content(
                    id = 0.0,
                    content = bindingContent.viewContent.text.toString(),
                    picture =
                    if (bindingContent.background.tag == null) ""
                    else bindingContent.background.tag.toString()
                )
            )
        }

        bindingTitle.newRecipeLayout.recipeContentEditLayout.adapter = contentAdapter

        recipeViewModel.currentRecipe.observe(viewLifecycleOwner) {
            contentAdapter.submitList(it?.content)
            with(bindingTitle.newRecipeLayout.titleRecipe) {
                setText(recipeViewModel.currentRecipe.value?.title)
                requestFocus()
                showKeyboard()
                Selection.setSelection(editableText, editableText.length)
            }
        }

        return bindingTitle.root
    }

    companion object {
        var Bundle.textArg: String? by StringArg
    }
}