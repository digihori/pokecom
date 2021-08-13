package tk.horiuchi.pokecom;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.IllegalFormatException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tk.horiuchi.pokecom.MainActivity.cacheClear;
import static tk.horiuchi.pokecom.MainActivity.title;
import static tk.horiuchi.pokecom.Sc61860Base.kon_cnt;
import static tk.horiuchi.pokecom.Sc61860Base.mainram;

public class SubActivityBase extends Activity implements View.OnTouchListener {
    protected static SubActivityBase instance = null;
    public static boolean nosave = false;
    protected int activityId;
    protected MainLoopBase ml;
    protected static KeyboardBase kb;
    //public static TextView dWin;
    public static String debugText;
    protected Vibrator vib;
    public static int main_width;
    public static int main_height;
//    public static float dpdx, dpdx_org;
    public static boolean debug_info = true;
    public static boolean beep_enable;
    public static boolean clock_emulate_enable;
    public static int cpuClockWait;
    public static boolean vibrate_enable = true;
    public static int machine;
    public static final int PC1401=1;
    public static final int PC1245=2;
    public static final int PC1350=3;

    public static String default_path, path;
    public static String filetype;
    public int startAddress;
    public int endAddress;

    // BASICテキスト領域のアドレスは継承クラス側で設定する
    protected int basicTextStart;
    protected int basicStartAdrL;
    protected int basicStartAdrH;
    protected int basicEndAdrL;
    protected int basicEndAdrH;

    public static int[] mBtnResIds;
    protected int[] mBtnStatusCnt;
    //public static Boolean[] mBtnStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        System.out.printf("SubActivityBase created!\n");

