package com.example.ntlong.fulltextsearch

import android.app.Application

/**
 * Created by ntlong on 1/10/18.
 */



class App : Application() {


    companion object {
        lateinit var instance: App
    }


    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}