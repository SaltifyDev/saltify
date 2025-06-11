package org.ntqqrev.lagrange

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableSharedFlow
import org.ntqqrev.lagrange.common.AppInfo
import org.ntqqrev.lagrange.common.SessionStore
import org.ntqqrev.lagrange.internal.LagrangeClient
import org.ntqqrev.lagrange.util.UrlSignProvider
import org.ntqqrev.saltify.Environment
import org.ntqqrev.saltify.ContextFactory
import org.ntqqrev.saltify.event.Event
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object LagrangeContextFactory : ContextFactory<LagrangeInit> {
    private val logger = KotlinLogging.logger { }
    private val objectMapper = jacksonObjectMapper()

    private val fallbackAppInfo = AppInfo(
        os = "Linux",
        kernel = "Linux",
        vendorOs = "linux",
        currentVersion = "3.2.15-30366",
        miscBitmap = 32764,
        ptVersion = "2.0.0",
        ssoVersion = 19,
        packageName = "com.tencent.qq",
        wtLoginSdk = "nt.wtlogin.0.0.1",
        appId = 1600001615,
        subAppId = 537258424,
        appClientVersion = 30366,
        mainSigMap = 169742560,
        subSigMap = 0,
        ntLoginType = 1
    )

    override suspend fun createContext(
        init: LagrangeInit,
        env: Environment,
        flow: MutableSharedFlow<Event>
    ): LagrangeContext {
        val signProvider = UrlSignProvider(init.signApiUrl, init.signApiProxyUrl)
        val appInfo = signProvider.getAppInfo() ?: {
            logger.warn { "Failed to get app info from sign API, using fallback app info" }
            fallbackAppInfo
        }()

        var sessionStore: SessionStore
        val keystorePath = env.rootDataPath.resolve(sessionStoreFileName)
        if (!keystorePath.exists()) {
            logger.debug { "Generating new keystore" }
            sessionStore = SessionStore.empty()
            keystorePath.writeText(objectMapper.writeValueAsString(sessionStore))
        } else {
            logger.debug { "Using existing session" }
            sessionStore = objectMapper.readValue(keystorePath.readText())
        }

        val client = LagrangeClient(appInfo, sessionStore, signProvider, env.scope)
        client.packetLogic.connect()

        return LagrangeContext(client, init, env, flow)
    }
}