package com.example.unum.domain.usecase

import com.example.unum.data.model.CompatibilityConsultation
import com.example.unum.data.model.CompatibilityRelationshipStatus
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.domain.service.JsonChatClient
import com.example.unum.data.ai.compatibility.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GenerateCompatibilityConsultationUseCase(
    private val chatClient: JsonChatClient
) {
    suspend operator fun invoke(
        apiKey: String,
        maleBundle: NumerologyResultBundle,
        femaleBundle: NumerologyResultBundle,
        concern: String,
        relationshipStatus: CompatibilityRelationshipStatus
    ): CompatibilityConsultation = withContext(Dispatchers.IO) {
        val relationshipNumber = relationshipNumber(maleBundle, femaleBundle)
        val prompt = buildPrompt(
            maleBundle = maleBundle,
            femaleBundle = femaleBundle,
            concern = concern,
            relationshipStatus = relationshipStatus,
            relationshipNumber = relationshipNumber
        )
        val content = chatClient.requestJsonContent(
            apiKey = apiKey,
            model = OPENAI_MODEL,
            systemPrompt = SYSTEM_PROMPT,
            userPrompt = prompt,
            failureLabel = "궁합노트"
        )
        parseConsultation(
            rawContent = content,
            maleBundle = maleBundle,
            femaleBundle = femaleBundle,
            concern = concern,
            relationshipNumber = relationshipNumber,
            relationshipStatus = relationshipStatus
        )
    }
}
