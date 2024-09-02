package com.example.mvvmtask

import android.app.Application
import com.example.mvvmtask.di.NetworkModule.Companion.databaseModule
import com.example.mvvmtask.di.NetworkModule.Companion.networkModule
import com.example.mvvmtask.di.NetworkModule.Companion.wallPaperViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class WallPaperApp:Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@WallPaperApp)
            androidLogger()
            modules(networkModule,wallPaperViewModel,databaseModule)
        }
    }
}