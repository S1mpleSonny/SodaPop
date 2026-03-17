package com.sodapop.app.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/**
 * Lightweight Markdown parser that handles:
 * - **bold** and __bold__
 * - *italic* and _italic_
 * - ### headings (h1-h3)
 * - - / * / numbered list items
 * - `inline code`
 *
 * Everything else is rendered as plain text.
 */
object SimpleMarkdown {

    fun parse(text: String): AnnotatedString = buildAnnotatedString {
        val lines = text.split('\n')
        lines.forEachIndexed { index, line ->
            parseLine(line)
            if (index < lines.lastIndex) append('\n')
        }
    }

    private fun AnnotatedString.Builder.parseLine(line: String) {
        val trimmed = line.trimStart()

        // Headings
        when {
            trimmed.startsWith("### ") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                    parseInline(trimmed.removePrefix("### "))
                }
                return
            }
            trimmed.startsWith("## ") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                    parseInline(trimmed.removePrefix("## "))
                }
                return
            }
            trimmed.startsWith("# ") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                    parseInline(trimmed.removePrefix("# "))
                }
                return
            }
        }

        // Unordered list: - item or * item (but not **bold**)
        if (trimmed.startsWith("- ") || (trimmed.startsWith("* ") && !trimmed.startsWith("**"))) {
            append("  •  ")
            parseInline(trimmed.substring(2))
            return
        }

        // Ordered list: 1. item, 2. item, etc.
        val orderedMatch = Regex("""^(\d+)\.\s+(.*)""").find(trimmed)
        if (orderedMatch != null) {
            val (num, content) = orderedMatch.destructured
            append("  $num.  ")
            parseInline(content)
            return
        }

        // Regular line
        parseInline(line)
    }

    private fun AnnotatedString.Builder.parseInline(text: String) {
        var i = 0
        while (i < text.length) {
            when {
                // Inline code `...`
                text[i] == '`' -> {
                    val end = text.indexOf('`', i + 1)
                    if (end > i) {
                        withStyle(SpanStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 13.sp
                        )) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append('`')
                        i++
                    }
                }
                // Bold **...** or __...__
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end > i) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            parseInline(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append("**")
                        i += 2
                    }
                }
                text.startsWith("__", i) -> {
                    val end = text.indexOf("__", i + 2)
                    if (end > i) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            parseInline(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append("__")
                        i += 2
                    }
                }
                // Italic *...* or _..._
                // Must check after ** to avoid conflict
                text[i] == '*' && (i == 0 || text[i - 1] != '*') -> {
                    val end = text.indexOf('*', i + 1)
                    if (end > i && (end + 1 >= text.length || text[end + 1] != '*')) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            parseInline(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append('*')
                        i++
                    }
                }
                text[i] == '_' && (i == 0 || text[i - 1] != '_') -> {
                    val end = text.indexOf('_', i + 1)
                    if (end > i && (end + 1 >= text.length || text[end + 1] != '_')) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            parseInline(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append('_')
                        i++
                    }
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}
