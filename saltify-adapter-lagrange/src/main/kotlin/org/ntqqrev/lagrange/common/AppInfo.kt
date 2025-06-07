package org.ntqqrev.lagrange.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class AppInfo(
    @SerialName("Os") val os: String,
    @SerialName("Kernel") val kernel: String,
    @SerialName("VendorOs") val vendorOs: String,
    @SerialName("CurrentVersion") val currentVersion: String,
    @SerialName("MiscBitmap") val miscBitmap: Int,
    @SerialName("PtVersion") val ptVersion: String,
    @SerialName("SsoVersion") val ssoVersion: Int,
    @SerialName("PackageName") val packageName: String,
    @SerialName("WtLoginSdk") val wtLoginSdk: String,
    @SerialName("AppId") val appId: Int,
    @SerialName("SubAppId") val subAppId: Int,
    @SerialName("AppClientVersion") val appClientVersion: Int,
    @SerialName("MainSigMap") val mainSigMap: Int,
    @SerialName("SubSigMap") val subSigMap: Int,
    @SerialName("NTLoginType") val ntLoginType: Int
)