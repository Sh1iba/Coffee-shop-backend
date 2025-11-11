package ru.mireadev.coffeeshop.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Files
import java.nio.file.Paths


@Service
class ImageStorageService(
    @Value("\${app.images.directory}") private val imagesDirectory: String
){
    init {
        Files.createDirectories(Paths.get(imagesDirectory))
    }

    fun getImageResource(fileName: String): Resource {
        val filePath = Paths.get(imagesDirectory, fileName)
        val resource = UrlResource(filePath.toUri())

        if (resource.exists() && resource.isReadable) {
            return resource
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found: $fileName")
        }
    }
}