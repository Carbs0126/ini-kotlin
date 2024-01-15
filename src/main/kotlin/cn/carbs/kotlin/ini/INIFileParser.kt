package cn.carbs.kotlin.ini

import cn.carbs.kotlin.ini.atom.INIComment
import cn.carbs.kotlin.ini.atom.INIEmpty
import cn.carbs.kotlin.ini.atom.INIKVPair
import cn.carbs.kotlin.ini.atom.INISectionHeader
import cn.carbs.kotlin.ini.atom.interfaces.IINIContent
import cn.carbs.kotlin.ini.entity.INIEntryObject
import cn.carbs.kotlin.ini.entity.INIObject
import cn.carbs.kotlin.ini.entity.INISectionObject
import cn.carbs.kotlin.ini.position.INIPosition
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

object INIFileParser {

    fun parseFileToINIObject(iniFile: File): INIObject {
        val CHARSET_NAME = "UTF-8"
        var content = ArrayList<String>(16)

        try {
            BufferedReader(InputStreamReader(FileInputStream(iniFile), CHARSET_NAME)).use { br ->
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    content.add(line!!)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val iniLines: ArrayList<IINIContent> = ArrayList(content.size)
        var lineNumber = 0
        val fileName = iniFile.absolutePath
        for (strLine in content) {
            var originLine: String = strLine ?: ""
            var trimmedLine: String = originLine.trim()
            if (trimmedLine.startsWith(';')) {
                // comment
                var iniComment = INIComment(originLine, INIPosition(fileName, lineNumber, 0, originLine.length))
                appendLineContentIntoLineList(iniComment, iniLines)
            } else if (trimmedLine.startsWith('[')) {
                // section header
                var rightSquareBracketsPosition = trimmedLine.indexOf(']')
                if (rightSquareBracketsPosition < 2) {
                    throw IllegalStateException("Right square bracket's position should be greater than 1, now it is $rightSquareBracketsPosition")
                }
                var sectionName = trimmedLine.substring(0, rightSquareBracketsPosition + 1)
                if (sectionName.contains(';')) {
                    throw IllegalStateException("Section's name should not contain ';' symbol")
                }
                var charBegin: Int = originLine.indexOf('[')
                var charEnd: Int = originLine.indexOf(']')
                var sectionHeader: INISectionHeader =
                    INISectionHeader(sectionName, INIPosition(fileName, lineNumber, charBegin, charEnd))
                appendLineContentIntoLineList(sectionHeader, iniLines)
                checkSemicolon(originLine, charEnd + 1, iniLines, fileName, lineNumber)
            } else if (trimmedLine.isEmpty()) {
                var iniEmpty = INIEmpty(INIPosition(fileName, lineNumber, 0, 0))
                appendLineContentIntoLineList(iniEmpty, iniLines)
            } else {
                // kv
                var indexOfEqualInTrimmedString = trimmedLine.indexOf('=')
                if (indexOfEqualInTrimmedString < 1) {
                    throw IllegalStateException("Equal's position should be greater than 0")
                }
                var indexOfEqualInOriginString = originLine.indexOf('=')
                var keyName = trimmedLine.substring(0, indexOfEqualInTrimmedString).trim()
                var rightStringOfEqual = trimmedLine.substring(indexOfEqualInTrimmedString + 1)
                var valueNameSB = StringBuilder()
                var length = rightStringOfEqual.length
                if (length > 0) {
                    // 0: 过滤前面的空格，还未找到value
                    // 1: 正在记录value
                    // 2: value结束
                    var stat = 0
                    var i = 0
                    while (i < length) {
                        var c: Char = rightStringOfEqual[i]
                        if (stat == 0) {
                            // 过滤前面的空格
                            if (c == ' ' || c == '\t') {
                                i++
                                continue
                            } else {
                                stat = 1
                                valueNameSB.append(c)
                            }
                        } else if (stat == 1) {
                            // 正在记录value
                            // value中允许有空格
                            if (c == ';') {
                                // 记录 value 结束
                                stat = 2
                                break
                            } else {
                                stat = 1
                                valueNameSB.append(c)
                            }
                        }
                        i++
                    }

                    var valueName = valueNameSB.toString()
                    var charBegin = originLine.indexOf(keyName)
                    var charEnd = indexOfEqualInOriginString + 1 + i
                    var iniKVPair = INIKVPair(keyName, valueName, INIPosition(fileName, lineNumber, charBegin, charEnd))
                    appendLineContentIntoLineList(iniKVPair, iniLines)
                    if (i != length) {
                        // 没有到结尾，检测是不是有分号
                        checkSemicolon(originLine, indexOfEqualInOriginString + 1 + i, iniLines, fileName, lineNumber)
                    }
                }
            }
            lineNumber++
        }

        // 最终解析为一个实体
        var iniObject = INIObject()
        // 收集 section 或者 kv 的 comments
        var commentsCache: ArrayList<INIComment> = ArrayList(8)
        // 解析完当前的 section ，一次存入
        var currentSectionObject: INISectionObject? = null
        // 解析当前的kvPair
        var currentEntryObject: INIEntryObject? = null

        // 0 解析 section 阶段，还没有解析到 section
        // 1 已经解析出 sectionName 阶段，(刚刚解析完 sectionHeader ) 还没有解析到下一个 section
        var parseState = 0
        var preINIContent: IINIContent? = null
        var curINIContent: IINIContent? = null
        for (iniContent in iniLines) {
            if (iniContent is INIEmpty) {
                continue
            }
            curINIContent = iniContent
            if (curINIContent is INIComment) {
                var iniComment: INIComment = curINIContent as INIComment
                if (parseState == 0) {
                    // 还没解析到 section
                    commentsCache.add(iniComment)
                } else {
                    if (preINIContent is INISectionHeader) {
                        if (checkSameLine(preINIContent, curINIContent)) {
                            // 当前 comment 属于 section
                            commentsCache.add(iniComment)
                            if (currentSectionObject == null) {
                                currentSectionObject = INISectionObject()
                            }
                            currentSectionObject.addComments(commentsCache)
                            commentsCache.clear()
                            // 当前 section 的所有 comment 已经结束
                        } else {
                            // 当前 comment 属于当前 section 的 kv 或者下一个 section 的 section
                            if (currentSectionObject == null) {
                                currentSectionObject = INISectionObject()
                            }
                            currentSectionObject.addComments(commentsCache)
                            commentsCache.clear()
                            commentsCache.add(iniComment)
                        }
                    } else if (preINIContent is INIComment) {
                        // comment 累加
                        commentsCache.add(iniComment)
                    } else if (preINIContent is INIKVPair) {
                        if (checkSameLine(preINIContent, curINIContent)) {
                            // 当前 comment 属于 kv
                            commentsCache.add(iniComment)
                            if (currentEntryObject == null) {
                                currentEntryObject = INIEntryObject()
                            }
                            currentEntryObject.addComments(commentsCache)
                            if (currentSectionObject == null) {
                                currentSectionObject = INISectionObject()
                            }
                            currentSectionObject.addEntryObject(currentEntryObject)
                            currentEntryObject = null
                            commentsCache.clear()
                            // 当前 kv 收尾
                        } else {
                            // 当前 comment 属于当前 section 的下一个 kv 或者下一个 section 的 section
                            commentsCache.clear()
                            commentsCache.add(iniComment)
                        }
                    }
                }
            } else if (curINIContent is INISectionHeader) {
                var iniSectionHeader: INISectionHeader = curINIContent as INISectionHeader
                if (parseState == 0) {
                    // 解析到第一个 section
                    parseState = 1
                    currentSectionObject = INISectionObject()
                    currentSectionObject.setSectionHeader(iniSectionHeader)
                } else {
                    if (preINIContent is INISectionHeader) {
                        // 连着两个 section header
                        // 收尾上一个 section header
                        if (currentSectionObject != null) {
                            currentSectionObject.addComments(commentsCache)
                            commentsCache.clear()
                            iniObject.addSection(currentSectionObject)
                        }
                        // 新建 section header
                        currentSectionObject = INISectionObject()
                        currentSectionObject.setSectionHeader(iniSectionHeader)
                    } else if (preINIContent is INIComment) {
                        if (commentsCache.size == 0) {
                            // 说明上一个 comment 和其之前的元素是一行，需要收尾上一个 section
                            if (currentSectionObject != null) {
                                iniObject.addSection(currentSectionObject)
                            }
                            currentSectionObject = INISectionObject()
                            currentSectionObject.setSectionHeader(iniSectionHeader)
                        } else {
                            currentSectionObject = INISectionObject()
                            currentSectionObject.setSectionHeader(iniSectionHeader)
                            currentSectionObject.addComments(commentsCache)
                            commentsCache.clear()
                        }
                    } else if (preINIContent is INIKVPair) {
                        // 说明上一个 section 结束了，需要收尾
                        if (currentSectionObject != null) {
                            if (currentEntryObject != null) {
                                currentSectionObject.addEntryObject(currentEntryObject)
                                currentEntryObject = null
                            }
                            iniObject.addSection(currentSectionObject)
                        }
                        currentSectionObject = INISectionObject()
                        currentSectionObject.setSectionHeader(iniSectionHeader)
                    }
                }
            } else if (curINIContent is INIKVPair) {
                var iniKVPair = curINIContent as INIKVPair
                if (parseState == 0) {
                    // 没有 section，就出现了 kv，说明格式出错
                    throw IllegalStateException("There should be a section header before key-value pairs")
                } else {
                    if (preINIContent is INISectionHeader) {
                        currentEntryObject = INIEntryObject()
                        currentEntryObject.iniKVPair = iniKVPair
                    } else if (preINIContent is INIComment) {
                        if (commentsCache.size == 0) {
                            // 说明上一行中，comment 是右边的注释，还包含左边的元素
                            // 当上一行的左侧是 section 时，不需要关心 section
                            // 当上一行的左侧是 kv 时，不需要关心当前 section 或者上一个 kv
                            currentEntryObject = INIEntryObject()
                            currentEntryObject.iniKVPair = iniKVPair
                        } else {
                            currentEntryObject = INIEntryObject()
                            currentEntryObject.iniKVPair = iniKVPair
                        }
                    } else if (preINIContent is INIKVPair) {
                        // 把前一个 kv 收尾到 section 中
                        if (currentEntryObject != null) {
                            currentEntryObject.addComments(commentsCache)
                            commentsCache.clear()
                            if (currentSectionObject != null) {
                                currentSectionObject.addEntryObject(currentEntryObject)
                            }
                        }
                        currentEntryObject = INIEntryObject()
                        currentEntryObject.iniKVPair = iniKVPair
                    }
                }
            }
            preINIContent = curINIContent
        }

        // 最后一个元素
        if (currentEntryObject != null) {
            currentEntryObject.addComments(commentsCache)
            commentsCache.clear()
        }
        if (currentSectionObject != null) {
            currentSectionObject.addComments(commentsCache)
            commentsCache.clear()
            if (currentEntryObject != null) {
                currentSectionObject.addEntryObject(currentEntryObject)
                currentEntryObject = null
            }
            iniObject.addSection(currentSectionObject)
        }
        return iniObject
    }

    private fun checkSameLine(preINIContent: IINIContent?, curINIContent: IINIContent?): Boolean {
        if (preINIContent == null && curINIContent == null) {
            return false
        }
        var prePosition: INIPosition? = preINIContent!!.getPosition()
        var curPosition: INIPosition? = curINIContent!!.getPosition()
        if (prePosition == null || curPosition == null) {
            return false
        }
        return prePosition.lineNumber == curPosition.lineNumber
    }

    private fun appendLineContentIntoLineList(iINIContent: IINIContent, iniLines: ArrayList<IINIContent>) {
        iniLines.add(iINIContent)
    }

    private fun checkSemicolon(
        originString: String,
        charBegin: Int,
        iniLines: ArrayList<IINIContent>,
        fileLocation: String,
        lineNumber: Int
    ): INIComment? {
        var remainStr = originString.substring(charBegin)
        var trimmedRemainStr = remainStr.trim()
        if (trimmedRemainStr.isNotEmpty()) {
            if (trimmedRemainStr.startsWith(';')) {
                var iniComment = INIComment(
                    trimmedRemainStr,
                    INIPosition(fileLocation, lineNumber, originString.indexOf(';'), originString.length)
                )
                appendLineContentIntoLineList(iniComment, iniLines)
                return iniComment
            } else {
                throw IllegalStateException("Need ';' symbol, but find ${trimmedRemainStr[0]} instead")
            }
        }
        return null
    }

}