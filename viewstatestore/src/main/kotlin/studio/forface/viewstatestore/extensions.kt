@file:Suppress("unused")

package studio.forface.viewstatestore

import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/*
 * A set of extension functions
 * Author: Davide Giuseppe Farella
 */

// region factory
/**
 * Create a [ViewStateStore] from a [LiveData]
 *
 * @param liveData [LiveData] of [V] that will handle the main flow of [ViewStateStore]
 * @param [dropOnSame]
 *
 * @see ViewStateStore primary constructor
 *
 * @return [ViewStateStore] of [V]
 */
@Suppress("UNCHECKED_CAST") // LiveData.map function produces a MediatorLiveData, which is subtype of MutableLiveData
fun <V> ViewStateStore.Companion.from(
    liveData: LiveData<V>,
    dropOnSame: Boolean = ViewStateStoreConfig.dropOnSame
) = ViewStateStore(
    liveData = liveData.map { ViewState(it) } as MutableLiveData<ViewState<V>>,
    dropOnSame = dropOnSame
)

/**
 * Create a [ViewStateStore] from a [Flow]
 *
 * @param flow [Flow] of [V] that will handle the main flow of [ViewStateStore]
 *
 * @param [dropOnSame]
 *
 * @param context The [CoroutineContext] to collect the upstream flow in. Defaults to [EmptyCoroutineContext]
 * combined with [Dispatchers.Main.immediate][kotlinx.coroutines.MainCoroutineDispatcher.immediate]
 *
 * @param timeoutInMs The timeout in ms before cancelling the block if there are no active observers
 * ([LiveData.hasActiveObservers]. Defaults to [DEFAULT_TIMEOUT].
 *
 *
 * @see ViewStateStore primary constructor
 *
 * @return [ViewStateStore] of [V]
 */
@Suppress("UNCHECKED_CAST") // LiveData.map function produces a MediatorLiveData, which is subtype of MutableLiveData
fun <V> ViewStateStore.Companion.from(
    flow: Flow<V>,
    dropOnSame: Boolean = ViewStateStoreConfig.dropOnSame,
    context: CoroutineContext = EmptyCoroutineContext,
    timeoutInMs: Long = DEFAULT_TIMEOUT
) = from(flow.asLiveData(context, timeoutInMs), dropOnSame)

/**
 * Create a [ViewStateStore] from a [Flow]
 *
 * @param flow [Flow] of [V] that will handle the main flow of [ViewStateStore]
 *
 * @param [dropOnSame]
 *
 * @param context The [CoroutineContext] to collect the upstream flow in.
 * Defaults to [EmptyCoroutineContext] combined with
 * [Dispatchers.Main.immediate][kotlinx.coroutines.MainCoroutineDispatcher.immediate]
 *
 * @param timeout The timeout in [Duration] before cancelling the block if there are no active observers
 * ([LiveData.hasActiveObservers]
 *
 *
 * @see ViewStateStore primary constructor
 *
 * @return [ViewStateStore] of [V]
 */
@ExperimentalTime
@Suppress("UNCHECKED_CAST") // LiveData.map function produces a MediatorLiveData, which is subtype of MutableLiveData
fun <V> ViewStateStore.Companion.from(
    flow: Flow<V>,
    dropOnSame: Boolean = ViewStateStoreConfig.dropOnSame,
    context: CoroutineContext = EmptyCoroutineContext,
    timeout: Duration
) = from(flow.asLiveData(context, timeout.toLongMilliseconds()), dropOnSame)

private const val DEFAULT_TIMEOUT = 5000L
// endregion

// region set
/**
 * Set a [ViewState] with the given [state].
 * @see ViewStateStoreScope.setState
 */
@UiThread
fun <V> ViewStateStore<V>.set(state: ViewState<V>, dropOnSame: Boolean = this.dropOnSame) {
    setState(state, dropOnSame)
}

/**
 * Set a [ViewState] with the given [state].
 * @see ViewStateStoreScope.setState
 */
@UiThread
fun <V> ViewStateStore<V>.setState(state: ViewState<V>, dropOnSame: Boolean = this.dropOnSame) {
    setState(state, dropOnSame)
}

/**
 * Set a [ViewState.Success] with the given [data].
 * @see ViewStateStoreScope.setState
 */
