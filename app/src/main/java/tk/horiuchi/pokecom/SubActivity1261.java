package tk.horiuchi.pokecom;

import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


/**
 * Created by yoshimine on 2017/07/29.
 */

public class SubActivity1261 extends SubActivityBase {
    public static int prog_mode;

    private final int BASIC_TEXT_START  = 0x4080;
    private final int BASIC_START_ADR_L = 0x66e1;
    private final int BASIC_START_ADR_H = 0x66e2;
    private final int BASIC_END_ADR_L   = 0x66e3;
    private final int BASIC_END_ADR_H   = 0x66e4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBtnResIds = new int[] {
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

        instance = this;
        activityId = 1261;
        setContentView(R.layout.activity_main_1261);

        //Log.w("LOG", "aaa");
        //System.out.printf("aaaaa\n");

        SurfaceView sv = (SurfaceView) findViewById(R.id.surfaceView);
        ml = new MainLoop1261(this, sv);

        // キーボード作成
        kb = new KeyBoard1245();

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
                Log.w("SubActivity1261", "mode change ->"+prog_mode);

                if (vibrate_enable) {
                    vib.vibrate(10);
                }

            }
        });

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
                "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "\0", "LN", "LOG", "EXP", "SQR", "SIN", "COS", "TAN", "INT", "ABS", "SGN", "DEG", "DMS", "ASN", "ACS", "ATN",
                "RND", "AND", "OR", "NOT", "ASC", "VAL", "LEN", "PEEK", "CHR$", "STR$", "MID$", "LEFT$", "RIGHT$", "INKEY$", "PI", "MEM",
                "RUN", "NEW", "CONT", "PASS", "LIST", "LLIST", "CSAVE", "CLOAD", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "RANDOM", "DEGREE", "RADIAN", "GRAD", "BEEP", "WAIT", "GOTO", "TRON", "TROFF", "CLEAR", "USING", "DIM", "CALL", "POKE", "CLS", "CURSOR",
                "TO", "STEP", "THEN", "ON", "IF", "FOR", "LET", "REM", "END", "NEXT", "STOP", "READ", "DATA", "PAUSE", "PRINT", "INPUT",
                "GOSUB", "AREAD", "LPRINT", "RETURN", "RESTORE", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\\BX", "\\INS", "\\PI", "\\SQR", "\0", "\0", "\0"
        };

        kana = true;

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

    //@Override
    //public void onClick(View v) {
    //    super.onClick(v);
    //}

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
                    RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroupMode);
                    prog_mode += 1;
                    if (prog_mode > 2) prog_mode = 0;
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
                }
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

}
