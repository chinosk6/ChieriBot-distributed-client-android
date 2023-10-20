package com.chinosk.chieri.client.distributed

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.chinosk.chieri.client.distributed.client.ServerRun
import com.chinosk.chieri.client.distributed.service.MainService
import com.chinosk.chieri.client.distributed.service.MainService.Companion.EXIT_ACTION
import com.chinosk.chieri.client.distributed.utils.ConfigManager
import com.chinosk.chieri.client.distributed.utils.Globals
import com.google.android.material.snackbar.Snackbar
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    companion object {
        val logStringLiveData = MutableLiveData<String>()
        val isLoginSuccessLiveData = MutableLiveData<Boolean>()
        val setReconnectingLiveData = MutableLiveData<Int>()
        val jumpToSetActivityLiveData = MutableLiveData<Boolean>()
    }

    private lateinit var editTextLog: EditText
    private lateinit var textViewTitle: TextView
    private lateinit var editWsPort: EditText
    private lateinit var editHttpPort: EditText
    private lateinit var editToken: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonDisconnect: Button
    private lateinit var buttonBWSet: Button
    private lateinit var buttonMsgRule: Button
    private lateinit var buttonPokeSet: Button
    private lateinit var buttonMsgRuleEdit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Globals.filesDir = applicationContext.filesDir

        buttonLogin = findViewById(R.id.buttonLogin)
        buttonDisconnect = findViewById(R.id.buttonDisconnect)
        buttonBWSet = findViewById(R.id.buttonBWSet)
        buttonMsgRule = findViewById(R.id.buttonMsgRule)
        buttonPokeSet = findViewById(R.id.buttonPokeSet)
        buttonMsgRuleEdit = findViewById(R.id.buttonMsgRuleEdit)
        buttonLogin.setOnClickListener { onButtonClickedLogin() }
        buttonDisconnect.setOnClickListener { onButtonClickedDisconnect() }
        buttonBWSet.setOnClickListener { onButtonClickedBWSet() }
        buttonMsgRule.setOnClickListener { onButtonClickedMsgRule() }
        buttonPokeSet.setOnClickListener { onButtonClickedPokeSet(it) }
        buttonMsgRuleEdit.setOnClickListener { onButtonClickedMsgRuleEdit() }

        editTextLog = findViewById(R.id.editTextTextLog)
        editWsPort = findViewById(R.id.editTextWsPort)
        editHttpPort = findViewById(R.id.editTextHttpPort)
        editToken = findViewById(R.id.editTextToken)
        textViewTitle = findViewById(R.id.textViewTitle)

        logStringLiveData.observe(this) { result ->
            addEditTextLog(result)
        }
        isLoginSuccessLiveData.observe(this) { result ->
            changeButtonStateByLoginSuccess(result)
        }
        setReconnectingLiveData.observe(this) {
            changeButtonStateToReconnecting()
        }
        jumpToSetActivityLiveData.observe(this) {
            buttonBWSet.text = getString(R.string.blackWhiteSet)
            buttonBWSet.isEnabled = true
            val intent = Intent(this, BWSetActivity::class.java)
            startActivity(intent)
        }

        loadVersion()
        loadConfig()

        val serviceIntent = Intent(this, MainService::class.java)
        startService(serviceIntent)

        if (intent.action == EXIT_ACTION) {
            stopService(serviceIntent)
        }

    }

    private fun loadConfig() {
        if (ConfigManager.loadConfig()) {
            editWsPort.setText(Globals.Config.wsPort.toString())
            editHttpPort.setText(Globals.Config.httpPort.toString())
            editToken.setText(Globals.Config.token)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadVersion() {
        val packageManager = applicationContext.packageManager
        val packageName = applicationContext.packageName
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        textViewTitle.text = "${textViewTitle.text} ${packageInfo.versionName}"
    }

    private fun addEditTextLog(msg: String) {
        var appendMsg = msg
        if (!msg.endsWith("\n")) {
            appendMsg = "$appendMsg\n"
        }
        runOnUiThread {
            editTextLog.append(appendMsg)
        }
    }

    private fun changeButtonStateByLoginSuccess(isLoginSuccess: Boolean) {
        buttonLogin.isEnabled = !isLoginSuccess
        buttonDisconnect.isEnabled = isLoginSuccess
        buttonLogin.text = getText(R.string.connect)
        buttonDisconnect.text = getText(R.string.disconnect)
    }

    private fun changeButtonStateToReconnecting() {
        buttonLogin.isEnabled = false
        buttonDisconnect.isEnabled = true
        // buttonLogin.text = getText(R.string.reconnecting)
        // buttonDisconnect.text = getText(R.string.stop_reconnect)
    }

    private fun updateGlobalTokenAndPorts(onSuccess: (() -> Unit)? = null) {
        runOnUiThread {
            try {
                val wsPort = editWsPort.text.toString().toInt()
                val httpPort = editHttpPort.text.toString().toInt()
                val token = editToken.text.toString()
                Globals.Config.wsPort = wsPort
                Globals.Config.httpPort = httpPort
                Globals.Config.token = token
                onSuccess?.invoke()
            }
            catch (e: Exception) {
                addEditTextLog(e.toString())
                Log.e("Chieri-onBtnClickLogin", e.toString())
            }
        }
    }

    private fun onButtonClickedLogin() {
        updateGlobalTokenAndPorts {
            ConfigManager.saveConfig()
            ServerRun.connect()
        }
    }

    private fun onButtonClickedDisconnect() {
        if (ServerRun.isReconnecting()) {
            ServerRun.stopReconnect()
        }
        else {
            ServerRun.disconnect(needReconnect = false)
        }
    }

    private fun onButtonClickedBWSet() {
        updateGlobalTokenAndPorts()
        buttonBWSet.isEnabled = false
        buttonBWSet.text = getString(R.string.loading)

        ConfigManager.updateListsAsync {
            jumpToSetActivityLiveData.postValue(true)
        }
    }

    private fun onButtonClickedMsgRule() {
        val intent = Intent(this, MsgRuleActivity::class.java)
        startActivity(intent)
    }
    private fun onButtonClickedMsgRuleEdit() {
        val intent = Intent(this, MsgRuleImportActivity::class.java)
        startActivity(intent)
    }
    private fun onButtonClickedPokeSet(view: View) {
        Snackbar.make(view, "未实现，暂停开发。", Snackbar.LENGTH_LONG).setAction("Action", null).show()
        // val intent = Intent(this, PokeSetActivity::class.java)
        // startActivity(intent)
    }

}
