package com.example.flashcardmaster.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.flashcardmaster.ui.theme.*
import java.io.File
import java.io.FileOutputStream

@Composable
fun AddCardScreen(
    onSaveClick: (String, String, String?, String?) -> Unit,
    onNavigateBack: () -> Unit
) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }
    var frontImageUri by remember { mutableStateOf<Uri?>(null) }
    var backImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Check if camera is available
    val hasCamera = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

    // ====== CAMERA PICKERS (Declare first) ======
    val frontCameraPicker = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val uri = saveBitmapToUri(context, bitmap, "front_${System.currentTimeMillis()}.jpg")
                frontImageUri = uri
            } catch (e: Exception) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Failed to save image: ${e.message}")
                }
            }
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Failed to capture image")
            }
        }
    }

    val backCameraPicker = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val uri = saveBitmapToUri(context, bitmap, "back_${System.currentTimeMillis()}.jpg")
                backImageUri = uri
            } catch (e: Exception) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Failed to save image: ${e.message}")
                }
            }
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Failed to capture image")
            }
        }
    }

    // ====== PERMISSION LAUNCHER ======
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, launch camera
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (cameraIntent.resolveActivity(context.packageManager) != null) {
                // Need to know which camera button was pressed
                // We'll handle this in the button click
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("No camera app found")
                }
            }
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Camera permission denied")
            }
        }
    }

    // ====== GALLERY PICKERS ======
    val frontGalleryPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            frontImageUri = it
        }
    }

    val backGalleryPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            backImageUri = it
        }
    }

    // Track which camera button was pressed
    var isFrontCamera by remember { mutableStateOf(true) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            SurfaceDark,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = EraCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "NEW CARD",
                    style = MaterialTheme.typography.labelLarge,
                    color = EraPurple,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Front Card Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "FRONT",
                        style = MaterialTheme.typography.labelLarge,
                        color = EraOrange,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Front image preview
                    if (frontImageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(SurfaceDark, RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(context)
                                        .data(frontImageUri)
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = "Front image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { frontImageUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(32.dp)
                                    .background(
                                        HumanPink.copy(alpha = 0.8f),
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Front text input
                    OutlinedTextField(
                        value = front,
                        onValueChange = { front = it },
                        placeholder = {
                            Text(
                                "Enter your question",
                                color = TextMuted
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EraOrange,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            cursorColor = EraOrange
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Image buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Gallery button
                        OutlinedButton(
                            onClick = { frontGalleryPicker.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent
                            ),
                            border = BorderStroke(1.dp, EraOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = EraOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery", color = EraOrange, fontSize = 14.sp)
                        }

                        // Camera button
                        OutlinedButton(
                            onClick = {
                                if (!hasCamera) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("No camera available on this device")
                                    }
                                    return@OutlinedButton
                                }

                                val permissionCheck = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                )

                                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                    isFrontCamera = true
                                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                    if (cameraIntent.resolveActivity(context.packageManager) != null) {
                                        frontCameraPicker.launch(null)
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("No camera app found")
                                        }
                                    }
                                } else {
                                    isFrontCamera = true
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent
                            ),
                            border = BorderStroke(1.dp, EraOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = EraOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Camera", color = EraOrange, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back Card Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "BACK",
                        style = MaterialTheme.typography.labelLarge,
                        color = EraCyan,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Back image preview
                    if (backImageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(SurfaceDark, RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(context)
                                        .data(backImageUri)
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = "Back image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { backImageUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(32.dp)
                                    .background(
                                        HumanPink.copy(alpha = 0.8f),
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Back text input
                    OutlinedTextField(
                        value = back,
                        onValueChange = { back = it },
                        placeholder = {
                            Text(
                                "Enter your answer",
                                color = TextMuted
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EraCyan,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            cursorColor = EraCyan
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Image buttons row for back
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { backGalleryPicker.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent
                            ),
                            border = BorderStroke(1.dp, EraCyan),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = EraCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery", color = EraCyan, fontSize = 14.sp)
                        }

                        // Camera button for back
                        OutlinedButton(
                            onClick = {
                                if (!hasCamera) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("No camera available on this device")
                                    }
                                    return@OutlinedButton
                                }

                                val permissionCheck = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                )

                                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                    isFrontCamera = false
                                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                    if (cameraIntent.resolveActivity(context.packageManager) != null) {
                                        backCameraPicker.launch(null)
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("No camera app found")
                                        }
                                    }
                                } else {
                                    isFrontCamera = false
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent
                            ),
                            border = BorderStroke(1.dp, EraCyan),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = EraCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Camera", color = EraCyan, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = {
                    onSaveClick(front, back, frontImageUri?.toString(), backImageUri?.toString())
                    onNavigateBack()
                },
                enabled = front.isNotBlank() && back.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(16.dp, RoundedCornerShape(20.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = if (front.isNotBlank() && back.isNotBlank())
                                    listOf(EraOrange, EraPurple)
                                else listOf(TextMuted, TextMuted)
                            ),
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "SAVE CARD",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Helper function to save bitmap to URI
fun saveBitmapToUri(context: Context, bitmap: Bitmap, filename: String): Uri {
    val file = File(context.filesDir, filename)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}