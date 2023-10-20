package com.chinosk.chieri.client.distributed.connector

import okhttp3.WebSocket
import okio.ByteString

interface IWebSocketCallbacks {
    fun onMessage(webSocket: WebSocket, message: String) {}
    fun onMessage(webSocket: WebSocket, bytes: ByteString) { onMessage(webSocket, bytes.toString()) }
    fun onClose(webSocket: WebSocket) {}
    fun onError(webSocket: WebSocket, error: Throwable) {}
    fun onOpen(webSocket: WebSocket) {}
}
