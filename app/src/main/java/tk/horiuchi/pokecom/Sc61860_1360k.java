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
import static tk.horiuchi.pokecom.MainLoop1360k.digi;
import static tk.horiuchi.pokecom.MainLoop1360k.state;
import static tk.horiuchi.pokecom.SubActivityBase.nosave;

import androidx.documentfile.provider.DocumentFile;

/**
 * Created by yoshimine on 2017/07/30.
 */

public class Sc61860_1360k extends  Sc61860Base {
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

    public Sc61860_1360k(Context c) {
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

        sc_param.id = 13601;
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
                SubActivity1360k.getInstance().actLoad();
                opcode = 0x37;
                break;
            case 0x42e3:
                // CSAVE
                Log.w("cmfHook", "exec CSAVE");
                nosave = true;
                SubActivity1360k.getInstance().actSave();
                opcode = 0x37;
                break;
            case 0x1f04:
                // CHR$
                Log.w("cmdHook", "exec CHR$");
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
            File f = new File(old_rom_path+"/pc1360kmem.bin");
            uri = Uri.fromFile(f);
        } else {
            DocumentFile src = rom_dir.findFile("pc1360kmem.bin");
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

            for (int j = 0x2000; j < 0x4000; j++) {
                mainram[j] = 0;
            }
            //mainram[0x3800] = 1;
            //mainram[0x3a00] = 1;
            //mainram[0x3c00] = 4;
            Log.d("ROM", String.format("ROM imagefile(1360K) is loaded(%d bytes)", i));
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

        bankram = new int[BANKRAMSIZ][BANKNUM];
        uri = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            File f = new File(old_rom_path+"/pc1360kbank.bin");
            uri = Uri.fromFile(f);
        } else {
            DocumentFile src = rom_dir.findFile("pc1360kbank.bin");
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
            Log.d("ROM", String.format("Bank imagefile(1360K) is loaded(%d bytes)", i));
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

    private int logCounter = 0;

    /*
    public class LogBuffer {
        private static final int SIZE = 256;

        private final int[] adr = new int[SIZE];
        private final int[] data = new int[SIZE];
        private final int[] pc = new int[SIZE];

        private int pos = 0;      // 次に書く位置
        private int count = 0;    // 有効件数

        public void add(int a, int d, int p) {
            adr[pos] = a;
            data[pos] = d;
            pc[pos] = p;

            pos = (pos + 1) % SIZE;

            if (count < SIZE) count++;
        }

        public void dump() {
            for (int i = 0; i < count; i++) {
                int idx = (pos - count + i + SIZE) % SIZE;
                System.out.printf("%04X : %02X (%04X)\n", adr[idx], data[idx], pc[idx]);
            }
        }
    }

    LogBuffer log = new LogBuffer();

     */

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

        //if (0x26d0 <= adr && adr <= 0x26ff) {
        //    dat = 0xff;
        //}
        //if (adr == 0x2000) dat = 0xfc;
        //if (adr == 0x2001) dat = 4;
        //if (adr == 0x26df) dat = 0xa5;
        //if (adr == 0x26fc) dat = 2;
        if ((0x2000 <= adr && adr < 0x2800 || 0x3100 <= adr && adr < 0x4000) && adr != 0x3e00) {
        //if ((0x2000 <= adr && adr < 0x4000) && adr != 0x3e00) {
            Log.w("1360K-LOG", String.format("memr adr=%04x dat=%02x", adr, dat));
        }

        if (logCounter != 0) {
            logCounter--;
            Log.w("1360K-LOG", String.format("mem r adr=%04x bank=%x dat=%02x", adr, bank, dat));

        }
        //if ((adr & 0xff00) == 0x3600) {
        //    dat = mainram[0x3600];
        //}
        //if ((adr & 0xff00) == 0x3800) {
        //    dat = mainram[0x3800];
        //}
        //if ((adr & 0xff00) == 0x3a00) {
        //    dat = mainram[0x3a00];
        //}

        //if ((adr & 0xff00) == 0x2900) dat = mainram[adr-0x100];
        //if ((adr & 0xff00) == 0x2b00) dat = mainram[adr-0x100];
        //if ((adr & 0xff00) == 0x2d00) dat = mainram[adr-0x100];
        //if ((adr & 0xff00) == 0x2f00) dat = mainram[adr-0x100];
        //if ((adr & 0xff00) == 0x3100) dat = mainram[adr-0x100];
        //log.add(adr, dat, pc);

        //if (kanji == 1 &&
        //    0x4000 < adr && adr < 0x8000 &&
        //        opcode == 0x56) dat = 0xaa;
        return(dat);
    }

