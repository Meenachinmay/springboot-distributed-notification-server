package org.polarmeet.redisdistributedserver.service

import kotlinx.coroutines.reactor.awaitSingle
import org.polarmeet.redisdistributedserver.model.Notification
import org.springframework.data.redis.connection.ReactiveSubscription
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux


@Service
class ReactiveRedisService(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>
) {
    companion object {
        const val NOTIFICATION_CHANNEL = "notifications"
    }

    fun subscribeToNotifications(): Flux<out ReactiveSubscription.Message<String, Any>> {
        return redisTemplate.listenTo(PatternTopic(NOTIFICATION_CHANNEL))
    }

    suspend fun publishNotification(message: String): Boolean {
        val notification = Notification(message)
        return try {
            redisTemplate.convertAndSend(NOTIFICATION_CHANNEL, notification)
                .awaitSingle() > 0 // Convert Long to Boolean
        } catch (e: Exception) {
            false
        }
    }
}