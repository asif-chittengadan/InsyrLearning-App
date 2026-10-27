package com.example.data

import org.junit.Assert.assertEquals
import org.junit.Test

class QuizScoringTest {
    @Test
    fun appliesFiveForCorrectAndMinusOneForWrong() {
        val questions = listOf(
            MockQuestion(correctAnswerIndex = 1),
            MockQuestion(correctAnswerIndex = 0),
            MockQuestion(correctAnswerIndex = 2)
        )

        val result = calculateQuizScore(questions, mapOf(0 to 1, 1 to 3, 2 to 2))

        assertEquals(9, result.score)
        assertEquals(15, result.maxScore)
        assertEquals(2, result.correctAnswers)
        assertEquals(1, result.wrongAnswers)
        assertEquals(60.0, result.percentage, 0.001)
    }

    @Test
    fun unansweredQuestionsAreNotPenalized() {
        val questions = listOf(MockQuestion(correctAnswerIndex = 0), MockQuestion(correctAnswerIndex = 1))

        val result = calculateQuizScore(questions, mapOf(0 to 0))

        assertEquals(5, result.score)
        assertEquals(1, result.correctAnswers)
        assertEquals(0, result.wrongAnswers)
    }

    @Test
    fun negativeScoreProducesNegativeMarksPercentage() {
        val questions = listOf(MockQuestion(correctAnswerIndex = 0), MockQuestion(correctAnswerIndex = 1))

        val result = calculateQuizScore(questions, mapOf(0 to 2, 1 to 3))

        assertEquals(-2, result.score)
        assertEquals(-20.0, result.percentage, 0.001)
    }
}
