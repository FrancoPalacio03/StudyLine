package com.example.studyline.ui.account

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.studyline.databinding.FragmentAccountBinding
import java.util.*

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)

        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Datos para los Spinners
        val languages = arrayOf("Español", "Inglés")
        val themes = arrayOf("Claro", "Oscuro")

        // Obtener SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        // Recuperar valores previamente guardados
        val savedLanguage = sharedPreferences.getString("language", "es") ?: "es" // Idioma por defecto: Español
        val currentTheme = sharedPreferences.getInt("theme", AppCompatDelegate.MODE_NIGHT_NO)
        val currentThemeIndex = if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) 1 else 0

        // Adaptadores para los Spinners
        val languageAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        val themeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, themes)

        // Estilo de los desplegables
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Asignar adaptadores a los Spinners
        binding.spinnerLanguage.adapter = languageAdapter
        binding.spinnerTheme.adapter = themeAdapter

        // Mapear el idioma guardado al índice del spinner
        val savedLanguageIndex = when (savedLanguage) {
            "es" -> 0
            "en" -> 1
            else -> 0 // Valor por defecto
        }

        // Establecer las selecciones guardadas
        binding.spinnerLanguage.setSelection(savedLanguageIndex)
        binding.spinnerTheme.setSelection(currentThemeIndex)

        // Listener para el Spinner de idiomas
        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Mapear el índice a un código de idioma
                val selectedLanguageCode = when (position) {
                    0 -> "es" // Español
                    1 -> "en" // Inglés
                    else -> "es" // Español por defecto
                }

                // Guardar la selección en SharedPreferences
                sharedPreferences.edit().putString("language", selectedLanguageCode).apply()

                // Cambiar el idioma
                updateLocale(requireContext(), selectedLanguageCode)

                // Solo recargar la actividad si el idioma cambia
                if (savedLanguage != selectedLanguageCode) {
                    requireActivity().recreate()  // Reinicia la actividad para aplicar el cambio de idioma
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Listener para el Spinner del tema
        binding.spinnerTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val newTheme = if (position == 0) {
                    AppCompatDelegate.MODE_NIGHT_NO
                } else {
                    AppCompatDelegate.MODE_NIGHT_YES
                }

                // Guardar el tema seleccionado
                sharedPreferences.edit().putInt("theme", newTheme).apply()

                // Aplicar el tema seleccionado
                AppCompatDelegate.setDefaultNightMode(newTheme)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Función para actualizar el idioma de la aplicación
    fun updateLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }

        // Actualiza la configuración de la app
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
