package org.ntqqrev.lagrange.common

import com.fasterxml.jackson.annotation.JsonProperty

internal class AppInfo(
    @JsonProperty("Os") val os: String,
    @JsonProperty("Kernel") val kernel: String,
    @JsonProperty("VendorOs") val vendorOs: String,
    @JsonProperty("CurrentVersion") val currentVersion: String,
    @JsonProperty("MiscBitmap") val miscBitmap: Int,
    @JsonProperty("PtVersion") val ptVersion: String,
    @JsonProperty("SsoVersion") val ssoVersion: Int,
    @JsonProperty("PackageName") val packageName: String,
    @JsonProperty("WtLoginSdk") val wtLoginSdk: String,
    @JsonProperty("AppId") val appId: Int,
    @JsonProperty("SubAppId") val subAppId: Int,
    @JsonProperty("AppClientVersion") val appClientVersion: Int,
    @JsonProperty("MainSigMap") val mainSigMap: Int,
    @JsonProperty("SubSigMap") val subSigMap: Int,
    @JsonProperty("NTLoginType") val ntLoginType: Int
)