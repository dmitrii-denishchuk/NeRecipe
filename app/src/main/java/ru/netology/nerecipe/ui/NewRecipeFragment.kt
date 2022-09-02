package ru.netology.nerecipe.ui

import android.R
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Selection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nerecipe.adapter.ContentAdapter
import ru.netology.nerecipe.clickListeners.ContentClickListeners
import ru.netology.nerecipe.databinding.FragmentNewRecipeBinding
import ru.netology.nerecipe.databinding.RecipeContentLayoutBinding
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
    lateinit var category: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bindingTitle = FragmentNewRecipeBinding.inflate(inflater, container, false)
        val bindingContent = RecipeContentLayoutBinding.inflate(inflater, container, false)
        val recycler = bindingTitle.editableContentRecipe
        val recipeViewModel: RecipeViewModel by viewModels(ownerProducer = ::requireParentFragment)

        var stepResource: String
        var contentResource: String

        fun savePicture(imageView: ImageView): String {
            val filename = UUID.randomUUID().toString() + ".jpg"
            requireContext().openFileOutput(filename, Context.MODE_PRIVATE).use {
                imageView.drawable.toBitmap().compress(Bitmap.CompressFormat.JPEG, 85, it)
            }
            return requireContext().getFileStreamPath(filename).absolutePath
        }

        val recipeImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            val path = it ?: return@registerForActivityResult
            bindingTitle.editablePictureRecipe.tag = path
            bindingTitle.editablePictureRecipe.setImageURI(path)
        }

        val contentImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            val path = it ?: return@registerForActivityResult
            view?.findViewById<ImageView>(ru.netology.nerecipe.R.id.editablePictureContent)?.tag =
                path
            view?.findViewById<ImageView>(ru.netology.nerecipe.R.id.editablePictureContent)
                ?.setImageURI(path)
        }

        val contentAdapter = ContentAdapter(object : ContentClickListeners {
            override fun clickedAddPicture() {
                contentImage.launch(arrayOf("image/*"))
            }

            override fun clickedRemoveOrAdd(content: Content) {
                stepResource =
                    view?.findViewById<EditText>(ru.netology.nerecipe.R.id.editableStepContent)?.text.toString()
                contentResource =
                    view?.findViewById<EditText>(ru.netology.nerecipe.R.id.editableTextContent)?.text.toString()
                val pictureResource =
                    view?.findViewById<ImageView>(ru.netology.nerecipe.R.id.editablePictureContent)

                if (content.step != "") {
//                    recipeViewModel.clickedSaveCurrentContent(
//                        content.copy(
//                            step = stepResource,
//                            content = contentResource,
//                            picture =
//                            if (pictureResource?.tag == null) ""
//                            else savePicture(pictureResource)
//                        )
//                    )

                    recipeViewModel.clickedRemoveContent(content.id)

                    recipeViewModel.clickedSaveCurrentRecipe(
                        Recipe(
                            title = bindingTitle.editableTitleRecipe.text.toString(),
                            category = category,
                            picture =
                            if (bindingTitle.editablePictureRecipe.tag == null) ""
                            else savePicture(bindingTitle.editablePictureRecipe),
                        )
                    )
                } else {
                    while (stepResource.isBlank() || contentResource.isBlank()) {
                        Toast.makeText(
                            context,
                            "Заполните этот шаг прежде чем создавать новый",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    recipeViewModel.clickedSaveCurrentContent(
                        content.copy(
                            step = stepResource,
                            content = contentResource,
                            picture =
                            if (pictureResource?.tag == null) ""
                            else savePicture(pictureResource)
                        )
                    )

                    recipeViewModel.clickedSaveCurrentRecipe(
                        Recipe(
                            title = bindingTitle.editableTitleRecipe.text.toString(),
                            category = category,
                            picture =
                            if (bindingTitle.editablePictureRecipe.tag == null) ""
                            else savePicture(bindingTitle.editablePictureRecipe),
                        )
                    )
                }
            }
        },
            object : OnStartDragListener {
                override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
                    itemTouchHelper!!.startDrag((viewHolder!!))
                }
            }
        )

        arguments?.textArg.let(bindingContent.editableStepContent::setText)

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
                    category = item
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        spinner.onItemSelectedListener = itemSelectedListener

        bindingTitle.addRecipePicture.setOnClickListener {
            recipeImage.launch(arrayOf("image/*"))
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (recipeViewModel.currentRecipe.value?.let { recipeViewModel.data.value?.contains(it) } == true) {
                recipeViewModel.currentRecipe.value = null
                bindingContent.editableStepContent.setText("")
            } else recipeViewModel.currentRecipe.value =
                Recipe(content = recipeViewModel.currentContentList)
            bindingTitle.editableContentRecipe
            bindingContent.editableStepContent.hideKeyboard()
            findNavController().navigateUp()
            recipeViewModel.currentContentList.clear()
        }

        bindingTitle.okButton.setOnClickListener {
            stepResource =
                view?.findViewById<EditText>(ru.netology.nerecipe.R.id.editableStepContent)?.text.toString()
            contentResource =
                view?.findViewById<EditText>(ru.netology.nerecipe.R.id.editableTextContent)?.text.toString()
            val pictureResource =
                view?.findViewById<ImageView>(ru.netology.nerecipe.R.id.editablePictureContent)

            while (bindingTitle.editableTitleRecipe.text.isNullOrBlank()) {
                Toast.makeText(context, "Заполни заголовок", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            while (stepResource == "" || contentResource == "") {
                Toast.makeText(context, "Создай хотя бы один шаг", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (stepResource == "" || contentResource == "") {
                recipeViewModel.clickedRemoveContent(-1)
                recipeViewModel.clickedSaveCurrentRecipe(
                    Recipe(
                        title = bindingTitle.editableTitleRecipe.text.toString(),
                        category = category,
                        picture =
                        if (bindingTitle.editablePictureRecipe.tag == null) ""
                        else savePicture(bindingTitle.editablePictureRecipe),
                    )
                )
            } else {
                recipeViewModel.clickedSaveCurrentContent(
                    Content(
                        id = 0,
                        step = stepResource,
                        content = contentResource,
                        picture =
                        if (pictureResource?.tag == null) ""
                        else savePicture(pictureResource)
                    )
                )

                recipeViewModel.clickedRemoveContent(-1)

                recipeViewModel.clickedSaveCurrentRecipe(
                    Recipe(
                        title = bindingTitle.editableTitleRecipe.text.toString(),
                        category = category,
                        picture =
                        if (bindingTitle.editablePictureRecipe.tag == null) ""
                        else savePicture(bindingTitle.editablePictureRecipe),
                    )
                )
            }
            recipeViewModel.currentRecipe.value?.let { recipe -> recipeViewModel.clickedSave(recipe) }
            bindingTitle.editableTitleRecipe.hideKeyboard()
            findNavController().navigateUp()
        }

        bindingTitle.cancelButton.setOnClickListener {
            recipeViewModel.currentRecipe.value = null
            bindingTitle.editableTitleRecipe.hideKeyboard()
            findNavController().navigateUp()
        }

        recycler.adapter = contentAdapter

        recipeViewModel.currentRecipe.observe(viewLifecycleOwner) {
            recycler.layoutManager?.smoothScrollToPosition(
                recycler,
                null,
                recipeViewModel.currentContentList.size
            )
            contentAdapter.submitList(it?.content)
            with(bindingTitle) {
                with(editableTitleRecipe) {
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