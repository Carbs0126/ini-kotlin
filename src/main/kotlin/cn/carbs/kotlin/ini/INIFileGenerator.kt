package cn.carbs.kotlin.ini

import cn.carbs.kotlin.ini.entity.INIObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

object INIFileGenerator {

    fun generateFileFromINIObject(iniObject: INIObject, fileAbsolutePath: String): File? {
        if (iniObject == null) {
            throw IllegalStateException("IniObject should not be null")
        }
        var lines: ArrayList<String> = iniObject.generateStringLines()
        // 写入文件
//        if (lines == null) {
//            return null
//        }
        var outputFile: File? = null
        try {
            outputFile = writeFile(lines, fileAbsolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputFile
    }

    private fun writeFile(lines: ArrayList<String>, fileAbsolutePath: String): File {
        var outputFile: File = File(fileAbsolutePath)
        var fileOutputStream: FileOutputStream = FileOutputStream(outputFile)
        var bufferedWriter: BufferedWriter = BufferedWriter(OutputStreamWriter(fileOutputStream))
        if (lines != null) {
            var length = lines.size
            var index = 0
            for (line in lines) {
                bufferedWriter.write(line)
                if (index < length - 1) {
                    bufferedWriter.newLine()
                }
                index++
            }
        }
        bufferedWriter.close()
        return outputFile
    }

}