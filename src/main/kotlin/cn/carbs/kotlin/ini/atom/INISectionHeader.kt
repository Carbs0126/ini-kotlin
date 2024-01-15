package cn.carbs.kotlin.ini.atom

import cn.carbs.kotlin.ini.atom.interfaces.IINIContent
import cn.carbs.kotlin.ini.position.INIPosition

class INISectionHeader(val name: String, val iniPosition: INIPosition?) : IINIContent {
    override fun getPosition(): INIPosition? {
        return iniPosition
    }

    override fun toINIOutputString(): String {
        if (name.isEmpty()) {
            throw IllegalStateException("Key of INISectionHeader should not be empty");
        }
        return name;
    }

    override fun toString(): String {
        return "INISectionHeader{name='${name}', iniPosition=${iniPosition}}"
    }
}