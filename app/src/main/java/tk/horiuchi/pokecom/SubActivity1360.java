package tk.horiuchi.pokecom;

import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by yoshimine on 2017/07/29.
 */

public class SubActivity1360 extends SubActivityBase {
    //private boolean _04game;

    private final int BASIC_TEXT_START  = 0x8030;
    private final int BASIC_START_ADR_L = 0xffd7;
    private final int BASIC_START_ADR_H = 0xffd8;
    private final int BASIC_END_ADR_L   = 0xffd9;
    private final int BASIC_END_ADR_H   = 0xffda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBtnResIds = new int[] {
                R.id.buttonMODE, R.id.buttonBRK, R.id.buttonDA, R.id.buttonUA, R.id.buttonLA, R.id.buttonRA,
                R.id.buttonKANA, R.id.buttonDEL, R.id.buttonINS, R.id.buttonSHIFT2, R.id.buttonCE,

                R.id.buttonSHIFT,
                R.id.buttonQ, R.id.buttonW, R.id.buttonE, R.id.buttonR, R.id.buttonT,
                R.id.buttonY, R.id.buttonU, R.id.buttonI, R.id.buttonO, R.id.buttonP,
                R.id.buttonDEF,
                R.id.buttonA, R.id.buttonS, R.id.buttonD, R.id.buttonF, R.id.buttonG,
                R.id.buttonH, R.id.buttonJ, R.id.buttonK, R.id.buttonL, R.id.buttonEQ,
                R.id.buttonSML,
                R.id.buttonZ, R.id.buttonX, R.id.buttonC, R.id.buttonV, R.id.buttonB,
                R.id.buttonN, R.id.buttonM, R.id.buttonSPC, R.id.buttonENTER,

                R.id.button7, R.id.button8, R.id.button9, R.id.buttonCOMMA, R.id.buttonCLN,
                R.id.button4, R.id.button5, R.id.button6, R.id.buttonDIV, R.id.buttonSCLN,
                R.id.button1, R.id.button2, R.id.button3, R.id.buttonMLT, R.id.buttonRSV1,
                R.id.button0, R.id.buttonDOT, R.id.buttonPLS, R.id.buttonMINUS, R.id.buttonRSV2

        };

        instance = this;
        activityId = 1360;
        setContentView(R.layout.activity_main_1360);

        SurfaceView sv = (SurfaceView) findViewById(R.id.surfaceView);
        ml = new MainLoop1360(this, sv);

        // キーボード作成
        kb = new KeyBoard1360();

        // Buttonインスタンスの取得
        // ButtonインスタンスのリスナーをこのActivityクラスそのものにする
        mBtnStatusCnt = new int[mBtnResIds.length];
        for (int i = 0; i < mBtnResIds.length; i++) {
            mBtnStatusCnt[i] = 0;
            //findViewById(mBtnResIds[i]).setOnClickListener(this);
            findViewById(mBtnResIds[i]).setOnTouchListener(this);
        }
        // ボタンの枠表示を切り替える
        if (debug_info) {
            changeButtonFrame(mBtnResIds, true);
        } else {
            changeButtonFrame(mBtnResIds, false);
        }

        // デバッグウィンドウの設定
        setDebugWindow((TextView) findViewById(R.id.debugWindow));

        // BASICテキスト領域の管理アドレスを初期化
        basicTextStart = BASIC_TEXT_START;
        basicStartAdrL = BASIC_START_ADR_L;
        basicStartAdrH = BASIC_START_ADR_H;
        basicEndAdrL = BASIC_END_ADR_L;
        basicEndAdrH = BASIC_END_ADR_H;

        cmd_tbl = new String[] {
                "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                " ", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-", ".", "/",
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?",
                "@", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
                "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "[", "\\", "]", "^", "_",
                "'", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
                "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "{", "|", "}", "\0", "\0",
                "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "\0", "LN", "LOG", "EXP", "SQR", "SIN", "COS", "TAN", "INT", "ABS", "SGN", "DEG", "DMS", "ASN", "ACS", "ATN",
                "RND", "AND", "OR", "NOT", "ASC", "VAL", "LEN", "PEEK", "CHR$", "STR$", "MID$", "LEFT$", "RIGHT$", "INKEY$", "PI", "MEM",
                "RUN", "NEW", "CONT", "PASS", "LIST", "LLIST", "CSAVE", "CLOAD", "MARGE", "\0", "\0", "OPEN", "CLOSE", "SAVE", "LOAD", "CONSOLE",
                "RANDOM", "DEGREE", "RADIAN", "GRAD", "BEEP", "WAIT", "GOTO", "TRON", "TROFF", "CLEAR", "USING", "DIM", "CALL", "POKE", "CLS", "CURSOR",
                "TO", "STEP", "THEN", "ON", "IF", "FOR", "LET", "REM", "END", "NEXT", "STOP", "READ", "DATA", "PAUSE", "PRINT", "INPUT",
                "GOSUB", "AREAD", "LPRINT", "RETURN", "RESTORE", "CHAIN", "GCURSOR", "GPRINT", "LINE", "POINT", "PSET", "PRESET", "BASIC", "TEXT", "OPEN$", "\0",
                "\0", "\\YEAR", "\\MON", "\\DAY", "\\YEN", "\\SP", "\\HT", "\\DI", "\\CL", "\\BX", "\\INS", "\\PI", "\\SQR", "\0", "\0", "\0"
        };

        kana  = true;

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        stretchItemSize((GridLayout)findViewById(R.id.keyArea), (ImageView)findViewById(R.id.keyAreaImageView), 57);
        //stretchItemSize((GridLayout)findViewById(R.id.tenKeyArea), (ImageView)findViewById(R.id.tenkey));

    }

    @Override
    public void onResume() {
        super.onResume();
        // ボタン表示枠の更新
        if (debug_info) {
            changeButtonFrame(mBtnResIds, true);
        } else {
            changeButtonFrame(mBtnResIds, false);
        }
    }

    //@Override
    //public void onClick(View v) {
    //    super.onClick(v);
    //}

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();

        int inputDevice = event.getSource();
        Log.w("dispatchKeyEvent", String.format("inputDevice=%x", inputDevice));

        if ((inputDevice & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD) {
            if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT) {
                if (action == KeyEvent.ACTION_DOWN) {
                    Log.w("dispatchKeyEvent", "onKeyDown!! -> ALT hooked");
                    kb.setBtnStatus(R.id.buttonMODE, true);
                    mBtnStatusCnt[getBtnIdx(R.id.buttonMODE)] = -1;
                } else {
                    mBtnStatusCnt[getBtnIdx(R.id.buttonMODE)] = 3;
                }
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

}
