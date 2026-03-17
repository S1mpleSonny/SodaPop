package com.sodapop.app.util

/**
 * Extracts JSON content from LLM responses that may wrap JSON in markdown code blocks.
 *
 * Handles cases like:
 * - Raw JSON: {"key": "value"}
 * - Markdown wrapped: ```json\n{"key": "value"}\n```
 * - Markdown wrapped without lang: ```\n{"key": "value"}\n```
 * - Text before/after JSON: Some text {"key": "value"} more text
 */
object JsonExtractor {

    /**
     * Extracts a JSON object {...} or array [...] from a string that may contain
     * markdown code fences or surrounding text.
     */
    fun extract(raw: String): String {
        val trimmed = raw.trim()

        // Try to parse directly first
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            return trimmed
        }

        // Strip markdown code fences: ```json ... ``` or ``` ... ```
        val codeFenceRegex = Regex("""```(?:json|JSON)?\s*\n?([\s\S]*?)\n?\s*```""")
        val codeFenceMatch = codeFenceRegex.find(trimmed)
        if (codeFenceMatch != null) {
            return codeFenceMatch.groupValues[1].trim()
        }

        // Try to find a JSON object {...} in the text
        val objStart = trimmed.indexOf('{')
        val objEnd = trimmed.lastIndexOf('}')
        if (objStart >= 0 && objEnd > objStart) {
            return trimmed.substring(objStart, objEnd + 1)
        }

        // Try to find a JSON array [...] in the text
        val arrStart = trimmed.indexOf('[')
        val arrEnd = trimmed.lastIndexOf(']')
        if (arrStart >= 0 && arrEnd > arrStart) {
            return trimmed.substring(arrStart, arrEnd + 1)
        }

        // Fallback: return as-is
        return trimmed
    }
}
