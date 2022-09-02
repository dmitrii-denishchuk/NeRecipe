package ru.netology.nerecipe.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nerecipe.R
import ru.netology.nerecipe.clickListeners.ContentClickListeners
import ru.netology.nerecipe.databinding.RecipeContentLayoutBinding
import ru.netology.nerecipe.dragAndDropHelpers.ItemTouchHelperAdapter
import ru.netology.nerecipe.dragAndDropHelpers.OnStartDragListener
import ru.netology.nerecipe.recipe.Content
import java.util.*

const val EDITABLE_VIEWHOLDER = 2131296445

class ContentAdapter(
    private val clickListener: ContentClickListeners?,
    private var listener: OnStartDragListener
) : ListAdapter<Content, RecyclerView.ViewHolder>(DiffCallback), ItemTouchHelperAdapter {

    constructor(listener: OnStartDragListener) : this(null, listener)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (parent.id) {
            EDITABLE_VIEWHOLDER -> EditableViewHolder(
                RecipeContentLayoutBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), clickListener
            )
            else -> ViewHolder(
                RecipeContentLayoutBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                holder.bind(getItem(position))
                holder.itemView.setOnLongClickListener {
                    listener.onStartDrag(holder)
                    false
                }
            }
            is EditableViewHolder -> {
                holder.bind(getItem(position))
                holder.itemView.setOnLongClickListener {
                    listener.onStartDrag(holder)
                    false
                }
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Content>() {
        override fun areItemsTheSame(oldItem: Content, newItem: Content) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Content, newItem: Content) = oldItem == newItem
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(currentList, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
        currentList.removeAt(position)
        notifyItemRemoved(position)
    }

    class EditableViewHolder(
        private val binding: RecipeContentLayoutBinding,
        clickListener: ContentClickListeners?
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var content: Content

        init {
            binding.addContentPicture.setOnClickListener {
                clickListener?.clickedAddPicture()
            }

            binding.removeOrAddContent.setOnClickListener {
                clickListener?.clickedRemoveOrAdd(content)
            }
        }

        fun bind(content: Content) {
            this.content = content
            with(binding) {
                editableStepContent.setText(content.step)
                editableTextContent.setText(content.content)
                editablePictureContent.setImageBitmap(BitmapFactory.decodeFile(content.picture))
                if (editableStepContent.text.isEmpty())
                    removeOrAddContent.setIconResource(R.drawable.ic_baseline_add_24)
                else
                    removeOrAddContent.setIconResource(R.drawable.ic_baseline_close_24)
            }
        }
    }

    class ViewHolder(
        private val binding: RecipeContentLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var content: Content

        fun bind(content: Content) {
            this.content = content
            with(binding) {
                with(editableStepContent) {
                    setText(content.step)
                    isEnabled = false
                }
                with(editableTextContent) {
                    setText(content.content)
                    isEnabled = false
                }
                if (content.picture.isEmpty()) editablePictureContent.visibility = View.GONE
                else editablePictureContent.setImageBitmap(BitmapFactory.decodeFile(content.picture))
                removeOrAddContent.visibility = View.GONE
                addContentPicture.visibility = View.GONE
            }
        }
    }
}