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

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class ATNode(var i : Int, val state : State = State()) {

        private var next : ATNode? = null
        private var prev : ATNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = ATNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawATNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : ATNode  {
            var curr : ATNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class ArrowTarget(var i : Int) {

        private var curr : ATNode = ATNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : ArrowTargetView) {

        private val animator : Animator = Animator(view)
        private val at : ArrowTarget = ArrowTarget(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            at.draw(canvas, paint)
            animator.animate {
                at.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            at.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : ArrowTargetView {
            val view : ArrowTargetView = ArrowTargetView(activity)
            activity.setContentView(view)
            return view
        }
    }
}