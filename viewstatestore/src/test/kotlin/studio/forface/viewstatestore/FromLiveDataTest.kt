package studio.forface.viewstatestore

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import io.mockk.Ordering
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import studio.forface.viewstatestore.utils.ArchTest
import kotlin.test.assertEquals

/**
 * A class for test [ViewStateStore] created from a `LiveData`.
 * @author Davide Giuseppe Farella
 */
internal class FromLiveDataTest : ArchTest {

    @Test
    fun `ViewStateStore is created correctly`() {
        val liveData = MutableLiveData<Boolean>()
        ViewStateStore.from(liveData)
    }

    @Test
    fun `ViewStateStore emits correctly from MutableLiveData`() {
        val liveData = MutableLiveData<Int>()
        val vss = ViewStateStore.from(liveData)

        // Start Observing
        val observer = mockk<(Int) -> Unit>(relaxed = true)
        vss.observeDataForever(observer)

        // Publish to ViewStateStore
        vss.setData(0)
        // Verify observer has been called once
        verify(exactly = 1) { observer(any()) }
        // Assert ViewStateStore has right value
        assertEquals(0, vss.unsafeState().data)

        // Publish to LiveData
        liveData.value = 1
        // Verify observer has been called twice
        verify(exactly = 2) { observer(any()) }
        // Assert ViewStateStore has right value
        assertEquals(1, vss.unsafeState().data)
    }

    @Test
    fun `LiveData_switchMap works correctly`() {
        val s1 = MutableLiveData<Int>()
        val s2 = MutableLiveData<Int>()
        val m = s1.switchMap { v1 -> s2.map { v2 -> v1 * v2 } }

        // Start Observing
        val observer = mockk<(Int) -> Unit>(relaxed = true)
        m.observeForever(observer)

        verify(exactly = 0) { observer(any()) }

        // Verity very first emit
        s1.value = 1
        verify(exactly = 0) { observer(any()) }
        s2.value = 1
        verify(exactly = 1) { observer(any()) }
        verify(exactly = 1) { observer(1) }

        // Verify emit on s1
        s1.value = 2
        verify(exactly = 2) { observer(any()) }
        verify(exactly = 1) { observer(2) }

        // Verify emit on s2
        s2.value = 2
        verify(exactly = 3) { observer(any()) }
        verify(exactly = 1) { observer(4) }

        // Re-verify emit on s1
        s1.value = 3
        verify(exactly = 4) { observer(any()) }
        verify(exactly = 1) { observer(6) }
    }

    @Test
    fun `ViewStateStore emits correctly from LiveData_switchMap`() {
        val s1 = MutableLiveData<Int>()
        val s2 = MutableLiveData<Int>()
        val m = ViewStateStore.from(s1.switchMap { v1 -> s2.map { v2 -> v1 * v2 } }).lock

        // Start Observing
        val observer = mockk<(Int) -> Unit>(relaxed = true)
        m.observeDataForever(observer)

        verify(exactly = 0) { observer(any()) }

        // Verity very first emit
        s1.value = 1
        verify(exactly = 0) { observer(any()) }
        s2.value = 1
        verify(exactly = 1) { observer(any()) }

        // Emit on s1, then twice on s1 and then on s1 again
        s1.value = 3 // s2 = 1 - total: 3
        s2.value = 2 // s1 = 3 - total: 6
        s2.value = 4 // s1 = 3 - total: 12
        s1.value = 4 // s2 = 4 - total: 16

        // Verify exactly, ordered, calls on the observer
        verify(Ordering.SEQUENCE) {
            observer(1)
            observer(3)
            observer(6)
            observer(12)
            observer(16)
        }
    }
}
