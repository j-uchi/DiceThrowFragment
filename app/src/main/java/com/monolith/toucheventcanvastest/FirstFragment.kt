package com.monolith.toucheventcanvastest

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment


class FirstFragment : Fragment() {

    var moveview: MoveView? = null

    //タップ位置Y軸の保持用
    var posY = 0f

    //ダイスの出目保持用
    var num = 1

    //初速、時間の保持用
    var v0 = 0f
    var t = 0

    //手を離した位置の保持用
    var detach = 0f

    //0=押下前　1=タッチ中 2=空中 3=着地後
    var dicestate = 0

    var baseline = 0f

    //ダイスの画像保持用
    lateinit var img_dice: Array<Bitmap>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first, container, false)
        val layout = view.findViewById<ConstraintLayout>(R.id.constlayout)

        //動的ビューをレイアウトに配置する
        moveview = MoveView(this.activity)
        layout.addView(moveview)
        layout.setWillNotDraw(false)

        //画像イメージを読み込む
        img_dice = arrayOf(
            BitmapFactory.decodeResource(resources, R.drawable.dice_10),
            BitmapFactory.decodeResource(resources, R.drawable.dice_20),
            BitmapFactory.decodeResource(resources, R.drawable.dice_30),
            BitmapFactory.decodeResource(resources, R.drawable.dice_40),
            BitmapFactory.decodeResource(resources, R.drawable.dice_50),
            BitmapFactory.decodeResource(resources, R.drawable.dice_60),
            BitmapFactory.decodeResource(resources, R.drawable.dice_11),
            BitmapFactory.decodeResource(resources, R.drawable.dice_21),
            BitmapFactory.decodeResource(resources, R.drawable.dice_31),
            BitmapFactory.decodeResource(resources, R.drawable.dice_41),
            BitmapFactory.decodeResource(resources, R.drawable.dice_51),
            BitmapFactory.decodeResource(resources, R.drawable.dice_61)
        )


        //動的ビューを実行
        animrun(moveview!!)

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ビューにリスナーを設定
        view.setOnTouchListener { _, event ->
            //画面を押下または移動したとき
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                //ダイスが静止/押下中の処理
                if (dicestate < 2) {
                    //指の位置にダイスを追従させ、ダイスの状態はタップ中の「１」に設定
                    posY = event.y
                    dicestate = 1
                }
            }
            //画面から指を離したとき
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                //下に引っ張った状態且つダイスがタップ中の場合
                //※ダイスが空中や停止後ステータスの場合は動かさない為
                if (posY > baseline && dicestate == 1) {
                    dicestate = 2
                    //初速を設定
                    v0 = (posY - baseline) * 1.5f
                    //最大初速を制限
                    if (v0 > 1000) v0 = 1000f
                    //投げ上げの開始点(手を離した場所)を設定
                    detach = posY
                }
                //上にある状態で手を離したら状態を戻す
                else if (dicestate == 1) {
                    dicestate = 0
                    posY = baseline
                    num=(0..5).random()
                }
            }

            true
        }

        view.post {
            baseline=view.height/8*6f
        }
    }

    fun animrun(mv: MoveView) {
        val handler = Handler()

        //サブスレッドで実行
        handler.post(object : Runnable {
            //Cでいうmain関数
            override fun run() {

                //ダイスの状態に
                when (dicestate) {
                    //投げる前
                    0 -> {
                        //位置を固定する（この行は必要ないけど一応）
                        posY = baseline
                    }
                    //タップ中
                    1 -> {
                        //番号をランダムで入れ替える
                        num = (0..11).random()
                    }
                    //空中
                    2 -> {
                        //番号をランダムで入れ替える
                        num = (0..11).random()
                        //時間を進める
                        t += 1
                        //位置を移動する
                        posY = (detach - ((v0 * t - 0.5 * 9.8 * t * t)) / 5).toFloat()
                        //状態を確認する
                        state_check()
                    }
                    //投げた後
                    3 -> {

                    }
                }
                //再描画
                mv.invalidate()
                handler.postDelayed(this, 0)
            }
        })
    }


    //ダイス状態を初期化
    private fun dice_reset() {
        dicestate = 0
        v0 = 0f
        t = 0
    }

    //ダイスが空中にある際の状態を確認
    private fun state_check() {
        //ベースラインより下まで落ちている場合
        if (posY >= baseline) {
            //初速100以下の場合は静止
            if (v0 <= 100) {
                posY = baseline
                dicestate = 3
                //以下初期化処理
                dice_reset()
                //サイコロの状態は0~5でランダム設定。ここで確定する
                num=(0..5).random()
            } else {//初速が100越えの場合は初速を半分にして反発
                //反発したポジションを基準線に設定し、経過時間を0にする
                posY = baseline
                detach = baseline
                t = 0
                v0 /= 2
            }
        }
    }


    //動的ビューのクラス
    inner class MoveView : View {
        constructor(context: Context?) : super(context)
        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
        constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        )

        @SuppressLint("DrawAllocation")
        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)

            val paint = Paint()

            //キャンバスの状態を保存
            canvas!!.save()

            //画像サイズを小さくするため一時的にスケールを20%に設定
            canvas.scale(0.2f, 0.2f)

            //ダイスのイメージを描画
            //width*2.5にしている理由↓
            //20%になっているので全体の幅を5倍にすると通常幅、それの半分の位置にしたいので2.5倍
            canvas.drawBitmap(
                img_dice[num],
                width *2.5f - (img_dice[num].width / 2f),
                posY * 5 - img_dice[num].height / 2,
                paint
            )

            //スケールを100%に戻す(リストアする)
            canvas.restore()

            //以下デバッグ
            //       ペイントする色の設定
            //paint.color = Color.argb(255, 0, 0, 255)
            // ペイントストロークの太さを設定
            //paint.strokeWidth = 2f

            // Styleのストロークを設定する
            //paint.style = Paint.Style.STROKE

            //canvas.drawRect(width / 2f, 0f, width / 2f, height*1f, paint)
            //canvas.drawRect(0f, height/2f, width*1f, height/2f, paint)

            //paint.color = Color.argb(255, 255, 0, 0)
            //canvas.drawRect(0f, posY, width*1f, posY, paint)
        }
    }


}

