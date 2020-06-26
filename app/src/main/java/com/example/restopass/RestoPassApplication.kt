package com.example.restopass

import android.app.Application
import androidx.core.provider.FontRequest
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import timber.log.Timber

// Application Class: contiene el estado global de toda la aplicación.
// Sirve para setear Timber (librería de logs) globalmente y demases
class RestoPassApplication : Application() {

    companion object {
        /** Change this to `false` when you want to use the downloadable Emoji font.  */
        private val USE_BUNDLED_EMOJI = true
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        val config: EmojiCompat.Config
        if (USE_BUNDLED_EMOJI) {
            config = BundledEmojiCompatConfig(applicationContext)
        } else {
            // Use a downloadable font for EmojiCompat
            val fontRequest = FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs)
            config = FontRequestEmojiCompatConfig(applicationContext, fontRequest)
                .setReplaceAll(true)
                .registerInitCallback(object : EmojiCompat.InitCallback() {
                    override fun onInitialized() {
                        Timber.i( "EmojiCompat initialized")
                    }

                    override fun onFailed(throwable: Throwable?) {
                        Timber.e( "EmojiCompat initialization failed $throwable")
                    }
                })
        }
        EmojiCompat.init(config)
    }
}