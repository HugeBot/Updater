package net.hugebot

import org.kohsuke.github.GitHubBuilder
import org.kohsuke.github.RateLimitHandler
import java.io.FileOutputStream
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.net.URL
import java.nio.channels.Channels
import java.util.*
import javax.net.ssl.HttpsURLConnection

class Updater(
    private val args: Array<String>,
    private val process: Optional<ProcessHandle>?
) {

    private val githubToken: String = args[0]
    private val repoName: String = args[1].takeIf { it.contains("/") } ?: throw IllegalArgumentException("Repo name does not contains / .")
    private val fileName: String = args[2].takeIf { it.endsWith(".jar") } ?: throw IllegalArgumentException("File name must end with .jar")

    fun update() {
        if (process == null) return doUpdate()

        if (process.isPresent && process.get().isAlive) throw RuntimeException("$fileName process is alive with pid ${process.get().pid()}.")

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

        val repo = api.getRepository(repoName)

        println("Fetching assets...")
        val assets = repo.latestRelease.listAssets().filter { it.name == fileName }
        if (assets.isEmpty()) throw IllegalStateException("Release returned 0 assets.")
        println("Assets found: ${assets.joinToString(" ") { it.name }}")

        val artifact = assets[0]
        println("Downloading ${artifact.name} id(${assets[0].id}) [${artifact.owner.fullName}]...")
        val url = URL("https://api.github.com/repos/$repoName/releases/assets/${artifact.id}")
        val connection = url.openConnection() as HttpsURLConnection
        connection.setRequestProperty("Accept", "application/octet-stream")
        connection.setRequestProperty("Authorization", "token ${props.getProperty("oauth")}")
        val channel = Channels.newChannel(connection.inputStream)
        val fileOutputStream = FileOutputStream(fileName)
        val fileChannel = fileOutputStream.channel
        fileChannel.transferFrom(channel, 0, Long.MAX_VALUE)
        channel.close()
        fileOutputStream.close()
        fileChannel.close()

        if (connection.responseCode == 200) println("Artifact downloaded successfully.")
        else println("The artifact could not be downloaded. ${connection.responseMessage}")
    }
}
