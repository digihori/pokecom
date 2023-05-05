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
import static tk.horiuchi.pokecom.MainLoop1470.digi;
import static tk.horiuchi.pokecom.MainLoop1470.state;
import static tk.horiuchi.pokecom.SubActivityBase.beep_enable;
import static tk.horiuchi.pokecom.SubActivityBase.nosave;

import androidx.documentfile.provider.DocumentFile;

/**
 * Created by yoshimine on 2017/07/29.
 */

public class Sc61860_1470 extends Sc61860Base {
    protected final int BANKNUM = 8;
    protected final int BANKRAMSIZ = 0x3fff + 1;
    public static int bankram[][];
    protected int bank = 0;
    protected final int bankreg = 0x3400;

    public Sc61860_1470(Context c) {
        super(c);

        RAM_START_ADR = 0x2000; // ちょっと手抜き
        RAM_END_ADR = 0xffff;

        CLOCK = 768;
    }

    @Override
    public Sc61860params saveParam() {
        Sc61860params sc_param = super.saveParam();

        sc_param.id = 1470;
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
            case 0x42c1:    // CLOAD
            case 0x4300:    // LOAD
                Log.w("cmdHook", String.format("exec CLOAD(%04x) bank=%d", pc, bank));
                nosave = true;
                SubActivity1470.getInstance().actLoad();
                opcode = 0x37;
                break;
            case 0x42c5:    // CSAVE
            case 0x430c:    // SAVE
                Log.w("cmdHook", String.format("exec CSAVE(%04x) bank=%d", pc, bank));
                nosave = true;
                SubActivity1470.getInstance().actSave();
                opcode = 0x37;
                break;
            case 0x41ea:    // BEEP
                //Log.w("cmfHook", String.format("exec BEEP(%d)", iram[DRA2] - '0'));
                //Log.w("cmfHook", String.format("%02X %02X %02X %02X %02X %02X %02X %02X", iram[DRA0],iram[DRA1],iram[DRA2],iram[DRA3],iram[DRA4],iram[DRA5],iram[DRA6],iram[DRA7]));
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

        InputStream is = null;
        Uri uri = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            File f = new File(old_rom_path+"/pc1470mem.bin");
            uri = Uri.fromFile(f);
        } else {
            DocumentFile src = rom_dir.findFile("pc1470mem.bin");
            if (src != null) {
                uri = src.getUri();
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
        for (int i = 0x2000; i < 0x10000; i++) {
            mainram[i] = 0;
        }

        bankram = new int[BANKRAMSIZ][BANKNUM];
        uri = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            File f = new File(old_rom_path+"/pc1470bank.bin");
            uri = Uri.fromFile(f);
        } else {
            DocumentFile src = rom_dir.findFile("pc1470bank.bin");
            if (src != null) {
                uri = src.getUri();
            }
        }

        try {
            is = c.getContentResolver().openInputStream(uri);

            byte buf[] = new byte[BANKRAMSIZ*BANKNUM];
            int len, i = 0;
            while ((len = is.read(buf)) != -1) {
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
            if (is != null) {
                try {
                    is.close();
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
            dat = bankram[adr - 0x4000][bank];
        } else {
            /*
            if (0x8000 <= adr && adr < 0xa000) {
                adr += 0x6000;
            } else if (0xa000 <= adr && adr < 0xc000) {
                adr += 0x4000;
            } else if (0xc000 <= adr && adr < 0xe000) {
                adr += 0x2000;
            } else if (0x2900 <= adr && adr < 0x2a00) {
                adr -= 0x0100;
            } else {
                ;
            }
            */

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
            //if (adr == bankreg) {
            if (0x3400 <= adr && adr < 0x3600) {
                bank = lobyte(dat & 0x07);
                //Log.w("LOG", "bank=" + bank);
            }

            if (0x2800 <= adr && adr <= 0x283b) {
                digi[adr - 0x2800] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (0x2a00 <= adr && adr <= 0x2a3b) {
                digi[60 + adr - 0x2a00] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (0x2840 <= adr && adr <= 0x287b) {
                digi[120 + adr - 0x2840] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (0x2a40 <= adr && adr <= 0x2a7b) {
                digi[180 + adr - 0x2a40] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (adr == 0x283c) {
                state[0] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (adr == 0x283d) {
                state[1] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (adr == 0x287c) {
                state[2] = (byte)mainram[adr];
                listener.refreshScreen();
            } else if (adr == 0x287d) {
                state[3] = (byte)mainram[adr];
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
            jj = memr(0x3e00) - 1;  // ビット表現になっていない！！！
            if (jj < 7 && mBtnStatus[jj] != 0) {
                iramw(AREG, mBtnStatus[jj]);
                /*
                keyBufCnt = 3000;
                iacnt = incr16(iacnt);
                if (iacnt > 2) {
                    iacnt = 0;
                    kb.keyclear();
                    keyBufCnt = 0;
                }
                */
            }
        } else if (iaval != 0) {
            jj = bit(iaval);
            if (jj < 6 && mBtnStatus[jj + 7] != 0) {
                iramw(AREG, mBtnStatus[jj + 7]);
                /*
                keyBufCnt = 3000;
                iacnt = incr16(iacnt);
                if (iacnt > 2) {
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
