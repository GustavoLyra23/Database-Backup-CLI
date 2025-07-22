package org.example.util

import com.google.gson.Gson
import java.io.FileReader
import java.io.IOException

object FileUtil {
    @Throws(IOException::class)
    fun <T> readDataJsonFile(path: String, clazz: Class<T?>): T? {
        val gson = Gson()
        FileReader(path).use { reader ->
            return gson.fromJson<T?>(reader, clazz)
        }
    }
}
