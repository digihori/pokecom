package tk.horiuchi.pokecom;

import java.io.Serializable;

/**
 * Created by yoshimine on 2017/09/23.
 */

public class Sc61860params implements Serializable {
    public int id;

    public boolean prog_mode_1245;
    public int prog_mode_1251;
    /* SC61860 registers */
    public int pc;
    public int current_pc;
    public int opcode;
    public int dp;
    public int preg;
    public int qreg;
    public int rreg;
    public int dreg;

    public int alu;
    public int cflag;
    public int zflag;
    public int xin;
    public int ticks;
    public int ticks2;
    public int div500;
    public int div2;

    public int power_on;
    public int disp_on;
    public static int kon;
    public static int kon_cnt;

    public int iaval;
    public int ibval;
    public int foval;
    public int ctrlval;
    public int testport;

    public final int IRAMSIZ = (0x005f + 1);
    public int[] iram = new int[256];

    public final int MAINRAMSIZ = (0xffff + 1);
    public int[] mainram = new int[MAINRAMSIZ];

    public byte[] digi = new byte[600]; // 1350の値
    public byte[] state = new byte[4];


    public Sc61860params() {
        //iram = new int[256];
        //mainram = new int[MAINRAMSIZ];
    }
}
