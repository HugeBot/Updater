package net.hugebot

import java.io.File
import java.lang.IllegalArgumentException

/**
 * Application init
 *
 * @param args Requires 3 arguments, (github_token user/repo file.jar)
 */
fun main(args: Array<String>) {
    if (args.size < 3) throw IllegalArgumentException("Updater require 3 arguments, only ${args.size} present.")
    val pidFile = File("process.pid").takeIf { it.exists() }
    val process = if (pidFile != null) ProcessHandle.of(pidFile.readText().trim().toLong()) else null

    val updater = Updater(args, process)
    updater.update()
}