        if (true || savedInstanceState == null) {


            //setContentView(R.layout.activity_main_1401);


            // デフォルトの設定
            PreferenceManager.setDefaultValues(this, R.xml.preference, true);

            // 設定値をロード
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            //clock_emulate_enable = sp.getBoolean("clock_emulate_checkbox_key", false);
            cpuClockWait = Integer.parseInt(sp.getString("cpu_clock_wait_key", "2"));
            debug_info = sp.getBoolean("debug_checkbox_key", false);
            beep_enable = sp.getBoolean("beep_checkbox_key", false);
            vibrate_enable = sp.getBoolean("vibrator_checkbox_key", true);



            //SurfaceView sv = (SurfaceView) findViewById(R.id.surfaceView);
            //ml = new MainLoop(this, sv);

            vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

            setTitle(title);

            default_path = path = Environment.getExternalStorageDirectory().getPath()+"/pokecom";
            Log.w("onCreate", "path="+path);

            final Handler _handler1 = new Handler();
            final int DELAY1 = 20;
            _handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < mBtnStatusCnt.length; i++) {
                        if (mBtnStatusCnt[i] > 0) {
                            mBtnStatusCnt[i]--;
                            if (mBtnStatusCnt[i] == 0) {
                                kb.setBtnStatus(mBtnResIds[i], false);
                            }
                        }
                    }
                    _handler1.postDelayed(this, DELAY1);
                }
            }, DELAY1);
        }

        Log.w("onCreate", String.format("--------- saveInstanceState=%d", (savedInstanceState == null)? 0 : 1));
    }

    public static SubActivityBase getInstance() {
        return instance;
    }

    public void modeInit() {
        ;
    }

    // デバッグウィンドウの設定
    protected void setDebugWindow(TextView tv) {
        //if (setKeyMapStep != 0) return;
        final TextView w = tv;
        final Handler _handler1 = new Handler();
        final int DELAY1 = 200;
        _handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (debug_info) {
                    w.setTextColor(Color.BLACK);
                    //dWin.setText("proto20  " + main_width + " x " + main_height + " - " + dpdx_org + " -> " + dpdx + "\n"+debugText);
                    w.setText(debugText);
                } else {
                    //w.setTextColor(Color.WHITE);
                }
                _handler1.postDelayed(this, DELAY1);
            }
        }, DELAY1);
    }

    // ボタンのリソースファイルを更新する処理
    // 位置調整用のボタン枠表示の切り替え
    protected void changeButtonFrame(int[] ids, boolean flg) {

        for (int i = 0; i < ids.length; i++) {
            if (flg) {
                findViewById(ids[i]).setBackgroundResource(R.drawable.button_debug);
            } else {
                findViewById(ids[i]).setBackgroundResource(R.drawable.button);
            }
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    public Point getBitmapSizeFromDynamicImageLayer(ImageView imageLayer) {
        final int actualHeight, actualWidth;
        final int imageLayerHeight = imageLayer.getHeight(), imageLayerWidth = imageLayer.getWidth();
        //Log.w("LOG", "imageLayerWidth="+imageLayerWidth+" imageLayerHeight="+imageLayerHeight);
        final int bitmapHeight = imageLayer.getDrawable().getIntrinsicHeight(), bitmapWidth = imageLayer.getDrawable().getIntrinsicWidth();
        //Log.w("LOG", "bitmapWidth="+bitmapWidth+" bitmapHeight="+bitmapHeight);

        if (imageLayerHeight * bitmapWidth <= imageLayerWidth * bitmapHeight) {
            actualWidth = bitmapWidth * imageLayerHeight / bitmapHeight;
            actualHeight = imageLayerHeight;
        } else {
            actualHeight = bitmapHeight * imageLayerWidth / bitmapWidth;
            actualWidth = imageLayerWidth;
        }
        //Log.w("LOG", "x="+actualWidth+" y="+actualHeight);
        return new Point(actualWidth, actualHeight);
    }

    public void stretchItemSize(GridLayout gl, ImageView iv) {
        Point p = getBitmapSizeFromDynamicImageLayer(iv);
        Log.w("LOG", "p.x="+p.x+" p.y="+p.y);

        int childWidth = p.x / gl.getColumnCount();
        int childHeight = p.y / gl.getRowCount();

        Log.w("LOG", "iv w="+iv.getWidth()+" h="+iv.getHeight());
        Log.w("LOG", "column="+gl.getColumnCount()+" row="+gl.getRowCount());
        Log.w("LOG", "w="+childWidth+" h="+childHeight);
        for (int i = 0; i < gl.getChildCount(); i++) {
            gl.getChildAt(i).setMinimumWidth(childWidth);
            gl.getChildAt(i).setMinimumHeight(childHeight);
        }
    }
    public void stretchItemSize(GridLayout gl, ImageView iv, int x) {
        Point p = getBitmapSizeFromDynamicImageLayer(iv);
        Log.w("LOG", "p.x="+p.x+" p.y="+p.y);

        int childWidth = p.x / gl.getColumnCount();
        int childHeight = p.y / gl.getRowCount();

        Log.w("LOG", "iv w="+iv.getWidth()+" h="+iv.getHeight());
        Log.w("LOG", "column="+gl.getColumnCount()+" row="+gl.getRowCount());
        Log.w("LOG", "w="+childWidth+" h="+childHeight);
        for (int i = 0; i < gl.getChildCount(); i++) {
            if (i == x) {
                gl.getChildAt(i).setMinimumWidth(childWidth * 2);
                gl.getChildAt(i).setMinimumHeight(childHeight);
            } else {
                gl.getChildAt(i).setMinimumWidth(childWidth);
                gl.getChildAt(i).setMinimumHeight(childHeight);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (nosave) {
            nosave = false;
        } else {
            ml.sc.halt();
            Sc61860params sc_params = ml.sc.saveParam();

            //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            Gson gson = new Gson();
            // objectをjson文字列へ変換
            String jsonInstanceString = gson.toJson(sc_params);
            // 変換後の文字列をputStringで保存
            prefs.edit().putString("PREF_SC", jsonInstanceString).apply();
            //outState.putSerializable("sc61860", sc_params);
            Log.w("SubActivity", "------------- onPause()");
            if (debug_info) Toast.makeText(this, String.format("Activity saved. id=%d", sc_params.id), Toast.LENGTH_SHORT).show();
        }

        // Game pad のマッピング情報を保存する
        prefs.edit().putInt("PREF_M0", mappedId[0]).apply();
        prefs.edit().putInt("PREF_M1", mappedId[1]).apply();
        prefs.edit().putInt("PREF_M2", mappedId[2]).apply();
        prefs.edit().putInt("PREF_M3", mappedId[3]).apply();
        prefs.edit().putInt("PREF_M4", mappedId[4]).apply();
        prefs.edit().putInt("PREF_M5", mappedId[5]).apply();
        prefs.edit().putInt("PREF_M6", mappedId[6]).apply();
        prefs.edit().putInt("PREF_M7", mappedId[7]).apply();
        for (int x : mappedId) {
            Log.w("mapping", String.format("saved -> '%x'", x));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // 保存されているjson文字列を取得
        String Sc61860String = prefs.getString("PREF_SC", "");
        prefs.edit().putString("PREF_SC", "").apply();  // 読み出した部分はクリア

        if (!cacheClear && Sc61860String != null) {
            Gson gson = new Gson();
            // json文字列をクラスのインスタンスに変換
            Sc61860params sc_params = gson.fromJson(Sc61860String, Sc61860params.class);
            if (sc_params != null && sc_params.id == activityId) {
                ml.sc.halt();
                ml.sc.restoreParam(sc_params);
                ml.sc.restart();
                Log.w("SubActivity", "------------ onResume()    parameter restored!!! ---------");
                if (debug_info) Toast.makeText(this, String.format("Activity loaded. id=%d", sc_params.id), Toast.LENGTH_SHORT).show();
            }
        }
        cacheClear = false;

        // Game pad のマッピング情報を取得する
        mappedId[0] = prefs.getInt("PREF_M0", 0);
        mappedId[1] = prefs.getInt("PREF_M1", 0);
        mappedId[2] = prefs.getInt("PREF_M2", 0);
        mappedId[3] = prefs.getInt("PREF_M3", 0);
        mappedId[4] = prefs.getInt("PREF_M4", 0);
        mappedId[5] = prefs.getInt("PREF_M5", 0);
        mappedId[6] = prefs.getInt("PREF_M6", 0);
        mappedId[7] = prefs.getInt("PREF_M7", 0);
        for (int x : mappedId) {
            Log.w("mapping", String.format("loaded -> '%x'", x));
        }

        // デバッグウィンドウの更新
        if (debug_info) {
            ((TextView)findViewById(R.id.debugWindow)).setTextColor(Color.BLACK);
        } else {
            ((TextView)findViewById(R.id.debugWindow)).setTextColor(Color.WHITE);
            ((TextView)findViewById(R.id.debugWindow)).setText("");
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // こんな感じでBundleにパラメータ保存しておく
        //TextView textView1 = (TextView)findViewById(R.id.textView1);
        //String value = textView1.getText().toString();
        //outState.putString("TEXT_VIEW_STR", value);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //こんな感じでBundleかたデータを取り出す
        //String value = savedInstanceState.getString("TEXT_VIEW_STR");
        //TextView textView1 = (TextView)findViewById(R.id.textView1);
        //textView1.setText(value);
    }

    //public void onClick(View v) {

        /*
        int c = v.getId();
        if (c == R.id.buttonBRK) {
            kon_cnt = 10;
        }
        if (c == R.id.buttonSHIFT2) {
            c = R.id.buttonSHIFT;
        }
        */
        /*
        if (c == R.id.buttonZ) {
            for (int i = 0; i < 256; i++) {
                Log.w("LOG", String.format("opcode[%02x]=%d", i, code_cnt[i]));
            }
        }
        if (c == R.id.buttonX) {
            for (int i = 0; i < 256; i++) {
                code_cnt[i] = 0;
            }
            Log.w("LOG", "code_cnt cleared.");
        }
        */

        //kb.setBuf(c);
        //keyBuf = v.getId();

        //Log.w("LOG", "KEY="+c);

        //if (vibrate_enable) {
        //    vib.vibrate(10);
        //}

    //}


    protected int getBtnIdx(int id) {
        for (int idx = 0; idx < mBtnResIds.length; idx++) {
            if (id == mBtnResIds[idx]) return idx;
        }
        return -1;
    }

    /*
    protected void setBtnStatus(int id, boolean sts) {
        int idx = getBtnIdx(id);
        if (idx != -1) mBtnStatus[idx] = sts;
    }

    public int getPressBtnId() {
        for (int idx = 0; idx < mBtnResIds.length; idx++) {
            if (mBtnStatus[idx]) return mBtnResIds[idx];
        }
        return 0;
    }
    */

    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();

        if (id == R.id.buttonSHIFT2) {
            id = R.id.buttonSHIFT;
        }

        int eventAction = event.getActionMasked();

        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
                //Log.w("onTouch", "DOWN");
                //kb.setBtnStatus(id, true);
                //mBtnStatusCnt[getBtnIdx(id)] = -1;
                //break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //Log.w("onTouch", "POINTER_DOWN");
                Log.w("onTouch", "DOWN");
                if (setKeyMapStep == 0) {
                    kb.setBtnStatus(id, true);
                    mBtnStatusCnt[getBtnIdx(id)] = -1;
                    if (id == R.id.buttonBRK) {
                        kon_cnt = 10;
                    }
                    if (vibrate_enable) {
                        vib.vibrate(10);
                    }
                } else {
                    mappedId[setKeyMapStep - 1] = id;
                    setKeyMapStep++;
                    setKeyMap();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //Log.w("onTouch", "POINTER_UP");
                //kb.setBtnStatus(id, false);
                //mBtnStatusCnt[getBtnIdx(id)] = 3;
                //break;
            case MotionEvent.ACTION_UP:
                //Log.w("onTouch", "UP");
                //kb.setBtnStatus(id, false);
                //mBtnStatusCnt[getBtnIdx(id)] = 3;
                //break;
            case MotionEvent.ACTION_CANCEL:
                //Log.w("onTouch", "CANCEL");
                Log.w("onTouch", "UP");
                //kb.setBtnStatus(id, false);
                if (setKeyMapStep == 0) {
                    mBtnStatusCnt[getBtnIdx(id)] = 3;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //Log.w("onTouch", "MOVE");
                break;
        }
        return false;
    }


    protected int hibyte(int x) {
        return((x & 0xff00) >> 8);
    }
    protected int lobyte(int x) {
        return(x & 0x00ff);
    }
    protected int hilo(int hi, int lo) {
        return(((hi & 0x00ff) << 8) | (lo & 0x00ff));
    }


    // BASICプログラムの読み込み
    protected int load(String filename) {

        int start = hilo(mainram[basicStartAdrH], mainram[basicStartAdrL]);
        //int len = load(filename, basicTextStart, 1);
        int len = load(filename, start, 1);
        //Log.w("---LOAD", "len="+len);

        mainram[basicStartAdrL]=lobyte(start);
        mainram[basicStartAdrH]=hibyte(start);
        mainram[basicEndAdrL]=lobyte(start+len-1);
        mainram[basicEndAdrH]=hibyte(start+len-1);

        Log.w("LOAD", String.format("start:%02x %02x", mainram[basicStartAdrH], mainram[basicStartAdrL]));
        Log.w("LOAD", String.format("end  :%02x %02x", mainram[basicEndAdrH], mainram[basicEndAdrL]));

        return len;
    }

    // マシン語プログラムの読み込み
    protected int load(String filename, int start) {
        int ret;

        ret = load(filename, start, 0);
        return ret;
    }

    // プログラムの読み込みの共通処理
    protected int load(String filename, int start, int mode) {
        int ret = 0;
        FileInputStream fis = null;
        //BufferedReader in = null;

        try {
            fis = new FileInputStream(filename);
            //in = new BufferedReader(new InputStreamReader(fis));

            byte buf[] = new byte[0x8000];
            int len, i = 0;
            while ((len = fis.read(buf)) != -1) {
                i += len;
            }

            int j;
            if (mode == 0) {
                // マシン語コードの場合はそのままメモリに展開する
                for (j = 0; j < i; j++) {
                        mainram[start + j] = 0x00ff & buf[j];
                }
            } else {
                // BASICコードの場合は中間コード片変換してからメモリに展開する
                int buf2[] = new int[i];
                for (j = 0; j < i; j++) {
                    buf2[j] = 0x00ff & buf[j];
                    //Log.w("LOG", String.format("%02x %c", buf[j], buf[j]));
                }
                int buf3[] = bas2code(buf2);
                for (j = 0; j < buf3.length; j++) {
                    mainram[start + j] = buf3[j];
                    //Log.w("LOG", String.format("%02x %c", buf3[j], buf3[j]));
                }
            }

            Log.w("LOAD", String.format("start=%04x", start));
            Log.w("LOAD", String.format("file=%s length=%04x", filename, j));

            //for (int k=0; k < i; k++) {
            //    Log.w("LOAD", String.format("%04x:%02x", basicTextStart+k, mainram[basicTextStart+k]));
            //}

            ret = j;
        } catch (IOException e) {
            Log.d("LOAD", e.toString());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    // BASIC プログラムの保存
    protected void save(String filename) {
        int start = hilo(mainram[basicStartAdrH], mainram[basicStartAdrL]);
        int end = hilo(mainram[basicEndAdrH], mainram[basicEndAdrL]);

        int data[] = new int[end - start + 1];
        for (int i = 0; i <= end - start; i++) {
            data[i] = mainram[start + i];
        }
        int data2[] = code2bas(data);

        save(filename, data2);
    }

    // マシン語プログラムの保存
    protected void save(String filename, int start, int end) {
        int data[] = new int[end - start + 1];
        for (int i = 0; i <= end - start; i++) {
            data[i] = mainram[start + i];
        }
        Log.w("SAVE", String.format("start=%04x end=%04x", start, end));
        save(filename, data);
    }

    // ファイル保存の共通処理
    protected void save(String filename, int data[]) {
        FileOutputStream fos = null;

        try {
            File dir = new File(default_path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            fos = new FileOutputStream(filename);
            byte buf[] = new byte[0x8000];

            int i, j;
            for (i = 0; i < data.length; i++) {
                buf[i] = (byte)(data[i] & 0xff);
            }
            fos.write(buf, 0, i);
            fos.flush();

            //Log.w("SAVE", String.format("start=%04x end=%04x", start, end));
            Log.w("SAVE", String.format("file=%s length=%04x", filename, i));
        } catch (IOException e) {
            Log.d("MainActivity", e.toString());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void actLoad(){
        Intent intent = new android.content.Intent(getApplication(), FileLoad.class);
        startActivityForResult(intent, 0);
    }

    public void actSave() {
        Intent intent = new android.content.Intent(getApplication(), FileSave.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        Log.w("LOG", "onActivityResult.");
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {

                    String txt1 = data.getExtras().getString("filePath");
                    if (txt1 != null) {
                        path = txt1; // オープンするファイルのパスとファイル名
                        Log.w("LOG", "path="+path);
                    }
                    String txt2 = data.getExtras().getString("fileType");
                    if (txt2 != null) {
                        filetype = txt2;
                        Log.w("LOG", "type="+filetype);
                    }
                    startAddress = data.getExtras().getInt("startAddress");

                    Log.w("LOG", "startAddress="+String.format("%04x", startAddress));

                    if (txt1 != null && filetype.equals("BASIC")) {
                        int len = load(path);
                        Toast.makeText(this, "loaded:"+path+"("+len+")", Toast.LENGTH_LONG).show();
                    } else if (txt1 != null && filetype.equals("BINARY")) {
                        int len = load(path, startAddress);
                        Toast.makeText(this, "loaded:"+path+"("+len+")"+" "+startAddress+"-", Toast.LENGTH_LONG).show();
                    } else {
                        // エラー
                    }
                } else {
                    // 何もしない
                    Log.w("LOG", "ファイル読み出しがキャンセルされた");
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    String txt1 = data.getExtras().getString("filePath");
                    if (txt1 != null) {
                        path = default_path + "/" + txt1; // オープンするファイルのパスとファイル名
                        Log.w("LOG", "path=" + path);
                    }
                    String txt2 = data.getExtras().getString("fileType");
                    if (txt2 != null) {
                        filetype = txt2;
                        Log.w("LOG", "type="+filetype);
                    }
                    startAddress = data.getExtras().getInt("startAddress");
                    Log.w("LOG", "startAddress="+String.format("%04x", startAddress));
                    endAddress = data.getExtras().getInt("endAddress");
                    Log.w("LOG", "endAddress="+String.format("%04x", endAddress));

                    if (txt1 != null && filetype.equals("BASIC")) {
                        save(path);
                        Toast.makeText(this, "saved:"+path, Toast.LENGTH_LONG).show();
                    } else if (txt1 != null && filetype.equals("BINARY")) {
                        save(path, startAddress, endAddress);
                        Toast.makeText(this, "saved:"+path+" "+startAddress+"-"+endAddress, Toast.LENGTH_LONG).show();
                    } else {
                        // エラー
                        Log.w("SAVE", "error!");
                    }
                } else {
                    Log.w("LOG", "ファイル保存がキャンセルされた");
                }
                break;
            default:
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.optionsMenu_01:
                nosave = true;
                actLoad();
                return true;
            case R.id.optionsMenu_02:
                nosave = true;
                actSave();
                return true;
            case R.id.optionsMenu_03:   // reset
                modeInit();
                ml.sc.halt();
                ml.sc.CpuReset();
                return true;
            case R.id.optionsMenu_04:   // settings
                nosave = true;
                Intent intent1 = new android.content.Intent(this, MyPreferenceActivity.class);
                startActivity(intent1);
                return true;
            case R.id.optionsMenu_05:   // gamepad mapping
                nosave = true;
                setKeyMapStep = 1;
                setKeyMap();
                return true;
            case R.id.optionsMenu_06:   // exit
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected String replaceSpecialChar(String str) {
        // エスケープ文字'\'そのものにしたいところを一旦0x7fに変換しておく
        //Log.w("replaceSpecialChar", String.format("str='%s'", str));
        str = str.replace("\\\\", String.valueOf((char)0x7f));
        //Log.w("replaceSpecialChar", String.format("str='%s' regex0='%s'", str, regex0));

        for (int i = 0xef; i < 0xff; i++) {
            String s = cmd_tbl[i];
            if (s.charAt(0) != '\\') continue;

            String regex = String.valueOf('\\')+s;
            //Log.w("replaceSpecialChar", String.format("code=%02x cmd='%s' regex='%s'", i, s, regex));
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(str);
            if (m.find()) {
                str = str.replaceAll(regex, String.valueOf((char)i));
            }
        }
        return str;
    }

    protected String[] split(String str) {
        String str2 = str;
        String RM = null;

        // 最初に特殊キャラを変換しておく
        str2 = str = replaceSpecialChar(str);

        // REMとダブルコートの文字列を制御キャラに変換しておく
        //String regex = "[^(a-zA-Z)]REM.*$";
        String regex = "REM.*$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        if (m.find()) {
            if (m.group().length() > 4) {
                RM = m.group().substring(4);
                str2 = str.replaceFirst(regex, "REM \\\\RM");
            } else {
                RM = null;
                str2 = str.replaceFirst(regex, "REM");
            }
        }
        //Log.w("split-REM", str2);

        //String[] DQ = new String[10];   // 10個まで（仮）
        ArrayList<String> DQ = new ArrayList<>();
        int n = 0;
        regex = "\".*?\"";
        p = Pattern.compile(regex);
        while (true /*n < 10*/) {
            m = p.matcher(str2);
            if (m.find()) {
                //DQ[n] = m.group();
                DQ.add(m.group());
                str2 = str2.replaceFirst(regex, " \\\\DQ"+n+" ");
                Log.w("split", String.format("DQ[%d] = '%s'", n, m.group()));
                n++;
            } else {
                break;
            }
        }
        // 行末とダブルコートを同時に検索する表現がわからないので処理を分けた
        regex = "\".*?$";
        p = Pattern.compile(regex);
        while (true) {
            m = p.matcher(str2);
            if (m.find()) {
                DQ.add(m.group());
                str2 = str2.replaceFirst(regex, " \\\\DQ"+n+" ");
                Log.w("split", String.format("DQ[%d] = '%s'", n, m.group()));
                n++;
            } else {
                break;
            }
        }
        //Log.w("split-DQ", str2);

        //Log.w("LOG", String.format("before -> '%s'", str));
        //Log.w("LOG", String.format("after  -> '%s'", str2));
        //if (RM != null) Log.w("LOG", String.format("REM = '%s'", RM));
        //for (int i = 0; i < n; i++) {
        //    Log.w("LOG", String.format("DQ[%d] = '%s'", i, DQ[i]));
        //}

        // トークンの切り出し
        String[] tmp;
        tmp = str2.split("\\s+|(?<=[\\*\\/\\+\\-><=:;,\\)\\(])|(?=[\\*\\/\\+\\-><=:;,\\)\\(])", 0);

        // 切り出したトークンでREMやダブルコートになっているところを戻す
        for (int i = 0, j = 0; i < tmp.length; i++) {
            if (tmp[i].equals("\\RM")) {
                tmp[i] = RM;
            } else if (tmp[i].equals("\\DQ"+j)) {
                //tmp[i] = DQ[j];
                try {
                    tmp[i] = DQ.get(j);
                } catch (IndexOutOfBoundsException e) {
                    ;
                }
                j++;
            }
            //Log.w("split", String.format("\"%s\" %x", tmp[i], (int)tmp[i].charAt(0)));
            Log.w("split", String.format("\"%s\"", tmp[i]));
        }

        return tmp;
    }

    protected static String trimLeft(String s) {
        int startPos = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ' ') {
                startPos = i;
                break;
            }
        }

        return s.substring(startPos);

    }

    protected int[] bas2code(int[] source) {
        int[] dest = new int[0x8000];
        int[] ret;
        int len = source.length;
        if (len == 0) {
            ret = new int[1];
            ret[0] = 0;
            return ret;
        }

        int r = 0, w = 0, c = 0;
        String str = "";
        dest[w++] = 0xff; // プログラム開始
        String[] token;

        while (r < len) {
            //Log.w("LOG", String.format("--- 行区切り ---"));
            // 1行読み込み
            // 2バイト文字は読み捨てる
            str = "";
            if (source[r] == '\n') {
                // 空行は読み捨てる
                Log.w("load", "blank line!");
                r++;
                continue;
            }
            while ((c = source[r++]) != '\n') {
                if (c == '\r') continue;    // \rは読み捨てる
                String s = "";
                try {
                    // 1byte文字以外はExceptionを吐くのでcatchで拾って読み捨てる
                    s = String.format("%c", c);
                } catch (IllegalFormatCodePointException e) {
                    Log.w("load", "IlleagalFormatCodeException!!!");
                } catch (IllegalFormatException e) {
                    Log.w("load", "IlleagalFormatException!!!");
                }
                //str += String.format("%c", c);
                str += s;
                if (r >= len) break;
            }
            // 先頭の空白を削除する
            str = trimLeft(str);
            //Log.w("LOG", String.format("%s", str));


            token = split(str);
            //for (int i = 0; i < token.length; i++) {
            //    Log.w("LOAD", String.format("%s", token[i]));
            //}

            // 一つ目のトークンは必ず行番号
            int line_num;
            try {
                line_num = Integer.parseInt(token[0]);
            } catch (NumberFormatException e) {
                Log.w("load", "NumberFormatException!!!");
                // パースに失敗したらこの行はスキップする
                continue;
            }
            if (line_num == 0) continue;   // パースに失敗したらこの行はスキップする
            dest[w++] = hibyte(line_num);
            dest[w++] = lobyte(line_num);

            int l = w++;
            int ll = 0;

            // 次のトークンからはコマンド
            for (int i = 1; i < token.length; i++) {
                //Log.w("LOAD", String.format("token[%d]=%s length=%d", i, token[i], token[i].length()));
                //for (int j = 0; j < token[i].length(); j++) {
                //    Log.w("LOAD", String.format("%02x", (int)(token[i].charAt(j))));
                //}
                if (i == 1 && token[i].equals(":")) continue;  // 行番号の次がコロンの場合は読み捨て
                int temp = cmdname2code(token[i]);
                if (temp != 0) {
                    dest[w++] = temp;
                    //Log.w("LOG", String.format("%02x", temp));
                    ll++;

                } else {
                    int n = token[i].length();
                    for (int j = 0; j < n; j++) {
                        int x = (int) (token[i].charAt(j));
                        if (x == 0xef) {    // '\EX'は'E'に変換する
                            dest[w++] = 'E';
                        } else if (x == 0x7f) { // '\\'を0x7fに待避していたものを'\'に戻す
                            dest[w++] = '\\';
                        } else {
                            dest[w++] = (int) (token[i].charAt(j));
                        }
                        //Log.w("LOG", String.format("%s", token[i].charAt(j)));
                        ll++;
                    }
                }
            }
            dest[l] = ll + 1;   // 行の長さ(区切りコードも含まれるので+1)

            dest[w++] = 0x0d;   // 行区切り
            //Log.w("LOG", String.format("separate"));
        }
        dest[w++] = 0xff;   // プログラム終了

        // 戻り値用に配列を作り直す
        ret = new int[w];
        for (int i = 0; i < w; i++) {
            ret[i] = dest[i];
        }
        return ret;
    }

    protected int[] code2bas(int[] source) {
        int[] dest = new int[0x8000];
        int[] ret;

        int len = source.length;
        //Log.w("LOG", "len"+len);
        if (len == 0) {
            ret = new int[1];
            ret[0] = 0;
            return ret;
        }

        //for (int i = 0; i < source.length; i++) {
        //    Log.w("LOG", String.format("%02x", source[i]));
        //}

        int r = 0, w = 0, c = 0;
        //String str = "";

        if (source[r++] != 0xff) {
            ret = new int[1];
            ret[0] = 0;
            return ret;
        }

        boolean cmd_exist = false;
        boolean non_cmd = false;
        while (r < len) {
            //Log.w("LOG", String.format("--- 業区切り --- r=%d len=%d", r, len));
            // 最初の2byteは必ず行番号
            int line_num = hilo(source[r++], source[r++]);
            //Log.w("LOG", "line_num="+line_num);
            for (int i = 0; i < (Integer.toString(line_num)).length(); i++) {
                dest[w++] = (Integer.toString(line_num)).charAt(i);
            }
            // 区切りのスペース
            dest[w++] = ' ';

            // 以降はコマンド
            String cmd;
            r++;    // 1byte飛ばす
            cmd_exist = false;
            non_cmd = false;
            while ((c = source[r++]) != 0x0d) {
                //Log.w("LOG", String.format("c=%02x", c));
                cmd = "";
                if (0x80 <= c && c <= 0xff) {
                    if (c < 0xf0 && non_cmd || cmd_exist) {
                        dest[w++] = ' ';
                    }
                    cmd_exist = false;
                    non_cmd = false;

                    cmd = cmd_tbl[c];
                    //Log.w("LOG", String.format("cmd=%s", cmd));
                    for (int j = 0; j < cmd.length(); j++) {
                        dest[w++] = cmd.charAt(j);
                    }
                    if (c < 0xf0) {
                        cmd_exist = true;
                    }
                } else {
                    if (cmd_exist) {
                        dest[w++] = ' ';
                        cmd_exist = false;
                    }
                    dest[w++] = c;
                    if (c == '\\') {
                        dest[w++] = c;
                    }

                    non_cmd = (c != ' ') ? true : false;
                }
                if (r >= source.length) break;
            }
            dest[w++] = '\n';   // 改行コード

            if (source[r] == 0xff) {
                // 次が終了コードの場合はここで終わり
                break;
            }
        }
        //dest[w++] = '\0';   // 最後にエンドマーク

        // 戻り値用に配列を作り直す
        ret = new int[w];
        for (int i = 0; i < w; i++) {
            ret[i] = dest[i];
        }
        return ret;
    }

    // コマンド文字列を内部コードに変換する
    protected int cmdname2code(String cmdname) {
        for (int i = 0; i < 256; i++) {
            if (cmd_tbl[i].equals(cmdname)) {
                return(i);
            }
        }
        return(0);
    }

    protected String cmd_tbl[]; // 実体は継承クラスで作る

    private int[] mappedId = new int[8];
    //private int mappedId[] = {R.id.button8, R.id.button2, R.id.button4, R.id.button6,
    //        R.id.buttonDEF, R.id.buttonDEF, R.id.buttonDEF, R.id.buttonDEF};

    private int setKeyMapStep = 0;
    private void setKeyMap() {
        Log.w("setKeyMap", String.format("step=%d", setKeyMapStep));
        TextView tv = findViewById(R.id.debugWindow);
        switch (setKeyMapStep) {
            case 1:
                tv.setTextColor(Color.BLACK);
                tv.setText("UP=?");
                break;
            case 2:
                tv.setText("DOWN=?");
                break;
            case 3:
                tv.setText("LEFT=?");
                break;
            case 4:
                tv.setText("RIGHT=?");
                break;
            case 5:
                tv.setText("BUTTON A=?");
                break;
            case 6:
                tv.setText("BUTTON B=?");
                break;
            case 7:
                tv.setText("BUTTON X=?");
                break;
            case 8:
                tv.setText("BUTTON Y=?");
                break;
            default:
                setKeyMapStep = 0;
                tv.setTextColor(Color.WHITE);
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();

        Log.w("GamePad", "onKeyDown!!");

        int inputDevice = event.getSource();
        int id = 0;
        if (true || (inputDevice & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK ||
                (inputDevice & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                (inputDevice & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD ||
                (inputDevice & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD) {

            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    //id = R.id.button8;
                    id = mappedId[0];
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    //id = R.id.button2;
                    id = mappedId[1];
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    //id = R.id.button4;
                    id = mappedId[2];
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    //id = R.id.button6;
                    id = mappedId[3];
                    break;

                case KeyEvent.KEYCODE_BUTTON_1:
                case KeyEvent.KEYCODE_BUTTON_X:
                    id = mappedId[6];
                    break;

                case KeyEvent.KEYCODE_BUTTON_2:
                case KeyEvent.KEYCODE_BUTTON_Y:
                    id = mappedId[7];
                    break;

                case KeyEvent.KEYCODE_BUTTON_3:
                case KeyEvent.KEYCODE_BUTTON_B:
                    id = mappedId[5];
                    break;

                case KeyEvent.KEYCODE_BUTTON_4:
                case KeyEvent.KEYCODE_BUTTON_A:
                    //id = R.id.buttonENTER;
                    id = mappedId[4];
                    break;

                default:
                    break;
            }
            if (id != 0) {
                if (action == KeyEvent.ACTION_DOWN) {
                    kb.setBtnStatus(id, true);
                    mBtnStatusCnt[getBtnIdx(id)] = -1;
                } else {
                    mBtnStatusCnt[getBtnIdx(id)] = 3;
                }
            }
            // キーコードをログ出力
            String msg = "keyCode:" + keyCode;
            Log.w("GamePad", msg);

        }
        return super.dispatchKeyEvent(event);
    }


}
