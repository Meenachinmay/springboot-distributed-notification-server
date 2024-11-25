package org.polarmeet.redisdistributedserver.controller

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    @PostMapping("/publish")
    fun publishMessage(@RequestBody message: String): String {
        redisTemplate.convertAndSend("notifications", message)
        return "Message published: $message"
    }

    @GetMapping("/test")
    fun test() = "Service running on port: ${System.getenv("SERVER_PORT") ?: "8080"}"
}