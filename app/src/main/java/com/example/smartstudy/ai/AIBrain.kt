package com.example.smartstudy.ai

import java.util.*
import kotlin.math.log10

object AIBrain {

    private val stopWords = setOf(
        "the", "and", "is", "of", "to", "in", "that", "it", "with", "as", "for", "was", "on", "are", 
        "by", "at", "be", "this", "from", "or", "had", "an", "they", "which", "you", "were", "has"
    )

    /**
     * PART 1: HYBRID SUMMARIZATION
     */
    fun summarize(text: String, title: String? = null, targetSentences: Int = 3): String {
        val sentences = text.split(Regex("(?<=[.!?])\\s+")).filter { it.length > 20 }
        if (sentences.size <= targetSentences) return text

        val wordFrequencies = mutableMapOf<String, Int>()
        val words = text.lowercase().split(Regex("\\W+")).filter { it !in stopWords && it.length > 2 }
        
        words.forEach { word -> wordFrequencies[word] = wordFrequencies.getOrDefault(word, 0) + 1 }

        val sentenceScores = mutableMapOf<Int, Double>()
        sentences.forEachIndexed { index, sentence ->
            var score = 0.0
            val sentenceWords = sentence.lowercase().split(Regex("\\W+"))
            
            sentenceWords.forEach { word ->
                score += wordFrequencies.getOrDefault(word, 0)
            }

            val positionWeight = when {
                index == 0 -> 2.0
                index == sentences.size - 1 -> 1.5
                else -> 1.0
            }
            score *= positionWeight

            title?.let {
                val titleWords = it.lowercase().split(Regex("\\W+")).toSet()
                val overlap = sentenceWords.intersect(titleWords).size
                score += (overlap * 5.0)
            }

            sentenceScores[index] = score / sentenceWords.size
        }

        return sentenceScores.entries
            .sortedByDescending { it.value }
            .take(targetSentences)
            .sortedBy { it.key }
            .joinToString(" ") { sentences[it.key] }
    }

    /**
     * PART 2: SMART KEYWORD EXTRACTION
     */
    fun extractKeywords(text: String): List<String> {
        val wordFrequencies = mutableMapOf<String, Int>()
        val words = text.split(Regex("\\s+"))
        
        words.forEach { rawWord ->
            val clean = rawWord.lowercase().replace(Regex("\\W"), "")
            if (clean.length > 4 && clean !in stopWords) {
                var weight = 1
                if (rawWord.isNotEmpty() && rawWord[0].isUpperCase()) weight = 2
                wordFrequencies[clean] = wordFrequencies.getOrDefault(clean, 0) + weight
            }
        }

        return wordFrequencies.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key }
    }

    /**
     * PART 3: CONTEXT-AWARE QUIZ GENERATOR
     */
    fun generateQuiz(text: String, keywords: List<String>): List<Question> {
        val sentences = text.split(Regex("(?<=[.!?])\\s+")).filter { it.length > 40 }
        val quiz = mutableListOf<Question>()

        sentences.forEach { sentence ->
            val foundKeyword = keywords.find { sentence.contains(it, ignoreCase = true) }
            
            if (foundKeyword != null && quiz.size < 10) {
                val rand = Random().nextInt(3)
                when (rand) {
                    0 -> quiz.add(createMCQ(sentence, foundKeyword, keywords))
                    1 -> quiz.add(createFillBlank(sentence, foundKeyword))
                    2 -> quiz.add(createConceptual(sentence))
                }
            }
        }
        return quiz.shuffled()
    }

    private fun createMCQ(sentence: String, answer: String, allKeywords: List<String>): Question {
        val distractors = allKeywords.filter { it != answer }.shuffled().take(3).toMutableList()
        while (distractors.size < 3) distractors.add("Concept ${distractors.size}")
        
        return Question(
            type = QuestionType.MCQ,
            questionText = sentence.replace(answer, "_____", ignoreCase = true),
            correctAnswer = answer,
            options = (distractors + answer).shuffled(),
            difficulty = Difficulty.MEDIUM,
            weight = 10
        )
    }

    private fun createFillBlank(sentence: String, answer: String): Question {
        return Question(
            type = QuestionType.FILL_BLANK,
            questionText = sentence.replace(answer, "_____", ignoreCase = true),
            correctAnswer = answer,
            difficulty = Difficulty.EASY,
            weight = 5
        )
    }

    private fun createConceptual(sentence: String): Question {
        return Question(
            type = QuestionType.SHORT_ANSWER,
            questionText = "Explain the context of: \"$sentence\"",
            correctAnswer = "Analysis required",
            difficulty = Difficulty.HARD,
            weight = 20
        )
    }

    /**
     * PART 4: SMART SCORING SYSTEM
     */
    fun calculateScore(isCorrect: Boolean, difficulty: Difficulty, timeTakenSeconds: Int, maxTimeSeconds: Int = 30): Int {
        if (!isCorrect) return 0
        val basePoints = when(difficulty) {
            Difficulty.EASY -> 10
            Difficulty.MEDIUM -> 20
            Difficulty.HARD -> 40
        }
        val timeBonus = if (timeTakenSeconds < maxTimeSeconds) {
            val ratio = (maxTimeSeconds - timeTakenSeconds).toDouble() / maxTimeSeconds
            (basePoints * 0.5 * ratio).toInt()
        } else 0
        return basePoints + timeBonus
    }
}