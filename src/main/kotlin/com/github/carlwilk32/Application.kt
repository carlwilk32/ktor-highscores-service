package com.github.carlwilk32

import com.github.carlwilk32.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureSerialization()

    val dbConnection = connectToPostgres(true)
    configureRouting(dbConnection)
}
