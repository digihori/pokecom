package tk.horiuchi.pokecom;

import android.media.AudioTrack;
import android.util.Log;

import static tk.horiuchi.pokecom.Sc61860Base.beep_freq;

/**
 * Created by yoshimine on 2017/07/11.
 */

public class Beep extends Thread {
    private static boolean thread_active = true;
    private Thread thread = null;
    private boolean isActive;
    public static boolean beep_on = false;
    private boolean beep_active = false;
    private int total_cnt = 0;
    private boolean beep_on_bak = false;
    private double beep_freq_bak = 0;

    public static final double EIGHTH_NOTE = 0.125;
    public static final double FORTH_NOTE = 0.25;
    public static final double HALF_NOTE = 0.5;
    public static final double WHOLE_NOTE = 1.0;

    // Sound生成クラス
    private DigitalSoundGenerator soundGenerator;
    // Sound再生クラス
    private AudioTrack audioTrack;

    public Beep() {
        // SoundGeneratorクラスをサンプルレート44100で作成
        soundGenerator = new DigitalSoundGenerator(44100, 44100);

        // 再生用AudioTrackは、同じサンプルレートで初期化したものを利用する
        audioTrack = soundGenerator.getAudioTrack();

        isActive = true;
        thread = new Thread(this);
        thread.start();

    }

    public void stopThread() {
        isActive = false;
    }

    /*
    @Override
    public void finalize() throws Throwable {
        Log.w("Beep", "finalize called.");
        try {
            super.finalize();
        } finally {
            destruction();

        }
    }

    public void destruction() {
        thread_active = false;
        // 再生中だったら停止してリリース
        if(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
            audioTrack.release();
        }
        Log.w("Beep", "destruction called.");
    }
    */


    /*
     * ８ビットのピコピコ音を生成する.
     * @param gen Generator
     * @param freq 周波数(音階)
     * @param length 音の長さ
     * @return 音データ
     */
    public byte[] generateSound(DigitalSoundGenerator gen, double freq, double length) {
        return gen.getSound(freq, length);
    }

    /*
     * 無音データを作成する
     * @param gen Generator
     * @param length 無音データの長さ
     * @return 無音データ
     */
    public byte[] generateEmptySound(DigitalSoundGenerator gen, double length) {
        return gen.getEmptySound(length);
    }

    public void run() {
        while (thread != null && this.isActive) {
            if (!beep_active) {
                try {
                    Thread.sleep(20L);
                } catch (InterruptedException e) {
                    ;
                }
                if (beep_on) {
                    beep_active = true;
                    total_cnt = 0;
                    beep_on_bak = false;
                    Log.w("Beep", "task waked");
                }
            } else {
                if (beep_on_bak && !beep_on) {  // on -> off
                    beep_on_bak = beep_on;

                    //if (beep_freq_bak != beep_freq) {
                    //    beep_freq_bak = beep_freq;
                        // 再生中なら一旦止める
                        //if(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                        //    audioTrack.stop();
                        //    audioTrack.reloadStaticData();
                        //}

                        SoundDto dto;
                        if (beep_freq == 2000) {
                            dto = new SoundDto(generateSound(soundGenerator, (int) beep_freq, FORTH_NOTE), FORTH_NOTE);
                        } else {
                            dto = new SoundDto(generateSound(soundGenerator, (int) beep_freq, EIGHTH_NOTE), EIGHTH_NOTE);
                        }
                        audioTrack.write(dto.getSound(), 0, dto.getSound().length);

                        // 再生開始
                        audioTrack.play();
                        // 再生停止
                        audioTrack.stop();

                    //}

                } else if (!beep_on_bak && beep_on) {   // off -> on
                    beep_on_bak = beep_on;
                } else {
                    ;
                }

                // OFFがしばらく続いたら一旦おしまい
                if (!beep_on) {
                    total_cnt++;
                    if (total_cnt > 10000000) {
                        //beep_active = false;
                        //Log.w("Beep", "task sleeped");
                    }
                } else {
                    total_cnt = 0;
                }

            }
        }
        Log.w("Beep", "thread finished.");
    }

}
