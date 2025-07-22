package org.example.util

object ProgressBarUtil {
    private const val RESET = "\u001B[0m"
    private const val GREEN = "\u001B[32m"

    @JvmStatic
    fun printProgress(current: Int, total: Int) {
        val progressWidth = 30
        val progress = (current.toDouble() / total * progressWidth).toInt()
        val percent = (current * 100 / total)

        val progressBar = StringBuilder("[")

        for (i in 0..<progressWidth) {
            if (i < progress) {
                progressBar.append(GREEN).append("#")
            } else {
                progressBar.append(" ")
            }
        }
        progressBar.append(RESET).append("] ").append(percent).append("%")
        print("\r" + progressBar)
    }
}
