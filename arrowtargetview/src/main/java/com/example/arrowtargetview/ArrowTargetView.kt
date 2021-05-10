package com.example.arrowtargetview

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.app.Activity
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.graphics.Path

val colors : Array<Int> = arrayOf(
    "#54EE11",
    "#CC1134",
    "#1122CC",
    "#DEAB12",
    "#56ADFE"
).map {
    Color.parseColor(it)
}.toTypedArray()
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")
val parts : Int = 4
val scGap : Float = 0.02f / parts
val strokeFactor : Float = 90f
val r1Factor  : Float = 12.2f
val r2Factor : Float = 3.9f
val lineFactor : Float = 6.7f
val arrowFactor : Float = 14.2f
val dFactor : Float = 3.2f
val deg : Float = 45f