    private int kanji = 0;
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
            if ((adr & 0xff00) == 0x3400 /*adr == bankreg*/) {
                mainram[adr] = bank = lobyte(dat & 0x07);
                Log.w("BANK-LOG", String.format("bank=%02x", dat));
            //} else if (adr == bankcreg) {
            //    bankc = lobyte(dat & 0x01);
            }

            if (adr == 0x3600) {
                kanji = lobyte(dat & 0x01) == 0 ? 1 : 0;
                Log.w("1360K-LOG", String.format("----- kanji=%d", kanji));
                if (kanji == 0) {
                    Log.w("1360K-LOG", "Log Dumped!!!");
                    logX.dump();
                }
                //if (kanji == 1) {
                    //iram[0x0f] = 0x55;
                    //for (int i = 0x0e; i < 0x10; i++) {
                    //    iram[i] = 0xa5;
                    //}
                //}
            }
            //if (adr == 0x3a00) kanji = 0;

            //if (0x8000 <= adr && adr <= 0xffff) {
            //    bankcram[adr - 0x8000][bankc] = lobyte(dat);
            //}

            //int adr1 = adr & 0xff00;
            //if (adr1 == 0x2900 || adr1 == 0x2b00 ||
            //    adr1 == 0x2d00 || adr1 == 0x2f00 || adr1 == 0x3100) {
            //    adr1 = adr - 0x0100;
            //    mainram[adr1] = mainram[adr];
            //    Log.w("1360K-LOG", String.format("--- mem w adr=%04x dat=%02x", adr, dat));

            //} else {
            //    adr1 = adr;
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
                    //Log.w("1360K-LOG", String.format("--- mem w adr=%04x dat=%02x", adr, dat));
                    listener.refreshScreen();
                    break;
                }
            }

            // シンボル表示
            if (adr == 0x303c) {
                //Log.w("1360K-LOG", "Wrote Symbol: " + hex2(dat) + " at " + hex4(adr));
                state[0] = (byte)mainram[adr];
                listener.refreshScreen();
            }

            //if (adr == 0x3e00) {
            //    if (lobyte(dat & 0x08) == 0x08) {
            //        Log.w("1360K-LOG", String.format("--- export bridge!!! -> %02x", dat));
            //    }
            //}
            //if ((0x2000 <= adr && adr < 0x2800 || 0x3100 <= adr && adr < 0x4000) && adr != 0x3e00 && adr != 0x3400) {
            //    Log.w("1360K-LOG", String.format("mem w adr=%04x dat=%02x", adr, dat));
            //}
            //if (adr == 0x3600) {
            //    logCounter = 300;
            //    Log.w("1360K-LOG", String.format("--- mem w adr=%04x dat=%02x ---", adr, dat));
            //}
            if (logCounter != 0) {
                logCounter--;
                Log.w("1360K-LOG", String.format("mem w adr=%04x bank=%x dat=%02x", adr, bank, dat));

            }
            //if (adr == 0x2a31) {
            //    Log.w("1360K-LOG", "Log Dumped!!!");
            //    logX.dump();
            //}
            //if (0x2e82 <= adr && adr < 0x2e90) {
            //    Log.w("1360K-LOG!!!", String.format("mem w adr=%04x bank=%x dat=%02x", adr, bank, dat));
            //}

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

        if ((iaval == 0) && (memr(0x3e00) != 0)) {
		    //jj = bit(memr(0x3e00));
            jj = memr(0x3e00) - 1;  // ビット表現になっていない！！！
		    if (jj < 7 && mBtnStatus[jj] != 0) {
			    iramw(AREG, mBtnStatus[jj]);
                //Log.w("0 ina", "bit="+jj);
                //Log.w("0 ina", String.format("bit=%d areg=%02x", jj, mBtnStatus[jj]));
                //if (jj == 1 && mBtnStatus[jj] == 0x20) {
                //    Log.w("1360K-LOG", "Log Dumped!!!");
                //    logX.setAfterTrace();
                //}
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
                //Log.w("1 ina", String.format("bit=%d areg=%02x", jj, mBtnStatus[jj + 7]));

                if (jj == 2 && mBtnStatus[jj + 7] == 0x10) {
                    Log.w("1360K-LOG", "Log Dumped!!!");
                    logX.setAfterTrace();
                }
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
        if ((foval & 8) != 0) {
            bankc = 1;
            Log.w("outf", String.format("rambank=%d", bankc));
        } else {
            bankc = 0;
            Log.w("outf", String.format("rambank=%d", bankc));
        }
    }

}
