package tk.horiuchi.pokecom;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import static tk.horiuchi.pokecom.SubActivityBase.beep_enable;
//import static tk.horiuchi.pokecom.SubActivityBase.clock_emulate_enable;
import static tk.horiuchi.pokecom.SubActivityBase.cpuClockWait;
import static tk.horiuchi.pokecom.SubActivityBase.debug_info;
import static tk.horiuchi.pokecom.SubActivityBase.vibrate_enable;


/**
 * Created by yoshimine on 2017/07/23.
 */
import static tk.horiuchi.pokecom.MainActivity.title;

public class MyPreferenceActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
            getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
            setTitle(title);

    }

    @Override
    public void onPause() {
        super.onPause();

        // 設定値をロード
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        //clock_emulate_enable = sp.getBoolean("clock_emulate_checkbox_key", false);
        cpuClockWait = Integer.parseInt(sp.getString("cpu_clock_wait_key", "2"));
        debug_info = sp.getBoolean("debug_checkbox_key", false);
        beep_enable = sp.getBoolean("beep_checkbox_key", false);
        vibrate_enable = sp.getBoolean("vibrator_checkbox_key", true);    }
}
