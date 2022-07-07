package com.quangln2.mydownloadmanager

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.StrictMode
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.asLiveData
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.quangln2.mydownloadmanager.controller.DownloadManagerController
import com.quangln2.mydownloadmanager.databinding.ActivityMainBinding
import com.quangln2.mydownloadmanager.ui.dialog.AddToDownloadDialog

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val navController by lazy { findNavController(R.id.nav_host_fragment_content_main) }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        DownloadManagerController.downloadListSchema = (application as DownloadManagerApplication).database.downloadDao().getAll().asLiveData()

        DownloadManagerController.getDataFromDatabase()

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val permissionCheck = ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setSupportActionBar(binding.toolbar)
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)


        binding.fab.setOnClickListener {
            val dialog = AddToDownloadDialog()
            dialog.show(supportFragmentManager, "AddToDownloadDialog")
        }


        //Set burger menu
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        }

        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.all -> DownloadManagerController.filterCategories("All")
                R.id.compressed -> DownloadManagerController.filterCategories("Compressed")
                R.id.documents -> DownloadManagerController.filterCategories("Documents")
                R.id.packages -> DownloadManagerController.filterCategories("Packages")
                R.id.music -> DownloadManagerController.filterCategories("Music")
                R.id.video -> DownloadManagerController.filterCategories("Video")
                R.id.others -> DownloadManagerController.filterCategories("Others")
            }
            binding.drawerLayout.closeDrawers()
            binding.fab.show()
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        binding.fab.hide()
        return when (item.itemId) {
            R.id.action_settings -> {
                if(navController.currentDestination?.id == R.id.FirstFragment){
                    navController.navigate(R.id.action_FirstFragment_to_SecondFragment)
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}