package dev.radomski.aidevs2

import android.app.Application
import di.initKoin
import init.init
import org.koin.android.ext.koin.androidContext

class AndroidApp : Application() {
    override fun onCreate() {
        super.onCreate()
        init()
        initKoin {
            androidContext(this@AndroidApp)
        }
    }
}