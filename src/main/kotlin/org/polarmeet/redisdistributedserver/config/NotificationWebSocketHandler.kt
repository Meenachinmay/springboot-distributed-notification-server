package org.polarmeet.redisdistributedserver.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.polarmeet.redisdistributedserver.model.Notification
import org.polarmeet.redisdistributedserver.model.NotificationType
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
        // Extract and validate the subscription type
        val subscriptionType = session.handshakeInfo.uri.query
            ?.split("&")
            ?.firstOrNull { it.startsWith("type=") }
            ?.substringAfter("type=")
            ?.let {
                try {
                    NotificationType.valueOf(it.uppercase())
                } catch (e: IllegalArgumentException) {
                    println("Invalid subscription type requested: $it") // Added logging
                    null
                }
            }

        if (subscriptionType == null) {
            println("No subscription type provided in WebSocket connection")
            return Mono.empty()
        }

        println("WebSocket connection established for type: $subscriptionType") // Added logging

        val notificationFlow = redisService.subscribeToNotifications(subscriptionType)
            .doOnNext { message ->
                println("Received message on channel ${subscriptionType}: ${message.message}") // Added logging
            }
            .map { message ->
                val notification = when (val msg = message.message) {
                    is Notification -> {
                        println("Processing notification of type: ${msg.type}") // Added logging
                        if (msg.type != subscriptionType) {
                            println("Message type mismatch! Expected: $subscriptionType, Got: ${msg.type}")
                        }
                        msg
                    }
                    else -> Notification(msg.toString(), subscriptionType)
                }
                session.textMessage(objectMapper.writeValueAsString(notification))
            }

        return session.send(notificationFlow)
    }
}

// You can publish something -> Redis pub/sub -> will push this to distributed server -> will push that
// to connected clients.
// subscribe to a channel (web sockets)