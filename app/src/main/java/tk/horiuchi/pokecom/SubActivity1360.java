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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                "RUN", "NEW", "CONT", "PASS", "LIST", "LLIST", "CLOAD", "MERGE", "LOAD", "RENUM", "\0", "DELETE", "FILES", "LNIT", "\0", "\0",
                "CSAVE", "OPEN", "CLOSE", "SAVE", "CONSOLE", "RANDOM", "DEGREE", "RADIAN", "GRAD", "BEEP", "WAIT", "GOTO", "TRON", "TROFF", "CLEAR", "USING",
                "DIM", "CALL", "POKE", "GPRINT", "PSET", "PRESET", "BASIC", "TEXT", "WIDTH", "\0", "ERASE", "LFILES", "KILL", "COPY", "NAME", "SET",
                "LTEXT", "GRAPH", "LF", "CSIZE", "COLOR", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "CLS", "CURSOR", "TO", "STEP", "THEN", "ON", "IF", "FOR", "LET", "REM", "END", "NEXT", "STOP", "READ", "DATA", "PAUSE",
                "PRINT", "INPUT", "GOSUB", "AREAD", "LPRINT", "RETURN", "RESTORE", "CHAIN", "GCURSOR", "LINE", "LLINE", "RLINE", "GLCURSOR", "SORGN", "CROTATE", "CIRCLE",
                "PAINT", "OUTPUT", "APPEND", "AS", "ARUN", "AUTOGOTO", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "MDF", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "\0", "LN", "LOG", "EXP", "SQR", "SIN", "COS", "TAN", "INT", "ABS", "SGN", "DEG", "DMS", "ASN", "ACS", "ATN",
                "RND", "AND", "OR", "NOT", "PEEK", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "POINT", "PI", "MEM",
                "EOF", "DSKF", "LOF", "LOC", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "ASC", "VAL", "LEN", "KLEN", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0",
                "AKCNV$", "KACNV$", "JIS$", "\0", "\0", "\0", "\0", "\0", "OPEN$", "INKEY$", "MID$", "LEFT$", "RIGHT$", "KMID$", "KLEFT$", "KRIGHT$",
                "CHR$", "STR$", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0", "\0"
        };

        kana  = true;
        kana1470 = true;

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

    private String ecode[] = { "\\YEAR", "\\MON", "\\DAY", "\\YEN", "\\SP", "\\HT", "\\DI", "\\CL", "\\BX", "\\INS", "\\PI", "\\SQR"};

    protected String replaceSpecialChar(String str) {
        str = str.replace("\\\\", String.valueOf((char)0xf0));

        for (int i = 0; i <= 11; i++) {
            String regex = String.valueOf('\\')+ecode[i];
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(str);
            if (m.find()) {
                str = str.replaceAll(regex, String.valueOf((char)(0xf1 + i)));
            }
        }
        str = str.replace(String.valueOf((char)0xf0), "\\");
        str = str.replace("\\EX", "E");
        //return str;
        return (kana ? replaceKana(str) : str);
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
        String[] token;

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

            // 先頭の空白を削除する
            str = trimLeft(str);

            token = split(str);

            // 一つ目のトークンは必ず行番号
            int line_num = Integer.parseInt(token[0]);
            dest[w++] = hibyte(line_num);
            dest[w++] = lobyte(line_num);
            if (line_num == 0) break;   // パースに失敗したらこの行はスキップする

            int l = w++;
            int ll = 0;

            // 次のトークンからはコマンド
            boolean flg_goto = false;
            loop: for (int i = 1; i < token.length; i++) {
                if (i == 1 && token[i].equals(":")) continue;  // 行番号の次がコロンの場合は読み捨て
                int temp = cmdname2code(token[i]);
                if (temp != 0) {
                    dest[w++] = 0xfe;   // 拡張コード
                    dest[w++] = temp;
                    //Log.w("LOG", String.format("%02x", temp));
                    ll += 2;
                    if (token[i].equals("GOTO") || token[i].equals("GOSUB")) {
                        flg_goto = true;
                    }

                } else {
                    int n = token[i].length();
                    if (flg_goto) {
                        flg_goto = false;
                        try {
                            line_num = Integer.parseInt(token[i]);
                            dest[w++] = 0x1f;   // ラインナンバー識別コード
                            dest[w++] = hibyte(line_num);
                            dest[w++] = lobyte(line_num);
                            ll += 3;
                        } catch (NumberFormatException e) {
                            i--;
                            continue;
                        }
                    } else {
                        replaceSpecialChar(token[i]);
                        if (token[i].equals("\\EX")) {
                            dest[w++] = 'E';
                            ll++;
                        } else {
                            for (int j = 0; j < n; j++) {
                                dest[w++] = (int) (token[i].charAt(j));
                                //Log.w("LOG", String.format("%s", token[i].charAt(j)));
                                ll++;
                            }
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
                if (kana1470 && 0xa1 <= c && c <= 0xdf) {
                    // カナのコードをUTF-8に変換する
                    if (cmd_exist) {
                        dest[w++] = ' ';
                        cmd_exist = false;
                    }
                    if (c <= 0xbf) {
                        dest[w++] = 0xef;
                        dest[w++] = 0xbd;
                        dest[w++] = c;
                    } else {
                        dest[w++] = 0xef;
                        dest[w++] = 0xbe;
                        dest[w++] = c - 0x40;
                    }
                } else
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

                    if (0xf1 <= c && c <= 0xfc) {
                        Log.w("LOG", String.format("c=%x", c));
                        tmp = ecode[c - 0xf1];
                        for (int j = 0; j < tmp.length(); j++) {
                            dest[w++] = tmp.charAt(j);
                        }
                    } else {
                        dest[w++] = c;
                        if (c == '\\') {
                            dest[w++] = c;
                        }
                    }
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
