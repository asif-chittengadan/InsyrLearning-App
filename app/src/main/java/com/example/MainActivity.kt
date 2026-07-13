package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.draw.scale
import com.example.data.*
import com.example.ui.AuthState
import com.example.ui.LearningViewModel
import com.example.ui.QuizState
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class MainActivity : ComponentActivity() {
    private val viewModel = LearningViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentUserState by viewModel.currentUser.collectAsState()
            androidx.compose.runtime.LaunchedEffect(currentUserState) {
                val isStudent = currentUserState?.role == "student"
                if (isStudent) {
                    window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

// Geometric Balance Design Color Tokens
object GeoPalette {
    val Background = Color(0xFFFBF5EB)
    val TextPrimary = Color(0xFF261215)
    val TextSecondary = Color(0xFF5E4E50)
    val CardBorder = Color(0xFFEBE3D5)
    val CardBackground = Color(0xFFFFFFFF)
    val Divider = Color(0xFFF2EAE0)
    
    // Core brand
    val Primary = Color(0xFF801A24)
    val SecondaryContainer = Color(0xFFF5ECE1)
    val PrimaryContainer = Color(0xFFFBEBEB)
    val DarkText = Color(0xFF4A0E15)
    
    // Status colours
    val ApprovedBg = Color(0xFF386A20)
    val PendingBg = Color(0xFFE0A800)
    val RejectedBg = Color(0xFFC51162)
    
    // Card Badge specific styling matching geometric balance design
    val BadgeQuizBg = Color(0xFFD0BCFF)
    val BadgeQuizText = Color(0xFF21005D)
    
    val BadgeVideoBg = Color(0xFFB4E4FF)
    val BadgeVideoText = Color(0xFF001D35)
    
    val BadgeMaterialBg = Color(0xFFC1EAD1)
    val BadgeMaterialText = Color(0xFF00210C)
    
    val BadgeFeedbackBg = Color(0xFFFFD8E4)
    val BadgeFeedbackText = Color(0xFF31111D)
}

@Composable
fun RowScope.InteractiveNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    contentDescription: String? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "nav_item_scale"
    )
    val iconColor = if (selected) GeoPalette.Primary else GeoPalette.TextSecondary
    val textColor = if (selected) GeoPalette.Primary else GeoPalette.TextSecondary

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.scale(scale)
            )
        },
        label = {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                fontSize = if (selected) 11.sp else 10.sp,
                color = textColor,
                modifier = Modifier.scale(scale)
            )
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = GeoPalette.PrimaryContainer.copy(alpha = 0.8f)
        )
    )
}

