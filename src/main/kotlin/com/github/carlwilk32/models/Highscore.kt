package com.github.carlwilk32.models

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.sql.Timestamp
import java.time.Instant

@Serializable
data class Highscore(val name: String, val score: Int)

class HighscoreService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_HIGHSCORES = "" +
                "CREATE TABLE IF NOT EXISTS HIGHSCORES (    " +
                "  ID           SERIAL PRIMARY KEY,         " +
                "  NAME         VARCHAR(255),               " +
                "  SCORE        INT,                        " +
                "  CREATED_AT   TIMESTAMP,                  " +
                "  CREATED_BY   VARCHAR(255))               "
        private const val SELECT_HIGHSCORE_BY_ID = "SELECT name, score FROM highscores WHERE id = ?"
        private const val LIST_SCORES = "SELECT name, score FROM highscores"
        private const val TOP_SCORES = "SELECT name, score FROM highscores ORDER BY score DESC, created_at"
        private const val INSERT_SCORE =
            "INSERT INTO highscores (name, score, created_at, created_by) VALUES (?, ?, ?, ?)"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_HIGHSCORES)
    }

    suspend fun create(highscore: Highscore): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_SCORE, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, highscore.name)
        statement.setInt(2, highscore.score)
        statement.setTimestamp(3, Timestamp.from(Instant.now()))
        statement.setString(4, highscore.name)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted city")
        }
    }

    suspend fun read(id: Int): Highscore = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_HIGHSCORE_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()
        if (resultSet.next()) {
            val name = resultSet.getString("name")
            val score = resultSet.getInt("score")
            return@withContext Highscore(name, score)
        } else {
            throw Exception("Record not found")
        }
    }

    suspend fun list(): List<Highscore> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(LIST_SCORES)
        return@withContext highscores(statement)
    }

    suspend fun top(limit: Int): List<Highscore> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(TOP_SCORES)
        statement.maxRows = limit
        return@withContext highscores(statement)
    }

    private fun highscores(statement: PreparedStatement): MutableList<Highscore> {
        val resultSet = statement.executeQuery()
        val result = mutableListOf<Highscore>()
        while (resultSet.next()) {
            val name = resultSet.getString("name")
            val score = resultSet.getInt("score")
            result.add(Highscore(name, score))
        }
        return result
    }
}
