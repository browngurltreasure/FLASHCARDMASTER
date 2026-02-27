package com.example.flashcardmaster.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Database(
    entities = [Deck::class, Card::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CardDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun cardDao(): CardDao

    companion object {
        @Volatile
        private var INSTANCE: CardDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cards ADD COLUMN frontImageUri TEXT")
                db.execSQL("ALTER TABLE cards ADD COLUMN backImageUri TEXT")
            }
        }

        fun getInstance(context: Context): CardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CardDatabase::class.java,
                    "flashcard_master_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()

                CoroutineScope(Dispatchers.IO).launch {
                    if (instance.deckDao().getDeckCountSync() == 0) {
                        prepopulateDatabase(instance)
                    }
                }

                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulateDatabase(db: CardDatabase) {
            val deckDao = db.deckDao()
            val cardDao = db.cardDao()

            val decks = listOf(
                Deck(name = "Machine Learning", description = "AI/ML concepts", color = "#f97316", icon = "🤖"),
                Deck(name = "Programming", description = "Kotlin & Android", color = "#22d3ee", icon = "💻"),
                Deck(name = "Spanish", description = "Vocabulary", color = "#a78bfa", icon = "🗣️")
            )

            decks.forEach { deck ->
                val deckId = deckDao.insertDeck(deck).toInt()
                val now = LocalDateTime.now()

                val sampleCards = when (deck.name) {
                    "Machine Learning" -> listOf(
                        Card(deckId = deckId, front = "What is a neural network?", back = "A computational model inspired by the brain", nextReviewDate = now),
                        Card(deckId = deckId, front = "What is backpropagation?", back = "Algorithm for training neural networks", nextReviewDate = now),
                        Card(deckId = deckId, front = "What is a transformer?", back = "Attention-based architecture for NLP", nextReviewDate = now)
                    )
                    "Programming" -> listOf(
                        Card(deckId = deckId, front = "What is a Composable function?", back = "Building block of Jetpack Compose", nextReviewDate = now),
                        Card(deckId = deckId, front = "What is State in Compose?", back = "Values that change over time", nextReviewDate = now),
                        Card(deckId = deckId, front = "What is ViewModel?", back = "Stores UI-related data", nextReviewDate = now)
                    )
                    else -> listOf(
                        Card(deckId = deckId, front = "Hola", back = "Hello", nextReviewDate = now),
                        Card(deckId = deckId, front = "Gracias", back = "Thank you", nextReviewDate = now),
                        Card(deckId = deckId, front = "Por favor", back = "Please", nextReviewDate = now)
                    )
                }
                sampleCards.forEach { cardDao.insertCard(it) }
            }
        }
    }
}