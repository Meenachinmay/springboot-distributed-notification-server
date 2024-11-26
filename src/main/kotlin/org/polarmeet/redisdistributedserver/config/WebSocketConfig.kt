package org.polarmeet.redisdistributedserver.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

@Configuration
class WebSocketConfig {
    @Bean
    fun webSocketHandlerMapping(
        notificationWebSocketHandler: NotificationWebSocketHandler
    ): HandlerMapping {
        val map = mapOf("/ws" to notificationWebSocketHandler)
        val mapping = SimpleUrlHandlerMapping()
        mapping.urlMap = map
        mapping.order = -1
        return mapping
    }

    @Bean
    fun handlerAdapter() = WebSocketHandlerAdapter()
}