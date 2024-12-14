package org.polarmeet.redisdistributedserver.model

import com.fasterxml.jackson.annotation.JsonTypeInfo

enum class NotificationType {
    GENERAL,
    PAYMENT_FAILURE,
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
data class Notification(
    val message: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis()
) {
    // Add a no-args constructor for Jackson
    constructor() : this("", NotificationType.GENERAL, System.currentTimeMillis())
}