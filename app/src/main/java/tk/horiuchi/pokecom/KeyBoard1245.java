package tk.horiuchi.pokecom;

/**
 * Created by yoshimine on 2017/07/29.
 */

public class KeyBoard1245 extends KeyboardBase {

    public KeyBoard1245() {
        super();

        KEYNM = 11;
        //keym = new int[KEYNM];

        // スキャンテーブルの初期化
        scandef = new int[] {
                R.id.buttonMINUS,R.id.buttonCE,R.id.buttonMLT,R.id.buttonDIV,R.id.buttonDA,R.id.buttonE,R.id.buttonD,R.id.buttonC,
                R.id.buttonPLS,R.id.button9,R.id.button3,R.id.button6,R.id.buttonSHIFT,R.id.buttonW,R.id.buttonS,R.id.buttonX,
                R.id.buttonDOT,R.id.button8,R.id.button2,R.id.button5,R.id.buttonDEF,R.id.buttonQ,R.id.buttonA,R.id.buttonZ,
                KNUL,R.id.button7,R.id.button1,R.id.button4,R.id.buttonUA,R.id.buttonR,R.id.buttonF,R.id.buttonV,
                KNUL,KNUL,R.id.buttonEQ,R.id.buttonP,R.id.buttonLA,R.id.buttonT,R.id.buttonG,R.id.buttonB,
                KNUL,KNUL,KNUL,R.id.buttonO,R.id.buttonRA, R.id.buttonY,R.id.buttonH,R.id.buttonN,
                KNUL,KNUL,KNUL,KNUL,KNUL, R.id.buttonU,R.id.buttonJ,R.id.buttonM,
                KNUL,KNUL,KNUL,KNUL,KNUL, R.id.buttonI,R.id.buttonK,R.id.buttonSPC,
                KNUL,KNUL,KNUL,KNUL,KNUL, KNUL,R.id.buttonL,R.id.buttonENTER,
                KNUL,KNUL,KNUL,KNUL,KNUL, KNUL,KNUL,R.id.button0,
                KNUL,KNUL,KNUL,KNUL,KNUL, KNUL,KNUL,KNUL};

        mBtnStatus = new int[KEYNM];
        for (int i = 0; i < KEYNM; i++) {
            mBtnStatus[i] = 0;
        }

    }
}
