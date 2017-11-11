package tk.horiuchi.pokecom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static tk.horiuchi.pokecom.MainActivity.dpdx;


/**
 * Created by yoshimine on 2017/07/29.
 */

public class MainLoop1470 extends MainLoopBase {
    private final int digit=5*24*2;
    public static byte digi[];
    public static byte state[]={0,0,0,0};

    private static final byte dispBATT  =(1<<0);
    private static final byte dispK     =(1<<1);
    private static final byte dispHYP   =(1<<2);
    private static final byte dispAER   =(1<<3);
    private static final byte dispPRO   =(1<<4);
    private static final byte dispRUN   =(1<<5);
    private static final byte dispCAL   =(1<<6);

    private static final byte dispBUSY  =(1<<0);
    private static final byte dispDEF   =(1<<1);
    private static final byte dispSHIFT =(1<<2);
    private static final byte dispDBL   =(1<<3);


    private static final byte dispE      =(1<<0);
    private static final byte dispM      =(1<<1);
    private static final byte dispRAD    =(1<<2);
    private static final byte dispG      =(1<<3);
    private static final byte dispMATRIX =(1<<4);
    private static final byte dispSTAT   =(1<<5);
    private static final byte dispPRINT  =(1<<6);

    private static final byte dispKANA  =(1<<0);
    private static final byte dispS     =(1<<1);
    private static final byte dispSML   =(1<<2);
    private static final byte dispDE    =(1<<3);


    public MainLoop1470(Context context, SurfaceView sv) {
        super(context, sv);

        digi = new byte[digit];
        for (int i=0; i<digit; i++) {
            digi[i] = 0;
        }
        Log.w("LOG", "digi is created.");

        // cpuオブジェクトの生成
        sc = new Sc61860_1470(context);
        sc.setListener(this);
        sc.CpuReset();

    }

    @Override
    protected void doDraw(SurfaceHolder holder) {
        //描画処理を開始
        Canvas c = holder.lockCanvas();

        if (c != null) {
            c.scale(dpdx, dpdx);

            int x_org, y_org, stp, d_row, d_col;
            int x, y;
            int i, j, k, l;
            Paint p = new Paint();

            x_org = 8;
            y_org = 22;
            stp = 3;
            d_row=7;
            d_col=5;
            c.drawColor(0xFFEEFFFF);

            p.setStyle(Paint.Style.FILL);

            for (l =0; l < 2; l++) {
                for (k = 0, x = x_org; k < 24; k++) {
                    for (j = 0; j < d_col; j++, x += stp) {
                        for (i = 0, y = y_org + (d_row * stp) * l; i < d_row; i++, y += stp) {
                            if ((digi[(24*l + k) * d_col + j] & 0x01 << i) != 0) {
                                p.setColor(Color.DKGRAY);
                            } else {
                                p.setColor(Color.LTGRAY);
                            }
                            c.drawRect(x, y, x + stp, y + stp, p);
                        }
                    }
                    x += stp;
                }
                y_org += stp;
            }

            // シンボル表示
            if ((state[0] & dispCAL) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            p.setTextSize(12);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            c.drawText("CAL", 12, 80, p);
            if ((state[0] & dispAER) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("AER", 40, 80, p);
            if ((state[0] & dispRUN) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("RUN", 68, 80, p);
            if ((state[0] & dispPRO) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("PRO", 100, 80, p);

            if ((state[2] & dispMATRIX) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("MATRIX", 300, 80, p);
            if ((state[2] & dispSTAT) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("STAT", 352, 80, p);
            if ((state[2] & dispPRINT) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("PRINT", 388, 80, p);



            if ((state[1] & dispBUSY) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("BUSY", 12, 16, p);
            if ((state[1] & dispDBL) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("DBL", 48, 16, p);
            if ((state[1] & dispDEF) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("DEF", 76, 16, p);
            if ((state[1] & dispSHIFT) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("SHIFT", 104, 16, p);


            if ((state[3] & dispKANA) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("カナ", 160, 16, p);
            if ((state[3] & dispS) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("小", 188, 16, p);
            if ((state[3] & dispSML) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("SML", 204, 16, p);

            if ((state[3] & dispDE) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("DE", 248, 16, p);
            if ((state[2] & dispG) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("G", 266, 16, p);
            if ((state[2] & dispRAD) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("RAD", 276, 16, p);


            if ((state[0] & dispHYP) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("HYP", 320, 16, p);
            if ((state[0] & dispK) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("( )", 352, 16, p);
            if ((state[2] & dispM) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("M", 368, 16, p);
            if ((state[2] & dispE) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("E", 384, 16, p);
            if ((state[0] & dispBATT) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("BATT", 400, 16, p);


            //描画処理を終了
            holder.unlockCanvasAndPost(c);
        }


    }

}
