package me.ash.reader.infrastructure.ai

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.ash.reader.BuildConfig
import me.ash.reader.infrastructure.di.IODispatcher
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
            val apiKey = settingsProvider.getOrDefault("geminiApiKey", "").let {
                if (it.isEmpty()) BuildConfig.DEFAULT_GEMINI_API_KEY else it
            }
            
            if (apiKey.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("No Gemini API key configured"))
            }

            val cleanContent = articleContent.replace(Regex("<[^>]*>"), " ").replace(Regex("\\s+"), " ").trim()
            val truncatedContent = if (cleanContent.length > 15000) cleanContent.substring(0, 15000) else cleanContent
            
            val prompt = "Summarize the following article in 2-3 concise sentences. Article title: $articleTitle\n\nArticle content:\n$truncatedContent"
            
            val requestBodyObj = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt)
                        )
                    )
                )
            )

            val jsonMediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = gson.toJson(requestBodyObj).toRequestBody(jsonMediaType)
            
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API returned error: ${response.code}"))
            }
            
            val responseBodyStr = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response body"))
            val responseObj = gson.fromJson(responseBodyStr, GeminiResponse::class.java)
            
            val summary = responseObj.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("Could not parse summary from response"))
                
            Result.success(summary)
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
}
