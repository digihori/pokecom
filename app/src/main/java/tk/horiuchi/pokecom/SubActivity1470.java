package tk.horiuchi.pokecom;

import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;


public class SubActivity1470 extends SubActivityBase {

    private final int BASIC_TEXT_START  = 0x8030;   // 32k
    private final int BASIC_START_ADR_L = 0xffd7;
    private final int BASIC_START_ADR_H = 0xffd8;
    private final int BASIC_END_ADR_L   = 0xffd9;
    private final int BASIC_END_ADR_H   = 0xffda;

    private int[] mBtnResIds = {
            R.id.buttonCAL, R.id.buttonBASIC, R.id.buttonBRK, R.id.buttonKANA, R.id.buttonDAK,R.id.buttonCHO,
            R.id.buttonDA,R.id.buttonUA,R.id.buttonLA,R.id.buttonRA,R.id.buttonSHIFT,
            R.id.button7, R.id.button8, R.id.button9, R.id.buttonDIV, R.id.buttonDEL,

            R.id.buttonSHIFT2,
            R.id.buttonQ, R.id.buttonW, R.id.buttonE, R.id.buttonR, R.id.buttonT,
            R.id.buttonY, R.id.buttonU, R.id.buttonI, R.id.buttonO, R.id.buttonP,
            R.id.button4, R.id.button5, R.id.button6, R.id.buttonMLT, R.id.buttonBS,

            R.id.buttonDEF,
            R.id.buttonA, R.id.buttonS, R.id.buttonD, R.id.buttonF, R.id.buttonG,
            R.id.buttonH, R.id.buttonJ, R.id.buttonK, R.id.buttonL, R.id.buttonCOMMA,
            R.id.button1, R.id.button2, R.id.button3, R.id.buttonMINUS, R.id.buttonINS,

            R.id.buttonSML,
            R.id.buttonZ, R.id.buttonX, R.id.buttonC, R.id.buttonV, R.id.buttonB,
            R.id.buttonN, R.id.buttonM, R.id.buttonSPC, R.id.buttonENTER,
            R.id.button0, R.id.buttonPM, R.id.buttonDOT, R.id.buttonPLS, R.id.buttonEQ,

            R.id.buttonSIN, R.id.buttonCOS, R.id.buttonTAN, R.id.buttonCE,
            R.id.buttonHEX, R.id.buttonDEG, R.id.buttonLN, R.id.buttonLOG,
            R.id.buttonEXP, R.id.buttonPOW, R.id.buttonROOT, R.id.buttonSQU,
            R.id.buttonHYP, R.id.buttonREC, R.id.buttonK1, R.id.buttonK2
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        activityId = 1470;
        setContentView(R.layout.activity_main_1470);


        SurfaceView sv = (SurfaceView) findViewById(R.id.surfaceView);
        ml = new MainLoop1470(this, sv);

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
        kb = new KeyBoard1470();

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
                "RUN", "NEW", "CONT", "PASS", "LIST", "LLIST", "CLOAD", "MERGE", "LOAD", "RENUM", "AUTO", "DELETE", "FILES", "INIT", "CONVERT", "\0",
                "CSAVE", "OPEN", "CLOSE", "SAVE", "CONSOLE", "RANDOM", "DEGREE", "RADIAN", "GRAD", "BEEP", "WAIT", "GOTO", "TRON", "TROFF", "CLEAR", "USING",
                "DIM", "CALL", "POKE", "GPRINT", "\0", "\0", "BASIC", "TEXT", "\0", "\0", "ERASE", "LFILES", "KILL", "COPY", "NAME", "SET",
                "LTEXT", "GRAPH", "LF", "CSIZE", "COLOR", "\0", "DEFDBL", "DEFSNG", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "CLS", "CURSOR", "TO", "STEP", "THEN", "ON", "IF", "FOR", "LET", "REM", "END", "NEXT", "STOP", "READ", "DATA", "PAUSE",
                "PRINT", "INPUT", "GOSUB", "AREAD", "LPRINT", "RETURN", "RESTORE", "CHAIN", "\0", "\0", "LLINE", "RLINE", "GLCURSOR", "SORGN", "CROTATE", "CIRCLE",
                "PAINT", "OUTPUT", "APPEND", "AS", "ARUN", "AUTOGOTO", "\0", "\0", "ERROR", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "MDF", "REC", "POL", "ROT", "DECI", "HEX", "TEN", "RCP", "SQU", "CUR", "HSN", "HCS", "HTN", "AHS", "AHC", "AHT",
                "FACT", "LN", "LOG", "EXP", "SQR", "SIN", "COS", "TAN", "INT", "ABS", "SGN", "DEG", "DMS", "ASN", "ACS", "ATN",
                "RND", "AND", "OR", "NOT", "PEEK", "XOR", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "PI", "MEM",
                "EOF", "DSKF", "LOF", "LOC", "\0", "\0", "NCR", "NRR", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "ERN", "ERL", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "ASC", "VAL", "LEN", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "OPEN$", "INKEY$", "MID$", "LEFT$", "RIGHT$", "\0", "\0", "\0",
                "CHR$", "STR$", "HEX$", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0"
        };

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        stretchItemSize((GridLayout)findViewById(R.id.keyArea), (ImageView)findViewById(R.id.keyAreaImageView), 57);
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

