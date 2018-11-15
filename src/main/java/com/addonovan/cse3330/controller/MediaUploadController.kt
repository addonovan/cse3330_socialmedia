package com.addonovan.cse3330.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.lang.IllegalStateException
import java.nio.file.Files
import java.nio.file.Paths

@RestController
@RequestMapping("/media")
open class MediaUploadController {

    /**
     * The type of file being uploaded.
     */
    private enum class UploadType(dirName: String) {

        ProfileImage("profiles"),
        HeaderImage("headers"),
        PostAttachment("posts");

        /** The location */
        val directory = "resources/static/media/$dirName".apply {
            val path = Paths.get(this)
            if (Files.exists(path)) return@apply

            Files.createDirectories(path)
            if (Files.notExists(path))
                throw IllegalStateException("Failed to create necessary media directory: $this")
        }

    }

    sealed class MediaUploadResponse(val success: Boolean) {

        companion object {
            fun fail(message: String) = FailedUploadResponse(message)

            fun success(link: String) = SuccessfulUploadResponse(link)
        }

        data class SuccessfulUploadResponse(val link: String) : MediaUploadResponse(true)

        data class FailedUploadResponse(val message: String) : MediaUploadResponse(false)

    }

    //
    // End points
    //

    @PostMapping("/upload", produces = ["application/json"])
    fun handleUpload(
            @RequestParam file: MultipartFile
    ): MediaUploadResponse {
        if (file.isEmpty) {
            return MediaUploadResponse.fail("Selected file empty or does not exist")
        }

        try {
            val bytes = file.bytes
            val path = Paths.get(UploadType.PostAttachment.directory + file.originalFilename)
            Files.write(path, bytes)

            return MediaUploadResponse.success(path.toString())
        } catch (e: IOException) {
            e.printStackTrace()
            return MediaUploadResponse.fail("A server-side error occurred: ${e.message}")
        }
    }

}

