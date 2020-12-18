package net.hugebot

import org.kohsuke.github.GitHubBuilder
import org.kohsuke.github.RateLimitHandler
import java.io.FileOutputStream
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.net.URL
import java.nio.channels.Channels
import java.util.*
import javax.net.ssl.HttpsURLConnection

class Updater(
    private val githubToken: String,
    private val process: Optional<ProcessHandle>?
) {

    fun update() {
        if (process == null) return doUpdate()

        if (process.isPresent && process.get().isAlive) throw RuntimeException("Huge process is alive.")

        doUpdate()
    }

    private fun doUpdate() {
        println("Starting...")
        val props = Properties()
        props["oauth"] = githubToken

        println("Authenticating...")
        val api = GitHubBuilder.fromProperties(props)
            .withRateLimitHandler(RateLimitHandler.WAIT)
            .build()

        val repo = api.getRepository("Blad3Mak3r/HUGE")

        println("Fetching assets...")
        val assets = repo.latestRelease.assets.filter { it.name == "Huge.jar" }
        if (assets.isEmpty()) throw IllegalStateException("Release returned 0 assets.")
        println("Assets found: ${assets.joinToString(" ") { it.name }}")

        println("Downloading ${assets[0].name} (${assets[0].id})...")
        val url = URL("https://api.github.com/repos/Blad3Mak3r/HUGE/releases/assets/${assets[0].id}")
        val connection = url.openConnection() as HttpsURLConnection
        connection.setRequestProperty("Accept", "application/octet-stream")
        connection.setRequestProperty("Authorization", "token ${props.getProperty("oauth")}")
        val channel = Channels.newChannel(connection.inputStream)
        val fileOutputStream = FileOutputStream("Huge.jar")
        val fileChannel = fileOutputStream.channel
        fileChannel.transferFrom(channel, 0, Long.MAX_VALUE)
        channel.close()
        fileOutputStream.close()
        fileChannel.close()

        if (connection.responseCode == 200) println("Se ha descargado la última versión de Huge.")
        else println("No se ha podido descargar la nueva versión de Huge... ${connection.responseMessage}")
    }
}