    @Override
    protected int[] bas2code(int[] source) {
        int[] dest = new int[0x8000];
        int[] ret;
        int len = source.length;
        if (len == 0) {
            ret = new int[1];
            ret[0] = 0;
            return ret;
        }

        int r = 0, w = 0, c = 0;
        String str = "";
        dest[w++] = 0xff; // プログラム開始
        String[] talken;

        while (r < len) {
            //Log.w("LOG", String.format("--- 行区切り ---"));
            // 1行読み込み
            str = "";
            while ((c = source[r++]) != '\n') {
                if (c == '\r') continue;    // \rは読み捨てる
                str += String.format("%c", c);
                if (r >= len) break;
            }
            //Log.w("LOG", String.format("%s", str));

            talken = split(str);

            // 一つ目のトークンは必ず行番号
            int line_num = Integer.parseInt(talken[0]);
            dest[w++] = hibyte(line_num);
            dest[w++] = lobyte(line_num);
            if (line_num == 0) break;   // パースに失敗したらこの行はスキップする

            int l = w++;
            int ll = 0;

            // 次のトークンからはコマンド
            boolean flg_goto = false;
            for (int i = 1; i < talken.length; i++) {
                if (i == 1 && talken[i].equals(":")) continue;  // 行番号の次がコロンの場合は読み捨て
                int temp = cmdname2code(talken[i]);
                if (temp != 0) {
                    dest[w++] = 0xfe;   // 拡張コード
                    dest[w++] = temp;
                    //Log.w("LOG", String.format("%02x", temp));
                    ll += 2;
                    if (talken[i].equals("GOTO") || talken[i].equals("GOSUB")) {
                        flg_goto = true;
                    }

                } else {
                    int n = talken[i].length();
                    if (flg_goto) {
                        flg_goto = false;
                        try {
                            line_num = Integer.parseInt(talken[i]);
                            dest[w++] = 0x1f;   // ラインナンバー識別コード
                            dest[w++] = hibyte(line_num);
                            dest[w++] = lobyte(line_num);
                            ll += 3;
                        } catch (NumberFormatException e) {
                            i--;
                            continue;
                        }
                    } else {
                        for (int j = 0; j < n; j++) {
                            dest[w++] = (int) (talken[i].charAt(j));
                            //Log.w("LOG", String.format("%s", talken[i].charAt(j)));
                            ll++;
                        }
                    }
                }
            }
            dest[l] = ll + 1;   // 行の長さ(区切りコードも含まれるので+1)

            dest[w++] = 0x0d;   // 行区切り
            //Log.w("LOG", String.format("separate"));
        }
        dest[w++] = 0xff;   // プログラム終了

        // 戻り値用に配列を作り直す
        ret = new int[w];
        for (int i = 0; i < w; i++) {
            ret[i] = dest[i];
        }
        return ret;
    }

    @Override
    protected int[] code2bas(int[] source) {
        int[] dest = new int[0x8000];
        int[] ret;

        int len = source.length;
        //Log.w("LOG", "len"+len);
        if (len == 0) {
            ret = new int[1];
            ret[0] = 0;
            return ret;
        }

        //for (int i = 0; i < source.length; i++) {
        //    Log.w("LOG", String.format("%02x", source[i]));
        //}

        int r = 0, w = 0, c = 0;
        //String str = "";

        if (source[r++] != 0xff) {
            ret = new int[1];
            ret[0] = 0;
            return ret;
        }

        boolean cmd_exist = false;
        boolean non_cmd = false;
        while (r < len) {
            //Log.w("LOG", String.format("--- 業区切り --- r=%d len=%d", r, len));
            // 最初の2byteは必ず行番号
            int line_num = hilo(source[r++], source[r++]);
            //Log.w("LOG", "line_num="+line_num);
            for (int i = 0; i < (Integer.toString(line_num)).length(); i++) {
                dest[w++] = (Integer.toString(line_num)).charAt(i);
            }
            // 区切りのスペース
            dest[w++] = ' ';

            // 以降はコマンド
            String cmd;
            String tmp;
            r++;    // 1byte飛ばす
            cmd_exist = false;
            non_cmd = false;
            while ((c = source[r++]) != 0x0d) {
                //Log.w("LOG", String.format("c=%02x", c));
                cmd = "";
                if (c == 0xfe) {
                    if (r >= source.length) break;
                    c = source[r++];
                    if (cmd_exist || non_cmd) {
                        dest[w++] = ' ';
                    }
                    cmd_exist = false;
                    non_cmd = false;

                    cmd = cmd_tbl[c];
                    //Log.w("LOG", String.format("cmd=%s", cmd));
                    for (int j = 0; j < cmd.length(); j++) {
                        dest[w++] = cmd.charAt(j);
                    }

                    if (cmd.equals("GOTO") || cmd.equals("GOSUB")) {
                        if (r+2 >= source.length) break;
                        if (source[r] == 0x1f) {
                            dest[w++] = ' ';
                            r++;
                            line_num = hilo(source[r++], source[r++]);
                            tmp = String.format("%d", line_num);
                            for (int j = 0; j < tmp.length(); j++) {
                                dest[w++] = tmp.charAt(j);
                            }
                        }
                    }
                    cmd_exist = true;
                } else {
                    if (cmd_exist) {
                        dest[w++] = ' ';
                        cmd_exist = false;
                    }
                    dest[w++] = c;

                    non_cmd = (c != ' ') ? true : false;
                }
                if (r >= source.length) break;
            }
            dest[w++] = '\n';   // 改行コード

            if (source[r] == 0xff) {
                // 次が終了コードの場合はここで終わり
                break;
            }
        }

        // 戻り値用に配列を作り直す
        ret = new int[w];
        for (int i = 0; i < w; i++) {
            ret[i] = dest[i];
        }
        return ret;
    }

}
