package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    data class Success(val profile: UserProfile) : AuthState
    data class Error(val message: String) : AuthState
}

data class QuizState(
    val activeTest: MockTest? = null,
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(), // QuestionIndex -> OptionIndex
    val isSubmitted: Boolean = false,
    val score: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val percentage: Double = 0.0,
    val isReviewMode: Boolean = false,
    val lastAttempt: TestAttempt? = null,
    val startTimeMillis: Long = 0L,
    val timeTakenSeconds: Long = 0L
)

class LearningViewModel(
    val repository: LearningRepository = AppLearningRepository()
) : ViewModel() {

    // Auth State
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    val currentUser: StateFlow<UserProfile?> = repository.currentUser

    // Loaded Data States
    private val _students = MutableStateFlow<List<UserProfile>>(emptyList())
    val students: StateFlow<List<UserProfile>> = _students.asStateFlow()

    private val _videos = MutableStateFlow<List<VideoContent>>(emptyList())
    val videos: StateFlow<List<VideoContent>> = _videos.asStateFlow()

    private val _materials = MutableStateFlow<List<StudyMaterial>>(emptyList())
    val materials: StateFlow<List<StudyMaterial>> = _materials.asStateFlow()

    private val _recordedClasses = MutableStateFlow<List<RecordedClass>>(emptyList())
    val recordedClasses: StateFlow<List<RecordedClass>> = _recordedClasses.asStateFlow()

    private val _mockTests = MutableStateFlow<List<MockTest>>(emptyList())
    val mockTests: StateFlow<List<MockTest>> = _mockTests.asStateFlow()

    private val _pyqs = MutableStateFlow<List<PYQ>>(emptyList())
    val pyqs: StateFlow<List<PYQ>> = _pyqs.asStateFlow()

    private val _feedbackList = MutableStateFlow<List<FeedbackEntry>>(emptyList())
    val feedbackList: StateFlow<List<FeedbackEntry>> = _feedbackList.asStateFlow()

    private val _testAttempts = MutableStateFlow<List<TestAttempt>>(emptyList())
    val testAttempts: StateFlow<List<TestAttempt>> = _testAttempts.asStateFlow()

    // Learning Activity Counters
    private val _videosWatched = MutableStateFlow(0)
    val videosWatched: StateFlow<Int> = _videosWatched.asStateFlow()

    private val _studyMaterialsOpened = MutableStateFlow(0)
    val studyMaterialsOpened: StateFlow<Int> = _studyMaterialsOpened.asStateFlow()

    private val _recordedClassesWatched = MutableStateFlow(0)
    val recordedClassesWatched: StateFlow<Int> = _recordedClassesWatched.asStateFlow()

    private val _pyqsViewed = MutableStateFlow(0)
    val pyqsViewed: StateFlow<Int> = _pyqsViewed.asStateFlow()

    fun trackVideoWatched() { _videosWatched.value += 1 }
    fun trackMaterialOpened() { _studyMaterialsOpened.value += 1 }
    fun trackRecordedClassWatched() { _recordedClassesWatched.value += 1 }
    fun trackPyqViewed() { _pyqsViewed.value += 1 }

    // Loading status for lists
    private val _isLoadingContent = MutableStateFlow(false)
    val isLoadingContent: StateFlow<Boolean> = _isLoadingContent.asStateFlow()

    // Active Quiz Play State
    private val _quizState = MutableStateFlow(QuizState())
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    // Message/Notification string
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        // Observe current user changes to trigger loading data automatically
        viewModelScope.launch {
            repository.currentUser.collect { user ->
                if (user != null) {
                    _authState.value = AuthState.Success(user)
                    loadContent()
                } else {
                    _authState.value = AuthState.Idle
                    clearLocalStates()
                }
            }
        }
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    fun restoreSession(context: android.content.Context) {
        viewModelScope.launch {
            repository.tryRestoreSession(context)
        }
    }

    private fun clearLocalStates() {
        _students.value = emptyList()
        _videos.value = emptyList()
        _materials.value = emptyList()
        _recordedClasses.value = emptyList()
        _mockTests.value = emptyList()
        _pyqs.value = emptyList()
        _feedbackList.value = emptyList()
        _quizState.value = QuizState()
    }

    fun toggleFirebase(enabled: Boolean) {
        repository.setFirebaseEnabled(enabled)
        showMessage(if (enabled) "Switched to Firebase Live Database" else "Switched to local offline sandbox")
    }

    // AUTH ACTION: Sign Up
    fun signUp(name: String, email: String, phone: String, stream: String, photoUrl: String, password: String) {
        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("All fields are required")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.signUp(name, email, phone, stream, "student", photoUrl, password)
                .onSuccess { profile ->
                    _authState.value = AuthState.Success(profile)
                    showMessage("Signed up successfully! Status: ${profile.status.uppercase()}")
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.localizedMessage ?: "Signup failed")
                }
        }
    }

    // AUTH ACTION: Login
    fun login(identifier: String, password: String) {
        if (identifier.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email/Mobile and Password are required")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.login(identifier, password)
                .onSuccess { profile ->
                    _authState.value = AuthState.Success(profile)
                    showMessage("Logged in successfully as ${profile.name}!")
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.localizedMessage ?: "Login failed. Check your credentials.")
                }
        }
    }

    // AUTH ACTION: Login with Google
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.loginWithGoogle(idToken)
                .onSuccess { profile ->
                    _authState.value = AuthState.Success(profile)
                    showMessage("Logged in successfully via Google as ${profile.name}!")
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.localizedMessage ?: "Google Sign-In failed.")
                }
        }
    }

    // AUTH ACTION: Logout
    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    // AUTH ACTION: Update User Profile
    fun updateUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateUserProfile(profile)
                .onSuccess { updatedProfile ->
                    _authState.value = AuthState.Success(updatedProfile)
                    showMessage("Profile updated successfully!")
                }
                .onFailure { error ->
                    showMessage("Failed to update profile: ${error.localizedMessage}")
                }
        }
    }

    // Content loading based on roles
    fun loadContent() {
        viewModelScope.launch {
            _isLoadingContent.value = true
            val currentUser = repository.currentUser.value ?: return@launch
            
            if (currentUser.isAdmin()) {
                // Admin loads everything
                _students.value = repository.getStudents()
                _videos.value = repository.getVideos()
                _materials.value = repository.getMaterials()
                _recordedClasses.value = repository.getRecordedClasses()
                _mockTests.value = repository.getMockTests()
                _pyqs.value = repository.getPYQs()
                _feedbackList.value = repository.getFeedback()
            } else if (currentUser.isApproved()) {
                // Automatically detect logged-in student's stream from Firestore
                val studentStream = currentUser.stream
                if (studentStream.isNotBlank()) {
                    _videos.value = repository.getVideos(stream = studentStream)
                    _materials.value = repository.getMaterials(stream = studentStream)
                    _recordedClasses.value = repository.getRecordedClasses(stream = studentStream)
                    _mockTests.value = repository.getMockTests(stream = studentStream)
                    _pyqs.value = repository.getPYQs(stream = studentStream)
                } else {
                    _videos.value = repository.getVideos()
                    _materials.value = repository.getMaterials()
                    _recordedClasses.value = repository.getRecordedClasses()
                    _mockTests.value = repository.getMockTests()
                    _pyqs.value = repository.getPYQs()
                }
                loadAttempts()
            }
            _isLoadingContent.value = false
        }
    }

    fun loadAttempts() {
        viewModelScope.launch {
            val uid = repository.currentUser.value?.uid ?: ""
            _testAttempts.value = repository.getTestAttempts(uid)
        }
    }

    // ADMIN ACTION: Update student status
    fun updateStudentStatus(studentUid: String, newStatus: String) {
        val currentUser = repository.currentUser.value
        if (currentUser == null || currentUser.role != "admin") {
            showMessage("Action rejected: Administrator permissions required.")
            return
        }
        viewModelScope.launch {
            repository.updateStudentStatus(studentUid, newStatus)
                .onSuccess {
                    showMessage("Student registration ${newStatus.uppercase()} successfully.")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to update status: ${error.localizedMessage}")
                }
        }
    }

    // ADMIN ACTION: Delete student
    fun deleteStudent(studentUid: String) {
        val currentUser = repository.currentUser.value
        if (currentUser == null || currentUser.role != "admin") {
            showMessage("Action rejected: Administrator permissions required.")
            return
        }
        viewModelScope.launch {
            repository.deleteStudent(studentUid)
                .onSuccess {
                    showMessage("Student removed successfully.")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to remove student: ${error.localizedMessage}")
                }
        }
    }

    // ADMIN ACTION: Update student details
    fun adminUpdateStudent(uid: String, name: String, phone: String, stream: String, status: String) {
        val currentUser = repository.currentUser.value
        if (currentUser == null || currentUser.role != "admin") {
            showMessage("Action rejected: Administrator permissions required.")
            return
        }
        viewModelScope.launch {
            repository.adminUpdateStudent(uid, name, phone, stream, status)
                .onSuccess {
                    showMessage("Student profile updated successfully by admin.")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to update student profile: ${error.localizedMessage}")
                }
        }
    }

    // ADMIN ACTION: Add video
    fun addVideo(title: String, description: String, url: String, category: String, stream: String, domain: String = "", thumbnailUrl: String, module: String = "", subject: String = "", contentType: String = "") {
        val currentUser = repository.currentUser.value
        if (currentUser == null || currentUser.role != "admin") {
            showMessage("Action rejected: Administrator permissions required.")
            return
        }
        if (title.isBlank() || url.isBlank()) {
            showMessage("Title and Video URL cannot be empty")
            return
        }
        viewModelScope.launch {
            repository.addVideo(title, description, url, category, stream, domain, thumbnailUrl, module, subject, contentType)
                .onSuccess {
                    showMessage("Video lecture uploaded successfully!")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to upload video: ${error.localizedMessage}")
                }
        }
    }

    // ADMIN ACTION: Add recorded class (Live Class)
    fun addRecordedClass(title: String, videoUrl: String, stream: String, domain: String = "", module: String = "", subject: String = "", contentType: String = "", description: String = "") {
        val currentUser = repository.currentUser.value
        if (currentUser == null || currentUser.role != "admin") {
            showMessage("Action rejected: Administrator permissions required.")
            return
        }
        if (title.isBlank() || videoUrl.isBlank()) {
            showMessage("Title and Live Class URL cannot be empty")
            return
        }
        viewModelScope.launch {
            repository.addRecordedClass(title, videoUrl, stream, domain, module, subject, contentType, description)
                .onSuccess {
                    showMessage("Live class uploaded successfully!")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to upload live class: ${error.localizedMessage}")
                }
        }
    }

    // ADMIN ACTION: Add material
    fun addMaterial(title: String, fileUrl: String, category: String, stream: String, domain: String = "", module: String = "", subject: String = "", contentType: String = "", description: String = "") {
        val currentUser = repository.currentUser.value
        if (currentUser == null || currentUser.role != "admin") {
            showMessage("Action rejected: Administrator permissions required.")
            return
        }
        if (title.isBlank() || fileUrl.isBlank()) {
            showMessage("Title and Material PDF link cannot be empty")
            return
        }
        viewModelScope.launch {
            repository.addMaterial(title, fileUrl, category, stream, domain, module, subject, contentType, description)
                .onSuccess {
                    showMessage("Study Material notes uploaded successfully!")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to upload material: ${error.localizedMessage}")
                }
        }
    }

    // ADMIN ACTION: Add Mock Test
    fun addMockTest(title: String, type: String, stream: String, domain: String = "", questions: List<MockQuestion>, module: String = "", subject: String = "", contentType: String = "", description: String = "", durationMinutes: Int = 0) {
        val currentUser = repository.currentUser.value
        if (currentUser == null || currentUser.role != "admin") {
            showMessage("Action rejected: Administrator permissions required.")
            return
        }
        if (title.isBlank()) {
            showMessage("Mock Test Title cannot be empty")
            return
        }
        if (questions.isEmpty()) {
            showMessage("Please add at least 1 question to the mock test")
            return
        }
        viewModelScope.launch {
            repository.addMockTest(title, type, stream, domain, questions, module, subject, contentType, description, durationMinutes = durationMinutes)
                .onSuccess {
                    showMessage("Mock Test created with ${questions.size} questions!")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to create mock test: ${error.localizedMessage}")
                }
        }
    }

    // ADMIN ACTION: Delete content
    fun editVideo(id: String, title: String, description: String, url: String, category: String, stream: String, domain: String = "", thumbnailUrl: String, module: String = "", subject: String = "", contentType: String = "") {
        val currentUser = repository.currentUser.value
        if (currentUser == null || currentUser.role != "admin") {
            showMessage("Action rejected: Administrator permissions required.")
            return
        }
        if (title.isBlank() || url.isBlank()) {
            showMessage("Title and Video URL cannot be empty")
            return
        }
        viewModelScope.launch {
            repository.editVideo(id, title, description, url, category, stream, domain, thumbnailUrl, module, subject, contentType)
                .onSuccess {
                    showMessage("Video lecture updated successfully!")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to update video: ${error.localizedMessage}")
                }
        }
    }

    // ADMIN ACTION: Edit recorded class (Live Class)
    fun editRecordedClass(id: String, title: String, videoUrl: String, stream: String, domain: String = "", module: String = "", subject: String = "", contentType: String = "", description: String = "") {
        val currentUser = repository.currentUser.value
        if (currentUser == null || currentUser.role != "admin") {
            showMessage("Action rejected: Administrator permissions required.")
            return
        }
        if (title.isBlank() || videoUrl.isBlank()) {
            showMessage("Title and Live Class URL cannot be empty")
            return
        }
        viewModelScope.launch {
            repository.editRecordedClass(id, title, videoUrl, stream, domain, module, subject, contentType, description)
                .onSuccess {
                    showMessage("Live class updated successfully!")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to update live class: ${error.localizedMessage}")
                }
        }
    }

    // ADMIN ACTION: Edit material
    fun editMaterial(id: String, title: String, fileUrl: String, category: String, stream: String, domain: String = "", module: String = "", subject: String = "", contentType: String = "", description: String = "") {
        val currentUser = repository.currentUser.value
        if (currentUser == null || currentUser.role != "admin") {
            showMessage("Action rejected: Administrator permissions required.")
            return
        }
        if (title.isBlank() || fileUrl.isBlank()) {
            showMessage("Title and Material PDF link cannot be empty")
            return
        }
        viewModelScope.launch {
            repository.editMaterial(id, title, fileUrl, category, stream, domain, module, subject, contentType, description)
                .onSuccess {
                    showMessage("Study Material notes updated successfully!")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to update material: ${error.localizedMessage}")
                }
        }
    }

    // ADMIN ACTION: Edit Mock Test
    fun editMockTest(id: String, title: String, type: String, stream: String, domain: String = "", questions: List<MockQuestion>, module: String = "", subject: String = "", contentType: String = "", description: String = "", durationMinutes: Int = 0) {
        val currentUser = repository.currentUser.value
        if (currentUser == null || currentUser.role != "admin") {
            showMessage("Action rejected: Administrator permissions required.")
            return
        }
        if (title.isBlank()) {
            showMessage("Mock Test Title cannot be empty")
            return
        }
        if (questions.isEmpty()) {
            showMessage("Please add at least 1 question to the mock test")
            return
        }
        viewModelScope.launch {
            repository.editMockTest(id, title, type, stream, domain, questions, module, subject, contentType, description, durationMinutes = durationMinutes)
                .onSuccess {
                    showMessage("Mock Test updated successfully!")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to update mock test: ${error.localizedMessage}")
                }
        }
    }

    // ADMIN ACTION: Add PYQ
    fun addPYQ(title: String, fileUrl: String, category: String, stream: String, domain: String = "", module: String = "", subject: String = "", contentType: String = "", description: String = "") {
        val currentUser = repository.currentUser.value
        if (currentUser == null || currentUser.role != "admin") {
            showMessage("Action rejected: Administrator permissions required.")
            return
        }
        if (title.isBlank() || fileUrl.isBlank()) {
            showMessage("Title and PYQ file link cannot be empty")
            return
        }
        viewModelScope.launch {
            repository.addPYQ(title, fileUrl, category, stream, domain, module, subject, contentType, description)
                .onSuccess {
                    showMessage("PYQ notes uploaded successfully!")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to upload PYQ: ${error.localizedMessage}")
                }
        }
    }

    // ADMIN ACTION: Edit PYQ
    fun editPYQ(id: String, title: String, fileUrl: String, category: String, stream: String, domain: String = "", module: String = "", subject: String = "", contentType: String = "", description: String = "") {
        val currentUser = repository.currentUser.value
        if (currentUser == null || currentUser.role != "admin") {
            showMessage("Action rejected: Administrator permissions required.")
            return
        }
        if (title.isBlank() || fileUrl.isBlank()) {
            showMessage("Title and PYQ file link cannot be empty")
            return
        }
        viewModelScope.launch {
            repository.editPYQ(id, title, fileUrl, category, stream, domain, module, subject, contentType, description)
                .onSuccess {
                    showMessage("PYQ notes updated successfully!")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to update PYQ: ${error.localizedMessage}")
                }
        }
    }

    fun deleteContent(type: String, id: String) {
        val currentUser = repository.currentUser.value
        if (currentUser == null || currentUser.role != "admin") {
            showMessage("Action rejected: Administrator permissions required.")
            return
        }
        viewModelScope.launch {
            repository.deleteContent(type, id)
                .onSuccess {
                    showMessage("Deleted item successfully")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to delete content: ${error.localizedMessage}")
                }
        }
    }

    // STUDENT ACTION: Submit Feedback
    fun submitFeedback(message: String) {
        if (message.isBlank()) {
            showMessage("Feedback message cannot be empty")
            return
        }
        viewModelScope.launch {
            repository.submitFeedback(message)
                .onSuccess {
                    showMessage("Feedback submitted to admin team. Thank you!")
                }
                .onFailure { error ->
                    showMessage("Failed to submit feedback: ${error.localizedMessage}")
                }
        }
    }

    // STUDENT QUIZ: Start a Test
    fun startQuiz(test: MockTest) {
        _quizState.value = QuizState(
            activeTest = test,
            currentQuestionIndex = 0,
            selectedAnswers = emptyMap(),
            isSubmitted = false,
            score = 0,
            correctCount = 0,
            wrongCount = 0,
            percentage = 0.0,
            isReviewMode = false,
            lastAttempt = null,
            startTimeMillis = System.currentTimeMillis(),
            timeTakenSeconds = 0L
        )
    }

    // STUDENT QUIZ: Select Answer
    fun selectQuizAnswer(questionIndex: Int, optionIndex: Int) {
        val current = _quizState.value
        if (current.isSubmitted || current.isReviewMode) return // disable changes after submit
        _quizState.value = current.copy(
            selectedAnswers = current.selectedAnswers + (questionIndex to optionIndex)
        )
    }

    // STUDENT QUIZ: Navigate question
    fun nextQuizQuestion() {
        val current = _quizState.value
        val total = current.activeTest?.questions?.size ?: 0
        if (current.currentQuestionIndex < total - 1) {
            _quizState.value = current.copy(currentQuestionIndex = current.currentQuestionIndex + 1)
        }
    }

    fun prevQuizQuestion() {
        val current = _quizState.value
        if (current.currentQuestionIndex > 0) {
            _quizState.value = current.copy(currentQuestionIndex = current.currentQuestionIndex - 1)
        }
    }

    fun jumpToQuestion(index: Int) {
        val current = _quizState.value
        val total = current.activeTest?.questions?.size ?: 0
        if (index in 0 until total) {
            _quizState.value = current.copy(currentQuestionIndex = index)
        }
    }

    fun setReviewMode(enabled: Boolean) {
        _quizState.value = _quizState.value.copy(isReviewMode = enabled)
    }

    // STUDENT QUIZ: Submit Test
    fun submitQuiz() {
        val current = _quizState.value
        val test = current.activeTest ?: return
        val user = currentUser.value
        var calculatedScore = 0
        var correctCount = 0
        var wrongCount = 0
        val totalQ = test.questions.size

        test.questions.forEachIndexed { idx, question ->
            val selected = current.selectedAnswers[idx]
            if (selected != null) {
                if (selected == question.correctAnswerIndex) {
                    correctCount++
                    calculatedScore += 4 // 4 marks per correct answer
                } else {
                    wrongCount++
                }
            }
        }

        val elapsedMs = if (current.startTimeMillis > 0) System.currentTimeMillis() - current.startTimeMillis else 0L
        val calculatedTimeTakenSec = (elapsedMs / 1000).coerceAtLeast(12L)

        val percentage = if (totalQ > 0) (correctCount.toDouble() / totalQ.toDouble()) * 100.0 else 0.0

        val attempt = TestAttempt(
            id = java.util.UUID.randomUUID().toString(),
            userId = user?.uid ?: "student_user",
            testId = test.id,
            testTitle = test.title,
            subject = test.subject.ifBlank { test.domain.ifBlank { test.stream } },
            score = calculatedScore,
            totalQuestions = totalQ,
            correctAnswers = correctCount,
            wrongAnswers = wrongCount,
            percentage = percentage,
            answers = current.selectedAnswers.mapKeys { it.key.toString() },
            submittedAt = com.google.firebase.Timestamp.now()
        )

        _quizState.value = current.copy(
            isSubmitted = true,
            score = calculatedScore,
            correctCount = correctCount,
            wrongCount = wrongCount,
            percentage = percentage,
            timeTakenSeconds = calculatedTimeTakenSec,
            lastAttempt = attempt,
            isReviewMode = false
        )

        viewModelScope.launch {
            repository.saveTestAttempt(attempt)
            loadAttempts()
        }

        showMessage("Exam Submitted! Correct: $correctCount, Wrong: $wrongCount, Score: $calculatedScore Marks (${String.format("%.1f", percentage)}%)")
    }

    // STUDENT QUIZ: Exit Quiz
    fun exitQuiz() {
        _quizState.value = QuizState()
    }
}
