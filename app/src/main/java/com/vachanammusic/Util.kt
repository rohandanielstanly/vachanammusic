package com.vachanammusic

import android.content.Context
import android.widget.Toast

object Util {

    fun showMessage(_context: Context?, _s: String?) {
        Toast.makeText(_context, _s, Toast.LENGTH_SHORT).show()
    }



    fun getDisplayHeightPixels(_context: Context): Int {
        return _context.resources.displayMetrics.heightPixels
    }

}
