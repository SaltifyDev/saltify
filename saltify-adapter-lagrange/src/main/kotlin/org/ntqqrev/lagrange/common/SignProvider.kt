package org.ntqqrev.lagrange.common

internal fun interface SignProvider {
    suspend fun sign(cmd: String, seq: Int, src: ByteArray): Result

    class Result(
        val sign: ByteArray,
        val token: ByteArray,
        val extra: ByteArray,
    )
}