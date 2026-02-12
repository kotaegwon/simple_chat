package com.ko.simple_chat

import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import com.ko.simple_chat.firebase.FirebaseManager
import timber.log.Timber

class MyApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        FirebaseManager.init()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}