package com.quangln2.downloadmanagerrefactor.data.database

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class PrefSingleton private constructor() {
    private var mContext: Context? = null
    private var mMyPreferences: SharedPreferences? = null
    fun initialize(ctx: Context?) {
        mContext = ctx
        mMyPreferences = mContext?.getSharedPreferences("preferences", Application.MODE_PRIVATE)
    }

    fun writeString(key: String, value: String) {
        val editor = mMyPreferences?.edit()
        editor?.putString(key, value)
        editor?.apply()
    }

    fun getString(key: String): String? {
        return mMyPreferences?.getString(key, "")
    }

    companion object {
        private var mInstance: PrefSingleton? = null
        val instance: PrefSingleton?
            get() {
                if (mInstance == null) mInstance = PrefSingleton()
                return mInstance
            }
    }
}