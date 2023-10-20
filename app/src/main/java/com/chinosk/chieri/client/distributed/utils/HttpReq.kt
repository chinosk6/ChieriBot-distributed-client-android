package com.chinosk.chieri.client.distributed.utils


import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit


class HttpReq {
    companion object {

        private fun requestGetAsync(url: String, callback: Callback) {
            val client = OkHttpClient().newBuilder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            val requestBuild = Request.Builder()
                .url(url)
                .method("GET", null)

            val request: Request = requestBuild.build()
            try {
                client.newCall(request).enqueue(callback)
            } catch (e: Exception) {
                Log.e("http", e.toString())
                e.printStackTrace()
            }
        }

        private fun requestPostAsync(url: String, body: RequestBody, callback: Callback) {
            val client = OkHttpClient().newBuilder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            val bdr = Request.Builder()
                .url(url)
                .method("POST", body)
            val request: Request = bdr.build()
            try {
                client.newCall(request).enqueue(callback)
            } catch (e: Exception) {
                Log.e("http", e.toString())
                e.printStackTrace()
            }
        }

        fun requestGet(url: String, headers: Map<String, String>? = null): Response {
            val client = OkHttpClient()
            val requestBuilder = Request.Builder().url(url)
            if (headers != null) {
                for (i in headers) {
                    requestBuilder.addHeader(i.key, i.value)
                }
            }
            return client.newCall(requestBuilder.build()).execute()
        }

        fun requestGetStr(url: String, headers: Map<String, String>? = null): String {
            try {
                val response: Response = requestGet(url, headers)
                return response.body?.string().toString()
            } catch (e: Exception) {
                Logger.error("requestGet failed: $e")
            }
            return ""
        }

        fun requestPost(url: String, requestBody: RequestBody? = null,
                           headers: Map<String, String>? = null): Response {
            val client = OkHttpClient()
            val requestBuilder = Request.Builder().url(url)
            if (requestBody == null) {
                requestBuilder.post("".toRequestBody("text/plain".toMediaType()))
            }
            else {
                requestBuilder.post(requestBody)
            }
            if (headers != null) {
                for (i in headers) {
                    requestBuilder.addHeader(i.key, i.value)
                }
            }
            return client.newCall(requestBuilder.build()).execute()
        }

        fun requestPost(url: String, rawJsonBody: String, headers: Map<String, String>? = null): Response {
            return requestPost(url, rawJsonBody.toRequestBody("application/json".toMediaType()), headers)
        }

        fun requestPost(url: String, multipartBodyMap: Map<*, *>, headers: Map<String, String>? = null): Response {
            val requestBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            for (k in multipartBodyMap.keys) {
                if (k is String) {
                    try {
                        val v = multipartBodyMap[k] as String
                        requestBodyBuilder.addFormDataPart(k, v)
                    }
                    catch (_: Exception) {}
                }
            }
            return requestPost(url, requestBodyBuilder.build(), headers)
        }

        fun requestPostStr(url: String, requestBody: RequestBody? = null,
                           headers: Map<String, String>? = null): String {
            try {
                val response: Response = requestPost(url, requestBody, headers)
                return response.body?.string().toString()
            } catch (e: Exception) {
                Logger.error("requestPost failed: $e")
            }
            return ""
        }

    }

}