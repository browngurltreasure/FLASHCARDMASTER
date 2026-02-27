package com.example.flashcardmaster.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcardmaster.data.Deck
import com.example.flashcardmaster.ui.theme.*

@Composable
fun DecksScreen(
    viewModel: CardViewModel,
    onNavigateToDeck: (Int) -> Unit,
    onNavigateToAddDeck: () -> Unit
) {
    val decks by viewModel.decks.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deckToDelete by remember { mutableStateOf<Deck?>(null) }

    Scaffold(
        topBar = {
            // Using a Box instead of CenterAlignedTopAppBar to remove height restrictions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = 24.dp), // Space around the title
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "FLASHCARD",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 42.sp, // Slightly larger for impact
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp,
                            lineHeight = 44.sp
                        ),
                        color = EraCyan
                    )
                    Text(
                        "MASTER",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp,
                            lineHeight = 44.sp
                        ),
                        color = EraPurple
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddDeck,
                containerColor = EraPurple,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(64.dp)
                    .shadow(20.dp, RoundedCornerShape(16.dp))
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Deck",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        containerColor = Color.Transparent
    ) { padding ->

        // Delete Dialog
        if (showDeleteDialog && deckToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Deck") },
                text = { Text("Are you sure you want to delete '${deckToDelete!!.name}'?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteDeck(deckToDelete!!)
                            showDeleteDialog = false
                            deckToDelete = null
                        }
                    ) {
                        Text("Delete", color = HumanPink)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (decks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("✨", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Decks Yet",
                        style = MaterialTheme.typography.headlineMedium,
                        color = EraCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap the + button to create your first deck",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                items(decks) { deck ->
                    DeckCard(
                        deck = deck,
                        onClick = { onNavigateToDeck(deck.id) },
                        onDelete = {
                            deckToDelete = deck
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DeckCard(
    deck: Deck,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val deckColor = Color(android.graphics.Color.parseColor(deck.color))
    var showOptions by remember { mutableStateOf(false) }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clickable { onClick() }
                .shadow(12.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, deckColor.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                deckColor.copy(alpha = 0.2f),
                                SurfaceDark
                            ),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    deckColor.copy(alpha = 0.2f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = deck.icon, fontSize = 24.sp)
                        }

                        IconButton(
                            onClick = { showOptions = !showOptions },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = deckColor
                            )
                        }
                    }

                    Column {
                        Text(
                            text = deck.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = deck.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showOptions,
            onDismissRequest = { showOptions = false }
        ) {
            DropdownMenuItem(
                text = { Text("Delete Deck") },
                onClick = {
                    showOptions = false
                    onDelete()
                },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            )
        }
    }
}