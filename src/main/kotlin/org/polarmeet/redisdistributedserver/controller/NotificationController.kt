package org.polarmeet.redisdistributedserver.controller

import org.polarmeet.redisdistributedserver.service.ReactiveRedisService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val redisService: ReactiveRedisService
) {
    @PostMapping("/publish")
    suspend fun publishMessage(@RequestBody message: String): String {
        val published = redisService.publishNotification(message)
        return if (published) {
            "Message published: $message"
        } else {
            "Failed to publish message"
        }
    }

    @GetMapping("/test")
    suspend fun test() = "Service running on port: ${System.getenv("SERVER_PORT") ?: "8080"}"
}