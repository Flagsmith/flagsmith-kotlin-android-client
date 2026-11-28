package com.flagsmith.internal

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

/** Sends on process backgrounding, not on individual Activity changes. */
internal class FlagsmithLifecycleObserver(
    private val onStop: () -> Unit,
    private val lifecycle: () -> Lifecycle = {
        ProcessLifecycleOwner.get().lifecycle
    },
    private val dispatch: (() -> Unit) -> Unit = {
        Handler(Looper.getMainLooper()).post(it)
        Unit
    }
) : LifecycleEventObserver {
    @Volatile
    private var closed = false

    init {
        // Clients may be created off the main thread.
        dispatch {
            if (!closed) lifecycle().addObserver(this)
        }
    }

    override fun onStateChanged(
        source: LifecycleOwner,
        event: Lifecycle.Event
    ) {
        if (!closed && event == Lifecycle.Event.ON_STOP) onStop()
    }

    fun close() {
        closed = true
        dispatch { lifecycle().removeObserver(this) }
    }
}
