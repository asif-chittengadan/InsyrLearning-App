package com.example.data

data class QuizScore(
    val score: Int,
    val maxScore: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val percentage: Double
)

/** Applies the app's current test marking policy: +5 correct, -1 wrong, 0 unanswered. */
fun calculateQuizScore(
    questions: List<MockQuestion>,
    selectedAnswers: Map<Int, Int>
): QuizScore {
    var correct = 0
    var wrong = 0

    questions.forEachIndexed { index, question ->
        val selected = selectedAnswers[index] ?: return@forEachIndexed
        if (selected == question.correctAnswerIndex) correct++ else wrong++
    }

    val score = correct * 5 - wrong
    val maxScore = questions.size * 5
    val percentage = if (maxScore > 0) score * 100.0 / maxScore else 0.0
    return QuizScore(score, maxScore, correct, wrong, percentage)
}
