package com.github.carlwilk32.routes

import com.github.carlwilk32.models.Highscore
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class HighscoreRoutesTest {

    @Test
    fun testTopOrder() = testApplication {
        postScore("Player1", 123)
        postScore("Player2", 55)
        postScore("Player3", 999)
        postScore("Player4", 11)

        val response = client.get("/highscores?top=3")

        assertEquals(
            """
                [{"name":"Player3","score":999},{"name":"Player1","score":123},{"name":"Player2","score":55}]
            """.trimIndent(), response.bodyAsText()
        )
        assertEquals(HttpStatusCode.OK, response.status)
    }

    suspend fun ApplicationTestBuilder.postScore(name: String, score: Int) {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        client.post("/highscores") {
            contentType(ContentType.Application.Json)
            setBody(Highscore(name, score))
        }
    }

}