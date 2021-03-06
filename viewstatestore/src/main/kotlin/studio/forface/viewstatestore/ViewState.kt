@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package studio.forface.viewstatestore

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import studio.forface.viewstatestore.ViewState.Error
import studio.forface.viewstatestore.ViewState.Error.Companion.createDefault
import studio.forface.viewstatestore.ViewState.Error.Companion.fromThrowable
import studio.forface.viewstatestore.ViewState.Loading
import studio.forface.viewstatestore.ViewState.None
import studio.forface.viewstatestore.ViewState.Success

/**
 * This class hold _data_ for [ViewStateStore].
 * The [data] could be a [Success], an [Error], a [Loading] or [None]
 *
 * @param T the type of the [Success] data
 *
 *
 * @author Davide Giuseppe Farella
 */
sealed class ViewState<out T> {

    /** An instance of [T] that will be available in case of [Success], else it will be null */
    open val data: T? = null

    /**
     * A class that represents the success and will contains the [data] [T]
     * Inherit from [ViewState]
     *
     * @param singleEvent if `true` this [ViewState] will be delivered only once, so it won't be
     * delivered again when the `LifecycleOwner` back in an active state.
     * Default is `false`
     *
     * @see setOnce
     * @see postOnce
     * @see setDataOnce
     * @see postDataOnce
     */
    data class Success<out T> internal constructor(
        override val data: T,
        internal var singleEvent: Boolean
    ) : ViewState<T>() {

        constructor(data: T) : this(data, false)
    }

