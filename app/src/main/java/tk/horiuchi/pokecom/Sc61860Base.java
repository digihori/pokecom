package tk.horiuchi.pokecom;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import static tk.horiuchi.pokecom.Beep.beep_on;
import static tk.horiuchi.pokecom.KeyboardBase.keyBufCnt;
import static tk.horiuchi.pokecom.KeyboardBase.keym;
import static tk.horiuchi.pokecom.MainActivity.dpdx_org;
import static tk.horiuchi.pokecom.SubActivityBase.beep_enable;
import static tk.horiuchi.pokecom.SubActivityBase.clock_emulate_enable;
import static tk.horiuchi.pokecom.SubActivityBase.debugText;
import static tk.horiuchi.pokecom.SubActivityBase.debug_info;
import static tk.horiuchi.pokecom.SubActivityBase.kb;

/**
 * Created by yoshimine on 2017/07/24.
 */

public class Sc61860Base implements Serializable {
    public static int[] code_cnt;
    public boolean step = false;
    public static boolean stepdebug = false;
    protected boolean halt = false;

    protected int RAM_START_ADR = 0x0000;
    protected int RAM_END_ADR = 0xffff;

    public int ndsteps = 0;
    private final int MAXNDS = 8192;
    public int traceiramdump = 1;
    private int debug_cnt;

    protected RefreshScreenInterFace listener = null;

    protected Context context = null;
    protected Beep beep = null;
    private int beep_cnt = 0;

    /* SC61860 registers */
    protected int pc = 0x0000;
    protected int current_pc = 0;
    protected int opcode = 0;
    protected int dp = 0x0000;
    protected int preg = 0;
    protected int qreg = 0;
    protected int rreg = 0;
    protected int dreg = 0;

    protected int alu = 0;
    protected int cflag = 0;
    protected int zflag = 0;
    protected int xin = 0;
    protected int ticks = 0;
    protected int ticks2 = 0;
    protected int div500 = 0;
    protected int div2 = 0;
    protected int iTick = 0;

    private final int XTICKS = 5;
    private final int XTICK2 = 2;

    protected int power_on = 0;
    public static int disp_on = 1;
    public static int kon = 1;
    public static int kon_cnt = 10;

    protected int iaval = 0;
    protected int ibval = 0;
    protected int foval = 0;
    protected int ctrlval = 0;
    protected int testport = 0;

    protected int iacnt = 0;
    protected int keychar = 0;

    private final int IRAMSIZ = (0x005f + 1);
    protected int iram[];

    protected final int MAINRAMSIZ = (0xffff + 1);
    public static int mainram[];

    /* registers on IRAM */
    protected final int IREG = 0;
    protected final int JREG = 1;
    protected final int AREG = 2;
    protected final int BREG = 3;
    protected final int XLREG = 4;
    protected final int XHREG = 5;
    protected final int YLREG = 6;
    protected final int YHREG = 7;
    protected final int KREG = 8;
    protected final int LREG = 9;
    protected final int MREG = 10;
    protected final int NREG = 11;


    protected final int IAPORT = 0x5c;
    protected final int IBPORT = 0x5d;
    protected final int FOPORT = 0x5e;
    protected final int CTRLPORT = 0x5f;


    protected int CLOCK = 576;
    protected int tick;
    protected float cpu_wait;

    protected long oldTime, newTime;
    protected int cpu_cnt = 0;

    protected int cnt = 0;

    protected int beep_on_cnt = 0;
    protected int beep_off_cnt = 0;
    protected int beep_off_cnt_last = 0;
    public static double beep_freq = 0;

    public Sc61860Base(Context c) {
        context = c;
        iram = new int[256];
        mainram = new int[MAINRAMSIZ];

        LoadRomImage(c);

        //beep = new Beep();
        //beep.start();

        code_cnt = new int[256];
        for (int i = 0; i < 256; i++) {
            code_cnt[i] = 0;
        }
    }

    public Sc61860params saveParam() {
        Sc61860params sc_param = new Sc61860params();

        sc_param.pc = pc;
        sc_param.current_pc = current_pc;
        sc_param.opcode = opcode;
        sc_param.dp = dp;
        sc_param.preg = preg;
        sc_param.qreg = qreg;
        sc_param.rreg = rreg;
        sc_param.dreg = dreg;

        sc_param.alu = alu;
        sc_param.cflag = cflag;
        sc_param.zflag = zflag;
        sc_param.xin = xin;
        sc_param.ticks = ticks;
        sc_param.ticks2 = ticks2;
        sc_param.div500 = div500;
        sc_param.div2 = div2;

        sc_param.power_on = power_on;
        sc_param.disp_on = disp_on;
        sc_param.kon = kon;
        sc_param.kon_cnt = kon_cnt;

        sc_param.iaval = iaval;
        sc_param.ibval = ibval;
        sc_param.foval = foval;
        sc_param.ctrlval = ctrlval;
        sc_param.testport = testport;

        for (int i = 0; i < 256; i++) {
            sc_param.iram[i] = iram[i];
        }
        for (int i = RAM_START_ADR; i <= RAM_END_ADR; i++) {
            sc_param.mainram[i] = mainram[i];
        }

        return sc_param;
    }

    public void restoreParam(Sc61860params sc_param) {

        pc = sc_param.pc;
        current_pc = sc_param.current_pc;
        opcode = sc_param.opcode;
        dp = sc_param.dp;
        preg = sc_param.preg;
        qreg = sc_param.qreg;
        rreg = sc_param.rreg;
        dreg = sc_param.dreg;

        alu = sc_param.alu;
        cflag = sc_param.cflag;
        zflag = sc_param.zflag;
        xin = sc_param.xin;
        ticks = sc_param.ticks;
        ticks2 = sc_param.ticks2;
        div500 = sc_param.div500;
        div2 = sc_param.div2;

        power_on = sc_param.power_on;
        disp_on = sc_param.disp_on;
        kon = sc_param.kon;
        kon_cnt = sc_param.kon_cnt;

        iaval = sc_param.iaval;
        ibval = sc_param.ibval;
        foval = sc_param.foval;
        ctrlval = sc_param.ctrlval;
        testport = sc_param.testport;

        for (int i = 0; i < 256; i++) {
            iram[i] = sc_param.iram[i];
        }
        for (int i = RAM_START_ADR; i <= RAM_END_ADR; i++) {
            mainram[i] = sc_param.mainram[i];
        }

    }

    protected void LoadRomImage(Context c) {
        // 実装は継承クラスで
    }

    public synchronized void halt() {
        halt = true;
    }

    public void restart() {
        halt = false;
        listener.refreshScreen();
    }

    public synchronized void CpuReset() {
        halt = false;
        debug_cnt = 0;
        pc = 0x0000;
        opcode = 0;
        dp = 0x0000;
        preg = 0;
        qreg = 0;
        rreg = IRAMSIZ;
        dreg = 0;
        alu = 0;
        cflag = 0;
        zflag = 0;
        xin = 0;
        ticks = 0;
        ticks2 = 0;
        div500 = 0;
        div2 = 0;
        power_on = 1;
        kon = 0;
        kon_cnt = 0;
        iaval = 0;
        ibval = 0;
        foval = 0;
        ctrlval = 0;
        testport = 0;
        iacnt = 0;
        keychar = 0;

        for (int i = 0; i < 256; i++) {
            iram[i] = 0;
        }
        for (int i = RAM_START_ADR; i <= RAM_END_ADR; i++) {
            mainram[i] = 0;
        }
    }

    protected void cmdHook() {
        ;
    }

