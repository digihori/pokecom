package tk.horiuchi.pokecom;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;

import static tk.horiuchi.pokecom.KeyboardBase.mBtnStatus;
import static tk.horiuchi.pokecom.MainActivity.rom_path;
import static tk.horiuchi.pokecom.MainLoop1261.digi;
import static tk.horiuchi.pokecom.MainLoop1261.state;
import static tk.horiuchi.pokecom.SubActivity1261.prog_mode;
import static tk.horiuchi.pokecom.SubActivityBase.beep_enable;
import static tk.horiuchi.pokecom.SubActivityBase.kb;
import static tk.horiuchi.pokecom.SubActivityBase.nosave;

/**
 * Created by yoshimine on 2017/07/29.
 */

public class Sc61860_1261 extends Sc61860Base {

    public Sc61860_1261(Context c) {
        super(c);

        RAM_START_ADR = 0x2000;
        RAM_END_ADR = 0x67ff;

        CLOCK = 768;
    }

    @Override
    public Sc61860params saveParam() {
        Sc61860params sc_param = super.saveParam();

        sc_param.id = 1261;
        sc_param.prog_mode_1251 = prog_mode;
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

        prog_mode = sc_param.prog_mode_1251;
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
                case 0x9695:    // CLOAD
                    Log.w("cmdHook", "exec CLOAD");
                    nosave = true;
                    SubActivity1261.getInstance().actLoad();
                    opcode = 0x37;
                    break;
                case 0x9513:    // CSAVE
                    Log.w("cmfHook", "exec CSAVE");
                    nosave = true;
                    SubActivity1261.getInstance().actSave();
                    opcode = 0x37;
                    break;
                case 0xc147:    // BEEP
                    Log.w("cmfHook", String.format("exec BEEP(%d)", iram[DRA2] - '0'));
                    Log.w("cmfHook", String.format("%02X %02X %02X %02X %02X %02X %02X %02X", iram[DRA0],iram[DRA1],iram[DRA2],iram[DRA3],iram[DRA4],iram[DRA5],iram[DRA6],iram[DRA7]));
                    if (beep_enable) {
                        beepDisable = 500;
                        //int n = iram[DRA2] - '0';
                        //if (n > 9) n = 9;
                        beep._2000Hz(1);
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
            fis = new FileInputStream(rom_path+"/pc1261mem.bin");

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
            //mainram[0x5d6] = 0x0b;
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
    }

    @Override
    protected int memr(int adr) {
        int dat = 0;

        if ((adr < 0) || (0xffff < adr)) {
            Log.w("LOG", "Invalid mainram address to read:" + hex4(adr));
        }

        dat = mainram[adr];

        if ((dat < 0) || (0x00ff < dat)) {
            Log.w("LOG", "Invalid mainram value read:" + hex4(dat) + " at " + hex4(adr));
        }

        return(dat);
    }

    /* write memory, check ROM address */
    @Override
    protected void memw(int adr, int dat) {
        if ((adr < 0) || (0xffff < adr)) {
            Log.w("LOG", "Invalid mainram address to write: " + hex4(adr));
        }
        if ((dat < 0) || (0x00ff < dat)) {
            Log.w("LOG", "Invalid mainram value written: " + hex4(dat) + " at " + hex4(adr));
        }

        //adr &= 0x7fff;
        if (0x2000 <= adr && adr < 0x6800) {
            if (0x2000 <= adr && adr < 0x4000) adr &= 0x28ff;

            mainram[adr] = lobyte(dat);

            if (0x2000 <= adr && adr < 0x3000) {
                mainram[adr + 0x1000] = lobyte(dat);
            }

            if (0x2000 <= adr && adr <= 0x203b) {
                digi[adr - 0x2000] = (byte) mainram[adr];
                listener.refreshScreen();
            } else if (0x2800 <= adr && adr <= 0x283b) {
                digi[60 + adr - 0x2800] = (byte) mainram[adr];
                listener.refreshScreen();
            } else if (0x2040 <= adr && adr <= 0x207b) {
                digi[120 + adr - 0x2040] = (byte) mainram[adr];
                listener.refreshScreen();
            } else if (0x2840 <= adr && adr <= 0x287b) {
                digi[180 + adr - 0x2840] = (byte) mainram[adr];
                listener.refreshScreen();
            } else if (adr == 0x203d) {
                state[0] = (byte) mainram[adr];
                listener.refreshScreen();
            } else if (adr == 0x207c) {
                state[1] = (byte) mainram[adr];
                listener.refreshScreen();
            } else {
                ;
            }
        } else {
            Log.w("LOG", "(pc="+hex4(pc-1)+")Wrote to ROM: " + hex2(dat) + " at " + hex4(adr));
        }
    }

    @Override
    protected void ina() {

        //int ii;
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

        if ((iaval == 0) && (ibval != 0)) {
            jj = bit(ibval);
            if (jj < 3 && mBtnStatus[jj] != 0) {
                iramw(AREG, mBtnStatus[jj]);
                /*
                keyBufCnt = 3000;
                //Log.w("LOG", "ibval="+ibval+" keym["+jj+"]="+keym[jj]+" AREG="+iramr(AREG));
                //keym[jj] = 0;
                iacnt = incr16(iacnt);
                if (iacnt > 1) {
                    iacnt = 0;
                    kb.keyclear();
                    keyBufCnt = 0;
                }
                */
            }
        }
        else if (iaval != 0) {
            jj = bit(iaval);
            if (jj < 7 && mBtnStatus[jj + 3] != 0) {
                iramw(AREG, mBtnStatus[jj + 3]);
                /*
                keyBufCnt = 3000;
                //Log.w("LOG", "iaval="+iaval+" keym["+(jj+3)+"]="+keym[jj+3]+" AREG="+iramr(AREG));
                //keym[jj+3] = 0;
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
        int ret=0;

        if ((ibval & 8) != 0) {
            if (prog_mode == 1) {
                ret |= 2;
                //Log.w("LOG", "inb "+ret);
            } else if (prog_mode == 2) {
                ret |= 1;
            }

        } else if ((ibval & 1) != 0) {
            if (prog_mode == 2) {
                ret |= 8;
            }
        } else if ((ibval & 2) != 0) {
            if (prog_mode == 1) {
                ret |= 8;
                //Log.w("LOG", "inb "+ret);
            }

        } else if ((ibval & 1) != 0) {

        } else {
            ;
        }

        ret = ret & ~ibval;

        iramw(AREG, ret);
        if (iramr(AREG) == 0) {
            zflag = 1;
        }
        else {
            zflag = 0;
        }
    }

}
