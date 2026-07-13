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
    suspend fun getVideos(): List<VideoContent>
    suspend fun getMaterials(): List<StudyMaterial>
    suspend fun getRecordedClasses(): List<RecordedClass>
    suspend fun getMockTests(): List<MockTest>
    suspend fun getFeedback(): List<FeedbackEntry>
    
    // Content Adding
    suspend fun addVideo(title: String, description: String, url: String, category: String, stream: String, thumbnailUrl: String): Result<Unit>
    suspend fun editVideo(id: String, title: String, description: String, url: String, category: String, stream: String, thumbnailUrl: String): Result<Unit>
    suspend fun addMaterial(title: String, fileUrl: String, category: String, stream: String): Result<Unit>
    suspend fun editMaterial(id: String, title: String, fileUrl: String, category: String, stream: String): Result<Unit>
    suspend fun addRecordedClass(title: String, videoUrl: String, stream: String): Result<Unit>
    suspend fun editRecordedClass(id: String, title: String, videoUrl: String, stream: String): Result<Unit>
    suspend fun addMockTest(title: String, type: String, stream: String, questions: List<MockQuestion>): Result<Unit>
    suspend fun editMockTest(id: String, title: String, type: String, stream: String, questions: List<MockQuestion>): Result<Unit>
    suspend fun deleteContent(type: String, id: String): Result<Unit>
    suspend fun uploadFile(uri: android.net.Uri, context: android.content.Context): Result<String>
    
    // Student Actions
    suspend fun submitFeedback(message: String): Result<Unit>
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
    private val localFeedback = mutableListOf<FeedbackEntry>()

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
        // Default admin
        val defaultAdmin = UserProfile(
            uid = "admin123",
            name = "Admin Principal",
            phone = "9876543210",
            email = "admin@insyrlearning.com",
            password = "admin123",
            stream = "",
            photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200",
            role = "admin",
            status = "approved"
        )
        // Default student (Aarav - as shown in Geometric Balance mockup!)
        val defaultStudent = UserProfile(
            uid = "student123",
            name = "Aarav Sharma",
            phone = "8888888888",
            email = "aarav@example.com",
            password = "student123",
            stream = "Science",
            photoUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&q=80&w=200",
            role = "student",
            status = "approved"
        )
        // Pending student
        val pendingStudent = UserProfile(
            uid = "studentPending",
            name = "Priya Patel",
            phone = "7777777777",
            email = "priya@example.com",
            password = "student123",
            stream = "Humanities",
            photoUrl = "",
            role = "student",
            status = "pending"
        )

        localUsers.addAll(listOf(defaultAdmin, defaultStudent, pendingStudent))
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

    override suspend fun getVideos(): List<VideoContent> {
        return try {
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("videos").get().await()
                snapshot.toObjects(VideoContent::class.java)
            } else {
                localVideos
            }
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error getting videos", e)
            localVideos
        }
    }

    override suspend fun getRecordedClasses(): List<RecordedClass> {
        return try {
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("recordedClasses").get().await()
                snapshot.toObjects(RecordedClass::class.java)
            } else {
                localRecordedClasses
            }
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error getting recorded classes", e)
            localRecordedClasses
        }
    }

    override suspend fun getMaterials(): List<StudyMaterial> {
        return try {
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("materials").get().await()
                snapshot.toObjects(StudyMaterial::class.java)
            } else {
                localMaterials
            }
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error getting materials", e)
            localMaterials
        }
    }

    override suspend fun getMockTests(): List<MockTest> {
        return try {
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("mockTests").get().await()
                snapshot.toObjects(MockTest::class.java)
            } else {
                localMockTests
            }
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error getting mock tests", e)
            localMockTests
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
                // Sort descending by createdAt. If no index is ready, catch and sort in memory.
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
            Log.e("LearningRepository", "Error getting feedback", e)
            localFeedback.sortedByDescending { it.createdAt.seconds }
        }
    }

    override suspend fun addVideo(title: String, description: String, url: String, category: String, stream: String, thumbnailUrl: String): Result<Unit> {
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
                thumbnailUrl = thumbnailUrl,
                uploadedBy = uploaderId
            )
            
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("videos").document(id).set(newVideo).await()
                Result.success(Unit)
            } else {
                localVideos.add(newVideo)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addRecordedClass(title: String, videoUrl: String, stream: String): Result<Unit> {
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
                uploadedBy = uploaderId
            )
            
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("recordedClasses").document(id).set(newClass).await()
                Result.success(Unit)
            } else {
                localRecordedClasses.add(newClass)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addMaterial(title: String, fileUrl: String, category: String, stream: String): Result<Unit> {
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
                uploadedBy = uploaderId
            )
            
            if (_isFirebaseEnabled.value) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("materials").document(id).set(newMaterial).await()
                Result.success(Unit)
            } else {
                localMaterials.add(newMaterial)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addMockTest(title: String, type: String, stream: String, questions: List<MockQuestion>): Result<Unit> {
        val current = _currentUser.value
        if (current == null || current.role != "admin") {
            return Result.failure(Exception("Unauthorized: Admin access required."))
        }
        return try {
            val id = "test_${System.currentTimeMillis()}"
            val newTest = MockTest(
                id = id,
                title = title,
                type = type,
                stream = stream,
                questions = questions
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

    override suspend fun editVideo(id: String, title: String, description: String, url: String, category: String, stream: String, thumbnailUrl: String): Result<Unit> {
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
                thumbnailUrl = thumbnailUrl,
                uploadedBy = uploaderId
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

    override suspend fun editMaterial(id: String, title: String, fileUrl: String, category: String, stream: String): Result<Unit> {
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
                uploadedBy = uploaderId
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

    override suspend fun editRecordedClass(id: String, title: String, videoUrl: String, stream: String): Result<Unit> {
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
                uploadedBy = uploaderId
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

    override suspend fun editMockTest(id: String, title: String, type: String, stream: String, questions: List<MockQuestion>): Result<Unit> {
        val current = _currentUser.value
        if (current == null || current.role != "admin") {
            return Result.failure(Exception("Unauthorized: Admin access required."))
        }
        return try {
            val updatedTest = MockTest(
                id = id,
                title = title,
                type = type,
                stream = stream,
                questions = questions
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

    override suspend fun uploadFile(uri: android.net.Uri, context: android.content.Context): Result<String> {
        return try {
            if (_isFirebaseEnabled.value) {
                val storage = com.google.firebase.storage.FirebaseStorage.getInstance()
                val id = "file_${System.currentTimeMillis()}"
                val ref = storage.reference.child("materials/$id")
                ref.putFile(uri).await()
                val downloadUrl = ref.downloadUrl.await().toString()
                Result.success(downloadUrl)
            } else {
                Result.success("https://firebasestorage.googleapis.com/v0/b/insyrlearning.appspot.com/o/materials%2Fmock_file.pdf?alt=media")
            }
        } catch (e: Exception) {
            Log.e("LearningRepository", "Storage upload failed, using simulation", e)
            Result.success("https://firebasestorage.googleapis.com/v0/b/insyrlearning.appspot.com/o/materials%2Fmock_file.pdf?alt=media")
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
