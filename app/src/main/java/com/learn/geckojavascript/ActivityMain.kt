package com.learn.geckojavascript

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.WebExtension

class ActivityMain : AppCompatActivity() {
    private lateinit var context: Context
    private lateinit var session: GeckoSession
    private var runtime: GeckoRuntime? = null
    private var port: WebExtension.Port? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this

        session = GeckoSession()
        if (runtime == null) {
            runtime = GeckoRuntime.create(context)
            runtime!!.settings.remoteDebuggingEnabled = true;

        }

        val settings = session.settings
        settings.allowJavascript = true
        session.progressDelegate = createProgressDelegate()
        session.open(runtime!!)

        geckoView.setSession(session)

        runtime!!.webExtensionController.installBuiltIn("resource://android/assets/messaging/")
            .accept(
                { extension -> setMessageDelegate(extension!!) },
                { e -> Log.e("MessageDelegate", "Error registering WebExtension", e) })


        session.loadUri("https://en.m.wikipedia.org/wiki/JavaScript")
    }


    private fun setMessageDelegate(ext:WebExtension){
        val delegate = object: WebExtension.MessageDelegate {
            override fun onConnect(_port: WebExtension.Port) {
                port = _port

                port!!.setDelegate(object: WebExtension.PortDelegate {
                    override fun onPortMessage(message: Any, _port: WebExtension.Port) {
                        Log.d("PortDelegate", "Received message from extension: " + message);
                    }

                    override fun onDisconnect(p0: WebExtension.Port) {
                        // This port is not usable anymore.
                        if (port == _port) {
                            port = null;
                        }
                    }
                })
            }
        }
        ext.setMessageDelegate(delegate, "browser")
    }

    private fun createProgressDelegate(): GeckoSession.ProgressDelegate {
        return object : GeckoSession.ProgressDelegate {

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                progressBar.visibility = View.GONE
            }

            override fun onSecurityChange(
                session: GeckoSession,
                securityInfo: GeckoSession.ProgressDelegate.SecurityInformation
            ) = Unit

            override fun onPageStart(session: GeckoSession, url: String) {
                progressBar.visibility = View.VISIBLE
            }

            override fun onProgressChange(session: GeckoSession, progress: Int) {
            }
        }
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        if (port == null) { // No extension registered yet, let's ignore this message
            return false
        }

        val message = JSONObject()
        try {
            message.put("keyCode", keyCode)
            message.put("event", KeyEvent.keyCodeToString(event!!.keyCode))
        } catch (ex: JSONException) {
            throw RuntimeException(ex)
        }

        port!!.postMessage(message)
        return true
    }
}