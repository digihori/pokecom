package tk.horiuchi.pokecom;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by yoshimine on 2017/07/23.
 */

public class MyPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
    }
}
