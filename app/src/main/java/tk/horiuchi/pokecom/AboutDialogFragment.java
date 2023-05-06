package tk.horiuchi.pokecom;

import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.DialogFragment;
import androidx.fragment.app.DialogFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * Created by yoshimine on 2017/10/29.
 */

public class AboutDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /*
        return new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
                .setTitle(R.string.about_title)
                .setMessage(R.string.about_body)
                .setPositiveButton(android.R.string.ok, null)
                .create();
                */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
                builder.setTitle(R.string.about_title);
                builder.setMessage(R.string.about_body);
                builder.setPositiveButton(android.R.string.ok, null);
                builder.create();

        AlertDialog dialog = builder.show();

        // メッセージテキストのサイズを変更する。
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        Resources res = getResources();
        float fontSize = res.getDimension(R.dimen.textsize_small);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);

        return(dialog);
    }

    @Override
    public void onPause() {
        super.onPause();

        // onPause でダイアログを閉じる場合
        dismiss();
        Log.w("Dialog", "object cleared.");
    }
}
