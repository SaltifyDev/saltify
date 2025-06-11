package org.ntqqrev.lagrange.common

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ntqqrev.lagrange.internal.util.ext.toHex
import kotlin.random.Random

internal class SessionStore(
    @JvmField var uin: Long,
    @JvmField var uid: String,

    var a2: ByteArray,
    var d2: ByteArray,
    var d2Key: ByteArray,
    var tgtgt: ByteArray,
    var encryptedA1: ByteArray,
    var noPicSig: ByteArray,

    var qrSig: ByteArray,

    val guid: ByteArray,
    val deviceName: String,
) {
    @JsonIgnore
    internal var keySig: ByteArray? = null

    @JsonIgnore
    internal var exchangeKey: ByteArray? = null

    @JsonIgnore
    internal var unusualCookies: String? = null

    companion object {
        fun empty(): SessionStore {
            return SessionStore(
                uin = 0,
                uid = "",
                a2 = ByteArray(0),
                d2 = ByteArray(0),
                d2Key = ByteArray(16),
                tgtgt = ByteArray(0),
                encryptedA1 = ByteArray(0),
                noPicSig = ByteArray(0),
                qrSig = ByteArray(0),
                guid = Random.nextBytes(16),
                deviceName = "Lagrange-${Random.nextBytes(3).toHex()}"
            )
        }
    }

    fun clear() {
        a2 = ByteArray(0)
        d2 = ByteArray(0)
        d2Key = ByteArray(16)
        tgtgt = ByteArray(0)
        encryptedA1 = ByteArray(0)
        noPicSig = ByteArray(0)
        qrSig = ByteArray(0)
        keySig = null
        exchangeKey = null
        unusualCookies = null
    }
}