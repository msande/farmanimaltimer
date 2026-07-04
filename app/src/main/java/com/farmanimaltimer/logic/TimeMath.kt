package com.farmanimaltimer.logic

object TimeMath {
    const val MAX_TOTAL_SECONDS: Long = 99L * 3600 + 99L * 60 + 99L // 362439

    fun fieldsToSeconds(h: Int, m: Int, s: Int): Long =
        h.toLong() * 3600 + m.toLong() * 60 + s.toLong()

    fun clampSeconds(total: Long): Long = total.coerceIn(0L, MAX_TOTAL_SECONDS)

    fun format(totalSeconds: Long): String {
        val t = clampSeconds(totalSeconds)
        val h = t / 3600
        val m = (t % 3600) / 60
        val s = t % 60
        return "%02d:%02d:%02d".format(h, m, s)
    }
}
