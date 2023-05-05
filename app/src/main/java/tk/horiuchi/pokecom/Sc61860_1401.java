package tk.horiuchi.pokecom;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static tk.horiuchi.pokecom.KeyboardBase.mBtnStatus;
import static tk.horiuchi.pokecom.MainActivity.old_rom_path;
import static tk.horiuchi.pokecom.MainActivity.rom_dir;
import static tk.horiuchi.pokecom.MainLoop1401.digi;
import static tk.horiuchi.pokecom.MainLoop1401.state;
import static tk.horiuchi.pokecom.SubActivityBase.beep_enable;
import static tk.horiuchi.pokecom.SubActivityBase.nosave;

import androidx.documentfile.provider.DocumentFile;

/**
 * Created by yoshimine on 2017/07/29.
 */

public class Sc61860_1401 extends Sc61860Base {

    public Sc61860_1401(Context c) {
        super(c);

        RAM_START_ADR = 0x2000;
        RAM_END_ADR = 0x7fff;
    }

    @Override
    public Sc61860params saveParam() {
        Sc61860params sc_param = super.saveParam();

        sc_param.id = 1401;
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
            case 0x9e25:    // CLOAD
                Log.w("cmdHook", "exec CLOAD");
                nosave = true;
                SubActivity1401.getInstance().actLoad();
                opcode = 0x37;
                break;
            case 0x9d20:    // CSAVE
                Log.w("cmfHook", "exec CSAVE");
                nosave = true;
                SubActivity1401.getInstance().actSave();
                opcode = 0x37;
                break;
            case 0xc014:    // BEEP
                if (beep_enable) {
                    beepDisable = 500;
                    //int n = iram[DRA2] - '0';
                    //if (n > 9) n = 9;
                    beep._2000Hz(1);
                }
            default:
                break;
        }
    }

    @Override
    protected void LoadRomImage(Context c) {
        InputStream is = null;
        Uri uri = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            File f = new File(old_rom_path+"/pc1401mem.bin");
            uri = Uri.fromFile(f);
        } else {
            DocumentFile src = rom_dir.findFile("pc1401mem.bin");
            if (src != null) {
                uri =src.getUri();
            }
        }

        try {
            is = c.getContentResolver().openInputStream(uri);

            byte buf[] = new byte[MAINRAMSIZ];
            int len, i = 0;
            while ((len = is.read(buf)) != -1) {
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
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


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
        if ((0x2000 <= adr) && (adr <= 0x7fff)) {
            mainram[adr] = lobyte(dat);

            if (0x6000 <= adr && adr <= 0x6027) {
                digi[adr - 0x6000] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (0x6040 <= adr && adr <= 0x6067) {
                digi[80 - (adr - 0x6040) - 1] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (adr == 0x603c) {
                state[0] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (adr == 0x603d) {
                state[1] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (adr == 0x607c) {
                state[2] = (byte)mainram[adr];
                listener.refreshScreen();
            }
        }
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
            if (jj < 7 && mBtnStatus[jj] != 0) {
                iramw(AREG, mBtnStatus[jj]);
                /*
                keyBufCnt = 3000;
                //Log.w("ina", "ibval="+ibval+" keym["+jj+"]="+keym[jj]);
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
            if (jj < 7 && mBtnStatus[jj+7] != 0) {
                iramw(AREG, mBtnStatus[jj + 7]);
                /*
                keyBufCnt = 3000;
                //Log.w("ina", "iaval="+iaval+" keym["+(jj+7)+"]="+keym[jj+7]);
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
        iramw(AREG, 0);
        if (iramr(AREG) == 0) {
            zflag = 1;
        }
        else {
            zflag = 0;
        }
    }

}
