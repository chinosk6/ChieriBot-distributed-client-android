package com.chinosk.chieri.client.distributed.utils.models

data class MsgRuleUIDataModel(
    var matchPattern: String?,  // 仅配置不合法时为 null
    var newString: String? = null,
    val flags: MutableList<Int> = mutableListOf()
)
