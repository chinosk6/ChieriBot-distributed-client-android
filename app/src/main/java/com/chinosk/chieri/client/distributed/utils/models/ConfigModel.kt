package com.chinosk.chieri.client.distributed.utils.models

data class ConfigModel (
    var wsPort: Int = -1,
    var httpPort: Int = -1,
    var token: String = ""
)
