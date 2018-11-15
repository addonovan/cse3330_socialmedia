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

    companion object {

        private const val LOCAL_DIR: String = "src/main/resources/static/media"

    }

    /**
     * The type of file being uploaded.
     */
    private enum class UploadType(dirName: String) {

        ProfileImage("profiles"),
        HeaderImage("headers"),
        PostAttachment("posts");

        /** The directory containing this type of media upload. */
        val directory = "$LOCAL_DIR/$dirName/".apply {
            val path = Paths.get(this)
            if (Files.exists(path)) return@apply

            Files.createDirectories(path)
            if (Files.notExists(path))
                throw IllegalStateException("Failed to create necessary media directory: $this")
        }

        /** The URL prefix of a file in this directory. */
        val url = "/media/$dirName"

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

        return try {

            // generate the new file name based off of the current system time,
            // which, admittedly, is probably a pretty bad idea, but whatever
            val fileName = (file.originalFilename ?: file.name).let {
                val extension = it.substringAfterLast('.')
                var name: String

                // continue trying new names until we find one that doesn't
                // already exist
                do {

                    // generate a random named based off of:
                    // - the system's clock time in millis
                    // - the JVM's monotonic clock time
                    // together, these should probably never actually cause a
                    // name collision, but better safe than sorry
                    // also base 36 because it's easy and looks cool
                    val millisPart = System.currentTimeMillis().toString(radix = 36)
                    val nanosPart = System.nanoTime().toString(radix = 36)

                    name = "$millisPart$nanosPart.$extension"
                } while (Files.exists(Paths.get(name)))

                name
            }

            val type = UploadType.PostAttachment

            // actually copy the file into its new location
            val bytes = file.bytes
            val path = Paths.get("${type.directory}/$fileName")
            Files.write(path, bytes)

            // now tell the user how to access that file
            MediaUploadResponse.success("${type.url}/$fileName")

        } catch (e: IOException) {
            e.printStackTrace()
            MediaUploadResponse.fail("A server-side error occurred: ${e.message}")
        }
    }

}