@UiThread
fun <V> ViewStateStore<V>.set(data: V, dropOnSame: Boolean = this.dropOnSame) {
    set(data, dropOnSame)
}

/**
 * Set a [ViewState.Success] with the given [data].
 * The data will be delivered only once.
 * @see ViewStateStoreScope.setState
 */
@UiThread
fun <V> ViewStateStore<V>.setOnce(data: V, dropOnSame: Boolean = this.dropOnSame) {
    setOnce(data, dropOnSame)
}

/**
 * Set a [ViewState.Success] with the given [data].
 * @see ViewStateStoreScope.setState
 */
@UiThread
fun <V> ViewStateStore<V>.setData(data: V, dropOnSame: Boolean = this.dropOnSame) {
    setData(data, dropOnSame)
}

/**
 * Set a [ViewState.Success] with the given [data].
 * The data will be delivered only once.
 * @see ViewStateStoreScope.setState
 */
@UiThread
fun <V> ViewStateStore<V>.setDataOnce(data: V, dropOnSame: Boolean = this.dropOnSame) {
    setDataOnce(data, dropOnSame)
}

/**
 * Set a [ViewState.Error] created from the given [errorThrowable].
 * @see ViewStateStoreScope.setState
 */
@UiThread
fun ViewStateStore<*>.setError(
    errorThrowable: Throwable,
    dropOnSame: Boolean = this.dropOnSame,
    errorResolution: ErrorResolution? = null
) {
    setState(ViewState.Error.fromThrowable(errorThrowable, errorResolution), dropOnSame)
}

/**
 * Set a [ViewState.Loading].
 * @see ViewStateStoreScope.postState
 */
@UiThread
fun ViewStateStore<*>.setLoading(dropOnSame: Boolean = this.dropOnSame) {
    setState(ViewState.Loading, dropOnSame)
}
// endregion

// region post
/**
 * Post a [ViewState] with the given [state].
 * @see ViewStateStoreScope.postState
 */
fun <V> ViewStateStore<V>.postState(state: ViewState<V>, dropOnSame: Boolean = this.dropOnSame) {
    postState(state, dropOnSame)
}

/**
 * Post a [ViewState] with the given [state].
 * @see ViewStateStoreScope.postState
 */
fun <V> ViewStateStore<V>.post(state: ViewState<V>, dropOnSame: Boolean = this.dropOnSame) {
    postState(state, dropOnSame)
}

/**
 * Post a [ViewState.Success] with the given [data].
 * @see ViewStateStoreScope.postState
 */
fun <V> ViewStateStore<V>.post(data: V, dropOnSame: Boolean = this.dropOnSame) {
    post(data, dropOnSame)
}

/**
 * Post a [ViewState.Success] with the given [data].
 * The data will be delivered only once.
 * @see ViewStateStoreScope.postState
 */
fun <V> ViewStateStore<V>.postOnce(data: V, dropOnSame: Boolean = this.dropOnSame) {
    postOnce(data, dropOnSame)
}

/**
 * Post a [ViewState.Success] with the given [data].
 * @see ViewStateStoreScope.postState
 */
fun <V> ViewStateStore<V>.postData(data: V, dropOnSame: Boolean = this.dropOnSame) {
    postData(data, dropOnSame)
}

/**
 * Post a [ViewState.Success] with the given [data].
 * The data will be delivered only once.
 * @see ViewStateStoreScope.postState
 */
fun <V> ViewStateStore<V>.postDataOnce(data: V, dropOnSame: Boolean = this.dropOnSame) {
    postDataOnce(data, dropOnSame)
}

/**
 * Post a [ViewState.Error] created from the given [errorThrowable].
 * @see ViewStateStoreScope.postState
 */
fun ViewStateStore<*>.postError(
    errorThrowable: Throwable,
    dropOnSame: Boolean = this.dropOnSame,
    errorResolution: ErrorResolution? = null
) {
    postState(ViewState.Error.fromThrowable(errorThrowable, errorResolution), dropOnSame)
}

/**
 * Post a [ViewState.Loading].
 * @see ViewStateStoreScope.postState
 */
fun ViewStateStore<*>.postLoading(dropOnSame: Boolean = this.dropOnSame) {
    postState(ViewState.Loading, dropOnSame)
}
// endregion
