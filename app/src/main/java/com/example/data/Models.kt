package com.example.data

import com.google.firebase.Timestamp

/**
 * Enumerations representing fixed domains of the platform
 */
enum class UserRole(val value: String) {
    ADMIN("admin"),
    STUDENT("student")
}

enum class UserStatus(val value: String) {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected")
}

enum class StreamCategory(val value: String) {
    SCIENCE("Science"),
    HUMANITIES("Humanities"),
    COMMERCE("Commerce")
}

enum class ContentCategory(val value: String) {
    GENERAL("General"),
    DOMAIN("Domain"),
    TEACHING_APTITUDE("TeachingAptitude")
}

enum class MockTestType(val value: String) {
    TOPIC_WISE("topicwise"),
    FULL_LENGTH("fulllength")
}

val ADMIN_EMAIL = "admin@insyrlearning.com"

/**
 * 1. Users collection
 * Path: /users/{uid}
 */
data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val phone: String = "", // compatible with previous phone
    val mobile: String = "", // specific mobile as requested
    val email: String = "",
    val password: String = "",
    val stream: String = "", // e.g., "Science", "Humanities", "Commerce"
    val photoUrl: String = "",
    val role: String = "student", // "admin" or "student"
    val status: String = "approved", // "pending", "approved", "rejected"
    val createdAt: Timestamp = Timestamp.now()
) {
    fun isApproved(): Boolean = status == UserStatus.APPROVED.value
    fun isAdmin(): Boolean = email.equals(ADMIN_EMAIL, ignoreCase = true)
    fun isStudent(): Boolean = !isAdmin()
}

/**
 * 2. Videos collection
 * Path: /videos/{videoId}
 */
data class VideoContent(
    val id: String = "", // Document ID
    val title: String = "",
    val description: String = "",
    val url: String = "",
    val category: String = "General", // "General", "Domain", "TeachingAptitude"
    val stream: String = "", // "Science", "Humanities", "Commerce", or "All"
    val thumbnailUrl: String = "",
    val uploadedBy: String = "", // Admin user ID
    val createdAt: Timestamp = Timestamp.now()
)

/**
 * RecordedClasses collection
 * Path: /recordedClasses/{classId}
 */
data class RecordedClass(
    val id: String = "", // Document ID
    val title: String = "",
    val videoUrl: String = "",
    val stream: String = "", // "Science", "Humanities", "Commerce"
    val uploadedBy: String = "", // Admin user ID
    val createdAt: Timestamp = Timestamp.now()
)

/**
 * 3. Materials collection
 * Path: /materials/{materialId}
 */
data class StudyMaterial(
    val id: String = "", // Document ID
    val title: String = "",
    val fileUrl: String = "",
    val category: String = "General", // "General", "Domain", "TeachingAptitude"
    val stream: String = "", // "Science", "Humanities", "Commerce", or "All"
    val uploadedBy: String = "", // Admin user ID
    val createdAt: Timestamp = Timestamp.now()
)

/**
 * Representing a question inside a Mock Test
 */
data class MockQuestion(
    val id: String = "",
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int = -1,
    val explanation: String = ""
)

/**
 * 4. MockTests collection
 * Path: /mockTests/{mockTestId}
 */
data class MockTest(
    val id: String = "", // Document ID
    val title: String = "",
    val type: String = "topicwise", // "topicwise" or "fulllength"
    val stream: String = "", // "Science", "Humanities", "Commerce" or "All"
    val questions: List<MockQuestion> = emptyList(),
    val createdAt: Timestamp = Timestamp.now()
)

/**
 * 5. Attempts collection
 * Path: /attempts/{attemptId}
 */
data class TestAttempt(
    val id: String = "", // Document ID
    val userId: String = "",
    val testId: String = "",
    val score: Int = 0,
    val answers: Map<String, Int> = emptyMap(),
    val submittedAt: Timestamp = Timestamp.now()
)

/**
 * 6. Feedback collection
 * Path: /feedback/{feedbackId}
 */
data class FeedbackEntry(
    val id: String = "", // Document ID
    val userId: String = "",
    val studentId: String = "", // kept for backward compatibility if needed
    val message: String = "",
    val createdAt: Timestamp = Timestamp.now()
)
