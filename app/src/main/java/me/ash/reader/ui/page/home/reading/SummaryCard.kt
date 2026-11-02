package me.ash.reader.ui.page.home.reading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SummaryCard(
    summaryState: SummaryState,
    onSummarize: () -> Unit,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = summaryState !is SummaryState.Idle,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                when (summaryState) {
                    is SummaryState.Idle -> {
                        // Handled by AnimatedVisibility
                    }
                    is SummaryState.Loading -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Summarizing...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    is SummaryState.Success -> {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = "AI Summary",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val textColor = MaterialTheme.colorScheme.onSecondaryContainer
                            val primaryColor = MaterialTheme.colorScheme.primary
                            val annotatedSummary = remember(summaryState.summary, textColor, primaryColor) {
                                parseMarkdownSummary(
                                    markdown = summaryState.summary,
                                    baseColor = textColor,
                                    primaryColor = primaryColor,
                                    boldColor = textColor,
                                )
                            }
                            Text(
                                text = annotatedSummary,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                    is SummaryState.Error -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = summaryState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = onSummarize) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Parses markdown formatting in AI summary text into rich AnnotatedString styling.
 * Formats *word* and **word** into bold typography (resolving raw asterisk issues),
 * converts bullet points into clean symbols, and supports code pills and links.
 */
fun parseMarkdownSummary(
    markdown: String,
    baseColor: Color,
    primaryColor: Color,
    boldColor: Color = baseColor,
): AnnotatedString {
    return buildAnnotatedString {
        val lines = markdown.trim().lines()
        val inlineRegex = Regex("""(\*\*\*(.+?)\*\*\*)|(\*\*(.+?)\*\*)|(<b>(.+?)</b>)|(<strong>(.+?)</strong>)|(\*(.+?)\*)|(`(.+?)`)|(_(.+?)_)|(<i>(.+?)</i>)|(<em>(.+?)</em>)|(\[(.+?)\]\((.+?)\))""")

        for ((lineIndex, rawLine) in lines.withIndex()) {
            var line = rawLine.trimEnd()

            // Handle headers (# , ## , ### )
            var isHeader = false
            if (line.startsWith("### ")) {
                isHeader = true
                line = line.removePrefix("### ").trimStart()
            } else if (line.startsWith("## ")) {
                isHeader = true
                line = line.removePrefix("## ").trimStart()
            } else if (line.startsWith("# ")) {
                isHeader = true
                line = line.removePrefix("# ").trimStart()
            }

            // Handle bullet items (* , - , + , • )
            val trimmedLeading = line.trimStart()
            if (trimmedLeading.startsWith("* ") || trimmedLeading.startsWith("- ") || trimmedLeading.startsWith("+ ") || trimmedLeading.startsWith("• ")) {
                val bulletChar = when {
                    trimmedLeading.startsWith("* ") -> "* "
                    trimmedLeading.startsWith("- ") -> "- "
                    trimmedLeading.startsWith("+ ") -> "+ "
                    else -> "• "
                }
                line = "•  " + trimmedLeading.removePrefix(bulletChar).trimStart()
            }

            var currentIndex = 0
            val matches = inlineRegex.findAll(line).toList()

            for (match in matches) {
                if (match.range.first > currentIndex) {
                    val plain = line.substring(currentIndex, match.range.first)
                    if (isHeader) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = boldColor)) {
                            append(plain)
                        }
                    } else {
                        withStyle(SpanStyle(color = baseColor)) {
                            append(plain)
                        }
                    }
                }

                val matchedText = match.value
                when {
                    // ***bold italic***
                    matchedText.startsWith("***") && matchedText.endsWith("***") -> {
                        val content = matchedText.removeSurrounding("***")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, color = boldColor)) {
                            append(content)
                        }
                    }
                    // **bold**
                    matchedText.startsWith("**") && matchedText.endsWith("**") -> {
                        val content = matchedText.removeSurrounding("**")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor)) {
                            append(content)
                        }
                    }
                    // <b>bold</b> or <strong>bold</strong>
                    matchedText.startsWith("<b>") && matchedText.endsWith("</b>") -> {
                        val content = matchedText.removePrefix("<b>").removeSuffix("</b>")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor)) {
                            append(content)
                        }
                    }
                    matchedText.startsWith("<strong>") && matchedText.endsWith("</strong>") -> {
                        val content = matchedText.removePrefix("<strong>").removeSuffix("</strong>")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor)) {
                            append(content)
                        }
                    }
                    // *word* -> bold (renders *word* cleanly bold without raw asterisks)
                    matchedText.startsWith("*") && matchedText.endsWith("*") -> {
                        val content = matchedText.removeSurrounding("*")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor)) {
                            append(content)
                        }
                    }
                    // `code`
                    matchedText.startsWith("`") && matchedText.endsWith("`") -> {
                        val content = matchedText.removeSurrounding("`")
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = primaryColor.copy(alpha = 0.12f),
                                fontWeight = FontWeight.Medium,
                                color = boldColor,
                            )
                        ) {
                            append(" $content ")
                        }
                    }
                    // _italic_
                    matchedText.startsWith("_") && matchedText.endsWith("_") -> {
                        val content = matchedText.removeSurrounding("_")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Italic, color = boldColor)) {
                            append(content)
                        }
                    }
                    // <i>italic</i> or <em>italic</em>
                    matchedText.startsWith("<i>") && matchedText.endsWith("</i>") -> {
                        val content = matchedText.removePrefix("<i>").removeSuffix("</i>")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Italic, color = boldColor)) {
                            append(content)
                        }
                    }
                    matchedText.startsWith("<em>") && matchedText.endsWith("</em>") -> {
                        val content = matchedText.removePrefix("<em>").removeSuffix("</em>")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Italic, color = boldColor)) {
                            append(content)
                        }
                    }
                    // [link](url)
                    matchedText.startsWith("[") && matchedText.contains("](") && matchedText.endsWith(")") -> {
                        val linkText = matchedText.substringAfter("[").substringBefore("](")
                        withStyle(SpanStyle(color = primaryColor, textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Medium)) {
                            append(linkText)
                        }
                    }
                    else -> {
                        append(matchedText)
                    }
                }

                currentIndex = match.range.last + 1
            }

            if (currentIndex < line.length) {
                val remaining = line.substring(currentIndex)
                if (isHeader) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = boldColor)) {
                        append(remaining)
                    }
                } else {
                    withStyle(SpanStyle(color = baseColor)) {
                        append(remaining)
                    }
                }
            }

            if (lineIndex < lines.size - 1) {
                append("\n")
            }
        }
    }
}