    /**
     * A class that represents a failure and will contains the relative [throwable].
     * Inherit from [ViewState]
     *
     *
     * @constructor is private so [Error] cannot be instantiated outside this class or a child class.
     * Use [fromThrowable] instead.
     *
     * This object can also be instantiated inside the library through [createDefault] function on the companion object.
     *
     *
     * @property customMessage an OPTIONAL [CharSequence] representing a custom message to show to the user
     *
     * @see ViewState.Error( [Throwable], [CharSequence] )
     *
     *
     * @property customMessageRes an OPTIONAL [StringRes] representing a custom message to show to the user, it can
     * be coupled with
     * @property customMessageResArgs a vararg or [Any] representing arguments for [customMessageRes]
     *
     * @see ViewState.Error( [Throwable], [StringRes], vararg [Any] )
     */
    open class Error private constructor(
        val throwable: Throwable,
        private val customMessage: CharSequence?,
        @StringRes private val customMessageRes: Int?,
        vararg customMessageResArgs: Any
    ) : ViewState<Nothing>() {

        companion object {

            /** @return an instance of [ViewState.Error] with an OPTIONAL [ErrorResolution] */
            fun fromThrowable(throwable: Throwable, resolution: ErrorResolution? = null) =
                create(throwable).appendResolution(resolution)

            /**
             * @return an instance of [ViewState.Error] created from [ErrorStateGenerator] with the given [Throwable]
             */
            private fun create(throwable: Throwable) =
                ViewStateStoreConfig.errorStateGenerator(ErrorStateFactory(throwable), throwable)

            /**
             * @return a new instance of [ViewState.Error]
             * This function is used for instantiate a [ViewState.DefaultError] inside the library.
             */
            internal fun createDefault(throwable: Throwable): Error = DefaultError(throwable)
        }

        /** @return a [String] message from [throwable] */
        private val throwableMessage: String
            get() = with(throwable) {
                localizedMessage ?: message ?: "error"
            }

        /** Strong reference of [Array] of [Any] to homonym vararg in primary constructor */
        private val customMessageResArgs: Array<Any> = customMessageResArgs.toList().toTypedArray()

        /**
         * An OPTIONAL [ErrorResolution] for this instance [ViewState.Error]
         *
         * E.g. for an hypothetical `DocumentsSyncError` instance of [ViewState.Error] a possible resolution could be
         * `{ DocumentsSyncService.tryToSync() }`
         *
         * Set it via [appendResolution]
         */
        private var resolution: ErrorResolution? = null


        /**
         * @constructor INTERNAL that takes only a [Throwable]
         *
         * @param throwable
         * @see ViewState.Error.throwable
         */
        internal constructor(throwable: Throwable) : this(throwable, null, null)

        /**
         * @constructor PROTECTED that takes a [Throwable] and a [CharSequence]
         *
         * @param throwable
         * @see ViewState.Error.throwable
         *
         * @param customMessage
         * @see ViewState.Error.customMessage
         */
        protected constructor(
            throwable: Throwable,
            customMessage: CharSequence
        ) : this(throwable, customMessage, null)

        /**
         * @constructor PROTECTED that takes a [Throwable] a [StringRes] and a vararg of [Any]
         *
         * @param throwable
         * @see ViewState.Error.throwable
         *
         * @param customMessageRes
         * @see ViewState.Error.customMessageRes
         *
         * @param args
         * @see ViewState.Error.customMessageResArgs
         */
        protected constructor(
            throwable: Throwable,
            @StringRes customMessageRes: Int,
            vararg args: Any
        ) : this(throwable, null, customMessageRes, args)


        /**
         * @return a [CharSequence] trying to resolve from [customMessage] if not `null`, else [customMessageRes] with
         * [customMessageResArgs] if not `null`, else [throwableMessage]
         */
        fun getMessage(context: Context) = getMessage(context.resources)

        /**
         * @return a [CharSequence] trying to resolve from [customMessage] if not `null`, else [customMessageRes] with
         * [customMessageResArgs] if not `null`, else [throwableMessage]
         */
        fun getMessage(resources: Resources): CharSequence {
            return customMessage
                ?: customMessageRes?.let { resources.getString(it, * customMessageResArgs) }
                ?: throwableMessage
        }

        /** [Throwable.printStackTrace] from [throwable] */
        fun printStackTrace() = throwable.printStackTrace()

        /**
         * Set a [ErrorResolution] for [ViewState.Error.resolution]
         *
         * @return this [ViewState.Error]
         */
        fun appendResolution(resolution: ErrorResolution?) = apply {
            this.resolution = resolution
        }

        /** @return `true` if [ViewState.Error.resolution] is not `null` */
        fun hasResolution() = resolution != null

        /**
         * Invoke [resolution] lambda
         * @throws NoResolutionException if [resolution] is `null`
         */
        fun resolve() = resolution?.invoke() ?: throw NoResolutionException(this)

        /** Invoke [resolution] lambda if not `null`, else do nothing */
        fun tryToResolve() = resolution?.invoke()
    }

    /** A default ( not customized ) [ViewState.Error] that only holds a [Throwable] */
    internal class DefaultError(throwable: Throwable) : Error(throwable)

    /**
     * A class that represents the loading state and will contains [Nothing]
     * Inherit from [ViewState]
     */
    object Loading : ViewState<Nothing>()

    /**
     * A class that represents the unknown state and will contains [Nothing]
     * Inherit from [ViewState]
     */
    object None : ViewState<Nothing>()
}

/**
 * An entity that contains any type of [ViewState]
 * Use it for destruction
 * e.g.
 * ```
 * for ((onData, onError, onLoadingChange) in viewStateStore.composed) {
 *     onData?.let {
 *         updateUi(it)
 *     }
 *     onError?.let {
 *         showError(it)
 *     }
 *     onLoadingChange?.let {
 *         showProgress(it)
 *     }
 * }
 * ```
 */
data class ComposedViewState<out T>(
    val success: T?,
    val error: Error?,
    val loading: Loading?
)

internal fun <T> ViewState<T>.asComposed() = ComposedViewState<T>(
    success = (this as? Success<T>)?.data,
    error = this as? Error,
    loading = this as? Loading
)

/** @constructor for [ViewState.Success] */
@Suppress("FunctionName")
fun <T> ViewState(data: T) = Success(data)

/** Typealias for a lambda that resolves a [ViewState.Error] */
typealias ErrorResolution = () -> Unit
