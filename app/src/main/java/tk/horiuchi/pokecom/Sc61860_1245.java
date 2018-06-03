package tk.horiuchi.pokecom;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;

import static tk.horiuchi.pokecom.KeyboardBase.mBtnStatus;
import static tk.horiuchi.pokecom.MainActivity.rom_path;
import static tk.horiuchi.pokecom.SubActivity1245.prog_mode;
import static tk.horiuchi.pokecom.MainLoop1245.digi;
import static tk.horiuchi.pokecom.MainLoop1245.state;
import static tk.horiuchi.pokecom.SubActivityBase.nosave;

/**
 * Created by yoshimine on 2017/07/29.
 */

public class Sc61860_1245 extends Sc61860Base {

    public Sc61860_1245(Context c) {
        super(c);

        RAM_START_ADR = 0x8000;
        RAM_END_ADR = 0xffff;
    }

    @Override
    public Sc61860params saveParam() {
        Sc61860params sc_param = super.saveParam();

        sc_param.id = 1245;
        sc_param.prog_mode_1245 = prog_mode;
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

        prog_mode = sc_param.prog_mode_1245;
        for (int i = RAM_START_ADR; i <= RAM_END_ADR; i++) {
            mainram[i] = sc_param.mainram[i];
        }
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
            case 0x7051:    // CLOAD
                Log.w("cmdHook", "exec CLOAD");
                nosave = true;
                SubActivity1245.getInstance().actLoad();
                opcode = 0x37;
                break;
            case 0x6dc7:    // CSAVE
                Log.w("cmfHook", "exec CSAVE");
                nosave = true;
                SubActivity1245.getInstance().actSave();
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
            fis = new FileInputStream(rom_path+"/pc1245mem.bin");

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
    }

    @Override
    protected int memr(int adr) {
        int dat = 0;

        if ((adr < 0) || (0xffff < adr)) {
            Log.w("LOG", "Invalid mainram address to read:" + hex4(adr));
        }

        /*
        if (0xb000 <= adr && adr < 0xb800 ) adr += 0x800;
        if (0x8000 <= adr && adr < 0xa000 ) adr += 0x2000;
        if (0xd000 <= adr && adr < 0xd800 ) adr -= 0x1000;
        if (0xf900 <= adr) adr &= 0xf8ff;
        */

        if (0x2000 <= adr && adr < 0x4000) {
            adr += 0x2000;
        }

        /*
        else if (0x8000 <= adr && adr < 0xa000) {
            adr += 0x2000;
        } else if (0xb000 <= adr && adr < 0xb800) {
            adr += 0x800;
        } else if (0xd000 <= adr && adr < 0xd800) {
            adr -= 0x1000;
        }
        */


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
        if (0xb000 <= adr && adr < 0xb800 ) adr += 0x800;
        if (0x8000 <= adr && adr < 0xa000 ) adr += 0x2000;
        if (0xd000 <= adr && adr < 0xd800 ) adr -= 0x1000;
        if (0xf900 <= adr) adr &= 0xf8ff;
        if (0xb800 <= adr && adr < 0xc000 ) adr += 0x800;

        if (0x8000 <= adr) {
            mainram[adr] = lobyte(dat);

            if (0xc000 <= adr && adr < 0xc800) {
                //mainram[adr] = lobyte(dat);
                mainram[adr + 0x1000] = lobyte(dat);
            } else if (0xb800 <= adr && adr < 0xc000) {
                mainram[adr - 0x800] = lobyte(dat);
            } else if (0xf800 <= adr && adr < 0xf900) {
                //mainram[adr] = lobyte(dat);
                mainram[adr + 0x100] = lobyte(dat);
                mainram[adr + 0x200] = lobyte(dat);
                mainram[adr + 0x300] = lobyte(dat);
                mainram[adr + 0x400] = lobyte(dat);
                mainram[adr + 0x500] = lobyte(dat);
                mainram[adr + 0x600] = lobyte(dat);
                mainram[adr + 0x700] = lobyte(dat);

                if (0xf800 <= adr && adr <= 0xf83b) {
                    //Log.w("LOG", ""+(adr-0xf800));
                    digi[adr - 0xf800] = (byte) mainram[adr];
                    listener.refreshScreen();
                } else if (0xf868 <= adr && adr <= 0xf87b) {
                    digi[80 - (adr - 0xf868) - 1] = (byte) mainram[adr];
                    listener.refreshScreen();
                } else if (adr == 0xf83c) {
                    state[0] = (byte) mainram[adr];
                    listener.refreshScreen();
                } else if (adr == 0xf83d) {
                    state[1] = (byte) mainram[adr];
                    listener.refreshScreen();
                } else {
                    ;
                }
            }
            else {
                //mainram[adr] = lobyte(dat);
                Log.w("memw", String.format("memw %04x %02x", adr, dat));
            }
        }

        /*
        if (0xc000 <= adr) {
            mainram[adr] = lobyte(dat);

            if (0xf800 <= adr && adr <= 0xf83b) {
                //Log.w("LOG", ""+(adr-0xf800));
                digi[adr - 0xf800] = (byte) mainram[adr];
                listener.refreshScreen();
            } else if (0xf868 <= adr && adr <= 0xf87b) {
                digi[80 - (adr - 0xf868) - 1] = (byte) mainram[adr];
                listener.refreshScreen();
            } else if (adr == 0xf83c) {
                state[0] = (byte) mainram[adr];
                listener.refreshScreen();
            } else if (adr == 0xf83d) {
                state[1] = (byte) mainram[adr];
                listener.refreshScreen();
            } else {
                ;
            }
        */
        else {
                Log.w("LOG", "Wrote to ROM: " + hex2(dat) + " at " + hex4(adr));
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
            if (prog_mode) {
                ret |= 2;
                //Log.w("LOG", "inb "+ret);
            }

        } else if ((ibval & 4) != 0) {

        } else if ((ibval & 2) != 0) {
            if (prog_mode) {
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
