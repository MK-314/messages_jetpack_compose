package com.example.class7

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import com.example.class7.ui.theme.Class7Theme
//
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.*

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var requestingLocationUpdates = true

    @ExperimentalPermissionsApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    Log.d(TAG, location.toString())
                }
            }
        }
        setContent {
            Class7Theme {
                Surface(color = MaterialTheme.colors.background) {
                    MainScreen(
                        navigateToSettingsScreen = {
                            startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_SETTINGS,
                                    Uri.fromParts("package", packageName, null)
                                )
                            )
                        },
                        permissionSuccessCallback = {
                            startLocationUpdates()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
        return locationRequest
    }

    private fun startLocationUpdates() {
        val locationRequest = createLocationRequest()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        val TAG = "lecture 5"
    }
}

@ExperimentalPermissionsApi
@Composable
private fun MainScreen(
    navigateToSettingsScreen: () -> Unit,
    permissionSuccessCallback: () -> Unit
) {
    var doNotShowRationale by rememberSaveable() {
        mutableStateOf(false)
    }
    val locationPermissionState =
        rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    if (locationPermissionState.hasPermission) {
        permissionSuccessCallback()
    }

    PermissionRequired(
        permissionState = locationPermissionState,
        permissionNotGrantedContent = {
            if (doNotShowRationale) {
                Text("This feature is not available")
            } else {
                Rationale(onDoNotShowRationale = {
                    doNotShowRationale = true
                },
                    onRequestPermission = { locationPermissionState.launchPermissionRequest() })
            }
        },
        permissionNotAvailableContent = {
            PermissionDenied(
                navigateToSettingsScreen = navigateToSettingsScreen
            )
        }) {
        Text(text = "Location permission granted")
    }
}

@Composable
private fun Rationale(
    onDoNotShowRationale: () -> Unit,
    onRequestPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            // Dismiss the dialog when the user clicks outside the dialog or on the back
            // button. If you want to disable that functionality, simply use an empty
            // onCloseRequest.
        },
        title = {
            Text(text = "Permission Rational")
        },
        text = {
            Text("Location permission ")
        },
        confirmButton = {
            Button(onClick = onRequestPermission) {
                Text("This is the Confirm Button")
            }
        },
        dismissButton = {
            Button(
                onClick = onDoNotShowRationale
            ) {
                Text("This is the dismiss Button")
            }
        }
    )
}

@Composable
fun PermissionDenied(
    navigateToSettingsScreen: () -> Unit
) {
    Column {
        Text(text = "Location permission denied. Please, we need this to spy =)")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = navigateToSettingsScreen) {
            Text(text = "Open Settings")
        }
    }
}