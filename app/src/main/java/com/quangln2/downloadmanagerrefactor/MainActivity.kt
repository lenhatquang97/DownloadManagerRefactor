package com.quangln2.downloadmanagerrefactor

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.quangln2.downloadmanagerrefactor.data.database.PrefSingleton
import com.quangln2.downloadmanagerrefactor.data.model.settings.GlobalSettings
import com.quangln2.downloadmanagerrefactor.databinding.ActivityMainBinding
import com.quangln2.downloadmanagerrefactor.ui.dialog.AddToDownloadDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val navController by lazy { findNavController(R.id.nav_host_fragment_content_main) }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        PrefSingleton.instance?.initialize(applicationContext)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        window.statusBarColor = ContextCompat.getColor(this, R.color.blue)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)

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
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                if (doesHaveWriteExternalPermission()) {
                    val dialog = AddToDownloadDialog()
                    dialog.show(supportFragmentManager, "AddToDownloadDialog")
                }
            } else {
                val dialog = AddToDownloadDialog()
                dialog.show(supportFragmentManager, "AddToDownloadDialog")
            }

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