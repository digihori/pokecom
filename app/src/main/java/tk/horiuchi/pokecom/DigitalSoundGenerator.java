package tk.horiuchi.pokecom;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Created by yoshimine on 2017/10/21.
 */

public class DigitalSoundGenerator {

    // とりあえず１オクターブ分の音階を確保（半音階含む）
//    public static final double FREQ_A  = 220.0;
//    public static final double FREQ_As = 233.081880;
//    public static final double FREQ_B  = 246.941650;
//    public static final double FREQ_C  = 261.625565;
//    public static final double FREQ_Cs = 277.182630;
//    public static final double FREQ_D  = 293.664767;
//    public static final double FREQ_Ds = 311.126983;
//    public static final double FREQ_E  = 329.627556;
//    public static final double FREQ_F  = 349.228231;
//    public static final double FREQ_Fs = 369.994227;
//    public static final double FREQ_G  = 391.994535;
//    public static final double FREQ_Gs = 415.304697;

    private AudioTrack audioTrack;

    // サンプリング周波数
    private int sampleRate;
    // バッファ・サイズ
    private int bufferSize;

    /*
     * コンストラクタ
     */
    public DigitalSoundGenerator(int sampleRate, int bufferSize) {
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;

        // AudioTrackを作成
        this.audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,  // 音楽ストリームを設定
                sampleRate, // サンプルレート
                AudioFormat.CHANNEL_OUT_MONO, // モノラル
                AudioFormat.ENCODING_DEFAULT,   // オーディオデータフォーマットPCM16とかPCM8とか
                bufferSize, // バッファ・サイズ
                AudioTrack.MODE_STREAM); // Streamモード。データを書きながら再生する
    }

    /*
     * サウンド生成
     * @param frequency 鳴らしたい音の周波数
     * @param soundLengh 音の長さ
     * @return 音声データ
     */
    public byte[] getSound(double frequency, double soundLength) {
        // byteバッファを作成
        byte[] buffer = new byte[(int)Math.ceil(bufferSize * soundLength)];
        for(int i=0; i<buffer.length; i++) {
            double wave = i / (this.sampleRate / frequency) * (Math.PI * 2);
            wave = Math.sin(wave);
            buffer[i] = (byte)(wave > 0.0 ? Byte.MAX_VALUE : Byte.MIN_VALUE);
        }

        return buffer;
    }

    /*
     * いわゆる休符
     * @param frequency
     * @param soundLength
     * @return 無音データ
     */
    public byte[] getEmptySound(double soundLength) {
        byte[] buff = new byte[(int)Math.ceil(bufferSize * soundLength)];

        for(int i=0; i<buff.length; i++) {
            buff[i] = (byte)0;
        }
        return buff;
    }

    /*
     *
     * @return
     */
    public AudioTrack getAudioTrack() {
        return this.audioTrack;
    }
}
