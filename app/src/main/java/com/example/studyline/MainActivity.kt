package com.example.studyline

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studyline.data.model.Subject
import com.example.studyline.data.repository.PublicationRepositories.QueryPublication
import com.example.studyline.data.model.User
import com.example.studyline.data.repository.SubjectRepository
import com.example.studyline.data.repository.UserRepository
import com.example.studyline.databinding.ActivityMainBinding
import com.example.studyline.ui.login.LoginActivity
import com.example.studyline.ui.login.LoginViewModel
import com.example.studyline.ui.login.LoginViewModelFactory
import com.example.studyline.ui.publications.PublicationAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

enum class ProviderType{
    BASIC
}

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var loginViewModel: LoginViewModel
    private val userRepo = UserRepository()
    private val subjectRepo = SubjectRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)
        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            navController.navigate(R.id.action_global_createPostFragment)
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val userId= FirebaseAuth.getInstance().currentUser?.uid
        lifecycleScope.launch {
            val user = userRepo.getUserById(userId.toString())
            setupNavigationHeader(user)
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                loginViewModel.logout()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

                true

            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)


        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupNavigationHeader(user: User?) {
        // Accede al NavigationView dentro de la Activity
        val navigationView: NavigationView = binding.navView
        val headerView = navigationView.getHeaderView(0)

        // Encuentra los TextViews en el encabezado usando el layout 'nav_header_main.xml'
        val mainUserName: TextView = headerView.findViewById(R.id.mainUserName)
        val mainMail: TextView = headerView.findViewById(R.id.mainMail)
        val mainPhoto : ImageView = headerView.findViewById(R.id.imageView)

        // Actualiza los TextViews con la información del usuario
        if (user != null) {
            mainUserName.text = user.name ?: "Usuario"
            mainMail.text = user.email ?: "Sin correo disponible"
            if(user.downloadUrl != null)
                loadImageIntoImageView(user.downloadUrl ,mainPhoto)
        } else {
            mainUserName.text = "Invitado"
            mainMail.text = "Por favor inicia sesión"
        }
    }

    fun hideToolbarAndFab() {
        binding.appBarMain.toolbar.visibility = View.GONE
        binding.appBarMain.fab.visibility = View.GONE
    }

    fun showToolbarAndFab() {
        binding.appBarMain.toolbar.visibility = View.VISIBLE
        binding.appBarMain.fab.visibility = View.VISIBLE
    }

    private fun loadImageIntoImageView(photoUrl: String, container: ImageView) {
        Glide.with(this)
            .load(photoUrl) // URL de la imagen
            .circleCrop() // Aplica un recorte circular a la imagen
            .into(container) // Carga la imagen en el ImageView
    }

}