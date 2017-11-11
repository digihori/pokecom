package tk.horiuchi.pokecom;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by yoshimine on 2017/07/24.
 */

public class MainLoopBase extends SurfaceView implements RefreshScreenInterFace, SurfaceHolder.Callback, Runnable {
    private Thread thread;
    private SurfaceHolder holder;
    //private int width = 0;
    //private int height = 0;
    private int refresh_cnt = 0;
    protected Sc61860Base sc = null;
    protected Beep beep = null;


    public MainLoopBase(Context context, SurfaceView sv) {
        super(context);
        holder = sv.getHolder();
        holder.addCallback(this);
        holder.setFixedSize(getWidth(), getHeight());

        Log.w("LOG", "--------------SurfaceView started!----------------");

    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {

        //beep = new Beep();

        //width = w;
        //height = h;
        thread = new Thread(this);
        thread.start();

        final Handler _handler1 = new Handler();
        final int DELAY1 = 20;
        _handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                sc.ticTac();
                //Log.w("LOG", "run.....");
                _handler1.postDelayed(this, DELAY1);
            }
        }, DELAY1);

        final Handler _handler2 = new Handler();
        final int DELAY2 = 50;
        _handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (refresh_cnt != 0) {
                    refresh_cnt = 0;
                    _refreshScreen();
                }
                //sc.ticTac();
                //Log.w("LOG", "run.....");
                _handler2.postDelayed(this, DELAY2);
            }
        }, DELAY2);

        Log.w("LOG", "--------------SurfaceView surfaceChanged!----------------");
        refreshScreen();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        doDraw(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread=null;
        if (beep != null) beep.stopThread();
    }

    @Override
    public void run() {
        while (thread != null) {
            sc.CpuRun();
            //Log.w("LOG", "run");
            if (sc.step) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    protected void doDraw(SurfaceHolder holder) {
        // 中身は継承クラスで実装する
    }


    public void refreshScreen() {
        refresh_cnt++;
    }

    private void _refreshScreen() {
        doDraw(holder);
    }

}
