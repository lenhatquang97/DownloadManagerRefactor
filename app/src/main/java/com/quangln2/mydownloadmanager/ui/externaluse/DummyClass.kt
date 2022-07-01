package com.quangln2.mydownloadmanager.ui.externaluse

class DummyClass() {
    private var onFinish : () -> Unit = {}
    fun setOnFinish(onFinish: () -> Unit) {
        this.onFinish = onFinish
    }
    fun doSomething() {
        onFinish()
    }
}