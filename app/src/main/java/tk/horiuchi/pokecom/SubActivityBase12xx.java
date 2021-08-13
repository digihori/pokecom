package tk.horiuchi.pokecom;


import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubActivityBase12xx extends SubActivityBase {

    /*
    @Override
    protected String replaceSpecialChar(String str) {
        Log.w("split-in", str);
        int[] ii = {0x19, 0x1a, 0x4b, 0x4c};
        for (int i = 0; i < ii.length; i++) {
            String s = cmd_tbl[ii[i]];
            if (s.charAt(0) != '\\') continue;

            String regex = "\\"+s;
            Log.w("split", String.format("code=%02x cmd='%s'", ii[i], regex));
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(str);
            if (m.find()) {
                str = str.replaceAll(regex, String.valueOf((char)(0xf0+i)));
                // 特殊文字は仮の文字コードに変換しておく
                // 0x19 -> 0xf0
                // 0x1a -> 0xf1
                // 0x4b -> 0xf2
                // 0x4c -> 0xf3
            }
        }
        Log.w("split-out", str);
        return str;
    }

    // コマンド文字列を内部コードに変換する
    @Override
    protected int cmdname2code(String cmdname) {
        int x = cmdname.charAt(0);
        switch (x) {
            case 0xf0: return 0x19;
            case 0xf1: return 0x1a;
            case 0xf2: return 0x4b;
            case 0xf3: return 0x4c;
            default:
                break;
        }

        // 特殊コード以外はスーパークラスへ渡す
        return super.cmdname2code(cmdname);
    }
    */
    @Override
    protected String replaceSpecialChar(String str) {
        // エスケープ文字'\'そのものにしたいところを一旦0x6fに変換しておく
        str = str.replace("\\\\", String.valueOf((char)0x6f));

        int cmd[] = { 0x19/*\PI*/, 0x1a/*\SQR*/, 0x4b/*\EX*/, 0x4c/*\BX*/};
        for (int i = 0; i < 4; i++) {
            String s = cmd_tbl[cmd[i]];
            if (s.charAt(0) != '\\') continue;

            String regex = String.valueOf('\\')+s;
            Log.w("replaceSpecialChar", String.format("code=%02x cmd='%s' regex='%s'", cmd[i], s, regex));
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(str);
            if (m.find()) {
                str = str.replaceAll(regex, String.valueOf((char)(cmd[i] | 0xf0)));
            }
        }
        return str;
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

        boolean flg = false;
        while (r < len) {
            //Log.w("LOG", String.format("--- 行区切り ---"));
            // 1行読み込み
            str = "";
            if (source[r] == '\n') {
                // 空行は読み捨てる
                Log.w("load", "blank line!");
                r++;
                continue;
            }
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
            if (line_num == 0) break;   // パースに失敗したらこの行はスキップする
            int num100 = line_num / 100;
            int num10  = (line_num - num100 * 100) / 10;
            int num1   = line_num - num100 * 100 - num10 * 10;
            dest[w++] = 0xe0 | (num100 & 0x0f);
            dest[w++] = num10 << 4 | num1;

            //int ll = 0;

            // 次のトークンからはコマンド
            int temp;
            for (int i = 1; i < token.length; i++) {
                if (i == 1 && token[i].equals(":")) continue;  // 行番号の次がコロンの場合は読み捨て

                if (i + 1 < token.length &&
                        (token[i].equals("<") && token[i+1].equals("=") ||
                         token[i].equals(">") && token[i+1].equals("=") ||
                         token[i].equals("<") && token[i+1].equals(">")) ) {
                    temp = cmdname2code(token[i]+token[i+1]);
                    i++;
                } else {
                    temp = cmdname2code(token[i]);
                }
                if (temp != 0) {
                    dest[w++] = temp;
                    //Log.w("LOG", String.format("%02x", temp));
                    //ll++;

                } else {
                    int n = token[i].length();
                    char[] ch = new char[1];
                    for (int j = 0; j < n; j++) {
                        ch[0] = token[i].charAt(j);
                        if (ch[0] >= 0xf0 || ch[0] == 0x6f) {
                            // 特殊記号は一旦別のコードにしているのでここで戻す
                            switch (ch[0]) {
                                case 0x6f: dest[w++] = 0x17; break; // \
                                case 0xf9: dest[w++] = 0x19; break; // \PI
                                case 0xfa: dest[w++] = 0x1a; break; // \SQR
                                case 0xfb: dest[w++] = 0x4b; break; // \EXP
                                case 0xfc: dest[w++] = 0x4c; break; // \BX
                                default:
                                    break;
                            }
                            //Log.w("LOG", String.format("%02x", dest[w-1]));
                        } else {
                            String tempStr = String.valueOf(ch);
                            temp = cmdname2code(tempStr);
                            if (temp != 0) dest[w++] = temp;
                            //ll++;
                            // もうちょっといいやり方ないのかな。。
                            //Log.w("LOG", String.format("%02x='%c'", dest[w-1], dest[w-1]));
                        }
                    }
                }
            }

            dest[w++] = 0x00;   // 行区切り
            //Log.w("LOG", String.format("separate"));
        }
        dest[w++] = 0xff;   // プログラム終了
        //dest[w++] = '\0';   // 一応最後にヌルコード

        // 戻り値用に配列を作り直す
        ret = new int[w];
        for (int i = 0; i < w; i++) {
            ret[i] = dest[i];
        }
        return ret;
    }

    protected int getLineNum(int hi, int lo) {
        if ((hi & 0xe0) != 0xe0) return 0;
        return ((hi & 0x0f) * 100 + ((lo & 0xf0) >> 4) * 10 + (lo & 0x0f));
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
            int line_num = getLineNum(source[r++], source[r++]);
            //Log.w("LOG", "line_num="+line_num);
            for (int i = 0; i < (Integer.toString(line_num)).length(); i++) {
                dest[w++] = (Integer.toString(line_num)).charAt(i);
            }
            // 区切りのスペース
            dest[w++] = ' ';

            // 以降はコマンド
            String cmd;
            //r++;    // 1byte飛ばす
            cmd_exist = false;
            non_cmd = false;
            while ((c = source[r++]) != 0x00) {
                //Log.w("LOG", String.format("c=%02x", c));
                cmd = "";
                if (0x7d <= c && c <= 0xdf || c == 0x19 || c == 0x1a || c == 0x4b || c == 0x4c) {
                    if (0x7d <= c && c <= 0xdf && non_cmd || cmd_exist) {
                        dest[w++] = ' ';
                    }
                    cmd_exist = false;
                    non_cmd = false;

                    cmd = cmd_tbl[c];
                    //Log.w("LOG", String.format("cmd=%s", cmd));
                    for (int j = 0; j < cmd.length(); j++) {
                        dest[w++] = cmd.charAt(j);
                    }
                    if (0x7d <= c && c <= 0xdf) {
                        cmd_exist = true;
                    }
                } else {
                    if (cmd_exist) {
                        dest[w++] = ' ';
                        cmd_exist = false;
                    }
                    if (0x11 <= c && c <= 0x1f || 0x30 <= c && c<= 0x39 ||
                            0x40 <= c && c <= 0x4f || 0x50 <= c && c <= 0x6a) {
                        dest[w++] = cmd_tbl[c].charAt(0);
                        if (c == 0x17) {    // '\'は'\\'にする
                            dest[w++] = cmd_tbl[c].charAt(0);
                        }
                    }

                    non_cmd = (c != ' ') ? true : false;
                }
                if (r >= source.length) break;
            }
            dest[w++] = '\n';   // 区切り// コード

            if (source[r] == 0xff) {
                // 次が終了コードの場合はここで終わり
                break;
            }
        }
        //dest[w++] = '\0';   // 最後にエンドマーク

        // 戻り値用に配列を作り直す
        ret = new int[w];
        for (int i = 0; i < w; i++) {
            ret[i] = dest[i];
        }
        return ret;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
