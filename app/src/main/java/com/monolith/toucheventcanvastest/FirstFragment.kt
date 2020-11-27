package com.monolith.toucheventcanvastest

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import kotlin.random.Random


class FirstFragment : Fragment() {

    var moveview:MoveView?=null

    init{

    }


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first, container, false)
        val layout=view.findViewById<ConstraintLayout>(R.id.constlayout)
        moveview=MoveView(this.activity)
        layout.addView(moveview)
        layout.setWillNotDraw(false)

        //test(moveview!!)

        return view
    }

    fun test(mv:MoveView){
        val handler = Handler()
        handler.post(object : Runnable{
            override fun run() {
                mv.invalidate()
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ビューにリスナーを設定
        view.setOnTouchListener { _, event ->
            if (event.action==MotionEvent.ACTION_DOWN||event.action == MotionEvent.ACTION_MOVE) {
                //MoveView(this.activity).postInvalidate()
                test(moveview!!)
            }
            true
        }
    }


    class MoveView: View {
        constructor(context: Context?) : super(context)
        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
        constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

        override fun onDraw(canvas :Canvas?){
            super.onDraw(canvas)

            val paint = Paint()
            paint.color = Color.BLACK
            val rect = Rect(400+Random.nextInt(300), 600+Random.nextInt(300), 700+Random.nextInt(300), 900+Random.nextInt(300))

            canvas!!.drawRect(rect, paint)

        }
    }



}

