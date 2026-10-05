package com.example.unum.domain.usecase

import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.model.PremiumConsultation
import com.example.unum.data.model.PremiumTopic
import com.example.unum.domain.service.JsonChatClient
import com.example.unum.data.ai.premium.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeneratePremiumConsultationUseCase(
    private val chatClient: JsonChatClient
) {
    suspend operator fun invoke(
        apiKey: String,
        topic: PremiumTopic,
        concern: String,
        bundle: NumerologyResultBundle
    ): PremiumConsultation = withContext(Dispatchers.IO) {
        val prompt = buildPrompt(topic, concern, bundle)
        val content = chatClient.requestJsonContent(
            apiKey = apiKey,
            model = OPENAI_MODEL,
            systemPrompt = SYSTEM_PROMPT,
            userPrompt = prompt,
            failureLabel = "운세노트"
        )
        parseConsultation(content, topic, bundle)
    }
}
