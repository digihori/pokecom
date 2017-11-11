package tk.horiuchi.pokecom;

import android.content.Context;
import android.util.Log;

/**
 * Created by yoshimine on 2017/09/30.
 */

public class CsUncaughtExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler {
    private static Context sContext = null;
    private static final Thread.UncaughtExceptionHandler sDefaultHandler
            = Thread.getDefaultUncaughtExceptionHandler();

    public CsUncaughtExceptionHandler(Context context){
        sContext = context;
    }

     // キャッチされない例外によって指定されたスレッドが終了したときに呼び出される
    public void uncaughtException(Thread thread, Throwable ex) {

        Log.w("UncaughtExcepton", "！！！予期せぬエラー発生！！！");
        sDefaultHandler.uncaughtException(thread, ex);
    }
}
