package com.core.domain

import com.core.data.repository.TerminalContentRepository

class DismissTerminalContentUseCase(
    private val terminalContentRepository: TerminalContentRepository
) {
    suspend operator fun invoke() = terminalContentRepository.dismissContent()
}


