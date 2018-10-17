package com.procurement.auction

import com.fasterxml.jackson.core.JsonFactory
import java.io.StringWriter
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

class JsonResource {
    fun load(fileName: String): String {
        val path = javaClass.classLoader.getResource(fileName).path
        val resource = read(path)
        return toCompact(resource)
    }

    private fun read(pathToFile: String): String {
        val path = Paths.get(pathToFile)
        val buffer = Files.readAllBytes(path)
        return String(buffer, Charset.defaultCharset())
    }

    private fun toCompact(source: String): String {
        val factory = JsonFactory()
        val parser = factory.createParser(source)
        val out = StringWriter()
        factory.createGenerator(out).use { gen ->
            while (parser.nextToken() != null) {
                gen.copyCurrentEvent(parser)
            }
        }
        return out.buffer.toString()
    }
}
