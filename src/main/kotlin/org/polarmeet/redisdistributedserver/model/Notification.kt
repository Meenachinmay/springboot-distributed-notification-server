package org.polarmeet.redisdistributedserver.model

data class Notification(
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)