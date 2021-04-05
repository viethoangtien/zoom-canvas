package com.soict.hoangviet.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import androidx.core.content.ContextCompat
import kotlin.math.pow

class ZoomView : androidx.appcompat.widget.AppCompatImageView {

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        viewWidth = context.resources.displayMetrics.widthPixels
        viewHeight = context.resources.displayMetrics.heightPixels
    }

    private val paint: Paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#000000")
    }
    private val radius = 100f
    private var scaleFactor = 1f
    private val detector: ScaleGestureDetector
    private var mode = 0
    private var dragged: Boolean = false

    //These two variables keep track of the X and Y coordinate of the finger when it first
    //touches the screen
    private var startX = 0f
    private var startY = 0f

    //These two variables keep track of the amount we need to translate the canvas along the X
    //and the Y coordinate
    private var translateX = 0f
    private var translateY = 0f

    var currentPoint: PointF? = null

    var viewWidth = 0
    var viewHeight = 0

    //These two variables keep track of the amount we translated the X and Y coordinates, the last time we
    //panned.
    private var previousTranslateX = 0f
    private var previousTranslateY = 0f
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.getAction() and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mode = DRAG

                //We assign the current X and Y coordinate of the finger to startX and startY minus the previously translated
                //amount for each coordinates This works even when we are translating the first time because the initial
                //values for these two variables is zero.
                startX = event.x - previousTranslateX
                startY = event.y - previousTranslateY
            }
            MotionEvent.ACTION_MOVE -> {
                translateX = event.x - startX
                translateY = event.y - startY

                //We cannot use startX and startY directly because we have adjusted their values using the previous translation values.
                //This is why we need to add those values to startX and startY so that we can get the actual coordinates of the finger.
                val distance = Math.sqrt(
                    (event.x - (startX + previousTranslateX)).toDouble().pow(2.0) +
                            (event.y - (startY + previousTranslateY)).toDouble().pow(2.0)
                )
                if (distance > 0) {
                    dragged = true
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> mode =
                ZOOM
            MotionEvent.ACTION_UP -> {
                mode = NONE
                dragged = false

                //All fingers went up, so let&#039;s save the value of translateX and translateY into previousTranslateX and
                //previousTranslate
                previousTranslateX = translateX
                previousTranslateY = translateY
            }
            MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE

                //This is not strictly necessary; we save the value of translateX and translateY into previousTranslateX
                //and previousTranslateY when the second finger goes up
                previousTranslateX = translateX
                previousTranslateY = translateY
            }
        }
        detector.onTouchEvent(event)

        //We redraw the canvas only in the following cases:
        //
        // o The mode is ZOOM
        //        OR
        // o The mode is DRAG and the scale factor is not equal to 1 (meaning we have zoomed) and dragged is
        //   set to true (meaning the finger has actually moved)
        if (mode == DRAG && scaleFactor != MIN_ZOOM && dragged || mode == ZOOM) {
            invalidate()
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()

        //We&#039;re going to scale the X and Y coordinates by the same amount
        canvas.scale(
            scaleFactor,
            scaleFactor,
            0f,
            0f
        )

        //If translateX times -1 is lesser than zero, let&#039;s set it to zero. This takes care of the left bound
        if (translateX > 0) {
            translateX = 0f
        } else if (translateX < (1 - scaleFactor) * viewWidth) {
            translateX = (1 - scaleFactor) * viewWidth
        }
        if (translateY > 0) {
            translateY = 0f
        } else if (translateY < (1 - scaleFactor) * viewHeight) {
            translateY = (1 - scaleFactor) * viewHeight
        }

        //We need to divide by the scale factor here, otherwise we end up with excessive panning based on our zoom level
        //because the translation amount also gets scaled according to how much we&#039;ve zoomed into the canvas.
        canvas.translate(translateX / scaleFactor, translateY / scaleFactor)

        canvas.drawCircle(0f, 0f, radius, paint)
        for (i in 2..40 step 2) {
            canvas.drawCircle(radius * i, 0f, radius, paint)
            canvas.drawCircle(-radius * i, 0f, radius, paint)
            canvas.drawCircle(0f, radius * i, radius, paint)
            canvas.drawCircle(0f, -radius * i, radius, paint)
            canvas.drawCircle(radius * i, radius * i, radius, paint)
            canvas.drawCircle(radius * i, -radius * i, radius, paint)
            canvas.drawCircle(-radius * i, radius * i, radius, paint)
            canvas.drawCircle(-radius * i, -radius * i, radius, paint)
        }

        /* The rest of your canvas-drawing code */canvas.restore()
    }

    private inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = Math.max(
                MIN_ZOOM,
                Math.min(
                    scaleFactor,
                    MAX_ZOOM
                )
            )
            return true
        }
    }

    companion object {
        //These two constants specify the minimum and maximum zoom
        private const val MIN_ZOOM = 1f
        private const val MAX_ZOOM = 5f

        //These constants specify the mode that we&#039;re in
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }

    init {
        detector = ScaleGestureDetector(context, ScaleListener())
    }
}