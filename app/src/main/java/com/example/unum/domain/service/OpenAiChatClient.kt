package com.example.unum.domain.service

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Thin infrastructure wrapper for the chat-completions API.
 *
 * Domain use cases should focus on "what prompt to send" and "how to interpret
 * the JSON". This client owns the repetitive transport details: request shape,
 * timeouts, API-key validation, and user-safe error messages.
 */
class OpenAiChatClient {
    fun requestJsonContent(
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPrompt: String,
        failureLabel: String
    ): String {
        validateApiKey(apiKey)

        val body = JSONObject()
            .put("model", model)
            .put(
                "messages",
                JSONArray()
                    .put(JSONObject().put("role", "developer").put("content", systemPrompt))
                    .put(JSONObject().put("role", "user").put("content", userPrompt))
            )
            .put("response_format", JSONObject().put("type", "json_object"))

        val response = postJson(
            url = CHAT_COMPLETIONS_URL,
            body = body,
            headers = mapOf("Authorization" to "Bearer ${apiKey.trim()}"),
            failureLabel = failureLabel
        )

        return JSONObject(response)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    private fun validateApiKey(apiKey: String) {
        require(apiKey.isNotBlank()) { "상담 연결 키가 설정되지 않았습니다." }
        require(apiKey.startsWith("sk-")) { "상담 연결 키 형식이 올바르지 않습니다." }
    }

    private fun postJson(
        url: String,
        body: JSONObject,
        headers: Map<String, String>,
        failureLabel: String
    ): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            headers.forEach { (key, value) -> setRequestProperty(key, value) }
        }

        connection.outputStream.use { output ->
            output.write(body.toString().toByteArray(Charsets.UTF_8))
        }

        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }

        val response = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        if (connection.responseCode !in 200..299) {
            throwOpenAiError(connection.responseCode, response, failureLabel)
        }
        return response
    }

    private fun throwOpenAiError(responseCode: Int, response: String, failureLabel: String): Nothing {
        val code = runCatching {
            JSONObject(response)
                .optJSONObject("error")
                ?.optString("code")
                .orEmpty()
        }.getOrDefault("")

        val message = when {
            responseCode == 401 || code == "invalid_api_key" ->
                "상담 연결 키가 유효하지 않습니다. 설정을 다시 확인해주세요."
            responseCode == 429 ->
                "상담 요청 한도를 확인해주세요."
            responseCode in 500..599 ->
                "상담 서버 응답이 불안정합니다. 잠시 뒤 다시 시도해주세요."
            else ->
                "$failureLabel 요청에 실패했습니다. 설정을 확인한 뒤 다시 시도해주세요."
        }

        error(message)
    }

    private companion object {
        const val CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions"
        const val CONNECT_TIMEOUT_MS = 20_000
        const val READ_TIMEOUT_MS = 60_000
    }
}
