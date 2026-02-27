package com.example.flashcardmaster.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcardmaster.ui.theme.*

val deckIcons = listOf("📚", "🤖", "💻", "🗣️", "🏛️", "🔬", "🎨", "🧠", "🌍", "🎵", "⚽", "🌮")
val deckColors = listOf("#FF7B4A", "#4AD9FF", "#C05AFF", "#FF5A8A", "#4ADE80", "#FFD966", "#9C89B8")

@Composable
fun AddDeckScreen(
    onSaveClick: (String, String, String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var deckName by remember { mutableStateOf("") }
    var deckDescription by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(deckIcons[0]) }
    var selectedColor by remember { mutableStateOf(deckColors[1]) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header with back button
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
                "NEW DECK",
                style = MaterialTheme.typography.labelLarge,
                color = EraPurple,
                modifier = Modifier.padding(end = 16.dp)
            )
        }

        // Title
        Text(
            "Create a Deck",
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Deck name input
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "DECK NAME",
                    style = MaterialTheme.typography.labelLarge,
                    color = EraOrange
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    placeholder = {
                        Text(
                            "e.g., Machine Learning",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EraOrange,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        cursorColor = EraOrange
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // Description input
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "DESCRIPTION",
                    style = MaterialTheme.typography.labelLarge,
                    color = EraCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = deckDescription,
                    onValueChange = { deckDescription = it },
                    placeholder = {
                        Text(
                            "Brief description",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EraCyan,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        cursorColor = EraCyan
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // Icon selector
        Text(
            "ICON",
            style = MaterialTheme.typography.labelLarge,
            color = TextMuted,
            modifier = Modifier.padding(start = 8.dp)
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(deckIcons) { icon ->
                Card(
                    onClick = { selectedIcon = icon },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedIcon == icon)
                            EraPurple.copy(alpha = 0.2f)
                        else SurfaceDark
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (selectedIcon == icon) EraPurple else BorderColor
                    ),
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            icon,
                            fontSize = 32.sp
                        )
                    }
                }
            }
        }

        // Color selector
        Text(
            "COLOR",
            style = MaterialTheme.typography.labelLarge,
            color = TextMuted,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(deckColors) { colorHex ->
                val color = Color(android.graphics.Color.parseColor(colorHex))
                Card(
                    onClick = { selectedColor = colorHex },
                    colors = CardDefaults.cardColors(containerColor = color),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(8.dp, RoundedCornerShape(12.dp)),
                    border = if (selectedColor == colorHex)
                        BorderStroke(2.dp, Color.White)
                    else null
                ) {}
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Create button with gradient
        Button(
            onClick = { onSaveClick(deckName, deckDescription, selectedIcon, selectedColor) },
            enabled = deckName.isNotBlank(),
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
                            colors = if (deckName.isNotBlank())
                                listOf(EraPurple, EraCyan)
                            else listOf(TextMuted, TextMuted)
                        ),
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "CREATE DECK",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Extra space at bottom for comfortable scrolling
        Spacer(modifier = Modifier.height(32.dp))
    }
}