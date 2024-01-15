package cn.carbs.kotlin.ini.entity

import cn.carbs.kotlin.ini.atom.interfaces.IINIContent
import cn.carbs.kotlin.ini.position.INIPosition
import java.util.concurrent.ConcurrentHashMap

class INIObject {

    private var sectionsMap: ConcurrentHashMap<String, INISectionObject> = ConcurrentHashMap()

    private var orderedSectionsName: ArrayList<String> = ArrayList(8)

    fun generateStringLines(): ArrayList<String> {
        var iniContentLines: ArrayList<IINIContent> = ArrayList(8)
        for (sectionName in orderedSectionsName) {
            if (sectionsMap.containsKey(sectionName)) {
                var iniSectionObject: INISectionObject? = sectionsMap.get(sectionName)
                var oneSectionLines: ArrayList<IINIContent>? = iniSectionObject?.generateContentLines()
                if (!oneSectionLines.isNullOrEmpty()) {
                    iniContentLines.addAll(oneSectionLines)
                }
            }
        }
        // 排序  先 line number，后 start position
        val comparator = Comparator { a: IINIContent?, b: IINIContent? ->
            if (a == null || b == null) {
                return@Comparator 0
            }
            val iniPositionA: INIPosition? = a.getPosition()
            val iniPositionB: INIPosition? = b.getPosition()
            // 将 position 为空的元素排到最后
            if (iniPositionA == null) {
                return@Comparator 1
            }
            if (iniPositionB == null) {
                return@Comparator -1
            }
            var lineNumberDelta = iniPositionA.lineNumber - iniPositionB.lineNumber
            if (lineNumberDelta != 0) {
                return@Comparator lineNumberDelta
            }
            return@Comparator iniPositionA.charBegin - iniPositionB.charBegin
        }
        iniContentLines.sortWith(comparator)
        var stringLines: ArrayList<String> = ArrayList(8)
        var sbOneLine: StringBuilder = StringBuilder()
        var preLineNumber = -1
        var curLineNumber = -1
        for (iiniContent in iniContentLines) {
//            if (iiniContent == null) {
//                continue
//            }
            var curINIPosition = iiniContent.getPosition()
            if (curINIPosition == null) {
                if (sbOneLine.isNotEmpty()) {
                    stringLines.add(sbOneLine.toString())
                    sbOneLine.clear()
                }
                stringLines.add(iiniContent.toINIOutputString())
                continue
            }
            curLineNumber = curINIPosition.lineNumber
            if (preLineNumber != curLineNumber) {
                if (preLineNumber > -1) {
                    stringLines.add(sbOneLine.toString())
                    sbOneLine.clear()
                }
                var lineDelta = curLineNumber - preLineNumber
                if (lineDelta > 1) {
                    // 中间有空行
                    for (i in 0 until lineDelta - 1) {
                        stringLines.add("")
                    }
                }
                sbOneLine.append(iiniContent.toINIOutputString())
            } else {
                sbOneLine.append(iiniContent.toINIOutputString())
            }
            preLineNumber = curLineNumber
        }
        stringLines.add(sbOneLine.toString())
        return stringLines
    }

    fun addSection(section: INISectionObject) {
        orderedSectionsName.add(section.getName())
        sectionsMap.put(section.getName(), section)
    }

    fun getSection(sectionName: String?): INISectionObject? {
        if (sectionName.isNullOrEmpty() || !sectionsMap.containsKey(sectionName)) {
            return null
        }
        return sectionsMap.get(sectionName)
    }

    fun getSectionsMap(): ConcurrentHashMap<String, INISectionObject> {
        return sectionsMap
    }

    fun getOrderedSectionsName(): ArrayList<String> {
        return orderedSectionsName
    }

}