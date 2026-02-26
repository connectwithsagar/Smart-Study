package com.example.smartstudy.ui.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.smartstudy.R
import com.example.smartstudy.databinding.FragmentResultBinding

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScanViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        observeViewModel()

        binding.btnSummarize.setOnClickListener {
            val text = binding.etResult.text.toString()
            if (text.isNotBlank()) {
                viewModel.summarizeAndGenerateQuiz(text)
            } else {
                Toast.makeText(context, "No text to summarize", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.extractedText.observe(viewLifecycleOwner) { text ->
            if (binding.etResult.text.isNullOrBlank()) {
                binding.etResult.setText(text)
            }
        }

        viewModel.studySummary.observe(viewLifecycleOwner) { summary ->
            summary?.let {
                // Update the text field with the summary
                binding.etResult.setText(it.summarizedText)
                binding.btnSummarize.text = "Start Quiz"
                
                // Change button behavior to go to quiz after summarization is done
                binding.btnSummarize.setOnClickListener {
                    findNavController().navigate(R.id.action_resultFragment_to_quizFragment)
                }
                
                Toast.makeText(context, "Summarization Complete!", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSummarize.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}