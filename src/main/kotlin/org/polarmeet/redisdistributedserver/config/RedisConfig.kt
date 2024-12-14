package org.polarmeet.redisdistributedserver.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {
    @Bean
    fun reactiveRedisTemplate(
        connectionFactory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, Any> {
        // Create an ObjectMapper configured for our needs
        val objectMapper = ObjectMapper().apply {
            // Enable Kotlin support for proper data class handling
            registerModule(KotlinModule.Builder().build())

            // Configure deserialization features for more flexible JSON parsing
            configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false)
            configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)

            // Set up type handling for proper object reconstruction
            activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                    .allowIfBaseType(Any::class.java)
                    .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
            )
        }

        // Create the JSON serializer with our configured ObjectMapper
        val serializer = Jackson2JsonRedisSerializer(objectMapper, Any::class.java)

        // Build the serialization context with just what we need for pub/sub
        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, Any>(StringRedisSerializer())
            // Configure the value serializer for our messages
            .value(serializer)
            .build()

        return ReactiveRedisTemplate(connectionFactory, serializationContext)
    }
}