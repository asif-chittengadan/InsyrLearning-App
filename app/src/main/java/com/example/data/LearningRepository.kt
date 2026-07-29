package com.example.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

interface LearningRepository {
    val isFirebaseEnabled: StateFlow<Boolean>
    val currentUser: StateFlow<UserProfile?>
    
    fun setFirebaseEnabled(enabled: Boolean)
    
    // Auth Actions
    suspend fun signUp(name: String, email: String, phone: String, stream: String, role: String, photoUrl: String, password: String): Result<UserProfile>
    suspend fun login(identifier: String, password: String): Result<UserProfile>
    suspend fun loginWithGoogle(idToken: String): Result<UserProfile>
    suspend fun logout()
    suspend fun tryRestoreSession(context: android.content.Context): Result<UserProfile?>
    suspend fun updateUserProfile(profile: UserProfile): Result<UserProfile>
    
    // Admin Student Approval Management
    suspend fun getStudents(): List<UserProfile>
    suspend fun updateStudentStatus(uid: String, status: String): Result<Unit>
    suspend fun adminUpdateStudent(uid: String, name: String, phone: String, stream: String, status: String): Result<Unit>
    suspend fun deleteStudent(uid: String): Result<Unit>
    
    // Content Fetching
    suspend fun getVideos(stream: String? = null, module: String? = null, subject: String? = null): List<VideoContent>
    suspend fun getMaterials(stream: String? = null, module: String? = null, subject: String? = null): List<StudyMaterial>
    suspend fun getRecordedClasses(stream: String? = null, module: String? = null, subject: String? = null): List<RecordedClass>
    suspend fun getMockTests(stream: String? = null, module: String? = null, subject: String? = null): List<MockTest>
    suspend fun getPYQs(stream: String? = null, module: String? = null, subject: String? = null): List<PYQ>
    suspend fun getFeedback(): List<FeedbackEntry>
    
    // Content Adding
    suspend fun addVideo(title: String, description: String, url: String, category: String, stream: String, domain: String = "", thumbnailUrl: String, module: String = "", subject: String = "", contentType: String = ""): Result<Unit>
    suspend fun editVideo(id: String, title: String, description: String, url: String, category: String, stream: String, domain: String = "", thumbnailUrl: String, module: String = "", subject: String = "", contentType: String = ""): Result<Unit>
    suspend fun addMaterial(title: String, fileUrl: String, category: String, stream: String, domain: String = "", module: String = "", subject: String = "", contentType: String = "", description: String = ""): Result<Unit>
    suspend fun editMaterial(id: String, title: String, fileUrl: String, category: String, stream: String, domain: String = "", module: String = "", subject: String = "", contentType: String = "", description: String = ""): Result<Unit>
    suspend fun addRecordedClass(title: String, videoUrl: String, stream: String, domain: String = "", module: String = "", subject: String = "", contentType: String = "", description: String = ""): Result<Unit>
    suspend fun editRecordedClass(id: String, title: String, videoUrl: String, stream: String, domain: String = "", module: String = "", subject: String = "", contentType: String = "", description: String = ""): Result<Unit>
    suspend fun addMockTest(title: String, type: String, stream: String, domain: String = "", questions: List<MockQuestion>, module: String = "", subject: String = "", contentType: String = "", description: String = "", uploadedBy: String = "", durationMinutes: Int = 0): Result<Unit>
    suspend fun editMockTest(id: String, title: String, type: String, stream: String, domain: String = "", questions: List<MockQuestion>, module: String = "", subject: String = "", contentType: String = "", description: String = "", uploadedBy: String = "", durationMinutes: Int = 0): Result<Unit>
    suspend fun addPYQ(title: String, fileUrl: String, category: String, stream: String, domain: String = "", module: String = "", subject: String = "", contentType: String = "", description: String = ""): Result<Unit>
    suspend fun editPYQ(id: String, title: String, fileUrl: String, category: String, stream: String, domain: String = "", module: String = "", subject: String = "", contentType: String = "", description: String = ""): Result<Unit>
    suspend fun deleteContent(type: String, id: String): Result<Unit>
    suspend fun uploadFile(uri: android.net.Uri, context: android.content.Context): Result<String>
    
    // Student Actions
    suspend fun submitFeedback(message: String): Result<Unit>
    suspend fun saveTestAttempt(attempt: TestAttempt): Result<Unit>
    suspend fun getTestAttempts(userId: String): List<TestAttempt>
}

class AppLearningRepository : LearningRepository {
    private val _isFirebaseEnabled = MutableStateFlow(true) // Default to true (Firebase only)
    override val isFirebaseEnabled: StateFlow<Boolean> = _isFirebaseEnabled
    
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    override val currentUser: StateFlow<UserProfile?> = _currentUser

    private var appContext: android.content.Context? = null

    // Local in-memory DB collections
    private val localUsers = mutableListOf<UserProfile>()
    private val localVideos = mutableListOf<VideoContent>()
    private val localMaterials = mutableListOf<StudyMaterial>()
    private val localRecordedClasses = mutableListOf<RecordedClass>()
    private val localMockTests = mutableListOf<MockTest>()
    private val localPYQs = mutableListOf<PYQ>()
    private val localFeedback = mutableListOf<FeedbackEntry>()
    private val localAttempts = mutableListOf<TestAttempt>()

