package tk.horiuchi.pokecom;

/**
 * Created by yoshimine on 2017/09/23.
 */

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;

public class RepeatButton extends AppCompatButton implements OnLongClickListener {

    /**
     * 連続してボタンを押す間隔(ms)
     */
    private static final int REPEAT_INTERVAL = 100;

    /**
     * 連打フラグ
     */
    private boolean isContinue = true;

    /**
     * ハンドラ
     */
    private Handler handler;

    public RepeatButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnLongClickListener(this);
        handler = new Handler();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        // キーから指が離されたら連打をオフにする
        if (event.getAction() == MotionEvent.ACTION_UP) {
            isContinue = false;
        }

        return true;
    }

    @Override
    public boolean onLongClick(View v) {
        isContinue = true;

        // 長押しをきっかけに連打を開始する
        handler.post(repeatRunnable);

        return true;
    }

    Runnable repeatRunnable = new Runnable() {
        @Override
        public void run() {
            // 連打フラグをみて処理を続けるか判断する
            if (!isContinue) {
                return;
            }

            // クリック処理を実行する
            performClick();

            // 連打間隔を過ぎた後に、再び自分を呼び出す
            handler.postDelayed(this, REPEAT_INTERVAL);
        }
    };
}