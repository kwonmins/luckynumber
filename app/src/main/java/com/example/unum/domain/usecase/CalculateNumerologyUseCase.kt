package com.example.unum.domain.usecase

import com.example.unum.data.model.BirthInput
import com.example.unum.data.model.NumerologyNumbers
import com.example.unum.domain.NumerologyCalculator

class CalculateNumerologyUseCase {
    operator fun invoke(input: BirthInput): NumerologyNumbers = NumerologyCalculator.calculate(input)
}
