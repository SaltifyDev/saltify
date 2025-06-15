package org.ntqqrev.saltify.message.outgoing

import java.io.InputStream

sealed class ResourceLocation

class LocalResource(val path: String) : ResourceLocation()
class RemoteResource(val url: String) : ResourceLocation()
class StreamResource(val stream: InputStream) : ResourceLocation()
class BytesResource(val bytes: ByteArray) : ResourceLocation()

fun local(path: String) = LocalResource(path)
fun remote(url: String) = RemoteResource(url)
fun stream(stream: InputStream) = StreamResource(stream)
fun bytes(bytes: ByteArray) = BytesResource(bytes)