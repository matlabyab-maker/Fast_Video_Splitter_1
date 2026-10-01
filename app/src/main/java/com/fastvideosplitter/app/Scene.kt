
package com.fastvideosplitter.app

import android.graphics.Bitmap

data class Scene(
    val id: Int,
    var startMs: Long,
    var endMs: Long,
    var human: Boolean = false,
    var thumbnail: Bitmap? = null,
    var selected: Boolean = false
) {
    val durationMs get() = (endMs - startMs).coerceAtLeast(0)
}
