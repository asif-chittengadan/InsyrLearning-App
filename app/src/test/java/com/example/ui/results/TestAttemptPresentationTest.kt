package com.example.ui.results

import com.example.data.MockTest
import com.example.data.TestAttempt
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Test

class TestAttemptPresentationTest {
    @Test
    fun groupsAttemptsBySubjectAndSortsNewestFirst() {
        val older = TestAttempt(
            id = "older",
            testId = "physics-test",
            testTitle = "Physics basics",
            subject = "Physics",
            submittedAt = Timestamp(100, 0)
        )
        val newer = TestAttempt(
            id = "newer",
            testId = "physics-test",
            testTitle = "Physics revision",
            subject = "Physics",
            submittedAt = Timestamp(200, 0)
        )
        val chemistry = TestAttempt(
            id = "chemistry",
            testId = "chemistry-test",
            testTitle = "Chemistry basics",
            subject = "Chemistry",
            submittedAt = Timestamp(150, 0)
        )

        val groups = groupTestAttemptsBySubject(
            attempts = listOf(older, chemistry, newer),
            tests = emptyList()
        )

        assertEquals(listOf("newer", "older"), groups["Physics"]?.map { it.id })
        assertEquals(listOf("chemistry"), groups["Chemistry"]?.map { it.id })
    }

    @Test
    fun mergesSubjectLabelsThatDifferByCaseAndWhitespace() {
        val newest = TestAttempt(id = "newest", subject = " Physics ", submittedAt = Timestamp(200, 0))
        val older = TestAttempt(id = "older", subject = "physics", submittedAt = Timestamp(100, 0))

        val groups = groupTestAttemptsBySubject(listOf(older, newest), emptyList())

        assertEquals(setOf("Physics"), groups.keys)
        assertEquals(listOf("newest", "older"), groups["Physics"]?.map { it.id })
    }

    @Test
    fun resolvesLegacyStreamSubjectFromTestMetadata() {
        val legacyAttempt = TestAttempt(testId = "test-1", subject = "Science")
        val test = MockTest(id = "test-1", subject = "Physics")

        val groups = groupTestAttemptsBySubject(listOf(legacyAttempt), listOf(test))

        assertEquals(listOf(legacyAttempt), groups["Physics"])
    }

    @Test
    fun derivesMaximumScoreForOlderAttemptsAndUsesSnapshotForNewAttempts() {
        val legacyAttempt = TestAttempt(totalQuestions = 5)
        val newAttempt = TestAttempt(totalQuestions = 5, maxScore = 25)

        assertEquals(20, legacyAttempt.maximumScoreForDisplay())
        assertEquals(25, newAttempt.maximumScoreForDisplay())
    }

    @Test
    fun keepsZeroMarksPercentageForNewNegativeMarkingAttempt() {
        val attempt = TestAttempt(
            totalQuestions = 5,
            correctAnswers = 1,
            maxScore = 25,
            percentage = 0.0
        )

        assertEquals(0.0, attempt.percentageForDisplay(), 0.001)
    }
}
