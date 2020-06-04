package com.learn.geckojavascript

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.WebExtension


class ActivityMain : AppCompatActivity() {
    private lateinit var context: Context
    private lateinit var session: GeckoSession
    private var runtime: GeckoRuntime? = null

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
            override fun onMessage(
                nativeApp: String,
                message: Any,
                sender: WebExtension.MessageSender
            ): GeckoResult<Any>? {
                Log.d("MessageDelegate", message.toString())
                return null
            }
        }
        session.webExtensionController.setMessageDelegate(ext, delegate, "browser")
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
}