package tk.horiuchi.pokecom;

/**
 * Created by yoshimine on 2017/07/29.
 */

public class KeyBoard1460 extends KeyboardBase {


    public KeyBoard1460() {
        super();

        KEYNM = 13;
        //keym = new int[KEYNM];

        // スキャンテーブルの初期化
        scandef = new int[] {
                R.id.button7,R.id.button8,R.id.button9,R.id.buttonDIV,R.id.buttonXM,KNUL,KNUL,KNUL,
                R.id.button4,R.id.button5,R.id.button6,R.id.buttonMLT,R.id.buttonRM,R.id.buttonSHIFT,R.id.buttonDEF,R.id.buttonSML,
                R.id.button1,R.id.button2,R.id.button3,R.id.buttonMINUS,R.id.buttonMP,R.id.buttonQ,R.id.buttonA,R.id.buttonZ,
                R.id.button0,R.id.buttonPM,R.id.buttonDOT,R.id.buttonPLS,R.id.buttonEQ,R.id.buttonW,R.id.buttonS,R.id.buttonX,
                R.id.buttonHYP,R.id.buttonSIN,R.id.buttonCOS,R.id.buttonTAN,R.id.buttonKANA,R.id.buttonE,R.id.buttonD,R.id.buttonC,
                R.id.buttonHEX,R.id.buttonDEG,R.id.buttonLN,R.id.buttonLOG,R.id.buttonDAK,R.id.buttonR,R.id.buttonF,R.id.buttonV,
                R.id.buttonEXP,R.id.buttonPOW,R.id.buttonROOT,R.id.buttonSQU,R.id.buttonCHO,R.id.buttonT,R.id.buttonG,R.id.buttonB,
                KNUL,R.id.buttonCE,KNUL,KNUL,R.id.buttonDA,R.id.buttonY,R.id.buttonH,R.id.buttonN,
                KNUL,   KNUL,R.id.buttonK2,R.id.buttonREC,R.id.buttonUA,R.id.buttonU,R.id.buttonJ,R.id.buttonM,
                KNUL,   KNUL,   KNUL,   R.id.buttonK1,   R.id.buttonLA,   R.id.buttonI,   R.id.buttonK,   R.id.buttonSPC,
                KNUL,   KNUL,   KNUL,   KNUL,   R.id.buttonRA,   R.id.buttonO,   R.id.buttonL,   R.id.buttonENTER,
                KNUL,   KNUL,   KNUL,   KNUL,   KNUL,   R.id.buttonP,   R.id.buttonCOMMA,   R.id.buttonBASIC,
                KNUL,   KNUL,   KNUL,   KNUL,   KNUL,   KNUL,   R.id.buttonLOCK,   R.id.buttonCAL
        };

        mBtnStatus = new int[KEYNM];
        for (int i = 0; i < KEYNM; i++) {
            mBtnStatus[i] = 0;
        }
    }
}
