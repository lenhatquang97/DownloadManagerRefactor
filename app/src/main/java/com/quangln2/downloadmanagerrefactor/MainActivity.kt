package com.quangln2.downloadmanagerrefactor

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController
import com.quangln2.downloadmanagerrefactor.data.model.settings.GlobalSettings
import com.quangln2.downloadmanagerrefactor.data.repository.DefaultDownloadRepository
import com.quangln2.downloadmanagerrefactor.data.source.local.LocalDataSourceImpl
import com.quangln2.downloadmanagerrefactor.data.source.remote.RemoteDataSourceImpl
import com.quangln2.downloadmanagerrefactor.databinding.ActivityMainBinding
import com.quangln2.downloadmanagerrefactor.ui.dialog.AddToDownloadDialog
import com.quangln2.downloadmanagerrefactor.ui.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val navController by lazy { findNavController(R.id.nav_host_fragment_content_main) }
    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory(
            DefaultDownloadRepository(
                LocalDataSourceImpl(DownloadManagerApplication.database.downloadDao()),
                RemoteDataSourceImpl()
            )
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        println("OnCreate MainActivity")

        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        DownloadManagerController.downloadListSchema =
            DownloadManagerApplication.database.downloadDao().getAll().asLiveData()

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setSupportActionBar(binding.toolbar)
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            }
        }

        binding.fab.setOnClickListener {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                if (doesHaveWriteExternalPermission()) {
                    val dialog = AddToDownloadDialog()
                    dialog.show(supportFragmentManager, "AddToDownloadDialog")
                } else {
                    Toast.makeText(
                        applicationContext,
                        "You must grant permission to use this feature!!",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } else {
                val dialog = AddToDownloadDialog()
                dialog.show(supportFragmentManager, "AddToDownloadDialog")
            }

        }


        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        }


        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.all -> viewModel.filterCategories("All")
                R.id.compressed -> viewModel.filterCategories("Compressed")
                R.id.documents -> viewModel.filterCategories("Documents")
                R.id.packages -> viewModel.filterCategories("Packages")
                R.id.music -> viewModel.filterCategories("Music")
                R.id.video -> viewModel.filterCategories("Video")
                R.id.others -> viewModel.filterCategories("Others")
            }
            binding.drawerLayout.closeDrawers()
            binding.fab.show()
            true
        }
        lifecycleScope.launch {
            GlobalSettings.getMaximumDownloadThread(applicationContext).collect {
                GlobalSettings.numsOfMaxDownloadThreadExported = it.toInt()
            }
            this.cancel()
        }

    }

    private fun doesHaveWriteExternalPermission(): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                if (navController.currentDestination?.id == R.id.FirstFragment) {
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

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch(Dispatchers.IO) {
            GlobalSettings.setMaximumDownloadThread(
                applicationContext,
                GlobalSettings.numsOfMaxDownloadThreadExported.toFloat()
            )
        }
    }

}