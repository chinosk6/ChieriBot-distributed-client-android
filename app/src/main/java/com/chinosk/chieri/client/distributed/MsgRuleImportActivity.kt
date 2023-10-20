package com.chinosk.chieri.client.distributed

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.chinosk.chieri.client.distributed.utils.ConfigManager
import com.chinosk.chieri.client.distributed.utils.Globals
import com.chinosk.chieri.client.distributed.utils.JsonParser
import com.chinosk.chieri.client.distributed.utils.models.MsgRuleModel
import com.google.android.material.snackbar.Snackbar

class MsgRuleImportActivity : AppCompatActivity() {

    private lateinit var buttonSave: Button
    private lateinit var editTextTextMultiLineMsgRule: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_msg_rule_import)

        buttonSave = findViewById(R.id.buttonSave)
        editTextTextMultiLineMsgRule = findViewById(R.id.editTextTextMultiLineMsgRule)
        buttonSave.setOnClickListener { onButtonSaveClicked(it) }

        initRuleText()
    }

    private fun initRuleText() {
        val initStr = JsonParser.dumpJson(Globals.MsgRule, 4)
        editTextTextMultiLineMsgRule.setText(initStr)
    }

    private fun onButtonSaveClicked(view: View) {
        val saveStr = editTextTextMultiLineMsgRule.text.toString()
        val newData = JsonParser.parseJson<MsgRuleModel>(saveStr)
        if (newData != null) {
            ConfigManager.updateProperty(Globals.MsgRule, newData)
            ConfigManager.saveConfig()
            finish()
        }
        else {
            Snackbar.make(view, "内容不合法，保存失败。", Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }
    }
}