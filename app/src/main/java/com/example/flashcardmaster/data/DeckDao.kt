package com.example.flashcardmaster.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks ORDER BY createdAt DESC")
    fun getAllDecks(): Flow<List<Deck>>

    @Query("SELECT * FROM decks WHERE id = :deckId")
    fun getDeckById(deckId: Int): Flow<Deck>

    @Insert
    suspend fun insertDeck(deck: Deck): Long

    @Update
    suspend fun updateDeck(deck: Deck)

    @Delete
    suspend fun deleteDeck(deck: Deck)

    @Query("SELECT COUNT(*) FROM decks")
    fun getDeckCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM decks")
    suspend fun getDeckCountSync(): Int
}