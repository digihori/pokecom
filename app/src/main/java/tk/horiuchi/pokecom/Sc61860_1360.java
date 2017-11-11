package tk.horiuchi.pokecom;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;

import static tk.horiuchi.pokecom.KeyboardBase.keyBufCnt;
import static tk.horiuchi.pokecom.KeyboardBase.keym;
import static tk.horiuchi.pokecom.MainActivity.rom_path;
import static tk.horiuchi.pokecom.MainLoop1360.digi;
import static tk.horiuchi.pokecom.MainLoop1360.state;
import static tk.horiuchi.pokecom.SubActivityBase.kb;
import static tk.horiuchi.pokecom.SubActivityBase.nosave;

/**
 * Created by yoshimine on 2017/07/30.
 */

public class Sc61860_1360 extends  Sc61860Base {
    protected final int BANKNUM = 8;
    protected final int BANKRAMSIZ = 0x3fff + 1;
    protected final int BANKCNUM = 2;
    protected final int BANKCRAMSIZE = 0x7fff + 1;
    public static int bankram[][];
    protected int bank = 0;
    protected final int bankreg = 0x3400;
    public static int bankcram[][];
    protected int bankc = 0;
    protected final int bankcreg = 0x3600;

    //protected int version = 1;

    public Sc61860_1360(Context c) {
        super(c);

        RAM_START_ADR = 0x8000;
        RAM_END_ADR = 0xffff;

        CLOCK = 768;    // 違うよなぁ

        bankcram = new int[BANKCRAMSIZE][BANKCNUM];
        for (int i = 0; i < BANKCNUM; i++) {
            for (int j = 0; j < BANKCRAMSIZE; j++) {
                bankcram[j][i] = 0;
            }
        }
    }

    @Override
    public Sc61860params saveParam() {
        Sc61860params sc_param = super.saveParam();

        sc_param.id = 1360;
        for (int i = RAM_START_ADR; i <= RAM_END_ADR; i++) {
            sc_param.mainram[i] = mainram[i];
        }
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
        if (bank != 0) return;

        switch (pc) {
            case 0x42df:
                // CLOAD
                Log.w("cmdHook", "exec CLOAD");
                nosave = true;
                SubActivity1360.getInstance().actLoad();
                opcode = 0x37;
                break;
            case 0x42e3:
                // CSAVE
                Log.w("cmfHook", "exec CSAVE");
                nosave = true;
                SubActivity1360.getInstance().actSave();
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
            fis = new FileInputStream(rom_path+"/pc1360mem.bin");

            byte buf[] = new byte[MAINRAMSIZ];
            int len, i = 0;
            while ((len = fis.read(buf)) != -1) {
                i += len;
            }
            for (int j = 0; j < i; j++) {
                mainram[j] = 0x00ff & buf[j];
            }

            for (int j = 0x2000; j < 0x4000; j++) {
                mainram[j] = 0;
            }
            //mainram[0x3800] = 1;
            //mainram[0x3a00] = 1;
            //mainram[0x3c00] = 4;
            Log.d("ROM", String.format("ROM imagefile(1360) is loaded(%d bytes)", i));
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
/*
        try {
            fis = new FileInputStream(rom_path+"/pc1360ram.bin");

            byte buf[] = new byte[MAINRAMSIZ];
            int len, i = 0;
            while ((len = fis.read(buf)) != -1) {
                i += len;
            }
            for (int j = 0x0e000; j < 0x10000; j++) {
                mainram[j] = 0x00ff & buf[j];
            }

            Log.d("ROM", String.format("RAM imagefile(1360) is loaded(%d bytes)", i));
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
*/
        bankram = new int[BANKRAMSIZ][BANKNUM];
        try {
            fis = new FileInputStream(rom_path+"/pc1360bank.bin");

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
            Log.d("ROM", String.format("Bank imagefile(1360) is loaded(%d bytes)", i));
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
        //} else if (0x8000 <= adr && adr <= 0xffff){
        //    dat = bankcram[adr-0x8000][bankc];
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
            mainram[adr] = lobyte(dat);
            if (adr == bankreg) {
                bank = lobyte(dat & 0x07);
                //Log.w("LOG", "bank=" + bank);
            //} else if (adr == bankcreg) {
            //    bankc = lobyte(dat & 0x01);
            }

            //if (0x8000 <= adr && adr <= 0xffff) {
            //    bankcram[adr - 0x8000][bankc] = lobyte(dat);
            //}

            // VRAMのデータをdigi[]にコピー
            int tbl[] = new int[]
                    {0x2800, 0x2a00, 0x2c00, 0x2e00, 0x3000,
                     0x2840, 0x2a40, 0x2c40, 0x2e40, 0x3040,
                     0x281e, 0x2a1e, 0x2c1e, 0x2e1e, 0x301e,
                     0x285e, 0x2a5e, 0x2c5e, 0x2e5e, 0x305e};
            for (int i = 0; i < tbl.length; i++) {
                if (tbl[i] <= adr && adr < tbl[i]+30) {
                    digi[i*30+adr-tbl[i]] = (byte)mainram[adr];
                    listener.refreshScreen();
                    break;
                }
            }

            // シンボル表示
            if (adr == 0x303c) {
                state[0] = (byte)mainram[adr];
                listener.refreshScreen();
            }

        } else {
            Log.w("LOG", "Wrote to ROM: " + hex2(dat) + " at " + hex4(adr));
        }
    }

    @Override
    protected void ina() {

        //int ii;
        int jj;

        iramw(AREG, 0);

        if (iacnt == 0) {
            int c = kb.getBuf();
            if (c != 0) {
                Log.w("ina", "inKey=" + c + " iaval=" + iaval + " ibval=" + ibval);
                kb.keyscan(c);
            }
        }

        if ((iaval == 0) && (memr(0x3e00) != 0)) {
		    jj = bit(memr(0x3e00));
		    if (jj < 7 && keym[jj] != 0) {
			    iramw(AREG, keym[jj]);
                keyBufCnt = 3000;
                iacnt = incr16(iacnt);
                if (iacnt > 1) {
                    iacnt = 0;
                    kb.keyclear();
                    keyBufCnt = 0;
                }
		    }
	    } else {
            jj = bit(iaval);
            //System.out.printf("jj=%d\n", jj);
            //Log.w("LOG", "jj="+jj);
            if (jj < 5 && keym[jj + 7] != 0) {
                iramw(AREG, keym[jj + 7]);
                keyBufCnt = 3000;
                //Log.w("LOG", "iaval="+iaval+" keym["+(jj+7)+"]="+keym[jj+7]);
                //keym[jj+7] = 0;
                iacnt = incr16(iacnt);
                if (iacnt > 1) {
                    iacnt = 0;
                    kb.keyclear();
                    keyBufCnt = 0;
                }
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

    @Override
    protected void outf() {
        //if ((foval & 8) != 0) {
        //    bankc = 1;
        //} else {
        //    bankc = 0;
        //}
    }

}
