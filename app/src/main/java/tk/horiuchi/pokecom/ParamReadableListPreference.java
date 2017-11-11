package tk.horiuchi.pokecom;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by yoshimine on 2017/07/23.
 */

public class ParamReadableListPreference extends ListPreference {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ParamReadableListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ParamReadableListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ParamReadableListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ParamReadableListPreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        setWidgetLayoutResource(R.layout.param_readable_list_preference);
        return super.onCreateView(parent);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView textView = (TextView)view.findViewById(R.id.valueView);
        textView.setText(getEntry());
    }
}
