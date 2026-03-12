package ru.bonch.pivder.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig(
    private val tokenHashAlgorithmConfiguration: TokenHashAlgorithmConfiguration
) {
    @Bean
    fun hashAlgorithm(): String = tokenHashAlgorithmConfiguration.hashAlgorithm
}