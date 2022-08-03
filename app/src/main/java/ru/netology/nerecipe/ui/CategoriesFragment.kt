package ru.netology.nerecipe.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.netology.nerecipe.databinding.RecipeFiltersLayoutBinding
import ru.netology.nerecipe.viewModel.RecipeViewModel

class CategoriesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = RecipeFiltersLayoutBinding.inflate(
            inflater,
            container,
            false
        )

        val viewModel: RecipeViewModel by viewModels(ownerProducer = ::requireParentFragment)

        val data = viewModel.data.value

        binding.america.setOnClickListener() { view ->
            view as CheckBox
            if (view.isChecked) data?.filter { it.category == binding.america.text }
            else viewModel.data
        }

        binding.asia.setOnCheckedChangeListener { buttonView, isChecked ->
            data?.filter { it.category == binding.asia.text }
        }

        binding.east.setOnClickListener() { view ->
            view as CheckBox
            if (view.isChecked) data?.filter { it.category == binding.east.text }
            else viewModel.data
        }

        binding.europa.setOnClickListener() { view ->
            view as CheckBox
            if (view.isChecked) data?.filter { it.category == binding.europa.text }
            else viewModel.data
        }

        binding.panasia.setOnClickListener() { view ->
            view as CheckBox
            if (view.isChecked) data?.filter { it.category == binding.panasia.text }
            else viewModel.data
        }

        binding.russia.setOnClickListener() { view ->
            view as CheckBox
            if (view.isChecked) data?.filter { it.category == binding.russia.text }
            else viewModel.data
        }

        binding.mediterran.setOnClickListener() { view ->
            view as CheckBox
            if (view.isChecked) data?.filter { it.category == binding.mediterran.text }
            else viewModel.data
        }

//        val checkedCategories = onCheckboxClicked(requireView() as CheckBox)
//
//        viewModel.data.observe(viewLifecycleOwner) { checkedCategories }


//        binding.apply.setOnClickListener {
//            while (binding.content.text.isNullOrBlank()) {
//                Snackbar.make(binding.root, "Пустой текст", 1000).show()
//                return@setOnClickListener
//            }
//            viewModel.clickedSave(
//                Recipe(
//                    content = binding.content.text.toString(),
//                    title = binding.titleRecipe.text.toString(),
//                    category = binding.category.text.toString(),
//                    picture = if (binding.background.tag == null) "" else binding.background.tag.toString()
//                )
//            )
//            binding.content.hideKeyboard()
//            findNavController().navigateUp()
//        }

        return binding.root
    }
}