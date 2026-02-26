package com.example.smartstudy.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.smartstudy.data.local.AppDatabase
import com.example.smartstudy.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeStats()
    }

    private fun observeStats() {
        val db = AppDatabase.getDatabase(requireContext())
        
        // Use Flow to observe real-time changes from the database
        lifecycleScope.launch {
            db.noteDao().getAllNotes().collectLatest { notes ->
                if (notes.isNotEmpty()) {
                    val sessionCount = notes.size
                    val avgScore = notes.map { it.quizScore }.average().toInt()
                    
                    binding.cardTitle.text = "Study Sessions: $sessionCount"
                    // Update your UI components with these stats
                    // (Assuming you have a TextView for these in fragment_home.xml)
                } else {
                    binding.cardTitle.text = "Start your first study session!"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}