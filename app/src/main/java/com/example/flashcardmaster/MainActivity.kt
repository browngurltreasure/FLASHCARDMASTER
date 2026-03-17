package com.example.flashcardmaster

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.flashcardmaster.ui.*
import com.example.flashcardmaster.ui.theme.FLASHCARDMASTERTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CardViewModel by viewModels {
        CardViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FLASHCARDMASTERTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    FlashcardMasterApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun FlashcardMasterApp(viewModel: CardViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Camera permission handling
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Listen for camera permission requests from ViewModel
    LaunchedEffect(Unit) {
        viewModel.cameraPermissionRequested.collect {
            if (!hasCameraPermission) {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "decks"
    ) {
        composable("decks") {
            DecksScreen(
                viewModel = viewModel,
                onNavigateToDeck = { deckId ->
                    navController.navigate("deck/$deckId")
                },
                onNavigateToAddDeck = {
                    navController.navigate("addDeck")
                }
            )
        }

        composable("addDeck") {
            AddDeckScreen(
                onSaveClick = { name, description, icon, color ->
                    viewModel.addDeck(name, description, icon, color)
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("deck/{deckId}") { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId")?.toIntOrNull() ?: 0
            DeckDetailScreen(
                deckId = deckId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddCard = { id ->
                    navController.navigate("addCard/$id")
                },
                onNavigateToReview = { id ->
                    navController.navigate("review/$id")
                }
            )
        }

        composable("addCard/{deckId}") { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId")?.toIntOrNull() ?: 0
            AddCardScreen(
                onSaveClick = { front, back, frontImageUri, backImageUri ->
                    viewModel.addCard(deckId, front, back, frontImageUri, backImageUri)
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("review/{deckId}") { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId")?.toIntOrNull() ?: 0
            val cards by viewModel.getDueCardsForDeck(deckId)
                .collectAsStateWithLifecycle(emptyList())

            ReviewScreen(
                cards = cards,
                onFinishReview = {
                    navController.popBackStack()
                },
                onRateCard = { card, quality ->
                    viewModel.rateCard(card, quality)
                },
                viewModel = viewModel
            )
        }
    }
}