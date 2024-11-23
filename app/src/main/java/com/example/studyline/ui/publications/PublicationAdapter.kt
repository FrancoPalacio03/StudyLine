package com.example.studyline.ui.publications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.studyline.R
import com.example.studyline.data.model.Publication
import com.example.studyline.ui.home.PostDetailFragment
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class PublicationAdapter(
    private var publications: List<Publication>
) : RecyclerView.Adapter<PublicationAdapter.PublicationViewHolder>() {

    private var itemClickListener: ((String) -> Unit)? = null // Definimos un listener privado

    // ViewHolder: Representa una tarjeta individual en la lista
    inner class PublicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(publication: Publication) {
            itemView.findViewById<TextView>(R.id.postTitle).text = publication.title
            val date: Timestamp = publication.date
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateParse = formatter.format(date.toDate())
            itemView.findViewById<TextView>(R.id.postDate).text = dateParse
            itemView.findViewById<TextView>(R.id.postDescription).text = publication.description
            itemView.findViewById<TextView>(R.id.postSubject).text = publication.subjectId
            itemView.findViewById<TextView>(R.id.postCountLike).text = publication.likes.toString()
            itemView.findViewById<TextView>(R.id.postCountDislike).text = publication.dislikes.toString()

            // Configura el listener de clics
            itemView.setOnClickListener {
                itemClickListener?.invoke(publication.publicationId) // Llama al listener cuando se hace clic
            }
        }
    }

    // Crear una nueva tarjeta (ViewHolder) cuando sea necesario
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_publication, parent, false)
        return PublicationViewHolder(view)
    }

    // Llenar la tarjeta con los datos correspondientes
    override fun onBindViewHolder(holder: PublicationViewHolder, position: Int) {
        holder.bind(publications[position])
    }

    override fun getItemCount(): Int = publications.size

    // Actualizar los datos cuando cambien
    fun updateData(newPublications: List<Publication>) {
        publications = newPublications
        notifyDataSetChanged() // Notificar al RecyclerView que los datos han cambiado
    }

    // Método para que el HomeFragment pueda establecer el listener
    fun setOnItemClickListener(listener: (String) -> Unit) {
        itemClickListener = listener
    }
}

