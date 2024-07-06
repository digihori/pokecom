package tk.horiuchi.pokecom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import static tk.horiuchi.pokecom.SubActivityBase.default_path;
import static tk.horiuchi.pokecom.SubActivityBase.legacy_storage_io;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


/**
 * Created by yoshimine on 2017/08/15.
 */

public class FileLoad extends AppCompatActivity implements FileSelectDialog.OnFileSelectDialogListener {
    private boolean bin = false;
    private String fileName;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loadfile);

        // ラジオグループのオブジェクトを取得
        RadioGroup rg = (RadioGroup)findViewById(R.id.rg_filetype);
        int checkedId = rg.getCheckedRadioButtonId();
        EditText et = (EditText)findViewById(R.id.startAdr);
        switch (checkedId) {
            default:
            case R.id.rb_basic:
                et.setTextColor(Color.LTGRAY);
                break;
            case R.id.rb_binary:
                et.setTextColor(Color.BLACK);
                break;
        }


        // ラジオグループのチェック状態変更イベントを登録
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            // チェック状態変更時に呼び出されるメソッド
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // チェック状態時の処理を記述
                // チェックされたラジオボタンオブジェクトを取得
                //RadioButton r = (RadioButton)findViewById(checkedId);
                EditText et = (EditText)findViewById(R.id.startAdr);
                switch (checkedId) {
                    default:
                    case R.id.rb_basic:
                        et.setTextColor(Color.LTGRAY);
                        break;
                    case R.id.rb_binary:
                        et.setTextColor(Color.BLACK);
                        break;
                }
            }
        });
    }

    /**
     * ファイル選択イベント
     */
    public void onClickFileSelect(View view) {
        Log.w("FileLoad", String.format("FileSelect"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || !legacy_storage_io) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
            actLoadResultLauncher.launch(intent);
        } else {
            // ファイル選択ダイアログを表示
            FileSelectDialog dialog = new FileSelectDialog(this);
            dialog.setOnFileSelectDialogListener(this);

            // 表示
            //dialog.show(Environment.getExternalStorageDirectory().getPath());
            dialog.show(default_path);
        }
    }
    private final ActivityResultLauncher<Intent> actLoadResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        //結果を受け取った後の処理
                        uri = result.getData().getData();
                        Log.w("actLoadResultLauncher", String.format("uri=%s", uri));
                        String text = getFileNameFromUri(this, uri);
                        setFileName(text);
                        setRadioButton(text);
                        if (bin) fillAddressField(text);
                    }
                }
            });

    private String getFileNameFromUri(@NonNull Context context, Uri uri) {
        // is null
        if (null == uri) {
            return null;
        }

        // get scheme
        String scheme = uri.getScheme();

        // get file name
        String fileName = null;
        switch (scheme) {
            case "content":
                String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
                Cursor cursor = context.getContentResolver()
                        .query(uri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        fileName = cursor.getString(
                                cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                    }
                    cursor.close();
                }
                break;

            case "file":
                fileName = new File(uri.getPath()).getName();
                break;

            default:
                break;
        }
        return fileName;
    }

    /**
     * ファイル選択完了イベント
     */
    @Override
    public void onClickFileSelect(File file) {

        if (file != null) {
            // 選択ファイルを設定
            EditText txt=(EditText)findViewById(R.id.dirPath);
            String dir = file.getPath();
            txt.setText(dir);
            txt.setSelection(dir.length());

            String[] token = dir.split("/", 0);
            //if (token.length == 0) return;
            fileName = token[token.length - 1];
            //String[] token2 = fileName.split("\\.", 0);
            //if (token2.length < 2) return;
            //Log.w("FileLoad", String.format("%s | %s", token2[0], token2[1]));
            File f = new File(dir);
            uri = Uri.fromFile(f);

            // ファイルの拡張子をみてファイルタイプのラジオボタンを切り替える
            setRadioButton(dir);

            if (bin) {
                fillAddressField(dir);
            }

        }
    }

    private void setFileName(String filename) {
        if (filename != null) {
            TextView t = (TextView)findViewById(R.id.textUri);
            t.setText(uri.toString());
            // 選択ファイルを設定
            EditText txt = (EditText) findViewById(R.id.dirPath);
            txt.setText(filename);
            txt.setSelection(filename.length());
        }
    }

    private void setRadioButton(String filename) {
        String[] token = filename.split("\\.", 0);
        if (token.length < 2) {
            bin = false;
            Log.w("FileLoad", "unknown filename or file type.");
            return;
        }
        Log.w("FileLoad", String.format("%s | %s", token[0], token[1]));

        if (token[1].equals("bas") || token[1].equals("BAS")) {
            RadioButton rb = (RadioButton) findViewById(R.id.rb_basic);
            rb.setChecked(true);
            bin = false;
        } else if (token[1].equals("bin") || token[1].equals("BIN")) {
            RadioButton rb = (RadioButton) findViewById(R.id.rb_binary);
            rb.setChecked(true);
            bin = true;
        } else {
            bin = false;
            Log.w("FileLoad", "unknown file type.");
        }
    }

    private void fillAddressField(String filename) {
        if (filename == null || filename.isEmpty()) return;
        String[] token = filename.split("[-\\.]", 0);
        if (token.length < 3) return;
        Log.w("FileLoad", String.format("adr = %s", token[token.length - 2]));
        try {
            int adr = Integer.parseInt(token[token.length - 2], 16);
            if (0x2000 <= adr && adr <= 0xffff) {
                EditText et = (EditText) findViewById(R.id.startAdr);
                et.setText(String.format("0x%04x", adr));
            } else {
                Log.w("FileLoad", String.format("Invalid data(%04x).", adr));
            }

        } catch (NumberFormatException e) {
            Log.w("FileLoad", "NumberFormatException occured.");
        }
    }
    public void btnOk(View view) {
        int adr;
        try {
            adr = Integer.decode(((EditText) findViewById(R.id.startAdr)).getText().toString());
        } catch (Exception e) {
            adr = 0;
        }
        //Log.w("LOG", ((EditText) findViewById(R.id.startAdr)).getText().toString());
        //Log.w("LOG", "adr="+adr);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_filetype);
        RadioButton radioButton = (RadioButton) findViewById(radioGroup.getCheckedRadioButtonId());
        String type = radioButton.getText().toString();
        String filename = ((EditText)findViewById(R.id.dirPath)).getText().toString();
        if (filename.length() == 0) {
            filename = null;
        }
        Intent data = new Intent();
        data.putExtra("filePath", filename);
        data.putExtra("fileType", type);
        data.putExtra("startAddress", adr);
        data.putExtra("uri", uri.toString());

        setResult(RESULT_OK, data);
        finish();
    }

    public void btnCancel(View view) {
        Intent data = new Intent();
        data.putExtra("key.canceledData", "CANCEL");
        setResult(RESULT_CANCELED, data);
        finish();
    }

}
