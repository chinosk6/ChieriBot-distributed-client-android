package com.chinosk.chieri.client.distributed.utils


import android.util.Log
import com.chinosk.chieri.client.distributed.MainActivity
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date


fun getCurrentTime(): String {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())
    val currentTime = Date()
    return dateFormat.format(currentTime)
}

class Logger {
    enum class LogLevel {
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }

    companion object {
        private fun log(msg: String, logLevel: LogLevel) {
            when (logLevel) {
                LogLevel.DEBUG -> {
                    MainActivity.logStringLiveData.postValue("[${getCurrentTime()}] [DEBUG] $msg\n")
                    Log.d("ChieriLog", msg)
                }
                LogLevel.INFO -> {
                    MainActivity.logStringLiveData.postValue("[${getCurrentTime()}] [INFO] $msg\n")
                }
                LogLevel.WARNING -> {
                    MainActivity.logStringLiveData.postValue("[${getCurrentTime()}] [WARNING] $msg\n")
                }
                LogLevel.ERROR -> {
                    MainActivity.logStringLiveData.postValue("[${getCurrentTime()}] [ERROR] $msg\n")
                    Log.e("ChieriLog", msg)
                }
            }
        }
        fun debug(msg: String) {
            log(msg, LogLevel.DEBUG)
        }
        fun info(msg: String) {
            log(msg, LogLevel.INFO)
        }
        fun warning(msg: String) {
            log(msg, LogLevel.WARNING)
        }
        fun error(msg: String) {
            log(msg, LogLevel.ERROR)
        }

    }

}


