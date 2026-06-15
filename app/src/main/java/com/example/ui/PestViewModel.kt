package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val results: List<DetectionResult>) : UiState()
    data class Error(val message: String) : UiState()
}

class PestViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val database = AppDatabase.getDatabase(application)
    private val repository = HistoryRepository(database.historyDao())
    private val classifier = PestClassifier(application)

    val isModelLoaded: Boolean get() = classifier.isModelLoaded

    val historyList: StateFlow<List<HistoryEntry>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap.asStateFlow()

    fun selectImageUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val bitmap = loadBitmapFromUri(uri)
            if (bitmap != null) {
                _selectedBitmap.value = bitmap
                _uiState.value = UiState.Idle
            } else {
                _uiState.value = UiState.Error("خطا در بارگذاری تصویر. لطفا مجددا تلاش کنید.")
            }
        }
    }

    fun selectBitmap(bitmap: Bitmap) {
        _selectedBitmap.value = bitmap
        _uiState.value = UiState.Idle
    }

    fun runClassification() {
        val bitmap = _selectedBitmap.value
        if (bitmap == null) {
            _uiState.value = UiState.Error("لطفا ابتدا یک تصویر بارگذاری یا ثبت کنید.")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // Perform classification on IO thread
                val results = withContext(Dispatchers.IO) {
                    classifier.classifyImage(bitmap)
                }

                _uiState.value = UiState.Success(results)

                // Save to history if there is any result matching >75%
                if (results.isNotEmpty()) {
                    val topResult = results.first()
                    val savedImagePath = withContext(Dispatchers.IO) {
                        saveBitmapToInternalStorage(bitmap)
                    }

                    if (savedImagePath.isNotEmpty() && topResult.pest != null) {
                        val historyEntry = HistoryEntry(
                            pestIndex = topResult.pest.index,
                            pestName = topResult.pest.nameFa,
                            confidence = topResult.confidence,
                            localImageUri = savedImagePath
                        )
                        withContext(Dispatchers.IO) {
                            repository.insert(historyEntry)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PestViewModel", "Classification error", e)
                _uiState.value = UiState.Error("خطایی در اجرای پردازش تصویر رخ داد.")
            }
        }
    }

    fun deleteHistoryItem(id: Int, imagePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Delete record
            repository.deleteById(id)
            // Delete local file to save storage Space
            try {
                val file = File(imagePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                Log.e("PestViewModel", "Error deleting historic image file", e)
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            // Clear all files
            val filesDir = context.filesDir
            filesDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("pest_detect_") && file.name.endsWith(".jpg")) {
                    file.delete()
                }
            }
            repository.clearAll()
        }
    }

    private suspend fun loadBitmapFromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e("PestViewModel", "Error loading bitmap", e)
            null
        }
    }

    private fun saveBitmapToInternalStorage(bitmap: Bitmap): String {
        return try {
            val file = File(context.filesDir, "pest_detect_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.flush()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            Log.e("PestViewModel", "Error saving bitmap", e)
            ""
        }
    }
}
