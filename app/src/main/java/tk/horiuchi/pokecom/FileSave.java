package tk.horiuchi.pokecom;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Created by yoshimine on 2017/08/15.
 */

public class FileSave extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savefile);

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
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_filetype);
        RadioButton radioButton = (RadioButton) findViewById(radioGroup.getCheckedRadioButtonId());
        String type = radioButton.getText().toString();
        // 返すデータ(Intent&Bundle)の作成
        Intent data = new Intent();
        data.putExtra("filePath", filename);
        data.putExtra("fileType", type);
        data.putExtra("startAddress", sadr);
        data.putExtra("endAddress", eadr);

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
