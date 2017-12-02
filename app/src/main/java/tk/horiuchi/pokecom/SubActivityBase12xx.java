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

        boolean flg = false;
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
            if (line_num == 0) break;   // パースに失敗したらこの行はスキップする
            int num100 = line_num / 100;
            int num10  = (line_num - num100 * 100) / 10;
            int num1   = line_num - num100 * 100 - num10 * 10;
            dest[w++] = 0xe0 | (num100 & 0x0f);
            dest[w++] = num10 << 4 | num1;

            //int ll = 0;

            // 次のトークンからはコマンド
            int temp;
            for (int i = 1; i < talken.length; i++) {
                if (i == 1 && talken[i].equals(":")) continue;  // 行番号の次がコロンの場合は読み捨て

                if (i + 1 < talken.length &&
                        (talken[i].equals("<") && talken[i+1].equals("=") ||
                         talken[i].equals(">") && talken[i+1].equals("=") ||
                         talken[i].equals("<") && talken[i+1].equals(">")) ) {
                    temp = cmdname2code(talken[i]+talken[i+1]);
                    i++;
                } else {
                    temp = cmdname2code(talken[i]);
                }
                if (temp != 0) {
                    dest[w++] = temp;
                    //Log.w("LOG", String.format("%02x", temp));
                    //ll++;

                } else {
                    int n = talken[i].length();
                    char[] ch = new char[1];
                    for (int j = 0; j < n; j++) {
                        ch[0] = talken[i].charAt(j);
                        String tempStr = String.valueOf(ch);
                        dest[w++] = cmdname2code(tempStr);
                        //ll++;
                        // もうちょっといいやり方ないのかな。。
                        //Log.w("LOG", String.format("%02x='%c'", dest[w-1], dest[w-1]));
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
                if (0x7d <= c && c <= 0xdf) {
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
                    cmd_exist = true;
                } else {
                    if (cmd_exist) {
                        dest[w++] = ' ';
                        cmd_exist = false;
                    }
                    if (0x12 <= c && c <= 0x1f || 0x30 <= c && c<= 0x39 ||
                            0x40 <= c && c <= 0x4a || 0x50 <= c && c <= 0x6a) {
                        dest[w++] = cmd_tbl[c].charAt(0);
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
