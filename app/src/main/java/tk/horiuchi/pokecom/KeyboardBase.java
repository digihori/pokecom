package tk.horiuchi.pokecom;

import android.util.Log;

import static tk.horiuchi.pokecom.Sc61860Base.kon;
import static tk.horiuchi.pokecom.Sc61860Base.kon_cnt;

/**
 * Created by yoshimine on 2017/07/03.
 */

public class KeyboardBase {
    private static int keyBuf;
    public static int keyBufCnt = 0;


    // 1401 key matrix
    protected final int KNUL = 0;
    protected int KEYNM;
    // key matrix 実体は継承クラスで定義する
    public static int keym[];
    protected int scandef[];

    public KeyboardBase() {
        keyBuf = 0;
    }

    public void keyclear() {
        for (int i = 0; i < KEYNM; i++) {
            keym[i] = 0;
        }
        keyBuf = 0;
    }

    public synchronized void setBuf(int c) {
        keyBuf = c;
        keyBufCnt = 10000;
    }

    public synchronized int getBuf() {
        int c = keyBuf;
        keyBuf = 0;
        return(c);
    }

    public void keyscan(int keychar) {
        int i;
        int j;

        kon = 0;
        for (i = 0; i < KEYNM; i++) {
            keym[i] = 0;
        }
        for (i = 0; i < KEYNM; i++) {
            for (j = 0; j < 8; j++) {
                if (keychar == scandef[i * 8 + j]) {
                    keym[i] = (1 << j);
                    Log.w("keyscan", "set key matrix --- keym["+i+"]="+keym[i]);
                    break;
                }
            }
            if (keym[i] != 0) {
                break;
            }
        }
        //if (keychar == R.id.buttonBRK) {
        //    kon_cnt = 10;
        //}
    }
}
