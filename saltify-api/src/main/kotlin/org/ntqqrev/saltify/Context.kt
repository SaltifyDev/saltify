package org.ntqqrev.saltify

import org.ntqqrev.saltify.action.*

/**
 * The context object, which represents the bot itself.
 */
interface Context : ContextAction, MessageAction, UserAction, GroupAction, RequestAction, FileAction {
    /**
     * The state of the context.
     */
    val state: State

    enum class State {
        /**
         * `(O_<)`
         * The context is newly created and not yet started.
         */
        INITIALIZED,

        /**
         * `(OwO)`
         * The context is started and ready to handle requests.
         */
        STARTED,

        /**
         * `(-_-) zzz`
         * The context is stopped and not running.
         * Note that a Context instance is not reusable after it is stopped.
         * A new Context instance is created whenever a new bot is started.
         */
        STOPPED,

        /**
         * `(>_<)`
         * The context encountered an issue and is temporarily interrupted.
         * This state is usually recoverable, and the context can be resumed.
         */
        INTERRUPTED,

        /**
         * `(T_T)`
         * The context encountered an error and is unexpectedly terminated.
         * This state is usually not recoverable.
         */
        TERMINATED,
    }
}