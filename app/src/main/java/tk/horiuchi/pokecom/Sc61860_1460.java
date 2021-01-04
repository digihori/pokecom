package tk.horiuchi.pokecom;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;

import static tk.horiuchi.pokecom.KeyboardBase.mBtnStatus;
import static tk.horiuchi.pokecom.MainActivity.rom_path;
import static tk.horiuchi.pokecom.MainLoop1460.digi;
import static tk.horiuchi.pokecom.MainLoop1460.state;
import static tk.horiuchi.pokecom.SubActivityBase.beep_enable;
import static tk.horiuchi.pokecom.SubActivityBase.nosave;

/**
 * Created by yoshimine on 2017/07/29.
 */

public class Sc61860_1460 extends Sc61860Base {
    protected final int BANKNUM = 4;
    protected final int BANKRAMSIZ = 0x3fff + 1;
    public static int bankram[][];
    protected int bank = 0;
    protected final int bankreg = 0x3c00;

    public Sc61860_1460(Context c) {
        super(c);

        RAM_START_ADR = 0x2000; // ちょっと手抜き
        RAM_END_ADR = 0xffff;

        CLOCK = 768;
    }

    @Override
    public Sc61860params saveParam() {
        Sc61860params sc_param = super.saveParam();

        sc_param.id = 1460;
        for (int i = 0; i < digi.length; i++) {
            sc_param.digi[i] = digi[i];
        }
        for (int i = 0; i < state.length; i++) {
            sc_param.state[i] = state[i];
        }
        return sc_param;
    }

    @Override
    public void restoreParam(Sc61860params sc_param) {
        super.restoreParam(sc_param);

        for (int i = 0; i < digi.length; i++) {
            digi[i] = sc_param.digi[i];
        }
        for (int i = 0; i < state.length; i++) {
            state[i] = sc_param.state[i];
        }
    }

