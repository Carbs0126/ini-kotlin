package cn.carbs.kotlin.ini.atom

import cn.carbs.kotlin.ini.atom.interfaces.IINIContent
import cn.carbs.kotlin.ini.position.INIPosition

class INIEmpty(val iniPosition: INIPosition?) : IINIContent {

    override fun getPosition(): INIPosition? {
        return iniPosition
    }

    override fun toINIOutputString(): String {
        return ""
    }

    override fun toString(): String {
        return "INIEmpty{iniPosition=${iniPosition}}"
    }
}