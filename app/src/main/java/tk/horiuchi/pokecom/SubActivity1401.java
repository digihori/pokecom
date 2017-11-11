package tk.horiuchi.pokecom;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;

import static tk.horiuchi.pokecom.Sc61860Base.mainram;

public class SubActivity1401 extends SubActivityBase {
    //protected KeyBoard1401 kb = null;
    //private int manhole = 0;
    private final int BASIC_TEXT_START  = 0x3800;
    private final int BASIC_START_ADR_L = 0x46e1;
    private final int BASIC_START_ADR_H = 0x46e2;
    private final int BASIC_END_ADR_L   = 0x46e3;
    private final int BASIC_END_ADR_H   = 0x46e4;

    private int[] mBtnResIds = {
            R.id.buttonCAL, R.id.buttonBASIC, R.id.buttonBRK, R.id.buttonDEF,
            R.id.buttonDA,R.id.buttonUA,R.id.buttonLA,R.id.buttonRA,R.id.buttonSHIFT,
            R.id.button7, R.id.button8, R.id.button9, R.id.buttonDIV, R.id.buttonXM,
            R.id.buttonQ, R.id.buttonW, R.id.buttonE, R.id.buttonR, R.id.buttonT,
            R.id.buttonY, R.id.buttonU, R.id.buttonI, R.id.buttonO, R.id.buttonP,
            R.id.button4, R.id.button5, R.id.button6, R.id.buttonMLT, R.id.buttonRM,
            R.id.buttonA, R.id.buttonS, R.id.buttonD, R.id.buttonF, R.id.buttonG,
            R.id.buttonH, R.id.buttonJ, R.id.buttonK, R.id.buttonL, R.id.buttonCOMMA,
            R.id.button1, R.id.button2, R.id.button3, R.id.buttonMINUS, R.id.buttonMP,
            R.id.buttonZ, R.id.buttonX, R.id.buttonC, R.id.buttonV, R.id.buttonB,
            R.id.buttonN, R.id.buttonM, R.id.buttonSPC, R.id.buttonENTER,
            R.id.button0, R.id.buttonPM, R.id.buttonDOT, R.id.buttonPLS, R.id.buttonEQ,

            R.id.buttonHYP, R.id.buttonSIN, R.id.buttonCOS, R.id.buttonTAN, R.id.buttonFE, R.id.buttonCE,
            R.id.buttonHEX, R.id.buttonDEG, R.id.buttonLN, R.id.buttonLOG, R.id.buttonREC, R.id.buttonUD,
            R.id.buttonEXP, R.id.buttonPOW, R.id.buttonROOT, R.id.buttonSQU, R.id.buttonK1, R.id.buttonK2
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        activityId = 1401;
        setContentView(R.layout.activity_main_1401);

        SurfaceView sv = (SurfaceView) findViewById(R.id.surfaceView);
        ml = new MainLoop1401(this, sv);
        //if (savedInstanceState != null) {
        //    ml.sc.halt();
        //    Sc61860params sc_param = (Sc61860params) (savedInstanceState.getSerializable("sc61860"));
        //    ml.sc.restoreParam(sc_param);
        //    Log.w("SubActivity", "------------ parameter restored!!! ---------");
        //    ml.sc.restart();
        //}

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

        // キーボード作成
        kb = new KeyBoard1401();

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
                "\0", "REC", "POL", "ROT", "DEC", "HEX", "TEN", "RCP", "SQU", "CUR", "HSN", "HCS", "HTN", "AHS", "AHC", "AHT",
                "FAC", "LN", "LOG", "EXP", "SQR", "SIN", "COS", "TAN", "INT", "ABS", "SGN", "DEG", "DMS", "ASN", "ACS", "ATN",
                "RND", "AND", "OR", "NOT", "ASC", "VAL", "LEN", "PEEK", "CHR$", "STR$", "MID$", "LEFT$", "RIGHT$", "INKEY$", "PI", "MEM",
                "RUN", "NEW", "CONT", "PASS", "LIST", "LLIST", "CSAVE", "CLOAD", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "RANDOM", "DEGREE", "RADIAN", "GRAD", "BEEP", "WAIT", "GOTO", "TRON", "TROFF", "CLEAR", "USING", "DIM", "CALL", "POKE", "\0", "\0",
                "TO", "STEP", "THEN", "ON", "IF", "FOR", "LET", "REM", "END", "NEXT", "STOP", "READ", "DATA", "PAUSE", "PRINT", "INPUT",
                "GOSUB", "AREAD", "LPRINT", "RETURN", "RESTORE", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0"
        };

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        stretchItemSize((GridLayout)findViewById(R.id.keyArea), (ImageView)findViewById(R.id.keyAreaImageView), 52);
        stretchItemSize((GridLayout)findViewById(R.id.funcKeyArea), (ImageView)findViewById(R.id.funckey));
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
