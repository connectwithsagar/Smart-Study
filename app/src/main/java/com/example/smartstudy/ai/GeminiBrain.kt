package com.example.smartstudy.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object GeminiBrain {

    private const val API_KEY = "AIzaSyCEM-dRZ3Yua4T6GliDrJFRzBpxT5rl-Y8"
    
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = API_KEY
    )

    suspend fun processText(text: String): StudySummary = withContext(Dispatchers.IO) {
        val prompt = """
            You are an expert study assistant. Analyze the following text and provide:
            1. A concise summary (3-5 sentences).
            2. Exactly 5 key terms/keywords.
            3. Exactly 5 quiz questions. Mix multiple choice and fill-in-the-blank.
            
            Return the result in this exact JSON format:
            {
              "summary": "...",
              "keywords": ["...", "..."],
              "questions": [
                {
                  "type": "MCQ",
                  "questionText": "...",
                  "correctAnswer": "...",
                  "options": ["...", "...", "...", "..."],
                  "difficulty": "MEDIUM"
                },
                {
                  "type": "FILL_BLANK",
                  "questionText": "...",
                  "correctAnswer": "...",
                  "difficulty": "EASY"
                }
              ]
            }
            
            Text to analyze:
            $text
        """.trimIndent()

        try {
            val response = model.generateContent(prompt)
            val jsonString = response.text?.replace("```json", "")?.replace("```", "")?.trim() ?: throw Exception("Empty response")
            val json = JSONObject(jsonString)
            
            val summaryText = json.getString("summary")
            val keywordsList = mutableListOf<String>()
            val keywordsArray = json.getJSONArray("keywords")
            for (i in 0 until keywordsArray.length()) {
                keywordsList.add(keywordsArray.getString(i))
            }

            val questionsList = mutableListOf<Question>()
            val questionsArray = json.getJSONArray("questions")
            for (i in 0 until questionsArray.length()) {
                val qJson = questionsArray.getJSONObject(i)
                val type = QuestionType.valueOf(qJson.getString("type"))
                val options = mutableListOf<String>()
                if (type == QuestionType.MCQ) {
                    val optArray = qJson.getJSONArray("options")
                    for (j in 0 until optArray.length()) {
                        options.add(optArray.getString(j))
                    }
                }
                
                questionsList.add(Question(
                    type = type,
                    questionText = qJson.getString("questionText"),
                    correctAnswer = qJson.getString("correctAnswer"),
                    options = options,
                    difficulty = Difficulty.valueOf(qJson.getString("difficulty")),
                    weight = if (type == QuestionType.MCQ) 10 else 5
                ))
            }

            StudySummary(
                originalText = text,
                summarizedText = summaryText,
                keywords = keywordsList,
                questions = questionsList
            )
        } catch (e: Exception) {
            // Fallback to local logic if cloud fails
            val localSummary = AIBrain.summarize(text)
            val localKeywords = AIBrain.extractKeywords(text)
            val localQuestions = AIBrain.generateQuiz(text, localKeywords)
            StudySummary(text, localSummary, localKeywords, localQuestions)
        }
    }
}