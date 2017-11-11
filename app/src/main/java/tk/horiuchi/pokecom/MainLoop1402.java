package tk.horiuchi.pokecom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static tk.horiuchi.pokecom.MainActivity.dpdx;
import static tk.horiuchi.pokecom.Sc61860Base.disp_on;


/**
 * Created by yoshimine on 2017/07/29.
 */

public class MainLoop1402 extends MainLoop1401 {

    public MainLoop1402(Context context, SurfaceView sv) {
        super(context, sv);

        digi = new byte[digit];
        for (int i=0; i<digit; i++) {
            digi[i] = 0;
        }

        // cpuオブジェクトの生成
        sc = new Sc61860_1402(context);
        sc.setListener(this);
        sc.CpuReset();

    }


}
