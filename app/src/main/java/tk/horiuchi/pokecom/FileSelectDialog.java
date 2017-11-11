package tk.horiuchi.pokecom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yoshimine on 2017/08/15.
 */

public class FileSelectDialog extends Activity implements DialogInterface.OnClickListener {

    /** アクティビティ */
    private Activity activity = null;

    /** リスナー */
    private OnFileSelectDialogListener listener = null;

    /** 対象となる拡張子 */
    private String extension = "";

    /** 表示中のファイル情報リスト */
    private List<File> viewFileDataList = null;

    /** 表示パスの履歴 */
    private List<String> viewPathHistory = null;

    /**
     * コンストラクタ
     *
     * @param activity アクティビティ
     */
    public FileSelectDialog(Activity activity) {

        this.activity = activity;
        this.viewPathHistory = new ArrayList<String>();
    }

    /**
     * コンストラクタ
     *
     * @param activity アクティビティ
     * @param extension 対象となる拡張子
     */
    public FileSelectDialog(Activity activity, String extension) {

        this.activity = activity;
        this.extension = extension;
        this.viewPathHistory = new ArrayList<String>();
    }

    /**
     * 選択イベント
     *
     * @param dialog ダイアログ
     * @param which 選択位置
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {

        File file = this.viewFileDataList.get(which);

        // ディレクトリの場合
        if (file.isDirectory()) {

            show(file.getAbsolutePath() + "/");

        } else {

            this.listener.onClickFileSelect(file);
        }
    }

    /**
     * ダイアログを表示
     *
     * @param dirPath ディレクトリのパス
     */
    public void show(final String dirPath) {

        // 変更ありの場合
        if (this.viewPathHistory.size() == 0 ||
                !dirPath.equals(this.viewPathHistory.get(this.viewPathHistory.size() - 1))) {

            // 履歴を追加
            this.viewPathHistory.add(dirPath);
        }

        // ファイルリスト
        File[] fileArray = new File(dirPath).listFiles();

        // 名前リスト
        List<String> nameList = new ArrayList<String>();

        if (fileArray != null) {

            // ファイル情報マップ
            Map<String, File> map = new HashMap<String, File>();

            for (File file : fileArray) {

                // ディレクトリの場合
                if (file.isDirectory()) {

                    nameList.add(file.getName() + "/");
                    map.put(nameList.get(map.size()), file);

                    // 対象となる拡張子の場合
                } else if ("".equals(this.extension) ||
                        file.getName().matches("^.*" + this.extension + "$")) {

                    nameList.add(file.getName());
                    map.put(nameList.get(map.size()), file);
                }
            }

            // ソート
            Collections.sort(nameList);

            // ファイル情報リスト
            this.viewFileDataList = new ArrayList<File>();

            for (String name : nameList) {

                this.viewFileDataList.add(map.get(name));
            }
        }

        // ダイアログを生成
        AlertDialog.Builder dialog = new AlertDialog.Builder(this.activity, R.style.MyFileDialogStyle);
        dialog.setTitle(dirPath);
        //dialog.setIcon(R.drawable.file);
        dialog.setItems(nameList.toArray(new String[0]), this);

        dialog.setPositiveButton(R.string.file_select_to_parent, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int value) {

                if (!"/".equals(dirPath)) {

                    String dirPathNew = dirPath.substring(0, dirPath.length() - 1);
                    dirPathNew = dirPathNew.substring(0, dirPathNew.lastIndexOf("/") + 1);

                    // 履歴を追加
                    FileSelectDialog.this.viewPathHistory.add(dirPathNew);

                    // 1つ上へ
                    show(dirPathNew);

                } else {

                    // 現状維持
                    show(dirPath);
                }
            }
        });

        dialog.setNeutralButton(R.string.file_select_back, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int value) {

                int index = FileSelectDialog.this.viewPathHistory.size() - 1;

                if (index > 0) {

                    // 履歴を削除
                    FileSelectDialog.this.viewPathHistory.remove(index);

                    // 1つ前に戻る
                    show(FileSelectDialog.this.viewPathHistory.get(index - 1));

                } else {

                    // 現状維持
                    show(dirPath);
                }
            }
        });

        dialog.setNegativeButton(R.string.file_select_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int value) {

                FileSelectDialog.this.listener.onClickFileSelect(null);
            }
        });

        dialog.show();
    }

    /**
     * リスナーを設定
     *
     * @param listener 選択イベントリスナー
     */
    public void setOnFileSelectDialogListener(OnFileSelectDialogListener listener) {

        this.listener = listener;
    }

    /**
     * ボタン押下インターフェース
     */
    public interface OnFileSelectDialogListener {

        /**
         * 選択イベント
         *
         * @param file ファイル
         */
        public void onClickFileSelect(File file);
    }
}