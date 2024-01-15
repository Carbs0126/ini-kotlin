package cn.carbs.kotlin.ini.atom

import cn.carbs.kotlin.ini.atom.interfaces.IINIContent
import cn.carbs.kotlin.ini.position.INIPosition

class INIComment(val comment: String, val iniPosition: INIPosition?) : IINIContent {

    override fun getPosition(): INIPosition? {
        return iniPosition
    }

    override fun toINIOutputString(): String {
        return comment
    }

    override fun toString(): String {
        return "INIComment{comment='$comment', iniPosition=$iniPosition}"
    }
}