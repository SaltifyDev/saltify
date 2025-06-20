package org.ntqqrev.saltify.command

interface ITokenizer<T> {
    fun hasMoreTokens(): Boolean
    fun read(): T
    fun unread()
    fun remaining(): T
}