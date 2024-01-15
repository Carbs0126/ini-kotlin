package cn.carbs.kotlin.ini.entity

import cn.carbs.kotlin.ini.atom.INIComment
import cn.carbs.kotlin.ini.atom.INIKVPair
import cn.carbs.kotlin.ini.atom.interfaces.IINIContent
import kotlin.collections.ArrayList

class INIEntryObject(var comments: ArrayList<INIComment>? = null, var iniKVPair: INIKVPair? = null) {

//    var comments: ArrayList<INIComment>? = null
//
//    var iniKVPair: INIKVPair? = null
//
//    constructor(comments: ArrayList<INIComment>?, iniKVPair: INIKVPair?) : this() {
//        this.comments = comments
//        this.iniKVPair = iniKVPair
//    }

    fun addComments(comments: ArrayList<INIComment>?) {
        if (comments.isNullOrEmpty()) {
            return
        }
        if (this.comments == null) {
            this.comments = ArrayList()
        }
        this.comments!!.addAll(comments)
    }

    fun addComment(comment: INIComment) {
        if (comments == null) {
            comments = ArrayList()
        }
        comments!!.add(comment)
    }

    fun generateContentLines(): ArrayList<IINIContent> {
        var lines = ArrayList<IINIContent>()
        if (!comments.isNullOrEmpty()) {
            lines.addAll(comments!!)
        }
        iniKVPair?.let { lines.add(it) }
        return lines
    }

}