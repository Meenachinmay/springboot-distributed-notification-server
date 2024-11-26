package org.polarmeet.redisdistributedserver.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.polarmeet.redisdistributedserver.model.Notification
import org.polarmeet.redisdistributedserver.service.ReactiveRedisService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

@Component
class NotificationWebSocketHandler(
    private val redisService: ReactiveRedisService,
    private val objectMapper: ObjectMapper
) : WebSocketHandler {
    override fun handle(session: WebSocketSession): Mono<Void> {
        val notificationFlow = redisService.subscribeToNotifications()
            .map { message ->
                val notification = when (message.message) {
                    is Notification -> message.message as Notification
                    else -> Notification(message.message.toString())
                }
                session.textMessage(objectMapper.writeValueAsString(notification))
            }
        return session.send(notificationFlow)
    }
}