package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PersonalMusicPlayerTheme
import com.example.ui.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalMusicPlayerTheme {
                val viewModel: MusicViewModel = viewModel()

                val permissionsToRequest = buildList {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        add(Manifest.permission.READ_MEDIA_AUDIO)
                        add(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    viewModel.refreshMusicLibrary()
                }

                LaunchedEffect(Unit) {
                    val missingPermissions = permissionsToRequest.filter {
                        ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
                    }
                    if (missingPermissions.isNotEmpty()) {
                        permissionLauncher.launch(missingPermissions.toTypedArray())
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

