import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class CountUpView(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    private var startTimeStamp: Long = 0L

    private val countUpAction: Runnable = object : Runnable {
        override fun run() {
            val currentTimeStamp = SystemClock.elapsedRealtime()
            val countTimeSeconds = ((currentTimeStamp - startTimeStamp)/1000L).toInt()      // 얼마의 초가 차이가 나는지
            updateCountTime(countTimeSeconds)

            handler?.postDelayed(this, 1000L)
        }
    }

    fun startCountUp() {
        startTimeStamp = SystemClock.elapsedRealtime()
        handler?.post(countUpAction)
    }

    fun stopCountUp() {
        handler?.removeCallbacks(countUpAction)
    }

    fun clearCountTime(){
        updateCountTime(0)
    }

    // 텍스트에 반영하는 메소드
    private fun updateCountTime(countTimeSeconds: Int){
        val minutes = countTimeSeconds/60
        val seconds = countTimeSeconds%60

        text = "%02d:%02d".format(minutes, seconds)
    }


}
