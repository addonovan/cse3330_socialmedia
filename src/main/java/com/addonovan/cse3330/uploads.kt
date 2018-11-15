package com.addonovan.cse3330

import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

/** The relative path to the local directory where all media is saved */
private const val LOCAL_DIR: String = "src/main/resources/static/media"

/**
 * The type of file being uploaded.
 */
enum class UploadType(dirName: String) {

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

/**
 * A response type of the [success] of a media upload.
 *
 * @see [SuccessfulUploadResponse]
 * @see [FailedUploadResponse]
 * @see [fail]
 * @see [success]
 */
sealed class MediaUploadResponse(val success: Boolean) {

    companion object {

        /**
         * Convenience function to create a new [FailedUploadResponse] with the
         * given [message].
         *
         * @see [FailedUploadResponse]
         */
        fun fail(message: String) = FailedUploadResponse(message)

        /**
         * Convenience function to create a new [SuccessfulUploadResponse] with
         * the given [link].
         *
         * @see [SuccessfulUploadResponse]
         */
        fun success(link: String) = SuccessfulUploadResponse(link)
    }

    /**
     * A successful media upload response, where [success] is `true` and the
     * [link] is a local URL which will be used to reference the new image.
     *
     * @see [success]
     */
    data class SuccessfulUploadResponse(val link: String) : MediaUploadResponse(true)

    /**
     * An unsuccessful (i.e. failed) media upload response, where [success] is
     * `false` and the [message] describes the problem when uploading.
     *
     * @see [fail]
     */
    data class FailedUploadResponse(val message: String) : MediaUploadResponse(false)

}

//
// End points
//

/**
 * Copies [this][MultipartFile] file from the hosts's computer to the
 * directory corresponding with the given upload [type] with a new file name.
 *
 * @return The JSON response to send back to the file uploader.
 *
 * @see [MediaUploadResponse]
 */
fun MultipartFile.writeAs(type: UploadType): MediaUploadResponse {
    if (isEmpty) {
        return MediaUploadResponse.fail("Selected file empty or does not exist")
    }

    return try {

        // generate the new file name based off of the current system time,
        // which, admittedly, is probably a pretty bad idea, but whatever
        val fileName = (originalFilename ?: name).let {
            val extension = it.substringAfterLast('.')

            findNewFileName(type.directory, extension)
        }

        // actually copy the file into its new location
        val bytes = bytes
        val path = Paths.get("${type.directory}/$fileName")
        Files.write(path, bytes)

        // now tell the user how to access that file
        MediaUploadResponse.success("${type.url}/$fileName")

    } catch (e: IOException) {
        e.printStackTrace()
        MediaUploadResponse.fail("A server-side error occurred: ${e.message}")
    }
}

/**
 * Finds a new file name based in part of the system's current time and in part
 * on the JVM's monotnonic clock. The file will be guaranteed to not already
 * exist in the given [path] with the given [extension].
 */
private fun findNewFileName(path: String, extension: String): String {
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
    } while (Files.exists(Paths.get("$path/$name")))

    return name
}
