package ru.netology.nerecipe.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Selection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import ru.netology.nerecipe.adapter.ContentAdapter
import ru.netology.nerecipe.clickListeners.ContentClickListeners
import ru.netology.nerecipe.databinding.FragmentNewOrEditRecipeBinding
import ru.netology.nerecipe.dragAndDropHelpers.OnStartDragListener
import ru.netology.nerecipe.dragAndDropHelpers.VerticalItemTouchHelperCallback
import ru.netology.nerecipe.recipe.Recipe
import ru.netology.nerecipe.recipe.Step
import ru.netology.nerecipe.utils.hideKeyboard
import ru.netology.nerecipe.viewModel.RecipeViewModel
import java.util.*

class NewOrEditRecipeFragment : Fragment() {

    var title = ""
    val recipeViewModel: RecipeViewModel by viewModels(ownerProducer = ::requireParentFragment)
    lateinit var itemTouchHelper: ItemTouchHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewOrEditRecipeBinding.inflate(inflater, container, false)
        val recycler = binding.editableStepsRecipe

        val spinner = binding.spinner
        ArrayAdapter.createFromResource(
            requireContext(), ru.netology.nerecipe.R.array.food_category,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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
                    recipeViewModel.clickedSaveCurrentRecipe(
                        Recipe(-2L, category = parent.getItemAtPosition(position) as String)
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        spinner.onItemSelectedListener = itemSelectedListener

        val recipeImage =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) {
                val uri = it ?: return@registerForActivityResult
                binding.editablePictureRecipe.setImageURI(uri)
                recipeViewModel.clickedSaveCurrentRecipe(
                    Recipe(-3L, picture = savePicture(binding.editablePictureRecipe))
                )
            }

        val contentAdapter = ContentAdapter(object : ContentClickListeners {

            override fun clickedAddPicture(step: Step) {
                setPicture(step, recycler)
            }

            override fun clickedEnterText(step: Step) {
                recipeViewModel.clickedSaveCurrentStep(step)
            }

            override fun clickedAddOrRemoveStep(step: Step) {
                recipeViewModel.clickedAddOrRemoveStep(step)
            }
        },
            object : OnStartDragListener {
                override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
                    itemTouchHelper.startDrag((viewHolder!!))
                }
            }
        )

        recycler.adapter = contentAdapter

        val callback: ItemTouchHelper.Callback = VerticalItemTouchHelperCallback(contentAdapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recycler)

        val itemAnimator = recycler.itemAnimator
        if (itemAnimator is DefaultItemAnimator) {
            itemAnimator.supportsChangeAnimations = false
        }

        binding.editableTitleRecipe.addTextChangedListener(
            afterTextChanged = { title = it.toString() }
        )

        binding.addRecipePicture.setOnClickListener {
            recipeImage.launch(arrayOf("image/*"))
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
            recipeViewModel.currentStepsList.clear()
        }

        binding.okButton.setOnClickListener {
            while (binding.editableTitleRecipe.text.isNullOrBlank()) {
                Toast.makeText(context, "Заполни заголовок", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (recipeViewModel.currentStepsList.size < 2) {
                while (recipeViewModel.currentStepsList.last().title.isEmpty() ||
                    recipeViewModel.currentStepsList.last().content.isEmpty()
                ) {
                    Toast.makeText(context, "Создай хотя бы один шаг", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } else {
                while (recipeViewModel.currentStepsList
                        .filterNot { it.id == 1000 }
                        .any { it.title.isEmpty() || it.content.isEmpty() }
                ) {
                    Toast.makeText(
                        context,
                        "Рецепт не должен содержать шаг без названия или содержимого",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
            }

            recipeViewModel.clickedSaveCurrentRecipe(
                Recipe(-1L, title = title)
            )

            recipeViewModel.clickedAddToDb()
            binding.editableTitleRecipe.hideKeyboard()
            findNavController().navigateUp()
        }

        binding.cancelButton.setOnClickListener {
            recipeViewModel.currentRecipe.value = null
            recipeViewModel.currentStepsList.clear()
            binding.editableTitleRecipe.hideKeyboard()
            findNavController().navigateUp()
        }

        recipeViewModel.currentRecipe.observe(viewLifecycleOwner) {
            contentAdapter.submitList(it?.steps)
            with(binding) {
                with(editableTitleRecipe) {
                    setText(title.ifEmpty { recipeViewModel.currentRecipe.value?.title })
//                    requestFocus()
//                    showKeyboard()
                    Selection.setSelection(editableText, editableText.length)
                }
//                if (recipeViewModel.currentRecipe.value?.category?.isEmpty() == true) category
                spinner.setSelection(getIndex(spinner, it?.category ?: ""))
                editablePictureRecipe.setImageBitmap(BitmapFactory.decodeFile(it?.picture))
            }
        }

        return binding.root
    }

    private fun getIndex(spinner: Spinner, category: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(category, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }

    private fun savePicture(imageView: ImageView): String {
        val filename = UUID.randomUUID().toString() + ".jpg"
        requireContext().openFileOutput(filename, Context.MODE_PRIVATE).use {
            imageView.drawable.toBitmap().compress(Bitmap.CompressFormat.JPEG, 85, it)
        }
        return requireContext().getFileStreamPath(filename).absolutePath
    }

    private fun setPicture(step: Step, view: View) {
        view.findViewById<ImageView>(ru.netology.nerecipe.R.id.addPictureStep).visibility = View.GONE
        view.findViewById<ProgressBar>(ru.netology.nerecipe.R.id.progressBar).visibility = View.VISIBLE
        Glide.with(view)
            .load("https://loremflickr.com/480/480/food?random=${(1..1000).random()}")
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    view.findViewById<ImageView>(ru.netology.nerecipe.R.id.addPictureStep).visibility = View.VISIBLE
                    view.findViewById<ProgressBar>(ru.netology.nerecipe.R.id.progressBar).visibility = View.GONE
                    view.findViewById<ImageView>(ru.netology.nerecipe.R.id.editablePictureStep).setImageDrawable(resource)
                    recipeViewModel.clickedSaveCurrentStep(
                        step.copy(
                            picture = savePicture(view.findViewById(ru.netology.nerecipe.R.id.editablePictureStep))
                        )
                    )
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    view.findViewById<ImageView>(ru.netology.nerecipe.R.id.addPictureStep).visibility = View.VISIBLE
                    view.findViewById<ProgressBar>(ru.netology.nerecipe.R.id.progressBar).visibility = View.GONE
                    Toast.makeText(context, "Упс...", Toast.LENGTH_SHORT).show()
                }
            })
    }
}