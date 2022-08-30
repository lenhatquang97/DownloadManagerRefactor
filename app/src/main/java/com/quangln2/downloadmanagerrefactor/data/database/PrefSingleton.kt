package com.quangln2.downloadmanagerrefactor.data.database

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.tencent.mmkv.MMKV

class PrefSingleton private constructor() {
    private var mContext: Context? = null
    private var mMyPreferences: MMKV? = null
    fun initialize(ctx: Context?) {
        mContext = ctx
        MMKV.initialize(ctx)
        mMyPreferences = MMKV.defaultMMKV()
    }
    fun writeString(key: String, value: String) {
        mMyPreferences?.encode(key, value)
    }
    fun getString(key: String): String {
        return mMyPreferences?.decodeString(key) ?: ""
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