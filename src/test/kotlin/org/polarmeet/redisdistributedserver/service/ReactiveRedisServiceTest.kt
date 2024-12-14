package org.polarmeet.redisdistributedserver.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.polarmeet.redisdistributedserver.model.Notification
import org.polarmeet.redisdistributedserver.model.NotificationType
import org.springframework.data.redis.connection.ReactiveSubscription
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReactiveRedisServiceTest {
    private lateinit var redisTemplate: ReactiveRedisTemplate<String, Any>
    private lateinit var redisService: ReactiveRedisService

    @BeforeEach
    fun setUp() {
        redisTemplate = mockk()
        redisService = ReactiveRedisService(redisTemplate)
    }

    @Test
    fun `subscribe to general channel should only receive general messages`() {
        // Create mock message for general channel
        val generalMessage = mockk<ReactiveSubscription.Message<String, Any>>()
        every { generalMessage.message } returns Notification("General message", NotificationType.GENERAL)

        // Create mock message for payment channel
        val paymentMessage = mockk<ReactiveSubscription.Message<String, Any>>()
        every { paymentMessage.message } returns Notification("Payment message", NotificationType.PAYMENT_FAILURE)

        // Mock Redis subscription
        every {
            redisTemplate.listenTo(ChannelTopic(ReactiveRedisService.GENERAL_CHANNEL))
        } returns Flux.just(generalMessage)

        // Subscribe to general channel
        val messages = redisService.subscribeToNotifications(NotificationType.GENERAL)
            .collectList()
            .block()

        // Verify we only received general messages
        assertEquals(1, messages?.size)
        assertEquals(
            NotificationType.GENERAL,
            (messages?.first()?.message as Notification).type
        )
    }

    @Test
    fun `publish to specific channel should only send to that channel`() = runBlocking {
        // Mock successful publish
        every {
            redisTemplate.convertAndSend(
                ReactiveRedisService.PAYMENT_FAILURE_CHANNEL,
                any<Notification>()
            )
        } returns Mono.just(1L)

        // Publish to payment failure channel
        val result = redisService.publishNotification(
            "Test message",
            NotificationType.PAYMENT_FAILURE
        )

        // Verify message was published to correct channel
        assertTrue(result)
        verify(exactly = 1) {
            redisTemplate.convertAndSend(
                ReactiveRedisService.PAYMENT_FAILURE_CHANNEL,
                match {
                    (it as Notification).type == NotificationType.PAYMENT_FAILURE
                }
            )
        }

        // Verify message was NOT published to general channel
        verify(exactly = 0) {
            redisTemplate.convertAndSend(
                ReactiveRedisService.GENERAL_CHANNEL,
                any()
            )
        }
    }
}