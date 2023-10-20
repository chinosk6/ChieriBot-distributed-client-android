package com.chinosk.chieri.client.distributed.utils.models


data class ApiCall(
    val id: String,
    var method: String = "GET",
    val path: String = "",
    var url: String = "",
    val header: Map<String, String>?,
    val body: Any?,
    val args: MutableMap<String, String>?,
)
