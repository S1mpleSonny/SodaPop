package com.sodapop.app.util

import com.sodapop.app.domain.model.ChatMessage
import com.sodapop.app.domain.model.DialogueMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PromptTemplates {

    fun autoTag(content: String): List<ChatMessage> = listOf(
        ChatMessage(
            "system",
            """You are a thought categorization assistant. Given a user's thought or note,
generate 2-5 relevant tags in the same language as the input.
Return ONLY a JSON array of lowercase tag strings. No explanations."""
        ),
        ChatMessage("user", content)
    )

    fun dailySummary(thoughts: List<Pair<Long, String>>): List<ChatMessage> {
        val formatted = thoughts.mapIndexed { i, (time, content) ->
            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time))
            "${i + 1}. [$timeStr] $content"
        }.joinToString("\n")

        return listOf(
            ChatMessage(
                "system",
                """You are a personal reflection assistant. Analyze the user's thoughts from today.
Respond in the SAME LANGUAGE as the thoughts.
Provide:
1. A concise summary (2-3 paragraphs) of the day's thinking
2. 2-3 key themes you identified
3. 2-3 follow-up questions to deepen reflection
4. Identify thoughts that deserve to be promoted:
   - "promote_to_topic": thoughts that the user has been thinking about repeatedly or that represent a significant direction worth tracking as a topic. Include the thought number and a short reason.
   - "promote_to_belief": thoughts that express a firm judgment, a clear conviction, or a stable insight that could serve as a core belief. Include the thought number and a short reason.
   Only suggest promotions when truly warranted. It's fine to return empty arrays.

Return as JSON:
{"summary": "...", "themes": ["theme1", "theme2"], "questions": ["question1", "question2"], "promote_to_topic": [{"index": 1, "reason": "..."}], "promote_to_belief": [{"index": 2, "reason": "..."}]}"""
            ),
            ChatMessage(
                "user",
                "Here are my thoughts from today (chronological order):\n\n$formatted"
            )
        )
    }

    fun weeklySummary(
        dailySummaries: List<Pair<String, String>>,
        thoughts: List<String>
    ): List<ChatMessage> {
        val summariesFormatted = dailySummaries.joinToString("\n\n") { (day, summary) ->
            "== $day ==\n$summary"
        }
        val thoughtsFormatted = thoughts.take(50).joinToString("\n") { "- $it" }

        return listOf(
            ChatMessage(
                "system",
                """You are a personal growth analyst. Given daily summaries and raw thoughts from the past week, provide:
Respond in the SAME LANGUAGE as the content.
1. A weekly overview (3-4 paragraphs)
2. Trends you noticed (what topics are growing/fading)
3. Thought evolution (how ideas developed across days)
4. 2-3 suggestions for next week's reflection focus

Return as JSON:
{"overview": "...", "trends": ["..."], "evolution": ["..."], "suggestions": ["..."]}"""
            ),
            ChatMessage(
                "user",
                "Daily Summaries:\n$summariesFormatted\n\nAll thoughts this week:\n$thoughtsFormatted"
            )
        )
    }

    fun analyzePrediction(
        thoughtContent: String,
        predictedOutcome: String,
        actualOutcome: String,
        predictionDate: String
    ): List<ChatMessage> = listOf(
        ChatMessage(
            "system",
            """You are an analytical thinking coach. The user made a prediction and now knows the outcome.
Respond in the SAME LANGUAGE as the content.
Analyze:
1. Was the prediction accurate? (score 0.0-1.0)
2. What factors made it right or wrong?
3. What cognitive biases might have been at play?
4. What can the user learn for future predictions?

Return as JSON:
{"accuracy": 0.7, "analysis": "...", "biases": ["..."], "lessons": ["..."]}"""
        ),
        ChatMessage(
            "user",
            """Original prediction ($predictionDate): "$predictedOutcome"
Actual outcome: "$actualOutcome"
Original thought context: "$thoughtContent""""
        )
    )

    fun dialogueSystem(mode: DialogueMode, thoughtContent: String): ChatMessage {
        val prompt = when (mode) {
            DialogueMode.DEVILS_ADVOCATE -> """You are a rigorous devil's advocate. Your job is to:
- Challenge every assumption
- Present the strongest counter-arguments
- Point out what could go wrong
- Ask uncomfortable but constructive questions

Be respectful but relentless. Push the user to strengthen their thinking.
Keep responses concise (2-4 paragraphs max).
Respond in the SAME LANGUAGE as the user.

The original thought being discussed: "$thoughtContent""""

            DialogueMode.EXPANSION -> """You are a creative thinking partner. Your job is to:
- Explore adjacent possibilities
- Suggest unexpected connections to other domains
- Ask "what if" questions that expand the idea
- Help the user see implications they haven't considered

Be enthusiastic and generative. Build on the user's ideas.
Keep responses concise (2-4 paragraphs max).
Respond in the SAME LANGUAGE as the user.

The original thought being discussed: "$thoughtContent""""

            DialogueMode.FEASIBILITY -> """You are a pragmatic strategist. Your job is to:
- Break the idea down into actionable components
- Identify required resources, skills, and timeline
- Flag the biggest risks and bottlenecks
- Suggest a concrete first step

Be practical and specific. Avoid vague encouragement.
Keep responses concise (2-4 paragraphs max).
Respond in the SAME LANGUAGE as the user.

The original thought being discussed: "$thoughtContent""""
        }
        return ChatMessage("system", prompt)
    }

    fun detectContradiction(
        newThought: String,
        beliefs: List<String>
    ): List<ChatMessage> {
        val beliefsFormatted = beliefs.mapIndexed { i, b -> "${i + 1}. \"$b\"" }.joinToString("\n")
        return listOf(
            ChatMessage(
                "system",
                """You are a belief consistency checker. Determine if the new thought contradicts any existing belief.
Respond in the SAME LANGUAGE as the content.

If a contradiction exists, return JSON:
{"contradiction": true, "conflictingBelief": "the belief text", "explanation": "how they conflict"}

If no contradiction, return:
{"contradiction": false}"""
            ),
            ChatMessage(
                "user",
                "Core beliefs:\n$beliefsFormatted\n\nNew thought: \"$newThought\""
            )
        )
    }

    fun findConnections(
        oldThought: String,
        oldDate: String,
        recentThoughts: List<Pair<String, String>>
    ): List<ChatMessage> {
        val recentFormatted = recentThoughts.mapIndexed { i, (id, content) ->
            "${i + 1}. [$id] \"$content\""
        }.joinToString("\n")

        return listOf(
            ChatMessage(
                "system",
                """You are a creative connection engine. Find surprising or insightful connections between the old thought and 1-3 recent ones.
Respond in the SAME LANGUAGE as the content.

Return as JSON:
{"connections": [{"recentThoughtId": "...", "explanation": "..."}]}"""
            ),
            ChatMessage(
                "user",
                "Old thought ($oldDate): \"$oldThought\"\n\nRecent thoughts:\n$recentFormatted"
            )
        )
    }

    fun clusterThoughts(thoughts: List<Pair<String, String>>): List<ChatMessage> {
        val formatted = thoughts.joinToString("\n") { (id, content) -> "$id: $content" }
        return listOf(
            ChatMessage(
                "system",
                """You are a semantic clustering engine. Group the thoughts into 3-8 thematic clusters.
Respond in the SAME LANGUAGE as the content.

Return as JSON:
{"clusters": [{"name": "...", "description": "...", "thoughtIds": ["id1", "id2"]}]}"""
            ),
            ChatMessage("user", formatted)
        )
    }
}
