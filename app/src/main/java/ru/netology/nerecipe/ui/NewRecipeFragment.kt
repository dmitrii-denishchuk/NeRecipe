package ru.netology.nerecipe.ui

import android.R
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
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
import java.util.*

class NewRecipeFragment : Fragment() {

    var itemTouchHelper: ItemTouchHelper? = null

    lateinit var stepName: EditText
    lateinit var content: EditText
    lateinit var picture: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bindingTitle = FragmentNewRecipeBinding.inflate(inflater, container, false)
        val bindingContent = RecipeContentEditLayoutBinding.inflate(inflater, container, false)

        val stepp = ""
        stepName = bindingContent.stepName
        content = bindingContent.viewContent
        picture = bindingContent.background

        val recipeViewModel: RecipeViewModel by viewModels(ownerProducer = ::requireParentFragment)
        val contentAdapter = ContentAdapter(object : ContentClickListeners {
            override fun clickedRemove(content: Content) {
            }

            override fun clickedEdit(content: Content) {
            }
        },
            object : OnStartDragListener {
                override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
                    itemTouchHelper!!.startDrag((viewHolder!!))
                }
            }
        )

        arguments?.textArg.let(bindingContent.stepName::setText)

        val spinner = bindingTitle.spinner
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
                    bindingTitle.category.text = item
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        spinner.onItemSelectedListener = itemSelectedListener

        fun savePicture(imageView: ImageView): String {
            val filename = UUID.randomUUID().toString() + ".jpg"
            requireContext().openFileOutput(filename, Context.MODE_PRIVATE).use {
                imageView.drawable.toBitmap().compress(Bitmap.CompressFormat.JPEG, 85, it)
            }
            return requireContext().getFileStreamPath(filename).absolutePath
        }

        val image = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            val path = it ?: return@registerForActivityResult
            bindingTitle.background.tag = path
            bindingTitle.background.setImageURI(path)
        }

        bindingTitle.background.setOnClickListener {
            image.launch(arrayOf("image/*"))
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (recipeViewModel.currentRecipe.value?.let { recipeViewModel.data.value?.contains(it) } == true) {
                recipeViewModel.currentRecipe.value = null
                bindingContent.stepName.setText("")
            } else recipeViewModel.currentRecipe.value =
                Recipe(content = recipeViewModel.currentContentList)
            bindingTitle.recipeContentEditLayout
            bindingContent.stepName.hideKeyboard()
            findNavController().navigateUp()
        }

        bindingTitle.okButton.setOnClickListener {
            while (bindingTitle.titleRecipe.text.isNullOrBlank()) {
                Toast.makeText(context, "Заполни заголовок", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            recipeViewModel.clickedSave(
                Recipe(
                    content = recipeViewModel.currentContentList,
                    title = bindingTitle.titleRecipe.text.toString(),
                    category = bindingTitle.category.text.toString(),
                    picture =
                    if (bindingTitle.background.tag == null) ""
                    else savePicture(bindingTitle.background)
                )
            )
            bindingTitle.titleRecipe.hideKeyboard()
            findNavController().navigateUp()
        }

        bindingTitle.cancelButton.setOnClickListener {
            recipeViewModel.currentRecipe.value = null
            bindingTitle.titleRecipe.hideKeyboard()
            findNavController().navigateUp()
        }

        bindingTitle.addButton.setOnClickListener {
//            while (stepName.text.isNullOrBlank() && content.text.isNullOrBlank()) {
//                Toast.makeText(context, "Заполните этот шаг прежде чем создавать новый", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
            recipeViewModel.clickedSaveContent(
                Recipe(
                    title = bindingTitle.titleRecipe.text.toString(),
                    category = bindingTitle.category.text.toString(),
                    picture =
                    if (bindingTitle.background.tag == null) ""
                    else savePicture(bindingTitle.background)
                ), Content(
                    id = -1,
                    step = stepName.text.toString(),
                    content = content.text.toString(),
                    picture =
                    if (picture.tag == null) ""
                    else picture.tag.toString()
                )
            )
        }

        bindingTitle.recipeContentEditLayout.adapter = contentAdapter

        recipeViewModel.currentRecipe.observe(viewLifecycleOwner) {
            contentAdapter.submitList(it?.content)
            with(bindingTitle) {
                background.setImageURI(recipeViewModel.currentRecipe.value?.picture?.toUri())
                category.text = recipeViewModel.currentRecipe.value?.category
                with(titleRecipe) {
                    setText(recipeViewModel.currentRecipe.value?.title)
                    requestFocus()
                    showKeyboard()
                    Selection.setSelection(editableText, editableText.length)
                }
            }
        }

        return bindingTitle.root
    }

    companion object {
        var Bundle.textArg: String? by StringArg
    }
}