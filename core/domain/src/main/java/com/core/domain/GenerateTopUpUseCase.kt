package com.core.domain

import com.core.data.repository.TerminalContentRepository

class GenerateTopUpUseCase(
    private val terminalContentRepository: TerminalContentRepository
) {
    suspend operator fun invoke() = terminalContentRepository.generateTopUp()
}


