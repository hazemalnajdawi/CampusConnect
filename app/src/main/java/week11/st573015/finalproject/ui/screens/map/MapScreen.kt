package week11.st573015.finalproject.ui.screens.map

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import week11.st573015.finalproject.vm.MapViewModel

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    navController: NavController,
    vm: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val pins by vm.pins.collectAsState()
    val loading by vm.loading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var tappedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(pins) {
        if (pins.isEmpty()) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude)
                }
            }
        }
    }

    val defaultPosition = pins.firstOrNull()?.let { LatLng(it.lat, it.lng) }
        ?: userLocation
        ?: LatLng(43.6532, -79.3832)
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, 16f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            onMapLongClick = { latLng ->
                tappedLatLng = latLng
                showAddDialog = true
            }
        ) {
            pins.forEach { pin ->
                Marker(
                    state = MarkerState(position = LatLng(pin.lat, pin.lng)),
                    title = pin.title,
                    snippet = pin.description,
                    onInfoWindowLongClick = {
                        vm.deletePin(pin.id)
                    }
                )
            }
        }

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .align(androidx.compose.ui.Alignment.BottomEnd)
            )
        }
    }

    if (showAddDialog && tappedLatLng != null) {
        AddLocationDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, desc ->
                vm.addPin(title, desc, tappedLatLng!!.latitude, tappedLatLng!!.longitude)
                showAddDialog = false
            }
        )
    }
}