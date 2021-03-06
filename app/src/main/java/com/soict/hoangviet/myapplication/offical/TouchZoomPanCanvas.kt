package com.soict.hoangviet.myapplication.offical

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View

class TouchZoomPanCanvas : View {
    companion object {
        const val INVALID_POINTER_ID = -1
        const val MAX_SCALE = 5f
        const val MIN_SCALE = 1f
    }

    private val paint: Paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#000000")
    }
    private val radius = 100f

    // The ‘active pointer’ is the one currently moving our object.
    private var mActivePointerId =
        INVALID_POINTER_ID

    private lateinit var mScaleDetector: ScaleGestureDetector
    private var mScaleFactor = 1f

    private var mPosX = 0f
    private var mPosY = 0f

    private var mLastTouchX = 0f
    private var mLastTouchY = 0f

    private var viewWidth = 0
    private var viewHeight = 0


    constructor(context: Context) : super(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0) {
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        viewWidth = context.resources.displayMetrics.widthPixels
        viewHeight = context.resources.displayMetrics.heightPixels
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()

        if (mPosX != 0f && mPosY != 0f) {
            if (mPosX > (mScaleFactor - 1) * viewWidth / 2) {
                mPosX = (mScaleFactor - 1) * viewWidth / 2
            } else if (mPosX < (1 - mScaleFactor) * viewWidth / 2) {
                mPosX = (1 - mScaleFactor) * viewWidth / 2
            }
            if (mPosY > (mScaleFactor - 1) * viewHeight / 2) {
                mPosY = (mScaleFactor - 1) * viewHeight / 2
            } else if (mPosY < (1 - mScaleFactor) * viewHeight / 2) {
                mPosY = (1 - mScaleFactor) * viewHeight / 2
            }
        }
        canvas.translate(mPosX, mPosY)
        canvas.scale(mScaleFactor, mScaleFactor, viewWidth / 2f, viewHeight / 2f)
        //Draw
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
        canvas.restore()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        mScaleDetector.onTouchEvent(ev)

        val action = ev.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                val x = ev.x
                val y = ev.y
                mLastTouchX = x
                mLastTouchY = y

                // Save the ID of this pointer
                mActivePointerId = ev.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {

                // Find the index of the active pointer and fetch its position
                val pointerIndex = ev.findPointerIndex(mActivePointerId)
                val x = ev.getX(pointerIndex)
                val y = ev.getY(pointerIndex)

                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!mScaleDetector.isInProgress) {
                    val dx = x - mLastTouchX
                    val dy = y - mLastTouchY

                    mPosX += dx
                    mPosY += dy

                    if (mScaleFactor != 1f) {
                        invalidate()
                    }
                }
                mLastTouchX = x
                mLastTouchY = y
            }
            MotionEvent.ACTION_UP -> {
                mActivePointerId =
                    INVALID_POINTER_ID
            }
            MotionEvent.ACTION_CANCEL -> {
                mActivePointerId =
                    INVALID_POINTER_ID
            }
            MotionEvent.ACTION_POINTER_UP -> {

                // Extract the index of the pointer that left the touch sensor
                val pointerIndex = (action and MotionEvent.ACTION_POINTER_INDEX_MASK
                        shr MotionEvent.ACTION_POINTER_INDEX_SHIFT)
                val pointerId = ev.getPointerId(pointerIndex)
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    mLastTouchX = ev.getX(newPointerIndex)
                    mLastTouchY = ev.getY(newPointerIndex)
                    mActivePointerId = ev.getPointerId(newPointerIndex)
                }
            }
        }

        return true
    }

    private inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor

            // Don't let the object get too small or too large.
            mScaleFactor = MIN_SCALE.coerceAtLeast(mScaleFactor.coerceAtMost(MAX_SCALE))
            invalidate()
            return true
        }
    }
}
