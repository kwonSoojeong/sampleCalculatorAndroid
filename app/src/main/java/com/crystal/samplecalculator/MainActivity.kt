package com.crystal.samplecalculator

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.room.Room
import com.crystal.samplecalculator.dao.AppDatabase
import com.crystal.samplecalculator.model.History


class MainActivity : AppCompatActivity() {
    private val expressionTextView: TextView by lazy{
        findViewById(R.id.expressionTextView)
    }
    private val resultTextView: TextView by lazy{
        findViewById(R.id.resultTextView)
    }
    private val historyView: View by lazy{
        findViewById(R.id.historyLayout)
    }
    private val historyLinearView: LinearLayout by lazy{
        findViewById(R.id.historyLinearLayout)
    }
    /** 숫자, 오퍼레이터 입력에 따른 처리를 위함?*/
    private var isOperator = false
    /** operator 딱 하나만 사용하기 위한 플레그 */
    private var hasOperator = false

    lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "historyDB",
        ).build()
    }
    fun buttonClicked (v: View){
        when(v.id){
            R.id.button0 -> numberButtonClicked("0")
            R.id.button1 ->numberButtonClicked("1")
            R.id.button2 ->numberButtonClicked("2")
            R.id.button3 ->numberButtonClicked("3")
            R.id.button4 ->numberButtonClicked("4")
            R.id.button5 ->numberButtonClicked("5")
            R.id.button6 ->numberButtonClicked("6")
            R.id.button7 ->numberButtonClicked("7")
            R.id.button8 ->numberButtonClicked("8")
            R.id.button9 ->numberButtonClicked("9")
            R.id.buttonPlus -> operatorButtonClicked("+")
            R.id.buttonMinus-> operatorButtonClicked("-")
            R.id.buttonDivider-> operatorButtonClicked("/")
            R.id.buttonModulo->operatorButtonClicked("%")
            R.id.buttonMulti->operatorButtonClicked("x")
        }
    }
    private fun numberButtonClicked(number: String){
        if(isOperator){
            expressionTextView.append(" ")
        }
        isOperator = false


        val expressionText = expressionTextView.text.split(" ")
        if(number == "0" && expressionText.last().isEmpty()){
            Toast.makeText(this, "0은 제일 앞에 올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }else if(expressionText.isNotEmpty() && expressionText.last().length>=15){
            Toast.makeText(this, "15자리까지만 들어올 수 있습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        expressionTextView.append(number)
        resultTextView.text = calculateExpression()

    }
    private fun operatorButtonClicked(operator: String){
        if(expressionTextView.text.isEmpty()) {
            return
        }
        when {
            isOperator->{
                //operator 연속,
                val text = expressionTextView.text.toString()
                expressionTextView.text = text.dropLast(1) + operator
            }
            hasOperator->{
                Toast.makeText(this, "연산자는 한 번만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
                return
            }
            else ->{
                //ex) 0 + 0 중에 0만 있는상태에서 + 가 들어온 상황
                expressionTextView.append(" $operator")
            }
        }
        val ssb = SpannableStringBuilder(expressionTextView.text)
        ssb.setSpan(
            ForegroundColorSpan(getColor(R.color.green2)),
            expressionTextView.text.length-1,
            expressionTextView.text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        expressionTextView.text = ssb;

        isOperator = true
        hasOperator = true
    }
    fun resultButtonClicked(view: View) {
        //예외에 toast
        val expressionTexts = expressionTextView.text.split(" ")
        if( expressionTextView.text.isEmpty() || expressionTexts.size == 1){
            return
        }
        //마지막 숫자까지 안들어옴
        if(expressionTexts.size !=3 && hasOperator)  {
            Toast.makeText(this, "수식이 완성되지 않았습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if(expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()){
            Toast.makeText(this, "숫자 변환 시 오류 발생하였습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val expressionText = expressionTextView.text.toString()
        val resultText = calculateExpression()

        //DB에 데이터 넣어주는 부분
        Thread(Runnable {
            db.historyDao().insertHistory(History(null, expressionText, resultText))
        }).start()

        resultTextView.text = ""
        expressionTextView.text = resultText

        isOperator = false
        hasOperator = false
    }
    private fun calculateExpression(): String {
        //result 을 만든다.
        val expressionTexts = expressionTextView.text.split(" ")
        if ( hasOperator.not() || expressionTexts.size!=3){
            return ""
        } else if(expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()){
            return ""
        }
        val exp1 = expressionTexts[0].toBigInteger()
        val exp2 = expressionTexts[2].toBigInteger()
        val op = expressionTexts[1]
        return when (op){
            "+"-> (exp1+exp2).toString()
            "-"-> (exp1-exp2).toString()
            "x"-> (exp1*exp2).toString()
            "/"-> (exp1/exp2).toString()
            "%"-> (exp1%exp2).toString()
            else-> ""
        }
    }

    fun clearButtonClicked(view: View) {
        expressionTextView.text = ""
        resultTextView.text = ""
        isOperator = false
        hasOperator = false
    }

    fun historyButtonClicked(view: View) {
        historyView.isVisible = true
        historyLinearView.removeAllViews()

        //DB에서 모든 기록 가져와서
        //뷰에 모든 기록 할당하기(historyLinearLayout 붙여준다)
        Thread(Runnable {
            //순서를 뒤집는다
            db.historyDao().getAll().reversed().forEach{
                runOnUiThread{
                    //ui 작업은 main thread
                    val historyView = LayoutInflater.from(this).inflate(R.layout.history_row, null, false)
                    historyView.findViewById<TextView>(R.id.expressionTextView).text = it.expression
                    historyView.findViewById<TextView>(R.id.resultTextView).text = "= ${it.result}"

                    historyLinearView.addView(historyView)
                }
            }
        }).start()


    }
    fun closeHistoryButtonClicked(view: View) {
        historyView.isVisible = false
    }

    fun historyClearButtonClicked(view: View){
        //뷰에서 기록 삭제
        historyLinearView.removeAllViews()
        //DB에서 모든 기록 삭제
        Thread(Runnable {
            db.historyDao().deleteAll()
        }).start()
    }
}

fun String.isNumber():Boolean{
    return try{
//        this.toInt()//21억밖에 안  됨
        this.toBigInteger()
        true
    }catch (e :NumberFormatException){
        false
    }
}