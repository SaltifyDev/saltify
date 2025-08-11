package org.ntqqrev.lagrange.cache

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.ntqqrev.lagrange.LagrangeContext

internal abstract class AbstractCacheService<T, K, D>(val ctx: LagrangeContext) {
    private val updateMutex = Mutex()
    private var currentTask: Deferred<Unit>? = null

    protected var currentCache = emptyMap<K, T>()

    protected abstract suspend fun fetchData(): Map<K, D>

    protected abstract fun constructNewEntity(existing: T?, data: D): T

    suspend fun get(key: K, cacheFirst: Boolean = true): T? {
        if (key !in currentCache || !cacheFirst) updatePreventRepeated()
        return currentCache[key]
    }

    suspend fun getAll(cacheFirst: Boolean = true): Iterable<T> {
        if (currentCache.isEmpty() || !cacheFirst) updatePreventRepeated()
        return currentCache.values
    }

    private suspend fun updatePreventRepeated() {
        return updateMutex.withLock {
            currentTask?.let { if (it.isActive) return@withLock it }
            val newTask = ctx.env.scope.async {
                val data = fetchData()
                val cacheSnapshot = currentCache
                currentCache = data.mapValues { (k, v) ->
                    constructNewEntity(cacheSnapshot[k], v)
                }
            }
            currentTask = newTask
            return@withLock newTask
        }.await()
    }
}


