package tk.horiuchi.pokecom;

import android.os.Bundle;
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


    private int[] mBtnResIds = {
            R.id.buttonMODE, R.id.buttonBRK, R.id.buttonDA, R.id.buttonUA, R.id.buttonLA, R.id.buttonRA,
            R.id.buttonDEL, R.id.buttonINS, R.id.buttonSHIFT2, R.id.buttonCE,

            R.id.buttonSHIFT,
            R.id.buttonQ, R.id.buttonW, R.id.buttonE, R.id.buttonR, R.id.buttonT,
            R.id.buttonY, R.id.buttonU, R.id.buttonI, R.id.buttonO, R.id.buttonP,
            R.id.buttonDEF,
            R.id.buttonA, R.id.buttonS, R.id.buttonD, R.id.buttonF, R.id.buttonG,
            R.id.buttonH, R.id.buttonJ, R.id.buttonK, R.id.buttonL, R.id.buttonEQ,
            R.id.buttonKANA,
            R.id.buttonZ, R.id.buttonX, R.id.buttonC, R.id.buttonV, R.id.buttonB,
            R.id.buttonN, R.id.buttonM, R.id.buttonSPC, R.id.buttonENTER,

            R.id.button7, R.id.button8, R.id.button9, R.id.buttonK1, R.id.buttonK2,
            R.id.button4, R.id.button5, R.id.button6, R.id.buttonDIV, R.id.buttonCLN,
            R.id.button1, R.id.button2, R.id.button3, R.id.buttonMLT, R.id.buttonSCLN,
            R.id.button0, R.id.buttonDOT, R.id.buttonPLS, R.id.buttonMINUS, R.id.buttonCOMMA

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        activityId = 1360;
        setContentView(R.layout.activity_main_1360);

        SurfaceView sv = (SurfaceView) findViewById(R.id.surfaceView);
        ml = new MainLoop1360(this, sv);

        // Buttonインスタンスの取得
        // ButtonインスタンスのリスナーをこのActivityクラスそのものにする
        for (int i = 0; i < mBtnResIds.length; i++) {
            //Log.w("LOG", "i="+i);
            findViewById(mBtnResIds[i]).setOnClickListener(this);
        }
        // ボタンの枠表示を切り替える
        if (debug_info) {
            changeButtonFrame(mBtnResIds, true);
        } else {
            changeButtonFrame(mBtnResIds, false);
        }

        // キーボード作成
        kb = new KeyBoard1350();

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
                "\0", "\\YEAR", "\\MONTH", "\\DAY", "\\YEN", "\0︎", "\0︎", "\0︎", "\0︎", "\\BX", "\\INS", "\\PI", "\\SQR", "\0", "\0", "\0"
        };

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        stretchItemSize((GridLayout)findViewById(R.id.keyArea), (ImageView)findViewById(R.id.keyAreaImageView), 41);
        stretchItemSize((GridLayout)findViewById(R.id.tenKeyArea), (ImageView)findViewById(R.id.tenkey));

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

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

}
