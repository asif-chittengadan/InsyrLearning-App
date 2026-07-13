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
    val score: Int = 0
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

    private val _feedbackList = MutableStateFlow<List<FeedbackEntry>>(emptyList())
    val feedbackList: StateFlow<List<FeedbackEntry>> = _feedbackList.asStateFlow()

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
                _feedbackList.value = repository.getFeedback()
            } else if (currentUser.isApproved()) {
                // Approved student loads relevant content
                _videos.value = repository.getVideos()
                _materials.value = repository.getMaterials()
                _recordedClasses.value = repository.getRecordedClasses()
                _mockTests.value = repository.getMockTests()
            }
            _isLoadingContent.value = false
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
    fun addVideo(title: String, description: String, url: String, category: String, stream: String, thumbnailUrl: String) {
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
            repository.addVideo(title, description, url, category, stream, thumbnailUrl)
                .onSuccess {
                    showMessage("Video lecture uploaded successfully!")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to upload video: ${error.localizedMessage}")
                }
        }
    }

    // ADMIN ACTION: Add recorded class
    fun addRecordedClass(title: String, videoUrl: String, stream: String) {
        val currentUser = repository.currentUser.value
        if (currentUser == null || currentUser.role != "admin") {
            showMessage("Action rejected: Administrator permissions required.")
            return
        }
        if (title.isBlank() || videoUrl.isBlank()) {
            showMessage("Title and Video URL cannot be empty")
            return
        }
        viewModelScope.launch {
            repository.addRecordedClass(title, videoUrl, stream)
                .onSuccess {
                    showMessage("Recorded class uploaded successfully!")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to upload recorded class: ${error.localizedMessage}")
                }
        }
    }

    // ADMIN ACTION: Add material
    fun addMaterial(title: String, fileUrl: String, category: String, stream: String) {
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
            repository.addMaterial(title, fileUrl, category, stream)
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
    fun addMockTest(title: String, type: String, stream: String, questions: List<MockQuestion>) {
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
            repository.addMockTest(title, type, stream, questions)
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
    fun editVideo(id: String, title: String, description: String, url: String, category: String, stream: String, thumbnailUrl: String) {
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
            repository.editVideo(id, title, description, url, category, stream, thumbnailUrl)
                .onSuccess {
                    showMessage("Video lecture updated successfully!")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to update video: ${error.localizedMessage}")
                }
        }
    }

    // ADMIN ACTION: Edit recorded class
    fun editRecordedClass(id: String, title: String, videoUrl: String, stream: String) {
        val currentUser = repository.currentUser.value
        if (currentUser == null || currentUser.role != "admin") {
            showMessage("Action rejected: Administrator permissions required.")
            return
        }
        if (title.isBlank() || videoUrl.isBlank()) {
            showMessage("Title and Video URL cannot be empty")
            return
        }
        viewModelScope.launch {
            repository.editRecordedClass(id, title, videoUrl, stream)
                .onSuccess {
                    showMessage("Recorded class updated successfully!")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to update recorded class: ${error.localizedMessage}")
                }
        }
    }

    // ADMIN ACTION: Edit material
    fun editMaterial(id: String, title: String, fileUrl: String, category: String, stream: String) {
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
            repository.editMaterial(id, title, fileUrl, category, stream)
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
    fun editMockTest(id: String, title: String, type: String, stream: String, questions: List<MockQuestion>) {
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
            repository.editMockTest(id, title, type, stream, questions)
                .onSuccess {
                    showMessage("Mock Test updated successfully!")
                    loadContent()
                }
                .onFailure { error ->
                    showMessage("Failed to update mock test: ${error.localizedMessage}")
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
            score = 0
        )
    }

    // STUDENT QUIZ: Select Answer
    fun selectQuizAnswer(questionIndex: Int, optionIndex: Int) {
        val current = _quizState.value
        if (current.isSubmitted) return // disable changes after submit
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

    // STUDENT QUIZ: Submit Test
    fun submitQuiz() {
        val current = _quizState.value
        val test = current.activeTest ?: return
        var calculatedScore = 0
        test.questions.forEachIndexed { idx, question ->
            val selected = current.selectedAnswers[idx]
            if (selected == question.correctAnswerIndex) {
                calculatedScore++
            }
        }
        _quizState.value = current.copy(
            isSubmitted = true,
            score = calculatedScore
        )
        showMessage("Quiz finished! You scored $calculatedScore/${test.questions.size}")
    }

    // STUDENT QUIZ: Exit Quiz
    fun exitQuiz() {
        _quizState.value = QuizState()
    }
}
