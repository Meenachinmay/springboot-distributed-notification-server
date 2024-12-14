package org.polarmeet.redisdistributedserver.controller

import org.polarmeet.redisdistributedserver.model.NotificationType
import org.polarmeet.redisdistributedserver.service.ReactiveRedisService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val redisService: ReactiveRedisService
) {
    @PostMapping("/publish")
    suspend fun publishMessage(
        @RequestBody message: String,
        @RequestParam type: NotificationType = NotificationType.GENERAL
    ): String {
        println("Publishing message to type: $type") // Added logging
        val published = redisService.publishNotification(message, type)
        return if (published) {
            "Message published to ${type.name} channel: $message"
        } else {
            "Failed to publish message"
        }
    }

    @GetMapping("/test")
    suspend fun test() = "Service running on port: ${System.getenv("SERVER_PORT") ?: "8080"}"
}