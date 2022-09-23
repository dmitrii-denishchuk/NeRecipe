package ru.netology.nerecipe.adapter

import android.graphics.BitmapFactory
import android.text.Selection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nerecipe.R
import ru.netology.nerecipe.clickListeners.ContentClickListeners
import ru.netology.nerecipe.databinding.RecipeStepLayoutBinding
import ru.netology.nerecipe.dragAndDropHelpers.ItemTouchHelperAdapter
import ru.netology.nerecipe.dragAndDropHelpers.OnStartDragListener
import ru.netology.nerecipe.recipe.Step
import java.util.*

class ContentAdapter(
    private val clickListener: ContentClickListeners?,
    private var listener: OnStartDragListener
) : ListAdapter<Step, RecyclerView.ViewHolder>(DiffCallback), ItemTouchHelperAdapter {

    constructor(listener: OnStartDragListener) : this(null, listener)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (parent.id) {
            R.id.editableStepsRecipe -> EditableViewHolder(
                RecipeStepLayoutBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), clickListener
            )
            else -> ViewHolder(
                RecipeStepLayoutBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EditableViewHolder -> {
                holder.bind(getItem(position))
                holder.itemView.setOnLongClickListener {
                    listener.onStartDrag(holder)
                    false
                }
            }
            is ViewHolder -> {
                holder.bind(getItem(position))
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Step>() {
        override fun areItemsTheSame(oldItem: Step, newItem: Step) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Step, newItem: Step) = oldItem == newItem
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val updated = currentList.toMutableList()
        Collections.swap(updated, fromPosition, toPosition)
        submitList(updated)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
        val updated = currentList.toMutableList()
        updated.removeAt(position)
        notifyItemRemoved(position)
        submitList(updated)
    }

    class EditableViewHolder(
        private val binding: RecipeStepLayoutBinding,
        clickListener: ContentClickListeners?
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var step: Step
        private var timer = Timer()

        init {
            binding.editableTitleStep.addTextChangedListener {
                timer.cancel()
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        clickListener?.clickedEnterText(step.copy(title = it.toString()))
                    }
                }, 300)
            }

            binding.editableContentStep.addTextChangedListener {
                timer.cancel()
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        clickListener?.clickedEnterText(step.copy(content = it.toString()))
                    }
                }, 300)
            }

            binding.addPictureStep.setOnClickListener {
                clickListener?.clickedAddPicture(step)
            }

            binding.addOrRemoveStep.setOnClickListener {
                clickListener?.clickedAddOrRemoveStep(step)
            }
        }

        fun bind(step: Step) {
            this.step = step
            with(binding) {
                with(binding.editableTitleStep) {
                    setText(step.title)
                    Selection.setSelection(editableText, editableText.length)
                }
                with(binding.editableContentStep) {
                    setText(step.content)
                    Selection.setSelection(editableText, editableText.length)
                }
                editablePictureStep.setImageBitmap(BitmapFactory.decodeFile(step.picture))
                with(binding.addOrRemoveStep) {
                    if (binding.editableTitleStep.text.isEmpty() || binding.editableContentStep.text.isEmpty() && step.id == 1000) {
                        visibility = View.INVISIBLE
                    } else if ((binding.editableTitleStep.text.isNotEmpty() || binding.editableContentStep.text.isNotEmpty()) && (step.id == 1000)) {
                        visibility = View.VISIBLE
                        setIconResource(R.drawable.ic_baseline_add_24)
                    } else if (binding.editableTitleStep.text.isNotEmpty() || binding.editableContentStep.text.isNotEmpty() && step.id != 1000) {
                        setIconResource(R.drawable.ic_baseline_close_24)
                        visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    class ViewHolder(
        private val binding: RecipeStepLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var step: Step

        fun bind(step: Step) {
            this.step = step
            with(binding) {
                with(editableTitleStep) {
                    setText(step.title)
                    isEnabled = false
                }
                with(editableContentStep) {
                    setText(step.content)
                    isEnabled = false
                }
                if (step.picture == "") editablePictureStep.visibility = View.GONE
                else editablePictureStep.setImageBitmap(BitmapFactory.decodeFile(step.picture))
                addOrRemoveStep.visibility = View.GONE
                addPictureStep.visibility = View.GONE
            }
        }
    }
}