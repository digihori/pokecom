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

    // Sound生成クラス
    private DigitalSoundGenerator soundGenerator;
    // Sound再生クラス
    private AudioTrack audioTrack = null;

    public Beep() {
        // SoundGeneratorクラスをサンプルレート44100で作成
        soundGenerator = new DigitalSoundGenerator(44100, 44100);
        //soundGenerator = new DigitalSoundGenerator(8000, 8000);

        // 再生用AudioTrackは、同じサンプルレートで初期化したものを利用する
        audioTrack = soundGenerator.getAudioTrack();

        isActive = true;
        thread = new Thread(this);
        thread.start();
    }

    public void stopThread() {
        isActive = false;
    }


    private int beep_2k_cnt = 0;
    private int beep_4k_cnt = 0;
    private int beep_off_cnt = 1;
    public boolean beep_2k;
    public boolean beep_4k;
    private int beep_state = 0;
    private int last_beep_state = 0;
    private boolean beep = false;

    public void count() {
        beepMain();
    }

    private class QueObj {
        private int type;
        private int count;
    }
    private Queue queue = new Queue(16);

    public void beep2k() {
        beep_2k = true;
        beep_4k = false;
    }
    public void beep4k() {
        beep_2k = false;
        beep_4k = true;
    }
    public void beepOff() {
        beep_2k = false;
        beep_4k = false;
    }
    private void beepMain() {
        if (beep_2k) {
            beep_state = 2;
            if (beep_2k_cnt < 255) beep_2k_cnt++;
        } else if (beep_4k) {
            beep_state = 4;
            if (beep_4k_cnt < 255) beep_4k_cnt++;
        } else {
            beep_state = 0;
            if (beep_off_cnt < 255) beep_off_cnt++;
        }
        if (last_beep_state != beep_state) {
            QueObj obj = new QueObj();
            switch (last_beep_state) {
                default:
                case 0:
                    obj.type = 0;
                    obj.count = beep_off_cnt;
                    queue.offer(obj);
                    break;
                case 2:
                    obj.type = 2;
                    obj.count = beep_2k_cnt;
                    queue.offer(obj);
                    break;
                case 4:
                    obj.type = 4;
                    obj.count = beep_4k_cnt;
                    queue.offer(obj);
                    break;
            }
            last_beep_state = beep_state;
            beep_2k_cnt = 0;
            beep_4k_cnt = 0;
            beep_off_cnt = 0;
        }

        if (queue.getNumOfData() > 4) {
            // キューに４個以上溜まっていたら取り出して中身を解析する
            Log.w("queue", String.format("getNumOfData"));
            QueObj obj[] = new QueObj[4];
            int sum=0, j=0;
            for (int i=0; i < 4; i++) {
                obj[i] = (QueObj) queue.poll();
                if (obj[i] == null) {
                    Log.w("queue", String.format("obj[%d] = null", i));
                } else {
                    Log.w("queue", String.format("obj[%d].type=%d count=%d", i, obj[i].type, obj[i].count));
                }
                if (obj[i] != null && obj[i].type != 0 /*&& obj[i].count < 256*/) {
                    sum += obj[i].count;
                    j++;
                }
            }
            if (j > 0) {
                int ave = sum / j;
                if (!beep) {
                    Log.w("queue", String.format("call -> calcFrequency"));
                    calcFrequency(ave);
                }
            }
        }
    }

    private double freq;
    private void calcFrequency(int puls_count) {
        freq = 15000 / (puls_count + 66 / puls_count * 2);
        //if (freq < 150 || 700 < freq) freq = 2000;
        Log.w("-----calc freq-----", String.format("puls_count=%d freq=%f", puls_count, freq));
        beep = true;
    }

    /*
    public void _2000Hz() {
        audioTrack.flush();
        byte[] sound = soundGenerator.getSound(2000, 0.500);
        audioTrack.write(sound, 0, sound.length);
        audioTrack.play();
        Log.w("beep", String.format("2000Hz 0.5sec"));
        audioTrack.stop();
    }
     */

    public void run() {
        while (thread != null && this.isActive) {
            if (beep == true) {
                audioTrack.flush();
                byte[] sound = soundGenerator.getSound(freq, 0.020);
                audioTrack.write(sound, 0, sound.length);
                audioTrack.play();
                Log.w("beep", String.format("freq=%f length=%d", freq, sound.length));
                audioTrack.stop();
                beep = false;
            }

        }
        Log.w("Beep", "thread finished.");
    }

}
