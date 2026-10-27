package com.example.ui.results

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MockTest
import com.example.data.TestAttempt
import com.example.data.UserProfile
import java.text.DateFormat
import java.util.Locale

private val streamOptions = listOf("Science", "Commerce", "Humanities", "General")
private val streamLabels = setOf("science", "commerce", "humanities", "arts", "general", "all")

@Composable
fun StudentTestResultsScreen(
    attempts: List<TestAttempt>,
    tests: List<MockTest>,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubject by rememberSaveable { mutableStateOf<String?>(null) }
    val subjectGroups = remember(attempts, tests) { groupTestAttemptsBySubject(attempts, tests) }

    LaunchedEffect(subjectGroups.keys.toList(), selectedSubject) {
        if (selectedSubject != null && selectedSubject !in subjectGroups) selectedSubject = null
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (selectedSubject != null) {
                IconButton(onClick = { selectedSubject = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to subjects")
                }
            }
            Column {
                Text(selectedSubject ?: "My Results", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    if (selectedSubject == null) "Your completed tests" else "Tests in this subject",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
            IconButton(onClick = onRefresh, enabled = !isLoading) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh results")
            }
        }

        errorMessage?.let { ResultsError(message = it, onRetry = onRefresh) }

        when {
            isLoading && attempts.isEmpty() -> LoadingResults(Modifier.weight(1f))
            attempts.isEmpty() && errorMessage != null -> Spacer(Modifier.weight(1f))
            attempts.isEmpty() && errorMessage == null -> EmptyResults(
                title = "No test results yet",
                description = "Completed tests will appear here."
            )
            else -> if (selectedSubject == null) {
                SubjectCategoryList(
                    subjectGroups = subjectGroups,
                    onSubjectSelected = { selectedSubject = it },
                    modifier = Modifier.weight(1f)
                )
            } else {
                SubjectAttemptList(
                    attempts = subjectGroups[selectedSubject].orEmpty(),
                    tests = tests,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AdminTestResultsScreen(
    students: List<UserProfile>,
    isLoadingStudents: Boolean,
    tests: List<MockTest>,
    attempts: List<TestAttempt>,
    isLoadingAttempts: Boolean,
    attemptsError: String?,
    onSelectStudent: (String) -> Unit,
    onRefreshAttempts: (String) -> Unit,
    onClearSelectedStudent: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStream by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedStudentUid by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedSubject by rememberSaveable { mutableStateOf<String?>(null) }
    var searchQuery by rememberSaveable(selectedStream) { mutableStateOf("") }
    val selectedStudent = students.firstOrNull { it.uid == selectedStudentUid }
    val subjectGroups = remember(attempts, tests) { groupTestAttemptsBySubject(attempts, tests) }

    LaunchedEffect(selectedStudent?.uid) {
        selectedSubject = null
        selectedStudent?.uid?.let(onSelectStudent)
    }
    LaunchedEffect(subjectGroups.keys.toList(), selectedSubject) {
        if (selectedSubject != null && selectedSubject !in subjectGroups) selectedSubject = null
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(selectedSubject ?: "Test Results", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = when {
                        selectedStudent != null && selectedSubject != null -> "Tests in this subject"
                        selectedStudent != null -> "${selectedStudent.name.ifBlank { selectedStudent.email }} · ${selectedStream.orEmpty()}"
                        selectedStream != null -> "${selectedStream} students"
                        else -> "Choose a stream to begin"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (selectedStudent != null) {
                IconButton(onClick = { selectedStudentUid?.let(onRefreshAttempts) }, enabled = !isLoadingAttempts) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh student results")
                }
            }
        }

        when {
            selectedStudent != null -> {
                Row(
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.TextButton(onClick = {
                        if (selectedSubject != null) {
                            selectedSubject = null
                        } else {
                            selectedStudentUid = null
                            onClearSelectedStudent()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Text(if (selectedSubject != null) "Subjects" else "Students")
                    }
                    Spacer(Modifier.weight(1f))
                    Text(selectedStudent.status.replaceFirstChar { it.uppercase() }, fontSize = 12.sp)
                }
                if (attemptsError != null) {
                    ResultsError(
                        message = attemptsError,
                        onRetry = { selectedStudentUid?.let(onRefreshAttempts) }
                    )
                }
                when {
                    isLoadingAttempts && attempts.isEmpty() -> LoadingResults(Modifier.weight(1f))
                    attempts.isEmpty() && attemptsError != null -> Spacer(Modifier.weight(1f))
                    attempts.isEmpty() && attemptsError == null -> EmptyResults(
                        title = "No test results",
                        description = "This student has not completed any tests yet."
                    )
                    else -> if (selectedSubject == null) {
                        SubjectCategoryList(
                            subjectGroups = subjectGroups,
                            onSubjectSelected = { selectedSubject = it },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        SubjectAttemptList(
                            attempts = subjectGroups[selectedSubject].orEmpty(),
                            tests = tests,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            selectedStream != null -> {
                androidx.compose.material3.TextButton(onClick = {
                    selectedStream = null
                    selectedStudentUid = null
                    selectedSubject = null
                    searchQuery = ""
                    onClearSelectedStudent()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Text("Streams")
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    singleLine = true,
                    label = { Text("Find a student") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )

                val matchingStudents = remember(students, selectedStream, searchQuery) {
                    students.filter { student ->
                        student.role == "student" &&
                            studentStreamBucket(student.stream) == selectedStream &&
                            (searchQuery.isBlank() || student.name.contains(searchQuery, true) || student.email.contains(searchQuery, true))
                    }.sortedBy { it.name.ifBlank { it.email }.lowercase(Locale.getDefault()) }
                }

                if (isLoadingStudents && matchingStudents.isEmpty()) {
                    LoadingResults(Modifier.weight(1f))
                } else if (matchingStudents.isEmpty()) {
                    EmptyResults(
                        title = if (searchQuery.isBlank()) "No students in this stream" else "No students found",
                        description = if (searchQuery.isBlank()) "There are no student profiles in $selectedStream." else "Try another name or email."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(matchingStudents, key = { it.uid }) { student ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedStudentUid = student.uid
                                },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(student.name.ifBlank { "Unnamed student" }, fontWeight = FontWeight.SemiBold)
                                    Text(student.email, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(student.status.replaceFirstChar { it.uppercase() }, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                if (isLoadingStudents && students.isEmpty()) {
                    LoadingResults(Modifier.weight(1f))
                } else LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(streamOptions, key = { it }) { stream ->
                        val count = students.count { it.role == "student" && studentStreamBucket(it.stream) == stream }
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedStream = stream
                                selectedStudentUid = null
                                selectedSubject = null
                                onClearSelectedStudent()
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stream, fontWeight = FontWeight.SemiBold)
                                Text("$count students", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectCategoryList(
    subjectGroups: Map<String, List<TestAttempt>>,
    onSubjectSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (subjectGroups.isEmpty()) {
        EmptyResults("No test results", "Completed tests will appear here.", modifier)
        return
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(subjectGroups.entries.toList(), key = { "subject-" + it.key }) { entry ->
            val subject = entry.key
            val count = entry.value.size
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSubjectSelected(subject) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (count == 1) "1 test" else count.toString() + " tests",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = "Open " + subject + " results")
                }
            }
        }
    }
}

@Composable
private fun SubjectAttemptList(
    attempts: List<TestAttempt>,
    tests: List<MockTest>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            attempts,
            key = { it.id.ifBlank { it.testId + "-" + it.submittedAt.seconds + "-" + it.submittedAt.nanoseconds } }
        ) { attempt ->
            TestAttemptCard(attempt = attempt, tests = tests)
        }
    }
}

@Composable
private fun TestAttemptCard(attempt: TestAttempt, tests: List<MockTest>) {
    val title = attempt.testTitle.ifBlank {
        tests.firstOrNull { it.id == attempt.testId }?.title ?: "Completed test"
    }
    val maximum = attempt.maximumScoreForDisplay()
    val percentage = attempt.percentageForDisplay()
    val submittedDate = remember(attempt.submittedAt) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
            .format(attempt.submittedAt.toDate())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(
                text = if (maximum != null) "Score: ${attempt.score} / $maximum" else "Score: ${attempt.score}",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
            Text("Percentage: ${"%.1f".format(Locale.getDefault(), percentage)}%", fontSize = 13.sp)
            Text(
                "Correct: ${attempt.correctAnswers}  ·  Wrong: ${attempt.wrongAnswers}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("Submitted: $submittedDate", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LoadingResults(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
            Text("Loading results…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }
}

@Composable
private fun EmptyResults(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ResultsError(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(message, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

/** Returns subject groups ordered by each group's newest submission, merging case/space variants. */
fun groupTestAttemptsBySubject(
    attempts: List<TestAttempt>,
    tests: List<MockTest>
): Map<String, List<TestAttempt>> {
    val testsById = tests.associateBy { it.id }
    val sorted = attempts.sortedWith(compareByDescending<TestAttempt> { it.submittedAt.seconds }
        .thenByDescending { it.submittedAt.nanoseconds })
    val grouped = linkedMapOf<String, MutableList<TestAttempt>>()
    val displaySubjectByKey = linkedMapOf<String, String>()
    sorted.forEach { attempt ->
        val attemptSubject = attempt.subject.trim()
        val testSubject = testsById[attempt.testId]?.let { test ->
            test.subject.ifBlank { test.domain }.trim()
        }.orEmpty()
        val subject = when {
            testSubject.isNotBlank() && testSubject.lowercase(Locale.ROOT) !in streamLabels -> testSubject
            attemptSubject.isNotBlank() && attemptSubject.lowercase(Locale.ROOT) !in streamLabels -> attemptSubject
            else -> "General / Uncategorised"
        }
        val key = subject.trim().lowercase(Locale.ROOT)
        val displaySubject = displaySubjectByKey.getOrPut(key) { subject.trim() }
        grouped.getOrPut(displaySubject) { mutableListOf() }.add(attempt)
    }
    return grouped.mapValues { (_, groupedAttempts) -> groupedAttempts.toList() }
}

/** Supports attempts written before maxScore was stored. */
fun TestAttempt.maximumScoreForDisplay(): Int? = when {
    maxScore > 0 -> maxScore
    totalQuestions > 0 -> totalQuestions * 4
    else -> null
}

fun TestAttempt.percentageForDisplay(): Double = when {
    maxScore > 0 -> percentage
    percentage > 0.0 -> percentage
    totalQuestions > 0 && correctAnswers > 0 -> correctAnswers * 100.0 / totalQuestions
    else -> percentage
}

private fun studentStreamBucket(stream: String): String = when (stream.trim().lowercase(Locale.ROOT)) {
    "science" -> "Science"
    "commerce" -> "Commerce"
    "humanities", "arts" -> "Humanities"
    else -> "General"
}
