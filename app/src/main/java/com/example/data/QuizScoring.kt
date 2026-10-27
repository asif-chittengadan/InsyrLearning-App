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

/** Keeps the quiz timer consistent with the duration shown on test cards when older tests omit it. */
fun MockTest.effectiveDurationMinutes(): Int {
    if (durationMinutes > 0) return durationMinutes
    val type = "${contentType.trim()} ${type.trim()}".lowercase()
    return if (listOf("stream", "full", "entrance").any { marker -> type.contains(marker) }) 60 else 20
}

/** Removes tests that this student has already submitted, including legacy title-only attempts. */
fun List<MockTest>.excludingCompletedAttempts(attempts: List<TestAttempt>): List<MockTest> {
    val completedIds = attempts.mapNotNull { it.testId.trim().takeIf(String::isNotEmpty) }.toSet()
    val completedLegacyTitles = attempts
        .filter { it.testId.isBlank() }
        .mapNotNull { it.testTitle.trim().takeIf(String::isNotEmpty)?.lowercase() }
        .toSet()
    return filter { test ->
        test.id.trim() !in completedIds &&
            !(test.id.isBlank() && test.title.trim().lowercase() in completedLegacyTitles)
    }
}
