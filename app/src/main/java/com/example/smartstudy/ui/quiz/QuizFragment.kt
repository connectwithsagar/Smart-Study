package com.example.smartstudy.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.smartstudy.R
import com.example.smartstudy.data.local.AppDatabase
import com.example.smartstudy.data.local.NoteEntity
import com.example.smartstudy.databinding.FragmentQuizBinding
import com.example.smartstudy.ui.scan.ScanViewModel
import com.example.smartstudy.ai.Question
import com.example.smartstudy.ai.QuestionType
import com.google.android.material.button.MaterialButton
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScanViewModel by activityViewModels()
    private var questions = listOf<Question>()
    private var currentQuestionIndex = 0
    private var score = 0
    private var selectedOption: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get questions from the AI Summary result (com.example.smartstudy.ai.Question)
        val summary = viewModel.studySummary.value
        questions = summary?.questions ?: emptyList()

        if (questions.isEmpty()) {
            Toast.makeText(context, "No quiz available for this scan.", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        displayQuestion()

        binding.btnSubmit.setOnClickListener {
            checkAnswer()
        }
    }

    private fun displayQuestion() {
        selectedOption = null
        val q = questions[currentQuestionIndex]
        binding.tvQuestionCount.text = "Question ${currentQuestionIndex + 1}/${questions.size}"
        binding.quizProgress.progress = (((currentQuestionIndex + 1).toFloat() / questions.size) * 100).toInt()
        binding.tvQuestionText.text = q.questionText

        binding.optionsContainer.removeAllViews()
        when (q.type) {
            QuestionType.MCQ -> {
                binding.tilAnswer.visibility = View.GONE
                binding.optionsContainer.visibility = View.VISIBLE
                q.options.forEach { option ->
                    val btn = MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle)
                    btn.text = option
                    btn.isCheckable = true
                    btn.setOnClickListener { selectOption(btn) }
                    binding.optionsContainer.addView(btn)
                }
            }
            QuestionType.FILL_BLANK, QuestionType.SHORT_ANSWER -> {
                binding.optionsContainer.visibility = View.GONE
                binding.tilAnswer.visibility = View.VISIBLE
                binding.etAnswer.text?.clear()
                binding.tilAnswer.hint = if (q.type == QuestionType.FILL_BLANK) "Fill the blank" else "Your answer"
            }
        }
    }

    private fun selectOption(button: MaterialButton) {
        for (i in 0 until binding.optionsContainer.childCount) {
            val child = binding.optionsContainer.getChildAt(i) as MaterialButton
            child.isChecked = false
            child.setStrokeColorResource(R.color.outline)
        }
        button.isChecked = true
        button.setStrokeColorResource(R.color.primary)
        selectedOption = button.text.toString()
    }

    private fun checkAnswer() {
        val q = questions[currentQuestionIndex]
        val userAnswer = if (q.type == QuestionType.MCQ) {
            selectedOption ?: ""
        } else {
            binding.etAnswer.text.toString()
        }

        if (userAnswer.isBlank()) {
            Toast.makeText(context, "Please select or type an answer", Toast.LENGTH_SHORT).show()
            return
        }

        // Logic for SHORT_ANSWER is flexible for demo; MCQ and FILL_BLANK are exact.
        val isCorrect = if (q.type == QuestionType.SHORT_ANSWER) {
            userAnswer.length > 5 // Simple heuristic for hackathon
        } else {
            userAnswer.equals(q.correctAnswer, ignoreCase = true)
        }

        if (isCorrect) {
            score++
            Toast.makeText(context, "✨ Correct!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "❌ Wrong. Answer: ${q.correctAnswer}", Toast.LENGTH_SHORT).show()
        }

        currentQuestionIndex++
        if (currentQuestionIndex < questions.size) {
            displayQuestion()
        } else {
            saveAndFinish()
        }
    }

    private fun saveAndFinish() {
        val finalPercentage = (score.toFloat() / questions.size * 100).toInt()
        val summary = viewModel.studySummary.value
        
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val note = NoteEntity(
                rawText = summary?.originalText ?: "",
                summary = summary?.summarizedText ?: "",
                quizScore = finalPercentage,
                date = System.currentTimeMillis()
            )
            db.noteDao().insertNote(note)
            
            Toast.makeText(context, "Quiz Finished! Score: $finalPercentage%", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}