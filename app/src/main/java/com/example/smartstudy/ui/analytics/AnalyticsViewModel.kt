package com.example.smartstudy.ui.analytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartstudy.data.repository.NoteRepository
import kotlinx.coroutines.launch

class AnalyticsViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _sessionCount = MutableLiveData<Int>()
    val sessionCount: LiveData<Int> = _sessionCount

    private val _avgScore = MutableLiveData<Float>()
    val avgScore: LiveData<Float> = _avgScore

    fun loadData() {
        viewModelScope.launch {
            _sessionCount.value = repository.getSessionCount()
            _avgScore.value = repository.getAverageScore()
        }
    }
}