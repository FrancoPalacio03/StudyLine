package com.example.studyline.ui.OwnPost

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studyline.data.repository.PublicationRepositories.QueryPublication
import com.example.studyline.databinding.FragmentOwnPostBinding
import com.example.studyline.databinding.FragmentHomeBinding
import com.example.studyline.ui.publications.PublicationAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class OwnPost : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var adapter: PublicationAdapter
    private val queryPublicationRepository = QueryPublication()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val recyclerView = binding.rvPublications
        adapter = PublicationAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Cargar publicaciones
        loadPublications()

        return root
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