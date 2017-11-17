package tk.horiuchi.pokecom;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;


/**
 * Created by yoshimine on 2017/07/29.
 */

public class SubActivity1251 extends SubActivityBase12xx {
    public static int prog_mode;

    private final int BASIC_TEXT_START  = 0xb800;
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
        activityId = 1251;
        setContentView(R.layout.activity_main_1251);

        //Log.w("LOG", "aaa");
        //System.out.printf("aaaaa\n");

        SurfaceView sv = (SurfaceView) findViewById(R.id.surfaceView);
        ml = new MainLoop1251(this, sv);

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

        // モードスイッチの状態設定と取り込み
        ((RadioButton)findViewById(R.id.btnRun)).setChecked(true);  // 初期値
        prog_mode = 0;
        RadioGroup rg = (RadioGroup)findViewById(R.id.radioGroupMode);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            // チェック状態変更時に呼び出されるメソッド
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // チェック状態時の処理を記述
                // チェックされたラジオボタンオブジェクトを取得
                //RadioButton radioButton = (RadioButton)findViewById(checkedId);
                switch (checkedId) {
                    default:
                    case R.id.btnRun:
                        prog_mode = 0;
                        break;
                    case R.id.btnPro:
                        prog_mode = 1;
                        break;
                    case R.id.btnRsv:
                        prog_mode = 2;
                        break;
                }
                Log.w("SubActivity1251", "mode change ->"+prog_mode);

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
                "\0", "\0", "\"", "?", "!", "#", "%", "\\", "$", "\\PI", "\\SQR", ",", ";", ":", "@", "&",
                "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "(", ")", ">", "<", "=", "+", "-", "*", "/", "^", "\0", "\0", "\0", "\0", "\0", "\0",
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "\\EXP", "\\BX", "~", "\0", "\0",
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
        ((RadioButton)findViewById(R.id.btnRun)).setChecked(true);  // 初期値
        prog_mode = 0;
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

        // モードスイッチの状態を合わせる
        RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroupMode);
        switch (prog_mode) {
            default:
            case 0:
                rg.check(R.id.btnRun);
                break;
            case 1:
                rg.check(R.id.btnPro);
                break;
            case 2:
                rg.check(R.id.btnRsv);
                break;
        }

        // ボタン表示枠の更新
        if (debug_info) {
            changeButtonFrame(mBtnResIds, true);
        } else {
            changeButtonFrame(mBtnResIds, false);
        }
    }

}
