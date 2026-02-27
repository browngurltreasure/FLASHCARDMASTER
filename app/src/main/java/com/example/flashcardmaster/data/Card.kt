package com.example.flashcardmaster.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.Duration

@Entity(
    tableName = "cards",
    foreignKeys = [ForeignKey(
        entity = Deck::class,
        parentColumns = ["id"],
        childColumns = ["deckId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val deckId: Int = 0,
    val front: String,
    val back: String,
    val frontImageUri: String? = null,
    val backImageUri: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val repetitions: Int = 0,
    val easeFactor: Float = 2.5f,
    val interval: Int = 0,
    val nextReviewDate: LocalDateTime = LocalDateTime.now(),
    val lapses: Int = 0,
    val lastReviewed: LocalDateTime? = null
) {
    fun isDue(): Boolean = LocalDateTime.now() >= nextReviewDate
    fun daysUntilDue(): Long = Duration.between(LocalDateTime.now(), nextReviewDate).toDays()
    fun hasFrontImage(): Boolean = !frontImageUri.isNullOrBlank()
    fun hasBackImage(): Boolean = !backImageUri.isNullOrBlank()

    fun getProgressStatus(): String = when {
        repetitions == 0 -> "New"
        interval <= 1 -> "Learning"
        interval <= 7 -> "Young"
        interval <= 30 -> "Mature"
        else -> "Mastered"
    }

    fun getEraColor(): String = when (getProgressStatus()) {
        "New" -> "#f97316"
        "Learning" -> "#22d3ee"
        "Young" -> "#a78bfa"
        "Mature" -> "#4ade80"
        else -> "#f43f5e"
    }
}