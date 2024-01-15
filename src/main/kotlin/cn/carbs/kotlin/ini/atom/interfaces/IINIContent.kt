package cn.carbs.kotlin.ini.atom.interfaces

import cn.carbs.kotlin.ini.position.INIPosition

interface IINIContent {

    fun getPosition(): INIPosition?

    fun toINIOutputString(): String

}