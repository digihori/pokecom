package tk.horiuchi.pokecom;

/**
 * Created by yoshimine on 2017/07/30.
 */

public class KeyBoard1350 extends KeyboardBase {
    public KeyBoard1350() {
        super();

        KEYNM = 12;
        //keym = new int[KEYNM];

        // スキャンテーブルの初期化
        scandef = new int[] {
                R.id.buttonK2,R.id.buttonCLN,R.id.buttonSCLN,R.id.buttonCOMMA,R.id.buttonKANA,R.id.buttonDEF,R.id.buttonSHIFT,KNUL,
                R.id.buttonK1,R.id.buttonDIV,R.id.buttonMLT,R.id.buttonMINUS,R.id.buttonZ,R.id.buttonA,R.id.buttonQ,KNUL,
                R.id.button9,R.id.button6,R.id.button3,R.id.buttonPLS,R.id.buttonX,R.id.buttonS,R.id.buttonW,KNUL,
                R.id.button8,R.id.button5,R.id.button2,R.id.buttonDOT,R.id.buttonC,R.id.buttonD,R.id.buttonE,KNUL,
                R.id.button7,R.id.button4,R.id.button1,R.id.button0,R.id.buttonV,R.id.buttonF,R.id.buttonR,KNUL,
                R.id.buttonUA,R.id.buttonDA,R.id.buttonLA,R.id.buttonRA,R.id.buttonB,R.id.buttonG,R.id.buttonT,KNUL,
                KNUL,   KNUL,   KNUL,   KNUL,   KNUL,   KNUL,   KNUL,   KNUL,
                KNUL,KNUL,R.id.buttonINS,R.id.buttonDEL,R.id.buttonN,R.id.buttonH,R.id.buttonY,KNUL,
                KNUL,KNUL,KNUL,R.id.buttonMODE,R.id.buttonM,R.id.buttonJ,R.id.buttonU,KNUL,
                KNUL,   KNUL,   KNUL,   R.id.buttonCE,   R.id.buttonSPC,   R.id.buttonK,   R.id.buttonI,   KNUL,
                KNUL,   KNUL,   KNUL,   KNUL,   R.id.buttonENTER,   R.id.buttonL,   R.id.buttonO,   KNUL,
                KNUL,   KNUL,   KNUL,   KNUL,   KNUL,   R.id.buttonEQ,   R.id.buttonP,   KNUL
        };

        mBtnStatus = new int[KEYNM];
        for (int i = 0; i < KEYNM; i++) {
            mBtnStatus[i] = 0;
        }
    }

}
