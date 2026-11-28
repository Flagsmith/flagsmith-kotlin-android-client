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
    private var sends = 0

    @Test
    fun doesNotSendBeforeTheProcessStops() {
        // Given
        val callback = registeredCallback(createObserver())

        // When
        callback.onStateChanged(owner, Lifecycle.Event.ON_START)
        callback.onStateChanged(owner, Lifecycle.Event.ON_RESUME)
        callback.onStateChanged(owner, Lifecycle.Event.ON_PAUSE)

        // Then
        assertEquals(0, sends)
    }

    @Test
    fun sendsEachTimeTheProcessStops() {
        // Given
        val callback = registeredCallback(createObserver())

        // When
        callback.onStateChanged(owner, Lifecycle.Event.ON_STOP)
        callback.onStateChanged(owner, Lifecycle.Event.ON_START)
        callback.onStateChanged(owner, Lifecycle.Event.ON_STOP)

        // Then
        assertEquals(2, sends)
    }

    @Test
    fun closingRemovesTheObserverAndStopsSending() {
        // Given
        val observer = createObserver()
        val callback = registeredCallback(observer)

        // When
        observer.close()
        callback.onStateChanged(owner, Lifecycle.Event.ON_STOP)

        // Then
        verify(lifecycle).removeObserver(observer)
        assertEquals(0, sends)
    }

    @Test
    fun closingBeforeMainThreadRegistrationDoesNotLeaveAnObserver() {
        // Given
        val queued = mutableListOf<() -> Unit>()
        val observer = FlagsmithLifecycleObserver(
            onStop = { error("Closed observer must not flush") },
            lifecycle = { lifecycle },
            dispatch = { queued.add(it) }
        )

        // When
        observer.close()
        verifyNoInteractions(lifecycle)
        queued.forEach { it() }

        // Then
        verify(lifecycle).removeObserver(observer)
        verify(lifecycle, never()).addObserver(observer)
    }

    private fun createObserver() = FlagsmithLifecycleObserver(
        onStop = { sends++ },
        lifecycle = { lifecycle },
        dispatch = { it() }
    )

    private fun registeredCallback(observer: FlagsmithLifecycleObserver): LifecycleEventObserver {
        val registered = argumentCaptor<LifecycleEventObserver>()
        verify(lifecycle).addObserver(registered.capture())
        assertEquals(observer, registered.firstValue)
        return registered.firstValue
    }
}
