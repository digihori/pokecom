package tk.horiuchi.pokecom;

import static tk.horiuchi.pokecom.SubActivityBase.default_path;
import static tk.horiuchi.pokecom.SubActivityBase.legacy_storage_io;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

/**
 * Created by yoshimine on 2017/08/15.
 */

public class FileSave extends AppCompatActivity {
    private boolean bin = false;
    private String fileName;
    private Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savefile);

        Button btn = findViewById(R.id.btn1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || !legacy_storage_io) {
            btn.setVisibility(View.VISIBLE);
        } else {
            btn.setVisibility(View.GONE);
        }

        // ラジオグループのオブジェクトを取得
        RadioGroup rg = (RadioGroup)findViewById(R.id.rg_filetype);
        int checkedId = rg.getCheckedRadioButtonId();
        EditText et1 = (EditText)findViewById(R.id.startAdr);
        EditText et2 = (EditText)findViewById(R.id.endAdr);
        switch (checkedId) {
            default:
            case R.id.rb_basic:
                et1.setTextColor(Color.LTGRAY);
                et2.setTextColor(Color.LTGRAY);
                break;
            case R.id.rb_binary:
                et1.setTextColor(Color.BLACK);
                et2.setTextColor(Color.BLACK);
                break;
        }


        // ラジオグループのチェック状態変更イベントを登録
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            // チェック状態変更時に呼び出されるメソッド
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // チェック状態時の処理を記述
                // チェックされたラジオボタンオブジェクトを取得
                //RadioButton r = (RadioButton)findViewById(checkedId);
                EditText et1 = (EditText)findViewById(R.id.startAdr);
                EditText et2 = (EditText)findViewById(R.id.endAdr);
                switch (checkedId) {
                    default:
                    case R.id.rb_basic:
                        et1.setTextColor(Color.LTGRAY);
                        et2.setTextColor(Color.LTGRAY);
                        break;
                    case R.id.rb_binary:
                        et1.setTextColor(Color.BLACK);
                        et2.setTextColor(Color.BLACK);
                        break;
                }
            }
        });

    }

    public void onClickFileSelect(View view) {
        Log.w("FileSave", String.format("FileSelect"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || !legacy_storage_io) {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
            actSaveResultLauncher.launch(intent);
        } else {
            ;
        }
    }

    private final ActivityResultLauncher<Intent> actSaveResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        //結果を受け取った後の処理
                        uri = result.getData().getData();
                        Log.w("actSaveResultLauncher", String.format("uri=%s", uri));
                        String text = getFileNameFromUri(this, uri);
                        setFileName(text);
                        //setRadioButton(text);
                        //if (bin) fillAddressField(text);
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

    public void btnOk(View view) {
        int sadr, eadr;
        try {
            sadr = Integer.decode(((EditText) findViewById(R.id.startAdr)).getText().toString());
            eadr = Integer.decode(((EditText) findViewById(R.id.endAdr)).getText().toString());
        } catch (Exception e) {
            sadr = 0;
            eadr = 0;
        }
        String filename = ((EditText)findViewById(R.id.dirPath)).getText().toString();
        if (filename.length() == 0) {
            filename = null;
        }
        if (uri == null) {
            File f = new File(default_path+'/'+filename);
            uri = Uri.fromFile(f);
        }
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_filetype);
        RadioButton radioButton = (RadioButton) findViewById(radioGroup.getCheckedRadioButtonId());
        String type = radioButton.getText().toString();
        // 返すデータ(Intent&Bundle)の作成
        Intent data = new Intent();
        data.putExtra("filePath", filename);
        data.putExtra("fileType", type);
        data.putExtra("startAddress", sadr);
        data.putExtra("endAddress", eadr);
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
