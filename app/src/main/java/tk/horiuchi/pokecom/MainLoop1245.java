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
import static tk.horiuchi.pokecom.Sc61860Base.disp_on;
import static tk.horiuchi.pokecom.SubActivity1245.prog_mode;

/**
 * Created by yoshimine on 2017/07/29.
 */

public class MainLoop1245 extends MainLoopBase {
    private final int digit=5*16;
    public static byte digi[];
    public static byte state[]={0,0};

    private static final byte dispDEF   =(1<<0);
    private static final byte dispP     =(1<<1);
    private static final byte dispG     =(1<<2);
    private static final byte dispDE    =(1<<3);

    private static final byte dispBUSY  =(1<<0);
    private static final byte dispSHIFT =(1<<1);
    private static final byte dispRAD   =(1<<2);


    public MainLoop1245(Context context, SurfaceView sv) {
        super(context, sv);

        digi = new byte[digit];
        for (int i=0; i<digit; i++) {
            digi[i] = 0;
        }

        // cpuオブジェクトの生成
        sc = new Sc61860_1245(context);
        sc.setListener(this);
        sc.CpuReset();

        Log.w("LOG", "MainLoop1245 started.");
    }

    @Override
    protected void doDraw(SurfaceHolder holder) {
        //描画処理を開始
        Canvas c = holder.lockCanvas();

        if (c != null) {
            c.scale(dpdx, dpdx);

            int x_org, y_org, stp_x, stp_y, d_row, d_col;
            int x, y;
            int i, j, k;
            Paint p = new Paint();

            x_org = 12;
            y_org = 22;
            stp_x = 4;
            stp_y = 4;
            d_row=7;
            d_col=5;
            c.drawColor(0xFFEEFFFF);

            p.setStyle(Paint.Style.FILL);

            for (k = 0, x = x_org; k < 16; k++) {
                for (j = 0; j < d_col; j++, x += stp_x) {
                    for (i = 0, y = y_org; i < d_row; i++, y += stp_y) {
                        if (disp_on == 1 && (digi[k * d_col + j] & 0x01 << i) != 0) {
                            p.setColor(Color.DKGRAY);
                        } else {
                            p.setColor(Color.LTGRAY);
                        }
                        c.drawRect(x, y, x + stp_x, y + stp_y, p);
                    }
                }
                x += stp_x;
            }

            // シンボル表示
            p.setTextSize(12);
            p.setTypeface(Typeface.DEFAULT_BOLD);

            if ((state[1] & dispBUSY) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("BUSY", 12, 16, p);

            if ((state[0] & dispP) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("P", 56, 16, p);

            if ((state[0] & dispDEF) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("DEF", 76, 16, p);

            if ((state[0] & dispDE) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("DE", 116, 16, p);

            if ((state[0] & dispG) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("G", 134, 16, p);

            if ((state[1] & dispRAD) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("RAD", 148, 16, p);

            if ((state[1] & dispSHIFT) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("SHIFT", 180, 16, p);

            if (!prog_mode) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("RUN", 12, 62, p);
            if (prog_mode) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("PRO", 44, 62, p);

            //描画処理を終了
            holder.unlockCanvasAndPost(c);
        }


    }

}
