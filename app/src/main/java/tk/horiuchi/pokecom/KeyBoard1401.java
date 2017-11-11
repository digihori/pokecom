package tk.horiuchi.pokecom;

/**
 * Created by yoshimine on 2017/07/29.
 */

public class KeyBoard1401 extends KeyboardBase {


    public KeyBoard1401() {
        super();

        KEYNM = 14;
        keym = new int[KEYNM];

        // スキャンテーブルの初期化
        scandef = new int[] {
                R.id.buttonPM,R.id.button8,R.id.button2,R.id.button5,R.id.buttonCAL,R.id.buttonQ,R.id.buttonA,R.id.buttonZ,
                R.id.buttonDOT,R.id.button9,R.id.button3,R.id.button6,R.id.buttonBASIC,R.id.buttonW,R.id.buttonS,R.id.buttonX,
                R.id.buttonPLS,R.id.buttonDIV,R.id.buttonMINUS,R.id.buttonMLT,R.id.buttonDEF,R.id.buttonE,R.id.buttonD,R.id.buttonC,
                R.id.buttonK2,R.id.buttonK1,R.id.buttonSQU,R.id.buttonROOT,R.id.buttonPOW,R.id.buttonEXP,R.id.buttonXM,R.id.buttonEQ,
                R.id.buttonUD,R.id.buttonREC,R.id.buttonLOG,R.id.buttonLN,R.id.buttonDEG,R.id.buttonHEX,   KNUL,   R.id.buttonMP,
                R.id.buttonCE,R.id.buttonFE,R.id.buttonTAN,R.id.buttonCOS,R.id.buttonSIN,R.id.buttonHYP,R.id.buttonSHIFT,R.id.buttonRM,
                KNUL,   KNUL,   KNUL,   KNUL,   KNUL,   KNUL,   KNUL,   KNUL,
                KNUL,R.id.button7,R.id.button1,R.id.button4,R.id.buttonDA,R.id.buttonR,R.id.buttonF,R.id.buttonV,
                KNUL,KNUL,R.id.buttonCOMMA,R.id.buttonP,R.id.buttonUA,R.id.buttonT,R.id.buttonG,R.id.buttonB,
                KNUL,   KNUL,   KNUL,   R.id.buttonO,   R.id.buttonLA,   R.id.buttonY,   R.id.buttonH,   R.id.buttonN,
                KNUL,   KNUL,   KNUL,   KNUL,   R.id.buttonRA,   R.id.buttonU,   R.id.buttonJ,   R.id.buttonM,
                KNUL,   KNUL,   KNUL,   KNUL,   KNUL,   R.id.buttonI,   R.id.buttonK,   R.id.buttonSPC,
                KNUL,   KNUL,   KNUL,   KNUL,   KNUL,   KNUL,   R.id.buttonL,   R.id.buttonENTER,
                KNUL,   KNUL,   KNUL,   KNUL,   KNUL,   KNUL,   KNUL,   R.id.button0};

    }
}
