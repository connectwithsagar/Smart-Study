package com.example.smartstudy.util

import java.util.*

object StudyEngine {

    private val stopWords = setOf(
        "the", "is", "at", "which", "on", "and", "a", "an", "of", "to", "in", "is", "it", "that", "with", "as", "for", "was", "were"
    )

    /**
     * PART 1: Smart Summarizer (Frequency-based)
     * 1. Tokenize into sentences and words.
     * 2. Calculate word frequency (excluding stop words).
     * 3. Score sentences based on the frequency of words they contain.
     * 4. Return top sentences.
     */
    fun summarize(text: String, sentenceCount: Int = 3): String {
        val sentences = text.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
        if (sentences.size <= sentenceCount) return text

        val wordFrequency = mutableMapOf<String, Int>()
        val words = text.lowercase(Locale.ROOT).split(Regex("\\W+")).filter { it.isNotBlank() && it !in stopWords }
        
        for (word in words) {
            wordFrequency[word] = wordFrequency.getOrDefault(word, 0) + 1
        }

        val sentenceScores = mutableMapOf<String, Int>()
        for (sentence in sentences) {
            val sentenceWords = sentence.lowercase(Locale.ROOT).split(Regex("\\W+"))
            var score = 0
            for (word in sentenceWords) {
                score += wordFrequency.getOrDefault(word, 0)
            }
            sentenceScores[sentence] = score
        }

        return sentenceScores.entries
            .sortedByDescending { it.value }
            .take(sentenceCount)
            .sortedBy { sentences.indexOf(it.key) } // Keep original order
            .joinToString(" ") { it.key }
    }

    /**
     * PART 2: Quiz Generator
     */
    fun generateQuiz(text: String): List<Question> {
        val sentences = text.split(Regex("(?<=[.!?])\\s+")).filter { it.length > 20 }
        val questions = mutableListOf<Question>()

        for (sentence in sentences.take(5)) {
            val words = sentence.split(Regex("\\W+")).filter { it.length > 5 && it.lowercase() !in stopWords }
            if (words.isNotEmpty()) {
                val answer = words.random()
                val type = if (Random().nextBoolean()) QuestionType.FILL_BLANK else QuestionType.MCQ
                
                val options = if (type == QuestionType.MCQ) {
                    val distractors = listOf("Concept", "Theory", "Process", "Example", "Method").filter { it != answer }.shuffled().take(3)
                    (distractors + answer).shuffled()
                } else emptyList()

                questions.add(Question(sentence.replace(answer, "_______", ignoreCase = true), answer, options, type))
            }
        }
        return questions
    }

    data class Question(
        val questionText: String,
        val correctAnswer: String,
        val options: List<String> = emptyList(),
        val type: QuestionType
    )

    enum class QuestionType { FILL_BLANK, MCQ }
}