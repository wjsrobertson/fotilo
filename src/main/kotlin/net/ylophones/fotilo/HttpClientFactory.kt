package net.ylophones.fotilo

import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients

object HttpClientFactory {
    fun create(cameraInfo: CameraInfo): CloseableHttpClient = with(cameraInfo) {
        val credsProvider = BasicCredentialsProvider()
        credsProvider.setCredentials(AuthScope(host, port), UsernamePasswordCredentials(user, pass))

        return HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build()
    }

}