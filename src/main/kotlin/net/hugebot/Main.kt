package net.hugebot

import java.io.File

fun main(args: Array<String>) {
    val pidFile = File("process.pid").takeIf { it.exists() }
    val process = if (pidFile != null) ProcessHandle.of(pidFile.readText().trim().toLong()) else null

    val updater = Updater(args[0], process)
    updater.update()
}