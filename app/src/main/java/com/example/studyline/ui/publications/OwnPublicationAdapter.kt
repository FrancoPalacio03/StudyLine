package com.example.studyline.ui.publications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studyline.R
import com.example.studyline.data.model.Publication
import com.example.studyline.utils.MapsUtility
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class OwnPublicationAdapter(
        private var publications: MutableList<Publication>,
        private val onEditClick: (Publication) -> Unit,
        private val onDeleteClick: (Publication) -> Unit,) :
    RecyclerView.Adapter<OwnPublicationAdapter.PublicationViewHolder>() {

    // ViewHolder: Representa una tarjeta individual en la lista
    inner class PublicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(publication: Publication) {
            val date: Timestamp = publication.date
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateParse = formatter.format(date.toDate())
            itemView.findViewById<TextView>(R.id.postDate).text = dateParse
            itemView.findViewById<TextView>(R.id.postTitle).text = publication.title
            itemView.findViewById<TextView>(R.id.postDescription).text = publication.description
            itemView.findViewById<TextView>(R.id.postSubject).text = publication.subjectId

            itemView.findViewById<ImageView>(R.id.btnEdit).setOnClickListener{
                onEditClick(publication)
            }

            itemView.findViewById<ImageView>(R.id.btnDelete).setOnClickListener{
                onDeleteClick(publication)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_publication_edit, parent, false)
        return PublicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: PublicationViewHolder, position: Int) {
        holder.bind(publications[position])
    }

    override fun getItemCount(): Int = publications.size

    fun updateData(newPublications: List<Publication>) {
        publications.clear()
        publications.addAll(newPublications)
        notifyDataSetChanged()
    }

    fun updatePublication(publication: Publication) {
        val index = publications.indexOfFirst { it.publicationId == publication.publicationId }
        if (index != -1) {
            publications[index] = publication
            notifyItemChanged(index)
        }
    }

}