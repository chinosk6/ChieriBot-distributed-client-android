package com.chinosk.chieri.client.distributed.connector

import android.annotation.SuppressLint
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class WSConnector(private val host: String, private val callbacks: IWebSocketCallbacks,
                  private val callbacks2: IWebSocketCallbacks? = null) {

    private var webSocket: WebSocket? = null

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
            callbacks.onOpen(webSocket)
            callbacks2?.onOpen(webSocket)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            callbacks.onMessage(webSocket, text)
            callbacks2?.onMessage(webSocket, text)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            callbacks.onMessage(webSocket, bytes)
            callbacks2?.onMessage(webSocket, bytes)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(code, reason)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            callbacks.onClose(webSocket)
            callbacks2?.onClose(webSocket)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
            callbacks.onError(webSocket, t)
            callbacks2?.onError(webSocket, t)
        }
    }

    fun start() {
        val trustAllCerts = arrayOf<TrustManager>(@SuppressLint("CustomX509TrustManager")
        object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        })

        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
        val client = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()

        // val client = OkHttpClient()
        val request = Request.Builder().url(host).build()
        webSocket = client.newWebSocket(request, webSocketListener)
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }
    fun sendMessage(message: ByteString) {
        webSocket?.send(message)
    }

    fun stop() {
        webSocket?.close(1000, "Normal closure")
    }
}
