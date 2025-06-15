package org.ntqqrev.milky.util

import io.ktor.util.encodeBase64
import org.ntqqrev.saltify.message.outgoing.*

fun ResourceLocation.toMilkyUri(): String =
    when (this) {
        is LocalResource -> "file://$path"
        is RemoteResource -> url
        is StreamResource -> {
            val bytes = stream.readBytes()
            "base64://${bytes.encodeBase64()}"
        }
        is BytesResource -> "base64://${bytes.encodeBase64()}"
    }