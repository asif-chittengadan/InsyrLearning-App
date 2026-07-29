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
    COMMERCE("Commerce"),
    GENERAL("General")
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
    val exam: String = "CUET / NCET",
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
    val domain: String = "", // "Physics", "Chemistry", etc.
    val thumbnailUrl: String = "",
    val uploadedBy: String = "", // Admin user ID
    val createdAt: Timestamp = Timestamp.now(),
    val module: String = "",
    val subject: String = "",
    val contentType: String = "",
    val videoUrl: String = ""
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
    val domain: String = "", // "Physics", "Chemistry", etc.
    val uploadedBy: String = "", // Admin user ID
    val createdAt: Timestamp = Timestamp.now(),
    val module: String = "",
    val subject: String = "",
    val contentType: String = "",
    val description: String = ""
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
    val domain: String = "", // "Physics", "Chemistry", etc.
    val uploadedBy: String = "", // Admin user ID
    val createdAt: Timestamp = Timestamp.now(),
    val module: String = "",
    val subject: String = "",
    val contentType: String = "",
    val description: String = ""
)

/**
 * 3b. PYQs collection
 * Path: /pyqs/{pyqId}
 */
data class PYQ(
    val id: String = "", // Document ID
    val title: String = "",
    val fileUrl: String = "",
    val category: String = "General", // "General", "Domain", "TeachingAptitude"
    val stream: String = "", // "Science", "Humanities", "Commerce"
    val domain: String = "", // "Physics", "Chemistry", etc.
    val uploadedBy: String = "", // Admin user ID
    val createdAt: Timestamp = Timestamp.now(),
    val module: String = "",
    val subject: String = "",
    val contentType: String = "",
    val description: String = ""
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
    val domain: String = "", // "Physics", "Chemistry", etc.
    val questions: List<MockQuestion> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val module: String = "",
    val subject: String = "",
    val contentType: String = "",
    val description: String = "",
    val uploadedBy: String = "",
    val durationMinutes: Int = 0 // Time limit in minutes set by admin (0 = no limit)
)

val StreamDomains = mapOf(
    "Science" to listOf("Physics", "Chemistry", "Biology", "Mathematics"),
    "Humanities" to listOf("History", "Politics", "Sociology", "Economics"),
    "Commerce" to listOf("Accountancy", "Business Studies", "Political science", "Economics"),
    "General" to emptyList()
)

/**
 * 5. Attempts collection
 * Path: /attempts/{attemptId}
 */
data class TestAttempt(
    val id: String = "", // Document ID
    val userId: String = "",
    val testId: String = "",
    val testTitle: String = "",
    val subject: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val percentage: Double = 0.0,
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

/**
 * Ensures any Google Storage or gs:// URL is correctly formatted as a Firebase Storage Download URL
 */
fun ensureFirebaseDownloadUrl(url: String): String {
    val trimmed = url.trim()
    try {
        if (trimmed.startsWith("https://storage.googleapis.com/", ignoreCase = true)) {
            val withoutPrefix = trimmed.substring("https://storage.googleapis.com/".length)
            val parts = withoutPrefix.split("/", limit = 2)
            if (parts.size == 2) {
                val bucket = parts[0]
                val pathAndQuery = parts[1]
                
                // If there's a query string, separate it
                val queryIdx = pathAndQuery.indexOf('?')
                val path = if (queryIdx != -1) pathAndQuery.substring(0, queryIdx) else pathAndQuery
                
                val encodedPath = java.net.URLEncoder.encode(path, "UTF-8")
                    .replace("+", "%20")
                return "https://firebasestorage.googleapis.com/v0/b/$bucket/o/$encodedPath?alt=media"
            }
        }
        if (trimmed.startsWith("gs://", ignoreCase = true)) {
            val withoutPrefix = trimmed.substring("gs://".length)
            val parts = withoutPrefix.split("/", limit = 2)
            if (parts.size == 2) {
                val bucket = parts[0]
                val path = parts[1]
                val encodedPath = java.net.URLEncoder.encode(path, "UTF-8")
                    .replace("+", "%20")
                return "https://firebasestorage.googleapis.com/v0/b/$bucket/o/$encodedPath?alt=media"
            }
        }
        if (trimmed.startsWith("https://firebasestorage.googleapis.com/", ignoreCase = true)) {
            if (!trimmed.contains("alt=media", ignoreCase = true)) {
                return if (trimmed.contains("?")) {
                    "$trimmed&alt=media"
                } else {
                    "$trimmed?alt=media"
                }
            }
            return trimmed
        }
    } catch (e: Exception) {
        android.util.Log.e("ModelsHelper", "Error formatting download URL: ${e.message}", e)
    }
    return trimmed
}
