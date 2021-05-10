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

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawArrowTarget(scale : Float, w : Float, h : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val sf3 : Float = sf.divideScale(2, parts)
    val sf4 : Float = sf.divideScale(3, parts)
    val r1 : Float = Math.min(w, h) / r1Factor
    val r2 : Float = Math.min(w, h) / r2Factor
    val line : Float = Math.min(w, h) / lineFactor
    val arrow : Float = Math.min(w, h) / arrowFactor
    save()
    translate(w / 2, h / 2)
    paint.style = Paint.Style.STROKE
    drawArc(RectF(-r1, -r1, r1, r1), 0f, 360f * sf1, false, paint)
    drawArc(RectF(-r2, -r2, r2, r2), 0f, 360f * sf2, false, paint)
    paint.style = Paint.Style.FILL
    save()
    translate(0f, -dFactor * r2 * (1 - sf4))
    rotate(deg)
    drawLine(0f, -line + line * sf3, 0f, -line, paint)
    save()
    translate(0f, -line)
    val path : Path = Path()
    path.moveTo(0f, 0f)
    path.lineTo(-arrow / 2, -arrow)
    path.lineTo(arrow / 2, -arrow)
    path.lineTo(0f, 0f)
    clipPath(path)
    drawRect(RectF(-arrow / 2, -arrow, -arrow / 2 + (arrow) * sf2, -arrow + arrow * sf2), paint)
    restore()
    restore()
    restore()
}

fun Canvas.drawATNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawArrowTarget(scale, w, h, paint)
}


class ArrowTargetView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }
}