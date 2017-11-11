package tk.horiuchi.pokecom;

import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;


/**
 * Created by yoshimine on 2017/07/29.
 */

public class SubActivity1245 extends SubActivityBase12xx {
    public static boolean prog_mode;

    private final int BASIC_TEXT_START  = 0xc000;
    private final int BASIC_START_ADR_L = 0xc6e1;
    private final int BASIC_START_ADR_H = 0xc6e2;
    private final int BASIC_END_ADR_L   = 0xc6e3;
    private final int BASIC_END_ADR_H   = 0xc6e4;


    private int[] mBtnResIds = {
            R.id.buttonDEF, R.id.buttonSHIFT,
            R.id.buttonDA, R.id.buttonUA, R.id.buttonLA, R.id.buttonRA, R.id.buttonBRK,
            R.id.button7, R.id.button8, R.id.button9, R.id.buttonCE,

            R.id.buttonQ, R.id.buttonW, R.id.buttonE, R.id.buttonR, R.id.buttonT,
            R.id.buttonY, R.id.buttonU, R.id.buttonI, R.id.buttonO, R.id.buttonP,
            R.id.button4, R.id.button5, R.id.button6, R.id.buttonDIV,

            R.id.buttonA, R.id.buttonS, R.id.buttonD, R.id.buttonF, R.id.buttonG,
            R.id.buttonH, R.id.buttonJ, R.id.buttonK, R.id.buttonL, R.id.buttonEQ,
            R.id.button1, R.id.button2, R.id.button3, R.id.buttonMLT,

            R.id.buttonZ, R.id.buttonX, R.id.buttonC, R.id.buttonV, R.id.buttonB,
            R.id.buttonN, R.id.buttonM, R.id.buttonSPC, R.id.buttonENTER,
            R.id.button0, R.id.buttonDOT, R.id.buttonPLS, R.id.buttonMINUS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        activityId = 1245;
        setContentView(R.layout.activity_main_1245);

        //Log.w("LOG", "aaa");
        //System.out.printf("aaaaa\n");

        SurfaceView sv = (SurfaceView) findViewById(R.id.surfaceView);
        ml = new MainLoop1245(this, sv);

        // Buttonインスタンスの取得
        // ButtonインスタンスのリスナーをこのActivityクラスそのものにする
        for (int i = 0; i < mBtnResIds.length; i++) {
            findViewById(mBtnResIds[i]).setOnClickListener(this);
        }
        // ボタンの枠表示を切り替える
        if (debug_info) {
            changeButtonFrame(mBtnResIds, true);
        } else {
            changeButtonFrame(mBtnResIds, false);
        }

        // ToggleButtonの状態取り込み
        ToggleButton tb = (ToggleButton)findViewById(R.id.toggleButton);
        prog_mode = false;
        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //トグルキーが変更された際に呼び出される
                //Log.d("ToggleButton","call OnCheckdChangeListener");
                prog_mode = isChecked;

                if (vibrate_enable) {
                    vib.vibrate(10);
                }
            }
        });


        // キーボード作成
        kb = new KeyBoard1245();

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
                "\0", "\0", "\"", "?", "!", "#", "%", "\\", "$", "π", "√", ",", ";", ":", "@", "&",
                "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "(", ")", ">", "<", "=", "+", "-", "*", "/", "^", "\0", "\0", "\0", "\0", "\0", "\0",
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "\0", "\0", "\0", "\0", "\0",
                " ", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
                "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "\0", "\0", "\0", "\0", "\0",
                "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "ASC", "VAL", "LEN",
                "\0", "AND", ">=", "<=", "<>", "OR", "NOT", "SQR", "CHR$", "COM$", "INKEY$", "STR$", "LEFT$", "RIGHT$", "MID$", "\0",
                "TO", "STEP", "THEN", "RANDOM", "\0", "WAIT", "ERROR", "\0", "\0", "KEY", "\0", "SETCOM", "\0", "\0", "ROM", "LPRINT",
                "SIN", "COS", "TAN", "ASN", "ACS", "ATN", "EXP", "LN", "LOG", "INT", "ABS", "SGN", "DEG", "DMS", "RND", "PEEK",
                "RUN", "NEW", "MEM", "LIST", "CONT", "DEBUG", "CSAVE", "CLOAD", "MARGE", "TRON", "TROFF", "PASS", "LLIST", "PI", "OUTSTAT", "INSTAT",
                "GRAD", "PRINT", "INPUT", "RADIAN", "DEGREE", "CLEAR", "\0", "\0", "\0", "CALL", "DIM", "DATA", "ON", "OFF", "POKE", "READ",
                "IF", "FOR", "LET", "REM", "END", "NEXT", "STOP", "GOTO", "GOSUB", "CHAIN", "PAUSE", "BEEP", "AREAD", "USING", "RETURN", "RESTORE",
                "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0"
        };

    }

    @Override
    public void modeInit() {
        ((ToggleButton)findViewById(R.id.toggleButton)).setChecked(false);
        prog_mode = false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        stretchItemSize((GridLayout)findViewById(R.id.keyArea), (ImageView)findViewById(R.id.keyAreaImageView), 47);

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

    }

    @Override
    public void onResume() {
        super.onResume();

        // トグルボタンの状態を合わせる
        ((ToggleButton)findViewById(R.id.toggleButton)).setChecked(prog_mode);

        // ボタン表示枠の更新
        if (debug_info) {
            changeButtonFrame(mBtnResIds, true);
        } else {
            changeButtonFrame(mBtnResIds, false);
        }
    }


}
