package com.example.flashcardmaster.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.flashcardmaster.data.Card
import com.example.flashcardmaster.ui.theme.*
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun ReviewScreen(
    cards: List<Card>,
    onFinishReview: () -> Unit,
    onRateCard: (Card, Int) -> Unit,
    viewModel: CardViewModel
) {
    var remainingCards by remember { mutableStateOf(cards.toMutableList()) }
    var currentCard by remember { mutableStateOf(remainingCards.firstOrNull()) }
    var showAnswer by remember { mutableStateOf(false) }
    var showRating by remember { mutableStateOf(false) }
    val shakeCount by viewModel.shakeCount.collectAsState()

    // Handle shake to shuffle
    LaunchedEffect(shakeCount) {
        if (shakeCount > 0 && shakeCount % 3 == 0) {
            remainingCards = remainingCards.shuffled().toMutableList()
            currentCard = remainingCards.firstOrNull()
            viewModel.shuffleCards()
        }
    }

    if (remainingCards.isEmpty() || currentCard == null) {
        // Completion screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("✨", fontSize = 48.sp)
                    Text(
                        "Review Complete!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = EraCyan
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Great job!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onFinishReview,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, EraPurple),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Back to Deck", color = EraPurple)
                    }
                }
            }
        }
        return
    }

    currentCard?.let { card ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${cards.size - remainingCards.size + 1} / ${cards.size}",
                    color = EraCyan,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Shakes: $shakeCount",
                    color = EraPurple,
                    fontSize = 12.sp
                )
            }

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(EraCyan.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            ) {
                val progress = (cards.size - remainingCards.size + 1) / cards.size.toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(EraCyan, RoundedCornerShape(2.dp))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Flip Card with Image
            FlipCardWithImage(
                card = card,
                showAnswer = showAnswer,
                onFlip = {
                    viewModel.triggerHapticFeedback("flip")
                    showAnswer = !showAnswer
                    if (showAnswer) showRating = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Rating buttons
            if (showRating) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "How well did you remember?",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )

                    RatingButton(
                        text = "Again (0-2)",
                        color = HumanPink,
                        onClick = {
                            onRateCard(card, 1)
                            remainingCards = remainingCards.drop(1).toMutableList()
                            currentCard = remainingCards.firstOrNull()
                            showAnswer = false
                            showRating = false
                        }
                    )

                    RatingButton(
                        text = "Hard (3)",
                        color = EraOrange,
                        onClick = {
                            onRateCard(card, 3)
                            remainingCards = remainingCards.drop(1).toMutableList()
                            currentCard = remainingCards.firstOrNull()
                            showAnswer = false
                            showRating = false
                        }
                    )

                    RatingButton(
                        text = "Good (4)",
                        color = EraCyan,
                        onClick = {
                            onRateCard(card, 4)
                            remainingCards = remainingCards.drop(1).toMutableList()
                            currentCard = remainingCards.firstOrNull()
                            showAnswer = false
                            showRating = false
                        }
                    )

                    RatingButton(
                        text = "Perfect (5) ✨",
                        color = EraPurple,
                        onClick = {
                            onRateCard(card, 5)
                            remainingCards = remainingCards.drop(1).toMutableList()
                            currentCard = remainingCards.firstOrNull()
                            showAnswer = false
                            showRating = false
                        }
                    )
                }
            } else {
                Text(
                    "👆 Tap card to reveal • Shake to shuffle",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun RatingButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, color = color)
    }
}

@Composable
fun FlipCardWithImage(
    card: Card,
    showAnswer: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (showAnswer) 180f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing)
    )

    Card(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onFlip() })
            }
            .graphicsLayer {
                rotationY = rotationAngle
                cameraDistance = 12f * density
            },
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val accentColor = if (rotationAngle <= 90f) EraOrange else EraPurple
                    onDrawBehind {
                        drawLine(
                            color = accentColor,
                            start = Offset(0f, 0f),
                            end = Offset(100f, 0f),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = accentColor,
                            start = Offset(size.width, size.height),
                            end = Offset(size.width - 100f, size.height),
                            strokeWidth = 3f
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (rotationAngle <= 90f) {
                // FRONT SIDE
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (card.hasFrontImage()) {
                        val imageUri = card.frontImageUri
                        if (!imageUri.isNullOrBlank()) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(if (imageUri.startsWith("/")) File(imageUri) else imageUri)
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = "Front image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    Text(
                        text = card.front,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                // BACK SIDE
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .graphicsLayer { rotationY = 180f }
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (card.hasBackImage()) {
                        val imageUri = card.backImageUri
                        if (!imageUri.isNullOrBlank()) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(if (imageUri.startsWith("/")) File(imageUri) else imageUri)
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = "Back image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    Text(
                        text = card.back,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}