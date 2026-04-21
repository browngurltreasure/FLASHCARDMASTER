package com.example.flashcardmaster.ui

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import com.example.flashcardmaster.data.*
import com.example.flashcardmaster.sensors.ShakeDetector
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.io.File
import java.io.FileOutputStream

class CardViewModel(private val database: CardDatabase, private val context: Context) : ViewModel() {

    private val deckDao = database.deckDao()
    private val cardDao = database.cardDao()

    private var _shakeDetector: ShakeDetector? = null
    val shakeDetector: ShakeDetector? get() = _shakeDetector

    val decks: StateFlow<List<Deck>> = deckDao.getAllDecks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _testResults = MutableStateFlow<Map<String, TestResult>>(emptyMap())
    val testResults: StateFlow<Map<String, TestResult>> = _testResults.asStateFlow()

    private val _sensorStatus = MutableStateFlow("Sensors: Initializing...")
    val sensorStatus: StateFlow<String> = _sensorStatus.asStateFlow()

    private val _shakeCount = MutableStateFlow(0)
    val shakeCount: StateFlow<Int> = _shakeCount.asStateFlow()

    // NEW: Camera permission request event
    private val _cameraPermissionRequested = MutableSharedFlow<Unit>()
    val cameraPermissionRequested: SharedFlow<Unit> = _cameraPermissionRequested.asSharedFlow()

    init {
        initializeSensors()
    }

    // NEW: Check camera permission
    fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    // NEW: Request camera permission
    fun requestCameraPermission() {
        viewModelScope.launch {
            _cameraPermissionRequested.emit(Unit)
        }
    }

    fun triggerHapticFeedback(type: String) {
        when (type) {
            "tap" -> _shakeDetector?.gentleTap()
            "success" -> _shakeDetector?.successVibration()
            "error" -> _shakeDetector?.errorVibration()
            "flip" -> _shakeDetector?.vibrate(20)
            else -> _shakeDetector?.gentleTap()
        }
    }

    private fun initializeSensors() {
        try {
            _shakeDetector = ShakeDetector(context).also { detector ->
                detector.startListening()
                viewModelScope.launch {
                    detector.shakeEvents.collect { count ->
                        _shakeCount.value = count
                        if (count % 3 == 0) {
                            _sensorStatus.value = "Sensors: ✓ Shake detected! ($count)"
                            detector.successVibration()
                        } else {
                            _sensorStatus.value = "Sensors: ✓ Online (Shake $count)"
                        }
                    }
                }
            }
            _sensorStatus.value = "Sensors: ✓ Online"
        } catch (e: Exception) {
            _sensorStatus.value = "Sensors: ✗ Offline - ${e.message}"
        }
    }

    fun addDeck(name: String, description: String, icon: String, color: String) {
        viewModelScope.launch {
            deckDao.insertDeck(Deck(name = name, description = description, icon = icon, color = color))
            triggerHapticFeedback("success")
            addTestResult("AddDeck: $name", true)
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch {
            deckDao.deleteDeck(deck)
            triggerHapticFeedback("error")
            addTestResult("DeleteDeck: ${deck.name}", true)
        }
    }

    fun addCard(deckId: Int, front: String, back: String, frontImageUri: String? = null, backImageUri: String? = null) {
        viewModelScope.launch {
            val savedFrontUri = frontImageUri?.let { saveImageToInternalStorage(it) }
            val savedBackUri = backImageUri?.let { saveImageToInternalStorage(it) }

            cardDao.insertCard(
                Card(
                    deckId = deckId,
                    front = front,
                    back = back,
                    frontImageUri = savedFrontUri,
                    backImageUri = savedBackUri,
                    interval = 1,
                    nextReviewDate = LocalDateTime.now()
                )
            )
            triggerHapticFeedback("tap")
            addTestResult("AddCard: $front", true)
        }
    }

    fun deleteCard(card: Card) {
        viewModelScope.launch {
            cardDao.deleteCard(card)
            triggerHapticFeedback("error")
            addTestResult("DeleteCard: ${card.front}", true)
        }
    }

    private fun saveImageToInternalStorage(uriString: String): String {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return uriString
            val fileName = "card_image_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { outputStream -> inputStream.copyTo(outputStream) }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            uriString
        }
    }

    fun getCardsForDeck(deckId: Int): Flow<List<Card>> = cardDao.getCardsForDeck(deckId)

    fun getDueCardsForDeck(deckId: Int): Flow<List<Card>> = cardDao.getDueCardsForDeck(deckId, LocalDateTime.now())

    fun rateCard(card: Card, quality: Int) {
        viewModelScope.launch {
            val newEase = when (quality) {
                5 -> card.easeFactor + 0.1f
                4 -> card.easeFactor
                3 -> card.easeFactor - 0.14f
                else -> card.easeFactor - 0.2f
            }.coerceIn(1.3f..2.5f)

            val newRepetitions = if (quality < 3) 0 else card.repetitions + 1

            val newInterval = when {
                quality < 3 -> 1
                newRepetitions == 1 -> 1
                newRepetitions == 2 -> 6
                else -> (card.interval * newEase).toInt()
            }

            val updatedCard = card.copy(
                repetitions = newRepetitions,
                easeFactor = newEase,
                interval = newInterval,
                nextReviewDate = LocalDateTime.now().plusDays(newInterval.toLong()),
                lapses = if (quality < 3) card.lapses + 1 else card.lapses,
                lastReviewed = LocalDateTime.now()
            )

            cardDao.updateCard(updatedCard)

            when {
                quality >= 4 -> triggerHapticFeedback("success")
                quality == 3 -> triggerHapticFeedback("tap")
                else -> triggerHapticFeedback("error")
            }

            addTestResult("Review: ${card.front}", quality >= 3)
        }
    }

    fun shuffleCards() {
        triggerHapticFeedback("tap")
        addTestResult("Shuffle", true)
    }

    fun addTestResult(name: String, passed: Boolean) {
        val newResult = TestResult(name, passed)
        _testResults.update { it + (name to newResult) }
    }

    fun clearTestResults() {
        _testResults.value = emptyMap()
    }

    fun getTestStatistics(): TestStatistics {
        val results = _testResults.value.values
        val total = results.size
        val passed = results.count { it.passed }
        val rate = if (total > 0) (passed.toFloat() / total * 100) else 0f
        return TestStatistics(total, passed, total - passed, rate)
    }

    override fun onCleared() {
        super.onCleared()
        _shakeDetector?.stopListening()
    }
}

data class TestResult(val testName: String, val passed: Boolean, val timestamp: Long = System.currentTimeMillis())
data class TestStatistics(val totalTests: Int, val passedTests: Int, val failedTests: Int, val passRate: Float)

class CardViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = CardDatabase.getInstance(context)
        return CardViewModel(database, context) as T
    }
}