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
import static tk.horiuchi.pokecom.SubActivity1261.prog_mode;

/**
 * Created by yoshimine on 2017/07/29.
 */

public class MainLoop1261 extends MainLoopBase {
    private final int digit=5*24*2;
    public static byte digi[];
    public static byte state[]={0,0};

    private static final byte dispBUSY  =(1<<0);
    private static final byte dispPRINT =(1<<1);
    private static final byte dispKANA  =(1<<3);
    private static final byte dispSMALL =(1<<4);
    private static final byte dispSHIFT =(1<<5);
    private static final byte dispDEF   =(1<<6);

    private static final byte dispDEG   =(1<<0);
    private static final byte dispRAD   =(1<<1);
    private static final byte dispERROR =(1<<5);


    public MainLoop1261(Context context, SurfaceView sv) {
        super(context, sv);

        digi = new byte[digit];
        for (int i=0; i<digit; i++) {
            digi[i] = 0;
        }

        // cpuオブジェクトの生成
        sc = new Sc61860_1261(context);
        sc.setListener(this);
        sc.CpuReset();

        Log.w("LOG", "MainLoop1261 started.");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
        Log.w("!!!1261!!!", String.format("width = %d, height=%d\n", w, h));
        float s1 = (float)h / 90f;
        float s2 = (float)w * 0.92f / 526f;
        dpdx = s1 < s2 ? s1 : s2;
        Log.w("!!!1261!!!", String.format("dpdx = %f (%f, %f)\n", dpdx, s1, s2));
        super.surfaceChanged(holder, f, w, h);
    }

    @Override
    protected void doDraw(SurfaceHolder holder) {
        //描画処理を開始
        Canvas c = holder.lockCanvas();

        if (c != null) {
            c.scale(dpdx, dpdx);

            int x_org, y_org, stp, stp_x, stp_y, d_row, d_col;
            int x, y;
            int i, j, k, l;
            Paint p = new Paint();

            x_org = 12;
            y_org = 16;
            stp = 2;
            stp_x = 4;
            stp_y = 4;
            d_row=7;
            d_col=5;
            c.drawColor(0xFFEEFFFF);

            p.setStyle(Paint.Style.FILL);

            for (l = 0; l < 2; l++) {
                for (k = 0, x = x_org; k < 24; k++) {
                    for (j = 0; j < d_col; j++, x += stp_x) {
                        for (i = 0, y = y_org + (d_row * stp_y) * l; i < d_row; i++, y += stp_y) {
                            if ((digi[(24*l + k) * d_col + j] & 0x01 << i) != 0) {
                                p.setColor(Color.DKGRAY);
                            } else {
                                p.setColor(Color.LTGRAY);
                            }
                            c.drawRect(x, y, x + stp_x, y + stp_y, p);
                        }
                    }
                    x += stp;
                }
                y_org += stp_y;
            }

            // シンボル表示
            p.setTextSize(12);
            p.setTypeface(Typeface.DEFAULT_BOLD);

            if ((state[0] & dispDEF) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("DEF", 50, 12, p);
            if ((state[0] & dispSHIFT) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("SHIFT", 80, 12, p);
            if ((state[0] & dispSMALL) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("SML", 150, 12, p);
            if ((state[0] & dispKANA) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("カナ", 180, 12, p);


            if ((state[0] & dispBUSY) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("BUSY", 12, 12, p);
            if ((state[0] & dispPRINT) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("PRINT", 150, 88, p);
            if ((state[1] & dispDEG) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("DEG", 200, 88, p);
            if ((state[1] & dispRAD) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("RAD", 230, 88, p);
            if ((state[1] & dispERROR) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("ERROR", 260, 88, p);

            switch (prog_mode) {
                default:
                case 0:
                    p.setColor(Color.DKGRAY);
                    c.drawText("RUN", 12, 88, p);
                    p.setColor(Color.LTGRAY);
                    c.drawText("PRO", 44, 88, p);
                    c.drawText("RSV", 76, 88, p);
                    break;
                case 1:
                    p.setColor(Color.DKGRAY);
                    c.drawText("PRO", 44, 88, p);
                    p.setColor(Color.LTGRAY);
                    c.drawText("RUN", 12, 88, p);
                    c.drawText("RSV", 76, 88, p);
                    break;
                case 2:
                    p.setColor(Color.DKGRAY);
                    c.drawText("RSV", 76, 88, p);
                    p.setColor(Color.LTGRAY);
                    c.drawText("RUN", 12, 88, p);
                    c.drawText("PRO", 44, 88, p);
                    break;
            }

            //描画処理を終了
            holder.unlockCanvasAndPost(c);
        }


    }

}