@Composable
fun InteractiveNavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    contentDescription: String? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rail_item_scale"
    )
    val iconColor = if (selected) GeoPalette.Primary else GeoPalette.TextSecondary
    val textColor = if (selected) GeoPalette.Primary else GeoPalette.TextSecondary

    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.scale(scale)
            )
        },
        label = {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                fontSize = if (selected) 11.sp else 10.sp,
                color = textColor,
                modifier = Modifier.scale(scale)
            )
        },
        colors = NavigationRailItemDefaults.colors(
            indicatorColor = GeoPalette.PrimaryContainer.copy(alpha = 0.8f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: LearningViewModel) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    // Screen navigation tracking
    // Student: "home", "quiz", "video_player", "material_reader", "feedback_form", "all_videos", "all_materials", "all_quizzes"
    // Admin Tabs: "students", "content", "feedback"
    var currentScreen by remember { mutableStateOf("home") }
    var selectedVideo by remember { mutableStateOf<VideoContent?>(null) }
    var selectedMaterial by remember { mutableStateOf<StudyMaterial?>(null) }
    var showProfileDialog by remember { mutableStateOf(false) }
    
    // Show toast for messages
    LaunchedEffect(userMessage) {
        userMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

    // Restore persistent session on app launch
    LaunchedEffect(Unit) {
        viewModel.restoreSession(context)
    }

    // Dynamic FLAG_SECURE management to block screenshots and screen recording for students
    LaunchedEffect(authState) {
        val activity = context as? android.app.Activity
        val profile = (authState as? AuthState.Success)?.profile
        
        val isEmulator = android.os.Build.FINGERPRINT.startsWith("generic") ||
                android.os.Build.FINGERPRINT.startsWith("unknown") ||
                android.os.Build.MODEL.contains("google_sdk") ||
                android.os.Build.MODEL.contains("Emulator") ||
                android.os.Build.MODEL.contains("Android SDK built for x86") ||
                android.os.Build.MANUFACTURER.contains("Genymotion") ||
                (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic")) ||
                "google_sdk" == android.os.Build.PRODUCT ||
                android.os.Build.HARDWARE.contains("goldfish") ||
                android.os.Build.HARDWARE.contains("ranchu")

        if (profile != null && profile.isStudent()) {
            if (isEmulator) {
                // To prevent a black screen in the Streaming Emulator, we bypass FLAG_SECURE
                activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
        } else {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp

        if (showProfileDialog && authState is AuthState.Success) {
            MyProfileDialog(
                profile = (authState as AuthState.Success).profile,
                onDismiss = { showProfileDialog = false },
                onLogout = {
                    showProfileDialog = false
                    viewModel.logout()
                    currentScreen = "home"
                },
                onUpdateProfile = { updated ->
                    viewModel.updateUserProfile(updated)
                },
                onSubmitFeedback = { msg ->
                    viewModel.submitFeedback(msg)
                },
                viewModel = viewModel
            )
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
        containerColor = GeoPalette.Background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        InsyrLogoHorizontal(scale = 1.0f)
                    }
                },
                actions = {
                    if (authState is AuthState.Success) {
                        val profile = (authState as AuthState.Success).profile
                        IconButton(
                            onClick = { showProfileDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .testTag("profile_button")
                        ) {
                            if (profile.photoUrl.isNotBlank()) {
                                coil.compose.AsyncImage(
                                    model = profile.photoUrl,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(GeoPalette.Primary.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (profile.name.firstOrNull()?.toString() ?: "U").uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = GeoPalette.Primary,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GeoPalette.Background
                )
            )
        }
    ) { innerPadding ->
        Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(GeoPalette.Background)
            ) {
            when (val state = authState) {
                is AuthState.Idle, is AuthState.Error -> {
                    AuthScreen(
                        errorMsg = (state as? AuthState.Error)?.message,
                        onLogin = { identifier, password -> viewModel.login(identifier, password) },
                        onSignUp = { name, email, phone, stream, photoUrl, password ->
                            viewModel.signUp(name, email, phone, stream, photoUrl, password)
                        },
                        onGoogleSignIn = { idToken -> viewModel.loginWithGoogle(idToken) }
                    )
                }
                is AuthState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GeoPalette.Primary)
                    }
                }
                is AuthState.Success -> {
                    val user = state.profile
                    if (user.role == "admin" || user.isAdmin()) {
                        AdminFlow(
                            currentTab = currentScreen,
                            viewModel = viewModel,
                            onTabChange = { currentScreen = it }
                        )
                    } else {
                        // Student journeys
                        when (user.status.lowercase()) {
                            "pending" -> {
                                StudentPendingScreen(
                                    profile = user,
                                    onAutoApprove = {
                                        viewModel.updateStudentStatus(user.uid, "approved")
                                    },
                                    onLogout = { viewModel.logout() }
                                )
                            }
                            "rejected" -> {
                                StudentRejectedScreen(
                                    profile = user,
                                    onLogout = { viewModel.logout() }
                                )
                            }
                            "approved" -> {
                                StudentApprovedFlow(
                                    currentScreen = currentScreen,
                                    viewModel = viewModel,
                                    selectedVideo = selectedVideo,
                                    selectedMaterial = selectedMaterial,
                                    onNavigate = { screen -> currentScreen = screen },
                                    onPlayVideo = { video ->
                                        selectedVideo = video
                                        currentScreen = "video_player"
                                    },
                                    onReadMaterial = { material ->
                                        selectedMaterial = material
                                        currentScreen = "material_reader"
                                    }
                                )
                            }
                            else -> {
                                // Default fallback to pending screen
                                StudentPendingScreen(
                                    profile = user,
                                    onAutoApprove = {
                                        viewModel.updateStudentStatus(user.uid, "approved")
                                    },
                                    onLogout = { viewModel.logout() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

// ----------------------------------------------------
// AUTH SCREEN: Login and Register Switcher
// ----------------------------------------------------
@Composable
fun AuthScreen(
    errorMsg: String?,
    onLogin: (identifier: String, password: String) -> Unit,
    onSignUp: (name: String, email: String, phone: String, stream: String, photoUrl: String, password: String) -> Unit,
    onGoogleSignIn: (idToken: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSignUpMode by remember { mutableStateOf(false) }
    
    // Login form state
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    
    // Register form state
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var stream by remember { mutableStateOf("Science") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var profilePhotoUrl by remember { mutableStateOf("") }
    
    var streamExpanded by remember { mutableStateOf(false) }
    var localErrorMsg by remember { mutableStateOf<String?>(null) }

    // Android system Photo Picker contract
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            profilePhotoUrl = uri.toString()
        }
    }

    val defaultAvatars = listOf(
        "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&q=80&w=200", // Aarav
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200", // Priya
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=200", // Vivek
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=200"  // Neha
    )

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = GeoPalette.TextPrimary,
        unfocusedTextColor = GeoPalette.TextPrimary,
        focusedLabelColor = GeoPalette.Primary,
        unfocusedLabelColor = GeoPalette.TextSecondary,
        focusedPlaceholderColor = GeoPalette.TextSecondary.copy(alpha = 0.6f),
        unfocusedPlaceholderColor = GeoPalette.TextSecondary.copy(alpha = 0.6f),
        focusedBorderColor = GeoPalette.Primary,
        unfocusedBorderColor = GeoPalette.CardBorder,
        focusedLeadingIconColor = GeoPalette.Primary,
        unfocusedLeadingIconColor = GeoPalette.TextSecondary,
        cursorColor = GeoPalette.Primary
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Beautiful Branded INSYR LEARNING Vector Logo
            if (!isSignUpMode) {
                InsyrLogo(
                    scale = 1.3f,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

        Text(
            text = if (isSignUpMode) "Create Account" else "Welcome Back",
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = GeoPalette.TextPrimary,
            letterSpacing = (-0.5).sp
        )

        Text(
            text = "CUET & NCET Entrance Exam Practice Platform",
            fontSize = 14.sp,
            color = GeoPalette.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Error Banner (System or Local validation)
        val activeError = errorMsg ?: localErrorMsg
        if (activeError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = activeError,
                    color = Color(0xFFC62828),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Form Fields Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, GeoPalette.Divider),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isSignUpMode) {
                    // --- ONE SINGLE LOGIN SCREEN ---
                    val isEmail = loginIdentifier.contains("@")
                    
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Email or Phone Number",
                                fontSize = 12.sp,
                                color = GeoPalette.TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            if (loginIdentifier.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isEmail) GeoPalette.SecondaryContainer else GeoPalette.PrimaryContainer,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isEmail) "EMAIL DETECTED" else "PHONE DETECTED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GeoPalette.DarkText
                                    )
                                }
                            }
                        }
                        
                        OutlinedTextField(
                            value = loginIdentifier,
                            onValueChange = { loginIdentifier = it },
                            placeholder = { Text("Enter email or mobile") },
                            leadingIcon = { 
                                Icon(
                                    imageVector = if (isEmail) Icons.Default.Email else Icons.Default.Phone, 
                                    contentDescription = null
                                ) 
                            },
                            modifier = Modifier.fillMaxWidth().testTag("login_identifier_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = textFieldColors
                        )
                    }

                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = textFieldColors
                    )

                    Button(
                        onClick = {
                            localErrorMsg = null
                            if (loginIdentifier.isBlank() || loginPassword.isBlank()) {
                                localErrorMsg = "Please fill in all login fields."
                            } else {
                                onLogin(loginIdentifier, loginPassword)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("login_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary)
                    ) {
                        Text(
                            text = "Access Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // OR Divider
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Divider(modifier = Modifier.weight(1f), color = GeoPalette.Divider)
                        Text("OR", fontSize = 11.sp, color = GeoPalette.TextSecondary, fontWeight = FontWeight.Bold)
                        Divider(modifier = Modifier.weight(1f), color = GeoPalette.Divider)
                    }

                    // Google Sign-In Button
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val credentialManager = CredentialManager.create(context)
                                    val webClientId = try {
                                        BuildConfig.GOOGLE_WEB_CLIENT_ID
                                    } catch (e: Exception) {
                                        "1034645319814-7i981at8un2llciid5iqjnfsol2beh9v.apps.googleusercontent.com"
                                    }
                                    val finalWebClientId = if (webClientId.isNullOrBlank() || webClientId == "GOOGLE_WEB_CLIENT_ID") {
                                        "1034645319814-7i981at8un2llciid5iqjnfsol2beh9v.apps.googleusercontent.com"
                                    } else {
                                        webClientId
                                    }
                                    
                                    val googleIdOption = GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setServerClientId(finalWebClientId)
                                        .setAutoSelectEnabled(false)
                                        .build()

                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()

                                    val result = credentialManager.getCredential(
                                        context = context,
                                        request = request
                                    )
                                    val credential = result.credential
                                    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                        val idToken = googleIdTokenCredential.idToken
                                        onGoogleSignIn(idToken)
                                    } else {
                                        Toast.makeText(context, "Unexpected credential type returned", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: GetCredentialException) {
                                    Toast.makeText(context, "Sign-In cancelled: ${e.message}", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("google_signin_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GeoPalette.CardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val sizePx = size.width
                                    drawCircle(color = Color(0xFFEA4335), radius = sizePx / 2)
                                    drawCircle(color = Color.White, radius = sizePx / 3)
                                }
                                Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Sign in with Google",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = GeoPalette.TextPrimary
                            )
                        }
                    }

                } else {
                    // --- STUDENT REGISTRATION SCREEN ---
                    // --- PROFILE PHOTO UPLOAD SECTION (TOP CENTERED) ---
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(GeoPalette.CardBackground)
                                .border(2.dp, GeoPalette.Primary, CircleShape)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profilePhotoUrl.isNotBlank()) {
                                coil.compose.AsyncImage(
                                    model = profilePhotoUrl,
                                    contentDescription = "Uploaded Photo",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = "Placeholder Icon",
                                        tint = GeoPalette.Primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Add Photo",
                                        fontSize = 11.sp,
                                        color = GeoPalette.Primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            ) {
                                Text("Pick from Device", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GeoPalette.Primary)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("reg_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = textFieldColors
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("reg_email_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = textFieldColors
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        placeholder = { Text("e.g. 8888888888") },
                        modifier = Modifier.fillMaxWidth().testTag("reg_phone_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = textFieldColors
                    )

                    // Stream selection dropdown trigger
                    Column {
                        Text(
                            text = "Academic Stream",
                            fontSize = 12.sp,
                            color = GeoPalette.TextSecondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GeoPalette.CardBorder, RoundedCornerShape(12.dp))
                                .clickable { streamExpanded = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stream,
                                    fontSize = 15.sp,
                                    color = GeoPalette.TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown Indicator",
                                    tint = GeoPalette.TextSecondary
                                )
                            }
                            
                            DropdownMenu(
                                expanded = streamExpanded,
                                onDismissRequest = { streamExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f).background(Color.White)
                            ) {
                                listOf("Science", "Commerce", "Humanities").forEach { choice ->
                                    DropdownMenuItem(
                                        text = { Text(choice, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            stream = choice
                                            streamExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("reg_password_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = textFieldColors
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("reg_confirm_password_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = textFieldColors
                    )

                    // Register trigger
                    Button(
                        onClick = {
                            localErrorMsg = null
                            when {
                                name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank() -> {
                                    localErrorMsg = "Please fill in all mandatory fields."
                                }
                                !email.contains("@") || !email.contains(".") -> {
                                    localErrorMsg = "Please enter a valid email address."
                                }
                                password != confirmPassword -> {
                                    localErrorMsg = "Passwords do not match."
                                }
                                else -> {
                                    onSignUp(name, email, phone, stream, profilePhotoUrl, password)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("signup_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary)
                    ) {
                        Text(
                            text = "Register Student",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Switch modes action
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isSignUpMode) "Already have an account?" else "New to Insyr Learning?",
                color = GeoPalette.TextSecondary,
                fontSize = 14.sp
            )
            Text(
                text = if (isSignUpMode) "Log In" else "Create Student Account",
                color = GeoPalette.Primary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { 
                        isSignUpMode = !isSignUpMode 
                        localErrorMsg = null
                    }
                    .padding(4.dp)
            )
        }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ----------------------------------------------------
// STUDENT: Registration Pending/Rejected State
// ----------------------------------------------------
@Composable
fun StudentPendingScreen(
    profile: UserProfile,
    onAutoApprove: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status symbol
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(GeoPalette.PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LockClock,
                contentDescription = null,
                tint = GeoPalette.Primary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Registration Status: Pending",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = GeoPalette.TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, GeoPalette.Divider),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to Insyr Learning, ${profile.name}!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GeoPalette.Primary
                )
                
                Text(
                    text = "Your student request is currently under review by our learning administration team.\n\nOnce an Admin approves your account, you will get instant access to NCET & CUET Mock Exams, video tutorials, and study modules.",
                    fontSize = 14.sp,
                    color = GeoPalette.TextSecondary,
                    textAlign = TextAlign.Center
                )

                Divider(color = GeoPalette.Divider, modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Stream Selected", fontSize = 11.sp, color = GeoPalette.TextSecondary)
                        Text(profile.stream, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Contact Phone", fontSize = 11.sp, color = GeoPalette.TextSecondary)
                        Text(profile.phone, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Switch Account / Log Out", fontWeight = FontWeight.Bold)
        }
        }
    }
}

// ----------------------------------------------------
// STUDENT REJECTED SCREEN
// ----------------------------------------------------
@Composable
fun StudentRejectedScreen(
    profile: UserProfile,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Red Rejected Alert/Block symbol
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFEBEE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = null,
                tint = Color(0xFFC62828),
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Registration Status: Rejected",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color(0xFFC62828),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Hello, ${profile.name}!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFFC62828)
                )
                
                Text(
                    text = "We regret to inform you that your student registration request has been rejected by our learning administration team.\n\nIf you believe this is an error or wish to appeal this decision, please reach out to our support team or try registering with a different account.",
                    fontSize = 14.sp,
                    color = GeoPalette.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828))
            ) {
                Text("Switch Account / Log Out", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ----------------------------------------------------
// STUDENT APPROVED SCREEN AND FLOWS
// ----------------------------------------------------
@Composable
fun StudentApprovedFlow(
    currentScreen: String,
    viewModel: LearningViewModel,
    selectedVideo: VideoContent?,
    selectedMaterial: StudyMaterial?,
    onNavigate: (String) -> Unit,
    onPlayVideo: (VideoContent) -> Unit,
    onReadMaterial: (StudyMaterial) -> Unit
) {
    val videos by viewModel.videos.collectAsState()
    val materials by viewModel.materials.collectAsState()
    val mockTests by viewModel.mockTests.collectAsState()
    val quizState by viewModel.quizState.collectAsState()
    val recordedClasses by viewModel.recordedClasses.collectAsState()
    val studentProfile = (viewModel.authState.collectAsState().value as? AuthState.Success)?.profile ?: return

    // Filter content based on student's selected stream
    val streamFilteredVideos = remember(videos, studentProfile.stream) {
        videos.filter { it.stream.isBlank() || it.stream.equals("All", ignoreCase = true) || it.stream.equals(studentProfile.stream, ignoreCase = true) }
    }
    val streamFilteredMaterials = remember(materials, studentProfile.stream) {
        materials.filter { it.stream.isBlank() || it.stream.equals("All", ignoreCase = true) || it.stream.equals(studentProfile.stream, ignoreCase = true) }
    }
    val streamFilteredMockTests = remember(mockTests, studentProfile.stream) {
        mockTests.filter { it.stream.isBlank() || it.stream.equals("All", ignoreCase = true) || it.stream.equals(studentProfile.stream, ignoreCase = true) }
    }
    val streamFilteredRecordedClasses = remember(recordedClasses, studentProfile.stream) {
        recordedClasses.filter { it.stream.isBlank() || it.stream.equals("All", ignoreCase = true) || it.stream.equals(studentProfile.stream, ignoreCase = true) }
    }

    // Determine if the current screen is one of the main tab screens
    val isTabScreen = currentScreen in listOf("home", "all_videos", "all_materials", "all_quizzes")

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            Crossfade(targetState = currentScreen, label = "student_navigation") { screen ->
                when (screen) {
                    "home" -> {
                        StudentDashboardHome(
                            profile = studentProfile,
                            videosCount = streamFilteredVideos.size,
                            materialsCount = streamFilteredMaterials.size,
                            mockTestsCount = streamFilteredMockTests.size,
                            recentMaterials = streamFilteredMaterials.take(2),
                            onNavigate = onNavigate,
                            onReadMaterial = onReadMaterial,
                            onUpdateStream = { newStream ->
                                viewModel.updateUserProfile(studentProfile.copy(stream = newStream))
                            }
                        )
                    }
                    "all_quizzes" -> {
                        StudentMockTestsList(
                            mockTests = streamFilteredMockTests,
                            onStartQuiz = { test ->
                                viewModel.startQuiz(test)
                                onNavigate("quiz")
                            },
                            onBack = { onNavigate("home") }
                        )
                    }
                    "quiz" -> {
                        if (quizState.activeTest != null) {
                            QuizPlayerScreen(
                                state = quizState,
                                onAnswerSelected = { qIdx, optIdx -> viewModel.selectQuizAnswer(qIdx, optIdx) },
                                onNext = { viewModel.nextQuizQuestion() },
                                onPrev = { viewModel.prevQuizQuestion() },
                                onSubmit = { viewModel.submitQuiz() },
                                onExit = {
                                    viewModel.exitQuiz()
                                    onNavigate("all_quizzes")
                                }
                            )
                        } else {
                            onNavigate("all_quizzes")
                        }
                    }
                    "all_videos" -> {
                        StudentVideosList(
                            videos = streamFilteredVideos,
                            recordedClasses = streamFilteredRecordedClasses,
                            onPlayVideo = onPlayVideo,
                            onBack = { onNavigate("home") }
                        )
                    }
                    "video_player" -> {
                        if (selectedVideo != null) {
                            StudentVideoPlayer(
                                video = selectedVideo,
                                onBack = { onNavigate("all_videos") }
                            )
                        } else {
                            onNavigate("all_videos")
                        }
                    }
                    "all_materials" -> {
                        StudentMaterialsList(
                            materials = streamFilteredMaterials,
                            onReadMaterial = onReadMaterial,
                            onBack = { onNavigate("home") }
                        )
                    }
                    "material_reader" -> {
                        if (selectedMaterial != null) {
                            StudentMaterialReader(
                                material = selectedMaterial,
                                onBack = { onNavigate("all_materials") }
                            )
                        } else {
                            onNavigate("all_materials")
                        }
                    }
                    "feedback_form" -> {
                        StudentFeedbackScreen(
                            onSubmit = { msg ->
                                viewModel.submitFeedback(msg)
                            }
                        )
                    }
                }
            }
        }

        if (isTabScreen) {
            NavigationBar(
                containerColor = GeoPalette.CardBackground,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(androidx.compose.foundation.BorderStroke(1.dp, GeoPalette.Divider.copy(alpha = 0.5f)))
                    .navigationBarsPadding(),
            ) {
                listOf(
                    Triple("home", "Home", Icons.Default.Home),
                    Triple("all_videos", "Videos", Icons.Default.PlayCircle),
                    Triple("all_materials", "Materials", Icons.Default.MenuBook),
                    Triple("all_quizzes", "Quizzes", Icons.Default.Quiz)
                ).forEach { (screenId, label, icon) ->
                    val isSelected = currentScreen == screenId
                    InteractiveNavigationBarItem(
                        selected = isSelected,
                        onClick = { onNavigate(screenId) },
                        icon = icon,
                        label = label,
                        contentDescription = label
                    )
                }
            }
        }
    }
}

@Composable
fun SelectStreamBanner(
    onUpdateStream: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GeoPalette.PrimaryContainer.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GeoPalette.Primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = GeoPalette.Primary
                )
                Text(
                    text = "Select Your Academic Stream",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = GeoPalette.TextPrimary
                )
            }
            Text(
                text = "Please choose your stream to access video lectures, study materials, and mock exams personalized for your syllabus.",
                fontSize = 12.sp,
                color = GeoPalette.TextSecondary,
                lineHeight = 16.sp
            )
            
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { showDropdown = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Text("Choose Stream", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                }
                
                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    listOf("Science", "Commerce", "Humanities").forEach { streamOption ->
                        DropdownMenuItem(
                            text = { Text(streamOption) },
                            onClick = {
                                onUpdateStream(streamOption)
                                showDropdown = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentDashboardHome(
    profile: UserProfile,
    videosCount: Int,
    materialsCount: Int,
    mockTestsCount: Int,
    recentMaterials: List<StudyMaterial>,
    onNavigate: (String) -> Unit,
    onReadMaterial: (StudyMaterial) -> Unit,
    onUpdateStream: (String) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 720.dp

        if (isWide) {
            // Dual Pane Layout: Left pane for Hero + Learning Hub, Right pane for Recent Materials
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Pane: Hero + Learning Hub
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (profile.stream.isBlank()) {
                        SelectStreamBanner(onUpdateStream = onUpdateStream)
                    }

                    // Welcoming Hero Banner Card
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = GeoPalette.PrimaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "CURRENT STATUS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GeoPalette.TextSecondary,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Welcome, ${profile.name}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GeoPalette.DarkText,
                                        lineHeight = 26.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(GeoPalette.ApprovedBg, RoundedCornerShape(100.dp))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "APPROVED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                modifier = Modifier.padding(top = 12.dp)
                            ) {
                                Column {
                                    Text("Selected Stream", fontSize = 11.sp, color = GeoPalette.TextSecondary)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = profile.stream.ifBlank { "N/A" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = GeoPalette.TextPrimary
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(32.dp)
                                        .background(GeoPalette.CardBorder)
                                )
                                Column {
                                    Text("Exam Prep", fontSize = 11.sp, color = GeoPalette.TextSecondary)
                                    Text("CUET / NCET", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GeoPalette.TextPrimary)
                                }
                            }
                        }
                    }

                    // Geometric Balanced Grid matching mock designs
                    Text(
                        text = "Learning Hub",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GeoPalette.TextPrimary,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GeometricGridItem(
                            title = "Mock Tests",
                            subtitle = "$mockTestsCount Available",
                            badgeBg = GeoPalette.BadgeQuizBg,
                            badgeTextColor = GeoPalette.BadgeQuizText,
                            icon = Icons.Default.Quiz,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("all_quizzes") }
                        )

                        GeometricGridItem(
                            title = "Videos",
                            subtitle = "$videosCount Lectures",
                            badgeBg = GeoPalette.BadgeVideoBg,
                            badgeTextColor = GeoPalette.BadgeVideoText,
                            icon = Icons.Default.PlayCircle,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("all_videos") }
                        )
                    }

                    GeometricGridItem(
                        title = "Materials",
                        subtitle = "$materialsCount PDFs & Notes",
                        badgeBg = GeoPalette.BadgeMaterialBg,
                        badgeTextColor = GeoPalette.BadgeMaterialText,
                        icon = Icons.Default.MenuBook,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigate("all_materials") }
                    )
                }

                // Right Pane: Recent Materials
                Column(
                    modifier = Modifier
                        .weight(0.8f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Recent Materials",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeoPalette.TextPrimary,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    if (recentMaterials.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GeoPalette.Divider, RoundedCornerShape(16.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No study notes uploaded for ${profile.stream} yet.",
                                fontSize = 13.sp,
                                color = GeoPalette.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        recentMaterials.forEach { material ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .border(1.dp, GeoPalette.Divider, RoundedCornerShape(16.dp))
                                    .clickable { onReadMaterial(material) }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(GeoPalette.Background)
                                        .border(1.dp, GeoPalette.CardBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = GeoPalette.Primary
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = material.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = GeoPalette.TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${material.stream.ifEmpty { "General" }} • PDF Module",
                                        fontSize = 11.sp,
                                        color = GeoPalette.TextSecondary
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Read PDF",
                                    tint = GeoPalette.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Normal Portrait Layout for Mobile
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (profile.stream.isBlank()) {
                    SelectStreamBanner(onUpdateStream = onUpdateStream)
                }

                // Welcoming Hero Banner Card
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = GeoPalette.PrimaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "CURRENT STATUS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GeoPalette.TextSecondary,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Welcome, ${profile.name}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GeoPalette.DarkText,
                                        lineHeight = 26.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(GeoPalette.ApprovedBg, RoundedCornerShape(100.dp))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "APPROVED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                modifier = Modifier.padding(top = 12.dp)
                            ) {
                                Column {
                                    Text("Selected Stream", fontSize = 11.sp, color = GeoPalette.TextSecondary)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = profile.stream.ifBlank { "N/A" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = GeoPalette.TextPrimary
                                        )
                                    }
                                }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(32.dp)
                                    .background(GeoPalette.CardBorder)
                            )
                            Column {
                                Text("Exam Prep", fontSize = 11.sp, color = GeoPalette.TextSecondary)
                                Text("CUET / NCET", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GeoPalette.TextPrimary)
                            }
                        }
                    }
                }

                // Geometric Balanced Grid matching mock designs
                Text(
                    text = "Learning Hub",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GeoPalette.TextPrimary,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GeometricGridItem(
                        title = "Mock Tests",
                        subtitle = "$mockTestsCount Available",
                        badgeBg = GeoPalette.BadgeQuizBg,
                        badgeTextColor = GeoPalette.BadgeQuizText,
                        icon = Icons.Default.Quiz,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("all_quizzes") }
                    )

                    GeometricGridItem(
                        title = "Videos",
                        subtitle = "$videosCount Lectures",
                        badgeBg = GeoPalette.BadgeVideoBg,
                        badgeTextColor = GeoPalette.BadgeVideoText,
                        icon = Icons.Default.PlayCircle,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("all_videos") }
                    )
                }

                GeometricGridItem(
                    title = "Materials",
                    subtitle = "$materialsCount PDFs & Notes",
                    badgeBg = GeoPalette.BadgeMaterialBg,
                    badgeTextColor = GeoPalette.BadgeMaterialText,
                    icon = Icons.Default.MenuBook,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onNavigate("all_materials") }
                )

                // Recent Materials section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Recent Materials",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeoPalette.TextSecondary,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    if (recentMaterials.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GeoPalette.Divider, RoundedCornerShape(16.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No study notes uploaded for ${profile.stream} yet.",
                                fontSize = 13.sp,
                                color = GeoPalette.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        recentMaterials.forEach { material ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .border(1.dp, GeoPalette.Divider, RoundedCornerShape(16.dp))
                                    .clickable { onReadMaterial(material) }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(GeoPalette.Background)
                                        .border(1.dp, GeoPalette.CardBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = GeoPalette.Primary
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = material.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = GeoPalette.TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${material.stream.ifEmpty { "General" }} • PDF Module",
                                        fontSize = 11.sp,
                                        color = GeoPalette.TextSecondary
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Read PDF",
                                    tint = GeoPalette.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeometricGridItem(
    title: String,
    subtitle: String,
    badgeBg: Color,
    badgeTextColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .border(1.dp, GeoPalette.CardBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = GeoPalette.CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeTextColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = GeoPalette.TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = GeoPalette.TextSecondary
                )
            }
        }
    }
}

// ----------------------------------------------------
// STUDENT: MOCK TESTS & MCQ ENGINE
// ----------------------------------------------------
@Composable
fun StudentMockTestsList(
    mockTests: List<MockTest>,
    onStartQuiz: (MockTest) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
            }
            Text("Entrance Mock Exams", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        if (mockTests.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Quiz, null, modifier = Modifier.size(64.dp), tint = GeoPalette.TextSecondary)
                    Text("No practice mock exams matching your stream yet.", color = GeoPalette.TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mockTests) { test ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, GeoPalette.Divider),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(GeoPalette.PrimaryContainer, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = test.type.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GeoPalette.DarkText
                                    )
                                }
                                Text(
                                    text = "${test.questions.size} MCQs",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GeoPalette.TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = test.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = GeoPalette.TextPrimary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { onStartQuiz(test) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary)
                            ) {
                                Text("Attempt Practice Exam", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizPlayerScreen(
    state: QuizState,
    onAnswerSelected: (questionIdx: Int, optionIdx: Int) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSubmit: () -> Unit,
    onExit: () -> Unit
) {
    val test = state.activeTest ?: return
    val totalQuestions = test.questions.size
    val currentIdx = state.currentQuestionIndex
    val currentQuestion = test.questions[currentIdx]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top exit action bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.Default.Close, "Exit Practice")
            }
            Text(
                text = "Question ${currentIdx + 1} of $totalQuestions",
                fontWeight = FontWeight.Bold,
                color = GeoPalette.TextSecondary
            )
            Text(
                text = "Score: ${state.score}/$totalQuestions",
                fontWeight = FontWeight.Bold,
                color = GeoPalette.Primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress indicators
        LinearProgressIndicator(
            progress = { (currentIdx + 1).toFloat() / totalQuestions },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = GeoPalette.Primary,
            trackColor = GeoPalette.CardBorder
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Active Question Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, GeoPalette.Divider),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = currentQuestion.questionText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GeoPalette.TextPrimary,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Options
                currentQuestion.options.forEachIndexed { optIdx, optionText ->
                    val isSelected = state.selectedAnswers[currentIdx] == optIdx
                    val isCorrect = currentQuestion.correctAnswerIndex == optIdx
                    val shouldHighlightCorrect = state.isSubmitted && isCorrect
                    val shouldHighlightWrong = state.isSubmitted && isSelected && !isCorrect

                    val optionBg = when {
                        shouldHighlightCorrect -> Color(0xFFE8F5E9)
                        shouldHighlightWrong -> Color(0xFFFFEBEE)
                        isSelected -> GeoPalette.PrimaryContainer
                        else -> GeoPalette.CardBackground
                    }

                    val optionBorderColor = when {
                        shouldHighlightCorrect -> Color(0xFF4CAF50)
                        shouldHighlightWrong -> Color(0xFFE53935)
                        isSelected -> GeoPalette.Primary
                        else -> GeoPalette.CardBorder
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .border(1.dp, optionBorderColor, RoundedCornerShape(12.dp))
                            .background(optionBg, RoundedCornerShape(12.dp))
                            .clickable { onAnswerSelected(currentIdx, optIdx) }
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Letter bubble (A, B, C, D)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) GeoPalette.Primary else GeoPalette.Divider
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ('A'.code + optIdx).toChar().toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else GeoPalette.TextPrimary,
                                    fontSize = 12.sp
                                )
                            }

                            Text(
                                text = optionText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = GeoPalette.TextPrimary,
                                modifier = Modifier.weight(1f)
                            )

                            if (state.isSubmitted) {
                                if (isCorrect) {
                                    Icon(Icons.Default.CheckCircle, "Correct", tint = Color(0xFF4CAF50))
                                } else if (isSelected) {
                                    Icon(Icons.Default.Cancel, "Incorrect", tint = Color(0xFFE53935))
                                }
                            }
                        }
                    }
                }

                // Answer explanation if submitted
                if (state.isSubmitted && currentQuestion.explanation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GeoPalette.Background),
                        border = BorderStroke(1.dp, GeoPalette.CardBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Explanation Note:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GeoPalette.Primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(currentQuestion.explanation, fontSize = 13.sp, color = GeoPalette.TextSecondary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions navigation footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onPrev,
                enabled = currentIdx > 0,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Previous")
            }

            if (currentIdx == totalQuestions - 1) {
                Button(
                    onClick = onSubmit,
                    enabled = !state.isSubmitted,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quiz_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary)
                ) {
                    Text("Finish Exam", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary)
                ) {
                    Text("Next Question")
                }
            }
        }
    }
}

// ----------------------------------------------------
// STUDENT: VIDEO LECTURES & PLAYBACK SCREEN
// ----------------------------------------------------
@Composable
fun StudentVideosList(
    videos: List<VideoContent>,
    recordedClasses: List<RecordedClass> = emptyList(),
    onPlayVideo: (VideoContent) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val filtered = remember(videos, recordedClasses, searchQuery, selectedCategory) {
        if (selectedCategory.equals("Recorded", ignoreCase = true)) {
            recordedClasses.map { rec ->
                VideoContent(
                    id = rec.id,
                    title = rec.title,
                    description = "Recorded interactive lecture.",
                    url = rec.videoUrl,
                    category = "Recorded",
                    stream = rec.stream,
                    createdAt = rec.createdAt
                )
            }.filter {
                searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true)
            }
        } else {
            videos.filter { video ->
                (selectedCategory == "All" || video.category.equals(selectedCategory, ignoreCase = true)) &&
                (searchQuery.isBlank() || video.title.contains(searchQuery, ignoreCase = true))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Aesthetic spacious top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(GeoPalette.SecondaryContainer, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = GeoPalette.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = "Video Masterclasses",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GeoPalette.DarkText
                )
                Text(
                    text = "Premium video content & recorded live streams",
                    fontSize = 12.sp,
                    color = GeoPalette.TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Custom premium compact Search bar
        BasicTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            textStyle = androidx.compose.ui.text.TextStyle(
                color = GeoPalette.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            ),
            singleLine = true,
            cursorBrush = androidx.compose.ui.graphics.SolidColor(GeoPalette.Primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, GeoPalette.CardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = GeoPalette.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search lectures...",
                                color = GeoPalette.TextSecondary.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = GeoPalette.TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        )

        // Custom Category Pills row with visual icons
        val categories = listOf(
            Triple("All", "All Lectures", Icons.Default.Apps),
            Triple("Domain", "Domain", Icons.Default.Book),
            Triple("General", "General", Icons.Default.Layers),
            Triple("TeachingAptitude", "Teaching Apt.", Icons.Default.School),
            Triple("Recorded", "Recorded", Icons.Default.Videocam)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp)
        ) {
            categories.forEach { (catId, catLabel, icon) ->
                val isSelected = selectedCategory == catId
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = if (isSelected) GeoPalette.Primary else GeoPalette.CardBorder,
                            shape = RoundedCornerShape(50.dp)
                        )
                        .background(
                            color = if (isSelected) GeoPalette.Primary else Color.White,
                            shape = RoundedCornerShape(50.dp)
                        )
                        .clickable { selectedCategory = catId }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = catLabel,
                            tint = if (isSelected) Color.White else GeoPalette.TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = catLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else GeoPalette.TextPrimary
                        )
                    }
                }
            }
        }

        // Adaptive Multi-Column Grid or List Layout based on available space
        BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val width = maxWidth
            val columns = if (width >= 800.dp) 3 else if (width >= 550.dp) 2 else 1

            if (filtered.isEmpty()) {
                // Grounded and stunning empty state illustration
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(GeoPalette.PrimaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "No lectures",
                            tint = GeoPalette.Primary,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "No Masterclasses Found",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GeoPalette.DarkText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "We couldn't find any lecture videos matching your filters. Try switching the category or clearing your search input.",
                        fontSize = 13.sp,
                        color = GeoPalette.TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            searchQuery = ""
                            selectedCategory = "All"
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FilterListOff, null, modifier = Modifier.size(16.dp))
                            Text("Reset Search & Filters", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                if (columns == 1) {
                    // Optimized List Layout
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(filtered) { video ->
                            VideoListItemCard(video = video, onPlayVideo = onPlayVideo)
                        }
                    }
                } else {
                    // Beautiful Responsive Grid Layout
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(filtered) { video ->
                            VideoGridItemCard(video = video, onPlayVideo = onPlayVideo)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoListItemCard(
    video: VideoContent,
    onPlayVideo: (VideoContent) -> Unit
) {
    val durationText = remember(video.id) {
        val hash = kotlin.math.abs(video.id.hashCode())
        val mins = (hash % 45) + 30
        val secs = hash % 60
        String.format("%d:%02d", mins, secs)
    }

    val formattedDate = remember(video.createdAt) {
        try {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            sdf.format(video.createdAt.toDate())
        } catch (e: Exception) {
            "Recent"
        }
    }

    val (badgeBg, badgeText) = remember(video.category) {
        when (video.category.lowercase(java.util.Locale.getDefault())) {
            "domain" -> Pair(GeoPalette.BadgeMaterialBg, GeoPalette.BadgeMaterialText)
            "general" -> Pair(GeoPalette.BadgeQuizBg, GeoPalette.BadgeQuizText)
            "teachingaptitude" -> Pair(GeoPalette.BadgeFeedbackBg, GeoPalette.BadgeFeedbackText)
            "recorded" -> Pair(GeoPalette.BadgeVideoBg, GeoPalette.BadgeVideoText)
            else -> Pair(GeoPalette.BadgeVideoBg, GeoPalette.BadgeVideoText)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlayVideo(video) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, GeoPalette.Divider),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // High-fidelity Thumbnail with Gradient overlay and play icon
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(GeoPalette.Primary, GeoPalette.Primary.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!video.thumbnailUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = "Video Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = GeoPalette.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(GeoPalette.Primary.copy(alpha = 0.9f), GeoPalette.DarkText)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.9f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = GeoPalette.Primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Deterministic Duration Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = durationText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Clean, scannable info column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .background(badgeBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = video.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeText
                        )
                    }

                    // Stream Badge
                    if (!video.stream.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .background(GeoPalette.SecondaryContainer, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = video.stream,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeoPalette.Primary
                            )
                        }
                    }
                }

                Text(
                    text = video.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GeoPalette.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = GeoPalette.TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = GeoPalette.TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (!video.uploadedBy.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Tutor",
                            tint = GeoPalette.TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = video.uploadedBy,
                            fontSize = 11.sp,
                            color = GeoPalette.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open video",
                tint = GeoPalette.Primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun VideoGridItemCard(
    video: VideoContent,
    onPlayVideo: (VideoContent) -> Unit
) {
    val durationText = remember(video.id) {
        val hash = kotlin.math.abs(video.id.hashCode())
        val mins = (hash % 45) + 30
        val secs = hash % 60
        String.format("%d:%02d", mins, secs)
    }

    val formattedDate = remember(video.createdAt) {
        try {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            sdf.format(video.createdAt.toDate())
        } catch (e: Exception) {
            "Recent"
        }
    }

    val (badgeBg, badgeText) = remember(video.category) {
        when (video.category.lowercase(java.util.Locale.getDefault())) {
            "domain" -> Pair(GeoPalette.BadgeMaterialBg, GeoPalette.BadgeMaterialText)
            "general" -> Pair(GeoPalette.BadgeQuizBg, GeoPalette.BadgeQuizText)
            "teachingaptitude" -> Pair(GeoPalette.BadgeFeedbackBg, GeoPalette.BadgeFeedbackText)
            "recorded" -> Pair(GeoPalette.BadgeVideoBg, GeoPalette.BadgeVideoText)
            else -> Pair(GeoPalette.BadgeVideoBg, GeoPalette.BadgeVideoText)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlayVideo(video) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, GeoPalette.Divider),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(GeoPalette.Primary, GeoPalette.Primary.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!video.thumbnailUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = "Video Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = GeoPalette.Primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(GeoPalette.Primary.copy(alpha = 0.9f), GeoPalette.DarkText)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.White.copy(alpha = 0.9f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = GeoPalette.Primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = durationText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(badgeBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = video.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeText
                        )
                    }

                    if (!video.stream.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .background(GeoPalette.SecondaryContainer, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = video.stream,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeoPalette.Primary
                            )
                        }
                    }
                }

                Text(
                    text = video.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GeoPalette.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.heightIn(min = 36.dp)
                )

                Divider(color = GeoPalette.Divider, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            tint = GeoPalette.TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 10.sp,
                            color = GeoPalette.TextSecondary
                        )
                    }
                    if (!video.uploadedBy.isNullOrBlank()) {
                        Text(
                            text = "By ${video.uploadedBy}",
                            fontSize = 10.sp,
                            color = GeoPalette.TextSecondary,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 80.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentVideoPlayer(
    video: VideoContent,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
            }
            Text("Lecture Player", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        // Mock Screen Player Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // Background simulated graphic
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Tv, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Text("SIMULATED LECTURE STREAM", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Lecture URL: ${video.url}", color = Color.Gray, fontSize = 10.sp)
                }

                // Control panel overlay bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(Icons.Default.Pause, "Pause", tint = Color.White)
                    // Custom Slider indicator
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .padding(horizontal = 12.dp)
                            .background(Color.DarkGray)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.35f)
                                .fillMaxHeight()
                                .background(GeoPalette.Primary)
                        )
                    }
                    Text("14:22 / 45:00", color = Color.White, fontSize = 11.sp)
                }
            }
        }

        // Metadata details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, GeoPalette.Divider),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = video.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GeoPalette.TextPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text(video.category) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = GeoPalette.CardBackground)
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("Stream: " + video.stream.ifEmpty { "General" }) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = GeoPalette.CardBackground)
                    )
                }

                Divider(color = GeoPalette.Divider)

                Text(
                    text = "This course study lecture helps aspirants understand complex topics related to the CUET / NCET syllabus. Take down notes in your workbook and cross-verify with study materials and practice mock exams inside the materials section.",
                    fontSize = 13.sp,
                    color = GeoPalette.TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ----------------------------------------------------
// STUDENT: STUDY MATERIALS / PDF SUMMARY READER
// ----------------------------------------------------
@Composable
fun StudentMaterialsList(
    materials: List<StudyMaterial>,
    onReadMaterial: (StudyMaterial) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(materials, searchQuery) {
        materials.filter {
            searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
            }
            Text("Study Material Notes", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search materials & formulae sheets...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No study materials found.", color = GeoPalette.TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { material ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, GeoPalette.Divider),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onReadMaterial(material) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(GeoPalette.BadgeMaterialBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = GeoPalette.BadgeMaterialText
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = material.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = GeoPalette.TextPrimary
                                )
                                Text(
                                    text = "Category: ${material.category} • Stream: ${material.stream.ifEmpty { "General" }}",
                                    fontSize = 11.sp,
                                    color = GeoPalette.TextSecondary
                                )
                            }

                            Icon(Icons.Default.ChevronRight, null, tint = GeoPalette.TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentMaterialReader(
    material: StudyMaterial,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
            }
            Text("Summary Notebook", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, GeoPalette.Divider),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.MenuBook, null, tint = GeoPalette.Primary, modifier = Modifier.size(28.dp))
                    Text(material.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GeoPalette.TextPrimary)
                }

                Divider(color = GeoPalette.Divider)

                Text(
                    text = "CHAPTER SUMMARY NOTEBOOK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeoPalette.Primary,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Aspirants preparing for the National Common Entrance Test (NCET) and Central Universities Entrance Test (CUET) can review this formulated capsule guide. This covers fundamental concept charts, definitions, typical questions patterns, and solutions logic.\n\nFile download stream: ${material.fileUrl}\n\nKey Concepts Breakdown:\n1. Core definitions of the topic outline.\n2. Fundamental theorems, derivations, and formulas mapped to the current curriculum.\n3. Model solved questions outlining correct step layouts to ensure zero marks lost.\n4. Quick response strategies for high speed MCQ cracking during live exams.",
                    fontSize = 14.sp,
                    color = GeoPalette.TextSecondary,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /* simulated open pdf */ },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download Full PDF Study Material", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ----------------------------------------------------
// STUDENT: FEEDBACK FORM SCREEN
// ----------------------------------------------------
@Composable
fun StudentFeedbackScreen(
    onSubmit: (String) -> Unit
) {
    var feedbackMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Contact Platform Admins", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Have questions regarding study topics, exam patterns, or need access to extra resources? Write your feedback here and our platform curators will assist you shortly.",
            color = GeoPalette.TextSecondary,
            fontSize = 13.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, GeoPalette.Divider),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = feedbackMsg,
                    onValueChange = { feedbackMsg = it },
                    placeholder = { Text("Write your message here... e.g. Requesting more questions for Physics Waves test") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        onSubmit(feedbackMsg)
                        feedbackMsg = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_feedback_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary)
                ) {
                    Icon(Icons.AutoMirrored.Default.Send, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Message", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// ----------------------------------------------------
// ADMIN FLOW: STUDENT APPROVALS, CONTENT MANAGEMENT, FEEDBACK
// ----------------------------------------------------
@Composable
fun AdminFlow(
    currentTab: String,
    viewModel: LearningViewModel,
    onTabChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Content Area depending on Tab
        Box(modifier = Modifier.weight(1f)) {
            when (currentTab) {
                "home", "students" -> {
                    AdminStudentsTab(viewModel = viewModel)
                }
                "content" -> {
                    AdminContentTab(viewModel = viewModel)
                }
                "feedback" -> {
                    AdminFeedbackTab(viewModel = viewModel)
                }
                else -> {
                    AdminStudentsTab(viewModel = viewModel)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab indicator header for Admin Flow at the bottom of the page
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GeoPalette.CardBackground, RoundedCornerShape(16.dp))
                .border(1.dp, GeoPalette.Divider, RoundedCornerShape(16.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Triple("home", "Approvals", Icons.Default.CheckCircle),
                Triple("content", "Content Manager", Icons.Default.LibraryBooks),
                Triple("feedback", "Feedback", Icons.Default.ChatBubble)
            ).forEach { (tabId, label, icon) ->
                val isSelected = currentTab == tabId || (currentTab == "students" && tabId == "home")
                
                val bgTabColor by animateColorAsState(
                    targetValue = if (isSelected) GeoPalette.Primary else Color.Transparent,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "bg_tab"
                )
                
                val contentTabColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else GeoPalette.TextSecondary,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "content_tab"
                )
                
                val scaleTab by animateFloatAsState(
                    targetValue = if (isSelected) 1.04f else 0.96f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "scale_tab"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .scale(scaleTab)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgTabColor)
                        .clickable { onTabChange(tabId) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = contentTabColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = contentTabColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStudentsTab(viewModel: LearningViewModel) {
    val students by viewModel.students.collectAsState()
    val pendingStudents = remember(students) {
        students.filter { it.status == "pending" }
    }
    var showAllUsersDialog by remember { mutableStateOf(false) }
    var showPendingUsersDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<UserProfile?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 1. Dashboard Cards Header Item
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    // Card 1: Total Students (Blue)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clickable { showAllUsersDialog = true },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2296F3))
                    ) {
                    Box(modifier = Modifier.fillMaxSize().padding(22.dp)) {
                        // Top row with Icon and Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Total Students Icon",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            
                            // Badge "+12% this week"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "+12% this week",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Bottom text content
                        Column(
                            modifier = Modifier.align(Alignment.BottomStart)
                        ) {
                            Text(
                                text = "Total Students",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format("%,d", students.size),
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Card 2: Pending Approvals (Purple)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clickable { showPendingUsersDialog = true },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF9E78F2))
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(22.dp)) {
                        // Top row with Icon and Red dot
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Ring icon
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(2.5.dp, Color.White, CircleShape)
                            )
                            
                            // Glowing red dot
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE53935))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            )
                        }
                        
                        // Bottom text content
                        Column(
                            modifier = Modifier.align(Alignment.BottomStart)
                        ) {
                            Text(
                                text = "Pending Approvals",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${pendingStudents.size}",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. Section Title Header
        item {
            Text(
                text = "Registration Requests (${pendingStudents.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = GeoPalette.Primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // 3. Pending Students list or Empty State
        if (pendingStudents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = GeoPalette.CardBackground),
                    border = BorderStroke(1.dp, GeoPalette.Divider),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "No Pending Approvals",
                            tint = GeoPalette.ApprovedBg,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No Pending Approvals",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = GeoPalette.TextPrimary
                        )
                        Text(
                            text = "All student registration requests have been processed successfully.",
                            fontSize = 13.sp,
                            color = GeoPalette.TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(pendingStudents) { student ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("pending_student_card_${student.uid}"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, GeoPalette.Divider),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Student Photo
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(GeoPalette.Primary.copy(alpha = 0.1f))
                                    .border(1.dp, GeoPalette.Divider, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (student.photoUrl.isNotBlank()) {
                                    coil.compose.AsyncImage(
                                        model = student.photoUrl,
                                        contentDescription = "Student Photo",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    val firstLetter = student.name.firstOrNull()?.toString() ?: "S"
                                    Text(
                                        text = firstLetter.uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = GeoPalette.Primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = GeoPalette.TextPrimary
                                )
                                Text(
                                    text = "Stream: ${student.stream}",
                                    fontSize = 13.sp,
                                    color = GeoPalette.TextSecondary
                                )
                                val displayPhone = student.phone.ifBlank { student.mobile }
                                Text(
                                    text = "Phone: $displayPhone",
                                    fontSize = 13.sp,
                                    color = GeoPalette.TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.updateStudentStatus(student.uid, "approved") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("approve_student_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.ApprovedBg)
                            ) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.updateStudentStatus(student.uid, "rejected") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reject_student_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, GeoPalette.RejectedBg),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GeoPalette.RejectedBg)
                            ) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAllUsersDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showAllUsersDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(GeoPalette.Background),
                    colors = CardDefaults.cardColors(containerColor = GeoPalette.Background)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(24.dp)
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { showAllUsersDialog = false }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GeoPalette.Primary)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "All Registered Users",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoPalette.TextPrimary
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(GeoPalette.Primary.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${students.size} Total",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoPalette.Primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (students.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No registered users currently.", color = GeoPalette.TextSecondary)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(students) { student ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, GeoPalette.Divider)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(44.dp)
                                                            .clip(CircleShape)
                                                            .background(GeoPalette.CardBackground),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (student.photoUrl.isNotBlank()) {
                                                            coil.compose.AsyncImage(
                                                                model = student.photoUrl,
                                                                contentDescription = "Profile Photo",
                                                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                            )
                                                        } else {
                                                            val firstLetter = student.name.firstOrNull()?.toString() ?: "S"
                                                            Text(
                                                                text = firstLetter.uppercase(),
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 16.sp,
                                                                color = GeoPalette.Primary
                                                            )
                                                        }
                                                    }

                                                    Column {
                                                        Text(
                                                            text = student.name,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 15.sp,
                                                            color = GeoPalette.TextPrimary
                                                        )
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(top = 2.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .background(
                                                                        when(student.status.lowercase()) {
                                                                            "approved" -> GeoPalette.ApprovedBg.copy(alpha = 0.1f)
                                                                            "pending" -> GeoPalette.PendingBg.copy(alpha = 0.1f)
                                                                            else -> GeoPalette.RejectedBg.copy(alpha = 0.1f)
                                                                        },
                                                                        RoundedCornerShape(4.dp)
                                                                    )
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = student.status.uppercase(),
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = when(student.status.lowercase()) {
                                                                        "approved" -> GeoPalette.ApprovedBg
                                                                        "pending" -> GeoPalette.PendingBg
                                                                        else -> GeoPalette.RejectedBg
                                                                    }
                                                                )
                                                            }

                                                            if (student.stream.isNotBlank()) {
                                                                Text(
                                                                    text = student.stream,
                                                                    fontSize = 11.sp,
                                                                    color = GeoPalette.Primary,
                                                                    fontWeight = FontWeight.SemiBold
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            editingStudent = student
                                                        },
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Edit student info",
                                                            tint = GeoPalette.Primary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            viewModel.deleteStudent(student.uid)
                                                        },
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Remove student",
                                                            tint = GeoPalette.RejectedBg,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 12.dp),
                                                thickness = 1.dp,
                                                color = GeoPalette.Divider
                                            )

                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.padding(start = 4.dp)
                                            ) {
                                                // Email detail
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Email,
                                                        contentDescription = "Email",
                                                        tint = GeoPalette.TextSecondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Email:",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = GeoPalette.TextSecondary,
                                                        modifier = Modifier.width(70.dp)
                                                    )
                                                    Text(
                                                        text = student.email,
                                                        fontSize = 13.sp,
                                                        color = GeoPalette.TextPrimary,
                                                        fontWeight = FontWeight.Normal
                                                    )
                                                }

                                                // Phone detail
                                                val displayPhone = student.phone.ifBlank { student.mobile }.ifBlank { "N/A" }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Phone,
                                                        contentDescription = "Phone",
                                                        tint = GeoPalette.TextSecondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Phone:",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = GeoPalette.TextSecondary,
                                                        modifier = Modifier.width(70.dp)
                                                    )
                                                    Text(
                                                        text = displayPhone,
                                                        fontSize = 13.sp,
                                                        color = GeoPalette.TextPrimary,
                                                        fontWeight = FontWeight.Normal
                                                    )
                                                }

                                                // Registration Date detail
                                                val formattedDate = try {
                                                    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault())
                                                    sdf.format(student.createdAt.toDate())
                                                } catch (e: Exception) {
                                                    "N/A"
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DateRange,
                                                        contentDescription = "Calendar",
                                                        tint = GeoPalette.TextSecondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Registered:",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = GeoPalette.TextSecondary,
                                                        modifier = Modifier.width(70.dp)
                                                    )
                                                    Text(
                                                        text = formattedDate,
                                                        fontSize = 13.sp,
                                                        color = GeoPalette.TextPrimary,
                                                        fontWeight = FontWeight.Normal
                                                    )
                                                }
                                            }

                                            if (student.status.lowercase() == "pending") {
                                                Spacer(modifier = Modifier.height(14.dp))
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Button(
                                                        onClick = { viewModel.updateStudentStatus(student.uid, "approved") },
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.ApprovedBg)
                                                    ) {
                                                        Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    OutlinedButton(
                                                        onClick = { viewModel.updateStudentStatus(student.uid, "rejected") },
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(8.dp),
                                                        border = BorderStroke(1.dp, GeoPalette.RejectedBg),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GeoPalette.RejectedBg)
                                                    ) {
                                                        Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }

    if (showPendingUsersDialog) {
        androidx.compose.ui.window.Dialog(
                onDismissRequest = { showPendingUsersDialog = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(GeoPalette.Background),
                    colors = CardDefaults.cardColors(containerColor = GeoPalette.Background)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(24.dp)
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { showPendingUsersDialog = false }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GeoPalette.Primary)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pending Approvals",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoPalette.TextPrimary
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(GeoPalette.PendingBg.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${pendingStudents.size} Pending",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoPalette.PendingBg
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (pendingStudents.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "No Pending",
                                        tint = GeoPalette.ApprovedBg,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Text(
                                        text = "All Caught Up!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = GeoPalette.TextPrimary
                                    )
                                    Text(
                                        text = "No pending registration requests to approve.",
                                        fontSize = 13.sp,
                                        color = GeoPalette.TextSecondary
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(pendingStudents) { student ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, GeoPalette.Divider)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(44.dp)
                                                            .clip(CircleShape)
                                                            .background(GeoPalette.CardBackground),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (student.photoUrl.isNotBlank()) {
                                                            coil.compose.AsyncImage(
                                                                model = student.photoUrl,
                                                                contentDescription = "Profile Photo",
                                                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                            )
                                                        } else {
                                                            val firstLetter = student.name.firstOrNull()?.toString() ?: "S"
                                                            Text(
                                                                text = firstLetter.uppercase(),
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 16.sp,
                                                                color = GeoPalette.Primary
                                                            )
                                                        }
                                                    }

                                                    Column {
                                                        Text(
                                                            text = student.name,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 15.sp,
                                                            color = GeoPalette.TextPrimary
                                                        )
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(top = 2.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .background(
                                                                        GeoPalette.PendingBg.copy(alpha = 0.1f),
                                                                        RoundedCornerShape(4.dp)
                                                                    )
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = student.status.uppercase(),
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = GeoPalette.PendingBg
                                                                )
                                                            }

                                                            if (student.stream.isNotBlank()) {
                                                                Text(
                                                                    text = student.stream,
                                                                    fontSize = 11.sp,
                                                                    color = GeoPalette.Primary,
                                                                    fontWeight = FontWeight.SemiBold
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                IconButton(
                                                    onClick = {
                                                        viewModel.deleteStudent(student.uid)
                                                    },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Remove student",
                                                        tint = GeoPalette.RejectedBg,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 12.dp),
                                                thickness = 1.dp,
                                                color = GeoPalette.Divider
                                            )

                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.padding(start = 4.dp)
                                            ) {
                                                // Email detail
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Email,
                                                        contentDescription = "Email",
                                                        tint = GeoPalette.TextSecondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Email:",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = GeoPalette.TextSecondary,
                                                        modifier = Modifier.width(70.dp)
                                                    )
                                                    Text(
                                                        text = student.email,
                                                        fontSize = 13.sp,
                                                        color = GeoPalette.TextPrimary,
                                                        fontWeight = FontWeight.Normal
                                                    )
                                                }

                                                // Phone detail
                                                val displayPhone = student.phone.ifBlank { student.mobile }.ifBlank { "N/A" }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Phone,
                                                        contentDescription = "Phone",
                                                        tint = GeoPalette.TextSecondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Phone:",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = GeoPalette.TextSecondary,
                                                        modifier = Modifier.width(70.dp)
                                                    )
                                                    Text(
                                                        text = displayPhone,
                                                        fontSize = 13.sp,
                                                        color = GeoPalette.TextPrimary,
                                                        fontWeight = FontWeight.Normal
                                                    )
                                                }

                                                // Registration Date detail
                                                val formattedDate = try {
                                                    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault())
                                                    sdf.format(student.createdAt.toDate())
                                                } catch (e: Exception) {
                                                    "N/A"
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DateRange,
                                                        contentDescription = "Calendar",
                                                        tint = GeoPalette.TextSecondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Registered:",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = GeoPalette.TextSecondary,
                                                        modifier = Modifier.width(70.dp)
                                                    )
                                                    Text(
                                                        text = formattedDate,
                                                        fontSize = 13.sp,
                                                        color = GeoPalette.TextPrimary,
                                                        fontWeight = FontWeight.Normal
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Button(
                                                    onClick = { viewModel.updateStudentStatus(student.uid, "approved") },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.ApprovedBg)
                                                ) {
                                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                OutlinedButton(
                                                    onClick = { viewModel.updateStudentStatus(student.uid, "rejected") },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, GeoPalette.RejectedBg),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GeoPalette.RejectedBg)
                                                ) {
                                                    Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingStudent != null) {
        val student = editingStudent!!
        var editName by remember { mutableStateOf(student.name) }
        var editPhone by remember { mutableStateOf(student.phone.ifBlank { student.mobile }) }
        var editStream by remember { mutableStateOf(student.stream) }
        var editStatus by remember { mutableStateOf(student.status) }
        var streamExpanded by remember { mutableStateOf(false) }
        var statusExpanded by remember { mutableStateOf(false) }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { editingStudent = null }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GeoPalette.Background),
                border = BorderStroke(1.dp, GeoPalette.CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Edit Student Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeoPalette.DarkText
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = GeoPalette.Primary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GeoPalette.TextPrimary,
                            unfocusedTextColor = GeoPalette.TextPrimary,
                            focusedBorderColor = GeoPalette.Primary,
                            unfocusedBorderColor = GeoPalette.CardBorder
                        )
                    )

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone / Mobile") },
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = GeoPalette.Primary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GeoPalette.TextPrimary,
                            unfocusedTextColor = GeoPalette.TextPrimary,
                            focusedBorderColor = GeoPalette.Primary,
                            unfocusedBorderColor = GeoPalette.CardBorder
                        )
                    )

                    // Stream Dropdown
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Academic Stream",
                            fontSize = 12.sp,
                            color = GeoPalette.TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GeoPalette.CardBorder, RoundedCornerShape(12.dp))
                                .clickable { streamExpanded = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = editStream.ifBlank { "Choose Stream" },
                                    fontSize = 14.sp,
                                    color = GeoPalette.TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown Indicator",
                                    tint = GeoPalette.TextSecondary
                                )
                            }
                            DropdownMenu(
                                expanded = streamExpanded,
                                onDismissRequest = { streamExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                listOf("Science", "Commerce", "Humanities").forEach { choice ->
                                    DropdownMenuItem(
                                        text = { Text(choice, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            editStream = choice
                                            streamExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Status Dropdown
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Account Status",
                            fontSize = 12.sp,
                            color = GeoPalette.TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GeoPalette.CardBorder, RoundedCornerShape(12.dp))
                                .clickable { statusExpanded = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = editStatus.replaceFirstChar { it.uppercase() },
                                    fontSize = 14.sp,
                                    color = GeoPalette.TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown Indicator",
                                    tint = GeoPalette.TextSecondary
                                )
                            }
                            DropdownMenu(
                                expanded = statusExpanded,
                                onDismissRequest = { statusExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                listOf("approved", "pending", "rejected").forEach { choice ->
                                    DropdownMenuItem(
                                        text = { Text(choice.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            editStatus = choice
                                            statusExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { editingStudent = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GeoPalette.CardBorder)
                        ) {
                            Text("Cancel", color = GeoPalette.TextPrimary)
                        }

                        Button(
                            onClick = {
                                if (editName.isNotBlank()) {
                                    viewModel.adminUpdateStudent(
                                        uid = student.uid,
                                        name = editName,
                                        phone = editPhone,
                                        stream = editStream,
                                        status = editStatus
                                    )
                                    editingStudent = null
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary)
                        ) {
                            Text("Save Changes", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminContentTab(viewModel: LearningViewModel) {
    var selectedStreamPage by remember { mutableStateOf<String?>(null) }

    if (selectedStreamPage == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GeoPalette.PrimaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Content Manager",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeoPalette.Primary
                    )
                    Text(
                        "Select an independent stream page below to manage lecture videos, PDF study notes, recorded classes, and entrance mock examinations.",
                        fontSize = 13.sp,
                        color = GeoPalette.TextSecondary
                    )
                }
            }

            Text("Available Academic Streams", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GeoPalette.TextPrimary)

            StreamLandingCard(
                title = "Science",
                description = "Manage curriculum topics: Physics, Chemistry, Biology, and Mathematics.",
                icon = Icons.Default.MenuBook,
                colorAccent = Color(0xFF1976D2),
                onClick = { selectedStreamPage = "Science" }
            )

            StreamLandingCard(
                title = "Commerce",
                description = "Manage business curriculum: Accountancy, Business Studies, Economics, and Finance.",
                icon = Icons.Default.People,
                colorAccent = Color(0xFF388E3C),
                onClick = { selectedStreamPage = "Commerce" }
            )

            StreamLandingCard(
                title = "Humanities",
                description = "Manage arts & humanities curriculum: History, Geography, Political Science, and Literature.",
                icon = Icons.Default.LibraryBooks,
                colorAccent = Color(0xFFD32F2F),
                onClick = { selectedStreamPage = "Humanities" }
            )
        }
    } else {
        StreamContentPage(
            streamName = selectedStreamPage!!,
            viewModel = viewModel,
            onBack = { selectedStreamPage = null }
        )
    }
}

@Composable
fun StreamLandingCard(
    title: String,
    description: String,
    icon: ImageVector,
    colorAccent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, GeoPalette.Divider, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = GeoPalette.CardBackground),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(colorAccent.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorAccent,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "$title Page",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeoPalette.TextPrimary
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = GeoPalette.TextSecondary,
                    lineHeight = 16.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = GeoPalette.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun StreamContentPage(
    streamName: String,
    viewModel: LearningViewModel,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val videos by viewModel.videos.collectAsState()
    val materials by viewModel.materials.collectAsState()
    val recordedClasses by viewModel.recordedClasses.collectAsState()
    val mockTests by viewModel.mockTests.collectAsState()

    val streamVideos = remember(videos, streamName) {
        videos.filter { it.stream.equals(streamName, ignoreCase = true) }
    }
    val streamMaterials = remember(materials, streamName) {
        materials.filter { it.stream.equals(streamName, ignoreCase = true) }
    }
    val streamRecordedClasses = remember(recordedClasses, streamName) {
        recordedClasses.filter { it.stream.equals(streamName, ignoreCase = true) }
    }
    val streamMockTests = remember(mockTests, streamName) {
        mockTests.filter { it.stream.equals(streamName, ignoreCase = true) }
    }

    var videoSearchQuery by remember { mutableStateOf("") }
    var videoSortNewest by remember { mutableStateOf(true) }

    val filteredAndSortedVideos = remember(streamVideos, videoSearchQuery, videoSortNewest) {
        var list = streamVideos.filter { it.title.contains(videoSearchQuery, ignoreCase = true) }
        list = if (videoSortNewest) {
            list.sortedByDescending { it.createdAt.seconds }
        } else {
            list.sortedBy { it.createdAt.seconds }
        }
        list
    }

    var showAddDialog by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf<Any?>(null) }
    var showViewDialog by remember { mutableStateOf<Any?>(null) }
    var isDownloadingId by remember { mutableStateOf<String?>(null) }
    var activeTab by remember { mutableStateOf("videos") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(GeoPalette.CardBackground, RoundedCornerShape(10.dp))
                    .border(1.dp, GeoPalette.Divider, RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = GeoPalette.Primary
                )
            }

            Column {
                Text(
                    text = "$streamName Content Management",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeoPalette.TextPrimary
                )
                Text(
                    text = "Stream Page • Admin Panel",
                    fontSize = 11.sp,
                    color = GeoPalette.TextSecondary
                )
            }
        }

        // Custom top tab bar for the stream tasks
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GeoPalette.CardBackground, RoundedCornerShape(12.dp))
                .border(1.dp, GeoPalette.Divider, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "videos" to "Videos",
                "materials" to "Study Materials",
                "recorded" to "Recorded Classes",
                "tests" to "Mock Tests"
            ).forEach { (tabId, label) ->
                val isSelected = activeTab == tabId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) GeoPalette.Primary else Color.Transparent
                        )
                        .clickable { activeTab = tabId }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isSelected) Color.White else GeoPalette.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Scrollable Content Area for the selected tab
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (activeTab) {
                    "videos" -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GeoPalette.CardBackground),
                            border = BorderStroke(1.dp, GeoPalette.Divider),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Videos", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GeoPalette.Primary)
                                        Text("Curricular lectures & video guides", fontSize = 11.sp, color = GeoPalette.TextSecondary)
                                    }
                                    Button(
                                        onClick = { showAddDialog = "video" },
                                        colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Video", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = videoSearchQuery,
                                        onValueChange = { videoSearchQuery = it },
                                        placeholder = { Text("Search videos...", fontSize = 11.sp) },
                                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp)) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GeoPalette.Primary,
                                            unfocusedBorderColor = GeoPalette.Divider
                                        )
                                    )

                                    Box(
                                        modifier = Modifier
                                            .height(44.dp)
                                            .background(GeoPalette.PrimaryContainer, RoundedCornerShape(8.dp))
                                            .clickable { videoSortNewest = !videoSortNewest }
                                            .padding(horizontal = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (videoSortNewest) "Sort: Newest" else "Sort: Oldest",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GeoPalette.Primary
                                        )
                                    }
                                }

                                if (filteredAndSortedVideos.isEmpty()) {
                                    Text(
                                        text = if (videoSearchQuery.isEmpty()) "No videos uploaded yet." else "No search results found.",
                                        fontSize = 12.sp,
                                        color = GeoPalette.TextSecondary,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        filteredAndSortedVideos.forEach { vid ->
                                            StreamVideoItem(
                                                video = vid,
                                                onView = { showViewDialog = vid },
                                                onEdit = { showEditDialog = vid },
                                                onDelete = { viewModel.deleteContent("videos", vid.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "materials" -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GeoPalette.CardBackground),
                            border = BorderStroke(1.dp, GeoPalette.Divider),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Study Materials", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GeoPalette.Primary)
                                        Text("PDF Notes, guides & documents", fontSize = 11.sp, color = GeoPalette.TextSecondary)
                                    }
                                    Button(
                                        onClick = { showAddDialog = "material" },
                                        colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Material", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (streamMaterials.isEmpty()) {
                                    Text("No study materials uploaded yet.", fontSize = 12.sp, color = GeoPalette.TextSecondary, modifier = Modifier.padding(vertical = 8.dp))
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        streamMaterials.forEach { mat ->
                                            StreamMaterialItem(
                                                material = mat,
                                                onView = { showViewDialog = mat },
                                                onDownload = {
                                                    isDownloadingId = mat.id
                                                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                        kotlinx.coroutines.delay(1500)
                                                        isDownloadingId = null
                                                        android.widget.Toast.makeText(context, "Downloaded '${mat.title}' PDF successfully!", android.widget.Toast.LENGTH_LONG).show()
                                                    }
                                                },
                                                isDownloading = isDownloadingId == mat.id,
                                                onEdit = { showEditDialog = mat },
                                                onDelete = { viewModel.deleteContent("materials", mat.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "recorded" -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GeoPalette.CardBackground),
                            border = BorderStroke(1.dp, GeoPalette.Divider),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Recorded Classes", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GeoPalette.Primary)
                                        Text("Archived recorded lectures", fontSize = 11.sp, color = GeoPalette.TextSecondary)
                                    }
                                    Button(
                                        onClick = { showAddDialog = "recorded_class" },
                                        colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Recorded", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (streamRecordedClasses.isEmpty()) {
                                    Text("No recorded classes available yet.", fontSize = 12.sp, color = GeoPalette.TextSecondary, modifier = Modifier.padding(vertical = 8.dp))
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        streamRecordedClasses.forEach { rec ->
                                            StreamRecordedClassItem(
                                                recClass = rec,
                                                onView = { showViewDialog = rec },
                                                onEdit = { showEditDialog = rec },
                                                onDelete = { viewModel.deleteContent("recordedClasses", rec.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "tests" -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GeoPalette.CardBackground),
                            border = BorderStroke(1.dp, GeoPalette.Divider),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Mock Tests", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GeoPalette.Primary)
                                        Text("Practice exam questions & answers", fontSize = 11.sp, color = GeoPalette.TextSecondary)
                                    }
                                    Button(
                                        onClick = { showAddDialog = "test" },
                                        colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Mock Test", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (streamMockTests.isEmpty()) {
                                    Text("No mock tests available yet.", fontSize = 12.sp, color = GeoPalette.TextSecondary, modifier = Modifier.padding(vertical = 8.dp))
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        streamMockTests.forEach { test ->
                                            StreamMockTestItem(
                                                test = test,
                                                onPreview = { showViewDialog = test },
                                                onEdit = { showEditDialog = test },
                                                onDelete = { viewModel.deleteContent("mockTests", test.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    showAddDialog?.let { dialogType ->
        Dialog(onDismissRequest = { showAddDialog = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, GeoPalette.Divider)
            ) {
                when (dialogType) {
                    "video" -> AddVideoDialogContent(
                        viewModel = viewModel,
                        initialStream = streamName,
                        onDismiss = { showAddDialog = null },
                        onSubmit = { title, description, url, category, stream, thumbnailUrl ->
                            viewModel.addVideo(title, description, url, category, stream, thumbnailUrl)
                            showAddDialog = null
                        }
                    )
                    "material" -> AddMaterialDialogContent(
                        viewModel = viewModel,
                        initialStream = streamName,
                        onDismiss = { showAddDialog = null },
                        onSubmit = { title, fileUrl, category, stream ->
                            viewModel.addMaterial(title, fileUrl, category, stream)
                            showAddDialog = null
                        }
                    )
                    "recorded_class" -> AddRecordedClassDialogContent(
                        initialStream = streamName,
                        onDismiss = { showAddDialog = null },
                        onSubmit = { title, videoUrl, stream ->
                            viewModel.addRecordedClass(title, videoUrl, stream)
                            showAddDialog = null
                        }
                    )
                    "test" -> AddMockTestDialogContent(
                        initialStream = streamName,
                        onDismiss = { showAddDialog = null },
                        onSubmit = { title, testType, stream, questions ->
                            viewModel.addMockTest(title, testType, stream, questions)
                            showAddDialog = null
                        }
                    )
                }
            }
        }
    }

    showEditDialog?.let { item ->
        Dialog(onDismissRequest = { showEditDialog = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, GeoPalette.Divider)
            ) {
                when (item) {
                    is VideoContent -> EditVideoDialogContent(
                        viewModel = viewModel,
                        video = item,
                        onDismiss = { showEditDialog = null },
                        onSubmit = { title, description, url, category, stream, thumbnailUrl ->
                            viewModel.editVideo(item.id, title, description, url, category, stream, thumbnailUrl)
                            showEditDialog = null
                        }
                    )
                    is StudyMaterial -> EditMaterialDialogContent(
                        viewModel = viewModel,
                        material = item,
                        onDismiss = { showEditDialog = null },
                        onSubmit = { title, fileUrl, category, stream ->
                            viewModel.editMaterial(item.id, title, fileUrl, category, stream)
                            showEditDialog = null
                        }
                    )
                    is RecordedClass -> EditRecordedClassDialogContent(
                        recClass = item,
                        onDismiss = { showEditDialog = null },
                        onSubmit = { title, videoUrl, stream ->
                            viewModel.editRecordedClass(item.id, title, videoUrl, stream)
                            showEditDialog = null
                        }
                    )
                    is MockTest -> EditMockTestDialogContent(
                        test = item,
                        onDismiss = { showEditDialog = null },
                        onSubmit = { title, testType, stream, questions ->
                            viewModel.editMockTest(item.id, title, testType, stream, questions)
                            showEditDialog = null
                        }
                    )
                }
            }
        }
    }

    showViewDialog?.let { item ->
        Dialog(onDismissRequest = { showViewDialog = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, GeoPalette.Divider)
            ) {
                when (item) {
                    is VideoContent -> ViewVideoDialogContent(
                        video = item,
                        onDismiss = { showViewDialog = null }
                    )
                    is StudyMaterial -> ViewMaterialDialogContent(
                        material = item,
                        onDismiss = { showViewDialog = null },
                        onDownload = {
                            showViewDialog = null
                            isDownloadingId = item.id
                            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                kotlinx.coroutines.delay(1500)
                                isDownloadingId = null
                                android.widget.Toast.makeText(context, "Downloaded '${item.title}' PDF successfully!", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                    is RecordedClass -> ViewRecordedClassDialogContent(
                        recClass = item,
                        onDismiss = { showViewDialog = null }
                    )
                    is MockTest -> PreviewMockTestDialogContent(
                        test = item,
                        onDismiss = { showViewDialog = null }
                    )
                }
            }
        }
    }
}

@Composable
fun StreamVideoItem(
    video: VideoContent,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, GeoPalette.Divider, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(GeoPalette.PrimaryContainer, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayCircle, null, tint = GeoPalette.Primary, modifier = Modifier.size(24.dp))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(video.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GeoPalette.TextPrimary)
            Text(
                text = "Category: ${video.category} • ${if (video.description.isNotEmpty()) video.description else "No description"}",
                fontSize = 11.sp,
                color = GeoPalette.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onView, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Visibility, "View", tint = GeoPalette.Primary, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, "Edit", tint = Color(0xFF689F38), modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, "Delete", tint = GeoPalette.RejectedBg, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun StreamMaterialItem(
    material: StudyMaterial,
    onView: () -> Unit,
    onDownload: () -> Unit,
    isDownloading: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, GeoPalette.Divider, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFFFFEBEE), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Description, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(22.dp))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(material.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GeoPalette.TextPrimary)
            Text(
                text = "Category: ${material.category} • PDF Notes Document",
                fontSize = 11.sp,
                color = GeoPalette.TextSecondary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isDownloading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = GeoPalette.Primary)
            } else {
                IconButton(onClick = onView, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Visibility, "View", tint = GeoPalette.Primary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDownload, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Download, "Download", tint = Color(0xFF0288D1), modifier = Modifier.size(18.dp))
                }
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, "Edit", tint = Color(0xFF689F38), modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, "Delete", tint = GeoPalette.RejectedBg, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun StreamRecordedClassItem(
    recClass: RecordedClass,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, GeoPalette.Divider, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Tv, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(22.dp))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(recClass.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GeoPalette.TextPrimary)
            Text(
                text = "Class Recording • Video Class Playback",
                fontSize = 11.sp,
                color = GeoPalette.TextSecondary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onView, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Visibility, "Play", tint = GeoPalette.Primary, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, "Edit", tint = Color(0xFF689F38), modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, "Delete", tint = GeoPalette.RejectedBg, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun StreamMockTestItem(
    test: MockTest,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, GeoPalette.Divider, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFFFFF3E0), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Quiz, null, tint = Color(0xFFE65100), modifier = Modifier.size(22.dp))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(test.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GeoPalette.TextPrimary)
            Text(
                text = "Type: ${test.type.uppercase()} • ${test.questions.size} Questions",
                fontSize = 11.sp,
                color = GeoPalette.TextSecondary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreview, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Visibility, "Preview", tint = GeoPalette.Primary, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, "Edit", tint = Color(0xFF689F38), modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, "Delete", tint = GeoPalette.RejectedBg, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun AdminFeedbackTab(viewModel: LearningViewModel) {
    val feedbackList by viewModel.feedbackList.collectAsState()
    val students by viewModel.students.collectAsState()

    if (feedbackList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MailOutline,
                    contentDescription = "Inbox Empty",
                    tint = GeoPalette.TextSecondary,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "Feedback Inbox Empty",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GeoPalette.TextPrimary
                )
                Text(
                    text = "No student feedback messages have been submitted yet.",
                    fontSize = 14.sp,
                    color = GeoPalette.TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(feedbackList) { feed ->
                // Look up student profile in memory
                val senderProfile = remember(students, feed.studentId, feed.userId) {
                    val searchUid = feed.studentId.ifEmpty { feed.userId }
                    students.find { it.uid == searchUid }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("feedback_card_${feed.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, GeoPalette.Divider),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Student Sender Avatar
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(GeoPalette.Primary.copy(alpha = 0.1f))
                                    .border(1.dp, GeoPalette.Divider, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (senderProfile != null && senderProfile.photoUrl.isNotBlank()) {
                                    coil.compose.AsyncImage(
                                        model = senderProfile.photoUrl,
                                        contentDescription = "Sender Photo",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    val firstLetter = senderProfile?.name?.firstOrNull()?.toString() ?: "S"
                                    Text(
                                        text = firstLetter.uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = GeoPalette.Primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = senderProfile?.name ?: "Student User",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = GeoPalette.TextPrimary
                                )
                                Text(
                                    text = if (senderProfile != null) "Stream: ${senderProfile.stream} • Phone: ${senderProfile.phone.ifBlank { senderProfile.mobile }}" else "Registered Student",
                                    fontSize = 11.sp,
                                    color = GeoPalette.TextSecondary
                                )
                            }
                        }

                        Divider(color = GeoPalette.Divider, modifier = Modifier.padding(vertical = 12.dp))

                        Text(
                            text = feed.message,
                            fontSize = 14.sp,
                            color = GeoPalette.TextPrimary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val formattedDate = remember(feed.createdAt) {
                            try {
                                val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                                sdf.format(feed.createdAt.toDate())
                            } catch (e: Exception) {
                                "Just now"
                            }
                        }
                        
                        Text(
                            text = "Submitted: $formattedDate",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = GeoPalette.TextSecondary,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
@Composable
fun AddRecordedClassDialogContent(
    initialStream: String,
    onDismiss: () -> Unit,
    onSubmit: (title: String, videoUrl: String, stream: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var stream by remember { mutableStateOf(initialStream) }

    Column(
        modifier = Modifier
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Upload Recorded Class", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Class Title") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = videoUrl,
            onValueChange = { videoUrl = it },
            label = { Text("Video URL (YouTube or Direct Link)") },
            placeholder = { Text("e.g. https://www.youtube.com/embed/...") },
            modifier = Modifier.fillMaxWidth()
        )

        // Stream Selection (Science, Commerce, Humanities)
        Column {
            Text("Applicable Stream:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GeoPalette.TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Science", "Commerce", "Humanities").forEach { currentStream ->
                    Box(
                        modifier = Modifier
                            .border(1.dp, if (stream == currentStream) GeoPalette.Primary else GeoPalette.CardBorder, RoundedCornerShape(8.dp))
                            .background(if (stream == currentStream) GeoPalette.PrimaryContainer else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { stream = currentStream }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(currentStream, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = { onSubmit(title, videoUrl, stream) },
                colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary),
                modifier = Modifier
                    .weight(1f)
                    .testTag("add_recorded_class_button"),
                shape = RoundedCornerShape(10.dp),
                enabled = title.isNotBlank() && videoUrl.isNotBlank() && stream.isNotBlank()
            ) {
                Text("Upload Class", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// POPUP DIALOG CONTENTS
// ----------------------------------------------------
@Composable
fun AddVideoDialogContent(
    viewModel: LearningViewModel,
    initialStream: String,
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String, url: String, category: String, stream: String, thumbnailUrl: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Domain") }
    var stream by remember { mutableStateOf(initialStream) }
    var thumbnailUrl by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatusMessage by remember { mutableStateOf<String?>(null) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            // Extract file name
            var name = ""
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            name = it.getString(index)
                        }
                    }
                }
            }
            if (name.isEmpty()) {
                name = uri.path ?: ""
                val cut = name.lastIndexOf('/')
                if (cut != -1) {
                    name = name.substring(cut + 1)
                }
            }
            selectedFileName = name.ifEmpty { "lecture_video.mp4" }
            uploadStatusMessage = "Video chosen: $selectedFileName"
        }
    }

    Column(
        modifier = Modifier
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Upload Video Lecture", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Lecture Title") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        // Video File Selector Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GeoPalette.Divider, RoundedCornerShape(12.dp))
                .background(GeoPalette.CardBackground)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Video Upload (Firebase Storage)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GeoPalette.TextSecondary
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { videoPickerLauncher.launch("video/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Upload, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Choose Video", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = if (selectedFileName.isNotEmpty()) selectedFileName else "No video selected",
                    fontSize = 11.sp,
                    color = if (selectedFileName.isNotEmpty()) GeoPalette.TextPrimary else GeoPalette.TextSecondary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            if (uploadStatusMessage != null) {
                Text(
                    text = uploadStatusMessage!!,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (uploadStatusMessage!!.startsWith("Upload failed")) Color.Red else GeoPalette.Primary
                )
            }
        }

        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Or enter YouTube Embed URL") },
            placeholder = { Text("e.g. https://www.youtube.com/embed/dQw4w9WgXcQ") },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Always use YouTube embed format (https://www.youtube.com/embed/VIDEO_ID) to support inline video players.",
            fontSize = 11.sp,
            color = GeoPalette.Primary,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = thumbnailUrl,
            onValueChange = { thumbnailUrl = it },
            label = { Text("Thumbnail Image URL (optional)") },
            placeholder = { Text("e.g. https://example.com/thumb.jpg") },
            modifier = Modifier.fillMaxWidth()
        )

        // Category Selection
        Column {
            Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GeoPalette.TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("General", "Domain", "TeachingAptitude").forEach { cat ->
                    Box(
                        modifier = Modifier
                            .border(1.dp, if (category == cat) GeoPalette.Primary else GeoPalette.CardBorder, RoundedCornerShape(8.dp))
                            .background(if (category == cat) GeoPalette.PrimaryContainer else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { category = cat }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Stream Selection
        Column {
            Text("Applicable Stream:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GeoPalette.TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Science", "Commerce", "Humanities").forEach { currentStream ->
                    Box(
                        modifier = Modifier
                            .border(1.dp, if (stream == currentStream) GeoPalette.Primary else GeoPalette.CardBorder, RoundedCornerShape(8.dp))
                            .background(if (stream == currentStream) GeoPalette.PrimaryContainer else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { stream = currentStream }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(currentStream, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (isUploading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Uploading video file...", fontSize = 12.sp, color = GeoPalette.Primary)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), enabled = !isUploading) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    if (title.isBlank()) {
                        uploadStatusMessage = "Please enter a lecture title"
                        return@Button
                    }
                    if (selectedUri != null) {
                        coroutineScope.launch {
                            isUploading = true
                            uploadStatusMessage = "Uploading file to Storage..."
                            val res = viewModel.repository.uploadFile(selectedUri!!, context)
                            isUploading = false
                            res.onSuccess { uploadedUrl ->
                                url = uploadedUrl
                                uploadStatusMessage = "Upload successful!"
                                onSubmit(title, description, uploadedUrl, category, stream, thumbnailUrl)
                            }.onFailure { err ->
                                uploadStatusMessage = "Upload failed: ${err.localizedMessage}"
                            }
                        }
                    } else {
                        if (url.isBlank()) {
                            uploadStatusMessage = "Please select a video file or enter a YouTube link"
                        } else {
                            onSubmit(title, description, url, category, stream, thumbnailUrl)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary),
                enabled = !isUploading,
                modifier = Modifier
                    .weight(1f)
                    .testTag("add_content_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Upload", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AddMaterialDialogContent(
    viewModel: LearningViewModel,
    initialStream: String,
    onDismiss: () -> Unit,
    onSubmit: (title: String, fileUrl: String, category: String, stream: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var fileUrl by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Domain") }
    var stream by remember { mutableStateOf(initialStream) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatusMessage by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            // Extract file name
            var name = ""
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            name = it.getString(index)
                        }
                    }
                }
            }
            if (name.isEmpty()) {
                name = uri.path ?: ""
                val cut = name.lastIndexOf('/')
                if (cut != -1) {
                    name = name.substring(cut + 1)
                }
            }
            selectedFileName = name.ifEmpty { "study_notes.pdf" }
            uploadStatusMessage = "File chosen: $selectedFileName"
        }
    }

    Column(
        modifier = Modifier
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Upload Study Notes (PDF)", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Material Title") },
            modifier = Modifier.fillMaxWidth()
        )

        // File Selector Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GeoPalette.Divider, RoundedCornerShape(12.dp))
                .background(GeoPalette.CardBackground)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "File Upload (Firebase Storage)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GeoPalette.TextSecondary
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { filePickerLauncher.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Upload, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Choose File", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = if (selectedFileName.isNotEmpty()) selectedFileName else "No file selected",
                    fontSize = 11.sp,
                    color = if (selectedFileName.isNotEmpty()) GeoPalette.TextPrimary else GeoPalette.TextSecondary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            if (uploadStatusMessage != null) {
                Text(
                    text = uploadStatusMessage!!,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (uploadStatusMessage!!.startsWith("Upload failed")) Color.Red else GeoPalette.Primary
                )
            }
        }

        OutlinedTextField(
            value = fileUrl,
            onValueChange = { fileUrl = it },
            label = { Text("Or enter custom URL link") },
            modifier = Modifier.fillMaxWidth()
        )

        // Category Selection
        Column {
            Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GeoPalette.TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("General", "Domain", "TeachingAptitude").forEach { cat ->
                    Box(
                        modifier = Modifier
                            .border(1.dp, if (category == cat) GeoPalette.Primary else GeoPalette.CardBorder, RoundedCornerShape(8.dp))
                            .background(if (category == cat) GeoPalette.PrimaryContainer else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { category = cat }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Stream Selection
        Column {
            Text("Applicable Stream:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GeoPalette.TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Science", "Commerce", "Humanities").forEach { currentStream ->
                    Box(
                        modifier = Modifier
                            .border(1.dp, if (stream == currentStream) GeoPalette.Primary else GeoPalette.CardBorder, RoundedCornerShape(8.dp))
                            .background(if (stream == currentStream) GeoPalette.PrimaryContainer else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { stream = currentStream }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(currentStream, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (isUploading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Uploading material file...", fontSize = 12.sp, color = GeoPalette.Primary)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), enabled = !isUploading) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    if (title.isBlank()) {
                        uploadStatusMessage = "Please enter a material title"
                        return@Button
                    }
                    if (selectedUri != null) {
                        coroutineScope.launch {
                            isUploading = true
                            uploadStatusMessage = "Uploading file to Storage..."
                            val res = viewModel.repository.uploadFile(selectedUri!!, context)
                            isUploading = false
                            res.onSuccess { url ->
                                fileUrl = url
                                uploadStatusMessage = "Upload successful!"
                                onSubmit(title, url, category, stream)
                            }.onFailure { err ->
                                uploadStatusMessage = "Upload failed: ${err.localizedMessage}"
                            }
                        }
                    } else {
                        if (fileUrl.isBlank()) {
                            uploadStatusMessage = "Please select a file or enter a custom link"
                        } else {
                            onSubmit(title, fileUrl, category, stream)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary),
                enabled = !isUploading,
                modifier = Modifier
                    .weight(1f)
                    .testTag("add_content_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Upload Notes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AddMockTestDialogContent(
    initialStream: String,
    onDismiss: () -> Unit,
    onSubmit: (title: String, type: String, stream: String, questions: List<MockQuestion>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("topicwise") }
    var stream by remember { mutableStateOf(initialStream) }

    // List of custom questions created inside dialog
    val questions = remember { mutableStateListOf<MockQuestion>() }

    // Form states for creating a new question
    var questionText by remember { mutableStateOf("") }
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }
    var optionC by remember { mutableStateOf("") }
    var optionD by remember { mutableStateOf("") }
    var correctOptionIndex by remember { mutableStateOf(0) }
    var explanation by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Create Entrance Mock Exam", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Practice Test Title") },
            modifier = Modifier.fillMaxWidth()
        )

        // Type selection
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = type == "topicwise", onClick = { type = "topicwise" })
                Text("Topicwise Quiz", fontSize = 13.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = type == "fulllength", onClick = { type = "fulllength" })
                Text("Full Length Exam", fontSize = 13.sp)
            }
        }

        // Stream selection
        Column {
            Text("Target Stream Exam:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GeoPalette.TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Science", "Commerce", "Humanities").forEach { currentStream ->
                    Box(
                        modifier = Modifier
                            .border(1.dp, if (stream == currentStream) GeoPalette.Primary else GeoPalette.CardBorder, RoundedCornerShape(8.dp))
                            .background(if (stream == currentStream) GeoPalette.PrimaryContainer else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { stream = currentStream }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(currentStream, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Divider(color = GeoPalette.Divider)

        // Question Creator Form
        Text("Add Question MCQ to Mock Test (${questions.size} Added)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GeoPalette.Primary)

        OutlinedTextField(
            value = questionText,
            onValueChange = { questionText = it },
            label = { Text("MCQ Question Text") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = optionA,
            onValueChange = { optionA = it },
            label = { Text("Option A") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = optionB,
            onValueChange = { optionB = it },
            label = { Text("Option B") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = optionC,
            onValueChange = { optionC = it },
            label = { Text("Option C") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = optionD,
            onValueChange = { optionD = it },
            label = { Text("Option D") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Select correct answer index
        Column {
            Text("Correct Option Index:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GeoPalette.TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("A", "B", "C", "D").forEachIndexed { index, optLetter ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = correctOptionIndex == index, onClick = { correctOptionIndex = index })
                        Text(optLetter, fontSize = 13.sp)
                    }
                }
            }
        }

        OutlinedTextField(
            value = explanation,
            onValueChange = { explanation = it },
            label = { Text("Correct answer explanation / proof notes") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (questionText.isBlank() || optionA.isBlank() || optionB.isBlank()) {
                    return@Button
                }
                val newQ = MockQuestion(
                    id = "q_${System.currentTimeMillis()}",
                    questionText = questionText,
                    options = listOf(optionA, optionB, optionC.ifBlank { "N/A" }, optionD.ifBlank { "N/A" }),
                    correctAnswerIndex = correctOptionIndex,
                    explanation = explanation
                )
                questions.add(newQ)
                
                // Clear question fields
                questionText = ""
                optionA = ""
                optionB = ""
                optionC = ""
                optionD = ""
                correctOptionIndex = 0
                explanation = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF689F38)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.AddCircleOutline, null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Confirm Add Question MCQ", fontWeight = FontWeight.Bold)
        }

        Divider(color = GeoPalette.Divider)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = { onSubmit(title, type, stream, questions.toList()) },
                colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary),
                modifier = Modifier
                    .weight(1f)
                    .testTag("add_content_button"),
                shape = RoundedCornerShape(10.dp),
                enabled = questions.isNotEmpty()
            ) {
                Text("Create Test", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// EDIT DIALOG CONTENTS
// ----------------------------------------------------
@Composable
fun EditVideoDialogContent(
    viewModel: LearningViewModel,
    video: VideoContent,
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String, url: String, category: String, stream: String, thumbnailUrl: String) -> Unit
) {
    var title by remember { mutableStateOf(video.title) }
    var description by remember { mutableStateOf(video.description) }
    var url by remember { mutableStateOf(video.url) }
    var category by remember { mutableStateOf(video.category) }
    var stream by remember { mutableStateOf(video.stream) }
    var thumbnailUrl by remember { mutableStateOf(video.thumbnailUrl) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var uploadStatusMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Edit Video Lecture", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GeoPalette.Primary)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Lecture Title") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("YouTube Embed or Video URL") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = thumbnailUrl,
            onValueChange = { thumbnailUrl = it },
            label = { Text("Thumbnail Image URL (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Category Selection
        Column {
            Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GeoPalette.TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("General", "Domain", "TeachingAptitude").forEach { cat ->
                    Box(
                        modifier = Modifier
                            .border(1.dp, if (category == cat) GeoPalette.Primary else GeoPalette.CardBorder, RoundedCornerShape(8.dp))
                            .background(if (category == cat) GeoPalette.PrimaryContainer else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { category = cat }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (uploadStatusMessage != null) {
            Text(uploadStatusMessage!!, color = Color.Red, fontSize = 11.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    if (title.isBlank() || url.isBlank()) {
                        uploadStatusMessage = "Title and URL are required"
                        return@Button
                    }
                    onSubmit(title, description, url, category, stream, thumbnailUrl)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EditMaterialDialogContent(
    viewModel: LearningViewModel,
    material: StudyMaterial,
    onDismiss: () -> Unit,
    onSubmit: (title: String, fileUrl: String, category: String, stream: String) -> Unit
) {
    var title by remember { mutableStateOf(material.title) }
    var fileUrl by remember { mutableStateOf(material.fileUrl) }
    var category by remember { mutableStateOf(material.category) }
    var stream by remember { mutableStateOf(material.stream) }

    var uploadStatusMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Edit Study Notes (PDF)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GeoPalette.Primary)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Material Title") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = fileUrl,
            onValueChange = { fileUrl = it },
            label = { Text("Material PDF URL") },
            modifier = Modifier.fillMaxWidth()
        )

        // Category Selection
        Column {
            Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GeoPalette.TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("General", "Domain", "TeachingAptitude").forEach { cat ->
                    Box(
                        modifier = Modifier
                            .border(1.dp, if (category == cat) GeoPalette.Primary else GeoPalette.CardBorder, RoundedCornerShape(8.dp))
                            .background(if (category == cat) GeoPalette.PrimaryContainer else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { category = cat }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (uploadStatusMessage != null) {
            Text(uploadStatusMessage!!, color = Color.Red, fontSize = 11.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    if (title.isBlank() || fileUrl.isBlank()) {
                        uploadStatusMessage = "Title and URL are required"
                        return@Button
                    }
                    onSubmit(title, fileUrl, category, stream)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EditRecordedClassDialogContent(
    recClass: RecordedClass,
    onDismiss: () -> Unit,
    onSubmit: (title: String, videoUrl: String, stream: String) -> Unit
) {
    var title by remember { mutableStateOf(recClass.title) }
    var videoUrl by remember { mutableStateOf(recClass.videoUrl) }
    var stream by remember { mutableStateOf(recClass.stream) }

    Column(
        modifier = Modifier
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Edit Recorded Class", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GeoPalette.Primary)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Class Title") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = videoUrl,
            onValueChange = { videoUrl = it },
            label = { Text("Video URL") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = { onSubmit(title, videoUrl, stream) },
                colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                enabled = title.isNotBlank() && videoUrl.isNotBlank()
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EditMockTestDialogContent(
    test: MockTest,
    onDismiss: () -> Unit,
    onSubmit: (title: String, type: String, stream: String, questions: List<MockQuestion>) -> Unit
) {
    var title by remember { mutableStateOf(test.title) }
    var type by remember { mutableStateOf(test.type) }
    var stream by remember { mutableStateOf(test.stream) }

    val questions = remember { mutableStateListOf<MockQuestion>().apply { addAll(test.questions) } }

    var questionText by remember { mutableStateOf("") }
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }
    var optionC by remember { mutableStateOf("") }
    var optionD by remember { mutableStateOf("") }
    var correctOptionIndex by remember { mutableStateOf(0) }
    var explanation by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Edit Mock Exam", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GeoPalette.Primary)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Practice Test Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = type == "topicwise", onClick = { type = "topicwise" })
                Text("Topicwise Quiz", fontSize = 13.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = type == "fulllength", onClick = { type = "fulllength" })
                Text("Full Length Exam", fontSize = 13.sp)
            }
        }

        Divider(color = GeoPalette.Divider)

        Text("Current Questions (${questions.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            questions.forEachIndexed { qIdx, q ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GeoPalette.CardBackground, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Q${qIdx + 1}: ${q.questionText}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Ans: Option ${listOf("A", "B", "C", "D").getOrNull(q.correctAnswerIndex) ?: "A"}", fontSize = 10.sp, color = GeoPalette.TextSecondary)
                    }
                    IconButton(onClick = { questions.removeAt(qIdx) }) {
                        Icon(Icons.Default.Delete, "Remove", tint = GeoPalette.RejectedBg, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        Divider(color = GeoPalette.Divider)

        Text("Add New Question MCQ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GeoPalette.Primary)

        OutlinedTextField(
            value = questionText,
            onValueChange = { questionText = it },
            label = { Text("MCQ Question Text") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = optionA,
            onValueChange = { optionA = it },
            label = { Text("Option A") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = optionB,
            onValueChange = { optionB = it },
            label = { Text("Option B") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = optionC,
            onValueChange = { optionC = it },
            label = { Text("Option C") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = optionD,
            onValueChange = { optionD = it },
            label = { Text("Option D") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Column {
            Text("Correct Option Index:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GeoPalette.TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("A", "B", "C", "D").forEachIndexed { index, optLetter ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = correctOptionIndex == index, onClick = { correctOptionIndex = index })
                        Text(optLetter, fontSize = 13.sp)
                    }
                }
            }
        }

        OutlinedTextField(
            value = explanation,
            onValueChange = { explanation = it },
            label = { Text("Correct answer explanation / proof notes") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (questionText.isBlank() || optionA.isBlank() || optionB.isBlank()) {
                    return@Button
                }
                val newQ = MockQuestion(
                    id = "q_${System.currentTimeMillis()}",
                    questionText = questionText,
                    options = listOf(optionA, optionB, optionC.ifBlank { "N/A" }, optionD.ifBlank { "N/A" }),
                    correctAnswerIndex = correctOptionIndex,
                    explanation = explanation
                )
                questions.add(newQ)
                
                // Clear fields
                questionText = ""
                optionA = ""
                optionB = ""
                optionC = ""
                optionD = ""
                correctOptionIndex = 0
                explanation = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF689F38)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.AddCircleOutline, null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Confirm Add Question MCQ", fontWeight = FontWeight.Bold)
        }

        Divider(color = GeoPalette.Divider)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = { onSubmit(title, type, stream, questions.toList()) },
                colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                enabled = questions.isNotEmpty()
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// VIEW / PREVIEW DIALOG CONTENTS
// ----------------------------------------------------
@Composable
fun ViewVideoDialogContent(video: VideoContent, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("View Video Lecture", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GeoPalette.Primary)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, "Close")
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Tv, null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                    Text("PLAYING: ${video.title}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Source: ${video.url}", color = Color.Gray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Title:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(video.title, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Category: ${video.category} • Stream: ${video.stream}", fontSize = 11.sp, color = GeoPalette.TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Description:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(video.description.ifBlank { "No description provided." }, fontSize = 12.sp, color = GeoPalette.TextSecondary)
        }

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary)
        ) {
            Text("Close Player")
        }
    }
}

@Composable
fun ViewMaterialDialogContent(
    material: StudyMaterial,
    onDismiss: () -> Unit,
    onDownload: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("View Study Material Notes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GeoPalette.Primary)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, "Close")
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .border(1.dp, GeoPalette.Divider, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = GeoPalette.Background),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Description, "PDF", tint = Color.Red, modifier = Modifier.size(32.dp))
                    Column {
                        Text(material.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Category: ${material.category} • Notes File", fontSize = 10.sp, color = GeoPalette.TextSecondary)
                    }
                }
                
                Divider(color = GeoPalette.Divider)

                Text("DOCUMENT PREVIEW (MOCK PAGES)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GeoPalette.Primary)
                Text(
                    "Page 1:\nThis document contains official curriculum notes for ${material.stream} stream candidates. Focus heavily on section summaries and practice questions.",
                    fontSize = 11.sp,
                    color = GeoPalette.TextSecondary
                )
                Text(
                    "Page 2:\nFormula & core principles summary. Make sure to solve the mock test associated with this topic under the Mock Tests panel.",
                    fontSize = 11.sp,
                    color = GeoPalette.TextSecondary
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Close")
            }
            Button(
                onClick = onDownload,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary)
            ) {
                Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Download PDF")
            }
        }
    }
}

@Composable
fun ViewRecordedClassDialogContent(recClass: RecordedClass, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("View Recorded Class", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GeoPalette.Primary)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, "Close")
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Videocam, null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                    Text("PLAYING LIVE RECORDING: ${recClass.title}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("URL: ${recClass.videoUrl}", color = Color.Gray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Class Title:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(recClass.title, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Stream: ${recClass.stream} • Duration: ~45 mins", fontSize = 11.sp, color = GeoPalette.TextSecondary)
        }

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary)
        ) {
            Text("Close Player")
        }
    }
}

@Composable
fun PreviewMockTestDialogContent(test: MockTest, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mock Test Preview", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GeoPalette.Primary)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, "Close")
            }
        }

        Text("Test: ${test.title}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("Type: ${test.type.uppercase()} • Stream: ${test.stream} • ${test.questions.size} Questions", fontSize = 12.sp, color = GeoPalette.TextSecondary)

        Divider(color = GeoPalette.Divider)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            test.questions.forEachIndexed { qIdx, q ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GeoPalette.CardBackground)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Question ${qIdx + 1}: ${q.questionText}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        q.options.forEachIndexed { oIdx, opt ->
                            val isCorrect = oIdx == q.correctAnswerIndex
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isCorrect) GeoPalette.ApprovedBg else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = opt,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCorrect) GeoPalette.ApprovedBg else GeoPalette.TextPrimary
                                )
                            }
                        }
                        if (q.explanation.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Explanation: ${q.explanation}", fontSize = 10.sp, color = GeoPalette.TextSecondary, style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                        }
                    }
                }
            }
        }

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary)
        ) {
            Text("Done")
        }
    }
}

@Composable
fun MyProfileDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    onUpdateProfile: (UserProfile) -> Unit,
    onSubmitFeedback: (String) -> Unit,
    viewModel: LearningViewModel? = null
) {
    var activeSection by remember { mutableStateOf("menu") }
    
    // Edit fields state
    var editName by remember { mutableStateOf(profile.name) }
    var editPhone by remember { mutableStateOf(profile.phone.ifBlank { profile.mobile }) }
    var editStream by remember { mutableStateOf(profile.stream.ifBlank { "Humanities" }) }
    
    // Change password state
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    // Expansion state for FAQ items
    var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }
    
    // Preferences toggles
    var dailyReminders by remember { mutableStateOf(true) }
    var pushNotifications by remember { mutableStateOf(true) }
    var saveMobileData by remember { mutableStateOf(false) }
    var syncDarkTheme by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val lightDialogColorScheme = androidx.compose.material3.lightColorScheme(
            primary = GeoPalette.Primary,
            onPrimary = Color.White,
            primaryContainer = GeoPalette.PrimaryContainer,
            onPrimaryContainer = GeoPalette.DarkText,
            secondary = GeoPalette.SecondaryContainer,
            background = Color.White,
            onBackground = GeoPalette.TextPrimary,
            surface = Color.White,
            onSurface = GeoPalette.TextPrimary,
            surfaceVariant = GeoPalette.CardBackground,
            onSurfaceVariant = GeoPalette.TextSecondary,
            outline = GeoPalette.Primary.copy(alpha = 0.5f),
            outlineVariant = GeoPalette.Divider
        )

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = GeoPalette.TextPrimary,
            unfocusedTextColor = GeoPalette.TextPrimary,
            focusedLabelColor = GeoPalette.Primary,
            unfocusedLabelColor = GeoPalette.TextSecondary,
            focusedPlaceholderColor = GeoPalette.TextSecondary.copy(alpha = 0.6f),
            unfocusedPlaceholderColor = GeoPalette.TextSecondary.copy(alpha = 0.6f),
            focusedBorderColor = GeoPalette.Primary,
            unfocusedBorderColor = GeoPalette.CardBorder,
            focusedLeadingIconColor = GeoPalette.Primary,
            unfocusedLeadingIconColor = GeoPalette.TextSecondary,
            cursorColor = GeoPalette.Primary,
            disabledTextColor = GeoPalette.TextSecondary,
            disabledBorderColor = GeoPalette.CardBorder.copy(alpha = 0.5f),
            disabledLabelColor = GeoPalette.TextSecondary.copy(alpha = 0.8f),
            disabledLeadingIconColor = GeoPalette.TextSecondary.copy(alpha = 0.5f)
        )

        MaterialTheme(colorScheme = lightDialogColorScheme) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
            Crossfade(
                targetState = activeSection,
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 250)
            ) { section ->
                when (section) {
                    "menu" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Header Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .background(
                                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                                colors = listOf(
                                                    GeoPalette.SecondaryContainer,
                                                    Color.White
                                                )
                                            )
                                        )
                                ) {
                                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawCircle(
                                            color = GeoPalette.Primary.copy(alpha = 0.05f),
                                            radius = size.width * 0.25f,
                                            center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, 0f)
                                        )
                                        drawCircle(
                                            color = GeoPalette.Primary.copy(alpha = 0.03f),
                                            radius = size.width * 0.2f,
                                            center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.6f)
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = onDismiss,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(12.dp)
                                            .size(32.dp)
                                            .background(Color.Black.copy(alpha = 0.04f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = GeoPalette.DarkText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                
                                // Centered overlapping Avatar
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .align(Alignment.BottomCenter)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .padding(3.dp)
                                            .clip(CircleShape)
                                            .background(GeoPalette.Primary.copy(alpha = 0.1f))
                                            .border(2.dp, GeoPalette.Primary, CircleShape)
                                    ) {
                                        if (profile.photoUrl.isNotBlank()) {
                                            coil.compose.AsyncImage(
                                                model = profile.photoUrl,
                                                contentDescription = "Profile Picture",
                                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Placeholder",
                                                tint = GeoPalette.Primary,
                                                modifier = Modifier.size(48.dp).align(Alignment.Center)
                                            )
                                        }
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .align(Alignment.BottomEnd)
                                            .background(GeoPalette.Primary, CircleShape)
                                            .border(2.dp, Color.White, CircleShape)
                                            .clickable { activeSection = "avatar_selector" },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit photo",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = profile.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = GeoPalette.DarkText,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = if (profile.isAdmin()) "Administrator" else "Student - ${profile.stream.ifBlank { "Humanities" }} Stream",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ProfileMenuItem(
                                    icon = Icons.Default.Person,
                                    title = "Edit Profile",
                                    onClick = { activeSection = "edit_profile" },
                                    tag = "menu_edit_profile"
                                )
                                ProfileMenuItem(
                                    icon = Icons.Default.Lock,
                                    title = "Change Password",
                                    onClick = { activeSection = "change_password" },
                                    tag = "menu_change_password"
                                )
                                ProfileMenuItem(
                                    icon = Icons.Default.HelpOutline,
                                    title = "Help & Support",
                                    onClick = { activeSection = "help" },
                                    tag = "menu_help"
                                )
                                ProfileMenuItem(
                                    icon = Icons.Default.Settings,
                                    title = "Preferences",
                                    onClick = { activeSection = "settings" },
                                    tag = "menu_settings"
                                )
                                if (profile.isAdmin()) {
                                    ProfileMenuItem(
                                        icon = Icons.Default.ChatBubble,
                                        title = "View Student Feedback",
                                        onClick = { activeSection = "feedback" },
                                        tag = "menu_feedback"
                                    )
                                } else {
                                    ProfileMenuItem(
                                        icon = Icons.Default.ChatBubble,
                                        title = "Submit Feedback",
                                        onClick = { activeSection = "feedback" },
                                        tag = "menu_feedback"
                                    )
                                }
                                ProfileMenuItem(
                                    icon = Icons.Default.Logout,
                                    title = "Log out",
                                    onClick = onLogout,
                                    showChevron = false,
                                    tag = "menu_logout"
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                    "edit_profile" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { activeSection = "menu" },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(GeoPalette.SecondaryContainer, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = GeoPalette.Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Edit Profile",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = GeoPalette.DarkText
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, null, tint = GeoPalette.Primary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = true,
                                colors = textFieldColors
                            )
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            OutlinedTextField(
                                value = profile.email,
                                onValueChange = {},
                                label = { Text("Email Address (Locked)") },
                                leadingIcon = { Icon(Icons.Default.Email, null, tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = false,
                                colors = textFieldColors
                            )
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            OutlinedTextField(
                                value = editPhone,
                                onValueChange = { editPhone = it },
                                label = { Text("Phone / Mobile") },
                                leadingIcon = { Icon(Icons.Default.Phone, null, tint = GeoPalette.Primary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = true,
                                colors = textFieldColors
                            )
                            
                            if (profile.isStudent()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                var streamExpanded by remember { mutableStateOf(false) }
                                Column {
                                    Text(
                                        text = "Academic Stream",
                                        fontSize = 12.sp,
                                        color = GeoPalette.TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, GeoPalette.CardBorder, RoundedCornerShape(12.dp))
                                            .let { 
                                                if (profile.isAdmin()) it.clickable { streamExpanded = true } else it
                                            }
                                            .padding(horizontal = 16.dp, vertical = 14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = editStream.ifBlank { "Choose Stream" },
                                                fontSize = 15.sp,
                                                color = GeoPalette.TextPrimary,
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (profile.isAdmin()) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDropDown,
                                                    contentDescription = "Dropdown Indicator",
                                                    tint = GeoPalette.TextSecondary
                                                )
                                            }
                                        }
                                        
                                        if (profile.isAdmin()) {
                                            DropdownMenu(
                                                expanded = streamExpanded,
                                                onDismissRequest = { streamExpanded = false },
                                                modifier = Modifier.fillMaxWidth(0.8f).background(Color.White)
                                            ) {
                                                listOf("Science", "Commerce", "Humanities").forEach { choice ->
                                                    DropdownMenuItem(
                                                        text = { Text(choice, fontWeight = FontWeight.Medium) },
                                                        onClick = {
                                                            editStream = choice
                                                            streamExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = {
                                    if (editName.isNotBlank()) {
                                        val updated = profile.copy(
                                            name = editName,
                                            phone = editPhone,
                                            mobile = editPhone,
                                            stream = if (profile.isStudent()) editStream else profile.stream
                                        )
                                        onUpdateProfile(updated)
                                        activeSection = "menu"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary)
                            ) {
                                Text("Save Profile Changes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                    "change_password" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { activeSection = "menu" },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(GeoPalette.SecondaryContainer, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = GeoPalette.Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Change Password",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = GeoPalette.DarkText
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            OutlinedTextField(
                                value = currentPassword,
                                onValueChange = { currentPassword = it },
                                label = { Text("Current Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = GeoPalette.Primary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (showCurrentPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                        Icon(
                                            imageVector = if (showCurrentPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = Color.Gray
                                        )
                                    }
                                },
                                colors = textFieldColors
                            )
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("New Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = GeoPalette.Primary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (showNewPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                        Icon(
                                            imageVector = if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = Color.Gray
                                        )
                                    }
                                },
                                colors = textFieldColors
                            )
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm New Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = GeoPalette.Primary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (showConfirmPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                        Icon(
                                            imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = Color.Gray
                                        )
                                    }
                                },
                                colors = textFieldColors
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = {
                                    if (newPassword.length >= 6 && newPassword == confirmPassword) {
                                        val updated = profile.copy(password = newPassword)
                                        onUpdateProfile(updated)
                                        activeSection = "menu"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary),
                                enabled = newPassword.length >= 6 && newPassword == confirmPassword
                            ) {
                                Text("Update Password", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                    "help" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { activeSection = "menu" },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(GeoPalette.SecondaryContainer, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = GeoPalette.Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Help & Support",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = GeoPalette.DarkText
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val faqs = listOf(
                                Pair("How do I get my registration approved?", "Our administration team reviews registration requests within 24 hours. Once approved, you'll receive full instant access to NCET & CUET Mock Exams, video tutorials, and study modules."),
                                Pair("Where can I find my mock tests?", "Under the Tests / Exams tab in the main interface, where you'll find custom NCET & CUET preparatory mock tests tailored to your chosen stream."),
                                Pair("Can I change my academic stream?", "Academic streams are assigned during registration. If you selected the wrong stream or need to change it, please contact support at support@insyrlearning.com."),
                                Pair("How do I contact support?", "You can reach us directly via support@insyrlearning.com or submit an instant query through the app Feedback tab.")
                            )
                            
                            faqs.forEachIndexed { idx, faq ->
                                val isExpanded = expandedFaqIndex == idx
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { expandedFaqIndex = if (isExpanded) null else idx },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isExpanded) GeoPalette.SecondaryContainer else GeoPalette.CardBackground),
                                    border = BorderStroke(1.dp, GeoPalette.Primary.copy(alpha = 0.1f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = faq.first,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = GeoPalette.DarkText,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = GeoPalette.Primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        if (isExpanded) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = faq.second,
                                                fontSize = 11.sp,
                                                color = Color.DarkGray,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "settings" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { activeSection = "menu" },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(GeoPalette.SecondaryContainer, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = GeoPalette.Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Preferences",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = GeoPalette.DarkText
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PreferenceToggleRow(
                                    title = "Daily Exam Reminders",
                                    subtitle = "Receive reminders for upcoming tests",
                                    checked = dailyReminders,
                                    onCheckedChange = { dailyReminders = it }
                                )
                                PreferenceToggleRow(
                                    title = "Push Notifications",
                                    subtitle = "Instant alerts for material uploads",
                                    checked = pushNotifications,
                                    onCheckedChange = { pushNotifications = it }
                                )
                                PreferenceToggleRow(
                                    title = "Save Mobile Data",
                                    subtitle = "Stream video player content at lower bitrates",
                                    checked = saveMobileData,
                                    onCheckedChange = { saveMobileData = it }
                                )
                                PreferenceToggleRow(
                                    title = "Sync System Theme",
                                    subtitle = "Auto-toggle light & dark visual theme",
                                    checked = syncDarkTheme,
                                    onCheckedChange = { syncDarkTheme = it }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "App Version: 1.4.2 (Production Build)\nInsyr Learning System © 2026",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    "avatar_selector" -> {
                        val context = LocalContext.current
                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri: Uri? ->
                            if (uri != null) {
                                onUpdateProfile(profile.copy(photoUrl = uri.toString()))
                                activeSection = "edit_profile"
                                Toast.makeText(context, "Profile picture uploaded successfully!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { activeSection = "edit_profile" },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(GeoPalette.SecondaryContainer, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = GeoPalette.Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Upload Profile Picture",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = GeoPalette.DarkText
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // Current Profile Image Preview
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(CircleShape)
                                    .background(GeoPalette.SecondaryContainer)
                                    .border(2.dp, GeoPalette.Primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!profile.photoUrl.isNullOrBlank()) {
                                    coil.compose.AsyncImage(
                                        model = profile.photoUrl,
                                        contentDescription = "Current profile picture",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "No photo",
                                        tint = GeoPalette.TextSecondary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Current Photo",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = GeoPalette.TextSecondary
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // Interactive Upload Area Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { launcher.launch("image/*") },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = GeoPalette.CardBackground),
                                border = BorderStroke(1.5.dp, GeoPalette.Primary.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 36.dp, horizontal = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .background(GeoPalette.PrimaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddAPhoto,
                                            contentDescription = "Upload Icon",
                                            tint = GeoPalette.Primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    
                                    Text(
                                        text = "Upload from Device",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = GeoPalette.Primary
                                    )
                                    
                                    Text(
                                        text = "Support JPEG, PNG, or WEBP up to 5MB",
                                        fontSize = 12.sp,
                                        color = GeoPalette.TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = { launcher.launch("image/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = "Upload icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Choose Image file",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                    "feedback" -> {
                        if (profile.isAdmin() && viewModel != null) {
                            val feedbackList by viewModel.feedbackList.collectAsState()
                            val students by viewModel.students.collectAsState()

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { activeSection = "menu" },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(GeoPalette.SecondaryContainer, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = GeoPalette.Primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Student Feedback",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = GeoPalette.DarkText
                                    )
                                }

                                if (feedbackList.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(300.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MailOutline,
                                                contentDescription = "Inbox Empty",
                                                tint = GeoPalette.TextSecondary,
                                                modifier = Modifier.size(64.dp)
                                            )
                                            Text(
                                                text = "Feedback Inbox Empty",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = GeoPalette.TextPrimary
                                            )
                                        }
                                    }
                                } else {
                                    feedbackList.forEach { feed ->
                                        val senderProfile = remember(students, feed.studentId, feed.userId) {
                                            val searchUid = feed.studentId.ifEmpty { feed.userId }
                                            students.find { it.uid == searchUid }
                                        }
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = GeoPalette.CardBackground),
                                            border = BorderStroke(1.dp, GeoPalette.Divider),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(GeoPalette.Primary.copy(alpha = 0.1f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (senderProfile != null && senderProfile.photoUrl.isNotBlank()) {
                                                            coil.compose.AsyncImage(
                                                                model = senderProfile.photoUrl,
                                                                contentDescription = "Sender Photo",
                                                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                            )
                                                        } else {
                                                            val firstLetter = senderProfile?.name?.firstOrNull()?.toString() ?: "S"
                                                            Text(
                                                                text = firstLetter.uppercase(),
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 12.sp,
                                                                color = GeoPalette.Primary
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = senderProfile?.name ?: "Student User",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = GeoPalette.TextPrimary
                                                        )
                                                        Text(
                                                            text = if (senderProfile != null) "Stream: ${senderProfile.stream} • Phone: ${senderProfile.phone.ifBlank { senderProfile.mobile }}" else "Registered Student",
                                                            fontSize = 10.sp,
                                                            color = GeoPalette.TextSecondary
                                                        )
                                                    }
                                                }
                                                HorizontalDivider(color = GeoPalette.Divider, modifier = Modifier.padding(vertical = 8.dp))
                                                Text(
                                                    text = feed.message,
                                                    fontSize = 13.sp,
                                                    color = GeoPalette.TextPrimary,
                                                    lineHeight = 16.sp
                                                )
                                                val formattedDate = remember(feed.createdAt) {
                                                    try {
                                                        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                                                        sdf.format(feed.createdAt.toDate())
                                                    } catch (e: Exception) {
                                                        "Just now"
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Submitted: $formattedDate",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = GeoPalette.TextSecondary,
                                                    modifier = Modifier.align(Alignment.End)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { activeSection = "menu" },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(GeoPalette.SecondaryContainer, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = GeoPalette.Primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Submit Feedback",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = GeoPalette.DarkText
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "Have questions regarding study topics, exam patterns, or need access to extra resources? Write your feedback here and our platform curators will assist you shortly.",
                                    color = GeoPalette.TextSecondary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                                
                                var feedbackMsg by remember { mutableStateOf("") }
                                
                                OutlinedTextField(
                                    value = feedbackMsg,
                                    onValueChange = { feedbackMsg = it },
                                    placeholder = { Text("Write your message here... e.g. Requesting more questions for Physics Waves test") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = textFieldColors
                                )
                                
                                Button(
                                    onClick = {
                                        if (feedbackMsg.isNotBlank()) {
                                            onSubmitFeedback(feedbackMsg)
                                            feedbackMsg = ""
                                            activeSection = "menu"
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("submit_feedback_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GeoPalette.Primary),
                                    enabled = feedbackMsg.isNotBlank()
                                ) {
                                    Icon(Icons.AutoMirrored.Default.Send, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Submit Message", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    showChevron: Boolean = true,
    tag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(GeoPalette.SecondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GeoPalette.Primary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = GeoPalette.DarkText
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Go",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PreferenceToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = GeoPalette.DarkText
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color.Gray,
                lineHeight = 12.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GeoPalette.Primary,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.LightGray.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(GeoPalette.CardBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GeoPalette.Primary,
                modifier = Modifier.size(18.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = GeoPalette.TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = GeoPalette.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ----------------------------------------------------
// Custom Branded Logos (Dynamic & Vector-Sharp)
// ----------------------------------------------------
@Composable
fun InsyrLogo(
    modifier: Modifier = Modifier,
    tint: Color = GeoPalette.Primary,
    showSubtitle: Boolean = true,
    scale: Float = 1.0f
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size((64 * scale).dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val pathColor = tint
                val cx = w / 2f
                val cy = h / 2f + h * 0.08f
                
                // Drawing the 4 leaves on the left
                for (i in 0..3) {
                    val leafPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx - w * 0.04f, cy - h * (i * 0.05f))
                        cubicTo(
                            cx - w * 0.12f, cy - h * (0.15f + i * 0.06f),
                            cx - w * 0.32f, cy - h * (0.22f + i * 0.04f),
                            cx - w * (0.42f - i * 0.04f), cy - h * (0.18f + i * 0.01f)
                        )
                        cubicTo(
                            cx - w * 0.32f, cy - h * (0.15f + i * 0.03f),
                            cx - w * 0.12f, cy - h * (0.08f + i * 0.04f),
                            cx - w * 0.04f, cy + h * 0.06f - h * (i * 0.05f)
                        )
                        close()
                    }
                    drawPath(leafPath, color = pathColor)
                }
                
                // Drawing the 4 leaves on the right (perfect mirror)
                for (i in 0..3) {
                    val leafPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx + w * 0.04f, cy - h * (i * 0.05f))
                        cubicTo(
                            cx + w * 0.12f, cy - h * (0.15f + i * 0.06f),
                            cx + w * 0.32f, cy - h * (0.22f + i * 0.04f),
                            cx + w * (0.42f - i * 0.04f), cy - h * (0.18f + i * 0.01f)
                        )
                        cubicTo(
                            cx + w * 0.32f, cy - h * (0.15f + i * 0.03f),
                            cx + w * 0.12f, cy - h * (0.08f + i * 0.04f),
                            cx + w * 0.04f, cy + h * 0.06f - h * (i * 0.05f)
                        )
                        close()
                    }
                    drawPath(leafPath, color = pathColor)
                }
            }
        }
        
        Spacer(modifier = Modifier.height((4 * scale).dp))
        
        // "INSYR" Bold Text
        Text(
            text = "INSYR",
            fontWeight = FontWeight.Black,
            fontSize = (32 * scale).sp,
            color = tint,
            letterSpacing = (-1).sp,
            modifier = Modifier.testTag("insyr_logo_text")
        )
        
        if (showSubtitle) {
            // "LEARNING" Subtitle Text with letter spacing
            Text(
                text = "LEARNING",
                fontWeight = FontWeight.SemiBold,
                fontSize = (12 * scale).sp,
                color = tint.copy(alpha = 0.9f),
                letterSpacing = (6 * scale).sp,
                modifier = Modifier
                    .padding(start = (6 * scale).dp)
                    .testTag("insyr_logo_subtitle")
            )
        }
    }
}

@Composable
fun InsyrLogoHorizontal(
    modifier: Modifier = Modifier,
    tint: Color = GeoPalette.Primary,
    scale: Float = 1.0f
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier.size((32 * scale).dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val pathColor = tint
                val cx = w / 2f
                val cy = h / 2f + h * 0.08f
                
                // Drawing the 4 leaves on the left
                for (i in 0..3) {
                    val leafPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx - w * 0.04f, cy - h * (i * 0.05f))
                        cubicTo(
                            cx - w * 0.12f, cy - h * (0.15f + i * 0.06f),
                            cx - w * 0.32f, cy - h * (0.22f + i * 0.04f),
                            cx - w * (0.42f - i * 0.04f), cy - h * (0.18f + i * 0.01f)
                        )
                        cubicTo(
                            cx - w * 0.32f, cy - h * (0.15f + i * 0.03f),
                            cx - w * 0.12f, cy - h * (0.08f + i * 0.04f),
                            cx - w * 0.04f, cy + h * 0.06f - h * (i * 0.05f)
                        )
                        close()
                    }
                    drawPath(leafPath, color = pathColor)
                }
                
                // Drawing the 4 leaves on the right (perfect mirror)
                for (i in 0..3) {
                    val leafPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx + w * 0.04f, cy - h * (i * 0.05f))
                        cubicTo(
                            cx + w * 0.12f, cy - h * (0.15f + i * 0.06f),
                            cx + w * 0.32f, cy - h * (0.22f + i * 0.04f),
                            cx + w * (0.42f - i * 0.04f), cy - h * (0.18f + i * 0.01f)
                        )
                        cubicTo(
                            cx + w * 0.32f, cy - h * (0.15f + i * 0.03f),
                            cx + w * 0.12f, cy - h * (0.08f + i * 0.04f),
                            cx + w * 0.04f, cy + h * 0.06f - h * (i * 0.05f)
                        )
                        close()
                    }
                    drawPath(leafPath, color = pathColor)
                }
            }
        }
        
        Spacer(modifier = Modifier.width((8 * scale).dp))
        
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "INSYR",
                fontWeight = FontWeight.Black,
                fontSize = (18 * scale).sp,
                color = tint,
                letterSpacing = (-0.5).sp,
                lineHeight = (18 * scale).sp
            )
            Text(
                text = "LEARNING",
                fontWeight = FontWeight.Bold,
                fontSize = (8 * scale).sp,
                color = tint.copy(alpha = 0.8f),
                letterSpacing = (2 * scale).sp,
                lineHeight = (8 * scale).sp
            )
        }
    }
}

