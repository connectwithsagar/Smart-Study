package com.example.smartstudy.ui.scan

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartstudy.ai.GeminiBrain
import com.example.smartstudy.ai.StudySummary
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ScanViewModel : ViewModel() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val _extractedText = MutableLiveData<String?>()
    val extractedText: LiveData<String?> = _extractedText

    private val _studySummary = MutableLiveData<StudySummary?>()
    val studySummary: LiveData<StudySummary?> = _studySummary

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun processImage(bitmap: Bitmap) {
        _isLoading.value = true
        val image = InputImage.fromBitmap(bitmap, 0)

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    recognizer.process(image).await()
                }
                _extractedText.value = result.text
            } catch (e: Exception) {
                _error.value = "Failed to recognize text: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun summarizeAndGenerateQuiz(text: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val summary = GeminiBrain.processText(text)
                _studySummary.value = summary
            } catch (e: Exception) {
                _error.value = "AI Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResults() {
        _extractedText.value = null
        _studySummary.value = null
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        recognizer.close()
    }
}