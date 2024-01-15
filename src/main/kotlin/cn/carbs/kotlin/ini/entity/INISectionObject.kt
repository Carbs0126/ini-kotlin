package cn.carbs.kotlin.ini.entity

import cn.carbs.kotlin.ini.atom.INIComment
import cn.carbs.kotlin.ini.atom.INISectionHeader
import cn.carbs.kotlin.ini.atom.interfaces.IINIContent

class INISectionObject {

    private var iniSectionHeader: INISectionHeader? = null

    private var comments: ArrayList<INIComment>? = null

    private var entryObjects: ArrayList<INIEntryObject>? = null

    fun addComment(comment: INIComment) {
        if (comments == null) {
            comments = ArrayList(8)
        }
        comments!!.add(comment)
    }

    fun addComments(comments: ArrayList<INIComment>?) {
        if (comments.isNullOrEmpty()) {
            return
        }
        if (this.comments == null) {
            this.comments = ArrayList(8)
        }
        this.comments!!.addAll(comments)
    }

    fun addEntryObject(entryObject: INIEntryObject) {
        if (entryObjects == null) {
            entryObjects = ArrayList(8)
        }
        entryObjects!!.add(entryObject);
    }

    fun getName(): String {
        return iniSectionHeader?.name ?: ""
    }

    fun setSectionHeader(sectionHeader:INISectionHeader) {
        iniSectionHeader = sectionHeader
    }

    fun generateContentLines(): ArrayList<IINIContent> {
        var lines: ArrayList<IINIContent> = ArrayList(8)
        if (!comments.isNullOrEmpty()) {
            lines.addAll(comments!!)
        }
        if (iniSectionHeader != null) {
            lines.add(iniSectionHeader!!)
        }
        entryObjects?.let {
            for (iniEntryObject in it) {
                var entryLines: ArrayList<IINIContent>? = iniEntryObject?.generateContentLines()
                if (!entryLines.isNullOrEmpty()) {
                    lines.addAll(entryLines)
                }
            }
        }
        return lines
    }

}