package cn.carbs.kotlin.ini.atom

import cn.carbs.kotlin.ini.atom.interfaces.IINIContent
import cn.carbs.kotlin.ini.position.INIPosition

class INIKVPair(val key: String, val value: String, val iniPosition: INIPosition?) : IINIContent {
    override fun getPosition(): INIPosition? {
        return iniPosition
    }

    override fun toINIOutputString(): String {
        if (key.isEmpty()) {
            throw IllegalStateException("Key of INIEntry should not be empty");
        }
        if (value.isEmpty()) {
            return "${key}="
        }
        return "${key}=${value}"
    }

    override fun toString(): String {
        return "INIKVPair{key='${key}', value='${value}', iniPosition=${iniPosition}}"
    }
}