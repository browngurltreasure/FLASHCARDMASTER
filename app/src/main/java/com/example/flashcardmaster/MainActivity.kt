package com.example.flashcardmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp  // THIS IS THE MISSING IMPORT!
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashcardmaster.ui.*
import com.example.flashcardmaster.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlashcardTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(DeepSpace, Color(0xFF05080f)),
                                radius = 1200f
                            )
                        )
                ) {
                    CanvasBackground()
                    val viewModel: CardViewModel = viewModel(
                        factory = CardViewModelFactory(applicationContext)
                    )
                    FlashcardApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun CanvasBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val spacing = 28.dp.toPx()
        val dotColor = Color.White.copy(alpha = 0.055f)
        for (x in 0..size.width.toInt() step spacing.toInt()) {
            for (y in 0..size.height.toInt() step spacing.toInt()) {
                drawCircle(
                    color = dotColor,
                    radius = 1f,
                    center = Offset(x.toFloat(), y.toFloat())
                )
            }
        }
    }
}

@Composable
fun FlashcardApp(viewModel: CardViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.DECKS) }
    var selectedDeckId by remember { mutableStateOf<Int?>(null) }

    when (val screen = currentScreen) {
        is Screen.DECKS -> DecksScreen(
            viewModel = viewModel,
            onNavigateToDeck = { deckId ->
                selectedDeckId = deckId
                currentScreen = Screen.DECK_DETAIL
            },
            onNavigateToAddDeck = { currentScreen = Screen.ADD_DECK }
        )

        is Screen.DECK_DETAIL -> {
            if (selectedDeckId != null) {
                DeckDetailScreen(
                    deckId = selectedDeckId!!,
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = Screen.DECKS },
                    onNavigateToAddCard = { id ->
                        currentScreen = Screen.ADD_CARD(id)
                    },
                    onNavigateToReview = { id ->
                        currentScreen = Screen.REVIEW(id)
                    }
                )
            }
        }

        is Screen.ADD_DECK -> AddDeckScreen(
            onSaveClick = { name, description, icon, color ->
                viewModel.addDeck(name, description, icon, color)
                currentScreen = Screen.DECKS
            },
            onNavigateBack = { currentScreen = Screen.DECKS }
        )

        is Screen.ADD_CARD -> {
            AddCardScreen(
                onSaveClick = { front, back, frontImageUri, backImageUri ->
                    viewModel.addCard(screen.deckId, front, back, frontImageUri, backImageUri)
                    currentScreen = Screen.DECK_DETAIL
                },
                onNavigateBack = { currentScreen = Screen.DECK_DETAIL }
            )
        }

        is Screen.REVIEW -> {
            val cards by viewModel.getDueCardsForDeck(screen.deckId).collectAsState(initial = emptyList())
            ReviewScreen(
                cards = cards,
                onFinishReview = { currentScreen = Screen.DECK_DETAIL },
                onRateCard = { card, quality -> viewModel.rateCard(card, quality) },
                viewModel = viewModel
            )
        }
    }
}

sealed class Screen {
    object DECKS : Screen()
    object DECK_DETAIL : Screen()
    object ADD_DECK : Screen()
    data class ADD_CARD(val deckId: Int) : Screen()
    data class REVIEW(val deckId: Int) : Screen()
}