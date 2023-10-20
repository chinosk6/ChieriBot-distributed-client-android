package com.chinosk.chieri.client.distributed.utils.models

data class MessageModel(
    val message_type: String,
    val sub_type: String,
    val group_id: Long?,
    val user_id: Long,
    var message: String
)
