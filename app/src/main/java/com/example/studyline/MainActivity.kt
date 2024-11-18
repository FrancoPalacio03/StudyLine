package com.example.studyline

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import com.example.studyline.data.model.Publication
import com.example.studyline.data.repository.PublicationRepositories.CommandPublication
import com.example.studyline.data.repository.PublicationRepositories.QueryPublication
import com.example.studyline.data.repository.StorageRepository
import com.example.studyline.data.repository.UserRepository
import com.example.studyline.databinding.ActivityMainBinding
import com.example.studyline.ui.login.LoginActivity
import com.example.studyline.ui.login.LoginViewModel
import com.example.studyline.ui.login.LoginViewModelFactory
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class ProviderType{
    BASIC
}

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var loginViewModel: LoginViewModel
    private var userRepo = UserRepository()
    private var queryPostRepo =  QueryPublication()
    private var commandPostRepo = CommandPublication()
    private var storageRepo = StorageRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)
        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
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

        val newPost = Publication(
            publicationId = "p1",
            userId = "user1",
            subjectId = "s1",
            topic = "publication",
            description = "publicacion 1 2 3",
        )

        val newCommnet = Publication(
            publicationId = "c2",
            userId = "user1",
            subjectId = "s1",
            fatherPublicationId = "p1",
            topic = "comment",
            description = "comentario 1 2 3",
        )

        /*val inputStream = assets.open("deyverson.jpg")
        val fileBytes = inputStream.readBytes()
        inputStream.close()*/

        lifecycleScope.launch {
            /*val url = storageRepo.uploadFileTest("p1", "deyverson", fileBytes)
            if(url != null)
                Log.i("uploadFileTest", "Success to upload file ${url}")*/
            //commandPostRepo.createNewPost(newPost, null)
            //commandPostRepo.createNewComment("p1", newCommnet)
            //commandPostRepo.updatePublicationByField("p1", "description", "upgradeando publicacion")
            //commandPostRepo.deletePostById("p1")
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
}