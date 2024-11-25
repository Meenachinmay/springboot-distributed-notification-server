package org.polarmeet.redisdistributedserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RedisDistributedServerApplication

fun main(args: Array<String>) {
    runApplication<RedisDistributedServerApplication>(*args)
}
