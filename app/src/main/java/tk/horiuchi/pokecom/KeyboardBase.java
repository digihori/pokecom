package tk.horiuchi.pokecom;

import android.util.Log;

//import static tk.horiuchi.pokecom.Sc61860Base.kon;
//import static tk.horiuchi.pokecom.Sc61860Base.kon_cnt;
//import static tk.horiuchi.pokecom.SubActivityBase.mBtnResIds;

/**
 * Created by yoshimine on 2017/07/03.
 */

public class KeyboardBase {
    //private static int keyBuf;
    //public static int keyBufCnt = 0;
    //public static int keym[];


    // 1401 key matrix
    protected final int KNUL = 0;
    protected int KEYNM;
    // key matrix 実体は継承クラスで定義する
    protected int scandef[];
    public static int[] mBtnStatus;

    public KeyboardBase() {
        //*keyBuf = 0;
    }

    /*
    public void keyclear() {
        for (int i = 0; i < KEYNM; i++) {
            keym[i] = 0;
        }
        keyBuf = 0;
    }
    */

    /*
    public synchronized void setBuf(int c) {
        keyBuf = c;
        keyBufCnt = 10000;
    }
    */

    /*
    public synchronized int getBuf() {
        int c = keyBuf;
        keyBuf = 0;
        return(c);
    }
    */

    /*
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
    */

    protected int getBtnIdx(int id) {
        for (int idx = 0; idx < scandef.length; idx++) {
            if (id == scandef[idx]) return idx;
        }
        return -1;
    }

    protected void setBtnStatus(int id, boolean sts) {
        int idx = getBtnIdx(id);
        if (idx != -1) {
            int col = idx / 8;
            int row = idx % 8;
            if (sts) {
                mBtnStatus[col] |= 1 << row;
            } else {
                mBtnStatus[col] &= ~(1 << row);
            }
            Log.w("setBtnStatus", String.format("set key matrix --- mBtnStatus[%02x]=%02x", col, mBtnStatus[col]));
        }
    }

    //public int getPressBtnId() {
    //    for (int idx = 0; idx < scandef.length; idx++) {
    //        if (mBtnStatus[idx]) return scandef[idx];
    //    }
    //    return 0;
    //}

}
