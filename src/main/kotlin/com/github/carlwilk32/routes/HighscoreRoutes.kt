package com.github.carlwilk32.routes

import com.github.carlwilk32.models.Highscore
import com.github.carlwilk32.models.HighscoreService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection

fun Route.highscoreRouting(dbConnection: Connection) {
    val highscoreService = HighscoreService(dbConnection)

    route("/highscores") {
        get {
            val top = call.request.queryParameters["top"]
            val highscores = top?.let { highscoreService.top(it.toInt()) } ?: highscoreService.list()
            call.respond(HttpStatusCode.OK, highscores)
        }
        get("{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            try {
                val city = highscoreService.read(id)
                call.respond(HttpStatusCode.OK, city)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        post {
            val highscore = call.receive<Highscore>()
            val id = highscoreService.create(highscore)
            call.respond(HttpStatusCode.Created, id)
        }
    }
}
