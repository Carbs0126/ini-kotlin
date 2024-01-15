package cn.carbs.kotlin.ini

import java.io.File
import java.nio.file.Paths

fun main() {

    // 打印当前工作目录
//    System.out.println("Current Directory: " + Paths.get("").toAbsolutePath());
    var iniObject = INIFileParser.parseFileToINIObject(getINITestInputFile())
    INIFileGenerator.generateFileFromINIObject(iniObject, getINITestOutputFile().absolutePath)
}

fun getINITestInputFile(): File {
    return File(Paths.get("").toAbsolutePath().toString() + File.separator + "test-input.ini")
}

fun getINITestOutputFile(): File {
    return File(Paths.get("").toAbsolutePath().toString() + File.separator + "test-output.ini")
}
