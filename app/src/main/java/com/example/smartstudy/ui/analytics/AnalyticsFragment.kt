package com.example.smartstudy.ui.analytics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.smartstudy.data.local.AppDatabase
import com.example.smartstudy.databinding.FragmentAnalyticsBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AnalyticsFragment : Fragment() {
    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
    }

    private fun observeData() {
        val db = AppDatabase.getDatabase(requireContext())
        
        lifecycleScope.launch {
            db.noteDao().getAllNotes().collectLatest { notes ->
                if (notes.isNotEmpty()) {
                    val sessionCount = notes.size
                    val avgScore = notes.map { it.quizScore }.average().toInt()
                    
                    binding.tvSessionCount.text = sessionCount.toString()
                    binding.tvAvgScore.text = "$avgScore%"
                    
                    setupChart(notes.reversed().mapIndexed { index, note -> 
                        Entry(index.toFloat(), note.quizScore.toFloat()) 
                    })
                }
            }
        }
    }

    private fun setupChart(entries: List<Entry>) {
        val dataSet = LineDataSet(entries, "Quiz Scores").apply {
            color = Color.parseColor("#6366F1")
            setCircleColor(Color.parseColor("#6366F1"))
            lineWidth = 3f
            circleRadius = 5f
            setDrawCircleHole(false)
            valueTextSize = 10f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#6366F1")
            fillAlpha = 30
        }

        binding.lineChart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(true)
            axisRight.isEnabled = false
            animateX(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}