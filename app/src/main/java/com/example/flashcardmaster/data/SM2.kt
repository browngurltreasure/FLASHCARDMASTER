package com.example.flashcardmaster.data

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

enum class RecallQuality(val value: Int, val description: String) {
    COMPLETE_BLACKOUT(0, "Complete blackout"),
    INCORRECT_BUT_RECOGNIZED(1, "Incorrect but recognized"),
    INCORRECT_EASY_RECALL(2, "Incorrect but easy"),
    CORRECT_DIFFICULT(3, "Correct with difficulty"),
    CORRECT_HESITANT(4, "Correct after hesitation"),
    PERFECT(5, "Perfect recall")
}

data class ReviewLog(
    val reviewDate: LocalDateTime,
    val quality: RecallQuality,
    val previousInterval: Int,
    val newInterval: Int,
    val previousEase: Float,
    val newEase: Float
)

data class CardProgress(
    val repetitions: Int = 0,
    val easeFactor: Float = 2.5f,
    val interval: Int = 0,
    val nextReviewDate: LocalDateTime = LocalDateTime.now(),
    val reviewHistory: List<ReviewLog> = emptyList(),
    val lapses: Int = 0,
    val lastReviewed: LocalDateTime? = null
)

object SM2Algorithm {
    fun calculateNextReview(
        currentProgress: CardProgress,
        quality: RecallQuality
    ): CardProgress {
        val q = quality.value

        if (q < 3) {
            return CardProgress(
                repetitions = 0,
                easeFactor = max(1.3f, currentProgress.easeFactor - 0.2f),
                interval = 1,
                nextReviewDate = LocalDateTime.now().plusDays(1),
                reviewHistory = currentProgress.reviewHistory + ReviewLog(
                    reviewDate = LocalDateTime.now(),
                    quality = quality,
                    previousInterval = currentProgress.interval,
                    newInterval = 1,
                    previousEase = currentProgress.easeFactor,
                    newEase = max(1.3f, currentProgress.easeFactor - 0.2f)
                ),
                lapses = currentProgress.lapses + 1,
                lastReviewed = LocalDateTime.now()
            )
        }

        val newEase = currentProgress.easeFactor + when (q) {
            5 -> 0.1f
            4 -> 0.0f
            3 -> -0.14f
            else -> 0f
        }

        val clampedEase = max(1.3f, min(2.5f, newEase))
        val newRepetitions = currentProgress.repetitions + 1
        val newInterval = when (newRepetitions) {
            1 -> 1
            2 -> 6
            else -> (currentProgress.interval * clampedEase).toInt()
        }
        val cappedInterval = min(newInterval, 365 * 5)

        return CardProgress(
            repetitions = newRepetitions,
            easeFactor = clampedEase,
            interval = cappedInterval,
            nextReviewDate = LocalDateTime.now().plusDays(cappedInterval.toLong()),
            reviewHistory = currentProgress.reviewHistory + ReviewLog(
                reviewDate = LocalDateTime.now(),
                quality = quality,
                previousInterval = currentProgress.interval,
                newInterval = cappedInterval,
                previousEase = currentProgress.easeFactor,
                newEase = clampedEase
            ),
            lapses = currentProgress.lapses,
            lastReviewed = LocalDateTime.now()
        )
    }
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @TypeConverter
    fun fromRecallQuality(value: Int): RecallQuality {
        return RecallQuality.entries.first { it.value == value }
    }

    @TypeConverter
    fun recallQualityToInt(quality: RecallQuality): Int {
        return quality.value
    }
}