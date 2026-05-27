package com.example.unum.domain.usecase

import com.example.unum.data.model.BirthInput
import com.example.unum.data.model.CalendarType
import com.example.unum.data.model.NumerologyResultBundle
import com.example.unum.data.repository.NumerologyRepository
import com.example.unum.domain.NumerologyCalculator

class BuildNumerologyResultBundleUseCase(
    private val repository: NumerologyRepository,
    private val calculateNumerologyUseCase: CalculateNumerologyUseCase
) {
    suspend operator fun invoke(userBirthInput: BirthInput): NumerologyResultBundle {
        val calculationInput = when (userBirthInput.calendarType) {
            CalendarType.SOLAR -> NumerologyCalculator.toLunarBirthInput(userBirthInput)
            CalendarType.LUNAR -> userBirthInput
        }
        val displaySolarInput = when (userBirthInput.calendarType) {
            CalendarType.SOLAR -> userBirthInput
            CalendarType.LUNAR -> NumerologyCalculator.toSolarBirthInput(userBirthInput)
        }

        val numbers = calculateNumerologyUseCase(calculationInput)
        val content = repository.getContent(numbers.code, userBirthInput.gender)
        return ConsultationTonePolisher.polishFreeResult(NumerologyResultBundle(
            input = calculationInput,
            numbers = numbers,
            content = content,
            displayInput = displaySolarInput
        ))
    }
}
