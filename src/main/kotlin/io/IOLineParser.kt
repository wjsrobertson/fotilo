package net.ylophones.fotilo.io

import org.apache.commons.io.IOUtils
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

fun parseLines(page: InputStream): List<String> {
    val reader = BufferedReader(InputStreamReader(page, StandardCharsets.UTF_8))
    return IOUtils.readLines(reader)
}
