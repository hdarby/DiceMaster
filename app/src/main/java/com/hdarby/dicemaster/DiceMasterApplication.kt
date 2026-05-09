package com.hdarby.dicemaster

import android.app.Application
import com.hdarby.dicemaster.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class DiceMasterApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@DiceMasterApplication)
            modules(appModule)
        }
    }
}
