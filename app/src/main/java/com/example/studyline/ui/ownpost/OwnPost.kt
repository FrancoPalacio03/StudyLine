package com.example.studyline.ui.ownpost

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studyline.data.model.Publication
import com.example.studyline.data.repository.PublicationRepositories.CommandPublication
import com.example.studyline.data.repository.PublicationRepositories.QueryPublication
import com.example.studyline.databinding.FragmentOwnPostBinding
import com.example.studyline.ui.publications.OwnPublicationAdapter
import com.example.studyline.utils.MapsUtility
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class OwnPost : Fragment() {

    private var _binding: FragmentOwnPostBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var adapter: OwnPublicationAdapter
    private val queryPublicationRepository = QueryPublication()
    private val commandPublicationRepository = CommandPublication()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentOwnPostBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView = binding.rvOwnPublications
        adapter = OwnPublicationAdapter(
            mutableListOf(),
            onEditClick = { publication -> Log.d("handleEdit", "no implemented yet")},
            onDeleteClick = { publication -> handleDelete(publication)})
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Cargar publicaciones
        loadPublications()

        return root
    }

    private fun handleDelete(publication: Publication) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Eliminar Publicación")
        builder.setMessage("¿Desea eliminar esta publicación?")
        builder.setPositiveButton("Sí") { dialog, _ ->
            lifecycleScope.launch{
                commandPublicationRepository.deletePostById(publication.publicationId)
                loadPublications()
            }
            dialog.dismiss() // Cerrar el diálogo
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun loadPublications () {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "Unknown"
        lifecycleScope.launch {
            try {
                // Obtener publicaciones desde el repositorio
                val publications = queryPublicationRepository.getPublicationsByUser(userId)

                // Actualizar el adaptador con las publicaciones obtenidas
                if (publications != null) {
                    adapter.updateData(publications)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error al cargar publicaciones", e)
            }
        }
    }
}