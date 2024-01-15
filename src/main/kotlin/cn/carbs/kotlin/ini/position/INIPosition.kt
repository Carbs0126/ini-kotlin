package cn.carbs.kotlin.ini.position

class INIPosition(val fileLocation: String, val lineNumber: Int, val charBegin: Int, val charEnd: Int) {
    override fun toString(): String {
        return "INIPosition{" +
                "fileLocation=${fileLocation}, " +
                "lineNumber=${lineNumber}, " +
                "charBegin=${charBegin}, " +
                "charEnd=${charEnd}}"
    }
}