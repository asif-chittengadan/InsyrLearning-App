package com.example.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoContentFilterTest {
    private val scienceSubjects = listOf("Physics", "motion", "optics")

    @Test
    fun explicitPhysicsVideoOnlyAppearsInPhysicsForScienceStudents() {
        val video = VideoContent(
            stream = "Science",
            domain = "Physics",
            subject = "Physics",
            module = "Domain",
            category = "Domain"
        )

        assertEquals(
            listOf(video),
            filterVideosForStudentSubject(listOf(video), "Science", "Domain", "Physics", scienceSubjects)
        )
        assertTrue(
            filterVideosForStudentSubject(listOf(video), "Science", "Domain", "Chemistry", listOf("chemistry")).isEmpty()
        )
        assertTrue(
            filterVideosForStudentSubject(listOf(video), "Science", "Domain", "Biology", listOf("biology")).isEmpty()
        )
        assertTrue(
            filterVideosForStudentSubject(listOf(video), "Science", "Domain", "Mathematics", listOf("math")).isEmpty()
        )
    }

    @Test
    fun matchingIsCaseInsensitiveAndTrimsWhitespace() {
        val video = VideoContent(
            stream = " science ",
            domain = " physics ",
            subject = "PHYSICS "
        )

        assertEquals(
            listOf(video),
            filterVideosForStudentSubject(listOf(video), "Science", "Domain", "Physics", scienceSubjects)
        )
    }

    @Test
    fun videosFromAnotherStreamAreExcluded() {
        val video = VideoContent(stream = "Commerce", subject = "Physics", domain = "Physics")

        assertTrue(
            filterVideosForStudentSubject(listOf(video), "Science", "Domain", "Physics", scienceSubjects).isEmpty()
        )
    }

    @Test
    fun explicitlyGeneralVideoRemainsAvailableAcrossSubjectsInItsStream() {
        val video = VideoContent(stream = "Science", category = "General")

        assertEquals(
            listOf(video),
            filterVideosForStudentSubject(listOf(video), "Science", "Domain", "Physics", scienceSubjects)
        )
        assertEquals(
            listOf(video),
            filterVideosForStudentSubject(listOf(video), "Science", "Domain", "Chemistry", listOf("chemistry"))
        )
    }

    @Test
    fun explicitlyAllStreamGeneralVideoRemainsAvailableAcrossStreams() {
        val video = VideoContent(stream = "All", category = "General")

        assertEquals(
            listOf(video),
            filterVideosForStudentSubject(listOf(video), "Humanities", "Domain", "History", listOf("history"))
        )
    }

    @Test
    fun generalVideosWithoutModuleRemainAvailableForModuleQueries() {
        val video = VideoContent(category = "General")

        assertTrue(video.matchesModule("Domain"))
        assertTrue(video.matchesModule("Language"))
    }

    @Test
    fun domainSpecificVideoIsNotTreatedAsGeneralEvenWithGeneralCategory() {
        val video = VideoContent(
            stream = "Science",
            category = "General",
            domain = "Physics"
        )

        assertFalse(
            filterVideosForStudentSubject(listOf(video), "Science", "Domain", "Chemistry", listOf("chemistry")).contains(video)
        )
    }

    @Test
    fun unmatchedStreamVideoDoesNotFallBackIntoEverySubject() {
        val physicsVideo = VideoContent(
            title = "Mechanics",
            stream = "Science",
            domain = "Physics",
            subject = "Physics"
        )

        assertTrue(
            filterVideosForStudentSubject(
                listOf(physicsVideo), "Science", "Domain", "Chemistry", listOf("chemistry")
            ).isEmpty()
        )
    }

    @Test
    fun legacyPhysicsVideoDoesNotFallBackIntoChemistry() {
        val video = VideoContent(
            title = "Physics Mechanics",
            stream = "Science",
            module = "Domain",
            category = "Domain"
        )

        assertTrue(
            filterVideosForStudentSubject(
                videos = listOf(video),
                studentStream = "Science",
                moduleName = "Domain",
                subjectName = "Chemistry",
                subjectKeywords = listOf("chemistry", "organic", "chemical"),
                otherSubjectTerms = listOf("Physics", "motion", "optics", "Biology", "Mathematics")
            ).isEmpty()
        )
    }

    @Test
    fun subjectlessModuleVideoRemainsAvailableWithinItsModule() {
        val video = VideoContent(
            title = "Domain overview",
            stream = "Science",
            module = "Domain",
            category = "Domain"
        )

        assertEquals(
            listOf(video),
            filterVideosForStudentSubject(
                listOf(video), "Science", "Domain", "Physics", scienceSubjects,
                otherSubjectTerms = listOf("Chemistry", "Biology", "Mathematics")
            )
        )
        assertTrue(
            filterVideosForStudentSubject(
                listOf(video), "Science", "Language", "English", listOf("English"),
                otherSubjectTerms = listOf("Communication")
            ).isEmpty()
        )
    }
}
