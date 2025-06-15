package org.ntqqrev.saltify.message.outgoing

import java.io.InputStream

sealed class ResourceLocation

class LocalResource(val path: String) : ResourceLocation()

class RemoteResource(val url: String) : ResourceLocation()

class RawResource(val stream: InputStream) : ResourceLocation()

fun local(path: String): LocalResource {
    return LocalResource(path)
}

fun remote(url: String): RemoteResource {
    return RemoteResource(url)
}

fun raw(stream: InputStream): RawResource {
    return RawResource(stream)
}

fun raw(bytes: ByteArray): RawResource {
    return RawResource(bytes.inputStream())
}