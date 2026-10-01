package com.fastvideosplitter.app

data class Scene(
    val number: Int,
    val startMs: Long,
    val endMs: Long,
    val human: Boolean = false,
    var selected: Boolean = false
) {
    val durationMs: Long get() = (endMs - startMs).coerceAtLeast(0)
}
