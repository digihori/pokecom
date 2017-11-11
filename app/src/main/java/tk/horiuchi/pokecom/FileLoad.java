package tk.horiuchi.pokecom;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.File;

import static tk.horiuchi.pokecom.SubActivityBase.default_path;


/**
 * Created by yoshimine on 2017/08/15.
 */

public class FileLoad extends Activity implements FileSelectDialog.OnFileSelectDialogListener {

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

        // ファイル選択ダイアログを表示
        FileSelectDialog dialog = new FileSelectDialog(this);
        dialog.setOnFileSelectDialogListener(this);

        // 表示
        //dialog.show(Environment.getExternalStorageDirectory().getPath());
        dialog.show(default_path);
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

            String[] talken = dir.split("/", 0);
            //if (talken.length == 0) return;
            String filename = talken[talken.length - 1];
            String[] talken2 = filename.split("\\.", 0);
            if (talken2.length < 2) return;
            Log.w("FileLoad", String.format("%s | %s", talken2[0], talken2[1]));

            boolean bin = false;
            // ファイルの拡張子をみてファイルタイプのラジオボタンを切り替える
            if (talken2[1].equals("bas") || talken2[1].equals("BAS")) {
                RadioButton rb = (RadioButton) findViewById(R.id.rb_basic);
                rb.setChecked(true);
            } else if (talken2[1].equals("bin") || talken2[1].equals("BIN")) {
                RadioButton rb = (RadioButton) findViewById(R.id.rb_binary);
                rb.setChecked(true);
                bin = true;
            } else {
                ;
            }

            if (bin) {
                String[] talken3 = talken2[0].split("-", 0);
                Log.w("FileLoad", String.format("adr = %s", talken3[talken3.length - 1]));
                //if (talken3.length == 0) return;
                try {
                    int adr = Integer.parseInt(talken3[talken3.length - 1], 16);
                    if (0x2000 <= adr && adr <= 0xffff) {
                        EditText et = (EditText) findViewById(R.id.startAdr);
                        et.setText(String.format("0x%04x", adr));
                    } else {
                        Log.w("FileLoad", String.format("Invalid data(%04x).", adr));
                    }

                } catch (NumberFormatException e) {
                    Log.w("FileLoad", "NumberFormatException occured.");
                    return;
                }
            }

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
        String filename = ((EditText)findViewById(R.id.dirPath)).getText().toString();
        if (filename.length() == 0) {
            filename = null;
        }
        String type = radioButton.getText().toString();
        Intent data = new Intent();
        data.putExtra("filePath", filename);
        data.putExtra("fileType", type);
        data.putExtra("startAddress", adr);

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
