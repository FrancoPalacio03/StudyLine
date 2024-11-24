package com.example.studyline.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studyline.data.repository.PublicationRepositories.QueryPublication
import com.example.studyline.databinding.FragmentHomeBinding
import com.example.studyline.ui.publications.PublicationAdapter
import kotlinx.coroutines.launch
import com.example.studyline.R
import com.example.studyline.data.model.Publication
import com.example.studyline.data.repository.PublicationRepositories.CommandPublication
import com.example.studyline.utils.MapsUtility
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

import androidx.navigation.fragment.findNavController

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var adapter: PublicationAdapter
    private val queryPublicationRepository = QueryPublication()
    private val commandPublicationRepository = CommandPublication()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val mapsUtility = MapsUtility(requireContext(), lifecycleScope)
        val recyclerView = binding.rvPublications
        adapter = PublicationAdapter(mutableListOf(),
                                    mapsUtility,
                                    onLikeDislikeClicked = { publication, isLike ->
                                    handleLikes(publication, isLike) // Llamar al método handleLikes
                                    })
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Configurar el OnClickListener para los ítems
        adapter.setOnItemClickListener { publicationId ->
            openPostDetail(publicationId) // Llamar a la función openPostDetail cuando se hace clic en un ítem
        }

        // Cargar publicaciones
        loadPublications()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleLikes(publication: Publication, isLike: Boolean) {
        lifecycleScope.launch{
            if(isLike) {
                commandPublicationRepository.likePublicationForId(publication.publicationId)
                publication.likes += 1
                Toast.makeText(requireContext(), "Has dejado un like", Toast.LENGTH_SHORT).show()
            }
            else {
                commandPublicationRepository.dislikePublicationForId(publication.publicationId)
                publication.dislikes += 1
                Toast.makeText(requireContext(), "Has dejado un dislike", Toast.LENGTH_SHORT).show()
            }
            adapter.updatePublication(publication)
        }
    }


    private fun loadPublications () {
        lifecycleScope.launch {
            try {
                // Obtener publicaciones desde el repositorio
                val publications = queryPublicationRepository.getRecentPublications()

                // Actualizar el adaptador con las publicaciones obtenidas
                if (publications != null) {
                    adapter.updateData(publications)
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error al cargar publicaciones", e)
            }
        }
    }

    private fun openPostDetail(publicationId: String) {
        val bundle = Bundle().apply {
            putString("publicationId", publicationId)
        }
        findNavController().navigate(R.id.action_homeFragment_to_postDetailFragment, bundle)
    }
}