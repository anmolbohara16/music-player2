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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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

                fun hasAudioAccess() = ContextCompat.checkSelfPermission(this@MainActivity, permissionsToRequest.first()) == PackageManager.PERMISSION_GRANTED
                var hasAudio by remember { mutableStateOf(hasAudioAccess()) }
                val lifecycle = LocalLifecycleOwner.current.lifecycle
                DisposableEffect(lifecycle) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            val granted = hasAudioAccess()
                            if (granted && !hasAudio) viewModel.refreshMusicLibrary()
                            hasAudio = granted
                        }
                    }
                    lifecycle.addObserver(observer)
                    onDispose { lifecycle.removeObserver(observer) }
                }
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    hasAudio = hasAudioAccess()
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
                    if (hasAudio) MainScreen(viewModel = viewModel)
                    else Column(Modifier.fillMaxSize().safeDrawingPadding().padding(28.dp),
                        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start) {
                        Text("Your music.\nYour space.", style = MaterialTheme.typography.displayLarge)
                        Spacer(Modifier.height(20.dp))
                        Text("Allow access to audio to bring your songs, albums and artists together. Your music stays on this device.", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { permissionLauncher.launch(permissionsToRequest.toTypedArray()) }) { Text("Find my music") }
                        TextButton(onClick = { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))) }) { Text("Open app permissions") }
                    }
                }
            }
        }
    }
}

