package org.polarmeet.redisdistributedserver.service

import kotlinx.coroutines.reactor.awaitSingle
import org.polarmeet.redisdistributedserver.model.Notification
import org.polarmeet.redisdistributedserver.model.NotificationType
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.ReactiveSubscription
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class ReactiveRedisService(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>
) {

    private val logger = LoggerFactory.getLogger(ReactiveRedisService::class.java)

    companion object {
        const val CHANNEL_PREFIX = "notifications"
        const val GENERAL_CHANNEL = "$CHANNEL_PREFIX.general"
        const val PAYMENT_FAILURE_CHANNEL = "$CHANNEL_PREFIX.payment-failure"
        const val PAYMENT_SUCCESS_CHANNEL = "$CHANNEL_PREFIX.payment-success"
    }

    // This function now subscribes only to the exact channel, not a pattern
    fun subscribeToNotifications(type: NotificationType): Flux<out ReactiveSubscription.Message<String, Any>> {
        val channel = when (type) {
            NotificationType.GENERAL -> GENERAL_CHANNEL
            NotificationType.PAYMENT_FAILURE -> PAYMENT_FAILURE_CHANNEL
        }

        println("Subscribing to channel: $channel") // Added logging
        logger.info("Subscribing to channel: $channel")

        // Use listenTo with ChannelTopic instead of PatternTopic
        return redisTemplate.listenTo(ChannelTopic(channel))
            .doOnNext { message ->
                logger.info("Received message on channel $channel: ${message.message}")
            }
            .doOnError { error ->
                logger.error("Error processing message on channel $channel", error)
            }
    }

    suspend fun publishNotification(message: String, type: NotificationType): Boolean {
        val notification = Notification(message, type)
        val channel = when (type) {
            NotificationType.GENERAL -> GENERAL_CHANNEL
            NotificationType.PAYMENT_FAILURE -> PAYMENT_FAILURE_CHANNEL
        }

        logger.info("Publishing to channel $channel: $notification")

        return try {
            redisTemplate.convertAndSend(channel, notification)
                .awaitSingle() > 0
        } catch (e: Exception) {
            logger.error("Failed to publish to channel $channel")
            false
        }
    }
}