    public void CpuRun() {
        //Log.w("LOG", "--- run ---");

        if (halt) return;

        if (clock_emulate_enable) {
            if (cpu_cnt == 0) {
                oldTime = System.currentTimeMillis();
                iTick = 0;
            }
            cpu_cnt++;
            if (cpu_cnt > 999) {
                cpu_cnt = 0;
                //Log.w("RUN", String.format("iTick = %d", iTick));
                newTime = System.currentTimeMillis();
                long sleepTime = iTick*2/CLOCK - (newTime - oldTime);
                //long sleepTime = 20 - (newTime - oldTime);

                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        ;
                    }
                }

                //Log.w("RUN", String.format("iTick=%d past time=%d diff=%d", iTick, newTime-oldTime, sleepTime));
            }
        } else {
            cpu_cnt = 0;
        }

        // 仮の2msecタイマー
        if (cnt == 0) {
            div2 = 1;
        }
        cnt++;
        if (cnt > 100) {
            cnt = 0;
        }

        tick = 1;

        // beep エミュレーション
        // beep on の状態でメインループを回った回数で擬似的にブザーの周波数を決める（かなり苦しいけど）
        // beep_freq 変数はBeepクラスで参照している
        /*
        if (beep_on) {
            beep_off_cnt = 0;
            beep_on_cnt++;
        } else {
            if (beep_on_cnt != 0) {
                // オン状態のカウンタ値から周波数を決める
                if (beep_off_cnt_last != 0) {
                    if (beep_on_cnt > 100) {
                        beep_freq = 2000;
                    } else {
                        beep_freq = 30000 / (beep_on_cnt * 2.5);
                    }
                    Log.w("Sc61860Base", String.format("beep on --- freq=%d", (int)beep_freq));
                } else {
                    beep_freq = 0;
                    //Log.w("Sc61860Base", String.format("beep off --- freq=%d", (int)beep_freq));
                }
            }
            beep_on_cnt = 0;
            beep_off_cnt_last = ++beep_off_cnt;
        }
        */

        current_pc = pc;
        opcode = memr(pc);
        cmdHook();
        pc = incr16(pc);
        opfunc[opcode & 0xff].intFunc();

        code_cnt[opcode & 0xff]++;

        if (debug_info && ++debug_cnt > 50) {
            debug_cnt = 0;
            debugText = String.format("PC=%04x(%02x %s) P=%02x Q=%02x R=%02x DP=%04x I=%02x J=%02x A=%02x B=%02x\n" +
                            "XhXl=%02x%02x YhYl=%02x%02x PA=%02x PB=%02x PC=%02x PT=%02x pow=%02x lcd=%02x kon=%02x d=%f",
                    current_pc, opcode, instractionCode[opcode], preg, qreg, rreg, dp, iram[IREG], iram[JREG], iram[AREG], iram[BREG],
                    iram[XHREG], iram[XLREG], iram[YHREG], iram[YLREG], iaval, ibval, ctrlval, testport, power_on, disp_on, kon_cnt, dpdx_org);
        }


        /*
        if (current_pc == 0x4000) stepdebug = true;
        if (stepdebug) {
            String text = String.format(
                    "PC=%04x([%s]%02x %02x %02x %02x) P=%02x Q=%02x R=%02x DP=%04x I=%02x J=%02x A=%02x B=%02x XhXl=%02x%02x YhYl=%02x%02x PA=%02x PB=%02x PT=%02x\n",
                    current_pc, instractionCode[opcode], opcode,
                    memr(current_pc+1), memr(current_pc+2), memr(current_pc+3),
                    preg, qreg, rreg, dp, iram[IREG], iram[JREG], iram[AREG], iram[BREG],
                    iram[XHREG], iram[XLREG], iram[YHREG], iram[YLREG], iaval, ibval, testport);
            Log.w("STEP", text);
        } else {
        }
        */


        if (keyBufCnt != 0) {
            keyBufCnt--;
            if (keyBufCnt == 0) {
                kb.keyclear();
                iacnt = 0;
                Log.w("mainloop", String.format("KeyBufCnt Cleared."));
            }
        }
    }

    public void ticTac() {
        //if (div500 == 0) {
            //div500 = 1;
            //Log.w("LOG", "tic5");
        //}
        //else {
        //    div500 = 0;
        //}
        if (++ticks2 >= 25) {
            div500 = 1;
            //if (div500 == 0) {
            //    div500 = 1;
            //}
            //else {
            //    div500 = 0;
            //}
            //if (div2 == 0) {
            //    div2 = 1;
                //Log.w("LOG", "tic2");
            //}
            //else {
            //    div2 = 0;
            //}
            ticks2 = 0;
        }

    }


    public void setListener(RefreshScreenInterFace listener) {
        this.listener = listener;
    }
    public void removeListener() {
        this.listener = null;
    }


    /* -------------------------------------- */
    /* 8bit, 16bit */
    protected int hibyte(int x) {
        return((x & 0xff00) >> 8);
    }


    protected int lobyte(int x) {
        return(x & 0x00ff);
    }


    protected int hilo(int hi, int lo) {
        return(((hi & 0x00ff) << 8) | (lo & 0x00ff));
    }


    protected String hex4(int x) {
        return(String.format("%04x", x));
    }


    protected String hex2(int x) {
        return(String.format("%02x", x));
    }


    protected int iramr(int adr) {
        int dat = 0;

        if ((adr < 0) || (0x5f < adr)) {
            Log.w("LOG", "Invalid iram address to read: " + hex4(adr));
        }
        dat = iram[adr];
        if ((dat < 0) || (0x00ff < dat)) {
            Log.w("LOG", "Invalid iram value read: " + hex4(dat) + " at " + hex4(adr));
        }
        return(dat);
    }


    protected void iramw(int adr, int dat) {
        if ((adr < 0) || (0x5f < adr)) {
            Log.w("LOG", "Invalid iram address to write:" + hex4(adr));
        }
        if ((dat < 0) || (0x00ff < dat)) {
            Log.w("LOG", "Invalid iram value written:" + hex4(dat) + " at " + hex4(adr));
        }
        iram[adr] = dat;
    }


    /* read memory */
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
    protected void memw(int adr, int dat) {
        if ((adr < 0) || (0xffff < adr)) {
            Log.w("LOG", "Invalid mainram address to write: " + hex4(adr));
        }
        if ((dat < 0) || (0x00ff < dat)) {
            Log.w("LOG", "Invalid mainram value written: " + hex4(dat) + " at " + hex4(adr));
        }
    }

    /*
     * short/unsigned char add/sub
     */
    protected int add16(int a, int b) {
        return(0xffff & (a + b));
    }

    protected int sub16(int a, int b) {
        return(0xffff & (a - b));
    }

    protected int incr16(int dat) {
        return(add16(dat, 1));
    }

    protected int decr16(int dat) {
        return(sub16(dat, 1));
    }

    protected int add8(int a, int b) {
        return(0x00ff & (a + b));
    }

    protected int sub8(int a, int b) {
        return(0x00ff & (a - b));
    }

    protected int incr8(int dat) {
        return(add8(dat, 1));
    }

    protected int decr8(int dat) {
        return(sub8(dat, 1));
    }

    protected int incr7(int dat) {
        //int ans = add8(dat, 1);
        //if (ans > 0x5f) ans = 0;
        //return ans;
        return(0x007f & (dat + 1));
    }

    protected int decr7(int dat) {
        //int ans = sub8(dat, 1);
        //if (ans < 0) ans = 0x5f;
        //return ans;
        return(0x007f & (dat - 1));
    }

    /*
     * operations
     */
    public interface Base {
        void intFunc();
    }

    /* secret */
    public class op35_mvwp implements Base {
        public void intFunc() {
            pc = decr16(pc);
            iTick += (iramr(IREG) * 4 + 7);
            dreg = incr8(iramr(IREG));
            rreg = decr8(rreg);
            iramw(rreg, hibyte(pc));
            rreg = decr8(rreg);
            iramw(rreg, lobyte(pc));
            pc = hilo(iramr(BREG), iramr(AREG));
            do {
                iramw(preg, memr(pc));
                preg = incr7(preg);
                pc = incr16(pc);
                dreg = decr8(dreg);
            } while (dreg != 0);
            pc = incr16(hilo(iramr(rreg + 1), iramr(rreg)));
            rreg = add8(rreg, 2);
        }
    }


    public class opc6_tsma implements Base {
        public void intFunc() {
            if ((iramr(preg) & iramr(AREG)) == 0) {
                zflag = 1;
            }
            else {
                zflag = 0;
            }
            iTick += 3;
        }
    }


    public class op23_clra implements Base {
        public void intFunc() {
            iramw(AREG, 0);
            iTick += 2;
        }
    }


    public class op54_mvmp implements Base {
        public void intFunc() {
            iramw(preg, memr(pc));
            iTick += 3;
        }
    }


    public class op56_ldpc implements Base {
        public void intFunc() {
            iramw(AREG, memr(pc));
            iTick += 3;
        }
    }


    public class opd7_sz implements Base {
        public void intFunc() {
            iramw(rreg - 1, memr(dp));
            pc = incr16(pc);
            zflag = 1;
            iTick += 6;
        }
    }


    public class op72_rz implements Base {
        public void intFunc() {
            pc = incr16(pc);
            zflag = 0;
            iTick += 4;
        }
    }


    /* 8bits load */
    public class op20_ldp implements Base {
        public void intFunc() {
            iramw(AREG, preg);
            iTick += 2;
        }
    }


    public class op21_ldq implements Base {
        public void intFunc() {
            iramw(AREG, qreg);
            iTick += 2;
        }
    }


    public class op22_ldr implements Base {
        public void intFunc() {
            iramw(AREG, rreg);
            iTick += 2;
        }
    }


    public class op59_ldm implements Base {
        public void intFunc() {
            iramw(AREG, iramr(preg));
            iTick += 2;
        }
    }


    public class op57_ldd implements Base {
        public void intFunc() {
            iramw(AREG, memr(dp));
            iTick += 3;
        }
    }


    public class op02_lia implements Base {
        public void intFunc() {
            iramw(AREG,  memr(pc));
            pc = incr16(pc);
            iTick += 4;
        }
    }


    public class op03_lib implements Base {
        public void intFunc() {
            iramw(BREG, memr(pc));
            pc = incr16(pc);
            iTick += 4;
        }
    }


    public class op01_lij implements Base {
        public void intFunc() {
            iramw(JREG, memr(pc));
            pc = incr16(pc);
            iTick += 4;
        }
    }


    public class op00_lii implements Base {
        public void intFunc() {
            iramw(IREG, memr(pc));
            pc = incr16(pc);
            iTick += 4;
        }
    }


    public class op12_lip implements Base {
        public void intFunc() {
            preg = memr(pc)&0x7f;
            pc = incr16(pc);
            iTick += 4;
        }
    }


    public class op80_lp implements Base {
        public void intFunc() {
            preg = opcode & 0x3f;
            iTick += 2;
        }
    }


    public class op30_stp implements Base {
        public void intFunc() {
            preg = iramr(AREG)&0x7f;
            iTick += 2;
        }
    }


    public class op31_stq implements Base {
        public void intFunc() {
            qreg = iramr(AREG)&0x7f;
            iTick += 2;
        }
    }


    public class op13_liq implements Base {
        public void intFunc() {
            qreg = memr(pc)&0x7f;
            pc = incr16(pc);
            iTick += 4;
        }
    }


    public class op32_str implements Base {
        public void intFunc() {
            rreg = iramr(AREG)&0x7f;
            iTick += 2;
        }
    }


    public class op11_lidl implements Base {
        public void intFunc() {
            dp = hilo(hibyte(dp), memr(pc));
            pc = incr16(pc);
            iTick += 5;
        }
    }


    public class op52_std implements Base {
        public void intFunc() {
            memw(dp, iramr(AREG));
            iTick += 2;
        }
    }


    public class op53_mvdm implements Base {
        public void intFunc() {
            memw(dp, iramr(preg));
            iTick += 3;
        }
    }


    public class op55_mvmd implements Base {
        public void intFunc() {
            iramw(preg, memr(dp));
            iTick += 3;
        }
    }


    public class opd8_leave implements Base {
        public void intFunc() {
            iramw(rreg, 0);
            iTick += 2;
        }
    }


    public class op5b_pop implements Base {
        public void intFunc() {
            iramw(AREG, iramr(rreg));
            rreg = incr8(rreg);
            iTick += 2;
        }
    }


    public class op34_push implements Base {
        public void intFunc() {
            rreg = decr8(rreg);
            iramw(rreg, iramr(AREG));
            iTick += 3;
        }
    }


    /* 16bits load */
    public class op10_lidp implements Base {
        public void intFunc() {
            dp = hilo(memr(pc), memr(pc + 1));
            pc = add16(pc, 2);
            iTick += 8;
        }
    }


    /* block load, exchange */
    public class op0a_mvb implements Base {
        public void intFunc() {
            iTick += (iramr(JREG)*2 + 3);
            dreg = incr8(iramr(JREG));
            do {
                iramw(preg, iramr(qreg));
                preg = incr7(preg);
                qreg = incr7(qreg);
                dreg = decr8(dreg);
            } while (dreg != 0);
        }
    }


    public class op08_mvw implements Base {
        public void intFunc() {
            iTick += (iramr(IREG)*2 + 3);
            dreg = incr8(iramr(IREG));
            do {
                iramw(preg, iramr(qreg));
                preg = incr7(preg);
                qreg = incr7(qreg);
                dreg = decr8(dreg);
            } while (dreg != 0);
        }
    }


    public class op1a_mvbd implements Base {
        public void intFunc() {
            iTick += (iramr(JREG)*4 + 3);
            dreg = incr8(iramr(JREG));
            do {
                iramw(preg, memr(dp));
                preg = incr7(preg);
                dp = incr16(dp);
                dreg = decr8(dreg);
            } while (dreg != 0);
        }
    }


    public class op18_mvwd implements Base {
        public void intFunc() {
            iTick += (iramr(IREG)*4 + 3);
            dreg = incr8(iramr(IREG));
            do {
                iramw(preg, memr(dp));
                preg = incr7(preg);
                dp = incr16(dp);
                dreg = decr8(dreg);
            } while (dreg != 0);
        }
    }


    public class op1e_film implements Base {
        public void intFunc() {
            iTick += (iramr(IREG) + 4);
            dreg = incr8(iramr(IREG));
            do {
                iramw(preg, iramr(AREG));
                preg = incr7(preg);
                dreg = decr8(dreg);
            } while (dreg != 0);
        }
    }


    public class op1f_fild implements Base {
        public void intFunc() {
            iTick += (iramr(IREG) + 4);
            dreg = incr8(iramr(IREG));
            do {
                memw(dp, iramr(AREG));
                dp = incr16(dp);
                dreg = decr8(dreg);
            } while (dreg != 0);
        }
    }


    public class opda_exab implements Base {
        public void intFunc() {
            int tempval = iramr(AREG);

            iramw(AREG, iramr(BREG));
            iramw(BREG, tempval);
            iTick += 3;
        }
    }


    public class opdb_exam implements Base {
        public void intFunc() {
            int tempval = iramr(AREG);

            iramw(AREG, iramr(preg));
            iramw(preg, tempval);
            iTick += 3;
        }
    }


    public class op0b_exb implements Base {
        public void intFunc() {
            int tempval = 0;
            iTick += (iramr(JREG)*3 + 3);
            dreg = incr8(iramr(JREG));
            do {
                tempval = iramr(preg);
                iramw(preg, iramr(qreg));
                iramw(qreg, tempval);
                preg = incr7(preg);
                qreg = incr7(qreg);
                dreg = decr8(dreg);
            } while (dreg != 0);
        }
    }


    public class op09_exw implements Base {
        public void intFunc() {
            int tempval = 0;
            iTick += (iramr(IREG)*3 + 3);
            dreg = incr8(iramr(IREG));
            do {
                tempval = iramr(preg);
                iramw(preg, iramr(qreg));
                iramw(qreg, tempval);
                preg = incr7(preg);
                qreg = incr7(qreg);
                dreg = decr8(dreg);
            } while (dreg != 0);
        }
    }


    public class op19_exwd implements Base {
        public void intFunc() {
            int tempval = 0;
            iTick += (iramr(IREG)*6 + 3);
            dreg = incr8(iramr(IREG));
            do {
                tempval = memr(dp);
                memw(dp, iramr(preg));
                iramw(preg, tempval);
                preg = incr7(preg);
                dp = incr16(dp);
                dreg = decr8(dreg);
            } while (dreg != 0);
        }
    }


    public class op1b_exbd implements Base {
        public void intFunc() {
            int tempval = 0;
            iTick += (iramr(JREG)*6 + 3);
            dreg = incr8(iramr(JREG));
            do {
                tempval = memr(dp);
                memw(dp, iramr(preg));
                iramw(preg, tempval);
                preg = incr7(preg);
                dp = incr16(dp);
                dreg = decr8(dreg);
            } while (dreg!= 0);
        }
    }


    public class op58_swp implements Base {
        public void intFunc() {
            int tempval = 0;

		/* shift lo nibble << 4 */
            tempval = ((iramr(AREG) & 0x000f) << 4)
		/* shift hi nibble >> 4	 */
                    | ((iramr(AREG) & 0x00f0) >> 4);
            iramw(AREG, tempval);
            iTick += 2;

        }
    }


    /* check condition, set flag */
    public void chkcz() {
        alu = alu & 0x0000ffff;
        if ((alu & 0x00ff) == 0) {
            zflag = 1;
        } else {
            zflag = 0;
        }
        if ((alu & 0xff00) != 0) {
            cflag = 1;
        } else {
            cflag = 0;
        }
    }


    /* 8bits operation */
    public class op74_adia implements Base {
        public void intFunc() {
            alu = iramr(AREG) + memr(pc);
            pc = incr16(pc);
            iramw(AREG, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class op70_adim implements Base {
        public void intFunc() {
            alu = iramr(preg) + memr(pc);
            pc = incr16(pc);
            iramw(preg, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class op44_adm implements Base {
        public void intFunc() {
            alu = iramr(preg) + iramr(AREG);
            iramw(preg, lobyte(alu));
            chkcz();
            iTick += 3;
        }
    }


    public class opc4_adcm implements Base {
        public void intFunc() {
            alu = iramr(preg) + iramr(AREG) + cflag;
            iramw(preg, lobyte(alu));
            chkcz();
            iTick += 3;
        }
    }


    public class op75_sbia implements Base {
        public void intFunc() {
            alu = iramr(AREG) - memr(pc);
            pc = incr16(pc);
            iramw(AREG, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class op71_sbim implements Base {
        public void intFunc() {
            alu = iramr(preg) - memr(pc);
            pc = incr16(pc);
            iramw(preg, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class op45_sbm implements Base {
        public void intFunc() {
            alu = iramr(preg) - iramr(AREG);
            iramw(preg, lobyte(alu));
            chkcz();
            iTick += 3;
        }
    }


    public class opc5_sbcm implements Base {
        public void intFunc() {
            alu = iramr(preg) - iramr(AREG) - cflag;
            iramw(preg, lobyte(alu));
            chkcz();
            iTick += 3;
        }
    }


    public class op42_inca implements Base {
        public void intFunc() {
            qreg = 2;
            alu = iramr(AREG) + 1;
            iramw(AREG, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class opc2_incb implements Base {
        public void intFunc() {
            qreg = 3;
            alu = iramr(BREG) + 1;
            iramw(BREG, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class op40_inci implements Base {
        public void intFunc() {
            qreg = 0;
            alu = iramr(IREG) + 1;
            iramw(IREG, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class opc0_incj implements Base {
        public void intFunc() {
            qreg = 1;
            alu = iramr(JREG) + 1;
            iramw(JREG, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class op48_inck implements Base {
        public void intFunc() {
            qreg = 8;
            alu = iramr(KREG) + 1;
            iramw(KREG, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class opc8_incl implements Base {
        public void intFunc() {
            qreg = 9;
            alu = iramr(LREG) + 1;
            iramw(LREG, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class op4a_incm implements Base {
        public void intFunc() {
            qreg = 10;
            alu = iramr(MREG) + 1;
            iramw(MREG, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class opca_incn implements Base {
        public void intFunc() {
            qreg = 11;
            alu = iramr(NREG) + 1;
            iramw(NREG, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class op50_incp implements Base {
        public void intFunc() {
            preg = incr7(preg);
            iTick += 2;
        }
    }


    public class op43_deca implements Base {
        public void intFunc() {
            qreg = 2;
            alu = iramr(AREG) - 1;
            iramw(AREG, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class opc3_decb implements Base {
        public void intFunc() {
            qreg = 3;
            alu = iramr(BREG) - 1;
            iramw(BREG, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class op41_deci implements Base {
        public void intFunc() {
            qreg = 0;
            alu = iramr(IREG) - 1;
            iramw(IREG, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class opc1_decj implements Base {
        public void intFunc() {
            qreg = 1;
            alu = iramr(JREG) - 1;
            iramw(JREG, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class op49_deck implements Base {
        public void intFunc() {
            qreg = 8;
            alu = iramr(KREG) - 1;
            iramw(KREG, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class opc9_decl implements Base {
        public void intFunc() {
            qreg = 9;
            alu = iramr(LREG) - 1;
            iramw(LREG, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class op4b_decm implements Base {
        public void intFunc() {
            qreg = 10;
            alu = iramr(MREG) - 1;
            iramw(MREG, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class opcb_decn implements Base {
        public void intFunc() {
            qreg = 11;
            alu = iramr(NREG) - 1;
            iramw(NREG, lobyte(alu));
            chkcz();
            iTick += 4;
        }
    }


    public class op51_decp implements Base {
        public void intFunc() {
            preg = decr7(preg);
            iTick += 2;
        }
    }


    public class op64_ania implements Base {
        public void intFunc() {
            alu = iramr(AREG) & memr(pc);
            pc = incr16(pc);
            iramw(AREG, lobyte(alu));
            if (alu == 0) {
                zflag = 1;
            }
            else {
                zflag = 0;
            }
            iTick += 4;
        }
    }


    public class op46_anma implements Base {
        public void intFunc() {
            alu = iramr(preg) & iramr(AREG);
            iramw(preg, lobyte(alu));
            if (alu == 0) {
                zflag = 1;
            }
            else {
                zflag = 0;
            }
            iTick += 3;
        }
    }


    public class op60_anim implements Base {
        public void intFunc() {
            alu = iramr(preg) & memr(pc);
            pc = incr16(pc);
            iramw(preg, lobyte(alu));
            if (alu == 0) {
                zflag = 1;
            }
            else {
                zflag = 0;
            }
            iTick += 4;
        }
    }


    public class opd4_anid implements Base {
        public void intFunc() {
            iramw(rreg - 1, memr(dp));
            memw(dp, memr(dp) & memr(pc));
            pc = incr16(pc);
            if (memr(dp) == 0) {
                zflag = 1;
            }
            else {
                zflag = 0;
            }
            iTick += 6;
        }
    }


    public class op65_oria implements Base {
        public void intFunc() {
            alu = iramr(AREG) | memr(pc);
            pc = incr16(pc);
            iramw(AREG, lobyte(alu));
            if (alu == 0) {
                zflag = 1;
            }
            else {
                zflag = 0;
            }
            iTick += 4;
        }
    }


    public class op47_orma implements Base {
        public void intFunc() {
            alu = iramr(preg) | iramr(AREG);
            iramw(preg, lobyte(alu));
            if (alu == 0) {
                zflag = 1;
            }
            else {
                zflag = 0;
            }
            iTick += 3;
        }
    }


    public class op61_orim implements Base {
        public void intFunc() {
            alu = iramr(preg) | memr(pc);
            pc = incr16(pc);
            iramw(preg, lobyte(alu));
            if (alu == 0) {
                zflag = 1;
            }
            else {
                zflag = 0;
            }
            iTick += 4;
        }
    }


    public class opd5_orid implements Base {
        public void intFunc() {
            iramw(rreg - 1, memr(dp));
            memw(dp, memr(dp) | memr(pc));
            pc = incr16(pc);
            if (memr(dp) == 0) {
                zflag = 1;
            }
            else {
                zflag = 0;
            }
            iTick += 6;
        }
    }


    public class op67_cpia implements Base {
        public void intFunc() {
            alu = iramr(AREG) - memr(pc);
            pc = incr16(pc);
            chkcz();
            iTick += 4;
        }
    }


    public class opc7_cpma implements Base {
        public void intFunc() {
            alu = iramr(preg) - iramr(AREG);
            chkcz();
            iTick += 3;
        }
    }


    public class op63_cpim implements Base {
        public void intFunc() {
            alu = iramr(preg) - memr(pc);
            pc = incr16(pc);
            chkcz();
            iTick += 4;
        }
    }


    /* 16bits operation */
    public class op14_adb implements Base {
        public void intFunc() {
            int xx;
            int pp;

            xx = hilo(iramr(preg + 1), iramr(preg));
            xx += hilo(iramr(BREG), iramr(AREG));
            iramw(preg, lobyte(xx));
            pp = incr7(preg);
            iramw(pp, hibyte(xx));
            preg = pp;
            if ((xx & 0x0000ffff) == 0) {
                zflag = 1;
            }
            else {
                zflag = 0;
            }
            if ((xx & 0xffff0000) != 0) {
                cflag = 1;
            }
            else {
                cflag = 0;
            }
            iTick += 5;
        }
    }


    public class op15_sbb implements Base {
        public void intFunc() {
            int xx;
            int pp;

            xx = hilo(iramr(preg + 1), iramr(preg));
            xx -= hilo(iramr(BREG), iramr(AREG));
            iramw(preg, lobyte(xx));
            pp = incr7(preg);
            iramw(pp, hibyte(xx));
            preg = pp;
            if ((xx & 0x0000ffff) == 0) {
                zflag = 1;
            }
            else {
                zflag = 0;
            }
            if ((xx & 0xffff0000) != 0) {
                cflag = 1;
            }
            else {
                cflag = 0;
            }
            iTick += 5;
        }
    }


    /* inc/dec load/store */
    public class op04_ix implements Base {
        public void intFunc() {
            qreg = 5;
            dp = incr16(hilo(iramr(XHREG), iramr(XLREG)));
            iramw(XHREG, hibyte(dp));
            iramw(XLREG, lobyte(dp));
            iTick += 6;
        }
    }


    public class op06_iy implements Base {
        public void intFunc() {
            qreg = 7;
            dp = incr16(hilo(iramr(YHREG), iramr(YLREG)));
            iramw(YHREG, hibyte(dp));
            iramw(YLREG, lobyte(dp));
            iTick += 6;
        }
    }


    public class op05_dx implements Base {
        public void intFunc() {
            qreg = 5;
            dp = decr16(hilo(iramr(XHREG), iramr(XLREG)));
            iramw(XHREG, hibyte(dp));
            iramw(XLREG, lobyte(dp));
            iTick += 6;
        }
    }


    public class op07_dy implements Base {
        public void intFunc() {
            qreg = 7;
            dp = decr16(hilo(iramr(YHREG), iramr(YLREG)));
            iramw(YHREG, hibyte(dp));
            iramw(YLREG, lobyte(dp));
            iTick += 6;
        }
    }


    public class op24_ixl implements Base {
        public void intFunc() {
            qreg = 5;
            dp = incr16(hilo(iramr(XHREG), iramr(XLREG)));
            iramw(AREG, memr(dp));
            iramw(XHREG, hibyte(dp));
            iramw(XLREG, lobyte(dp));
            iTick += 7;
        }
    }


    public class op26_iys implements Base {
        public void intFunc() {
            qreg = 7;
            dp = incr16(hilo(iramr(YHREG), iramr(YLREG)));
            memw(dp, iramr(AREG));
            iramw(YHREG, hibyte(dp));
            iramw(YLREG, lobyte(dp));
            iTick += 6;
        }
    }


    public class op25_dxl implements Base {
        public void intFunc() {
            qreg = 5;
            dp = decr16(hilo(iramr(XHREG), iramr(XLREG)));
            iramw(AREG, memr(dp));
            iramw(XHREG, hibyte(dp));
            iramw(XLREG, lobyte(dp));
            iTick += 7;
        }
    }


    public class op27_dys implements Base {
        public void intFunc() {
            qreg = 7;
            dp = decr16(hilo(iramr(YHREG), iramr(YLREG)));
            memw(dp, iramr(AREG));
            iramw(YHREG, hibyte(dp));
            iramw(YLREG, lobyte(dp));
            iTick += 6;
        }
    }


    /* rotate / shift */
    public class opd2_sr implements Base {
        public void intFunc() {
            alu = (cflag << 8) | iramr(AREG);
            iramw(AREG, lobyte(alu >> 1));
            if ((alu & 1) != 0) {
                cflag = 1;
            }
            else {
                cflag = 0;
            }
            iTick += 2;
        }
    }


    public class op5a_sl implements Base {
        public void intFunc() {
            alu = (iramr(AREG) << 1) | cflag;
            iramw(AREG, lobyte(alu));
            if ((alu & 0xff00) != 0) {
                cflag = 1;
            }
            else {
                cflag = 0;
            }
            iTick += 2;
        }
    }


    public class op1c_srw implements Base {
        public void intFunc() {
            iTick += (iramr(IREG) + 4);
            dreg = incr8(iramr(IREG));
            alu = 0;
            do {
                alu = (alu << 8) | iramr(preg);
                iramw(preg, lobyte(alu >> 4));
                preg = incr7(preg);
                dreg = decr8(dreg);
            } while (dreg != 0);
        }
    }


    public class op1d_slw implements Base {
        public void intFunc() {
            iTick += (iramr(IREG) + 4);
            dreg = incr8(iramr(IREG));
            alu = 0;
            do {
                alu =  (iramr(preg) << 8) | (alu >> 8);
                iramw(preg, lobyte(alu >> 4));
                preg = decr7(preg);
                dreg = decr8(dreg);
            } while (dreg != 0);
        }
    }


    /* bit test */
    public class op66_tsia implements Base {
        public void intFunc() {
            if ((iramr(AREG) & memr(pc)) == 0) {
                zflag = 1;
            }
            else {
                zflag = 0;
            }
            pc = incr16(pc);
            iTick += 4;
        }
    }


    public class op62_tsim implements Base {
        public void intFunc() {
            if ((iramr(preg) & memr(pc)) == 0) {
                zflag = 1;
            }
            else {
                zflag = 0;
            }
            pc = incr16(pc);
            iTick += 4;
        }
    }


    public class opd6_tsid implements Base {
        public void intFunc() {
            iramw(rreg - 1, memr(dp));
            if ((memr(dp) & memr(pc)) == 0) {
                zflag = 1;
            }
            else {
                zflag = 0;
            }
            pc = incr16(pc);
            iTick += 6;
        }
    }


    public class op4f_cup implements Base {
        public void intFunc() {
            iTick += (iramr(IREG) * 4);
            dreg = incr8(iramr(IREG));
            zflag = 0;
            if (xin == 0) {
                do {
                    preg = incr7(preg);
                    dreg = decr8(dreg);
                } while ((xin == 0) && (dreg != 0x00ff));
                if (xin == 0) {
                    zflag = 1;
                }
            }
        }
    }


    public class op6f_cdn implements Base {
        public void intFunc() {
            iTick += (iramr(IREG) * 4);
            dreg = incr8(iramr(IREG));
            zflag = 0;
            if (xin != 0) {
                do {
                    preg = incr7(preg);
                    dreg = decr8(dreg);
                } while ((xin != 0) && (dreg != 0xff));
                if (xin != 0) {
                    zflag = 1;
                }
            }
        }
    }


    /* BCD operation */
    public int deciadd(int p, int q) {
        int ans = 0;
/*
        ans = (p & 0x000f) + (q & 0x000f);
        if (ans > 0x0009) {
            ans += 6;
        }
        ans += (p & 0x00f0) + (q & 0x00f0);
        if ((ans & 0xfff0) > 0x0090) {
            ans += 0x60;
        }
        return(ans & 0x01ff);
*/
        // 10進でない値の場合は無効にする
        if ((p & 0x000f) > 9) p &= 0x00f0;
        if ((p & 0x00f0) > 0x90) p &= 0x000f;
        if ((q & 0x000f) > 9) q &= 0x00f0;
        if ((q & 0x00f0) > 0x90) q &= 0x000f;

        ans = (p & 0x000f) + (q & 0x000f);
        if (ans >= 0x0a) {
            ans -= 0x0a;
            ans += (p & 0x00f0) + (q & 0x00f0) + 0x10;
        } else {
            ans += (p & 0x00f0) + (q & 0x00f0);
        }
        if (ans >= 0xa0) {
            ans = ans - 0xa0 + 0x100;
        }
        return(ans & 0x1ff);

    }


    public int decisub(int p, int q) {
        int ans = 0;
/*
        Log.w("decisub", String.format("p=%x, q=%x", p, q));
        if ((p & 0x000f) > 9) p &= 0x00f0;
        if ((p & 0x00f0) > 0x90) p &= 0x000f;
        if ((q & 0x000f) > 9) q &= 0x00f0;
        if ((q & 0x00f0) > 0x90) q &= 0x000f;

        ans = ((p & 0x000f) - (q & 0x000f)) & 0xffff;
        if (ans > 0x0009) {
            ans -= 0x06;
        }
        ans = (ans + ((p & 0x00f0) - (q & 0x00f0))) & 0xffff;
        if ((ans & 0xfff0) > 0x0090) {
            ans -= 0x60;
        }
        Log.w("decisub", String.format("ans=%x", ans&0x1ff));
        return(ans & 0x1ff);
*/

        //Log.w("decisub", String.format("p=%x, q=%x", p, q));
        // 10進でない値の場合は無効にする
        // 12xx系はプログラム入力時の行番号比較で、Exxxをそのまま入れてくるので上位4bitの部分をマスクしてやらないとうまく処理されない
        // 逆に12xx系以外でもA-Fが入っている時がある。この時の処理は不明だが、とりあえずマスク処理しておく（様子見）
        if ((p & 0x000f) > 9) p &= 0x00f0;
        if ((p & 0x00f0) > 0x90) p &= 0x000f;
        if ((q & 0x000f) > 9) q &= 0x00f0;
        if ((q & 0x00f0) > 0x90) q &= 0x000f;

        ans = (p & 0x000f) - (q & 0x000f);
        if (ans < 0) {
            ans += 0x0a;
            ans += (p & 0x00f0) - (q & 0x00f0) -0x10;
        } else {
            ans += (p & 0x00f0) - (q & 0x00f0);
        }
        if (ans < 0) {
            ans = ans + 0xa0 + 0x100;
        }
        //Log.w("decisub", String.format("ans=%x", ans&0x1ff));
        return (ans & 0x1ff);

    }


    public class op0c_adn implements Base {
        public void intFunc() {
            iTick += (iramr(IREG) * 3 + 4);
            dreg = incr8(iramr(IREG));
            alu = iramr(AREG);
            zflag = 1;
            do {
                alu = deciadd(iramr(preg), alu);
                iramw(preg, lobyte(alu));
                if ((zflag != 0) && (iramr(preg) == 0)) {
                    zflag = 1;
                }
                else {
                    zflag = 0;
                }
                preg = decr7(preg);
                dreg = decr8(dreg);
                alu >>= 8;
            } while (dreg != 0);
            if (alu != 0) {
                cflag = 1;
            }
            else {
                cflag = 0;
            }
        }
    }


    public class op0d_sbn implements Base {
        public void intFunc() {
            iTick += (iramr(IREG) * 3 + 4);
            dreg = incr8(iramr(IREG));
            alu = iramr(AREG);
            zflag = 1;
            do {
                alu = decisub(iramr(preg), alu);
                iramw(preg, lobyte(alu));
                if ((zflag != 0) && (iramr(preg) == 0)) {
                    zflag = 1;
                }
                else {
                    zflag = 0;
                }
                preg = decr7(preg);
                dreg = decr8(dreg);
                alu >>= 8;
            } while (dreg != 0);
            if (alu != 0) {
                cflag = 1;
            }
            else {
                cflag = 0;
            }
        }
    }


    public class op0e_adw implements Base {
        public void intFunc() {
            iTick += (iramr(IREG) * 3 + 4);
            dreg = incr8(iramr(IREG));
            alu = 0;
            zflag = 1;
            do {
                alu = deciadd(iramr(preg), alu);
                alu = (alu & 0x0100) | deciadd(lobyte(alu), iramr(qreg));
                iramw(preg, lobyte(alu));
                if ((zflag != 0) && (iramr(preg) == 0)) {
                    zflag = 1;
                }
                else {
                    zflag = 0;
                }
                preg = decr7(preg);
                qreg = decr7(qreg);
                dreg = decr8(dreg);
                alu >>= 8;
            } while (dreg != 0);
            if (alu != 0) {
                cflag = 1;
            }
            else {
                cflag = 0;
            }
        }
    }


    public class op0f_sbw implements Base {
        public void intFunc() {
            iTick += (iramr(IREG) * 3 + 4);
            dreg = incr8(iramr(IREG));
            alu = 0;
            zflag = 1;
            do {
                alu = decisub(iramr(preg), alu);
                alu = (alu & 0x0100) | decisub(lobyte(alu), iramr(qreg));
                iramw(preg, lobyte(alu));
                if ((zflag != 0) && (iramr(preg) == 0)) {
                    zflag = 1;
                }
                else {
                    zflag = 0;
                }
                preg = decr7(preg);
                qreg = decr7(qreg);
                dreg = decr8(dreg);
                alu >>= 8;
            } while (dreg != 0);
            if (alu != 0) {
                cflag = 1;
            }
            else {
                cflag = 0;
            }
        }
    }


    /* jumps */
    public class op2c_jrp implements Base {
        public void intFunc() {
            iramw(rreg - 1, memr(pc));
            pc = add16(pc, memr(pc));
            iTick += 7;
        }
    }


    public class op2d_jrm implements Base {
        public void intFunc() {
            iramw(rreg - 1, memr(pc));
            pc = sub16(pc, memr(pc));
            iTick += 7;
        }
    }


    public class op38_jrzp implements Base {
        public void intFunc() {
            iramw(rreg - 1, memr(pc));
            if (zflag != 0) {
                pc = add16(pc, memr(pc));
                iTick += 7;
            }
            else {
                pc = incr16(pc);
                iTick += 4;
            }
        }
    }


    public class op39_jrzm implements Base {
        public void intFunc() {
            iramw(rreg - 1, memr(pc));
            if (zflag != 0) {
                pc = sub16(pc, memr(pc));
                iTick += 7;
            }
            else {
                pc = incr16(pc);
                iTick += 4;
            }
        }
    }


    public class op28_jrnzp implements Base {
        public void intFunc() {
            iramw(rreg - 1, memr(pc));
            if (zflag == 0) {
                pc = add16(pc, memr(pc));
                iTick += 7;
            }
            else {
                pc = incr16(pc);
                iTick += 4;
            }
        }
    }


    public class op29_jrnzm implements Base {
        public void intFunc() {
            iramw(rreg - 1, memr(pc));
            if (zflag == 0) {
                pc = sub16(pc, memr(pc));
                iTick += 7;
            }
            else {
                pc = incr16(pc);
                iTick += 4;
            }
        }
    }


    public class op3a_jrcp implements Base {
        public void intFunc() {
            iramw(rreg - 1, memr(pc));
            if (cflag != 0) {
                pc = add16(pc, memr(pc));
                iTick += 7;
            }
            else {
                pc = incr16(pc);
                iTick += 4;
            }
        }
    }


    public class op3b_jrcm implements Base {
        public void intFunc() {
            iramw(rreg - 1, memr(pc));
            if (cflag != 0) {
                pc = sub16(pc, memr(pc));
                iTick += 7;
            }
            else {
                pc = incr16(pc);
                iTick += 4;
            }
        }
    }


    public class op2a_jrncp implements Base {
        public void intFunc() {
            iramw(rreg - 1, memr(pc));
            if (cflag == 0) {
                pc = add16(pc, memr(pc));
                iTick += 7;
            }
            else {
                pc = incr16(pc);
                iTick += 4;
            }
        }
    }


    public class op2b_jrncm implements Base {
        public void intFunc() {
            iramw(rreg - 1, memr(pc));
            if (cflag == 0) {
                pc = sub16(pc, memr(pc));
                iTick += 7;
            }
            else {
                pc = incr16(pc);
                iTick += 4;
            }
        }
    }


    public class op79_jp implements Base {
        public void intFunc() {
            pc = hilo(memr(pc), memr(pc + 1));
            iTick += 6;
        }
    }



    public class op7e_jpz implements Base {
        public void intFunc() {
            if (zflag != 0) {
                pc = hilo(memr(pc), memr(pc + 1));
            }
            else {
                pc = add16(pc, 2);
            }
            iTick += 6;
        }
    }

    public class op7c_jpnz implements Base {
        public void intFunc() {
            if (zflag == 0) {
                pc = hilo(memr(pc), memr(pc + 1));
            }
            else {
                pc = add16(pc, 2);
            }
            iTick += 6;
        }
    }


    public class op7f_jpc implements Base {
        public void intFunc() {
            if (cflag != 0) {
                pc = hilo(memr(pc), memr(pc + 1));
            }
            else {
                pc = add16(pc, 2);
            }
            iTick += 6;
        }
    }


    public class op7d_jpnc implements Base {
        public void intFunc() {
            if (cflag == 0) {
                pc = hilo(memr(pc), memr(pc + 1));
            }
            else {
                pc = add16(pc, 2);
            }
            iTick += 6;
        }
    }


    public class op2f_loop implements Base {
        public void intFunc() {
            alu = iramr(rreg);
            if (alu != 0) {
                pc = sub16(pc, memr(pc));
            } else {
                pc = incr16(pc);
            }
            --alu;
            chkcz();
            iramw(rreg, lobyte(alu));
            if (lobyte(alu) == 0xff) {
                rreg = incr7(rreg);
                iTick += 7;
            } else {
                iTick += 10;
            }

/*
            alu = iramr(rreg);
            --alu;
            iramw(rreg, lobyte(alu));
            chkcz();
            if (cflag != 0) {
                pc = incr16(pc);
                iTick += 7;
            }
            else {
                pc = sub16(pc, memr(pc));
                iTick += 10;
            }
            Log.w("CORE", "loop");
*/
        }
    }


    public class op7a_case1 implements Base {
        public void intFunc() {
            dreg = memr(pc);
            pc = incr16(pc);
            rreg = decr8(rreg);
            iramw(rreg, memr(pc));
            pc = incr16(pc);
            rreg = decr8(rreg);
            iramw(rreg, memr(pc));
            pc = incr16(pc);
            iTick += 9;
        }
    }


    public class op69_case2 implements Base {
        public void intFunc() {
            int n = dreg;
            int i = 1;
            do {
                pc = incr16(pc);
                if (iramr(AREG) == memr(pc - 1)) {
                    pc = hilo(memr(pc), memr(pc + 1));
                    iTick += (i * 7 + 2);
                    break;
                }
                else {
                    pc = add16(pc, 2);
                }
                dreg = decr8(dreg);
                i++;
            } while (dreg != 0);
            if (dreg == 0) {
                pc = hilo(memr(pc), memr(pc + 1));
                iTick += (n * 7 + 7);
            }
        }
    }


    public class opex_cal implements Base {
        public void intFunc() {
            pc = incr16(pc);
            rreg = decr8(rreg);
            iramw(rreg, hibyte(pc));
            rreg = decr8(rreg);
            iramw(rreg, lobyte(pc));
            pc = hilo(opcode & 0x001f, memr(pc - 1));
            iTick += 7;
        }
    }


    public class op78_call implements Base {
        public void intFunc() {
            pc = add16(pc, 2);
            rreg = decr8(rreg);
            iramw(rreg, hibyte(pc));
            rreg = decr8(rreg);
            iramw(rreg, lobyte(pc));
            pc = hilo(memr(pc - 2), memr(pc - 1));
            iTick += 8;
        }
    }


    public class op37_rtn implements Base {
        public void intFunc() {
            pc = hilo(iramr(rreg + 1), iramr(rreg));
            rreg = add8(rreg, 2);
            iTick += 4;
        }
    }


    public class op5d_outa implements Base {
        public void intFunc() {
            int newia;
            //int c=0;

            newia = iramr(IAPORT);
            //if (newia != 0 && newia != iaval) {
            //    c = kb.getBuf();
            //    if (c != 0) {
            //        Log.w("LOG", "inKey=" + c + " iaval=" + newia);
            //        kb.keyscan(c);
            //    }
            //}

            /*
            //Log.w("LOG", "newia="+newia);
            if (true || (newia != 0) && (newia != iaval)) {
                iacnt = incr16(iacnt);
                //Log.w("LOG", "outa!!!");
                //c=kb.getBuf();
                if (keychar == 0) {
                    c = kb.getBuf();
                    if (c != 0) {
                        keychar = c;
                        Log.w("LOG", "inKey=" + c + " iaval=" + newia);
                        kb.keyscan(c);
                        //keyBuf = 0;
                        iacnt = 0;
                    }

                }

                if (20 < iacnt) {
                    kb.keyclear();
                    //Log.w("LOG", "keyclear");
                }
                if (40 < iacnt) {
                    keychar = 0;
                    //Log.w("LOG", "keychar = 0");
                }
                if (64 < iacnt) {
                    iacnt = 0;
                    //Log.w("LOG", "iacnt = 0");
                }

            }
            */
            qreg = IAPORT&0x7f;
            iaval = iramr(IAPORT);
            iTick += 2;
        }
    }


    public class opdd_outb implements Base {
        public void intFunc() {
            qreg= IBPORT;
            ibval = iramr(IBPORT);
            if (ibval != 0) {
                //Log.w("LOG", "outb!!! ibval="+ibval);
            }
            iTick += 2;
        }
    }

    protected void outf() {
        ;
    }

    public class op5f_outf implements Base {
        public void intFunc() {
            qreg= FOPORT;
            foval = iramr(FOPORT);
            outf();
            iTick += 3;
        }
    }


    public class opdf_outc implements Base {
        public void intFunc() {
            qreg = CTRLPORT&0x7f;
            ctrlval = iramr(CTRLPORT);

            if (disp_on != (ctrlval & 1)) {
                disp_on = ctrlval & 1;
                listener.refreshScreen();
            }
            if ((ctrlval & 2) != 0) {
                ticks = 0;
                ticks2 = 0;
                div500 = 0;
                div2 = 0;

                //div2ms = 0;
                //div500ms = 0;
            }
            if ((ctrlval & 8) != 0) {
                power_on = 0;
                //Log.w("LOG", "power on");
            } else {
                power_on = 1;
                //Log.w("LOG", "power off");
            }
            if ((ctrlval & 0x30) == 0x30) {
                if (beep_enable) {
                    beep_on = true;
                    //beep.beep5khz();
                }
            } else if ((ctrlval & 0x30) == 0x20) {
                if (beep_enable) {
                    beep_on = false;
                    //beep.beepStop();
                }
            } else if ((ctrlval & 0x30) == 0) {
                if (beep_enable) {
                    beep_on = false;
                    //beep.beepStop();
                }
            }
            iTick += 2;
        }
    }


    protected int bit(int ii) {
        int jj;

        for (jj = 0; jj < 8; jj++) {
            if ((ii & 1) != 0) {
                break;
            }
            ii >>= 1;
        }
        return(jj);
    }


    protected void ina() {
        ;   // 継承クラス側で実装する
    }

    public class op4c_ina implements Base {
        public void intFunc() {
            ina();
            iTick += 2;
        }
    }


    protected void inb() {

        iramw(AREG, 0x00);
        if (iramr(AREG) == 0) {
            zflag = 1;
        }
        else {
            zflag = 0;
        }
    }

    public class opcc_inb implements Base {
        public void intFunc() {
            inb();
            iTick += 2;
        }
    }


    public class opd0_sc implements Base {
        public void intFunc() {
            cflag = 1;
            zflag = 1;
            iTick += 2;
        }
    }


    public class opd1_rc implements Base {
        public void intFunc() {
            cflag = 0;
            zflag = 1;
            iTick += 2;
        }
    }


    public class op4d_nopw implements Base {
        public void intFunc() {
	        /* no operation */
            iTick += 2;
        }
    }

    public class opce_nopt implements Base {
        public void intFunc() {
	        /* no operation */
            iTick += 3;
        }
    }


    public class op4e_wait implements Base {
        public void intFunc() {
            iTick += (memr(pc) * 6) / 4;    // 何故か長すぎるので仮で1/4
            pc = incr16(pc);
        }
    }


    public class op6b_test implements Base {
        public void intFunc() {
            testport = 0;

	        if (div500 != 0) {
                div500 = 0;
		        testport |= 1;
        	}
            if (div2 != 0) {
                div2 = 0;
                testport |= 2;
                // div2はメインループのカウントで適当に作っているタイマなので不正確
            }

            if (kon_cnt > 0) {
                kon_cnt--;
                testport |= 8;
            }
            if ((testport & memr(pc)) == 0) {
                zflag = 1;
            }
            else {
                zflag = 0;
            }
            pc = incr16(pc);
            iTick += 4;
        }
    }


    /* undefined code */
    public class opxx_undef implements Base {
        public void intFunc() {
            Log.w("LOG", "Undefined code: " + hex2(opcode) + " at " + hex4(pc));
        }
    }


    public class op3f_escape implements Base {
        public void intFunc() {
            ;
        }
    }

    public Base op80_lp = new op80_lp();
    public Base opex_cal = new opex_cal();
    //public Base opxx_undef = new opxx_undef();
    /* */
    public Base opfunc[] = {
            new op00_lii(),	    new op01_lij(),	    new op02_lia(),	    new op03_lib(),
            new op04_ix(),	    new op05_dx(),	    new op06_iy(),	    new op07_dy(),
            new op08_mvw(),	    new op09_exw(),	    new op0a_mvb(),	    new op0b_exb(),
            new op0c_adn(),	    new op0d_sbn(),	    new op0e_adw(),	    new op0f_sbw(),
	/* 1x */
            new op10_lidp(),	new op11_lidl(),	new op12_lip(),	    new op13_liq(),
            new op14_adb(),	    new op15_sbb(),	    new opxx_undef(),	new opxx_undef(),
            new op18_mvwd(),	new op19_exwd(),	new op1a_mvbd(),	new op1b_exbd(),
            new op1c_srw(),	    new op1d_slw(),	    new op1e_film(),	new op1f_fild(),
	/* 2x */
            new op20_ldp(),	    new op21_ldq(),	    new op22_ldr(),	    new op23_clra(),
            new op24_ixl(),	    new op25_dxl(),	    new op26_iys(),	    new op27_dys(),
            new op28_jrnzp(),	new op29_jrnzm(),	new op2a_jrncp(),	new op2b_jrncm(),
            new op2c_jrp(),	    new op2d_jrm(),	    new opxx_undef(),	new op2f_loop(),
	/* 3x */
            new op30_stp(),	    new op31_stq(),	    new op32_str(),	    new opce_nopt(),
            new op34_push(),	new op35_mvwp(),	new opxx_undef(),	new op37_rtn(),
            new op38_jrzp(),	new op39_jrzm(),	new op3a_jrcp(),	new op3b_jrcm(),
            new opxx_undef(),	new opxx_undef(),	new opxx_undef(),	new opxx_undef(),
	/* 4x */
            new op40_inci(),	new op41_deci(),	new op42_inca(),	new op43_deca(),
            new op44_adm(),	    new op45_sbm(),	    new op46_anma(),	new op47_orma(),
            new op48_inck(),	new op49_deck(),	new op4a_incm(),	new op4b_decm(),
            new op4c_ina(),	    new op4d_nopw(),	new op4e_wait(),	new op4f_cup(),
	/* 5x */
            new op50_incp(),	new op51_decp(),	new op52_std(),	    new op53_mvdm(),
            new op54_mvmp(),	new op55_mvmd(),	new op56_ldpc(),	new op57_ldd(),
            new op58_swp(),	    new op59_ldm(),	    new op5a_sl(),	    new op5b_pop(),
            new opxx_undef(),	new op5d_outa(),	new opxx_undef(),	new op5f_outf(),
	/* 6x */
            new op60_anim(),	new op61_orim(),	new op62_tsim(),	new op63_cpim(),
            new op64_ania(),	new op65_oria(),	new op66_tsia(),	new op67_cpia(),
            new opce_nopt(),    new op69_case2(),	new opce_nopt(),	new op6b_test(),
            new opxx_undef(),	new opxx_undef(),	new opxx_undef(),	new op6f_cdn(),
	/* 7x */
            new op70_adim(),	new op71_sbim(),	new op72_rz(),	    new op72_rz(),
            new op74_adia(),	new op75_sbia(),	new op72_rz(),	    new op72_rz(),
            new op78_call(),	new op79_jp(),	    new op7a_case1(),	new opxx_undef(),
            new op7c_jpnz(),	new op7d_jpnc(),	new op7e_jpz(),	    new op7f_jpc(),
	/* 8x */
            op80_lp, op80_lp, op80_lp, op80_lp,
            op80_lp, op80_lp, op80_lp, op80_lp,
            op80_lp, op80_lp, op80_lp, op80_lp,
            op80_lp, op80_lp, op80_lp, op80_lp,
	/* 9x */
            op80_lp, op80_lp, op80_lp, op80_lp,
            op80_lp, op80_lp, op80_lp, op80_lp,
            op80_lp, op80_lp, op80_lp, op80_lp,
            op80_lp, op80_lp, op80_lp, op80_lp,
	/* Ax */
            op80_lp, op80_lp, op80_lp, op80_lp,
            op80_lp, op80_lp, op80_lp, op80_lp,
            op80_lp, op80_lp, op80_lp, op80_lp,
            op80_lp, op80_lp, op80_lp, op80_lp,
	/* Bx */
            op80_lp, op80_lp, op80_lp, op80_lp,
            op80_lp, op80_lp, op80_lp, op80_lp,
            op80_lp, op80_lp, op80_lp, op80_lp,
            op80_lp, op80_lp, op80_lp, op80_lp,
	/* Cx */
            new opc0_incj(),	new opc1_decj(),	new opc2_incb(),	new opc3_decb(),
            new opc4_adcm(),	new opc5_sbcm(),	new opc6_tsma(),	new opc7_cpma(),
            new opc8_incl(),	new opc9_decl(),	new opca_incn(),	new opcb_decn(),
            new opcc_inb(),	    new opce_nopt(),	new op4d_nopw(),	new opxx_undef(),
	/* Dx */
            new opd0_sc(),	    new opd1_rc(),	    new opd2_sr(),	    new op4d_nopw(),
            new opd4_anid(),	new opd5_orid(),	new opd6_tsid(),	new opd7_sz(),
            new opd8_leave(),	new op4d_nopw(),	new opda_exab(),	new opdb_exam(),
            new opxx_undef(),	new opdd_outb(),	new opxx_undef(),	new opdf_outc(),
	/* Ex */
            opex_cal, opex_cal, opex_cal, opex_cal,
            opex_cal, opex_cal, opex_cal, opex_cal,
            opex_cal, opex_cal, opex_cal, opex_cal,
            opex_cal, opex_cal, opex_cal, opex_cal,
	/* Fx */
            opex_cal, opex_cal, opex_cal, opex_cal,
            opex_cal, opex_cal, opex_cal, opex_cal,
            opex_cal, opex_cal, opex_cal, opex_cal,
            opex_cal, opex_cal, opex_cal, opex_cal
    };

    public final String instractionCode[] = {
            // 0x00",
            "LII  ", "LIJ  ", "LIA  ", "LIB  ", "IX   ", "DX   ", "IY   ", "DY   ",
            "MVW  ", "EXW  ", "MVB  ", "EXB  ", "ADN  ", "SBN  ", "ADW  ", "SBW  ",
            // 0x10",
            "LIDP ", "LIDL ", "LIP  ", "LIQ  ", "ADB  ", "SBB  ", "Panic", "Panic",
            "MVWD ", "EXWD ", "MVBD ", "EXBD ", "SRW  ", "SLW  ", "FILM ", "FILD ",
            // 0x20",
            "LDP  ", "LDQ  ", "LDR  ", "CLRA ", "IXL  ", "DXL  ", "IYS  ", "DYS  ",
            "JRNZP", "JRNZM", "JRNCP", "JRNCM", "JRP  ", "JRM  ", "Panic", "LOOP ",
            // 0x30",
            "STP  ", "STQ  ", "STR  ", "NOPT ", "PUSH ", "MVWP ", "Panic", "RTN  ",
            "JRZP ", "JRZM ", "JRCP ", "JRCM ", "Panic", "Panic", "Panic", "Panic",
            // 0x40",
            "INCI ", "DECI ", "INCA ", "DECA ", "ADM  ", "SBM  ", "ANMA ", "ORMA ",
            "INCK ", "DECK ", "INCM ", "DECM ", "INA  ", "NOPW ", "WAIT ", "CUP  ",
            // 0x50",
            "INCP ", "DECP ", "STD  ", "MVDM ", "MVMP ", "MVMD ", "LDPC ", "LDD  ",
            "SWP  ", "LDM  ", "SL   ", "POP  ", "Panic", "OUTA ", "Panic", "OUTF ",
            // 0x60
            "ANIM ", "ORIM ", "TSIM ", "CPIM ", "ANIA ", "ORIA ", "TSIA ", "CPIA ",
            "NOPT ", "CASE2", "NOPT ", "TEST ", "Panic", "Panic", "Panic", "CDN  ",
            // 0x70
            "ADIM ", "SBIM ", "Panic", "Panic", "ADIA ", "SBIA ", "Panic", "Panic",
            "CALL ", "JP   ", "CASE1", "Panic", "JPNZ ", "JPNC ", "JPZ  ", "JPC  ",
            // 0x80 - 0xaf
            "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ",
            "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ",
            "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ",
            "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ",
            "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ",
            "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ",
            // 0xb0
            "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ",
            "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ", "LP   ",
            // 0xc0
            "INCJ ", "DECJ ", "INCB ", "DECB ", "ADCM ", "SBCM ", "TSMA ", "CPMA ",
            "INCL ", "DECL ", "INCN ", "DECN ", "INB  ", "NOPW ", "NOPT ", "Panic",
            // 0xd0
            "SC   ", "RC   ", "SR   ", "NOPW ", "ANID ", "ORID ", "TSID ", "Panic",
            "LEAVE", "NOPW ", "EXAB ", "EXAM ", "Panic", "OUTB ", "Panic", "OUTC ",
            // 0xe0 - 0xff
            "CAL  ", "CAL  ", "CAL  ", "CAL  ", "CAL  ", "CAL  ", "CAL  ", "CAL  ",
            "CAL  ", "CAL  ", "CAL  ", "CAL  ", "CAL  ", "CAL  ", "CAL  ", "CAL  ",
            "CAL  ", "CAL  ", "CAL  ", "CAL  ", "CAL  ", "CAL  ", "CAL  ", "CAL  ",
            "CAL  ", "CAL  ", "CAL  ", "CAL  ", "CAL  ", "CAL  ", "CAL  ", "CAL  "
    };

}
