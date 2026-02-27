package com.example.flashcardmaster.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY nextReviewDate ASC")
    fun getCardsForDeck(deckId: Int): Flow<List<Card>>

    @Query("SELECT * FROM cards ORDER BY createdAt DESC")
    fun getAllCards(): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND nextReviewDate <= :now ORDER BY nextReviewDate ASC")
    fun getDueCardsForDeck(deckId: Int, now: LocalDateTime): Flow<List<Card>>

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND nextReviewDate <= :now")
    suspend fun getDueCountForDeck(deckId: Int, now: LocalDateTime): Int

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND repetitions = 0")
    fun getNewCardCount(deckId: Int): Flow<Int>

    @Query("SELECT AVG(easeFactor) FROM cards WHERE deckId = :deckId")
    fun getAverageEaseFactor(deckId: Int): Flow<Float?>

    @Insert
    suspend fun insertCard(card: Card): Long

    @Update
    suspend fun updateCard(card: Card)

    @Delete
    suspend fun deleteCard(card: Card)

    @Query("DELETE FROM cards WHERE deckId = :deckId")
    suspend fun deleteCardsForDeck(deckId: Int)
}