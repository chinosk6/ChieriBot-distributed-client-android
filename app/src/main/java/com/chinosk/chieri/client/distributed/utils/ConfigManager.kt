package com.chinosk.chieri.client.distributed.utils


import com.chinosk.chieri.client.distributed.utils.models.ConfigModel
import com.chinosk.chieri.client.distributed.utils.models.MsgRuleModel
import kotlinx.coroutines.delay
import java.io.File
import kotlin.concurrent.thread


fun <T> MutableList<T>.updateFromList(list: List<T>?) {
    if (list == null) return
    val iterator = this.listIterator()
    while (iterator.hasNext()) {
        val element = iterator.next()
        if (!list.contains(element)) {
            iterator.remove()
        }
    }
    for (element in list) {
        if (!this.contains(element)) {
            this.add(element)
        }
    }
}

fun <T> MutableList<T>.replaceFromList(list: List<T>?) {
    if (list == null) return
    this.clear()
    this.addAll(list)
}


object ConfigManager {
    private val configFile = File(Globals.filesDir, "config.json")
    private val msgRuleFile = File(Globals.filesDir, "msgrule.json")

    inline fun <reified T> updateProperty(self: T, other: T) {
        for (property in T::class.java.declaredFields) {
            property.isAccessible = true
            val value = property.get(other)
            property.set(self, value)
        }
    }

    fun loadConfig(): Boolean {
        val readConfig = JsonParser.parseJson<ConfigModel>(configFile)
        val readMsgRule = JsonParser.parseJson<MsgRuleModel>(msgRuleFile)
        if (readMsgRule != null) updateProperty(Globals.MsgRule, readMsgRule)
        if (readConfig != null) {
            updateProperty(Globals.Config, readConfig)
            return true
        }
        return false
    }

    fun saveConfig() {
        JsonParser.dumpJson(Globals.Config, configFile, 4)
        JsonParser.dumpJson(Globals.MsgRule, msgRuleFile, 4)
    }

    fun updateListsAsync(onFinish: (() -> Unit)? = null) {
        thread {
            val friendsList = Api.getFriendList()
            val groupList = Api.getGroupList()
            var hasError = false

            if (friendsList?.retcode == 0) {
                Globals.friendsList.updateFromList(friendsList.data)
            }
            else {
                hasError = true
            }
            if (groupList?.retcode == 0) {
                Globals.groupList.updateFromList(groupList.data)
            }
            else {
                hasError = true
            }
            if (hasError) {
                Logger.error("加载好友/群列表失败, 请检查 HTTP 端口是否正确填写")
                Thread.sleep(1500)
            }
            onFinish?.invoke()
        }
    }

}