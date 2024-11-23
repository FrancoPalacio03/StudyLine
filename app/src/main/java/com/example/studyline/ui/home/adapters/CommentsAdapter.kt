package com.example.studyline.ui.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studyline.data.model.Comment
import com.example.studyline.data.model.Publication
import com.example.studyline.databinding.ItemCommentBinding
import java.text.SimpleDateFormat
import java.util.Locale


class CommentsAdapter : ListAdapter<Comment, CommentsAdapter.CommentViewHolder>(DiffCallback()) {

    class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.commentDescription.text = comment.content

            val formattedDate = formatTimestampToDate(comment.date)
            binding.commentDate.text = formattedDate

            // Mostrar el ID del usuario (puedes reemplazarlo con el nombre si lo tienes)
            binding.commentUserId.text = comment.userId
        }

        private fun formatTimestampToDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(timestamp)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun addComment(comment: Comment) {
        val updatedList = currentList.toMutableList()
        updatedList.add(comment)
        submitList(updatedList)
    }

    class DiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.commentId == newItem.commentId
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }
}
