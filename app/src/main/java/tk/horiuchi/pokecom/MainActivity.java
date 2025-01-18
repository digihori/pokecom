package tk.horiuchi.pokecom;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.preference.PreferenceManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by yoshimine on 2017/07/29.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private final static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private RadioGroup radioGroup;
    public static float dpdx, dpdx_org;
    public final static int typePhone = 0;
    public final static int type7inch = 1;
    public final static int type10inch = 2;
    public static int deviceType;
    public static String title;
    public final static String old_app_path = Environment.getExternalStorageDirectory().getPath()+"/pokecom";
    public final static String old_rom_path = old_app_path + "/rom";
    public static Uri rom_path_uri = null;
    public static DocumentFile rom_dir = null;
    public static boolean cacheClear = false;
    private File pc1245 = null;
    private File pc1251 = null;
    private File pc1261 = null;
    private File pc1350 = null;
    private File pc1360 = null;
    private File pc1401 = null;
    private File pc1402 = null;
    private File pc1450 = null;
    private File pc1460 = null;
    private File pc1470 = null;
    private SharedPreferences prefs;
    private boolean debug = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_main);

        //アプリケーションで共通に利用するオブジェクトには、メモリリークが発生しないようにthisではなく
        //Context.getApplicationContext()を使用します。
        Context context = this.getApplicationContext();
        //キャッチされない例外により、スレッドが突然終了したときや、
        //このスレッドに対してほかにハンドラが定義されていないときに
        //呼び出されるデフォルトのハンドラを設定します。
        Thread.setDefaultUncaughtExceptionHandler(new CsUncaughtExceptionHandler(context));

        // ボタンのリスナー登録
        findViewById(R.id.select_button).setOnClickListener(this);
        findViewById(R.id.btn_reload).setOnClickListener(this);
        //findViewById(R.id.btn_clear).setOnClickListener(this);
        // RadioGroup の取得
        radioGroup = findViewById(R.id.RadioGroup);

        // ファイルIOのパーミッション関係の設定
        verifyStoragePermissions(this);
        // 初めて起動する時にショートカットを作成する
        // Play Storeからインストールすると自動でショートカットを作るので、ここではやらない
        //createShortcut();

        // dp->px変換のためにDisplayMetricsを取得しておく
        //DisplayMetrics metrics = getResources().getDisplayMetrics();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        dpdx_org = dpdx = metrics.density;
        //int main_width = metrics.widthPixels;
        //int main_height = metrics.heightPixels;
        Log.w("Main", String.format("widthPixels=%d\n", metrics.widthPixels));
        Log.w("Main", String.format("heightPixels=%d\n", metrics.heightPixels));
        Log.w("Main", String.format("Xdpi=%f\n", metrics.xdpi));
        Log.w("Main", String.format("Ydpi=%f\n", metrics.ydpi));
        Log.w("Main", String.format("density=%f\n", metrics.density));
        Log.w("Main", String.format("densityDpi=%d\n", metrics.densityDpi));
        Log.w("Main", String.format("scaledDensity=%f\n", metrics.scaledDensity));

        String dtext = String.format(Locale.US,"widthPixels=%d\n", metrics.widthPixels);
        dtext += String.format(Locale.US,"heightPixels=%d\n", metrics.heightPixels);
        dtext += String.format(Locale.US,"Xdpi=%f\n", metrics.xdpi);
        dtext += String.format(Locale.US,"Ydpi=%f\n", metrics.ydpi);
        dtext += String.format(Locale.US,"density=%f\n", metrics.density);
        dtext += String.format(Locale.US,"densityDpi=%d\n", metrics.densityDpi);
        dtext += String.format(Locale.US,"scaledDensity=%f\n", metrics.scaledDensity);

        // タイトルバーの表示を更新する
        title = "Pokecom GO";
        setTitle(title);

        // デバイスタイプとスケールの設定
        // スケールはそれぞれのエミュを立ち上げた時に動的に決定するので、ここでは仮の値
        if (getResources().getBoolean(R.bool.is_7inch)) {
            deviceType = type7inch;
            if (1.3f < dpdx_org && dpdx_org< 1.4f) {
                // nexus7(2012) tvdpi の時はスケール２倍の少し小さめ
                dpdx = 1.8f;
            } else if (dpdx_org == 1.0f) {
                dpdx = 1.5f;    // mdpi の時はスケール1.5倍
            } else if (dpdx_org == 2.0f) {
                dpdx = 2.6f;
            } else {
                // それ以外（多分xhdpiしかない？）の時はスケール３倍
                dpdx = 3.0f;
            }
            Log.w("Main", String.format("deviceType=7inch tablet(%d) scale=%f\n", deviceType, dpdx));
        } else if (getResources().getBoolean(R.bool.is_10inch)) {
            deviceType = type10inch;
            //dpdx = 4f;
            dpdx = dpdx_org * 2;    // スケールは2倍
            Log.w("Main", String.format("deviceType=10inch tablet(%d) scale=%f\n", deviceType, dpdx));
        } else {
            deviceType = typePhone;
            if (dpdx_org == 1.5f) {
                // hdpiの時は少し小さめにする
                dpdx = 1.3f;
            } else if (dpdx_org == 2.0f) {
                dpdx = 1.7f;
            } else if (2.6f < dpdx_org && dpdx_org < 3.0f) {
                // pixel6はdensity=2.875とか変な値？なので
                dpdx = 3.0f;
            } else if (dpdx_org == 3.5f) {
                // xxxhdpiの時は少し大きめにする
                dpdx = 4.0f;
            }
            Log.w("Main", String.format("deviceType=phone(%d) scale=%f\n", deviceType, dpdx));
        }

        dtext += String.format(Locale.US,"deviceType=%d scale=%f\n", deviceType, dpdx);
        //((TextView)findViewById(R.id.debug_msg)).setText(dtext);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String s = prefs.getString("uri", "");
        if (!s.isEmpty()) {
            rom_path_uri = Uri.parse(s);
            rom_dir = DocumentFile.fromTreeUri(this, rom_path_uri);
            Log.w("onCreate", String.format("rom_path_v2 loaded -> '%s'", rom_path_uri));
            //Log.w("onCreate", String.format("rom_path loaded -> '%s'", rom_path_v2));
        } else {
            rom_path_uri = null;
            rom_dir = null;
            Log.w("onCreate", "rom_path unloaded");
        }

        debug = prefs.getBoolean("debug", false);


        TextView title = findViewById(R.id.title);
        TextView dmsg = findViewById(R.id.debug_msg);
        dmsg.setText(dtext);
        dmsg.setVisibility(debug ? View.VISIBLE : View.INVISIBLE);

        title.setOnClickListener(view -> {
            debug = !debug;
            prefs.edit().putBoolean("debug", debug).apply();
            dmsg.setVisibility(debug ? View.VISIBLE : View.INVISIBLE);
        });

        TextView textView1 = findViewById(R.id.rom_uri_title);
        TextView textView2 = findViewById(R.id.rom_uri_string);
        textView1.setOnClickListener(view -> {
            rom_path_uri = null;
            rom_dir = null;
            textView2.setText("");
            prefs.edit().putString("uri", "").apply();
            reloadRomList();
        });

        //final ImageView iv = (ImageView)findViewById(R.id.pc_img);
        // ラジオグループのチェック状態変更イベントを登録
        // チェック状態変更時に呼び出されるメソッド
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // チェック状態を保存しておく
            prefs.edit().putInt("SelectStatus", checkedId).apply();


            /*
            // メニュー画面でポケコンの画像を出す処理
            switch (checkedId) {
                default:
                case R.id.RadioButton1245:
                    iv.setImageResource(R.drawable.pc1245);
                    break;
                case R.id.RadioButton1251:
                    iv.setImageResource(R.drawable.pc1251);
                    break;
                case R.id.RadioButton1261:
                    iv.setImageResource(R.drawable.pc1261);
                    break;
                case R.id.RadioButton1350:
                    iv.setImageResource(R.drawable.pc1350);
                    break;
                //case R.id.RadioButton1360:
                //    iv.setImageResource(R.drawable.pc1360);
                //    break;
                case R.id.RadioButton1401:
                    iv.setImageResource(R.drawable.pc1401);
                    break;
                case R.id.RadioButton1402:
                    iv.setImageResource(R.drawable.pc1402);
                    break;
                //case R.id.RadioButton1450:
                //    iv.setImageResource(R.drawable.pc1450);
                //    break;
                case R.id.RadioButton1460:
                    iv.setImageResource(R.drawable.pc1460);
                    break;
                case R.id.RadioButton1470:
                    iv.setImageResource(R.drawable.pc1470);
                    break;

            }
            */

        });

        // 前回のチェックボックス状態を復元
        int id = prefs.getInt("SelectStatus", 0);
        if (id == R.id.RadioButton1245 || id == R.id.RadioButton1251 || id == R.id.RadioButton1261 ||
                id == R.id.RadioButton1350 || id == R.id.RadioButton1360 ||
                id == R.id.RadioButton1401 || id == R.id.RadioButton1402  ||
                id == R.id.RadioButton1450 || id == R.id.RadioButton1460 || id == R.id.RadioButton1470) {
            ((RadioButton) findViewById(id)).setChecked(true);
        } else {
            // 何もセットされていないとき
            ((RadioButton) findViewById(R.id.RadioButton1245)).setChecked(true);
        }

        reloadRomList();

        Log.w("LOG", "!!!onCreate!!!");
    }

    /*
    private String getPathFromUri(Uri uri) {
        String path = "";
        String scheme = uri.getScheme();
        if ("file".equals(scheme)) {
            path = uri.getPath();
        } else if("content".equals(scheme)) {
            String[] projection = {MediaStore.MediaColumns.DATA};
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                }
                cursor.close();
            } else {
                Log.w("getPathFromUri", String.format("rom_path error"));
            }
        }
        return path;
    }

     */

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int id = sp.getInt("SelectedRom", 0);
        //id = 0;
        if (id != 0) {
            Intent intent;
            switch (id) {
                default:
                case R.id.RadioButton1245:
                    intent = new Intent(getApplication(), SubActivity1245.class);
                    break;
                case R.id.RadioButton1251:
                    intent = new Intent(getApplication(), SubActivity1251.class);
                    break;
                case R.id.RadioButton1261:
                    intent = new Intent(getApplication(), SubActivity1261.class);
                    break;
                case R.id.RadioButton1350:
                    intent = new Intent(getApplication(), SubActivity1350.class);
                    break;
                case R.id.RadioButton1360:
                    intent = new Intent(getApplication(), SubActivity1360.class);
                    break;
                case R.id.RadioButton1401:
                    intent = new Intent(getApplication(), SubActivity1401.class);
                    break;
                case R.id.RadioButton1402:
                    intent = new Intent(getApplication(), SubActivity1402.class);
                    break;
                case R.id.RadioButton1450:
                    intent = new Intent(getApplication(), SubActivity1450.class);
                    break;
                case R.id.RadioButton1460:
                    intent = new Intent(getApplication(), SubActivity1460.class);
                    break;
                case R.id.RadioButton1470:
                    intent = new Intent(getApplication(), SubActivity1470.class);
                    break;
            }
            //activityResultLauncher.launch(intent);
            startActivityForResult(intent, 0);
            Log.w("LOG", "------- id = "+id);
        }

        Log.w("LOG", "!!!onResume!!!");

    }

    private static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /*
    // ショートカットを作成する関数　　　　　　
    private void createShortcut(){

        // 1回だけ作成
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        if(!preference.getBoolean("shortcut",false)) {

            // アプリを起動するインテント
            Intent launcherIntent = new Intent();

            launcherIntent.setClass(this, MainActivity.class);
            launcherIntent.setAction(Intent.ACTION_MAIN);
            launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            // ショートカットインテント用のアイコンを取得
            Intent.ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher);

            // ショートカット用インテント
            Intent shortcutIntent = new Intent();
            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Pokecom GO!");
            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon); // アイコン画像セット
            shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

            shortcutIntent.putExtra("duplicate", false); // ショートカットを重複して作らない

            // ショートカットの作成
            preference.edit().putBoolean("shortcut", true).commit();
            sendBroadcast(shortcutIntent);
        }
    }

     */

    private void reloadRomList() {
        String rom_path;

        // ディレクトリが存在しない時はディレクトリを作成してダミーファイルを作成する
        // Android10以降はやらない
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            rom_path = old_rom_path;
            File app_dir = new File(old_app_path);
            if (!app_dir.exists()) {
                if (app_dir.mkdir()) {
                    Log.w("reloadRomList", String.format("mkdir ->'%s'", old_app_path));
                }
            }
            File dir = new File(rom_path);
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    Log.w("reloadRomList", String.format("mkdir ->'%s'", rom_path));
                }

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(rom_path + "/pc1245mem.bin");
                    byte[] buf = new byte[0x8000];

                    int i;
                    for (i = 0; i < dummyRom.length; i++) {
                        buf[i] = (byte) (dummyRom[i] & 0xff);
                    }
                    fos.write(buf, 0, i);
                    fos.flush();
                    Log.d("MainActivity", "dummyRom file created.");

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
            if (!rom_path.isEmpty()) {
                TextView t = findViewById(R.id.rom_uri_string);
                File f = new File(rom_path);
                t.setText(Uri.fromFile(f).toString());
                // ROMリストの表示色を更新する
                pc1245 = new File(rom_path + "/pc1245mem.bin");
                RadioButton rb1245 = findViewById(R.id.RadioButton1245);
                if (pc1245.exists()) {
                    rb1245.setTextColor(Color.BLACK);
                } else {
                    rb1245.setTextColor(Color.LTGRAY);
                }
                pc1251 = new File(rom_path + "/pc1251mem.bin");
                RadioButton rb1251 = findViewById(R.id.RadioButton1251);
                if (pc1251.exists()) {
                    rb1251.setTextColor(Color.BLACK);
                } else {
                    rb1251.setTextColor(Color.LTGRAY);
                }
                pc1261 = new File(rom_path + "/pc1261mem.bin");
                RadioButton rb1261 = findViewById(R.id.RadioButton1261);
                if (pc1261.exists()) {
                    rb1261.setTextColor(Color.BLACK);
                } else {
                    rb1261.setTextColor(Color.LTGRAY);
                }
                pc1350 = new File(rom_path + "/pc1350mem.bin");
                RadioButton rb1350 = findViewById(R.id.RadioButton1350);
                if (pc1350.exists()) {
                    rb1350.setTextColor(Color.BLACK);
                } else {
                    rb1350.setTextColor(Color.LTGRAY);
                }
                pc1360 = new File(rom_path + "/pc1360mem.bin");
                File pc1360bank = new File(rom_path + "/pc1360bank.bin");
                RadioButton rb1360 = findViewById(R.id.RadioButton1360);
                if (pc1360.exists() && pc1360bank.exists()) {
                    rb1360.setTextColor(Color.BLACK);
                } else {
                    rb1360.setTextColor(Color.LTGRAY);
                }
                pc1401 = new File(rom_path + "/pc1401mem.bin");
                RadioButton rb1401 = findViewById(R.id.RadioButton1401);
                if (pc1401.exists()) {
                    rb1401.setTextColor(Color.BLACK);
                } else {
                    rb1401.setTextColor(Color.LTGRAY);
                }
                pc1402 = new File(rom_path + "/pc1402mem.bin");
                RadioButton rb1402 = findViewById(R.id.RadioButton1402);
                if (pc1402.exists()) {
                    rb1402.setTextColor(Color.BLACK);
                } else {
                    rb1402.setTextColor(Color.LTGRAY);
                }
                pc1450 = new File(rom_path + "/pc1450mem.bin");
                RadioButton rb1450 = findViewById(R.id.RadioButton1450);
                if (pc1450.exists()) {
                    rb1450.setTextColor(Color.BLACK);
                } else {
                    rb1450.setTextColor(Color.LTGRAY);
                }
                pc1460 = new File(rom_path + "/pc1460mem.bin");
                File pc1460bank = new File(rom_path + "/pc1460bank.bin");
                RadioButton rb1460 = findViewById(R.id.RadioButton1460);
                if (pc1460.exists() && pc1460bank.exists()) {
                    rb1460.setTextColor(Color.BLACK);
                } else {
                    rb1460.setTextColor(Color.LTGRAY);
                }
                pc1470 = new File(rom_path + "/pc1470mem.bin");
                File pc1470bank = new File(rom_path + "/pc1470bank.bin");
                RadioButton rb1470 = findViewById(R.id.RadioButton1470);
                if (pc1470.exists() && pc1470bank.exists()) {
                    rb1470.setTextColor(Color.BLACK);
                } else {
                    rb1470.setTextColor(Color.LTGRAY);
                }
            }
        } else {
            if (rom_path_uri != null && rom_dir != null) {
                TextView t = findViewById(R.id.rom_uri_string);
                t.setText(rom_path_uri.toString());
            }
            for (ChkTbl chkTbl : chktbl) {
                RadioButton rb = findViewById(chkTbl.id);
                if (rom_dir != null &&
                        rom_dir.findFile(chkTbl.file1) != null &&
                        (chkTbl.file2.equals("") || rom_dir.findFile(chkTbl.file2) != null)) {
                    rb.setTextColor(Color.BLACK);
                    chkTbl.valid = true;
                } else {
                    rb.setTextColor(Color.LTGRAY);
                    chkTbl.valid = false;
                }
            }
        }
    }
    private static class ChkTbl {
        int id;
        String file1;
        String file2;
        boolean valid;
        ChkTbl(int i, String f1, String f2) {
            id = i;
            file1 = f1;
            file2 = f2;
            valid = false;
        }
    }
    private final ChkTbl[] chktbl = {
            new ChkTbl(R.id.RadioButton1245, "pc1245mem.bin", ""),
            new ChkTbl(R.id.RadioButton1251, "pc1251mem.bin", ""),
            new ChkTbl(R.id.RadioButton1261, "pc1261mem.bin", ""),
            new ChkTbl(R.id.RadioButton1350, "pc1350mem.bin", ""),
            new ChkTbl(R.id.RadioButton1360, "pc1360mem.bin", "pc1360bank.bin"),
            new ChkTbl(R.id.RadioButton1401, "pc1401mem.bin", ""),
            new ChkTbl(R.id.RadioButton1402, "pc1402mem.bin", ""),
            new ChkTbl(R.id.RadioButton1450, "pc1450mem.bin", ""),
            new ChkTbl(R.id.RadioButton1460, "pc1460mem.bin", "pc1460bank.bin"),
            new ChkTbl(R.id.RadioButton1470, "pc1470mem.bin", "pc1470bank.bin")
    };

    private boolean isRadioButtonEnable(int id) {
        for (ChkTbl chkTbl : chktbl) {
            if (chkTbl.id == id) {
                return chkTbl.valid;
            }
        }
        return false;
    }
    private final int[] dummyRom = {
            // 1245で「Hello!」と表示するプログラム
            0x03, 0x00, 0x02, 0x1b, 0x01, 0x01, 0x84, 0x13,
            0x02, 0x0a, 0x03, 0xf7, 0x02, 0xff, 0x86, 0x13,
            0x02, 0x0a, 0x00, 0x1e, 0x24, 0x26, 0x40, 0x29,
            0x04, 0x4d, 0x2d, 0x02, 0x7f, 0x08, 0x08, 0x08,
            0x7f, 0x38, 0x54, 0x54, 0x54, 0x18, 0x00, 0x00,
            0x7f, 0x00, 0x00, 0x00, 0x00, 0x7f, 0x00, 0x00,
            0x38, 0x44, 0x44, 0x44, 0x38, 0x00, 0x00, 0x5f,
            0x00, 0x00
    };

    public void onClick(View v) {

        int c = v.getId();

        // select_buttonが押された時の処理
        if (c == R.id.select_button) {

            // Activityの切り替え
            Intent intent;
            int rb_id = radioGroup.getCheckedRadioButtonId();
            switch (rb_id) {
                case R.id.RadioButton1245:
                    if (isRadioButtonEnable(rb_id) || pc1245 != null && pc1245.exists()) {
                        intent = new Intent(getApplication(), SubActivity1245.class);
                    } else {
                        return;
                    }
                    break;
                case R.id.RadioButton1251:
                    if (isRadioButtonEnable(rb_id) || pc1251 != null && pc1251.exists()) {
                        intent = new Intent(getApplication(), SubActivity1251.class);
                    } else {
                        return;
                    }
                    break;
                case R.id.RadioButton1261:
                    if (isRadioButtonEnable(rb_id) || pc1261 != null && pc1261.exists()) {
                        intent = new Intent(getApplication(), SubActivity1261.class);
                    } else {
                        return;
                    }
                    break;
                case R.id.RadioButton1350:
                    if (isRadioButtonEnable(rb_id) || pc1350 != null && pc1350.exists()) {
                        intent = new Intent(getApplication(), SubActivity1350.class);
                    } else {
                        return;
                    }
                    break;
                case R.id.RadioButton1360:
                    if (isRadioButtonEnable(rb_id) || pc1360 != null && pc1360.exists()) {
                        intent = new Intent(getApplication(), SubActivity1360.class);
                    } else {
                        return;
                    }
                    break;
                case R.id.RadioButton1401:
                    if (isRadioButtonEnable(rb_id) || pc1401 != null && pc1401.exists()) {
                        intent = new Intent(getApplication(), SubActivity1401.class);
                    } else {
                        return;
                    }
                    break;
                case R.id.RadioButton1402:
                    if (isRadioButtonEnable(rb_id) || pc1402 != null && pc1402.exists()) {
                        intent = new Intent(getApplication(), SubActivity1402.class);
                    } else {
                        return;
                    }
                    break;
                case R.id.RadioButton1450:
                    if (isRadioButtonEnable(rb_id) || pc1450 != null && pc1450.exists()) {
                        intent = new Intent(getApplication(), SubActivity1450.class);
                    } else {
                        return;
                    }
                    break;
                case R.id.RadioButton1460:
                    if (isRadioButtonEnable(rb_id) || pc1460 != null && pc1460.exists()) {
                        intent = new Intent(getApplication(), SubActivity1460.class);
                    } else {
                        return;
                    }
                    break;
                case R.id.RadioButton1470:
                    if (isRadioButtonEnable(rb_id) || pc1470 != null && pc1470.exists()) {
                        intent = new Intent(getApplication(), SubActivity1470.class);
                    } else {
                        return;
                    }
                    break;
                default:
                    return;
            }
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            sp.edit().putInt("SelectedRom", rb_id).apply();
            //activityResultLauncher.launch(intent);
            startActivityForResult(intent, 0);
        } else if (c == R.id.btn_reload) {
            reloadRomList();
            cacheClear = true;
            Toast.makeText(this, "ROM list reloaded & cache cleared.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                        sp.edit().putInt("SelectedRom", 0).apply();
                        Log.w("LOG", "!!!onActivityResult   SelectRom cleared.");
                    }
                }
            });


     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putInt("SelectedRom", 0).apply();
        Log.w("LOG", "!!!onActivityResult   SelectRom cleared.");

        super.onActivityResult(requestCode, resultCode, data);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Log.w("menu", "settings.");
                dirInit();
                break;
            case R.id.about:
                // ボタンをタップした際の処理を記述
                AboutDialogFragment dialog = new AboutDialogFragment();
                dialog.showNow(getSupportFragmentManager(), "");

                // メッセージテキストのサイズを変更する。
                //TextView textView = (TextView) findViewById(android.R.id.message);
                //textView.setTextSize(R.dimen.textsize_small);
                break;
        }
        return true;
    }

    private void dirInit() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        actInitResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> actInitResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        //結果を受け取った後の処理
                        rom_path_uri = result.getData().getData();
                        getContentResolver().takePersistableUriPermission(rom_path_uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        String stringUri = rom_path_uri.toString();
                        prefs.edit().putString("uri", stringUri).apply();
                        Log.w("actInitResultLauncher", String.format("uri=%s", stringUri));
                        rom_dir = DocumentFile.fromTreeUri(this, rom_path_uri);
                        reloadRomList();
                    }
                }
            });

}
