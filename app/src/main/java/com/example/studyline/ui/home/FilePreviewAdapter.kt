package com.example.studyline.ui.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studyline.R

class FilePreviewAdapter(
    private val files: MutableList<Uri>,
    private val onRemoveClick: (Uri) -> Unit
) : RecyclerView.Adapter<FilePreviewAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.file_thumbnail)
        val removeButton: ImageView = view.findViewById(R.id.remove_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]
        Glide.with(holder.itemView.context)
            .load(file)
            .into(holder.thumbnail)

        holder.removeButton.setOnClickListener {
            onRemoveClick(file)
        }
    }

    override fun getItemCount(): Int = files.size
}
