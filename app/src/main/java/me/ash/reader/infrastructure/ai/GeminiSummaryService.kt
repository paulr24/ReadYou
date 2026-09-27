package me.ash.reader.infrastructure.ai

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.ash.reader.BuildConfig
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.preference.GeminiModelPreference
import me.ash.reader.infrastructure.preference.SettingsProvider
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiSummaryService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val settingsProvider: SettingsProvider,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val gson = Gson()

    suspend fun summarize(articleTitle: String, articleContent: String): Result<String> = withContext(ioDispatcher) {
        try {
            val customEndpoint = settingsProvider.getOrDefault("aiApiEndpoint", "").trim()
            val apiKey = settingsProvider.getOrDefault("geminiApiKey", "").let {
                if (it.isEmpty()) BuildConfig.DEFAULT_GEMINI_API_KEY else it
            }

            if (apiKey.isEmpty() && customEndpoint.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("No AI API key configured"))
            }

            val model = settingsProvider.getOrDefault("geminiModel", "").let {
                if (it.isBlank()) GeminiModelPreference.default else it.trim()
            }

            val cleanContent = articleContent.replace(Regex("<[^>]*>"), " ").replace(Regex("\\s+"), " ").trim()
            val truncatedContent = if (cleanContent.length > 15000) cleanContent.substring(0, 15000) else cleanContent

            val prompt = "Summarize the following article in 2-3 concise sentences. Article title: $articleTitle\n\nArticle content:\n$truncatedContent"

            val jsonMediaType = "application/json; charset=utf-8".toMediaType()

            val isCustomOpenAiCompatible = customEndpoint.isNotBlank() &&
                !customEndpoint.contains("generativelanguage.googleapis.com")

            val request = if (isCustomOpenAiCompatible) {
                val normalizedEndpoint = customEndpoint.trimEnd('/')
                val url = if (normalizedEndpoint.endsWith("/chat/completions")) {
                    normalizedEndpoint
                } else {
                    "$normalizedEndpoint/chat/completions"
                }

                val openAiRequest = OpenAiChatRequest(
                    model = model,
                    messages = listOf(
                        OpenAiMessage(role = "user", content = prompt)
                    )
                )

                val requestBuilder = Request.Builder()
                    .url(url)
                    .post(gson.toJson(openAiRequest).toRequestBody(jsonMediaType))

                if (apiKey.isNotBlank()) {
                    requestBuilder.header("Authorization", "Bearer $apiKey")
                }

                requestBuilder.build()
            } else {
                val geminiRequest = GeminiRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = prompt)))
                    )
                )

                Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
                    .post(gson.toJson(geminiRequest).toRequestBody(jsonMediaType))
                    .build()
            }

            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorMsg = response.body?.string() ?: ""
                return@withContext Result.failure(Exception("API returned error ${response.code}: $errorMsg"))
            }

            val responseBodyStr = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response body"))

            val summary = if (isCustomOpenAiCompatible) {
                val openAiResponse = gson.fromJson(responseBodyStr, OpenAiChatResponse::class.java)
                openAiResponse.choices?.firstOrNull()?.message?.content
            } else {
                val geminiResponse = gson.fromJson(responseBodyStr, GeminiResponse::class.java)
                geminiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            } ?: return@withContext Result.failure(Exception("Could not parse summary from response"))

            Result.success(summary.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private data class GeminiRequest(
        @SerializedName("contents") val contents: List<Content>
    )

    private data class Content(
        @SerializedName("parts") val parts: List<Part>
    )

    private data class Part(
        @SerializedName("text") val text: String
    )

    private data class GeminiResponse(
        @SerializedName("candidates") val candidates: List<Candidate>?
    )

    private data class Candidate(
        @SerializedName("content") val content: Content?
    )

    private data class OpenAiChatRequest(
        @SerializedName("model") val model: String,
        @SerializedName("messages") val messages: List<OpenAiMessage>,
    )

    private data class OpenAiMessage(
        @SerializedName("role") val role: String,
        @SerializedName("content") val content: String,
    )

    private data class OpenAiChatResponse(
        @SerializedName("choices") val choices: List<OpenAiChoice>?,
    )

    private data class OpenAiChoice(
        @SerializedName("message") val message: OpenAiMessage?,
    )
}
