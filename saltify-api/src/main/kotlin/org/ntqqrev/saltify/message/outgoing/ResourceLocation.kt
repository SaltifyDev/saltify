package org.ntqqrev.saltify.message.outgoing

import java.io.InputStream

sealed class ResourceLocation

class LocalResource(val path: String) : ResourceLocation()

class RemoteResource(val url: String) : ResourceLocation()

class RawResource(val stream: InputStream) : ResourceLocation()