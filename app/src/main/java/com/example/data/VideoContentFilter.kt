package com.example.data

/** Returns whether this video is available to a student in the given stream. */
fun VideoContent.matchesStudentStream(studentStream: String): Boolean {
    val contentStream = stream.normalizedVideoLabel()
    val requestedStream = studentStream.normalizedVideoLabel()
    return contentStream.isBlank() || contentStream == "all" ||
        requestedStream.isBlank() || contentStream == requestedStream
}

/** Matches a module using the structured field first and the legacy category field as fallback. */
fun VideoContent.matchesModule(moduleName: String): Boolean {
    val requestedModule = moduleName.normalizedVideoLabel()
    if (requestedModule.isBlank()) return true

    val storedModule = module.normalizedVideoLabel()
    if (storedModule.isNotBlank()) return storedModule == requestedModule

    val storedCategory = category.normalizedVideoLabel()
    return storedCategory.isBlank() || storedCategory == "general" ||
        storedCategory == "all" || storedCategory == requestedModule
}

/**
 * Filters videos for a particular student subject. Explicit subject/domain metadata takes
 * precedence over general tags and text matching, so a Physics video can never fall through
 * into another subject when that subject has no matching videos.
 */
fun filterVideosForStudentSubject(
    videos: List<VideoContent>,
    studentStream: String,
    moduleName: String,
    subjectName: String,
    subjectKeywords: List<String>,
    otherSubjectTerms: List<String> = emptyList()
): List<VideoContent> {
    return videos.filter { video ->
        video.matchesStudentStream(studentStream) &&
            video.matchesStudentSubject(moduleName, subjectName, subjectKeywords, otherSubjectTerms)
    }
}

/** Checks explicit subject metadata first, then safe legacy/general-content fallbacks. */
fun VideoContent.matchesStudentSubject(
    moduleName: String,
    subjectName: String,
    subjectKeywords: List<String>,
    otherSubjectTerms: List<String> = emptyList()
): Boolean {
    val requestedModule = moduleName.normalizedVideoLabel()
    val requestedSubject = subjectName.normalizedVideoLabel()

    val explicitSubjects = listOf(subject, domain)
        .map(String::normalizedVideoLabel)
        .filter(String::isNotBlank)
        .distinct()

    if (explicitSubjects.isNotEmpty()) {
        // Both fields are populated by current upload flows. Requiring all populated
        // labels to agree prevents inconsistent metadata from leaking into another subject.
        return explicitSubjects.all { it == requestedSubject }
    }

    val normalizedCategory = category.normalizedVideoLabel()
    val effectiveModule = module.ifBlank {
        category.takeUnless { it.normalizedVideoLabel() in setOf("", "general", "all") }.orEmpty()
    }.normalizedVideoLabel()

    if (effectiveModule.isNotBlank() && effectiveModule != requestedModule) return false

    val searchText = "$title $description".normalizedVideoLabel()
    val targetTerms = (listOf(subjectName) + subjectKeywords)
        .map(String::normalizedVideoLabel)
        .filter(String::isNotBlank)
    val hasLegacySubjectMatch = targetTerms.any(searchText::contains)
    if (hasLegacySubjectMatch) return true

    val hasOtherSubjectMatch = otherSubjectTerms
        .map(String::normalizedVideoLabel)
        .filter(String::isNotBlank)
        .any(searchText::contains)
    if (hasOtherSubjectMatch) return false

    // A module-level video with no subject is general only within that module.
    if (effectiveModule.isNotBlank()) return true

    // Preserve explicitly general / all content when no subject is assigned.
    return normalizedCategory == "general" || normalizedCategory == "all" ||
        (module.isBlank() && category.isBlank())
}

private fun String.normalizedVideoLabel(): String =
    trim().lowercase().filter(Char::isLetterOrDigit)
