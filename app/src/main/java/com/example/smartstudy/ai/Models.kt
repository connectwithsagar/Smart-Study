package com.example.smartstudy.ai

import java.util.UUID

enum class Difficulty { EASY, MEDIUM, HARD }
enum class QuestionType { FILL_BLANK, MCQ, SHORT_ANSWER }

data class Question(
    val id: String = UUID.randomUUID().toString(),
    val type: QuestionType,
    val questionText: String,
    val correctAnswer: String,
    val options: List<String> = emptyList(),
    val difficulty: Difficulty,
    val weight: Int
)

data class StudySummary(
    val originalText: String,
    val summarizedText: String,
    val keywords: List<String>,
    val questions: List<Question>
)