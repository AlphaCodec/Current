package com.current.news

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

/**
 * Coil's default disk cache size is a percentage of total device storage
 * (roughly 2%) with no hard ceiling — fine on a phone with plenty of free
 * space, but it grows unbounded as the user scrolls through more and more
 * unique article thumbnails (especially now that Home/Search use infinite
 * scroll) and is never cleaned up automatically.
 *
 * Supplying our own [ImageLoader] here — picked up automatically by Coil
 * app-wide because this Application implements [ImageLoaderFactory] — caps
 * both the disk and memory caches at fixed, small sizes instead.
 */
class CurrentApp : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15) // modest slice of available RAM, not disk
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(MAX_DISK_CACHE_BYTES)
                    .build()
            }
            .build()

    companion object {
        // 75MB — enough for a long browsing session's worth of thumbnails
        // without silently eating device storage over time.
        const val MAX_DISK_CACHE_BYTES = 75L * 1024 * 1024
    }
}
