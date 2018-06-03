package tk.horiuchi.pokecom;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;

import static tk.horiuchi.pokecom.KeyboardBase.mBtnStatus;
import static tk.horiuchi.pokecom.MainActivity.rom_path;
import static tk.horiuchi.pokecom.MainLoop1450.digi;
import static tk.horiuchi.pokecom.MainLoop1450.state;
import static tk.horiuchi.pokecom.SubActivityBase.nosave;

/**
 * Created by yoshimine on 2017/07/29.
 */

public class Sc61860_1450 extends Sc61860Base {

    public Sc61860_1450(Context c) {
        super(c);

        RAM_START_ADR = 0x2000; // ちょっと手抜き
        RAM_END_ADR = 0x7fff;

        CLOCK = 768;
    }

    @Override
    public Sc61860params saveParam() {
        Sc61860params sc_param = super.saveParam();

        sc_param.id = 1450;
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

        switch (pc) {
            case 0xae2f:    // CLOAD
            case 0xefdb:    // LOAD
                Log.w("cmdHook", "exec CLOAD");
                nosave = true;
                SubActivity1450.getInstance().actLoad();
                opcode = 0x37;
                break;
            case 0xacd8:    // CSAVE
            case 0xefe1:    // SAVE
                Log.w("cmfHook", "exec CSAVE");
                nosave = true;
                SubActivity1450.getInstance().actSave();
                opcode = 0x37;
                break;
            default:
                break;
        }
    }

    @Override
    protected void LoadRomImage(Context c) {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(rom_path+"/pc1450mem.bin");

            byte buf[] = new byte[MAINRAMSIZ];
            int len, i = 0;
            while ((len = fis.read(buf)) != -1) {
                i += len;
            }
            for (int j = 0; j < i; j++) {
                mainram[j] = 0x00ff & buf[j];
            }
            for (int j = 0x2000; j < 0x8000; j++) {
                mainram[j] = 0;
            }
            Log.d("ROM", String.format("ROM(1450) imagefile is loaded(%d bytes)", i));
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

        dat = mainram[adr];

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

        if (0x2000 <= adr && adr < 0x8000) {
            //Log.w("LOG", "memw!!!  adr="+hex4(adr));
            mainram[adr] = lobyte(dat);

            // VRAM領域はミラー処理がいるかも
            //if (0x7000 <= adr && adr < 0x7100) {
            //    mainram[adr+0x100] = lobyte(dat);
            //}

            if (0x7000 <= adr && adr <= 0x703b) {
                digi[adr - 0x7000] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (0x7068 <= adr && adr <= 0x707b) {
                digi[80 - (adr - 0x7068) - 1] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (adr == 0x703c) {
                state[0] = (byte)mainram[adr];
                //Log.w("memw", String.format("adr=%04x dat=%02x", adr, state[0]));
                listener.refreshScreen();
            } else if (adr == 0x703d) {
                state[1] = (byte)mainram[adr];
                //Log.w("memw", String.format("adr=%04x dat=%02x", adr, state[1]));
                listener.refreshScreen();
            } else if (adr == 0x707c) {
                state[2] = (byte) mainram[adr];
                //Log.w("memw", String.format("adr=%04x dat=%02x", adr, state[2]));
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

        if ((iaval == 0) && (memr(0x7e00) != 0)) {
            jj = bit(memr(0x7e00));
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
        iramw(AREG, 0x38);
        if (iramr(AREG) == 0) {
            zflag = 1;
        }
        else {
            zflag = 0;
        }
    }

}
