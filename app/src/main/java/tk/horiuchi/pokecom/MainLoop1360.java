package tk.horiuchi.pokecom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static tk.horiuchi.pokecom.MainActivity.dpdx;

/**
 * Created by yoshimine on 2017/07/30.
 */

public class MainLoop1360 extends MainLoopBase {

    private final int digit=6*5*5*4;
    public static byte digi[];
    public static byte state[]={0};

    private static final int dispSHIFT =(1<<0);
    private static final int dispDEF   =(1<<1);
    private static final int dispRUN   =(1<<4);
    private static final int dispPRO   =(1<<5);
    private static final int dispKANA  =(1<<6);
    private static final int dispSML   =(1<<7);


    public MainLoop1360(Context context, SurfaceView sv) {
        super(context, sv);

        digi = new byte[digit];
        for (int i=0; i<digit; i++) {
            digi[i] = 0;
        }

        // cpuオブジェクトの生成
        sc = new Sc61860_1360(context);
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

            x_org = 52;
            y_org = 8;
            stp = 3;
            d_row=8;
            d_col=6;
            c.drawColor(0xFFEEFFFF);

            p.setStyle(Paint.Style.FILL);

            for (l =0; l < 4; l++) {
                for (k = 0, x = x_org; k < 25; k++) {
                    for (j = 0; j < d_col; j++, x += stp) {
                        for (i = 0, y = y_org + (d_row * stp)*l; i < d_row; i++, y += stp) {
                            if ((digi[150*l + k * d_col + j] & 0x01 << i) != 0) {
                                p.setColor(Color.DKGRAY);
                            } else {
                                p.setColor(Color.LTGRAY);
                            }
                            c.drawRect(x, y, x + stp, y + stp, p);
                        }
                    }
                    //x += stp;
                }
            }

            // シンボル表示
            p.setTextSize(12);
            if ((state[0] & dispRUN) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("RUN", 8, 20, p);
            if ((state[0] & dispPRO) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("PRO", 8, 36, p);
            if ((state[0] & dispKANA) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("カナ", 8, 52, p);
            if ((state[0] & dispSML) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("SML", 8, 68, p);

            if ((state[0] & dispSHIFT) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("SHIFT", 8, 84, p);
            if ((state[0] & dispDEF) != 0) {
                p.setColor(Color.DKGRAY);
            } else {
                p.setColor(Color.LTGRAY);
            }
            c.drawText("DEF", 8, 100, p);


            //描画処理を終了
            holder.unlockCanvasAndPost(c);

        }


    }

}
