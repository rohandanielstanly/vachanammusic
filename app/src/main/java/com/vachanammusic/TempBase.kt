package com.vachanammusic

object TempBase {
    var _data = HashMap<String, Any>()
    var allMusicListMap = ArrayList<HashMap<String?, Any?>?>()
    fun getData(key: String): String {
        return if (_data.containsKey(key)) {
            _data[key].toString()
        } else ""
    }

    fun getData(key: String, clear: Boolean): String {
        val st = getData(key)
        if (clear) {
            removeData(key)
        }
        return st
    }

    fun removeData(key: String) {
        if (_data.containsKey(key)) {
            _data.remove(key)
        }
    }

    fun addData(key: String, value: String) {
        _data[key] = value
    }

    fun destroy() {
        try {
            _data.clear()
        } catch (_: Exception) {
        }
    }
}
