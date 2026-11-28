package com.flagsmith.internal

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class FlagsmithLifecycleObserverTest {
    private val lifecycle = mock<Lifecycle>()
    private val owner = mock<LifecycleOwner>()

    @Test
    fun sendsOnlyWhenTheProcessStopsAndCanSendAgain() {
        var sends = 0
        val observer = FlagsmithLifecycleObserver(
            onStop = { sends++ },
            lifecycle = { lifecycle },
            dispatch = { it() }
        )
        val registered = argumentCaptor<LifecycleEventObserver>()
        verify(lifecycle).addObserver(registered.capture())
        val callback = registered.firstValue

        callback.onStateChanged(owner, Lifecycle.Event.ON_START)
        callback.onStateChanged(owner, Lifecycle.Event.ON_RESUME)
        callback.onStateChanged(owner, Lifecycle.Event.ON_PAUSE)
        assertEquals(0, sends)
        callback.onStateChanged(owner, Lifecycle.Event.ON_STOP)
        assertEquals(1, sends)
        callback.onStateChanged(owner, Lifecycle.Event.ON_START)
        callback.onStateChanged(owner, Lifecycle.Event.ON_STOP)
        assertEquals(2, sends)

        observer.close()
        verify(lifecycle).removeObserver(observer)
        callback.onStateChanged(owner, Lifecycle.Event.ON_STOP)
        assertEquals(2, sends)
    }

    @Test
    fun closingBeforeMainThreadRegistrationDoesNotLeaveAnObserver() {
        val queued = mutableListOf<() -> Unit>()
        val observer = FlagsmithLifecycleObserver(
            onStop = { error("Closed observer must not flush") },
            lifecycle = { lifecycle },
            dispatch = { queued.add(it) }
        )
        observer.close()
        verifyNoInteractions(lifecycle)

        queued.forEach { it() }
        verify(lifecycle).removeObserver(observer)
        verify(lifecycle, never()).addObserver(observer)
    }
}
