package com.chinosk.chieri.client.distributed.client


import android.util.Log
import com.chinosk.chieri.client.distributed.MainActivity
import com.chinosk.chieri.client.distributed.connector.IWebSocketCallbacks
import com.chinosk.chieri.client.distributed.connector.WSConnector
import com.chinosk.chieri.client.distributed.utils.Globals
import com.chinosk.chieri.client.distributed.utils.Logger
import okhttp3.WebSocket
import java.util.*
import kotlin.concurrent.thread


object ServerRun: IWebSocketCallbacks {
    val chieriConnector = WSConnector("wss://api.byt.chinosk6.cn:54778", ChieriConnectorCallback, this)
    // val chieriConnector = WSConnector("wss://192.168.1.4:54778", ChieriConnectorCallback, this)
    val qqConnector = WSConnector("ws://127.0.0.1:${Globals.Config.wsPort}", QQConnectorCallback, this)
    private var reconnecting = false
    private var stopReconnectFlag = false
    private var noReconnectTime: Long = 0


    fun connect() {
        thread {
            if (!QQConnectorCallback.getIsConnect()) qqConnector.start()
        }
        thread {
            if (!ChieriConnectorCallback.getIsConnect()) chieriConnector.start()
        }
    }

    fun disconnect(needReconnect: Boolean = true) {
        val currentTime = Date().time
        if (!needReconnect) noReconnectTime = currentTime

        qqConnector.stop()
        chieriConnector.stop()
        if (reconnecting) return
        if (QQConnectorCallback.getIsConnect() or ChieriConnectorCallback.getIsConnect()) return

        MainActivity.isLoginSuccessLiveData.postValue(false)
        if (needReconnect) {
            if (currentTime - noReconnectTime > 3000) {
                reconnect()
            }
        }
    }

    fun reconnect() {
        reconnecting = true
        MainActivity.setReconnectingLiveData.postValue(10)
        thread {
            try {
                for (i in 10 downTo 1) {
                    if (stopReconnectFlag) {
                        MainActivity.isLoginSuccessLiveData.postValue(false)
                        Logger.info("重连中止")
                        break
                    }
                    Logger.warning("即将重连 ($i)...")
                    Thread.sleep(1000)
                }
                if (!stopReconnectFlag) connect()
            }
            finally {
                stopReconnectFlag = false
                reconnecting = false
            }
        }
    }

    fun isReconnecting(): Boolean {
        return reconnecting
    }

    fun stopReconnect() {
        if (reconnecting) stopReconnectFlag = true
    }

    override fun onMessage(webSocket: WebSocket, message: String) {
    }

    override fun onClose(webSocket: WebSocket) {
        disconnect()
    }

    override fun onError(webSocket: WebSocket, error: Throwable) {
        error.printStackTrace()
        disconnect()
    }

    override fun onOpen(webSocket: WebSocket) {
        if (ChieriConnectorCallback.getIsConnect() and QQConnectorCallback.getIsConnect()) {
            MainActivity.isLoginSuccessLiveData.postValue(true)
        }
    }

}