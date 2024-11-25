package org.polarmeet.redisdistributedserver.service

import org.polarmeet.redisdistributedserver.model.Notification
import org.springframework.data.redis.connection.MessageListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class RedisMessageSubscriber(
    private val messagingTemplate: SimpMessagingTemplate
) : MessageListener {

    override fun onMessage(message: org.springframework.data.redis.connection.Message, pattern: ByteArray?) {
        val notification = Notification(
            message = String(message.body)
        )

        // Broadcast to all connected WebSocket clients
        messagingTemplate.convertAndSend("/topic/notifications", notification)
    }
}