    @Override
    protected void cmdHook() {

        if (bank != 0) return;

        switch (pc) {
            case 0x4186:    // CLOAD
            case 0x417a:    // LOAD
                Log.w("cmdHook", "exec CLOAD"+"("+bank+")");
                nosave = true;
                SubActivity1460.getInstance().actLoad();
                opcode = 0x37;
                break;
            case 0x418a:    // CSAVE
            case 0x417e:    // SAVE
                Log.w("cmfHook", "exec CSAVE"+"("+bank+")");
                nosave = true;
                SubActivity1460.getInstance().actSave();
                opcode = 0x37;
                break;
            case 0x40ed:    // BEEP
                Log.w("cmfHook", String.format("exec BEEP(%d)", iram[DRA2] - '0'));
                Log.w("cmfHook", String.format("%02X %02X %02X %02X %02X %02X %02X %02X", iram[DRA0],iram[DRA1],iram[DRA2],iram[DRA3],iram[DRA4],iram[DRA5],iram[DRA6],iram[DRA7]));
                if (beep_enable) {
                    beepDisable = 500;
                    int n = iram[DRA2] - '0';
                    if (n > 9) n = 9;
                    beep._2000Hz(n);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void LoadRomImage(Context c) {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(rom_path+"/pc1460mem.bin");

            byte buf[] = new byte[MAINRAMSIZ];
            int len, i = 0;
            while ((len = fis.read(buf)) != -1) {
                i += len;
            }
            for (int j = 0; j < i; j++) {
                mainram[j] = 0x00ff & buf[j];
            }
            Log.d("ROM", String.format("ROM imagefile is loaded(%d bytes)", i));
        } catch (IOException e) {
            Log.d("ROM", e.toString());
            halt = true;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        bankram = new int[BANKRAMSIZ][BANKNUM];
        try {
            fis = new FileInputStream(rom_path+"/pc1460bank.bin");

            byte buf[] = new byte[BANKRAMSIZ*BANKNUM];
            int len, i = 0;
            while ((len = fis.read(buf)) != -1) {
                i += len;
            }
            for (int k = 0; k < BANKNUM; k++) {
                for (int j = 0; j < BANKRAMSIZ; j++) {
                    bankram[j][k] = 0x00ff & buf[BANKRAMSIZ*k+j];
                }
            }
            Log.d("ROM", String.format("Bank imagefile is loaded(%d bytes)", i));
        } catch (IOException e) {
            Log.d("ROM", e.toString());
            halt = true;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    @Override
    protected int memr(int adr) {
        int dat = 0;

        if ((adr < 0) || (0xffff < adr)) {
            //alert('Invalid mainram address to read:' + adr.toString(16));
            Log.w("LOG", "Invalid mainram address to read:" + hex4(adr));
            //return(dat);
        }
        //dat = mainram[adr];
        if ((dat < 0) || (0x00ff < dat)) {
            //alert('Invalid mainram value read:' + hex4(dat) + ' at ' + hex4(adr));
            Log.w("LOG", "Invalid mainram value read:" + hex4(dat) + " at " + hex4(adr));
        }

        if (0x4000 <= adr && adr <= 0x7fff) {
            // bank switch
            dat = bankram[adr-0x4000][bank];
        } else {
            dat = mainram[adr];
        }

        return(dat);
    }

    /* write memory, check ROM address */
    @Override
    protected void memw(int adr, int dat) {
        if ((adr < 0) || (0xffff < adr)) {
            //alert('Invalid mainram address to write: ' + adr.toString(16));
            Log.w("LOG", "Invalid mainram address to write: " + hex4(adr));
        }
        if ((dat < 0) || (0x00ff < dat)) {
            //alert('Invalid mainram value written: ' + hex4(dat) + ' at ' + hex4(adr));
            Log.w("LOG", "Invalid mainram value written: " + hex4(dat) + " at " + hex4(adr));
        }

        if (0x2000 <= adr && adr <= 0x3fff || 0x8000 <= adr && adr <= 0xffff) {
            //Log.w("LOG", "memw!!!  adr="+hex4(adr));
            mainram[adr] = lobyte(dat);
            if (adr == bankreg) {
                bank = lobyte(dat & 0x03);
                //Log.w("LOG", "bank=" + bank);
            }

            if (0x3000 <= adr && adr <= 0x301d) {
                digi[adr - 0x3000] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (0x302d <= adr && adr <= 0x303b) {
                digi[30 + adr - 0x302d] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (0x301e <= adr && adr <= 0x302c) {
                digi[45 + adr - 0x301e] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (0x305e <= adr && adr <= 0x306c) {
                digi[75 - (adr - 0x305e) - 1] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (0x306d <= adr && adr <= 0x307b) {
                digi[90 - (adr - 0x306d) - 1] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (0x3040 <= adr && adr <= 0x305d) {
                digi[120 - (adr - 0x3040) - 1] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (adr == 0x303c) {
                state[0] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (adr == 0x303d) {
                state[1] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (adr == 0x307c) {
                state[2] = (byte) mainram[adr];
                listener.refreshScreen();
            }
        }
        else {
            Log.w("LOG", "Wrote to ROM: " + hex2(dat) + " at " + hex4(adr));
        }
    }

    @Override
    protected void ina() {
        int jj;

        iramw(AREG, 0);

        /*
        if (iacnt == 0) {
            int c = kb.getBuf();
            if (c != 0) {
                Log.w("ina", "inKey=" + c + " iaval=" + iaval + " ibval=" + ibval);
                kb.keyscan(c);
            }
        }
        */

        if ((iaval == 0) && (memr(0x3e00) != 0)) {
            //jj = memr(0x3e00) - 1;  // ビット表現になっていない！！！
            jj = bit(memr(0x3e00));
            if (jj < 7 && mBtnStatus[jj] != 0) {
                iramw(AREG, mBtnStatus[jj]);
                /*
                keyBufCnt = 3000;
                iacnt = incr16(iacnt);
                if (iacnt > 1) {
                    iacnt = 0;
                    kb.keyclear();
                    keyBufCnt = 0;
                }
                */
            }
        } else {
            jj = bit(iaval);
            if (jj < 6 && mBtnStatus[jj + 7] != 0) {
                iramw(AREG, mBtnStatus[jj + 7]);
                /*
                keyBufCnt = 3000;
                iacnt = incr16(iacnt);
                if (iacnt > 1) {
                    iacnt = 0;
                    kb.keyclear();
                    keyBufCnt = 0;
                }
                */
            }
        }
        if (iramr(AREG) == 0) {
            zflag = 1;
        }
        else {
            zflag = 0;
        }

    }

    @Override
    protected void inb() {

        //iramw(AREG, ibval&0xfe);
        iramw(AREG, 0x00);
        if (iramr(AREG) == 0) {
            zflag = 1;
        }
        else {
            zflag = 0;
        }
    }

}
