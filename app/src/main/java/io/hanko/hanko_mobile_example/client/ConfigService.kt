package io.hanko.hanko_mobile_example.client

import io.hanko.hanko_mobile_example.ApiRoutes
import io.hanko.hanko_mobile_example.KtorClient
import io.hanko.hanko_mobile_example.models.Config
import io.ktor.client.request.get
import io.ktor.client.request.url

interface ConfigService {
    suspend fun getConfig(): Config
}

class ConfigServiceImpl : ConfigService {
    private val client by lazy { KtorClient.instance }
    override suspend fun getConfig(): Config {
        return client.get {
            url(ApiRoutes.GET_CONFIG)
        }
    }
}