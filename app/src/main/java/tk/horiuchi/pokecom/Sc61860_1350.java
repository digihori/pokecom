package tk.horiuchi.pokecom;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;

import static tk.horiuchi.pokecom.KeyboardBase.mBtnStatus;
import static tk.horiuchi.pokecom.MainActivity.rom_path;
import static tk.horiuchi.pokecom.MainLoop1350.digi;
import static tk.horiuchi.pokecom.MainLoop1350.state;
import static tk.horiuchi.pokecom.SubActivityBase.nosave;

/**
 * Created by yoshimine on 2017/07/30.
 */

public class Sc61860_1350 extends  Sc61860Base {
    protected int version = 1;

    public Sc61860_1350(Context c) {
        super(c);

        RAM_START_ADR = 0x2000;
        RAM_END_ADR = 0x7fff;

        CLOCK = 768;    // 違うよなぁ
    }

    @Override
    public Sc61860params saveParam() {
        Sc61860params sc_param = super.saveParam();

        sc_param.id = 1350;
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
        if (mainram[0xfff0] == 3) { // version 2
            switch (pc) {
                case 0x977e:
                case 0xf603:
                    // CLOAD/LOAD
                    Log.w("cmdHook", "exec CLOAD");
                    nosave = true;
                    SubActivity1350.getInstance().actLoad();
                    opcode = 0x37;
                    break;
                case 0x95fc:
                case 0xf597:
                    // CSAVE/SAVE
                    Log.w("cmfHook", "exec CSAVE");
                    nosave = true;
                    SubActivity1350.getInstance().actSave();
                    opcode = 0x37;
                    break;
                default:
                    break;
            }
        } else {    // version 1
            switch (pc) {
                case 0x9627:
                case 0xf343:
                    // CLOAD/LOAD
                    Log.w("cmdHook", "exec CLOAD");
                    nosave = true;
                    SubActivity1350.getInstance().actLoad();
                    opcode = 0x37;
                    break;
                case 0x94a7:
                case 0xf267:
                    // CSAVE/SAVE
                    Log.w("cmfHook", "exec CSAVE");
                    nosave = true;
                    SubActivity1350.getInstance().actSave();
                    opcode = 0x37;
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void LoadRomImage(Context c) {
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(rom_path+"/pc1350mem.bin");

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

        if (mainram[0xfff0] == 3) {
            version = 2;
        }
        Log.w("LoadRomImage", "version="+version);

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

        if (0x2000 <= adr && adr <= 0x7fff) {
            mainram[adr] = lobyte(dat);

            // VRAMのデータをdigi[]にコピー
            int tbl[] = new int[]
                    {0x7000, 0x7200, 0x7400, 0x7600, 0x7800,
                     0x7040, 0x7240, 0x7440, 0x7640, 0x7840,
                     0x701e, 0x721e, 0x741e, 0x761e, 0x781e,
                     0x705e, 0x725e, 0x745e, 0x765e, 0x785e};
            for (int i = 0; i < tbl.length; i++) {
                if (tbl[i] <= adr && adr < tbl[i]+30) {
                    digi[i*30+adr-tbl[i]] = (byte)mainram[adr];
                    listener.refreshScreen();
                    break;
                }
            }

            // シンボル表示
            if (adr == 0x783c) {
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
            //System.out.printf("jj=%d\n", jj);
            //Log.w("LOG", "jj="+jj);
            if (jj < 5 && mBtnStatus[jj + 7] != 0) {
                iramw(AREG, mBtnStatus[jj + 7]);
                /*
                keyBufCnt = 3000;
                //Log.w("LOG", "iaval="+iaval+" keym["+(jj+7)+"]="+keym[jj+7]);
                //keym[jj+7] = 0;
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

}