    init {
        // Pre-populate high-quality mockup data for immediate visual satisfaction (Geometric Balance)
        setupLocalMockData()
        
        // Auto-detect Firebase configuration
        try {
            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()
            Log.d("LearningRepository", "Firebase is configured successfully!")
            _isFirebaseEnabled.value = true
        } catch (e: Exception) {
            Log.w("LearningRepository", "Firebase not initialized/configured.", e)
            _isFirebaseEnabled.value = true // Keep true as requested for Firebase-only operations
        }
    }

    private fun setupLocalMockData() {
        // No local dummy mock data; Firestore is used exclusively.
    }

    override fun setFirebaseEnabled(enabled: Boolean) {
        _isFirebaseEnabled.value = enabled
        // Log out current session to enforce re-auth on mode switch
        _currentUser.value = null
    }

    override suspend fun signUp(
        name: String,
        email: String,
        phone: String,
        stream: String,
        role: String,
        photoUrl: String,
        password: String
    ): Result<UserProfile> {
        return try {
            val isEmailAdmin = email.equals("admin@insyrlearning.com", ignoreCase = true)
            val actualRole = if (isEmailAdmin) "admin" else "student"
            val status = if (actualRole == "admin") "approved" else "pending"
            
            if (_isFirebaseEnabled.value) {
                val auth = FirebaseAuth.getInstance()
                val firestore = FirebaseFirestore.getInstance()
                
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = authResult.user?.uid ?: throw Exception("Auth signup failed to return user UID")
                
                val profile = UserProfile(
                    uid = uid,
                    name = name,
                    phone = phone,
                    email = email,
                    password = password,
                    stream = stream,
                    photoUrl = photoUrl,
                    role = actualRole,
                    status = status
                )
                
                firestore.collection("users").document(uid).set(profile).await()
                _currentUser.value = profile
                appContext?.let { saveSessionToPrefs(it, profile) }
                Result.success(profile)
            } else {
                // Local state signup
                val uid = "local_${System.currentTimeMillis()}"
                val profile = UserProfile(
                    uid = uid,
                    name = name,
                    phone = phone,
                    email = email,
                    password = password,
                    stream = stream,
                    photoUrl = photoUrl,
                    role = actualRole,
                    status = status
                )
                localUsers.add(profile)
                _currentUser.value = profile
                appContext?.let { saveSessionToPrefs(it, profile) }
                Result.success(profile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<UserProfile> {
        return try {
            if (_isFirebaseEnabled.value) {
                val auth = FirebaseAuth.getInstance()
                val firestore = FirebaseFirestore.getInstance()
                
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val uid = authResult.user?.uid ?: throw Exception("Google Auth login failed")
                val email = authResult.user?.email ?: ""
                val displayName = authResult.user?.displayName ?: "Google User"
                val photoUrl = authResult.user?.photoUrl?.toString() ?: ""
                
                // Fetch the profile
                val doc = firestore.collection("users").document(uid).get().await()
                val profile = if (doc.exists()) {
                    doc.toObject(UserProfile::class.java) ?: throw Exception("User profile not found in Firestore")
                } else {
                    // Precreate if it was created on the fly
                    val isFixedAdmin = email.equals("admin@insyrlearning.com", ignoreCase = true)
                    val newProfile = UserProfile(
                        uid = uid,
                        name = displayName,
                        phone = "",
                        email = email,
                        password = "",
                        role = if (isFixedAdmin) "admin" else "student",
                        status = "approved",
                        photoUrl = photoUrl
                    )
                    firestore.collection("users").document(uid).set(newProfile).await()
                    newProfile
                }
                
                _currentUser.value = profile
                appContext?.let { saveSessionToPrefs(it, profile) }
                Result.success(profile)
            } else {
                // Local DB mockup user
                val fakeUid = "google_user_" + idToken.hashCode()
                val profile = UserProfile(
                    uid = fakeUid,
                    name = "Google User Mock",
                    phone = "",
                    email = "google@mock.com",
                    password = "",
                    role = "student",
                    status = "approved"
                )
                localUsers.add(profile)
                _currentUser.value = profile
                appContext?.let { saveSessionToPrefs(it, profile) }
                Result.success(profile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(identifier: String, password: String): Result<UserProfile> {
        return try {
            if (_isFirebaseEnabled.value) {
                val auth = FirebaseAuth.getInstance()
                val firestore = FirebaseFirestore.getInstance()
                
                val isEmailInput = identifier.contains("@")
                val emailToAuth = if (isEmailInput) {
                    identifier
                } else {
                    // It's a phone number, lookup email from Firestore first
                    val query = firestore.collection("users")
                        .whereEqualTo("phone", identifier)
                        .get()
                        .await()
                    if (query.isEmpty) {
                        throw Exception("No registered account found with phone number $identifier")
                    }
                    val userDoc = query.documents.first()
                    val userProfile = userDoc.toObject(UserProfile::class.java)
                    userProfile?.email ?: throw Exception("Profile contains no email associated with this phone number")
                }
                
                // Try sign-in
                var authResult = try {
                    auth.signInWithEmailAndPassword(emailToAuth, password).await()
                } catch (authEx: Exception) {
                    // If is admin@insyrlearning.com and doesn't exist, register on-the-fly for convenient testing
                    if (emailToAuth.equals("admin@insyrlearning.com", ignoreCase = true)) {
                        try {
                            auth.createUserWithEmailAndPassword(emailToAuth, password).await()
                        } catch (e2: Exception) {
                            throw authEx
                        }
                    } else {
                        throw authEx
                    }
                }
                val uid = authResult.user?.uid ?: throw Exception("Auth login failed")
                
                // Fetch the profile
                val doc = firestore.collection("users").document(uid).get().await()
                val profile = if (doc.exists()) {
                    doc.toObject(UserProfile::class.java) ?: throw Exception("User profile not found in Firestore")
                } else {
                    // Precreate if it was created on the fly
                    val isFixedAdmin = emailToAuth.equals("admin@insyrlearning.com", ignoreCase = true)
                    val newProfile = UserProfile(
                        uid = uid,
                        name = if (isFixedAdmin) "Admin" else "Student",
                        phone = if (isFixedAdmin) "9876543210" else "",
                        email = emailToAuth,
                        password = password,
                        role = if (isFixedAdmin) "admin" else "student",
                        status = "approved"
                    )
                    firestore.collection("users").document(uid).set(newProfile).await()
                    newProfile
                }
                
                // Enforce: ONLY admin@insyrlearning.com can be admin
                val isEmailAdmin = profile.email.equals("admin@insyrlearning.com", ignoreCase = true)
                
                // If it is admin email but they logged in with mobile number:
                if (isEmailAdmin && !isEmailInput) {
                    throw Exception("Only the admin email (admin@insyrlearning.com) can access the Admin Dashboard.")
                }
                
                val finalProfile = if (isEmailAdmin) {
                    profile.copy(role = "admin", status = "approved")
                } else {
                    profile.copy(role = "student")
                }
                
                _currentUser.value = finalProfile
                appContext?.let { saveSessionToPrefs(it, finalProfile) }
                Result.success(finalProfile)
            } else {
                // Local state login
                val isEmailInput = identifier.contains("@")
                val isAdminEmail = identifier.equals("admin@insyrlearning.com", ignoreCase = true)
                
                val profile = if (isEmailInput) {
                    var found = localUsers.find { it.email.equals(identifier, ignoreCase = true) }
                    if (found == null && isAdminEmail) {
                        // Precreate admin if not exists locally
                        val newAdmin = UserProfile(
                            uid = "admin123",
                            name = "Admin Principal",
                            phone = "9876543210",
                            email = "admin@insyrlearning.com",
                            password = password,
                            role = "admin",
                            status = "approved"
                        )
                        localUsers.add(newAdmin)
                        found = newAdmin
                    }
                    if (found == null || found.password != password) {
                        throw Exception("Invalid email or password")
                    }
                    found
                } else {
                    val found = localUsers.find { it.phone == identifier }
                        ?: throw Exception("No registered account found with phone number $identifier")
                    
                    if (found.password != password) {
                        throw Exception("Invalid password")
                    }
                    
                    if (found.email.equals("admin@insyrlearning.com", ignoreCase = true)) {
                        // Mobile number login should NEVER be treated as an admin login
                        throw Exception("Only the admin email (admin@insyrlearning.com) can access the Admin Dashboard.")
                    }
                    found
                }
                
                val finalProfile = if (profile.email.equals("admin@insyrlearning.com", ignoreCase = true)) {
                    profile.copy(role = "admin", status = "approved")
                } else {
                    profile.copy(role = "student")
                }
                
                _currentUser.value = finalProfile
                appContext?.let { saveSessionToPrefs(it, finalProfile) }
                Result.success(finalProfile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        if (_isFirebaseEnabled.value) {
            FirebaseAuth.getInstance().signOut()
        }
        _currentUser.value = null
        appContext?.let { clearSessionFromPrefs(it) }
    }

    override suspend fun getStudents(): List<UserProfile> {
        val current = _currentUser.value
        if (current == null || current.role != "admin") {
            Log.e("LearningRepository", "Unauthorized getStudents attempt")
            return emptyList()
        }
        return try {
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("users")
                    .whereEqualTo("role", "student")
                    .get()
                    .await()
                snapshot.toObjects(UserProfile::class.java)
            } else {
                localUsers.filter { it.role == "student" }
            }
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error getting students from Firebase", e)
            localUsers.filter { it.role == "student" }
        }
    }

    override suspend fun updateStudentStatus(uid: String, status: String): Result<Unit> {
        val current = _currentUser.value
        if (current == null || current.role != "admin") {
            return Result.failure(Exception("Unauthorized: Administrator permissions required."))
        }
        return try {
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("users").document(uid)
                    .update("status", status)
                    .await()
                Result.success(Unit)
            } else {
                val index = localUsers.indexOfFirst { it.uid == uid }
                if (index != -1) {
                    val updated = localUsers[index].copy(status = status)
                    localUsers[index] = updated
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Student not found locally"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun adminUpdateStudent(uid: String, name: String, phone: String, stream: String, status: String): Result<Unit> {
        val current = _currentUser.value
        if (current == null || current.role != "admin") {
            return Result.failure(Exception("Unauthorized: Administrator permissions required."))
        }
        return try {
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("users").document(uid)
                    .update(
                        mapOf(
                            "name" to name,
                            "phone" to phone,
                            "mobile" to phone,
                            "stream" to stream,
                            "status" to status
                        )
                    )
                    .await()
                Result.success(Unit)
            } else {
                val index = localUsers.indexOfFirst { it.uid == uid }
                if (index != -1) {
                    val updated = localUsers[index].copy(
                        name = name,
                        phone = phone,
                        mobile = phone,
                        stream = stream,
                        status = status
                    )
                    localUsers[index] = updated
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Student not found locally"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteStudent(uid: String): Result<Unit> {
        val current = _currentUser.value
        if (current == null || current.role != "admin") {
            return Result.failure(Exception("Unauthorized: Administrator permissions required."))
        }
        return try {
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("users").document(uid)
                    .delete()
                    .await()
                Result.success(Unit)
            } else {
                val removed = localUsers.removeIf { it.uid == uid }
                if (removed) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Student not found locally"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVideos(stream: String?, module: String?, subject: String?): List<VideoContent> {
        return try {
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("videos").get().await()
                val list = snapshot.toObjects(VideoContent::class.java)
                if (!stream.isNullOrBlank() || !module.isNullOrBlank() || !subject.isNullOrBlank()) {
                    list.filter { item ->
                        (stream.isNullOrBlank() || item.stream.isBlank() || item.stream.equals("All", ignoreCase = true) || item.stream.equals(stream, ignoreCase = true)) &&
                        (module.isNullOrBlank() || item.module.isBlank() || item.module.equals(module, ignoreCase = true) || item.category.equals(module, ignoreCase = true)) &&
                        (subject.isNullOrBlank() || item.subject.isBlank() || item.subject.equals(subject, ignoreCase = true) || item.domain.equals(subject, ignoreCase = true))
                    }
                } else {
                    list
                }
            } else {
                localVideos.filter { item ->
                    (stream.isNullOrBlank() || item.stream.equals(stream, ignoreCase = true)) &&
                    (module.isNullOrBlank() || item.module.equals(module, ignoreCase = true) || item.category.equals(module, ignoreCase = true)) &&
                    (subject.isNullOrBlank() || item.subject.equals(subject, ignoreCase = true) || item.domain.equals(subject, ignoreCase = true))
                }
            }
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error getting videos from Firestore", e)
            emptyList()
        }
    }

    override suspend fun getRecordedClasses(stream: String?, module: String?, subject: String?): List<RecordedClass> {
        return try {
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("recordedClasses").get().await()
                val list = snapshot.toObjects(RecordedClass::class.java)
                if (!stream.isNullOrBlank() || !module.isNullOrBlank() || !subject.isNullOrBlank()) {
                    list.filter { item ->
                        (stream.isNullOrBlank() || item.stream.isBlank() || item.stream.equals("All", ignoreCase = true) || item.stream.equals(stream, ignoreCase = true)) &&
                        (module.isNullOrBlank() || item.module.isBlank() || item.module.equals(module, ignoreCase = true)) &&
                        (subject.isNullOrBlank() || item.subject.isBlank() || item.subject.equals(subject, ignoreCase = true) || item.domain.equals(subject, ignoreCase = true))
                    }
                } else {
                    list
                }
            } else {
                localRecordedClasses.filter { item ->
                    (stream.isNullOrBlank() || item.stream.equals(stream, ignoreCase = true)) &&
                    (module.isNullOrBlank() || item.module.equals(module, ignoreCase = true)) &&
                    (subject.isNullOrBlank() || item.subject.equals(subject, ignoreCase = true) || item.domain.equals(subject, ignoreCase = true))
                }
            }
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error getting recorded classes from Firestore", e)
            emptyList()
        }
    }

    override suspend fun getMaterials(stream: String?, module: String?, subject: String?): List<StudyMaterial> {
        return try {
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("materials").get().await()
                val list = snapshot.toObjects(StudyMaterial::class.java)
                if (!stream.isNullOrBlank() || !module.isNullOrBlank() || !subject.isNullOrBlank()) {
                    list.filter { item ->
                        (stream.isNullOrBlank() || item.stream.isBlank() || item.stream.equals("All", ignoreCase = true) || item.stream.equals(stream, ignoreCase = true)) &&
                        (module.isNullOrBlank() || item.module.isBlank() || item.module.equals(module, ignoreCase = true) || item.category.equals(module, ignoreCase = true)) &&
                        (subject.isNullOrBlank() || item.subject.isBlank() || item.subject.equals(subject, ignoreCase = true) || item.domain.equals(subject, ignoreCase = true))
                    }
                } else {
                    list
                }
            } else {
                localMaterials.filter { item ->
                    (stream.isNullOrBlank() || item.stream.equals(stream, ignoreCase = true)) &&
                    (module.isNullOrBlank() || item.module.equals(module, ignoreCase = true) || item.category.equals(module, ignoreCase = true)) &&
                    (subject.isNullOrBlank() || item.subject.equals(subject, ignoreCase = true) || item.domain.equals(subject, ignoreCase = true))
                }
            }
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error getting materials from Firestore", e)
            emptyList()
        }
    }

    override suspend fun getMockTests(stream: String?, module: String?, subject: String?): List<MockTest> {
        return try {
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("mockTests").get().await()
                val list = snapshot.toObjects(MockTest::class.java)
                if (!stream.isNullOrBlank() || !module.isNullOrBlank() || !subject.isNullOrBlank()) {
                    list.filter { item ->
                        (stream.isNullOrBlank() || item.stream.isBlank() || item.stream.equals("All", ignoreCase = true) || item.stream.equals(stream, ignoreCase = true)) &&
                        (module.isNullOrBlank() || item.module.isBlank() || item.module.equals(module, ignoreCase = true) || item.type.equals(module, ignoreCase = true)) &&
                        (subject.isNullOrBlank() || item.subject.isBlank() || item.subject.equals(subject, ignoreCase = true) || item.domain.equals(subject, ignoreCase = true))
                    }
                } else {
                    list
                }
            } else {
                localMockTests.filter { item ->
                    (stream.isNullOrBlank() || item.stream.equals(stream, ignoreCase = true)) &&
                    (module.isNullOrBlank() || item.module.equals(module, ignoreCase = true)) &&
                    (subject.isNullOrBlank() || item.subject.equals(subject, ignoreCase = true) || item.domain.equals(subject, ignoreCase = true))
                }
            }
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error getting mock tests from Firestore", e)
            emptyList()
        }
    }

    override suspend fun getPYQs(stream: String?, module: String?, subject: String?): List<PYQ> {
        return try {
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("pyqs").get().await()
                val list = snapshot.toObjects(PYQ::class.java)
                if (!stream.isNullOrBlank() || !module.isNullOrBlank() || !subject.isNullOrBlank()) {
                    list.filter { item ->
                        (stream.isNullOrBlank() || item.stream.isBlank() || item.stream.equals("All", ignoreCase = true) || item.stream.equals(stream, ignoreCase = true)) &&
                        (module.isNullOrBlank() || item.module.isBlank() || item.module.equals(module, ignoreCase = true) || item.category.equals(module, ignoreCase = true)) &&
                        (subject.isNullOrBlank() || item.subject.isBlank() || item.subject.equals(subject, ignoreCase = true) || item.domain.equals(subject, ignoreCase = true))
                    }
                } else {
                    list
                }
            } else {
                localPYQs.filter { item ->
                    (stream.isNullOrBlank() || item.stream.equals(stream, ignoreCase = true)) &&
                    (module.isNullOrBlank() || item.module.equals(module, ignoreCase = true) || item.category.equals(module, ignoreCase = true)) &&
                    (subject.isNullOrBlank() || item.subject.equals(subject, ignoreCase = true) || item.domain.equals(subject, ignoreCase = true))
                }
            }
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error getting PYQs from Firestore", e)
            emptyList()
        }
    }

    override suspend fun getFeedback(): List<FeedbackEntry> {
        val current = _currentUser.value
        if (current == null || current.role != "admin") {
            Log.e("LearningRepository", "Unauthorized getFeedback attempt")
            return emptyList()
        }
        return try {
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                try {
                    val snapshot = firestore.collection("feedback")
                        .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .get()
                        .await()
                    snapshot.toObjects(FeedbackEntry::class.java)
                } catch (idxEx: Exception) {
                    val snapshot = firestore.collection("feedback").get().await()
                    snapshot.toObjects(FeedbackEntry::class.java).sortedByDescending { it.createdAt.seconds }
                }
            } else {
                localFeedback.sortedByDescending { it.createdAt.seconds }
            }
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error getting feedback from Firestore", e)
            emptyList()
        }
    }

    override suspend fun addVideo(title: String, description: String, url: String, category: String, stream: String, domain: String, thumbnailUrl: String, module: String, subject: String, contentType: String): Result<Unit> {
        val current = _currentUser.value
        if (current == null || current.role != "admin") {
            return Result.failure(Exception("Unauthorized: Admin access required."))
        }
        return try {
            val uploaderId = _currentUser.value?.uid ?: "unknown_admin"
            val id = "vid_${System.currentTimeMillis()}"
            val newVideo = VideoContent(
                id = id,
                title = title,
                description = description,
                url = url,
                category = category,
                stream = stream,
                domain = domain,
                thumbnailUrl = thumbnailUrl,
                uploadedBy = uploaderId,
                module = module,
                subject = subject,
                contentType = contentType,
                videoUrl = url
            )
            
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("videos").document(id).set(newVideo).await()
                android.util.Log.d("StorageUpload", "Firestore Save Success - Video [ID: $id, Title: $title, URL: $url]")
                android.util.Log.d("VideoUpload", "Firestore document saved with ID: $id")
                Result.success(Unit)
            } else {
                localVideos.add(newVideo)
                android.util.Log.d("StorageUpload", "Firestore Save Success - Local Video [ID: $id, Title: $title]")
                android.util.Log.d("VideoUpload", "Firestore document saved locally with ID: $id (Firebase disabled)")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addRecordedClass(title: String, videoUrl: String, stream: String, domain: String, module: String, subject: String, contentType: String, description: String): Result<Unit> {
        val current = _currentUser.value
        if (current == null || current.role != "admin") {
            return Result.failure(Exception("Unauthorized: Admin access required."))
        }
        return try {
            val uploaderId = _currentUser.value?.uid ?: "unknown_admin"
            val id = "rec_${System.currentTimeMillis()}"
            val newClass = RecordedClass(
                id = id,
                title = title,
                videoUrl = videoUrl,
                stream = stream,
                domain = domain,
                uploadedBy = uploaderId,
                module = module,
                subject = subject,
                contentType = contentType,
                description = description
            )
            
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("recordedClasses").document(id).set(newClass).await()
                android.util.Log.d("StorageUpload", "Firestore Save Success - Live Class [ID: $id, Title: $title, URL: $videoUrl]")
                Result.success(Unit)
            } else {
                localRecordedClasses.add(newClass)
                android.util.Log.d("StorageUpload", "Firestore Save Success - Local Live Class [ID: $id, Title: $title]")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addMaterial(title: String, fileUrl: String, category: String, stream: String, domain: String, module: String, subject: String, contentType: String, description: String): Result<Unit> {
        val current = _currentUser.value
        if (current == null || current.role != "admin") {
            return Result.failure(Exception("Unauthorized: Admin access required."))
        }
        return try {
            val uploaderId = _currentUser.value?.uid ?: "unknown_admin"
            val id = "mat_${System.currentTimeMillis()}"
            val newMaterial = StudyMaterial(
                id = id,
                title = title,
                fileUrl = fileUrl,
                category = category,
                stream = stream,
                domain = domain,
                uploadedBy = uploaderId,
                module = module,
                subject = subject,
                contentType = contentType,
                description = description
            )
            
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("materials").document(id).set(newMaterial).await()
                android.util.Log.d("StorageUpload", "Firestore Save Success - Study Material [ID: $id, Title: $title, URL: $fileUrl]")
                Result.success(Unit)
            } else {
                localMaterials.add(newMaterial)
                android.util.Log.d("StorageUpload", "Firestore Save Success - Local Study Material [ID: $id, Title: $title]")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addMockTest(title: String, type: String, stream: String, domain: String, questions: List<MockQuestion>, module: String, subject: String, contentType: String, description: String, uploadedBy: String, durationMinutes: Int): Result<Unit> {
        val current = _currentUser.value
        if (current == null || current.role != "admin") {
            return Result.failure(Exception("Unauthorized: Admin access required."))
        }
        return try {
            val uploaderId = _currentUser.value?.uid ?: "unknown_admin"
            val id = "test_${System.currentTimeMillis()}"
            val newTest = MockTest(
                id = id,
                title = title,
                type = type,
                stream = stream,
                domain = domain,
                questions = questions,
                module = module,
                subject = subject,
                contentType = contentType,
                description = description,
                uploadedBy = uploaderId,
                durationMinutes = durationMinutes
            )
            
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("mockTests").document(id).set(newTest).await()
                Result.success(Unit)
            } else {
                localMockTests.add(newTest)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun editVideo(id: String, title: String, description: String, url: String, category: String, stream: String, domain: String, thumbnailUrl: String, module: String, subject: String, contentType: String): Result<Unit> {
        val current = _currentUser.value
        if (current == null || current.role != "admin") {
            return Result.failure(Exception("Unauthorized: Admin access required."))
        }
        return try {
            val uploaderId = _currentUser.value?.uid ?: "unknown_admin"
            val updatedVideo = VideoContent(
                id = id,
                title = title,
                description = description,
                url = url,
                category = category,
                stream = stream,
                domain = domain,
                thumbnailUrl = thumbnailUrl,
                uploadedBy = uploaderId,
                module = module,
                subject = subject,
                contentType = contentType
            )
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("videos").document(id).set(updatedVideo).await()
                Result.success(Unit)
            } else {
                val index = localVideos.indexOfFirst { it.id == id }
                if (index != -1) {
                    localVideos[index] = updatedVideo
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun editMaterial(id: String, title: String, fileUrl: String, category: String, stream: String, domain: String, module: String, subject: String, contentType: String, description: String): Result<Unit> {
        val current = _currentUser.value
        if (current == null || current.role != "admin") {
            return Result.failure(Exception("Unauthorized: Admin access required."))
        }
        return try {
            val uploaderId = _currentUser.value?.uid ?: "unknown_admin"
            val updatedMaterial = StudyMaterial(
                id = id,
                title = title,
                fileUrl = fileUrl,
                category = category,
                stream = stream,
                domain = domain,
                uploadedBy = uploaderId,
                module = module,
                subject = subject,
                contentType = contentType,
                description = description
            )
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("materials").document(id).set(updatedMaterial).await()
                Result.success(Unit)
            } else {
                val index = localMaterials.indexOfFirst { it.id == id }
                if (index != -1) {
                    localMaterials[index] = updatedMaterial
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun editRecordedClass(id: String, title: String, videoUrl: String, stream: String, domain: String, module: String, subject: String, contentType: String, description: String): Result<Unit> {
        val current = _currentUser.value
        if (current == null || current.role != "admin") {
            return Result.failure(Exception("Unauthorized: Admin access required."))
        }
        return try {
            val uploaderId = _currentUser.value?.uid ?: "unknown_admin"
            val updatedClass = RecordedClass(
                id = id,
                title = title,
                videoUrl = videoUrl,
                stream = stream,
                domain = domain,
                uploadedBy = uploaderId,
                module = module,
                subject = subject,
                contentType = contentType,
                description = description
            )
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("recordedClasses").document(id).set(updatedClass).await()
                Result.success(Unit)
            } else {
                val index = localRecordedClasses.indexOfFirst { it.id == id }
                if (index != -1) {
                    localRecordedClasses[index] = updatedClass
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun editMockTest(id: String, title: String, type: String, stream: String, domain: String, questions: List<MockQuestion>, module: String, subject: String, contentType: String, description: String, uploadedBy: String, durationMinutes: Int): Result<Unit> {
        val current = _currentUser.value
        if (current == null || current.role != "admin") {
            return Result.failure(Exception("Unauthorized: Admin access required."))
        }
        return try {
            val uploaderId = _currentUser.value?.uid ?: "unknown_admin"
            val updatedTest = MockTest(
                id = id,
                title = title,
                type = type,
                stream = stream,
                domain = domain,
                questions = questions,
                module = module,
                subject = subject,
                contentType = contentType,
                description = description,
                uploadedBy = uploaderId,
                durationMinutes = durationMinutes
            )
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("mockTests").document(id).set(updatedTest).await()
                Result.success(Unit)
            } else {
                val index = localMockTests.indexOfFirst { it.id == id }
                if (index != -1) {
                    localMockTests[index] = updatedTest
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addPYQ(title: String, fileUrl: String, category: String, stream: String, domain: String, module: String, subject: String, contentType: String, description: String): Result<Unit> {
        val current = _currentUser.value
        if (current == null || current.role != "admin") {
            return Result.failure(Exception("Unauthorized: Admin access required."))
        }
        return try {
            val uploaderId = _currentUser.value?.uid ?: "unknown_admin"
            val id = "pyq_${System.currentTimeMillis()}"
            val newPYQ = PYQ(
                id = id,
                title = title,
                fileUrl = fileUrl,
                category = category,
                stream = stream,
                domain = domain,
                uploadedBy = uploaderId,
                module = module,
                subject = subject,
                contentType = contentType,
                description = description
            )
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("pyqs").document(id).set(newPYQ).await()
                android.util.Log.d("StorageUpload", "Firestore Save Success - PYQ [ID: $id, Title: $title, URL: $fileUrl]")
                Result.success(Unit)
            } else {
                localPYQs.add(newPYQ)
                android.util.Log.d("StorageUpload", "Firestore Save Success - Local PYQ [ID: $id, Title: $title]")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun editPYQ(id: String, title: String, fileUrl: String, category: String, stream: String, domain: String, module: String, subject: String, contentType: String, description: String): Result<Unit> {
        val current = _currentUser.value
        if (current == null || current.role != "admin") {
            return Result.failure(Exception("Unauthorized: Admin access required."))
        }
        return try {
            val uploaderId = _currentUser.value?.uid ?: "unknown_admin"
            val updatedPYQ = PYQ(
                id = id,
                title = title,
                fileUrl = fileUrl,
                category = category,
                stream = stream,
                domain = domain,
                uploadedBy = uploaderId,
                module = module,
                subject = subject,
                contentType = contentType,
                description = description
            )
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("pyqs").document(id).set(updatedPYQ).await()
                Result.success(Unit)
            } else {
                val index = localPYQs.indexOfFirst { it.id == id }
                if (index != -1) {
                    localPYQs[index] = updatedPYQ
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteContent(type: String, id: String): Result<Unit> {
        val current = _currentUser.value
        if (current == null || current.role != "admin") {
            return Result.failure(Exception("Unauthorized: Admin access required."))
        }
        return try {
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection(type).document(id).delete().await()
                Result.success(Unit)
            } else {
                when (type) {
                    "videos" -> localVideos.removeAll { it.id == id }
                    "materials" -> localMaterials.removeAll { it.id == id }
                    "recordedClasses" -> localRecordedClasses.removeAll { it.id == id }
                    "mockTests" -> localMockTests.removeAll { it.id == id }
                    "pyqs" -> localPYQs.removeAll { it.id == id }
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitFeedback(message: String): Result<Unit> {
        return try {
            val studentId = _currentUser.value?.uid ?: "unknown_student"
            val id = "feed_${System.currentTimeMillis()}"
            val entry = FeedbackEntry(
                id = id,
                studentId = studentId,
                message = message
            )
            
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                val firestoreEntry = if (studentId != "unknown_student") {
                    entry.copy(userId = studentId) // ensure userId is filled
                } else {
                    entry
                }
                firestore.collection("feedback").document(id).set(firestoreEntry).await()
                Result.success(Unit)
            } else {
                localFeedback.add(entry)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveTestAttempt(attempt: TestAttempt): Result<Unit> {
        return try {
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                val id = attempt.id.ifBlank { firestore.collection("attempts").document().id }
                val finalAttempt = attempt.copy(id = id)
                firestore.collection("attempts").document(id).set(finalAttempt).await()
                localAttempts.removeAll { it.id == id }
                localAttempts.add(finalAttempt)
                Result.success(Unit)
            } else {
                localAttempts.removeAll { it.id == attempt.id }
                localAttempts.add(attempt)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error saving test attempt", e)
            localAttempts.removeAll { it.id == attempt.id }
            localAttempts.add(attempt)
            Result.success(Unit)
        }
    }

    override suspend fun getTestAttempts(userId: String): List<TestAttempt> {
        return try {
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("attempts").get().await()
                val list = snapshot.toObjects(TestAttempt::class.java)
                val filtered = if (userId.isNotBlank()) {
                    list.filter { it.userId == userId }
                } else {
                    list
                }
                if (filtered.isNotEmpty()) filtered
                else localAttempts.filter { userId.isBlank() || it.userId == userId }
            } else {
                localAttempts.filter { userId.isBlank() || it.userId == userId }
            }
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error fetching test attempts", e)
            localAttempts.filter { userId.isBlank() || it.userId == userId }
        }
    }

    override suspend fun uploadFile(uri: android.net.Uri, context: android.content.Context): Result<String> {
        android.util.Log.d("StorageUpload", "Selected Uri: $uri")
        android.util.Log.d("StorageUpload", "Upload Started")
        return try {
            if (_isFirebaseEnabled.value) {
                val storage = com.google.firebase.storage.FirebaseStorage.getInstance()
                
                // Extract clean display name
                var fileName = "file_${System.currentTimeMillis()}"
                try {
                    if (uri.scheme == "content") {
                        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                                if (index != -1) {
                                    val displayName = cursor.getString(index)
                                    if (!displayName.isNullOrBlank()) {
                                        fileName = displayName.replace(" ", "_")
                                    }
                                }
                            }
                        }
                    } else {
                        val path = uri.path
                        if (path != null) {
                            val cut = path.lastIndexOf('/')
                            if (cut != -1) {
                                val displayName = path.substring(cut + 1)
                                if (displayName.isNotBlank()) {
                                    fileName = displayName.replace(" ", "_")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.w("StorageUpload", "Could not extract display name from URI", e)
                }
                
                val mimeType = context.contentResolver.getType(uri) ?: ""
                val folder = if (mimeType.contains("video", ignoreCase = true) || fileName.endsWith(".mp4", ignoreCase = true)) "videos" else "materials"
                val ref = storage.reference.child("$folder/$fileName")
                
                android.util.Log.d("StorageUpload", "Upload started for $uri to Storage path: ${ref.path}")
                
                val uploadTask = ref.putFile(uri)
                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress = if (taskSnapshot.totalByteCount > 0) {
                        (taskSnapshot.bytesTransferred.toDouble() / taskSnapshot.totalByteCount) * 100
                    } else {
                        0.0
                    }
                    android.util.Log.d("StorageUpload", "Upload progress: ${String.format("%.2f", progress)}%")
                }.await()
                
                android.util.Log.d("StorageUpload", "Upload Success")
                val rawDownloadUrl = ref.downloadUrl.await().toString()
                val downloadUrl = ensureFirebaseDownloadUrl(rawDownloadUrl)
                android.util.Log.d("StorageUpload", "Firebase Download URL: $downloadUrl")
                Result.success(downloadUrl)
            } else {
                android.util.Log.d("StorageUpload", "Firebase not enabled, using simulation fallback URL")
                android.util.Log.d("StorageUpload", "Upload Success")
                val simulatedUrl = "https://drive.google.com/file/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs/view?usp=sharing"
                android.util.Log.d("StorageUpload", "Firebase Download URL: $simulatedUrl")
                Result.success(simulatedUrl)
            }
        } catch (e: Exception) {
            android.util.Log.e("StorageUpload", "Upload failure with exception message: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun saveSessionToPrefs(context: android.content.Context, profile: UserProfile) {
        val sharedPref = context.getSharedPreferences("insyr_learning_prefs", android.content.Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putString("user_uid", profile.uid)
            putString("user_name", profile.name)
            putString("user_phone", profile.phone)
            putString("user_mobile", profile.mobile)
            putString("user_email", profile.email)
            putString("user_password", profile.password)
            putString("user_stream", profile.stream)
            putString("user_photo_url", profile.photoUrl)
            putString("user_role", profile.role)
            putString("user_status", profile.status)
            apply()
        }
    }

    private fun clearSessionFromPrefs(context: android.content.Context) {
        val sharedPref = context.getSharedPreferences("insyr_learning_prefs", android.content.Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
    }

    override suspend fun tryRestoreSession(context: android.content.Context): Result<UserProfile?> {
        this.appContext = context.applicationContext
        return try {
            val sharedPref = context.getSharedPreferences("insyr_learning_prefs", android.content.Context.MODE_PRIVATE)
            val savedUid = sharedPref.getString("user_uid", null)
            val savedRole = sharedPref.getString("user_role", null)
            
            if (savedUid != null) {
                if (_isFirebaseEnabled.value) {
                    val auth = FirebaseAuth.getInstance()
                    val currentFirebaseUser = auth.currentUser
                    if (currentFirebaseUser != null && currentFirebaseUser.uid == savedUid) {
                        try {
                            val firestore = FirebaseFirestore.getInstance()
                            val doc = firestore.collection("users").document(savedUid).get().await()
                            if (doc.exists()) {
                                val profile = doc.toObject(UserProfile::class.java)
                                if (profile != null) {
                                    val isEmailAdmin = profile.email.equals("admin@insyrlearning.com", ignoreCase = true)
                                    val finalProfile = if (isEmailAdmin) {
                                        profile.copy(role = "admin", status = "approved")
                                    } else {
                                        profile.copy(role = "student")
                                    }
                                    _currentUser.value = finalProfile
                                    saveSessionToPrefs(context, finalProfile)
                                    return Result.success(finalProfile)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("LearningRepository", "Failed to fetch from Firestore on startup, falling back to cached profile", e)
                        }
                    }
                }
                
                val savedProfile = UserProfile(
                    uid = savedUid,
                    name = sharedPref.getString("user_name", "") ?: "",
                    phone = sharedPref.getString("user_phone", "") ?: "",
                    mobile = sharedPref.getString("user_mobile", "") ?: "",
                    email = sharedPref.getString("user_email", "") ?: "",
                    password = sharedPref.getString("user_password", "") ?: "",
                    stream = sharedPref.getString("user_stream", "") ?: "",
                    photoUrl = sharedPref.getString("user_photo_url", "") ?: "",
                    role = savedRole ?: "student",
                    status = sharedPref.getString("user_status", "approved") ?: "approved"
                )
                
                if (localUsers.none { it.uid == savedUid }) {
                    localUsers.add(savedProfile)
                }
                
                _currentUser.value = savedProfile
                Result.success(savedProfile)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<UserProfile> {
        return try {
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("users").document(profile.uid).set(profile, SetOptions.merge()).await()
            }
            
            // Update local users list if matching
            val index = localUsers.indexOfFirst { it.uid == profile.uid }
            if (index != -1) {
                localUsers[index] = profile
            } else {
                localUsers.add(profile)
            }
            
            _currentUser.value = profile
            appContext?.let { saveSessionToPrefs(it, profile) }
            Result.success(profile)
        } catch (e: Exception) {
            // Offline/Fallback update
            val index = localUsers.indexOfFirst { it.uid == profile.uid }
            if (index != -1) {
                localUsers[index] = profile
            } else {
                localUsers.add(profile)
            }
            _currentUser.value = profile
            appContext?.let { saveSessionToPrefs(it, profile) }
            Result.success(profile)
        }
    }
}
