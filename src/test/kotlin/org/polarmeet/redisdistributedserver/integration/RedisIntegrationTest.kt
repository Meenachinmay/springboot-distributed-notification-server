package org.polarmeet.redisdistributedserver.integration

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.polarmeet.redisdistributedserver.model.Notification
import org.polarmeet.redisdistributedserver.model.NotificationType
import org.polarmeet.redisdistributedserver.service.ReactiveRedisService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@TestConfiguration
class TestRedisConfiguration {
    companion object {
        val redisContainer = GenericContainer<Nothing>("redis:6-alpine").apply {
            withExposedPorts(6379)
        }
    }

    @PostConstruct
    fun initializeRedis() {
        redisContainer.start()
        System.setProperty("spring.data.redis.host", redisContainer.host)
        System.setProperty("spring.data.redis.port", redisContainer.getMappedPort(6379).toString())
    }
}

@SpringBootTest
@Testcontainers
@Import(TestRedisConfiguration::class)
class RedisIntegrationTest {
    @Autowired
    private lateinit var redisService: ReactiveRedisService

    @Test
    fun `messages should only be received on their designated channels`() {
        val testLatch = CountDownLatch(2) // Will help us wait for message reception
        val receivedMessages = ConcurrentHashMap<NotificationType, MutableList<String>>()

        // Initialize message lists
        receivedMessages[NotificationType.PAYMENT_FAILURE] = mutableListOf()
        receivedMessages[NotificationType.GENERAL] = mutableListOf()

        // Create subscriptions with proper error handling
        val subscriptions = listOf(NotificationType.PAYMENT_FAILURE, NotificationType.GENERAL)
            .map { type ->
                redisService.subscribeToNotifications(type)
                    .doOnNext { message ->
                        val notification = message.message as Notification
                        receivedMessages[type]?.add(notification.message)
                        testLatch.countDown()
                    }
                    .doOnError { error ->
                        println("Error in subscription to $type: ${error.message}")
                    }
                    .subscribe()
            }

        // Give subscriptions time to establish
        Thread.sleep(500)

        runBlocking {
            // Send test messages
            redisService.publishNotification("Payment test", NotificationType.PAYMENT_FAILURE)
            redisService.publishNotification("General test", NotificationType.GENERAL)
        }

        // Wait for messages to be received (with timeout)
        assertTrue(testLatch.await(5, TimeUnit.SECONDS), "Timed out waiting for messages")

        // Clean up subscriptions
        subscriptions.forEach { it.dispose() }

        // Verify message routing
        with(receivedMessages) {
            assertTrue(get(NotificationType.PAYMENT_FAILURE)?.contains("Payment test") == true)
            assertFalse(get(NotificationType.PAYMENT_FAILURE)?.contains("General test") == true)
            assertTrue(get(NotificationType.GENERAL)?.contains("General test") == true)
            assertFalse(get(NotificationType.GENERAL)?.contains("Payment test") == true)
        }
    }
}