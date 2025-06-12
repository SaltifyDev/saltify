package org.ntqqrev.saltify.utils.cache

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class AbstractCacheService<T : CachedEntity<D>, K, D>(val scope: CoroutineScope) {
    val updateMutex = Mutex()
    var currentTask: Deferred<Unit>? = null

    var currentCache = emptyMap<K, T>()

    abstract suspend fun fetchData(): Map<K, D>

    abstract fun constructNewEntity(data: D): T

    suspend fun get(key: K, cacheFirst: Boolean = true): T? {
        if (key !in currentCache || !cacheFirst) {
            updatePreventRepeated()
        }
        return currentCache[key]
    }

    suspend fun getAll(cacheFirst: Boolean = true): Iterable<T> {
        if (currentCache.isEmpty() || !cacheFirst) {
            updatePreventRepeated()
        }
        return currentCache.values
    }

    suspend fun updatePreventRepeated() {
        return updateMutex.withLock {
            currentTask?.let {
                if (it.isActive) {
                    return@withLock it
                }
            }

            val newTask = scope.async {
                val data = fetchData()
                val cacheSnapshot = currentCache
                currentCache = data.mapValues { (k, v) ->
                    cacheSnapshot[k]?.apply {
                        dataBinding = v
                    } ?: constructNewEntity(v)
                }
            }

            currentTask = newTask
            return@withLock newTask
        }.await()
    }
}