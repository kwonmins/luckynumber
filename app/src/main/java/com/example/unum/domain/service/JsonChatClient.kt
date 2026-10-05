package com.example.unum.domain.service

/** Transport contract: domain orchestration does not depend on HTTP implementation. */
interface JsonChatClient {
    fun requestJsonContent(
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPrompt: String,
        failureLabel: String,
        maxCompletionTokens: Int = 3_200
    ): String
}
